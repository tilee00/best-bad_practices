# view all notification with filter and pagination
- for small amount of not frequently updated data can use the getMapping() below

```java
// service layer
@Transactional(readOnly = true)
    @Override
    public ApiResponse<PagedResponse<PushNotificationGroupedByMonthRes>> getNotifications(
        PushNotificationFilter filter, Pageable pageable) {

        // get notification info (has filter and pagination)
        var slice = notifHistoryRepo.findAll(filter, pageable);

        // get category info [and change it into a Map (key -> { {}, {} }) ]
        var catMap = notificationCategoryService.getMapping();
        var items = slice.getContent().stream()
            .map(it -> mapToItem(it, catMap))
            .toList();
        return ApiResponse.setSuccess(PagedResponse.of(slice.hasNext(),
            slice.hasPrevious(),
            groupByDatePreservingOrder(items)));
    }
```

```java
// getMapping()
@Override
    public Map<String, NotificationCategoryRes> getMapping() {
        var cacheKey = CACHE_KEY_PREFIX + "mapping";
        var cached = cachingService.get(cacheKey, NOTIFICATION_RES_CAT_LIST_TYPE);
        // if cache not null then get category info from cache
        if (cached != null) {
            return cached.stream()
                .collect(Collectors.toMap(
                    NotificationCategoryRes::getId,
                    // return exactly the cahce value
                    java.util.function.Function.identity()
                ));
        }
        // if no cache, then all get category info
        var freshData = catRepo.getAll();
        // save the category info in cache, expired after 1 hour
        cachingService.set(cacheKey, freshData, Duration.ofHours(1));
        return freshData.stream()
            .collect(Collectors.toMap(
                NotificationCategoryRes::getId,
                java.util.function.Function.identity()
            ));
    }
```

```java
// mapToItem()
private PushNotificationListItem mapToItem(PushNotificationHistoryEntity entity,
                                               Map<String, NotificationCategoryRes> catMap) {
        return PushNotificationListItem.builder()
            .id(entity.getId())
            .title(entity.getTitle())
            .body(entity.getBody())
            .category(entity.getCatId())
            .categoryLabel(getCatLabel(entity.getCatId(), catMap))
            .createdAt(DatetimeDisplayUtil.toUserOffsetDatetime(entity.getCreatedAt()))
            .createdTime(DatetimeDisplayUtil.toUserLocalTime(entity.getCreatedAt()))
            .brandId(entity.getBrandId())
            .outletId(entity.getOutletId())
            .ctaButtons(JsonValueUtils.toObject(entity.getCta(), mapper, CtaButton.CTA_BUTTON_LIST_TYPE_REF))
            .hasRead(hasRead(entity.getStatus()))
            .createdDate(DatetimeDisplayUtil.toUserLocalDate(entity.getCreatedAt()))
            .build();
    }
```
