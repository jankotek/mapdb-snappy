/** Apache 2.0 licence */

package org.mapdb.snappy;

import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * An Serializer wrapper which compress output of other serializer
 */
public class SnappySerializer<A> implements Serializer<A>, Serializable {

    private static final long serialVersionUID = 8929893893832982388L;

    protected final Serializer<A> ser;

    protected final boolean storeSize;

    /**
     * Construct new Snappy serializer instance
     *
     * @param ser serializer whose input/output will be (de)compressed
     * @param storeSize true if compressed size is stored as part of record, must be true when used as part of maps or other collections
     */
    public SnappySerializer(Serializer ser, boolean storeSize) {
        this.ser = ser;
        this.storeSize = storeSize;
    }


    transient protected Queue<DataOutput2> recycledDataOuts = new ArrayBlockingQueue<DataOutput2>(128);

    protected DataOutput2 newDataOut2() {
        if(recycledDataOuts==null) //if this get deserialized, than reinitialize
            recycledDataOuts = new ArrayBlockingQueue<DataOutput2>(128);

        DataOutput2 tmp = recycledDataOuts.poll();
        if(tmp==null) tmp = new DataOutput2();
        else tmp.pos=0;
        return tmp;
    }

    @Override
    public void serialize(DataOutput out, A value) throws IOException {
        //serialize object
        final DataOutput2 tmp1 = newDataOut2();
        ser.serialize(tmp1,value);

        //compress
        final DataOutput2 tmp2 = newDataOut2();
        tmp2.ensureAvail(tmp1.pos+40);
        int flag=0;
        int compresSize = 0;
        try{
            compresSize = Snappy.compress(tmp1.buf,0,tmp1.pos,tmp2.buf,0);
            flag = compresSize>tmp1.pos? 0 : tmp1.pos+1;
        }catch(Exception e){
            flag=0;
        }

        DataOutput2.packInt(out,flag);
        if(flag==0){
            //compression is ineffective
            out.write(tmp1.buf,0,tmp1.pos);
        }else{
            if(storeSize)
                DataOutput2.packInt(out, compresSize);
            //write compressed data
            out.write(tmp2.buf, 0, compresSize);
        }


        recycledDataOuts.offer(tmp1);
        recycledDataOuts.offer(tmp2);
    }

    @Override
    public A deserialize(DataInput in, int available) throws IOException {
        final DataInput2 in_ = (DataInput2) in;
        final int origPos = in_.pos;

        int flag = DataInput2.unpackInt(in);

        if(flag==0){
            //no compressed data
            if(available>0)
                available--;
            return ser.deserialize(in,available);
        }


        if(available==-1 && !storeSize)
            throw new IllegalArgumentException("Serializer "+getClass().getName()+" must have storeSize=true");

        if(storeSize){
            available = DataInput2.unpackInt(in);
        }


        //prepare buffers
        DataOutput2 tmp2 = newDataOut2();
        flag--;
        tmp2.ensureAvail(flag);

        ByteBuffer buf = in_.buf.duplicate();
        buf.position(in_.pos);
        buf.limit(in_.pos + available - (storeSize?0:(in_.pos-origPos)));
        in_.pos = buf.limit();
        ByteBuffer buf2 = ByteBuffer.wrap(tmp2.buf);

        //decompress
        Snappy.uncompress(buf,buf2);

        //deserialize
        DataInput2 in2 = new DataInput2(tmp2.buf);
        A a = ser.deserialize(in2,flag);

        //recycle byte[]
        recycledDataOuts.offer(tmp2);
        return a;
    }

    @Override
    public int fixedSize() {
        return -1;
    }
}
