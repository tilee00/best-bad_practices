# view all notification with filter and pagination
- for small amount of not frequently updated data can use the getMapping() below

```java
@Service
@RequiredArgsConstructor
public class PushNotificationHistSvc extends BaseService implements IPushNotificationHistService {

    private final PushNotificationHistRepository notifHistoryRepo;

    @Transactional(readOnly = true)
    @Override
    public ApiResponse<PagedResponse<PushNotificationGroupedByMonthRes>> getNotifications(
        PushNotificationFilter filter, Pageable pageable) {

        // var slice = notifHistoryRepo.findAll(filter, pageable);

        // ☆ FOCUS - get category info (with cahce and map)
        var catMap = notificationCategoryService.getMapping();

        // IGNORE - process the data then put into response
        // var items = slice.getContent().stream()
        //    .map(it -> mapToItem(it, catMap))
        //    .toList();
        // return ApiResponse.setSuccess(PagedResponse.of(slice.hasNext(),
        //    slice.hasPrevious(),
        //    groupByDatePreservingOrder(items)));
    }
}
```

```java
// getMapping()

@Service
@RequiredArgsConstructor
public class NotificationCategorySvc implements INotificationCategoryService {

    private final NotificationCategoryCustomRepository catRepo;
    private final ICachingService cachingService;

    private static final String CACHE_KEY_PREFIX = "notification:category:";

    private static final TypeReference<List<NotificationCategoryRes>> NOTIFICATION_RES_CAT_LIST_TYPE
        = new TypeReference<>() {
    };

    @Override
    public Map<String, NotificationCategoryRes> getMapping() {
        var cacheKey = CACHE_KEY_PREFIX + "mapping";

        // Uses NOTIFICATION_RES_CAT_LIST_TYPE to tell the caching service 
        // how to convert (deserialize) the JSON text into a List<NotificationCategoryRes> 
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
}
```

```java
// CachingService

public interface ICachingService {

    void set(String key, Object data, Duration ttl);
}

import org.springframework.data.redis.core.StringRedisTemplate;

@Service
@RequiredArgsConstructor
public class RedisCachingService implements ICachingService {

    // (StringRedisTemplate) Helper library provided by Spring Framework to communicate with Redis
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper mapper;

    @Override
    public void set(String key, Object data, @NonNull Duration ttl) {
        redisTemplate.opsForValue() // Tells Spring to use simple key-value operations (ValueOperations)
            .set(key, toJson(data), ttl); // Built-in method from Spring Redis
    }

    private String toJson(Object data) {
        try {
            if (data instanceof String val) {
                return val;
            }
            return mapper.writeValueAsString(data);
        } catch (Exception e) {
            throw new CachingException(e);
        }
    }
}
```
