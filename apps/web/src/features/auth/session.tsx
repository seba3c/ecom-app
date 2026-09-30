import {
  createContext,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import { api, type UserInfo } from "../../shared/api/api";

type Session = {
  user: UserInfo | null;
  loading: boolean;
  signIn: (username: string, password: string) => Promise<UserInfo>;
  signOut: () => Promise<void>;
  refresh: () => Promise<void>;
};
const SessionContext = createContext<Session | null>(null);

export function SessionProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserInfo | null>(null);
  const [loading, setLoading] = useState(true);
  async function refresh() {
    try {
      const result = await api.currentUser();
      setUser("roles" in result ? result : null);
    } catch {
      setUser(null);
    } finally {
      setLoading(false);
    }
  }
  useEffect(() => {
    void refresh();
  }, []);
  const value = useMemo<Session>(
    () => ({
      user,
      loading,
      refresh,
      async signIn(username, password) {
        const next = await api.signIn(username, password);
        setUser(next);
        return next;
      },
      async signOut() {
        await api.signOut();
        setUser(null);
      },
    }),
    [user, loading],
  );
  return (
    <SessionContext.Provider value={value}>{children}</SessionContext.Provider>
  );
}

export function useSession() {
  const context = useContext(SessionContext);
  if (!context) throw new Error("SessionProvider is required");
  return context;
}
