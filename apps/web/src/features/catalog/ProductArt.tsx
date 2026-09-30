import type { Product } from "../../shared/api/api";

const palettes = [
  ["#f8c9b6", "#ec5b44", "#fcefe8"],
  ["#d4c7f2", "#6d4bbb", "#efebfa"],
  ["#bddfce", "#2c8364", "#e6f4e9"],
  ["#f3d589", "#ca752a", "#fff4d8"],
  ["#bbd9ea", "#267ca4", "#eaf6fa"],
];

export function ProductArt({
  product,
  className = "",
}: {
  product: Pick<Product, "id" | "name">;
  className?: string;
}) {
  const [ground, ink, light] = palettes[Math.abs(product.id) % palettes.length];
  const variant = Math.abs(product.id) % 4;
  return (
    <div
      className={`product-art ${className}`}
      style={{ background: ground }}
      role="img"
      aria-label={`Illustration for ${product.name}`}
    >
      <svg
        viewBox="0 0 360 320"
        preserveAspectRatio="xMidYMid meet"
        aria-hidden="true"
      >
        <circle cx="300" cy="55" r="62" fill={light} opacity=".65" />
        <ellipse cx="180" cy="275" rx="104" ry="14" fill={ink} opacity=".14" />
        {variant === 0 && (
          <>
            <path
              d="M135 93 Q180 78 225 93 L212 123 L209 248 Q180 268 151 248 L148 123Z"
              fill={light}
              stroke={ink}
              strokeWidth="6"
            />
            <path
              d="M144 120 Q180 137 216 120"
              fill="none"
              stroke={ink}
              strokeWidth="5"
            />
            <path
              d="M180 105 C158 73 170 54 181 38 M180 108 C193 74 213 61 225 55"
              fill="none"
              stroke={ink}
              strokeWidth="6"
              strokeLinecap="round"
            />
            <path
              d="M180 72 C144 72 142 46 151 42 C172 37 181 57 180 72 M205 71 C212 44 235 37 246 47 C242 67 222 75 205 71"
              fill={ink}
            />
          </>
        )}
        {variant === 1 && (
          <>
            <path
              d="M180 74 L180 239 M131 238 H229"
              stroke={ink}
              strokeWidth="9"
              strokeLinecap="round"
            />
            <path
              d="M114 120 C113 70 133 55 180 55 C227 55 247 70 246 120Z"
              fill={light}
              stroke={ink}
              strokeWidth="6"
            />
            <circle cx="180" cy="129" r="13" fill={ink} />
            <path
              d="M150 239 Q180 224 210 239"
              fill="none"
              stroke={ink}
              strokeWidth="7"
            />
          </>
        )}
        {variant === 2 && (
          <>
            <rect
              x="111"
              y="98"
              width="138"
              height="151"
              rx="28"
              fill={light}
              stroke={ink}
              strokeWidth="6"
            />
            <circle
              cx="180"
              cy="160"
              r="39"
              fill={ground}
              stroke={ink}
              strokeWidth="6"
            />
            <circle cx="180" cy="160" r="14" fill={ink} />
            <circle cx="180" cy="222" r="8" fill={ink} />
            <path
              d="M132 112 H157"
              stroke={ink}
              strokeWidth="5"
              strokeLinecap="round"
            />
          </>
        )}
        {variant === 3 && (
          <>
            <path
              d="M120 151 Q180 106 240 151 L221 247 Q180 264 139 247Z"
              fill={light}
              stroke={ink}
              strokeWidth="6"
            />
            <ellipse
              cx="180"
              cy="152"
              rx="60"
              ry="15"
              fill={ground}
              stroke={ink}
              strokeWidth="5"
            />
            <path
              d="M180 147 C159 117 138 100 149 73 M181 144 C180 104 196 75 215 62 M185 132 C208 112 230 105 237 85"
              fill="none"
              stroke={ink}
              strokeWidth="7"
              strokeLinecap="round"
            />
            <path
              d="M151 77 C119 73 118 52 126 47 C147 42 156 61 151 77 M213 65 C211 37 237 28 246 42 C244 58 230 69 213 65 M233 88 C249 61 273 68 272 80 C266 97 246 101 233 88"
              fill={ink}
            />
          </>
        )}
      </svg>
      <span className="art-sparkle" aria-hidden="true">
        ✦
      </span>
    </div>
  );
}
