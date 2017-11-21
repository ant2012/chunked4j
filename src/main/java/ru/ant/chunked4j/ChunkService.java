package ru.ant.chunked4j;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChunkService {
    private static ChunkService instance = new ChunkService();
    public static ChunkService getInstance() {
        return instance;
    }
    private ChunkService() {
    }

    private final Logger log = LogManager.getLogger();

    private Map<String, ChunkInputStream> streamMap = new HashMap<>();
    private List<ChunkStreamListener> listeners = new ArrayList<>();

    public void putChunk(HttpServletRequest request) throws ChunkException {
        try {
            Chunk chunk = new Chunk(request);
            ChunkInputStream stream = getStream(chunk);
            stream.putChunk(chunk);
        } catch (Exception e) {
            throw new ChunkException(e);
        }
    }

    private synchronized ChunkInputStream getStream(Chunk chunk) throws IOException, ChunkException {
        ChunkInputStream stream = streamMap.get(chunk.getFileId());
        if(stream != null) return stream;

        stream = new ChunkInputStream(chunk);
        streamMap.put(chunk.getFileId(), stream);
        notifyNewStreamAppeared(stream);
        return stream;
    }

    private void notifyNewStreamAppeared(ChunkInputStream stream) throws ChunkException {
        for (ChunkStreamListener listener : listeners) {
            listener.newStreamAppeared(stream);
        }
    }


    private void addStreamListener(ChunkStreamListener listener) {
        log.info(String.format("Registering %1$s", listener));
        listeners.add(listener);
    }

    public <S extends Serializable, R extends ChunkStreamReader<S>> void registerStreamReaderFactory(ChunkStreamReaderFactory<R> streamReaderFactory) {
        addStreamListener(new ChunkStreamListener<>(streamReaderFactory));
    }

    public Serializable getUploadResult(String fileId) {
        for (ChunkStreamListener listener : listeners) {
            Serializable result = listener.getUploadResult(fileId);
            if(result != null) return result;
        }
        return null;
    }
}
