# Enum
- use enum when 
  - need to avoid string value typo 
    - Example: Active not Actve
  - need to make the numeric value have a clear meaning 
    - Example: 1 = Feedback type Katalog

### default enum
```typescript
// print will get index
enum CardinalDirections {
  North = 1,
  East,
  South,
  West
}

// will get index when print
let currentDirection = CardinalDirections.North;
// default index start from 0, but can change to desired number, then it would auto increment
console.log(currentDirection);  // log output: 1
console.log(CardinalDirections.West); // log output: 4
```

### return enum value - specific
```typescript
enum CardinalDirections {
  North = 'North',
  East = "East",
  South = "South",
  West = "West"
};
console.log(CardinalDirections.North); // log output: "North"

function getCardinalValues(): string[] {
  return Object.values(CardinalDirections);
}
console.log(getCardinalValues()); 
// log output: ["North", "East", "South", "West"]
```

### return enum value - general
```typescript
enum CardinalDirections {
  North = 'North',
  East = "East",
  South = "South",
  West = "West"
};

// T is a generic type parameter that says "fill in what this box actually is later"
function getEnumStringValues<T extends Record<string, string>>(enumObj: T): string[] {
  return Object.values(enumObj);
}

const myDirections = getEnumStringValues(CardinalDirections);
console.log(myDirections); 
// log output: ["North", "East", "South", "West"]
```

# Record
- use Record when 
  - need to map key to value
    - Example: RED = "Stop right there!"

```typescript
const permissions: Record<'admin' | 'guest', boolean> = {
  admin: true,
  guest: false
};
```

# Record combined with Enum
```typescript
type ClubhouseMember = 'boss' | 'sidekick' | 'guest';
const allowedToOpenSecretFort: Record<ClubhouseMember, boolean> = {
  boss: true,
  sidekick: true,
  guest: false,
};

```

