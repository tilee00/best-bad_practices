# project structure

### App.tsx
```tsx
import { store } from "./redux-toolkit";
import { router } from "./router/routes";

function App() {
  return (
    <StyleProvider layer>
      <ConfigProvider
        getPopupContainer={(trigger) => trigger?.parentElement || document.body}
        theme={{
          token: {
            colorPrimary: "#034a25",
            colorPrimaryActive: "#023618",
            colorPrimaryHover: "#025929",

            colorSuccess: "#047857",
            colorWarning: "#ffaa00",
            colorError: "#ee443f",
            colorInfo: "#4e61f6",
          },
        }}
      >
        <AntdApp>
          <Provider store={store}>
            <Suspense fallback={<div>Loading...</div>}>
              <RouterProvider router={router} />
            </Suspense>
          </Provider>
        </AntdApp>
      </ConfigProvider>
    </StyleProvider>
  );
}

export default App;
```

### routes.tsx
```tsx

const RolesAndPermissions = lazy(() => import("../modules/roles-permissions/RolesAndPermissions"));
const StaffList = lazy(() => import("../modules/staff/StaffList"));
const CreateStaff = lazy(() => import("../modules/staff/CreateStaff"));
const EditStaff = lazy(() => import("../modules/staff/EditStaff"));
const basename = import.meta.env.VITE_BASE_PATH === "/" ? undefined : import.meta.env.VITE_BASE_PATH;

export const router = createBrowserRouter(
  [
    { path: "/account", Component: RolesAndPermissions },
    { path: "/account/roles-permissions", Component: RolesAndPermissions },
    { path: "/account/staff", Component: StaffList },
    { path: "/account/staff/create", Component: CreateStaff },
    { path: "/account/staff/edit/:staffId", Component: EditStaff },
    {
      path: "*",
      Component: () => {
        const base =
          import.meta.env.VITE_BASE_PATH && import.meta.env.VITE_BASE_PATH !== "/"
            ? import.meta.env.VITE_BASE_PATH
            : "";
        const target = `${base}/dashboard`;
        useEffect(() => {
          if (window.location.pathname !== target) {
            window.history.replaceState(null, "", target);
            window.dispatchEvent(new PopStateEvent("popstate"));
          }
        }, [target]);
        return null;
      },
    },
  ],
  { basename }
);
```

