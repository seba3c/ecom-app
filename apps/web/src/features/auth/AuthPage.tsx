import { useEffect, useState, type FormEvent } from "react";
import { ArrowRight, Check } from "lucide-react";
import { Link, useNavigate, useSearchParams } from "react-router";
import { api } from "../../shared/api/api";
import { useSession } from "./session";

function safeNext(value: string | null) {
  return value?.startsWith("/") && !value.startsWith("//") ? value : "/";
}

export function AuthPage({ mode }: { mode: "signin" | "signup" }) {
  const [params] = useSearchParams();
  const { user, signIn } = useSession();
  const navigate = useNavigate();
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const [registered, setRegistered] = useState(false);
  const next = safeNext(params.get("next"));
  useEffect(() => {
    if (user)
      navigate(
        next === "/" && user.roles.includes("ROLE_ADMIN") ? "/admin" : next,
        { replace: true },
      );
  }, [user, next, navigate]);
  async function submit(event: FormEvent) {
    event.preventDefault();
    setError("");
    setBusy(true);
    try {
      if (mode === "signup") {
        await api.signUp(username.trim(), email.trim(), password);
        setRegistered(true);
      } else {
        const signedIn = await signIn(username.trim(), password);
        navigate(
          next === "/" && signedIn.roles.includes("ROLE_ADMIN")
            ? "/admin"
            : next,
          { replace: true },
        );
      }
    } catch (error) {
      setError(
        error instanceof Error ? error.message : "Something went wrong.",
      );
    } finally {
      setBusy(false);
    }
  }
  return (
    <div className="auth-page">
      <div className="auth-art">
        <span className="auth-sun">✳</span>
        <div className="auth-art-copy">
          <span className="eyebrow">WELCOME TO GOODTHINGS</span>
          <h2>
            More color.
            <br />
            More joy.
            <br />
            <em>More you.</em>
          </h2>
          <p>Sign in to save your finds and make the good things yours.</p>
        </div>
      </div>
      <div className="auth-panel">
        <div className="auth-card">
          {registered ? (
            <>
              <div className="success-icon">
                <Check size={26} />
              </div>
              <h1>You're on the list!</h1>
              <p>Your account is ready. Sign in to start shopping.</p>
              <Link
                className="button button-dark"
                to={`/signin?next=${encodeURIComponent(next)}`}
              >
                Sign in <ArrowRight size={17} />
              </Link>
            </>
          ) : (
            <>
              <span className="eyebrow purple">
                {mode === "signin" ? "WELCOME BACK" : "JOIN THE GOOD SIDE"}
              </span>
              <h1>{mode === "signin" ? "Hello again." : "Make it yours."}</h1>
              <p>
                {mode === "signin"
                  ? "Your next favorite is waiting."
                  : "Create an account and collect the things you love."}
              </p>
              <form onSubmit={submit} className="form-stack">
                <label>
                  Username
                  <input
                    required
                    autoComplete="username"
                    minLength={mode === "signup" ? 3 : undefined}
                    maxLength={mode === "signup" ? 20 : undefined}
                    value={username}
                    onChange={(event) => setUsername(event.target.value)}
                    placeholder="Your username"
                  />
                </label>
                {mode === "signup" && (
                  <label>
                    Email address
                    <input
                      type="email"
                      required
                      autoComplete="email"
                      maxLength={50}
                      value={email}
                      onChange={(event) => setEmail(event.target.value)}
                      placeholder="you@example.com"
                    />
                  </label>
                )}
                <label>
                  Password
                  <input
                    type="password"
                    required
                    minLength={mode === "signup" ? 8 : undefined}
                    maxLength={mode === "signup" ? 40 : undefined}
                    autoComplete={
                      mode === "signup" ? "new-password" : "current-password"
                    }
                    value={password}
                    onChange={(event) => setPassword(event.target.value)}
                    placeholder="Your password"
                  />
                </label>
                {error && (
                  <div className="form-error" role="alert">
                    {error}
                  </div>
                )}
                <button
                  className="button button-dark full-width"
                  disabled={busy}
                >
                  {busy
                    ? "Just a moment…"
                    : mode === "signin"
                      ? "Sign in"
                      : "Create account"}{" "}
                  <ArrowRight size={18} />
                </button>
              </form>
              <p className="auth-switch">
                {mode === "signin"
                  ? "New around here?"
                  : "Already have an account?"}{" "}
                <Link
                  to={`${mode === "signin" ? "/signup" : "/signin"}?next=${encodeURIComponent(next)}`}
                >
                  {mode === "signin" ? "Create an account" : "Sign in"}
                </Link>
              </p>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
