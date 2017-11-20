# chunked4j
Chunked upload server library for java

It serves [DropzoneJs's](http://www.dropzonejs.com/) chunking protocol

- Each dropzone's request is a chunk. Just put it to the `ChunkService` singletone
```java
protected void doPost(HttpServletRequest request, HttpServletResponse response)
{
    ChunkService.getInstance().putChunk(request);
}
```

Chunked4j provides `ChunkInputStream` to save your upload to local file or anywhere else.
Chunked4j uses your `StreamReaderFactory` interface implementation, that constructs new `ChunkStreamReader` runnable instance for each upload

- Register your `StreamReaderFactory` on application start
```java
public void contextInitialized(ServletContextEvent sce) {
    ChunkService.getInstance().registerStreamReaderFactory(new LocalFileWriterFactory());
}
```

Go [chunked4j-sample](https://github.com/ant2012/chunked4j-sample) to learn simple `StreamReaderFactory` implementation
