

```java
public interface SelectableEnum {


    String name();

    /**
     * Default key for selection map (enum name)
     */
    @JsonIgnore
    default String getSelectionKey() {
        return name();
    }

    @JsonIgnore
    default String getDescription() {
        return "";
    }

    @JsonIgnore
    default String getI18nCode() {
        return name();
    }


    /**
     * Default value for selection map (capitalized name with spaces)
     */
    @JsonIgnore
    default String getSelectionValue() {
        return WordUtils.capitalizeFully(name().replace('_', ' '));
    }

    @JsonIgnore
    default void appendSelectionKeyValue(Map<String, Object> selectionMap) {
    }

    /**
     * Utility method to generate selection map for any enum implementing this interface
     */
    @JsonIgnore
    static <E extends Enum<E> & SelectableEnum> Map<String, String> getSelectionMap(
        Class<E> enumClass
    ) {
        return getSelectionMap(
            enumClass,
            SelectableEnum::getSelectionKey,
            SelectableEnum::getSelectionValue,
            null
        );
    }

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
    static <E extends Enum<E> & SelectableEnum> List<Map<String, Object>> getSelectionList(
        Class<E> enumClass,
        UnaryOperator<String> i18nFunc
    ) {
        return getSelectionList(
            enumClass,
            SelectableEnum::getSelectionKey,
            SelectableEnum::getSelectionValue,
            SelectableEnum::getDescription,
            i18nFunc
        );
    }

    @JsonIgnore
    static <E extends Enum<E> & SelectableEnum> List<Map<String, Object>> getSelectionList(
        Class<E> enumClass,
        Function<E, String> labelExtractor,
        UnaryOperator<String> i18nFunc
    ) {
        return getSelectionList(
            enumClass,
            SelectableEnum::getSelectionKey,
            labelExtractor,
            SelectableEnum::getDescription,
            i18nFunc
        );
    }

    @JsonIgnore
    static <E extends Enum<E> & SelectableEnum> List<Map<String, Object>> getSelectionList(
        Class<E> enumClass
    ) {
        return getSelectionList(
            enumClass,
            SelectableEnum::getSelectionKey,
            SelectableEnum::getSelectionValue,
            SelectableEnum::getDescription,
            null
        );
    }

    /**
     * Utility method with custom key and value functions
     */
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

    @JsonIgnore
    static <E extends Enum<E> & SelectableEnum> List<Map<String, Object>> getSelectionList(
        Class<E> enumClass,
        Function<E, String> keyFunc,
        Function<E, String> valueFunc,
        Function<E, String> descriptionFunc,
        UnaryOperator<String> i18nFunc
    ) {
        var selections = new LinkedList<Map<String, Object>>();
        for (E e : enumClass.getEnumConstants()) {
            var map = new HashMap<String, Object>();
            var value = valueFunc.apply(e);
            var description = descriptionFunc.apply(e);
            if (!Objects.isNull(i18nFunc)) {
                value = i18nFunc.apply(value);
                description = i18nFunc.apply(description);
            }
            map.put(
                "key",
                keyFunc.apply(e)
            );
            map.put("label", value);
            map.put("description", description);
            e.appendSelectionKeyValue(map);
            selections.add(map);

        }
        return selections;
    }

    @JsonIgnore
    static String translateLabel(
        SelectableEnum enumVal,
        UnaryOperator<String> i18nFunc
    ) {
        return Optional.ofNullable(enumVal)
            .map(SelectableEnum::getI18nCode)
            .map(i18nFunc)
            .orElse(null);
    }


}

```