### RolesAndPermissions.tsx
> modules
```tsx

import {
  useGetPermissionsQuery,
  useGetRolesQuery,
  useCreateRoleMutation,
  useUpdateRoleMutation,
  useDeleteRoleMutation,
} from "../../redux-toolkit/rtk-query";
import { derivePermissionList } from "../../redux-toolkit/transformers";
import type { Role } from "../../redux-toolkit/fe.type";
import EditRoleModal from "./EditRoleModal";
import PermissionsTab from "./PermissionsTab";

type TabValue = "roles" | "permissions";

export default function RolesAndPermissions() {
  const [activeTab, setActiveTab] = useState<TabValue>("roles");
  const [addingName, setAddingName] = useState<string | null>(null);
  const [editing, setEditing] = useState<Role | null>(null);
  const [pendingDelete, setPendingDelete] = useState<Role | null>(null);

  const { data, isLoading, isFetching } = useGetRolesQuery();
  const { data: permissionsData } = useGetPermissionsQuery();
  const [createRole, { isLoading: isCreating }] = useCreateRoleMutation();
  const [updateRole, { isLoading: isUpdating }] = useUpdateRoleMutation();
  const [deleteRole, { isLoading: isDeleting }] = useDeleteRoleMutation();

  const roles = useMemo(() => data?.roles ?? [], [data]);
  // Prefer the real /permissions response; fall back to deriving from loaded roles
  const allPermissions = useMemo(
    () => permissionsData?.permissions ?? derivePermissionList(roles).permissions,
    [permissionsData, roles]
  );

  const handleStartAdd = () => setAddingName("");

  const handleConfirmAddName = async () => {
    const trimmed = (addingName ?? "").trim();
    if (!trimmed) {
      setAddingName(null);
      return;
    }
    // Per requirement: name entered → row appears in table, but role is not yet
    // created. Open the EditRoleModal so the user can pick permissions and Save.
    setEditing({
      id: "",
      name: trimmed,
      permissions: [],
      createdAt: null,
      modifiedAt: null,
      editable: true,
    });
    setAddingName(null);
  };

  const handleSaveRole = async (form: { name: string; permissionNames: string[] }) => {
    if (!editing) return;
    try {
      if (editing.id) {
        // Without .unwrap() : save it and immediately walk away assuming it success, even if it eventually fail
        // With .unwrap() : save and wait for success confirmation. If crashes,immediately get an alert
        await updateRole({ id: editing.id, ...form }).unwrap();
      } else {
        await createRole(form).unwrap();
      }
      setEditing(null);
    } catch {
      // toast middleware surfaces success / error
    }
  };

  const handleConfirmDelete = async () => {
    if (!pendingDelete) return;
    try {
      await deleteRole(pendingDelete.id).unwrap();
      setPendingDelete(null);
    } catch {
      // toast middleware surfaces success / error
    }
  };

  const roleColumns: DataTableColumn<Role>[] = [
    {
      key: "name",
      title: "Roles",
      dataIndex: "name",
    },
    {
      key: "actions",
      title: "",
      dataIndex: "id",
      width: 100,
      // for every row, 
      // first position _v (v: value of that single cell; _ underscore: mean unused variable;)
      // second position record (whole package of data for that entire row)
      render: (_v, record) => (
        <div className="flex items-center justify-end gap-3">
          <AclController scopes={["UPDATE_ROLE"]}>
            <button
              type="button"
              onClick={() => setEditing(record)}
              className="font-semibold text-primary hover:underline cursor-pointer"
              aria-label={`Edit ${record.name}`}
            >
              Edit
            </button>
          </AclController>
          <AclController scopes={["DELETE_ROLE"]}>
            <button
              type="button"
              onClick={() => {
                if (record.editable) {
                  setPendingDelete(record);
                }
              }}
              className={`text-red-600 hover:text-red-800 ${!record.editable ? "cursor-not-allowed opacity-50" : "cursor-pointer"}`}
              aria-label={`Delete ${record.name}`}
              disabled={!record.editable}
            >
              <DeleteOutlined />
            </button>
          </AclController>
        </div>
      ),
    },
  ];

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <LoadingSpinner />
      </div>
    );
  }

  return (
    <section className="w-full flex flex-col min-h-full">
      <div className="flex-1 w-full pb-10 flex flex-col gap-6">
        {/* leading (line-height) = tight (vertical distance between two lines of text) */}
        {/* tracking (letter-spacing) = tight (horizontal gap between letters in a word) */}
        <h1 className="text-2xl font-semibold leading-tight tracking-tight text-gray-900">Roles &amp; Permissions</h1>

        <div className="flex items-center justify-between gap-4">
          <Segmented
            options={[
              { label: "Roles", value: "roles" },
              { label: "Permissions", value: "permissions" },
            ]}
            value={activeTab}
            onChange={(val) => setActiveTab(val as TabValue)}
          />
          {activeTab === "roles" && (
            <AclController scopes={["CREATE_ROLE"]}>
              <PrimaryAddButton label="Add Roles" onClick={handleStartAdd} disabled={addingName !== null} />
            </AclController>
          )}
        </div>

        {activeTab === "roles" && (
          <AclController scopes={["VIEW_ROLE"]}>
            <>
              <DataTable<Role>
                columns={roleColumns}
                dataSource={roles}
                rowKey="id"
                pagination={false}
                loading={isFetching}
              />
              {addingName !== null && (
                <div className="flex flex-col gap-2">
                  <label htmlFor="new-role-name" className="text-sm font-medium text-gray-900">
                    Enter Role Name
                  </label>
                  <Input
                    id="new-role-name"
                    autoFocus
                    value={addingName}
                    placeholder="Role Name"
                    onChange={(e) => setAddingName(e.target.value)}
                    onPressEnter={handleConfirmAddName}
                    onBlur={handleConfirmAddName}
                  />
                </div>
              )}
            </>
          </AclController>
        )}

        {activeTab === "permissions" && (
          <AclController scopes={["VIEW_ROLE"]}>
            <PermissionsTab roles={roles} allPermissions={allPermissions} onEditRole={(role) => setEditing(role)} />
          </AclController>
        )}
      </div>

      {editing && (
        <EditRoleModal
          key={editing.id || "new"}
          open
          role={editing}
          allPermissions={allPermissions}
          onCancel={() => setEditing(null)}
          onSave={handleSaveRole}
          saving={isCreating || isUpdating}
        />
      )}

      <ConfirmationModal
        open={pendingDelete !== null}
        title="Confirm Delete?"
        description="Once deleted, roles will not be saved"
        actions={[
          { label: "Cancel", onClick: () => setPendingDelete(null) },
          { label: isDeleting ? "Deleting..." : "Confirm & Delete", type: "danger", onClick: handleConfirmDelete },
        ]}
        onCancel={() => setPendingDelete(null)}
      />
    </section>
  );
}
```