# fetch data from Rest API 

### rtk-query
```typescript
const IAM_BASE = Url.concat(import.meta.env.VITE_API_BASE_URL, "/my/bd_merchant_iam", import.meta.env.VITE_API_VERSION);

export const accountApi = createApi({
  // reducerPath give a unique label to store the data fetch under createApi
  reducerPath: "account-RtkQueryApi",
  // tagTypes: same tag type group data together, refresh group data when one of it change
  tagTypes: ["Role", "Permission", "Staff", "AdminRole", "AdminStaff", "StaffSession", "TwoFactor"],
  baseQuery: createBaseQueryWithReauth({
    // baseUrl: where all requests start
    baseUrl: IAM_BASE,
    timeout: import.meta.env.VITE_API_TIMEOUT ? Number(import.meta.env.VITE_API_TIMEOUT) : 10000,
    prepareHeaders: (headers) => {
      headers.set("Content-Type", "application/json");
      return headers;
    },
  }),
  endpoints: (builder) => ({
    // builder.query: read data from the server
    getPermissions: builder.query<PermissionList, void>({
      query: () => "/permissions",
      // transformResponse: transform backend return response into requested format
      transformResponse: (response: IPermissionListResponse) => transformPermissionList(response),
      providesTags: ["Permission"],
    }),

    getRoles: builder.query<RoleList, { page?: number; size?: number } | void>({
      query: (arg) => {
        const page = arg?.page ?? 0;
        const size = arg?.size ?? 50;
        return `/roles?page=${page}&size=${size}`;
      },
      transformResponse: (response: IRoleListResponse) => transformRoleList(response),
      providesTags: ["Role"],
    }),

    // GET /roles/:id
    getRoleById: builder.query<Role, string>({
      query: (id) => `/roles/${encodeURIComponent(id)}`,
      transformResponse: (response: IRoleResponse) => transformRole(response),
      providesTags: (_r, _e, id) => [{ type: "Role", id }],
    }),

    // builder.mutation: saving/deleting data
    createRole: builder.mutation<Role, CreateRoleForm>({
      query: (form) => ({
        url: "/roles",
        method: "POST",
        body: transformCreateRoleRequest(form) satisfies ICreateRoleRequest,
      }),
      transformResponse: (response: IRoleResponse) => transformRole(response),
      invalidatesTags: ["Role"],
    }),

    updateRole: builder.mutation<void, UpdateRoleForm>({
      query: ({ id, ...form }) => ({
        url: `/roles/${encodeURIComponent(id)}`,
        method: "PATCH",
        body: transformCreateRoleRequest(form) satisfies IUpdateRoleRequest,
      }),
      invalidatesTags: ["Role"],
    }),

    deleteRole: builder.mutation<void, string>({
      query: (id) => ({
        url: `/roles/${encodeURIComponent(id)}`,
        method: "DELETE",
      }),
      invalidatesTags: ["Role"],
    }),

    // ─── Admin (FRAP) — spec §19.14–19.27 ───────────────────────────
   
    updateAdminStaff: builder.mutation<Staff, AdminUpdateStaffForm>({
      query: ({ id, ...form }) => ({
        url: `/admin/users/${encodeURIComponent(id)}`,
        method: "PUT",
        body: transformAdminUpdateStaffRequest(form) satisfies IAdminUpdateIamUserRequest,
      }),
      transformResponse: (response: IAdminIamUserResponse) => transformAdminStaff(response),
      invalidatesTags: (_r, _e, { id }) => ["AdminStaff", { type: "AdminStaff", id }],
    }),

  }),
});
```

### transformer
```typescript

// ─── Roles & Permissions ────────────────────────────────────────────

export const transformPermission = (raw: IPermissionResponse): Permission => ({
  id: raw.name,
  name: raw.name,
  description: raw.description,
});

export const transformPermissionList = (raw: IPermissionResponse[]): PermissionList => ({
  permissions: (raw ?? []).map(transformPermission),
});

export const transformRole = (raw: IRoleResponse): Role => ({
  id: raw.id,
  name: raw.name,
  permissions: (raw.permissions ?? []).map(transformPermission),
  createdAt: raw.createdAt,
  modifiedAt: raw.modifiedAt,
  editable: raw.editable ?? true, // all roles are editable in current implementation (no built-in roles)
});

export const transformRoleList = (raw: IRoleResponse[]): RoleList => ({
  roles: (raw ?? []).map(transformRole),
});

// Fallback: aggregate unique permissions from loaded roles (used when
// GET /permissions is unavailable or loading).
export const derivePermissionList = (roles: Role[]): PermissionList => {
  const map = new Map<string, Permission>();
  roles.forEach((role) => {
    role.permissions.forEach((p) => {
      if (!map.has(p.name)) map.set(p.name, p);
    });
  });
  return { permissions: Array.from(map.values()) };
};

export const transformCreateRoleRequest = (form: CreateRoleForm): ICreateRoleRequest => ({
  name: form.name,
  permissions: form.permissionNames,
});

```


### webservice.type
```typescript
export interface IPermissionResponse {
  name: string;
  description: string;
}

export interface IRoleResponse {
  id: string;
  name: string;
  permissions: IPermissionResponse[];
  createdAt: string | null;
  modifiedAt: string | null;
  createdBy: string | null;
  modifiedBy: string | null;
  editable?: boolean;
}

export type IRoleListResponse = IRoleResponse[];
export type IPermissionListResponse = IPermissionResponse[];
```