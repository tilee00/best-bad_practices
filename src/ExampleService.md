# Java Service
- show the best practice and bad practice of the EXAMPLE code

# Note
- The key is balancing readability with performance.
- there might be wrong info, as the info mainly from AI and my experience

### ☆ Example Code 1 (22 Aug 2026) ☆☆☆☆☆☆☆☆☆☆☆☆
- a code to get data from nested entity named MerchantEntity

```java
// Example 1 START ==========================
// DESC = using entityManager.getReference()
@Entity
@Table(name = "brand", schema = "rewd_brand")
public class BrandEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_brand_id")
    @SequenceGenerator(name = "seq_brand_id", schema = "rewd_brand", sequenceName = "brand_brand_id_seq",
        allocationSize = 1)
    @Column(name = "brand_id", nullable = false)
    private Integer brandId;

    @Column(name = "brand_name")
    private String brandName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", referencedColumnName = "merchant_id", nullable = false)
    private MerchantEntity merchant;

}

// in service layer
List<BrandEntity> brandsEntities = brandRepository.findByMerchantOrderByBrandNameAsc(entityManager.getReference(MerchantEntity.class, merchantId));
// Example 1 END ==========================
```

### BAD Practice for Example Code 1

```java

// Bad Practice 1 START ==========================
// DESC = bad because executes 2 database queries when 1 is sufficient
MerchantEntity merchant = merchantRepository.findById(merchantId)
    .orElseThrow(() -> new EntityNotFoundException("Merchant not found"));

List<BrandEntity> brands = brandRepository.findByMerchantOrderByBrandNameAsc(merchant);
// Bad Practice 1 END ==========================


// Bad Practice 2 START ==========================
// DESC = bad because stream in java, bypasses database indexes, consumes massive JVM heap space
List<BrandEntity> allBrands = brandRepository.findAll();

List<BrandEntity> merchantBrands = allBrands.stream()
    .filter(b -> b.getMerchant() != null && merchantId.equals(b.getMerchant().getMerchantId()))
    .sorted(Comparator.comparing(BrandEntity::getBrandName))
    .toList();
// Bad Practice 2 END ==========================

```

### BEST Practice for Example Code 1

```java

// Best Practice 1 START ==========================
// DESC = using jpa, merchant.merchantId
List<BrandEntity> findByMerchantMerchantIdOrderByBrandNameAsc(Integer merchantId);

List<BrandEntity> brandEntities = brandRepository.findByMerchantMerchantIdOrderByBrandNameAsc(merchantId);
// Best Practice 1 END ==========================


// Best Practice 2 START ==========================
// DESC = using jpa + projection
public interface BrandSummaryProjection {
    Integer getBrandId();
    String getBrandName();
    Integer getMerchantMerchantId(); // Maps to merchant.merchantId
}

List<BrandSummaryProjection> findProjectedByMerchantMerchantIdOrderByBrandNameAsc(Integer merchantId);

List<BrandSummaryProjection> brands = brandRepository.findProjectedByMerchantMerchantIdOrderByBrandNameAsc(merchantId);
// Best Practice 2 END ==========================


// Best Practice 3 START ==========================
// DESC = using jpql query
@Query("SELECT b FROM BrandEntity b JOIN FETCH b.merchant m WHERE m.merchantId = :merchantId ORDER BY b.brandName ASC")
List<BrandEntity> findByMerchantIdWithMerchant(@Param("merchantId") Integer merchantId);

List<BrandEntity> brandEntities = brandRepository.findByMerchantIdWithMerchant(merchantId);
// Best Practice 3 END ==========================

```


### ☆ Example Code 2 (22 Aug 2026) ☆☆☆☆☆☆☆☆☆☆☆☆
- a code to throw error when id is not null

```java
// Example 2 START ==========================
// DESC = throw error when any Id is not null
List<Integer> listOutletIdPayload = brandOutletReq.getOutlets().stream().map(OutletReq::getOutletId).toList();

if (listOutletIdPayload.stream().anyMatch(Objects::nonNull)) {
    return response.setCode(404).setMessage(getI18nMessage("brand.error.outlet-must-null"));
}
// Example 2 END ==========================
```

### Bad Practice for Example Code 2

```java

// Bad Practice 1 START ==========================
// DESC = code 404 mean page not found, so should be 400 Bad Request
// DESC = 2 stream is used to achieve result, a bit waste memory and CPU
List<Integer> listOutletIdPayload = brandOutletReq.getOutlets().stream().map(OutletReq::getOutletId).toList();

if (listOutletIdPayload.stream().anyMatch(Objects::nonNull)) {
    return response.setCode(404).setMessage(getI18nMessage("brand.error.outlet-must-null"));
}
// Bad Practice 1 END ==========================


// Bad Practice 2 START ==========================
// DESC = .filter() did not stop after found the first match which wasted computations
// DESC = a bit waste memory to store a list with nonNullId when only a boolean is needed
List<Integer> nonNullOutletIds = brandOutletReq.getOutlets().stream()
    .map(OutletReq::getOutletId)
    .filter(Objects::nonNull)
    .toList();

if (!nonNullOutletIds.isEmpty()) {
    return response.setCode(404).setMessage(getI18nMessage("brand.error.outlet-must-null"));
}
// Bad Practice 2 END ==========================

```

