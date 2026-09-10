# MyPlanner — Android Productivity Planner

Native Android personal planner. Kotlin · Jetpack Compose · Material 3 · Room · WorkManager. Offline-first, no accounts, no cloud.

**Philosophy:** Open → see what needs doing → execute. Not a tool you have to manage.

---

## Features

### Today
- Date header with day name, date, progress bar (done/total)
- Today's tasks ordered by time then priority
- Collapsible completed-tasks section (collapsed by default)
- Empty state with add button

### Tasks
- Create / edit / complete / postpone / delete
- **Material 3 DatePicker dialog** — no free-text date input
- **Material 3 TimePicker dialog** — no free-text time input
- Filter chips: الكل / اليوم / القادمة / المتأخرة / المكتملة
- Compact TaskRow: priority dot, recurrence chip (horizontal), overflow menu

### Projects
- Create / edit / delete with confirmation dialog
- Delete keeps tasks (removes project association only)
- Live progress bar per project

### Habits
- Binary or quantity targets, daily/weekly/specific-days frequency
- Check off from Today or Habits tab
- Streak tracking, reminders

### Eisenhower Matrix
- Q1–Q4 auto-classification (importance + urgency)
- **Readable design**: light tinted fills, dark text, accent border — not saturated backgrounds
- Move tasks between quadrants (writes real data)

### Smart Plan Import
- File picker + **paste text directly** in the app
- TXT and CSV formats
- 8-step pipeline: parse → validate → date sanity → preview → confirm → import
- **Date sanity**: old dates (>1 year) flagged, unchecked by default
- **Anomaly detection**: ≥40% suspicious dates triggers warning banner
- In-app format documentation with copyable examples
- Duplicate protection, keeps project/time/duration/notes/recurrence

### Calendar
- Month view with navigation
- Days with tasks show a dot indicator
- Tap day → see that day's tasks (same data source as Today)
- Back → More root

### Notifications
- Per-task start-time reminders (WorkManager)
- Once-a-day overdue digest
- Daily review reminder (opt-in)
- Per-habit reminders

### Home-screen Widget
- Top 3 today's tasks with completion toggle

### Backup & Restore
- Full export/import to JSON file
- Preview before restore

### Settings
Sections: Appearance (theme) · Task Experience (completion sound) · Navigation · Notifications · Data

### Theme
- **Light is the default** (bright, clean, productivity-first)
- Dark and System modes available
- Persists across restarts

### Completion Sound
- Short 880Hz chime (300ms) on task completion
- Toggle in Settings
- Only fires on explicit user completion, never on DB refresh or recomposition

---

## Technical stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose |
| Database | Room v6 (explicit migrations) |
| Background | WorkManager |
| Widget | Jetpack Glance |
| Settings | Room (SettingsEntity) |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 35 |

---

## Database

Version 6 — all migrations explicit (`MIGRATION_5_6` adds `completionSoundEnabled`).
`fallbackToDestructiveMigration()` is **not used**. User data is safe on upgrade.

---

## Building

```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

```bash
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest
```

---

## Known limitations

- Habit edit-after-creation not available (archive + recreate)
- Widget has one layout size only
- Main tabs customization UI planned (Settings entry present, full drag-reorder not yet implemented)
- Completion sound requires `res/raw/task_complete.wav` (included as generated 300ms tone; replace with a professionally-recorded chime before store release)
