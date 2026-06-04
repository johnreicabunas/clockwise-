import fs from "node:fs/promises";
import path from "node:path";
import { createRequire } from "node:module";

const require = createRequire(import.meta.url);
const sharp = require("sharp");

const root = path.dirname(new URL(import.meta.url).pathname);
const rawDir = path.join(root, "raw");
const screenshotsDir = path.join(root, "phone-screenshots");

await fs.mkdir(screenshotsDir, { recursive: true });

const palette = {
  background: "#121116",
  surface: "#1B1A21",
  raised: "#24232B",
  coral: "#FF5A72",
  violet: "#8D7CFF",
  cyan: "#55D6E8",
  text: "#F8F7FB",
  muted: "#9C99A8",
};

function svg(width, height, body, background = palette.background) {
  return Buffer.from(`
    <svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="${height}" viewBox="0 0 ${width} ${height}">
      <rect width="${width}" height="${height}" fill="${background}"/>
      ${body}
    </svg>
  `);
}

function clockMark(cx, cy, radius, strokeScale = 1) {
  return `
    <circle cx="${cx}" cy="${cy}" r="${radius}" fill="${palette.raised}" stroke="${palette.text}" stroke-width="${5 * strokeScale}"/>
    <ellipse cx="${cx}" cy="${cy}" rx="${radius * 0.45}" ry="${radius}" fill="none" stroke="${palette.text}" stroke-opacity="0.72" stroke-width="${2.5 * strokeScale}"/>
    <path d="M ${cx - radius} ${cy} H ${cx + radius} M ${cx - radius * 0.8} ${cy - radius * 0.42} H ${cx + radius * 0.8} M ${cx - radius * 0.8} ${cy + radius * 0.42} H ${cx + radius * 0.8}"
      fill="none" stroke="${palette.cyan}" stroke-linecap="round" stroke-width="${2.5 * strokeScale}"/>
    <path d="M ${cx} ${cy} V ${cy - radius * 0.5} M ${cx} ${cy} L ${cx + radius * 0.42} ${cy + radius * 0.22}"
      fill="none" stroke="${palette.coral}" stroke-linecap="round" stroke-width="${4 * strokeScale}"/>
    <circle cx="${cx}" cy="${cy}" r="${5 * strokeScale}" fill="${palette.coral}"/>
  `;
}

const icon = svg(
  512,
  512,
  `
    <defs>
      <radialGradient id="glow" cx="65%" cy="25%" r="80%">
        <stop offset="0%" stop-color="${palette.violet}" stop-opacity="0.45"/>
        <stop offset="100%" stop-color="${palette.background}" stop-opacity="0"/>
      </radialGradient>
    </defs>
    <rect width="512" height="512" fill="url(#glow)"/>
    <circle cx="256" cy="256" r="158" fill="${palette.surface}" stroke="${palette.text}" stroke-width="18"/>
    <ellipse cx="256" cy="256" rx="72" ry="158" fill="none" stroke="${palette.text}" stroke-opacity="0.7" stroke-width="10"/>
    <path d="M98 256H414 M126 188H386 M126 324H386" fill="none" stroke="${palette.cyan}" stroke-linecap="round" stroke-width="10"/>
    <path d="M256 256V172 M256 256L330 294" fill="none" stroke="${palette.coral}" stroke-linecap="round" stroke-width="18"/>
    <circle cx="256" cy="256" r="21" fill="${palette.coral}"/>
  `,
);

await sharp(icon)
  .ensureAlpha()
  .png()
  .toFile(path.join(root, "app-icon-512.png"));

const featureGraphic = svg(
  1024,
  500,
  `
    <defs>
      <linearGradient id="featureBg" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0%" stop-color="${palette.violet}"/>
        <stop offset="48%" stop-color="${palette.background}"/>
        <stop offset="100%" stop-color="${palette.coral}"/>
      </linearGradient>
      <radialGradient id="featureGlow" cx="35%" cy="35%" r="75%">
        <stop offset="0%" stop-color="${palette.cyan}" stop-opacity="0.28"/>
        <stop offset="100%" stop-color="${palette.background}" stop-opacity="0"/>
      </radialGradient>
    </defs>
    <rect width="1024" height="500" fill="url(#featureBg)"/>
    <rect width="1024" height="500" fill="url(#featureGlow)"/>
    <circle cx="790" cy="250" r="190" fill="${palette.surface}" fill-opacity="0.86"/>
    ${clockMark(790, 250, 145, 1.6)}
    <text x="92" y="205" fill="${palette.text}" font-family="Arial, Helvetica, sans-serif" font-size="68" font-weight="700">Clockwise</text>
    <text x="92" y="270" fill="${palette.text}" font-family="Arial, Helvetica, sans-serif" font-size="34" font-weight="600">World time, made clear</text>
    <text x="92" y="325" fill="${palette.text}" fill-opacity="0.76" font-family="Arial, Helvetica, sans-serif" font-size="25">Compare timezones. Plan with confidence.</text>
  `,
);

await sharp(featureGraphic)
  .removeAlpha()
  .png()
  .toFile(path.join(root, "feature-graphic-1024x500.png"));

const screenshotSpecs = [
  {
    source: "home.png",
    output: "01-world-time-clear.png",
    title: "World time, beautifully clear",
    subtitle: "See your local time and timezone at a glance",
    accent: palette.coral,
    cropTop: 120,
  },
  {
    source: "world-clocks.png",
    output: "02-browse-timezones.png",
    title: "Browse timezones with ease",
    subtitle: "Compare cities, countries, and UTC offsets",
    accent: palette.cyan,
    cropTop: 850,
  },
  {
    source: "schedules.png",
    output: "03-timezone-alerts.png",
    title: "Keep alerts aligned",
    subtitle: "Set alarms and reminders for the right timezone",
    accent: palette.violet,
    cropTop: 120,
  },
  {
    source: "editor.png",
    output: "04-plan-across-timezones.png",
    title: "Plan across timezones",
    subtitle: "Preview conversions before you save",
    accent: palette.coral,
    cropTop: 120,
  },
];

for (const spec of screenshotSpecs) {
  const screen = await sharp(path.join(rawDir, spec.source))
    .extract({ left: 0, top: spec.cropTop, width: 1280, height: 2856 - spec.cropTop })
    .resize(1080, 1600, { fit: "cover", position: "top" })
    .png()
    .toBuffer();

  const caption = svg(
    1080,
    320,
    `
      <defs>
        <linearGradient id="captionBg" x1="0" y1="0" x2="1" y2="0">
          <stop offset="0%" stop-color="${palette.background}"/>
          <stop offset="100%" stop-color="${palette.raised}"/>
        </linearGradient>
      </defs>
      <rect width="1080" height="320" fill="url(#captionBg)"/>
      <rect x="70" y="78" width="84" height="8" rx="4" fill="${spec.accent}"/>
      <text x="70" y="158" fill="${palette.text}" font-family="Arial, Helvetica, sans-serif" font-size="54" font-weight="700">${spec.title}</text>
      <text x="70" y="218" fill="${palette.muted}" font-family="Arial, Helvetica, sans-serif" font-size="29">${spec.subtitle}</text>
    `,
  );

  await sharp({
    create: {
      width: 1080,
      height: 1920,
      channels: 3,
      background: palette.background,
    },
  })
    .composite([
      { input: caption, top: 0, left: 0 },
      { input: screen, top: 320, left: 0 },
    ])
    .removeAlpha()
    .png()
    .toFile(path.join(screenshotsDir, spec.output));
}

console.log("Generated Play Store assets in", root);
