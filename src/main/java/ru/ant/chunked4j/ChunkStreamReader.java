package ru.ant.chunked4j;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;

/**
 * It is abstract reader<br/>
 * There is one reader instance for one chunked input stream<br/>
 * See the {@link ru.ant.chunked4j.ChunkStreamReaderFactory} interface to learn how to produce readers
 * @param <R> Result object class - optional, <br/>
 *           may be helpful to get some metadata from chunked input stream<br/>
 *           as file name, file size, checksum, some other http headers etc.
 */
public abstract class ChunkStreamReader<R extends Serializable> implements Runnable {
    protected final ChunkInputStream stream;
    protected final Logger log = LogManager.getLogger();

    /**
     * Constructor
     * @param stream incoming chunked stream
     */
    public ChunkStreamReader(ChunkInputStream stream) {
        this.stream = stream;
    }

    /**
     * This method may be helpful to provide some metadata, collected during the stream fetch
     * @return Your metadata class instance
     */
    public abstract R getResult();
}