### Best Practice for Example Code 2

```java

// Best Practice 1 START ==========================
// DESC = result of stream just to evaluate, zero memory allocated to stored result of stream, saved memory
// DESC = .anyMatch() is stop once match, does not waste much CPU cycles evaluating the rest 
if (brandOutletReq.getOutlets().stream().map(OutletReq::getOutletId).anyMatch(Objects::nonNull)) {
    return response.setCode(400).setMessage(getI18nMessage("brand.error.outlet-must-null"));
}
// Best Practice 1 END ==========================

```


### ☆ Example Code 3 (22 Aug 2026) ☆☆☆☆☆☆☆☆☆☆☆☆
- the code is to get all the enum value and place into a map list

```java
// Example 3 START ==========================
private Map<Integer, String> getFeedbackType() {
    Map<Integer, String> map = new LinkedHashMap<>();

    EnumSet<FeedbackTypeEnum> enumGrp = EnumSet.allOf(FeedbackTypeEnum.class);

    for (FeedbackTypeEnum item : enumGrp) {
        map.put(item.getKey(), ret18nMessageValue(item.getValue()));
    }

    return map;
}
// Example 3 END ==========================
```

### Bad Practice for Example Code 3

```java

// Bad Practice 1 START ==========================
// DESC = bad because creating a new I18n resource reader/context on EVERY loop iteration (degrades performance)
private Map<Integer, String> getFeedbackType() {
    Map<Integer, String> map = new HashMap<>();
    for (FeedbackTypeEnum item : FeedbackTypeEnum.values()) {
        MessageSource messageSource = new ResourceBundleMessageSource(); 
        String translated = messageSource.getMessage(item.getValue(), null, Locale.getDefault());
        map.put(item.getKey(), translated);
    }
    return map;
}
// Bad Practice 1 END ==========================

// Bad Practice 2 START ==========================
private Map<Integer, String> getFeedbackType() {
    Map map = new HashMap(); // BAD: Raw types map (Map instead of Map<Integer, String>)
    
    // BAD: Hardcoding enum handling manually instead of looping enum.values()
    for (int i = 0; i < 100; i++) { 
        try {
            FeedbackTypeEnum item = FeedbackTypeEnum.values()[i];
            map.put(item.getKey(), ret18nMessageValue(item.getValue()));
        } catch (ArrayIndexOutOfBoundsException e) {
            break; // BAD: Using exceptions to stop loop when outOfBounds
        }
    }
    return map;
}
// Bad Practice 2 END ==========================

```

### Best Practice for Example Code 3

```java

// Best Practice 1 START ==========================
// DESC = not need create EnumSet object just use directly FeedbackTypeEnum.values()
private Map<Integer, String> getFeedbackType() {
    FeedbackTypeEnum[] values = FeedbackTypeEnum.values();
    // Good: Passing values.length to prevents the LinkedHashMap from constantly resizing
    Map<Integer, String> map = new LinkedHashMap<>(values.length);

    for (FeedbackTypeEnum item : values) {
        map.put(item.getKey(), ret18nMessageValue(item.getValue()));
    }

    return map;
}
// Best Practice 1 END ==========================

// Best Practice 2 START ==========================
// DESC = replaces explicit loop boilerplates with a functional stream pipeline
private Map<Integer, String> getFeedbackType() {
    return Arrays.stream(FeedbackTypeEnum.values())
        .collect(Collectors.toMap(
            FeedbackTypeEnum::getKey,
            item -> ret18nMessageValue(item.getValue()),
            // Map only allow uniquey key, hence if duplicate key happen, keep the oldValue
            (oldVal, newVal) -> oldVal,
            // convert to target return type
            LinkedHashMap::new
        ));
}
// Best Practice 2 END ==========================

```


### ☆ TEMPLATE Example Code (date) ☆☆☆☆☆☆☆☆☆☆☆☆
- desc

```java
// Example 1 START ==========================
// DESC = 
// Example 1 END ==========================
```

### Bad Practice for Example Code 1

```java

// Bad Practice 1 START ==========================
// DESC = 
// Bad Practice 1 END ==========================

```

### Best Practice for Example Code 1

```java

// Best Practice 1 START ==========================
// DESC = 
// Best Practice 1 END ==========================

```

