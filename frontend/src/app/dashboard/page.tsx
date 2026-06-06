import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import Link from "next/link";

interface RoleDto {
  id: number;
  name: string;
  permissions?: string[];
}

interface UserDto {
  id: number;
  username: string;
  roleId?: number | null;
  role?: RoleDto | string | null;
  permissions?: string[];
}

async function fetchUsers(token: string): Promise<UserDto[]> {
  const apiUrl = process.env.API_INTERNAL_URL || process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";
  const res = await fetch(`${apiUrl}/api/users?expand=role`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
    cache: "no-store",
  });

  if (!res.ok) {
    return [];
  }

  const data = await res.json();
  return Array.isArray(data) ? data : (data.content ?? []);
}

async function fetchRoles(token: string): Promise<RoleDto[]> {
  const apiUrl = process.env.API_INTERNAL_URL || process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";
  const res = await fetch(`${apiUrl}/api/roles?expand=permissions`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
    cache: "no-store",
  });

  if (!res.ok) {
    return [];
  }

  const data = await res.json();
  return Array.isArray(data) ? data : (data.content ?? []);
}

export default async function DashboardPage({ searchParams }: { searchParams?: { view?: string; roleId?: string } }) {
  const token = (await cookies()).get("access_token")?.value;

  if (!token) {
    redirect("/");
  }

  const users = await fetchUsers(token);
  const roles = await fetchRoles(token);
  const rolesMap = new Map(roles.map((r) => [r.id, r]));

  // `searchParams` can be a Promise in some Next.js runtime configurations; unwrap if necessary
  let sp = searchParams as any;
  if (sp && typeof sp.then === "function") {
    sp = await sp;
  }
  const view = sp?.view;
  const selectedRoleId = sp?.roleId ? Number(sp.roleId) : undefined;

  return (
    <div className="min-h-screen bg-gray-50">
      <nav className="bg-white shadow-sm">
        <div className="max-w-5xl mx-auto px-4 py-3 flex items-center justify-between">
          <h1 className="text-xl font-bold text-gray-900">Dashboard</h1>
          <div className="flex items-center gap-4">
            <Link href="/dashboard" className="text-sm text-gray-700 hover:text-gray-900 font-medium">Users</Link>
            <Link href="/dashboard?view=roles" className="text-sm text-gray-700 hover:text-gray-900 font-medium">Roles</Link>
            <span className="text-sm text-gray-500">Authenticated</span>
            <Link href="/api/auth/logout" className="text-sm text-red-600 hover:text-red-700 font-medium">Sign Out</Link>
          </div>
        </div>
      </nav>

      <main className="max-w-5xl mx-auto px-4 py-8">
        <div className="bg-white rounded-2xl shadow-lg overflow-hidden">
          {view === 'roles' ? (
            // Roles list view
            <div>
              <div className="px-6 py-4 border-b border-gray-200">
                <h2 className="text-lg font-semibold text-gray-900">Roles</h2>
                <p className="text-sm text-gray-500">List of roles with permissions</p>
              </div>
              {roles.length === 0 ? (
                <div className="px-6 py-12 text-center text-gray-400">No roles found</div>
              ) : (
                <ul className="divide-y">
                  {roles.map((role) => (
                    <li key={role.id} className="px-6 py-4 hover:bg-gray-50 flex items-center justify-between">
                      <div>
                        <div className="text-sm font-medium text-gray-900">{role.name}</div>
                        <div className="text-xs text-gray-500">{(role.permissions ?? []).join(', ')}</div>
                      </div>
                      <a href={`/dashboard?view=roles&roleId=${role.id}`} className="text-sm text-blue-600 hover:underline">View</a>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          ) : selectedRoleId ? (
            // Role detail view
            <div>
              <div className="px-6 py-4 border-b border-gray-200">
                <h2 className="text-lg font-semibold text-gray-900">Role details</h2>
                <p className="text-sm text-gray-500">Permissions for the selected role</p>
              </div>
              {(() => {
                const role = rolesMap.get(selectedRoleId);
                if (!role) return <div className="px-6 py-12 text-center text-gray-400">Role not found</div>;
                return (
                  <div className="px-6 py-6">
                    <h3 className="text-lg font-medium">{role.name}</h3>
                    <ul className="mt-4 space-y-2">
                      {(role.permissions ?? []).map((p) => (
                        <li key={p} className="text-sm text-gray-700">• {p}</li>
                      ))}
                    </ul>
                  </div>
                );
              })()}
            </div>
          ) : (
            // Users view (default)
            <div>
              <div className="px-6 py-4 border-b border-gray-200">
                <h2 className="text-lg font-semibold text-gray-900">Users</h2>
                <p className="text-sm text-gray-500">List of all users in the system</p>
              </div>

              {users.length === 0 ? (
                <div className="px-6 py-12 text-center text-gray-400">No users found</div>
              ) : (
                <table className="w-full">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Username</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Role</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-200">
                    {users.map((user) => (
                      <tr key={user.id} className="hover:bg-gray-50">
                        <td className="px-6 py-4 text-sm text-gray-900">{user.id}</td>
                        <td className="px-6 py-4">
                          <div className="flex items-center gap-3">
                            <div className="w-8 h-8 bg-blue-100 text-blue-700 rounded-full flex items-center justify-center text-sm font-medium">{user.username.charAt(0).toUpperCase()}</div>
                            <span className="text-sm font-medium text-gray-900">{user.username}</span>
                          </div>
                        </td>
                        <td className="px-6 py-4">
                          {user.roleId ? (
                            <a href={`/dashboard?view=roles&roleId=${user.roleId}`} className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">{typeof user.role === "object" && user.role ? user.role.name : (user.role ?? "")}</a>
                          ) : (
                            <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-700">{typeof user.role === "object" && user.role ? user.role.name : (user.role ?? "")}</span>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          )}
        </div>
      </main>
    </div>
  );
}
