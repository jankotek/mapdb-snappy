This library offers Serializer wrapper for MapDB, which wraps another serializer and compresses its output.

It uses [Snappy compression](https://code.google.com/p/snappy/) from Google. Compared to LZW it should offer
better compression at similar speed. However Snappy has more dependencies compared to LZW.

To use add this maven dependency:

```xml
    <dependencies>
        <dependency>
            <groupId>org.mapdb</groupId>
            <artifactId>mapdb-snappy</artifactId>
            <version>1.0</version>
        </dependency>
    </dependencies>
```

And add Serializer to a Map or other collection:

```java

    import org.mapdb.snappy.*;

    DB db = DBMaker.newMemoryDB().transactionDisable().cacheDisable().make();
    Map m = db.createHashMap("a")
            .keySerializer(new SnappySerializer(Serializer.STRING,true))
            .valueSerializer(new SnappySerializer(Serializer.STRING,true))
            .make();

```

Thi serializer can use two Snappy implementations:

 * [IQ80 pure java port](https://github.com/dain/snappy) from Dain Sundstrom.
 * [Xarial](https://github.com/xerial/snappy-java)  which uses native libraries.

We try to load  IQ80 implementation first and falls back to the xerial Snappy implementation it cannot be loaded.
You can change the  load order by setting the `mapdb.snappy` system property.  Example:

```
    -Dmapdb.snappy=xerial,iq80
```


TODO: enable as store wide serializer
TODO: BTreeKeySerializer

This library uses Snappy wrapper from [Hiram Chirino](http://hiramchirino.com)
