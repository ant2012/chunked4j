package ru.ant.chunked4j;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;

public abstract class ChunkStreamReader<R extends Serializable> implements Runnable {
    protected final ChunkInputStream stream;
    protected final Logger log = LogManager.getLogger();

    public ChunkStreamReader(ChunkInputStream stream) {
        this.stream = stream;
    }

    public abstract R getResult();
}
