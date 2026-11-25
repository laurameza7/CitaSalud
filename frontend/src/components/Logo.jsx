// Isotipo de CitaSalud: cruz médica dentro de un escudo redondeado.
export default function Logo({ tamano = 34 }) {
  return (
    <svg width={tamano} height={tamano} viewBox="0 0 40 40" aria-hidden="true">
      <defs>
        <linearGradient id="logo-grad" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0" stopColor="#14b8a6" />
          <stop offset="1" stopColor="#0e7490" />
        </linearGradient>
      </defs>
      <rect x="1" y="1" width="38" height="38" rx="11" fill="url(#logo-grad)" />
      <path d="M16 9h8v7h7v8h-7v7h-8v-7H9v-8h7z" fill="#fff" />
      <circle cx="29.5" cy="29.5" r="4.5" fill="#fff" />
      <path d="M27.6 29.6l1.3 1.3 2.6-2.7" stroke="#0e7490" strokeWidth="1.6" fill="none" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}
