package ru.ant.chunked4j;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Chunked service singleton<br/>
 * Usage:<br/>
 * register your factory on application start: ChunkService.{@link #getInstance()}.{@link #registerStreamReaderFactory(ChunkStreamReaderFactory)};<br/>
 * when get chunked request use: chunkService.{@link #putChunk(HttpServletRequest)};
 */
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

    /**
     * Adds chunk request to chunked incoming stream<br/>
     * If stream does not exist yet - it will be created
     * @param request Your web server's http request
     * @throws Exception any exception in chunk creation
     */
    public void putChunk(HttpServletRequest request) throws Exception {
        Chunk chunk = new Chunk(request);
        ChunkInputStream stream = getStream(chunk);
        stream.putChunk(chunk);
    }

    private synchronized ChunkInputStream getStream(Chunk chunk) throws Exception {
        ChunkInputStream stream = streamMap.get(chunk.getFileId());
        if(stream != null) return stream;

        stream = new ChunkInputStream(chunk);
        streamMap.put(chunk.getFileId(), stream);
        notifyNewStreamAppeared(stream);
        return stream;
    }

    private void notifyNewStreamAppeared(ChunkInputStream stream) throws Exception {
        for (ChunkStreamListener listener : listeners) {
            listener.newStreamAppeared(stream);
        }
    }


    private void addStreamListener(ChunkStreamListener listener) {
        log.info(String.format("Registering %1$s", listener));
        listeners.add(listener);
    }

    /**
     * Register your factory as stream listener<br/>
     * Multiple listener registrations are allowed
     * @param streamReaderFactory user's factory implementing {@link ru.ant.chunked4j.ChunkStreamReaderFactory}
     * @param <S> User's stream metadata class
     * @param <R> User's stream reader implementation class
     */
    public <S extends Serializable, R extends ChunkStreamReader<S>> void registerStreamReaderFactory(ChunkStreamReaderFactory<R> streamReaderFactory) {
        addStreamListener(new ChunkStreamListener<>(streamReaderFactory));
    }

    /**
     * Finds finished chunked incoming stream by fileId<br/>
     * and returns it's metadata
     * @param fileId id of incoming file
     * @return Your metadata class instance or null, if no stream was found
     */
    public Serializable getUploadResult(String fileId) {
        for (ChunkStreamListener listener : listeners) {
            Serializable result = listener.getUploadResult(fileId);
            if(result != null) return result;
        }
        return null;
    }
}
