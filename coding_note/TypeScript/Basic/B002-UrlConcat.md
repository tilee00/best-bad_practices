# 

### call method
```typescript
const IAM_BASE = Url.concat(import.meta.env.VITE_API_BASE_URL, "/my/bd_merchant_iam", import.meta.env.VITE_API_VERSION);

const MERCHANT_BASE = Url.concat(
  import.meta.env.VITE_API_BASE_URL,
  import.meta.env.VITE_API_MODULE,
  import.meta.env.VITE_API_VERSION
);
```

### method definition
```typescript
class Url {

  // static methodType see Note-1 below
  // methodType methodName(argument): returnType
  static concat(...parts: string[]): string {
    return parts
      .map((part, index) => {
        if (index === 0) {
          // .replace(/.../g)
          // backslash (\) — The "Escape" Shield; \/+ means "a slash, or a bunch of slashes stuck together (like / or ///)
          return part.replace(/\/+$/g, ""); // Remove trailing slashes for the first part
        } else {
          // ^ = start of the text, and $ = end of the text
          // Vertical Bar (|) — "OR"
          return part.replace(/^\/+|\/+$/g, ""); // Remove leading and trailing slashes for other parts
        }
      })
      .filter((part) => part.length > 0) // Remove empty parts
      .join("/"); // Join with a single slash
  }

}

export default Url;
```

### Note-1
```typescript

// with static - just use it right away!
const myUrl = Url.concat("https://google.com", "search");

// without static - instance method
const myRobot = new Url(); 
const myUrl = myRobot.concat("https://google.com", "search");

```
