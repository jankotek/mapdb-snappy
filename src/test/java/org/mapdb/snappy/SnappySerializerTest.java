package org.mapdb.snappy;

import org.junit.Test;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Engine;
import org.mapdb.Serializer;

import java.util.Map;

import static org.junit.Assert.*;

public class SnappySerializerTest {

    final String rec = "ad8989a89d89dk            qwdqwkd            dqwdqwkdkk asciasc909  dvvdf vd        ";

    @Test
    public void testRecord(){
        Engine db = DBMaker.newMemoryDB().transactionDisable().cacheDisable().makeEngine();
        Serializer<String> ser = new SnappySerializer(Serializer.STRING, false);
        long recid = db.put(rec,ser);

        String rec2 = db.get(recid,ser);
        assertEquals(rec,rec2);
    }

    @Test public void hash_map_key(){
        DB db = DBMaker.newMemoryDB().transactionDisable().cacheDisable().make();
        Map m = db.createHashMap("a")
            .keySerializer(new SnappySerializer(Serializer.STRING,true))
            .valueSerializer(new SnappySerializer(Serializer.STRING,true))
            .make();

        m.put(rec+"1", rec);
        m.put(rec+"2", rec);
        m.put(rec+"3", rec);

        assertEquals(m.get(rec+"1"),rec);
        assertEquals(m.get(rec+"2"),rec);
        assertEquals(m.get(rec+"3"),rec);
    }


    @Test public void tree_map_key(){
        DB db = DBMaker.newMemoryDB().transactionDisable().cacheDisable().make();
        Map m = db.createTreeMap("a")
//                .keySerializer(new SnappySerializer(Serializer.STRING))
                .valueSerializer(new SnappySerializer(Serializer.STRING,true))
                .make();

        m.put(rec+"1", rec);
        m.put(rec+"2", rec);
        m.put(rec+"3", rec);

        assertEquals(m.get(rec+"1"),rec);
        assertEquals(m.get(rec+"2"),rec);
        assertEquals(m.get(rec+"3"),rec);
    }
}