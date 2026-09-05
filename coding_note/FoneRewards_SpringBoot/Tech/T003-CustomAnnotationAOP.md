> Custom Annotation + Aspect Oriented Programming

# When need to use
1. Reducing Duplicate Boilerplate
- such as exact same code 
    for logging, measuring execution time, checking permissions, or catching specific errors across many methods

2. Creating Specific Labels
- mark specific classes or fields 
    for custom scanning, validation, or processing in your framework

# When NOT need to use
1. Standard Built-in Annotations Already Exist

2. Plain Java Methods or Interfaces Can Do the Job

3. Single-Use Logic

# Table of Content
1. how to do configuration for AOP
2. how to make custom annotation
3. how to use those custom annotation in AOP

# Code - 1 - how to do configuration for AOP
```java
/*  
 *  @Configuration 
 *      - registers this class as a @Component (specifically a configuration component)
 *      - creates a proxy around THIS CLASS to filter @Bean method calls to create new object
 *      - prevents creating duplicate objects by fetching the existing object from Spring's container
 *  @EnableAspectJAutoProxy
 *      - turns on Spring AOP support so aspect tools (like logging) can run
 */
@Configuration
@EnableAspectJAutoProxy
public class LoggingAspectConfiguration {

    /*
     *  @Bean
     *      - creates/register third party library object which the library code are read-only
     *      - OR used when the object creation is only apply to certain env
     *  @Profile
     *      - is based on Property (such as [java -Dspring.profiles.active=dev -jar app.jar])
     *      - if profile parameter match the property in setting then execute the code
     *  @ConditionalOnProperty
     *      - plus extra condition to execute the code
     *      - only when the application.debug.enabled: true then execute
     */
    @Bean
    @Profile(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT)
    @ConditionalOnProperty(value = "application.debug.enabled", havingValue = "true")
    public LoggingAspect loggingAspect(Environment env) {
        return new LoggingAspect(env);
    }
}

```

# Code - 2 - how to make custom annotation
```java
```
> o?

> haha

> huhu

# Code - 3 - how to use those custom annotation in AOP
```java

```
> plenty many
> not ok