> UnaryOperator
- It gives back an output that is the exact same data type as the input

# Example 1
- retrieve label by id from id-label mapping

### Example 1 - without UnaryOperator
```java
var catMap = notificationCategorySvc.getMapping();
var page = blastRepository.findMerchantBlasts(filter, pageable)
        .map(entity -> MerchantBlastListItem.builder()
                .id(entity.getId())
                .blastName(entity.getBlastName())
                .category(entity.getCategory())
                .categoryDisplay(resolveCategoryLabel(catMap, entity.getCategory()))
                .build());

private String resolveCategoryLabel(Map<String, NotificationCategoryRes> catMap, String category) {
    NotificationCategoryRes res = catMap.get(category);
    return res != null ? res.getLabel() : Constants.NONE;
}
```

### Example 1 - with UnaryOperator
```java
var catMap = notificationCategorySvc.getMapping();
UnaryOperator<String> catLabelGetter = cat -> Optional.ofNullable(catMap.get(cat))
    .map(NotificationCategoryRes::getLabel)
    .orElse(Constants.NONE);

var page = blastRepository.findMerchantBlasts(filter, pageable)
        .map(entity -> MerchantBlastListItem.builder()
            .id(entity.getId())
            .blastName(entity.getBlastName())
            .category(entity.getCategory())
            .categoryDisplay(catLabelGetter.apply(entity.getCategory()))
            .build());
```


# Example 2
- get translated message from message code

### Example 2 - without UnaryOperator
```java
var statuses = keyLabelOptionsFactory(getBlastStatusOptions());

private Map<String, String> getBlastStatusOptions() {
    Map<String, String> options = new LinkedHashMap<>();
    for (BlastStatus status : BlastStatus.values()) {
        options.put(status.getSelectionKey(), getI18nMessage(status.getSelectionValue()));
    }
    return options;
}
```

### Example 2 - with UnaryOperator
```java
var statuses = keyLabelOptionsFactory(SelectableEnum.getSelectionMap(BlastStatus.class, this::getI18nMessage));

/*
 *  E extends Enum<E> ::
 *  E == whatever enum; extends Enum<E> == must be a enum class
 *  & SelectableEnum == E must also implement SelectableEnum interface
 */
@JsonIgnore
static <E extends Enum<E> & SelectableEnum> Map<String, String> getSelectionMap(
    Class<E> enumClass,
    UnaryOperator<String> i18nFunc
) {
    return getSelectionMap(
        enumClass,
        SelectableEnum::getSelectionKey,
        SelectableEnum::getSelectionValue,
        i18nFunc
    );
}

@JsonIgnore
static <E extends Enum<E> & SelectableEnum> Map<String, String> getSelectionMap(
    Class<E> enumClass,
    Function<E, String> keyFunc,
    Function<E, String> valueFunc,
    UnaryOperator<String> i18nFunc
) {
    Map<String, String> map = new HashMap<>();

    for (E e : enumClass.getEnumConstants()) {
        var value = valueFunc.apply(e);
        if (!Objects.isNull(i18nFunc)) {
            value = i18nFunc.apply(value);
        }
        map.put(
            keyFunc.apply(e),
            value
        );
    }
    return map;
}
```