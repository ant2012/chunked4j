package ru.ant.chunked4j;

import java.io.Serializable;

public abstract class ChunkStreamReader<R extends Serializable> implements Runnable {
    protected final ChunkInputStream stream;

    public ChunkStreamReader(ChunkInputStream stream) {
        this.stream = stream;
    }

    public abstract ChunkUploadResult<R> getResult();
}
