package ru.ant.chunked4j;

import java.io.Serializable;

public class ChunkUploadResult<R extends Serializable> {
    private final R result;

    public ChunkUploadResult(R result) {
        this.result = result;
    }

    public R getResult() {
        return result;
    }
}
