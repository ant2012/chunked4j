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
import java.util.HashMap;
import java.util.Map;

public class Chunk implements Comparable<Chunk> {
    private Logger log = LogManager.getLogger();

    private String fileId;
    private BigInteger chunkIndex;
    private BigInteger chunkSize;
    private BigInteger totalChunkCount;
    private byte[] chunkData;
    private String fileName;
    private boolean processed;

    private Map<String, String> requestFormFields = new HashMap<>();

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
                requestFormFields.put(fieldName, value);
            } else {
                if(!fieldName.equals("file"))
                    throw new RuntimeException("File content in field named \""+fieldName+"\" was ignored. Content field name must be \"file\"");
                fileName = item.getName();
                chunkData = IOUtils.toByteArray(stream);
            }
        }
    }

    public String getFileId() {
        return fileId;
    }

    public BigInteger getChunkIndex() {
        return chunkIndex;
    }

    public BigInteger getChunkSize() {
        return chunkSize;
    }

    public BigInteger getTotalChunkCount() {
        return totalChunkCount;
    }

    String getFileName() {
        return fileName;
    }

    byte[] getChunkData() {
        return chunkData;
    }

    @Override
    public String toString() {
        return String.format("%1$s{fileName=[%2$s], fileId=[%3$s], num=[%4$s of %5$s]}", getClass().getSimpleName(), fileName, fileId, chunkIndex, totalChunkCount);
    }

    private void setFormField(String fieldName, String value) {
        switch (fieldName){
            case "dzuuid":
                fileId = value;
                break;
            case "dzchunkindex":
                chunkIndex = new BigInteger(value);
                break;
            case "dzchunksize":
                chunkSize = new BigInteger(value);
                break;
            case "dztotalchunkcount":
                totalChunkCount = new BigInteger(value);
                break;
//            case "dztotalfilesize":
//                dzTotalFileSize = new BigInteger(value);
//                break;
//            case "dzchunkbyteoffset":
//                dzChunkByteOffset = new BigInteger(value);
//                break;
            default:
                log.debug(String.format("Form field [%1$s=%2$s] ignored by %3$s", fieldName, value, getClass().getSimpleName()));
        }
    }

    public String getRequestFormField(String fieldName){
        return requestFormFields.get(fieldName);
    }

    @Override
    public int compareTo(Chunk o) {
        return chunkIndex.compareTo(o.chunkIndex);
    }
}
