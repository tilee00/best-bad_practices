# Java Service
- show the best practice and bad practice of the EXAMPLE code

### Example Code 1 (22 Aug 2026)
- get data from nested entity named MerchantEntity

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

### TEMPLATE Example Code (date)
- desc

```java
// Example 1 START ==========================
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

