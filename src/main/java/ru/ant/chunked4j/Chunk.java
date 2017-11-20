package ru.ant.chunked4j;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

public class Chunk implements Comparable<Chunk> {
    private Logger log = LogManager.getLogger();

    private String dzUuid;
    private BigInteger dzChunkIndex;
    private BigInteger dzTotalFileSize;
    private BigInteger dzChunkSize;
    private BigInteger dzTotalChunkCount;
    private BigInteger dzChunkByteOffset;
    private byte[] chunkData;
    private String fileName;
    private boolean processed;

    Chunk(HttpServletRequest request) throws IOException, FileUploadException {
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if(!isMultipart)
            throw new RuntimeException("Multipart data expected");

        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        ServletFileUpload upload = new ServletFileUpload();

        FileItemIterator iter = upload.getItemIterator(request);
        while (iter.hasNext()) {
            FileItemStream item = iter.next();
            String fieldName = item.getFieldName();
            InputStream stream = item.openStream();
            if (item.isFormField()) {
                String value = Streams.asString(stream, StandardCharsets.UTF_8.name());
                setFormField(fieldName, value);
            } else {
                if(!fieldName.equals("file"))
                    throw new RuntimeException("File content in field named \""+fieldName+"\" was ignored. Content field name must be \"file\"");
                fileName = item.getName();
                chunkData = IOUtils.toByteArray(stream);
            }
        }
    }

    String getFileId(){
        return dzUuid;
    }

    public String getDzUuid() {
        return dzUuid;
    }

    BigInteger getDzChunkIndex() {
        return dzChunkIndex;
    }

    BigInteger getDzChunkSize() {
        return dzChunkSize;
    }

    BigInteger getDzTotalChunkCount() {
        return dzTotalChunkCount;
    }

    public BigInteger getDzChunkByteOffset() {
        return dzChunkByteOffset;
    }

    String getFileName() {
        return fileName;
    }

    byte[] getChunkData() {
        return chunkData;
    }

    @Override
    public String toString() {
        return String.format("%1$s{fileName=[%2$s], fileId=[%3$s], num=[%4$s of %5$s], bytes=[%6$s of %7$s]}", getClass().getSimpleName(), fileName, dzUuid, dzChunkIndex, dzTotalChunkCount, dzChunkSize, dzTotalFileSize);
    }

    private void setFormField(String fieldName, String value) {
        switch (fieldName){
            case "dzuuid":
                dzUuid = value;
                break;
            case "dzchunkindex":
                dzChunkIndex = new BigInteger(value);
                break;
            case "dztotalfilesize":
                dzTotalFileSize = new BigInteger(value);
                break;
            case "dzchunksize":
                dzChunkSize = new BigInteger(value);
                break;
            case "dztotalchunkcount":
                dzTotalChunkCount = new BigInteger(value);
                break;
            case "dzchunkbyteoffset":
                dzChunkByteOffset = new BigInteger(value);
                break;
            default:
                log.warn(String.format("Form field [%1$s=%2$s] ignored by %3$s", fieldName, value, getClass().getSimpleName()));
        }

    }

    @Override
    public int compareTo(Chunk o) {
        return dzChunkIndex.compareTo(o.dzChunkIndex);
    }

    public boolean isProcessed() {
        return processed;
    }
    void setProcessed(boolean processed) {
        this.processed = processed;
    }
}
