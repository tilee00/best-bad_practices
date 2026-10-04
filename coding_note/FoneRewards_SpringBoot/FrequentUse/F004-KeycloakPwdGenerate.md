# How to generate temp password?

# Example code

```java
@Component
@Setter
@Getter
@ConfigurationProperties("keycloak")
public class KeycloakProperties {

    private Datasource datasource = new Datasource();

    private PasswordPolicy passwordPolicy = new PasswordPolicy();

    private List<String> realmNames = new ArrayList<>();

    @Setter
    @Getter
    public static class Datasource {
        private String jdbcUrl;
        private String username;
        private String password;
        private String driverClassName;
    }

    @Setter
    @Getter
    public static class PasswordPolicy {

        private int minimumLength = 8;
        private int uppercaseChars = 1;
        private int lowercaseChars = 1;
        private int digits = 1;
        private int specialChars = 1;

    }
}
```

```java
import com.project.config.KeycloakProperties;
import com.ifb.core.exception.ApiBadRequestException;
import org.passay.generate.PasswordGenerator;
import org.passay.rule.CharacterRule;
import org.springframework.stereotype.Component;

@Component
public class PasswordUtil {

    // CharacterRule = Validates whether a password contains a certain number of a type of character.
    private final List<CharacterRule> rules;
    private final int minRequiredLength;

    public PasswordUtil(
        List<CharacterRule> rules,
        KeycloakProperties props
    ) {
        this.rules = rules;
        var policy = props.getPasswordPolicy();
        minRequiredLength = Math.max(
            policy.getMinimumLength(),
            policy.getUppercaseChars()
                + policy.getLowercaseChars()
                + policy.getDigits()
                + policy.getSpecialChars()
        );
    }

    public String generateTempPassword(int length) {
        if (length < minRequiredLength) {
            throw new ApiBadRequestException(
                "length must be at least " + minRequiredLength + " to satisfy the configured character rules"
            );
        }
        return getGenerator(length).generate().toString();
    }

    private PasswordGenerator getGenerator(int length) {
        return new PasswordGenerator(length, rules);
    }
}
```

