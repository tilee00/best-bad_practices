```java
public record TemplateVariableData(
    String key,
    String example
) {

    public static TemplateVariableData from(TemplateVariable variable) {
        return new TemplateVariableData(variable.getKey(), variable.getPreviewValue());
    }

    /*
    * Summary
    * -> variables is a List
    * -> wrap the List in a Stream
    *      = Stream<List<TemplateVariableData>>
    *
    * -> Map() get all List item Stream
    * -> flat() then flat those streams into one Stream
    *      = Stream<TemplateVariableData>
    *
    * -> collect those items into a Map<String, String>
    */
    // Sample Data 
    // change List [ { key: "k1", example: "v1" }, { key: "k2", example: "v2" } ]  
    // to Map { "k1": "v1", "k2": "v2" } 
    public static Map<String, String> toParams(List<TemplateVariableData> variables) { 
        // default is a empty stream, else change the variables List into a stream
        return Stream.ofNullable(variables) 
            // List::stream is shorthand for: list -> list.stream()
            // It converts each List item into a Stream
            //
            // map(List::stream) would produce:
            // Stream<Stream<TemplateVariableData>>
            //
            // flatMap() instead flattens those nested streams into:
            // Stream<TemplateVariableData>
            .flatMap(List::stream) 
            // for each item 
            .collect( 
                // set each item key (type = TemplateVariableData) into a map key
                Collectors.toMap( 
                    // short form of it -> it.key() is TemplateVariableData::key 
                    TemplateVariableData::key, 
                    // set each item value into map value; else fall back to item key
                    it -> StringUtils.defaultIfBlank(it.example(), it.key()) 
                ) 
            ); 
    }
}   

    private Map<String, String> getWhatsAppParamsValue(List<TemplateVariableData> templateVariableData) {
        if (templateVariableData == null || templateVariableData.isEmpty()) {
            return Map.of();
        }
        // change List.of(templateVariableData) to [ Map<key, value> = {paramName : variableDictionaryID} ]
        var whatsAppParamMap = TemplateVariableData.toParams(templateVariableData);
        // find sampleValue based on all the variableDictionaryID
        var dictMap = variablesDictionaryRepo.getSampleValuesByIdIn(whatsAppParamMap.values().stream().toList())
            .stream().collect(
                Collectors.toMap(
                    VariableDict::getId, VariableDict::getSampleValue,
                    // if duplicate variableDictionaryID, keep the first <id : sampleValue> pair
                    (first, second) -> first
                )
            );
        // map variableDictionaryID of whatsAppParamMap to variableDictionaryID of dictMap
        whatsAppParamMap.replaceAll(
            // replace variableDictionaryID of whatsAppParamMap to sampleValue of dictMap, else fall back to default variableDictionaryID
            (paramName, variableDictionaryID) -> dictMap.getOrDefault(variableDictionaryID, variableDictionaryID)
        );
        return whatsAppParamMap;
    }
```