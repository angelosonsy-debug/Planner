# MyPlanner — Release 1.0 Summary

## Release
Version: 1.0 | Database: v6 | Min SDK: 26 | Target SDK: 35

---

## Major Changes

### Visual Design
- **Light theme is now the default** (`themeMode = "light"` in SettingsEntity)
- Theme is user-selectable (light/dark/system) from Settings → Appearance
- Theme change persists via Room SettingsEntity and takes effect immediately
- Compact Typography scale: headlineSmall=18sp, bodyMedium=13sp, labelMedium=11sp
- Matrix readability overhaul: light quadrant tints (MatrixQ1Tint=#FFF0F0 etc.) + dark text + accent border strip

### Navigation
- `navigateToTab()` uses `saveState = false` + `restoreState = false` — tab switches always show root
- Calendar route registered in AppNavHost (`NestedRoute.CALENDAR`) — was missing, screen never opened
- More hub → children: Matrix, Import, Calendar, Settings, Backup
- Back from any child → More root

### Task Creation / Editing
- Date field: tapping opens Material 3 `DatePickerDialog` — no more YYYY-MM-DD text input
- Time field: tapping opens Material 3 `TimeInput` dialog — no more HH:MM text input
- Default date = today when creating a new task

### Tasks Filters
- `TaskFilter` enum: ALL / TODAY / UPCOMING / OVERDUE / COMPLETED
- Filter chips in TasksScreen header (horizontal scrollable row)
- Filters applied in `TasksViewModel.tasks` StateFlow using `LocalDate.now(ZoneId.systemDefault())`

### Today Screen
- Compact header: day name + date + progress fraction + 3dp progress bar
- Completed tasks: collapsible section (collapsed by default), toggle with count
- `TodayViewModel.completedExpanded: StateFlow<Boolean>`
- `TodayViewModel.soundEnabled`: observes `SettingsEntity.completionSoundEnabled`
- Date bug fix: `todayString = LocalDate.now(ZoneId.systemDefault()).toString()` — explicit local timezone

### Matrix Readability
- Quadrant fills: MatrixQ1Tint (#FFF0F0), MatrixQ2Tint (#EFF8FF), MatrixQ3Tint (#FFFBEE), MatrixQ4Tint (#F4F4F4)
- Text color always `MaterialTheme.colorScheme.onSurface` (dark) regardless of quadrant
- Move-to behavior unchanged (still writes real importance/urgency data)

### Import
- Added **Paste text** mode alongside File picker (tab selection)
- Anomaly banner: shown when ≥40% of dated items have suspicious dates
- Format help bottom sheet with copyable TXT and CSV examples
- ImportDateSanityChecker: VALID / RECENT_PAST / OLD_PAST / VERY_OLD / FAR_FUTURE
- OLD_PAST and VERY_OLD items unchecked by default in preview
- datePlausibility stored in ValidatedImportItem for UI coloring (not added to issues list)

### Completion Sound
- `CompletionSoundPlayer.kt` in `ui/util/` — SoundPool-based
- `task_complete.wav`: generated 880Hz A5 tone, 300ms, fade in/out
- Initialised in `PlannerApp.onCreate()`
- Called only on `PENDING → COMPLETED` transition in TaskRow checkbox
- Toggle: Settings → Task Experience → "صوت إنجاز المهام"
- `SettingsEntity.completionSoundEnabled` (default true)
- `MIGRATION_5_6` adds the column

### Settings Screen
- Sections: Appearance / Task Experience / Navigation / Notifications / Data
- Theme picker dialog (light/dark/system)
- Completion sound toggle
- Navigation section (Main Tabs customization entry point)

### Projects
- Delete project: confirmation dialog, tasks kept (projectId set NULL)
- `TaskRepository.clearProjectAssociation(projectId)` + matching DAO query

### Calendar (Fixed)
- Was completely broken (route not registered)
- Now opens from More → Calendar
- Month view with day dots for task-having days
- Uses `taskRepository.observeAll()` + client-side filter for month prefix (avoids adding DAO method)
- `CalendarViewModel` uses `@OptIn(ExperimentalCoroutinesApi::class)` with explicit type annotations

### TaskRow
- Compact height, priority dot (small colored circle, not background fill)
- Recurrence shown as single-line horizontal chip ("🔁 يومي") not vertical block
- `onSoundPlay: () -> Unit` hook — fires only on PENDING→COMPLETED

---

## Important Files

### New files added
- `app/src/main/java/com/plannermvp/app/ui/util/CompletionSoundPlayer.kt`
- `app/src/main/java/com/plannermvp/app/ui/screens/CalendarScreen.kt`
- `app/src/main/java/com/plannermvp/app/ui/viewmodel/CalendarViewModel.kt`
- `app/src/main/java/com/plannermvp/app/domain/importing/ImportDateSanityChecker.kt`
- `app/src/main/res/raw/task_complete.wav`

### Modified files
- `ui/theme/Theme.kt` — light default, themeMode param
- `ui/theme/Color.kt` — full palette with Matrix tints
- `ui/theme/Type.kt` — compact scale
- `ui/screens/MatrixScreen.kt` — readability overhaul
- `ui/screens/TodayScreen.kt` — compact header, collapsible completed
- `ui/screens/TasksScreen.kt` — filter chips, DatePicker, TimePicker
- `ui/screens/ImportScreen.kt` — paste mode, anomaly banner, format help
- `ui/screens/SettingsScreen.kt` — sections, theme picker, sound toggle
- `ui/screens/TaskRow.kt` — compact, horizontal recurrence, sound hook
- `ui/screens/ProjectsScreen.kt` — delete dialog
- `ui/viewmodel/TodayViewModel.kt` — completedExpanded, soundEnabled
- `ui/viewmodel/TasksViewModel.kt` — TaskFilter, soundEnabled
- `ui/viewmodel/SettingsViewModel.kt` — setCompletionSoundEnabled, setThemeMode
- `ui/viewmodel/ProjectsViewModel.kt` — confirmDelete with task disassociation
- `ui/viewmodel/CalendarViewModel.kt` — @OptIn, explicit types
- `ui/viewmodel/ImportViewModel.kt` — anomaly detection, plausibility defaults
- `data/local/AppDatabase.kt` — v6, MIGRATION_5_6, no fallbackToDestructiveMigration
- `data/local/SettingsEntity.kt` — completionSoundEnabled field
- `data/local/TaskDao.kt` — clearProjectAssociation
- `data/repository/TaskRepository.kt` — clearProjectAssociation
- `data/repository/SettingsRepository.kt` — setCompletionSoundEnabled, setThemeMode
- `domain/importing/ImportValidator.kt` — datePlausibility in object, not in issues
- `navigation/AppNavHost.kt` — saveState=false, Calendar route
- `MainActivity.kt` — themeMode from settings, navigateToTab
- `.github/workflows/android-ci.yml` — validate-wrappers: false, gradle-version: 8.9

---

## Database Migrations

| Version | Change |
|---------|--------|
| v5 → v6 | `ALTER TABLE settings ADD COLUMN completionSoundEnabled INTEGER NOT NULL DEFAULT 1` |

All previous versions (v1–v5) used `fallbackToDestructiveMigration` during MVP development.
Release 1.0 removes this. The v6 migration is additive-only — no data loss on upgrade.

---

## Tests

- Unit tests: `app/src/test/` — 23 files including release10/ package (DateSanityTest, TodayDateTest, NavigationContractTest, ImportValidatorTest)
- Instrumented tests: `app/src/androidTest/`
- CI: `.github/workflows/android-ci.yml` — unit-tests job + instrumented-tests job
- `validate-wrappers: false` in both jobs — resolves Gradle Wrapper JAR validation failure
- `gradle-version: 8.9` — Gradle downloaded by CI, no JAR in repo

---

## Build

```bash
./gradlew testDebugUnitTest     # JVM unit tests
./gradlew assembleDebug         # Debug APK
./gradlew connectedDebugAndroidTest  # UI tests (emulator)
```

CI: GitHub Actions, ubuntu-latest, JDK 17 Temurin, API 34 emulator

---

## Known Limitations (Verified)

- Main tabs drag-reorder not implemented (Settings entry is a placeholder)
- Habit edit UI not available (archive + recreate)
- Widget has one layout size
- `task_complete.wav` is a generated 880Hz sine tone — replace with a properly-recorded chime before any store submission
- Backup/Restore serialization not independently verified in automated tests
