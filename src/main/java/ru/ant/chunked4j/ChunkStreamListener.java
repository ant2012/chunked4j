package ru.ant.chunked4j;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * It is package private class-wrapper for user's factory<br/>
 * It wraps factory to be streams listener<br/>
 * User's {@link ru.ant.chunked4j.ChunkStreamReaderFactory} implementation will receive new streams through this listener<br/>
 * @param <S> User's stream metadata class
 * @param <R> User's stream reader implementation class
 * @param <F> User's factory implementation class
 */
class ChunkStreamListener<S extends Serializable, R extends ChunkStreamReader<S>, F extends ChunkStreamReaderFactory<R>> {

    private final F readerFactory;
    private final Map<String, R> readers = new HashMap<>();

    ChunkStreamListener(F readerFactory) {
        this.readerFactory = readerFactory;
    }

    void newStreamAppeared(ChunkInputStream stream) throws Exception {
        R reader = readerFactory.create(stream);
        readers.put(stream.getFileId(), reader);
        new Thread(reader, String.format("%1$s-async{%2$s}", getClass().getSimpleName(), stream.getFileName())).start();
    }

    @Override
    public String toString() {
        return String.format("%1$s<%2$s>", getClass().getSimpleName(), readerFactory.getClass().getSimpleName());
    }

    S getUploadResult(String fileId) {
        R reader = readers.get(fileId);
        return reader.getResult();
    }
}
