interface GradientPalette {
  start: string;
  end: string;
}

function hashString(value: string): number {
  let hash = 0;
  for (let i = 0; i < value.length; i += 1) {
    hash = (hash << 5) - hash + value.charCodeAt(i);
    hash |= 0;
  }
  return Math.abs(hash);
}

function gradientCoordinates(angleDegrees: number): { x1: string; y1: string; x2: string; y2: string } {
  const radians = (angleDegrees * Math.PI) / 180;
  const x = Math.cos(radians) * 50;
  const y = Math.sin(radians) * 50;
  return {
    x1: `${50 - x}%`,
    y1: `${50 - y}%`,
    x2: `${50 + x}%`,
    y2: `${50 + y}%`,
  };
}

function getPalette(categoryId: number): GradientPalette {
  if (categoryId === 1) return { start: '#6366f1', end: '#8b5cf6' };
  if (categoryId === 2) return { start: '#f59e0b', end: '#ea580c' };
  if (categoryId === 3) return { start: '#f43f5e', end: '#ec4899' };
  if (categoryId === 4) return { start: '#14b8a6', end: '#10b981' };
  if (categoryId === 5) return { start: '#0ea5e9', end: '#06b6d4' };
  return { start: '#6366f1', end: '#8b5cf6' };
}

function getIconMarkup(categoryId: number): string {
  if (categoryId === 1) {
    return `
      <g stroke="rgba(255,255,255,0.45)" stroke-width="8" stroke-linecap="round" fill="none">
        <rect x="145" y="95" width="110" height="110" rx="14"/>
        <line x1="172" y1="95" x2="172" y2="70"/>
        <line x1="228" y1="95" x2="228" y2="70"/>
        <line x1="172" y1="205" x2="172" y2="230"/>
        <line x1="228" y1="205" x2="228" y2="230"/>
        <line x1="145" y1="122" x2="120" y2="122"/>
        <line x1="145" y1="178" x2="120" y2="178"/>
        <line x1="255" y1="122" x2="280" y2="122"/>
        <line x1="255" y1="178" x2="280" y2="178"/>
      </g>
      <g fill="rgba(255,255,255,0.38)">
        <circle cx="120" cy="122" r="6"/>
        <circle cx="120" cy="178" r="6"/>
        <circle cx="280" cy="122" r="6"/>
        <circle cx="280" cy="178" r="6"/>
      </g>
    `;
  }

  if (categoryId === 2) {
    return `
      <g fill="none" stroke="rgba(255,255,255,0.43)" stroke-width="8" stroke-linecap="round" stroke-linejoin="round">
        <path d="M105 214V102c45 0 72 10 95 28v111c-23-18-50-27-95-27z"/>
        <path d="M295 214V102c-45 0-72 10-95 28v111c23-18 50-27 95-27z"/>
      </g>
      <g stroke="rgba(255,255,255,0.32)" stroke-width="5" stroke-linecap="round">
        <line x1="127" y1="138" x2="178" y2="148"/>
        <line x1="127" y1="166" x2="178" y2="176"/>
        <line x1="222" y1="148" x2="273" y2="138"/>
        <line x1="222" y1="176" x2="273" y2="166"/>
      </g>
    `;
  }

  if (categoryId === 3) {
    return `
      <g fill="none" stroke="rgba(255,255,255,0.44)" stroke-width="8" stroke-linecap="round" stroke-linejoin="round">
        <path d="M200 97c0-17 24-17 24 0 0 8-5 14-10 18l-15 10"/>
        <path d="M136 168h128c17 0 31 14 31 31v16H105v-16c0-17 14-31 31-31z"/>
        <line x1="136" y1="168" x2="172" y2="128"/>
        <line x1="264" y1="168" x2="228" y2="128"/>
      </g>
    `;
  }

  if (categoryId === 4) {
    return `
      <g fill="none" stroke="rgba(255,255,255,0.44)" stroke-width="8" stroke-linecap="round" stroke-linejoin="round">
        <path d="M118 168l82-66 82 66"/>
        <rect x="146" y="168" width="108" height="72" rx="10"/>
        <rect x="188" y="190" width="24" height="50" rx="5"/>
      </g>
      <g fill="rgba(255,255,255,0.28)">
        <rect x="162" y="182" width="16" height="14" rx="3"/>
        <rect x="222" y="182" width="16" height="14" rx="3"/>
      </g>
    `;
  }

  return `
    <g fill="none" stroke="rgba(255,255,255,0.44)" stroke-width="8" stroke-linecap="round" stroke-linejoin="round">
      <polygon points="200,92 224,146 283,151 239,190 252,247 200,217 148,247 161,190 117,151 176,146"/>
    </g>
    <g fill="rgba(255,255,255,0.36)">
      <circle cx="160" cy="208" r="8"/>
      <circle cx="240" cy="208" r="8"/>
      <circle cx="200" cy="170" r="6"/>
    </g>
  `;
}

export function getProductImageUri(productName: string, categoryId: number): string {
  const hash = hashString(productName.trim().toLowerCase());
  const angle = 120 + (hash % 61);
  const patternOffsetX = 6 + (hash % 12);
  const patternOffsetY = 6 + ((hash >> 3) % 12);
  const palette = getPalette(categoryId);
  const { x1, y1, x2, y2 } = gradientCoordinates(angle);
  const iconMarkup = getIconMarkup(categoryId);

  const svg = `
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 400 300" width="400" height="300">
      <defs>
        <linearGradient id="bg" x1="${x1}" y1="${y1}" x2="${x2}" y2="${y2}">
          <stop offset="0%" stop-color="${palette.start}" />
          <stop offset="100%" stop-color="${palette.end}" />
        </linearGradient>
        <pattern id="dots" width="30" height="30" patternUnits="userSpaceOnUse">
          <circle cx="${patternOffsetX}" cy="${patternOffsetY}" r="2.2" fill="rgba(255,255,255,0.12)" />
        </pattern>
      </defs>
      <rect width="400" height="300" fill="url(#bg)" />
      <rect width="400" height="300" fill="url(#dots)" />
      ${iconMarkup}
    </svg>
  `;

  return `data:image/svg+xml,${encodeURIComponent(svg)}`;
}
