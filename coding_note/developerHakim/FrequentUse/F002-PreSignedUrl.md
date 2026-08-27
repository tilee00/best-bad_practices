# What is PreSigned Url?
- a way to upload file directly to cloud storage in limited time period
- skip middleman (server side) from flow :: [client side -> server side -> cloud storage]

# Why use PreSigned Url?
1. when skip middleman (server side), then is Safe and Fast
- upload to cloud storage directly prevent put the master login credential in the client side

2. With Expired Time
- consist of token active in limited time period 
- SAFETY NOTE (who with link will have access)
    - so the access control need to be done before link generated
    - allow the link for single use only
    - access granted restricted by the requested user home IP address

# Example usage
```java
private List<PushNotificationGroupedByMonthRes> groupByDatePreservingOrder(List<PushNotificationListItem> items) {

    var brandLogoUrlMap = brandLogoRepository.getLogoInfoByBrandIdIn(brandIds)
        .stream()
        .collect(Collectors.toMap(
            BrandLogoInfo::getBrandId,
            logoInfo -> Optional.ofNullable(logoInfo.getLogoPath())
                .map(fileService::presignedUrl) // pass the logoPath into fileService with parameter presingedUrl
                .orElse(""),
            (existing, replacement) -> existing
        ));
}

@Override
    public String presignedUrl(String bucket, Supplier<String> pathSupplier, Duration duration) {
        String path = pathSupplier.get();
        // bucket = top-level folder or storage container
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