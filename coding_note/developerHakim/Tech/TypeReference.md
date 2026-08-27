> TypeReference

# When need to use?
```java
// use when need to consume (read) data from type below

class ItemBean {
    private Long itemId;
    private String itemName;
    private BigDecimal itemPrice;
    private Integer quantity;
}

// get data (requestBean / payload)
List<ItemBean>
Set<ItemBean>
Map<Long, ItemBean>

```

# When NOT need to use?
```java
class ItemBean {
    private Long itemId;
    private String itemName;
    private BigDecimal itemPrice;
    private Integer quantity;
}

ItemBean[]
ItemBean

```

# Example usage
1. declare typeReference (bean prepare)
2. read json in typeReference and change to java variable for further process

- Data Prepare
```java
// TypeReference used here, bean prepare
@Data
@Builder(toBuilder = true)
public class CtaButton implements Serializable {

    public static final TypeReference<List<CtaButton>> CTA_BUTTON_LIST_TYPE_REF = new TypeReference<>() {
    };

    private String label;
    private String target;
    private String targetType;

    // Ignore during serialization (will not appear in the JSON response)
    // Ignore during deserialization (will not be read from the incoming JSON payload)
    // Still fully usable in Java code, business logic, and database operations (JPA/Hibernate)
    @JsonIgnore 
    private UUID pnhId;

}

// bean prepare
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class PushNotificationListItem implements Serializable {

    private UUID id;

    private String title;

    private String body;

    @Builder.Default
    private List<CtaButton> ctaButtons = List.of();


}

// database entity prepare
@Getter
@Setter
@Builder(toBuilder = true)
@Entity
@Table(name = "push_notification_history", schema = SchemaNames.REWD_MERCHANT)
@AllArgsConstructor
@NoArgsConstructor
public class PushNotificationHistoryEntity {

    @Id
    @UuidGenerator
    private UUID id;

    private String title;

    private String body;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String cta;

}
```

- usage
```java
// set data into bean
@Service
@RequiredArgsConstructor
public class PushNotificationHistSvc extends BaseService implements IPushNotificationHistService {

    private final ObjectMapper mapper;

    private PushNotificationListItem mapToItem(PushNotificationHistoryEntity entity) {
        return PushNotificationListItem.builder()
            .id(entity.getId())
            .title(entity.getTitle())
            .body(entity.getBody())
            .ctaButtons(JsonValueUtils.toObject(entity.getCta(), mapper, CtaButton.CTA_BUTTON_LIST_TYPE_REF))
            .build();
    }
}

// change json to java object in specific type
@Slf4j
@UtilityClass
public class JsonValueUtils {

    public static <T> T toObject(String json, ObjectMapper mapper, TypeReference<T> typeReference) {
        try {
            return mapper.readValue(json, typeReference);
        } catch (Exception e) {
            throw new ApiBadRequestException("Failed to parse content");
        }
    }

}

// Extra - read from json
public static void main(String[] args) throws Exception {
    String jsonResponse = getVouchersFromApi();
    ObjectMapper mapper = new ObjectMapper();

    List<MerchantVoucher> vouchers = mapper.readValue(
        jsonResponse, 
        MerchantVoucher.VOUCHER_LIST_REF
    );

    // We can safely call methods on MerchantVoucher objects:
    for (MerchantVoucher voucher : vouchers) {
        System.out.println("Code: " + voucher.getVoucherCode());
        System.out.println("Max Savings: $" + voucher.getMaxDiscountCap());
    }

}
```
