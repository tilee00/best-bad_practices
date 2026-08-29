# search notification with filter and pagination

```java
@Service
@RequiredArgsConstructor
public class PushNotificationHistSvc extends BaseService implements IPushNotificationHistService {

    private final PushNotificationHistRepository notifHistoryRepo;

    @Transactional(readOnly = true)
    @Override
    public ApiResponse<PagedResponse<PushNotificationGroupedByMonthRes>> getNotifications(
        PushNotificationFilter filter, Pageable pageable) {

        // ☆ FOCUS - get notification info (has filter and pagination)
        var slice = notifHistoryRepo.findAll(filter, pageable);

        // IGNORE - process the data then put into response
        // var catMap = notificationCategoryService.getMapping();
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
// .findAll()
public interface PushNotificationHistRepository extends JpaRepository<PushNotificationHistoryEntity, UUID> {

    // #{ ... } tell spring inside is SpEL language; #filter = java parameter pass in
    //  ---
    // [AND input.x IS NULL OR database.x = anyValue] If input x is NOT null, filter by database.x = input.x. 
    // If input x IS null, ignore this filter completely.
    @Query("""
        SELECT pnh FROM PushNotificationHistoryEntity pnh
         WHERE pnh.userId = :#{#filter.userId}
             AND (:#{#filter.brandId} IS NULL OR pnh.brandId = :#{#filter.brandId})
             AND (:#{#filter.outletId} IS NULL OR pnh.outletId = :#{#filter.outletId})
             AND (:#{#filter.category} IS NULL OR pnh.catId IN :#{#filter.category})
             AND (coalesce(:#{#filter.startDate}, null) IS NULL OR pnh.createdDate >= :#{#filter.startDate})
             AND (coalesce(:#{#filter.endDate}, null) IS NULL OR pnh.createdDate <= :#{#filter.endDate})
        """)
    Slice<PushNotificationHistoryEntity> findAll(PushNotificationFilter filter, Pageable pageable);

}


@Data
public class PushNotificationFilter {

    private UUID userId;

    private Integer brandId;

    private Integer outletId;

    private Set<NotificationCategory> category;

    private LocalDate startDate;

    private LocalDate endDate;
}

```

