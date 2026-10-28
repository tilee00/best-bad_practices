### json like pattern

```json
{
	name: Alice
	age: 25
	active: True
}
```

```java
    private static String formatMap(Map<String, Object> map) {
        return map.entrySet()
            .stream()
            .map(entry -> "%s: %s".formatted(entry.getKey(), entry.getValue()))
            // Collectors.joining(delimiter[Glues each pair together], prefix[Starts], suffix[the very end])
            .collect(Collectors.joining("\n\t", "\n{\n\t", "\n}"));
    }
```

### web address query param

```
?search=java&page=1&sort=latest
```

```java
import java.util.Map;
import java.util.stream.Collectors;

public class UrlQueryFormatterExample {

    // A function that turns our dictionary into a web address link extension
    private static String formatToQueryString(Map<String, Object> map) {
        return map.entrySet()
            .stream()
            .map(entry -> "%s=%s".formatted(entry.getKey(), entry.getValue()))
            // Collectors.joining(delimiter[Glues each pair together], prefix[Starts], suffix[the very end])
            .collect(Collectors.joining("&", "?", ""));
    }

    public static void main(String[] args) {
        // Create a sample dictionary (Map) with some search options
        Map<String, Object> searchParams = Map.of(
            "search", "java",
            "page", 1,
            "sort", "latest"
        );

        String result = formatToQueryString(searchParams);
        System.out.println(result);
    }
}
```