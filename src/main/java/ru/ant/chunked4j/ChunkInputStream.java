package ru.ant.chunked4j;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.PriorityBlockingQueue;

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
    private Chunk currentChunk;

    ChunkInputStream(Chunk chunk) {
        super();
        initialChunk = chunk;
    }

    @Override
    public int available() throws IOException {
        return buffer.length + queue.size() * initialChunk.getChunkSize().intValue();
    }

    @Override
    public int read() throws IOException {
        int b = getByteFromBuffer();
        if(b!=-1) return b;

        long availableChunkIndex = -1;
        while(availableChunkIndex!=nextChunkIndex){
            currentChunk = queue.peek();
            if(currentChunk == null && allChunksAccepted) return -1;//Stream is finished
            if(currentChunk != null)
                availableChunkIndex = currentChunk.getChunkIndex().longValue();
            if(availableChunkIndex!=nextChunkIndex){
                try {
                    log.debug("Queue peek timeout");
                    Thread.sleep(QUEUE_PEEK_TIMEOUT);
                } catch (InterruptedException e) {
                    log.error(e);
                }
            }
        }
        currentChunk = queue.poll();
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

    public String getFileName() {
        return initialChunk.getFileName();
    }

    public String getContentType() {
        return initialChunk.getContentType();
    }

    public String getRequestFormField(String fieldName){
        return initialChunk.getRequestFormField(fieldName);
    }

    public String getRequestQueryParam(String paramName){
        return initialChunk.getRequestQueryParam(paramName);
    }

    public Object getRequestAttribute(String attributeName){
        return initialChunk.getRequestAttribute(attributeName);
    }

    String getFileId() {
        return initialChunk.getFileId();
    }

}
