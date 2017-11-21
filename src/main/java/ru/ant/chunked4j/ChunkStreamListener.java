package ru.ant.chunked4j;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ChunkStreamListener<S extends Serializable, R extends ChunkStreamReader<S>, F extends ChunkStreamReaderFactory<R>> {

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
