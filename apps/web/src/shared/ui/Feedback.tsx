import { ArrowRight, PackageOpen } from "lucide-react";
import { Link } from "react-router";

export function EmptyState({
  title,
  text,
  link,
  linkText,
}: {
  title: string;
  text: string;
  link?: string;
  linkText?: string;
}) {
  return (
    <div className="empty-state">
      <div className="empty-icon">
        <PackageOpen size={30} />
      </div>
      <h3>{title}</h3>
      <p>{text}</p>
      {link && (
        <Link className="button button-dark" to={link}>
          {linkText || "Explore the shop"} <ArrowRight size={16} />
        </Link>
      )}
    </div>
  );
}

export function PageLoader() {
  return (
    <div className="page-loader">
      <div className="loader-orb" />
      <p>Gathering the good things…</p>
    </div>
  );
}

export function ErrorState({
  message,
  retry,
}: {
  message: string;
  retry?: () => void;
}) {
  return (
    <div className="empty-state">
      <h3>Something got in the way.</h3>
      <p>{message}</p>
      {retry && (
        <button className="button button-dark" onClick={retry}>
          Try again <ArrowRight size={16} />
        </button>
      )}
    </div>
  );
}
