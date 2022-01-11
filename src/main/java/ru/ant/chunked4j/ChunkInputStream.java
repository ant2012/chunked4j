package ru.ant.chunked4j;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * This {@link InputStream} extension collects incoming chunked requests<br/>
 * sort them by chunkIndex and provide standard {@link InputStream} access to incoming data<br/>
 * Streams always created internally in {@link ChunkService}<br/>
 */
public class ChunkInputStream extends InputStream {
    private final static int QUEUE_PEEK_TIMEOUT = 500;//ms
    private final static int QUEUE_PUT_TIMEOUT = 10;//ms
    private final static int QUEUE_MAX_CAPACITY = 100*1024*1024;//100Mb

    private final Logger log = LogManager.getLogger();
    private final PriorityBlockingQueue<Chunk> queue = new PriorityBlockingQueue<>();
    private final Chunk initialChunk;

    private long nextChunkIndex = 0;
    private byte[] buffer = null;
    private int bufferIndex = -1;
    private long acceptedChunkCount;
    private boolean allChunksAccepted;

    ChunkInputStream(Chunk chunk) {
        super();
        initialChunk = chunk;
    }

    @Override
    public int available() {
        return buffer.length + queue.size() * initialChunk.getChunkSize().intValue();
    }

    @Override
    public int read() {
        int b = getByteFromBuffer();
        if(b!=-1) return b;

        long availableChunkIndex = -1;
        Chunk currentChunk;
        while(availableChunkIndex!=nextChunkIndex){
            currentChunk = queue.peek();
            if(currentChunk == null && allChunksAccepted) return -1;//Stream is finished
            if(currentChunk != null)
                availableChunkIndex = currentChunk.getChunkIndex().longValue();
            if(availableChunkIndex!=nextChunkIndex){
                try {
                    log.debug("Queue peek timeout");
                    //noinspection BusyWait
                    Thread.sleep(QUEUE_PEEK_TIMEOUT);
                } catch (InterruptedException e) {
                    log.error(e);
                }
            }
        }
        currentChunk = queue.poll();
        //noinspection ConstantConditions
        buffer = currentChunk.getChunkData();
        nextChunkIndex++;

        return getByteFromBuffer();
    }

    private int getByteFromBuffer() {
        if(buffer!=null){
            bufferIndex++;
            if(bufferIndex == buffer.length){
                bufferIndex = -1;
                buffer = null;
                return -1;
            }else{
                return Byte.toUnsignedInt(buffer[bufferIndex]);
            }
        }
        return -1;
    }

    void putChunk(Chunk chunk) {

        if(allChunksAccepted)
            throw new IllegalStateException(String.format("Stream initialised for %1$s already accepted %2$s chunks and can not accept currentChunk %3$s", initialChunk, acceptedChunkCount, chunk));

        if(!chunk.getFileId().equals(initialChunk.getFileId()))
            throw new IllegalArgumentException(String.format("Stream initialised for %1$s can not accept currentChunk %2$s", initialChunk, chunk));

        while((queue.size() + 1) * initialChunk.getChunkSize().intValue() > QUEUE_MAX_CAPACITY){
            try {
                log.debug("Queue put timeout");
                //noinspection BusyWait
                Thread.sleep(QUEUE_PUT_TIMEOUT);
            } catch (InterruptedException e) {
                log.error(e);
            }
        }
        queue.put(chunk);
        acceptedChunkCount++;
        log.debug(String.format("Received %1$.2f%% of %2$s", (double)acceptedChunkCount/chunk.getTotalChunkCount().longValue()*100, getFileName()));
        if(acceptedChunkCount == initialChunk.getTotalChunkCount().longValue()){
            allChunksAccepted = true;
            log.info("All chunks received");
        }
    }

    /**
     * Part of initial chunk metadata
     * @return file name
     */
    public String getFileName() {
        return initialChunk.getFileName();
    }

    /**
     * Part of initial chunk metadata
     * @return content type
     */
    public String getContentType() {
        return initialChunk.getContentType();
    }

    /**
     * Part of initial chunk metadata
     * @param fieldName form field name
     * @return form field value
     */
    public String getRequestFormField(String fieldName){
        return initialChunk.getRequestFormField(fieldName);
    }

    /**
     * Part of initial chunk metadata
     * @param paramName query param name
     * @return request query param value
     */
    public String getRequestQueryParam(String paramName){
        return initialChunk.getRequestQueryParam(paramName);
    }

    /**
     * Part of initial chunk metadata
     * @param attributeName request attribute name
     * @return request attribute value
     */
    public Object getRequestAttribute(String attributeName){
        return initialChunk.getRequestAttribute(attributeName);
    }

    String getFileId() {
        return initialChunk.getFileId();
    }

}
