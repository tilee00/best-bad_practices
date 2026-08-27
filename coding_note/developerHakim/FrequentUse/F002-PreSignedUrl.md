# What is PreSigned Url?
- allow PUT/POST (update) and GET (view) file
- directly from cloud storage within limited time period
- skip middleman (server side) from normal flow :: [client side -> server side -> cloud storage]

# Why use PreSigned Url?
1. when skip middleman (server side), then is Fast
- in order to be fast, we access directly to cloud storage
- SAFETY NOTE (access to cloud but can't put the master cloud login credential in client side is dangerous)
    - hence, use this presigned url, which with a active token expired after a duration

2. With Expired Time
- consist of token active in limited time period 
- SAFETY NOTE (who with link will have access)
    - so the access control need to be done before link generated
    - allow the link for single use only [AWS S3 not supported]
    - access granted restricted by the requested user home IP address [AWS S3 not supported]

# Example usage for viewing purpose
```java
private List<PushNotificationGroupedByMonthRes> groupByDatePreservingOrder(List<PushNotificationListItem> items) {

    var brandLogoUrlMap = brandLogoRepository.getLogoInfoByBrandIdIn(brandIds)
        .stream()
        .collect(Collectors.toMap(
            BrandLogoInfo::getBrandId,
            logoInfo -> Optional.ofNullable(logoInfo.getLogoPath())
                .map(fileService::presignedUrl) // pass the logoPath into fileService as parameter presingedUrl
                .orElse(""),
            (existing, replacement) -> existing
        ));
}

@Override
    public String presignedUrl(String bucket, Supplier<String> pathSupplier, Duration duration) {
        String path = pathSupplier.get();
        // bucket = name for the top-level folder/storageContainer in cloudStorage
        String resolvedBucket = Optional.ofNullable(StringUtils.trimToNull(bucket))
            .orElse(storageProperties.getBucket());

        // ask AWS S3 to create a presigned url for view (GET) method
        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(req ->
            req.signatureDuration(duration) // set expired time
                .getObjectRequest(it -> it
                    .bucket(resolvedBucket)
                    .key(path)
                )
        );

    }
```