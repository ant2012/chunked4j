package ru.ant.chunked4j;

/**
 * Interface for factories, produces reader to process every chunked stream
 * Implement it in your application
 * @param <R> Reader, must extend the abstract ChunkStreamReader. It saves input stream data according to your application logic
 */
public interface ChunkStreamReaderFactory<R extends ChunkStreamReader> {
    /**
     * Creates reader for new chuncked input stream
     * @param stream New chunked input stream to process
     * @return Reader, saves stream data according to the your application logic
     * @throws Exception Any errors in user's stream reader creation
     */
    R create(ChunkInputStream stream) throws Exception;
}
