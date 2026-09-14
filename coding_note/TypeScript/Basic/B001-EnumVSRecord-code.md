# Enum

```typescript
```

# Record
```typescript
export interface Permission {
  id: string; // permission name (used as id since names are unique)
  name: string;
  description: string;
}

export const groupPermissionsByModule = (permissions: Permission[]): Record<string, Permission[]> => {
  const groups: Record<string, Permission[]> = {};
  permissions.forEach((p) => {
    const m = moduleOfPermission(p.name);
    if (!groups[m]) groups[m] = [];
    groups[m].push(p);
  });
  return groups;
};

export const moduleOfPermission = (permissionName: string): string => {
  // get the first underscore index : admin_edit_billing_report (index = 5)
  const idx = permissionName.indexOf("_");
  // if no underscore then index = -1
  if (idx === -1) return "Other";
  // cutting a piece out of text like slicing a loaf of bread) starts right after that first underscore
  // then replace every = /g underscore = /_ to empty space
  return permissionName.slice(idx + 1).replace(/_/g, " ");
};
```

# Record combined with Enum
```typescript

```

