# Prerequisites for AWS S3
1. enter key      <!--like primary key-->
2. enter bucket   <!--name for the top-level folder in cloudStorage-->
3. enter path     <!--path to store the source file-->

# Generate Key
```java
public class S3ObjectKeyBuilderUtil {

    public static final String SEPARATOR = "/";
    public static final DateTimeFormatter FILE_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    public static final String FILENAME_SEPARATOR = "_";

    private S3ObjectKeyBuilderUtil() {
    }

    public static String buildBlastTemplateImageKey(UUID blastId, String extension) {
        return RootDirType.NOTIFICATIONS_IMAGE.getPath() + "blast"
            + SEPARATOR + blastId
            + FILENAME_SEPARATOR + LocalDateTime.now().format(FILE_TIMESTAMP_FORMAT)
            + FILENAME_SEPARATOR + UUID.randomUUID().toString().substring(0, 8)
            + extension;
    }

}

```

# API Upload to cloud
```java
@RestController
@RequestMapping("/api/v1/file")
@RequiredArgsConstructor
@LogRequestResponse
@Slf4j
public class FileController {

    private final IFileService iFileService;
    private final AWSS3Configuration.StorageProperties storageProperties;

    @PostMapping(value = "/upload/preview-img", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> getPreviewImgUrl(@RequestPart("file") MultipartFile file) {
        var path = S3ObjectKeyBuilderUtil.buildBlastTemplateImageKey(
                UUID.randomUUID(),
                getExtension(file.getOriginalFilename())
            );

        // ☆ FOCUS - call upload method to upload file to cloud
        return ApiResponse.setSuccess(iFileService.upload(file, storageProperties.getPublicBucket(), path));
    }

    public static String getExtension(String originalFilename) {
        var extension = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            // Whitelist safe extensions only
            extension = extension.toLowerCase().replaceAll("[^a-z0-9.]", "");
        }

        return extension;
    }

}
```

# Upload Logic Blueprint
```java
public interface IFileService {
    /**
     * Shortcut
     * Upload from a {@link MultipartFile}. Content type and length are read from the
     * file itself — never pass them separately.
     *
     * @param file         multipart file from HTTP request
     * @param bucket       target bucket; {@code null} uses the configured default bucket
     * @param pathSupplier supplies the storage object key
     * @return the object key that was used
     */
    default String upload(MultipartFile file, String bucket, Supplier<String> pathSupplier) {
        return upload(UploadParams.forFile(file).bucket(bucket).path(pathSupplier).build());
    }

    /**
     * Shortcut — known path string.
     */
    default String upload(MultipartFile file, String bucket, String path) {
        return upload(file, bucket, () -> path);
    }

    /**
     * Actual Method
     * Upload using a pre-built {@link UploadParams}. This is the one non-default method
     * every implementation must provide; all other {@code upload(...)} overloads funnel
     * into this after building the appropriate {@link UploadParams}.
     *
     * @param params source, bucket, path, and (derived or explicit) content metadata
     * @return the object key that was used
     */
    String upload(UploadParams params);
}
```

# Upload Logic
```java

    /** Explicit bucket wins; blank/null falls back to the configured default. */
    private String resolveBucket(String bucket) {
        return Optional.ofNullable(StringUtils.trimToNull(bucket))
            .orElse(storageProperties.getBucket());
    }

    @Override
    public String upload(UploadParams params) {
        String path = params.getPath();
        String bucket = resolveBucket(params.getBucket());
        String contentType = params.getContentType();

        PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
            .bucket(bucket)
            .key(path)
            .contentType(contentType);

        RequestBody body; // different ways file data is held in memory (file/bytes/input stream)
        try {
            // If is file (A web-uploaded file coming directly from an HTTP request)
            if (params.getFile() != null) {
                // pass length so it knows how many bytes to expect over the stream
                long contentLength = params.getContentLength();
                requestBuilder.contentLength(contentLength);
                body = RequestBody.fromInputStream(params.getFile().getInputStream(), contentLength);
            } else if (params.getInputStream() != null) {
                // If is input stream (continuous flow that reads bytes piece by piece from a source)
                long contentLength = params.getContentLength();
                requestBuilder.contentLength(contentLength);
                body = RequestBody.fromInputStream(params.getInputStream(), contentLength);
            } else if (params.getBytes() != null) {
                // If is bytes (raw binary data stored entirely in an array inside the server's RAM)
                body = RequestBody.fromBytes(params.getBytes());
            } else {
                throw new ApiBadRequestException("UploadParams must carry a file, bytes, or input stream");
            }
        } catch (ApiBadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new FileUploadException("Failed to upload file", e);
        }

        s3Client.putObject(requestBuilder.build(), body);
        log.debug(UPLOADED_OBJECT_TO_PATH_LOG, path);

        return path;
    }
```


# Get File logic
```java
// Interface ---------------------------------

    /**
     * Generate a presigned GET URL with the default expiry, in a bucket.
     *
     * @param bucket       bucket the object lives in; {@code null} uses the configured default bucket
     * @param pathSupplier supplies the storage object key
     * @return presigned URL string
     */
    String presignedUrl(String bucket, Supplier<String> pathSupplier);

    /**
     * Shortcut — explicit bucket, known path string, default expiry.
     */
    default String presignedUrl(String bucket, String path) {
        return presignedUrl(bucket, () -> path);
    }

    /**
     * Generate a presigned GET URL with a custom expiry, in a bucket.
     *
     * @param bucket       bucket the object lives in; {@code null} uses the configured default bucket
     * @param pathSupplier supplies the storage object key
     * @param duration     how long the URL stays valid
     * @return presigned URL string
     */
    String presignedUrl(String bucket, Supplier<String> pathSupplier, Duration duration);


// Service Layer ---------------------------------
    private static final Duration DEFAULT_PRESIGN_DURATION = Duration.ofDays(7);
    
    @Override
    public String presignedUrl(String bucket, Supplier<String> pathSupplier) {
        return presignedUrl(bucket, pathSupplier, DEFAULT_PRESIGN_DURATION);
    }

    @Override
    public String presignedUrl(String bucket, Supplier<String> pathSupplier, Duration duration) {
        String path = pathSupplier.get();
        String resolvedBucket = resolveBucket(bucket);

        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(req ->
            req.signatureDuration(duration) // token expired after duration
                .getObjectRequest(it -> it
                    .bucket(resolvedBucket)
                    .key(path)
                )
        );

        return presigned.url().toString();
    }
```

# Delete File logic
```java
    @Override
    public void delete(String bucket, Supplier<String> pathSupplier) {
        String path = pathSupplier.get();

        s3Client.deleteObject(
            DeleteObjectRequest.builder()
                .bucket(resolveBucket(bucket))
                .key(path)
                .build()
        );
        log.debug("Deleted object at path={}", path);
    }
```