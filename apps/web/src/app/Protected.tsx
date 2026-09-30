import { type ReactNode } from "react";
import { Navigate, useLocation } from "react-router";
import { useSession } from "../features/auth/session";

export function Protected({
  children,
  admin = false,
}: {
  children: ReactNode;
  admin?: boolean;
}) {
  const { user, loading } = useSession();
  const location = useLocation();
  if (loading) return <div className="page-loader">Checking your account…</div>;
  if (!user)
    return (
      <Navigate
        to={`/signin?next=${encodeURIComponent(location.pathname + location.search)}`}
        replace
      />
    );
  if (admin && !user.roles.includes("ROLE_ADMIN"))
    return <Navigate to="/" replace />;
  return children;
}
