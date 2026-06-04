# Clockwise Google Play Store Listing

## App title

Clockwise: World Clock

## Short description

Compare world times, plan across timezones, and set timezone-aligned alerts.

## Full description

Clockwise makes it easy to understand time around the world.

Whether you work with a global team, call friends and family abroad, travel often, or simply need a reliable world clock, Clockwise helps you compare timezones at a glance and plan with confidence.

See your local time on a clear analog clock, browse timezones from around the world, and quickly check the time difference between your location and another city. When timing matters, create alarms and meeting reminders that stay aligned with the timezone you selected.

Clockwise is designed to feel focused, modern, and easy to browse, so you can spend less time calculating time differences and more time connecting with people.

## Key features

- Browse world clocks and timezones in one clean list
- Search by city, country, or timezone ID
- Compare local time with cities around the world
- View UTC offsets and date differences at a glance
- Set alarms for a selected timezone
- Create meeting reminders across timezones
- Handle daylight saving time changes more confidently
- Enjoy a polished dark interface built for quick reading

## Suggested Play Store highlights

- Know the right time, anywhere
- Plan global calls without the mental math
- Keep alarms and reminders aligned with local timezones

## Graphic assets

The upload-ready Play Store assets are in `docs/play-store-assets/`.

- App icon: `app-icon-512.png` — 512 × 512 PNG
- Feature graphic: `feature-graphic-1024x500.png` — 1024 × 500 PNG
- Phone screenshots: `phone-screenshots/` — four 1080 × 1920 PNG images

The raw emulator captures are kept in `docs/play-store-assets/raw/`. Regenerate the polished assets with:

```bash
NODE_PATH=/Users/johnreicabunas/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules \
/Users/johnreicabunas/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/bin/node \
docs/play-store-assets/generate-assets.mjs
```

## Screenshot order and alt text

1. `01-world-time-clear.png`
   Caption: World time, beautifully clear
   Alt text: Clockwise local time screen showing a clear analog clock for Manila.

2. `02-browse-timezones.png`
   Caption: Browse timezones with ease
   Alt text: Clockwise world clock list comparing cities and UTC offsets.

3. `03-timezone-alerts.png`
   Caption: Keep alerts aligned
   Alt text: Clockwise schedules screen showing a timezone-aligned alarm.

4. `04-plan-across-timezones.png`
   Caption: Plan across timezones
   Alt text: Clockwise schedule editor showing a timezone conversion preview.
