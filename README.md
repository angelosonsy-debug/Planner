# Planner MVP — Android (Phase 1 → Phase 12, feature-complete MVP)

Native Android productivity planner. Kotlin + Jetpack Compose + Room +
Navigation-Compose, offline-first, no backend. Built against the master
product spec: plan externally → import as TXT/CSV → preview → confirm →
Projects/Tasks/Habits → Today → Eisenhower Matrix → daily/weekly review.

## Feature list

- **Today**: Top 3 priority tasks, today's full task list, daily
  completion progress.
- **Tasks**: create/edit/complete/postpone/delete; title, date, start
  time, duration, priority, recurrence (daily/weekly/monthly — auto-
  creates the next occurrence on completion); delete asks for
  confirmation first.
- **Projects**: create with an optional deadline; live progress bar
  (completed/total tasks).
- **Import**: TXT or CSV plan files → parsed, validated, previewed
  (✅/⚠️/❌ per row, checkboxes to include/exclude) → confirmed → written,
  skipping anything already in the database.
- **Eisenhower Matrix**: automatic Q1–Q4 classification from
  importance + due date/urgency; move a task between quadrants from a
  menu, which writes real data, not just a visual change.
- **Habits**: binary or quantity targets; daily, N×/week, or specific-
  weekdays frequency; check off from the list; per-habit detail screen
  with current/best streak, weekly/monthly/yearly stats, and a
  Week/Month/Year heatmap; optional daily reminder notification.
- **Notifications**: per-task start-time reminders (with a "Mark
  complete" action right on the notification), a once-a-day overdue-
  tasks digest, an opt-in daily-review nudge — all with their own
  Settings toggles.
- **Home-screen widget**: today's Top 3 tasks with live completion
  state; tap a task to complete it without opening the app.
- **Backup & Restore**: export everything to a JSON file, restore from
  one — always with a preview and confirmation before anything is
  replaced. Fully offline, no cloud sync.
- **Settings**: notification toggles, backup/restore, all backed by a
  real `SettingsEntity` row.
- Arabic (default) + English, RTL-correct, light/dark theme.

## What's implemented, phase by phase

**Phase 1 — project setup**
- Android app module wired with Compose (Material 3), Navigation-Compose,
  and Room (KSP annotation processing).
- Bottom navigation with the 5 top-level tabs: **Today, Tasks, Projects,
  Habits, More**.
- Arabic (default) + English strings, RTL support enabled.

**Phase 2 — Tasks + Projects**
- `TaskEntity` / `ProjectEntity` Room tables (Section 8 / 9 of the spec),
  with `TaskRepository` / `ProjectRepository` on top.
- **Tasks tab**: create a task with just Title (+ optional Date, Priority);
  check it off; postpone it to tomorrow; delete it.
- **Projects tab**: create a project by name; see it listed with a live
  progress bar (`completed / total tasks`), computed from real task data.
- `TasksViewModel` / `ProjectsViewModel` expose `StateFlow`s backed by Room,
  so the UI updates automatically as data changes.
- `calculateCompletionProgress()` is a pure function, unit-tested on its
  own without any database (shared by Projects *and* Today's daily
  progress — same "completed/total" concept, Section 38 vs Section 10).

**Phase 3 — Today**
- **Today tab**: shows tasks dated today only, in three sections —
  **Top 3** (up to 3 highest-priority pending tasks), **Today's Tasks**
  (everything dated today, completed ones sink to the bottom), and
  **Daily Progress** (`completed / total` + `%`), per Section 10.
- `buildTodaySummary()` is a pure function (sorting + top-3 selection +
  progress) — unit-tested with no ViewModel/database involved.
- `TaskRow` was pulled out into its own file so Tasks and Today share the
  exact same row UI instead of two copies drifting apart.
- Fixed a latent bug from Phase 2: `TaskDao.observeByDate` was ordering by
  `priority ASC` in SQL, which — since priority is stored as its enum name
  — sorted alphabetically (HIGH, LOW, MEDIUM) instead of by actual
  importance. Priority ordering now happens in Kotlin (`buildTodaySummary`),
  where it's also testable.

Habits and More are still placeholder screens — TXT/CSV import lands in
Phase 4, the Matrix in Phase 5, Habits in Phase 6+, per the spec's phase
order.

**Phase 4 — TXT/CSV import + preview**
- Full pipeline from Section 43: File → Reader → Parser → Validation →
  Normalized Model → Preview → Confirmation → Repository → Room.
- `TxtPlanParser` / `CsvPlanParser` turn a file's text into the
  `ImportedItem` normalized model from Section 6 — tolerant of blank
  lines, missing optional fields, `DATE: daily` (→ recurring, no fixed
  date), and unknown fields (folded into notes instead of dropped).
- `ImportValidator` resolves each item into typed values and flags
  problems without ever blocking the import: an invalid date becomes "no
  date" with a warning; an unrecognized priority defaults to Medium with a
  warning; a genuinely blank task title is the one real error. Duplicates
  *within the same file* are flagged too.
- **Import screen** (reached from More → Import Plan): pick a `.txt`/`.csv`
  file via the system picker, see a Preview grouped by project with a
  ✅/⚠️/❌ per item and a checkbox to include/exclude it, then Import.
  Items with errors are unchecked and locked by default; everything else
  is preselected.
- `ImportRepository` does the actual writing: creates a project only if
  it doesn't already exist (by name), and skips a task if an identical one
  (same title/date/project) is already in the database — so importing the
  same file twice doesn't duplicate anything.
- Extra fields the current model doesn't have a column for (duration,
  freeform notes) are folded into the task's `notes` field rather than
  discarded.

Habits and the Matrix are still placeholder/unbuilt — the Matrix is
Phase 5, Habits Phase 6+.

**Phase 5 — Eisenhower Matrix**
- `classifyQuadrant()` is a pure function (Section 45): a task's quadrant
  comes from its `importance` flag and its *effective* urgency — either
  the explicit `urgency` flag, or the fact that it's due today/overdue
  (Section 11, "Dynamic Urgency"). Fully unit-tested.
- **Matrix screen** (More → Eisenhower Matrix): a 2×2 grid, each quadrant
  scrollable on its own. Tapping a task opens a menu to move it to another
  quadrant, which writes real `importance`/`urgency` data via
  `TaskRepository.setQuadrant()` (Section 12: a real data change, not a
  visual-only one).
- Known simplification: the spec describes drag-and-drop between
  quadrants; this ships tap + menu instead, since it gets the same
  outcome (task → chosen quadrant → real data change) without the added
  gesture-handling complexity. Swapping in real drag-and-drop later only
  touches the UI layer — `setQuadrant()` already does the actual work.

**Phase 6 — Habits + check-ins**
- `HabitEntity` / `HabitCheckInEntity` (Section 14/16), with a unique
  `(habitId, date)` index so a check-in is naturally an upsert — logging
  twice in one day updates the row instead of duplicating it.
- **Habits tab**: create a habit (name required; frequency — daily or N×
  per week — and target — simple done/not-done or a daily quantity with a
  unit — are optional, defaulting small per Section 15). Binary habits
  check off with a checkbox; quantity habits use +/− steppers. Tapping a
  habit opens its detail screen.
- **Habit detail screen**: current streak, best streak, this week, this
  month, 30-day completion rate, and a 35-day heatmap (not-done / partial
  / done, Section 18).
- `calculateHabitStats()` is a pure function — streaks, completion rate,
  and the heatmap's day-by-day status are all *derived* from check-in
  rows every time, never stored as their own fact (Section 44: "don't
  store the streak as a standalone truth"), so there's nothing that can
  drift out of sync. Extensively unit-tested (13 cases covering streak
  breaks/continuations, quantity vs. binary targets, week/month windows).
- Known gaps: there's no habit ↔ project link (Section 39) or home-screen
  widget (Section 22) yet — both are explicitly later-phase/non-blocking
  in the spec itself. Habits can't be edited after creation (no edit-habit
  UI), only archived.

**Phase 6, completed — habit reminders + specific weekdays**
- `HabitFrequencyType.SPECIFIC_WEEKDAYS` is now a real option (multi-select
  weekday chips, stored as `"MO,WE,FR"`-style codes), separate from the
  generic `CUSTOM` placeholder.
- Reminder time is now wired to an actual daily Android notification via
  WorkManager, not just stored data: `HabitReminderScheduler` schedules a
  `PeriodicWorkRequest` timed to first fire at the chosen hour:minute
  (`minutesUntilNextOccurrence()` is a pure, unit-tested delay
  calculation), and `HabitReminderWorker` looks the habit up fresh each
  time it fires — if it was archived or deleted since being scheduled, the
  worker cancels its own periodic work instead of notifying about nothing.
  A single notification channel is created on app start
  (`NotificationHelper`). `POST_NOTIFICATIONS` (Android 13+) is requested
  from the Add Habit dialog the moment the reminder toggle is turned on.
- Known simplification: reminder time can only be set when a habit is
  first created (no edit-habit UI exists yet to change it afterward);
  archiving a habit cancels its reminder.

**Phase 7 — Habit Statistics + Heatmap + Streaks**
- Most of this was already built as part of Phase 6 (streaks, completion
  rate, and a 35-day heatmap are all driven by the same pure
  `calculateHabitStats()`). This phase adds what was still missing per
  Section 19: the heatmap now has a **Week / Month / Year** toggle on the
  habit detail screen (7 / 35 / 371 days — 371 keeps the 7-column grid
  aligned to full weeks for the year view) instead of always showing 35
  days.

**Phase 8 — Recurring + Scheduling**
- **Recurring tasks**: the Add/Edit Task dialog has a Repeat option (None
  / Daily / Weekly / Monthly). Completing a recurring task automatically
  creates its next occurrence — same title/project/priority/schedule,
  date advanced by the interval — via `nextOccurrenceDate()`, a pure,
  unit-tested function. Un-completing a task never removes an
  already-created follow-up; that's a deliberate simplification (undo
  affects only that one instance, not the whole chain).
- **Time + Duration**: `startTime` and `durationMinutes` (already on
  `TaskEntity` since Phase 2 but never exposed in the UI) are now real
  optional fields in the Add/Edit Task dialog, and show up in the task
  row's subtitle alongside the date.
- **Edit / general reschedule**: tapping a task's title (Tasks tab only)
  opens an edit dialog — title, date, time, duration, priority, and
  repeat all in one place. This is also how you reschedule a task to *any*
  date now, not just "tomorrow" — the existing Postpone-to-tomorrow
  shortcut is still there as a quick action too.
- **Deadline**: `ProjectEntity.deadline` (already in the schema since
  Phase 2 but never surfaced) is now settable when creating a project and
  shown on its card.

**Phase 9 — Notifications**
- `SettingsEntity` (built in Phase 1, never used until now) grew three
  real preferences — task reminders, overdue digest, daily review reminder
  — behind a new `SettingsRepository`, surfaced in a real **Settings
  screen** (More → Settings).
- **Task reminders**: a task with both a date *and* a start time gets a
  one-time WorkManager job at that exact moment (`taskReminderInstant()`
  is the pure date+time parsing, unit-tested). Creating, editing,
  completing, deleting, or postponing a task all keep its reminder in
  sync — reschedule on change, cancel on complete/delete — wired from
  `TasksViewModel`/`TodayViewModel` right alongside the repository calls
  that already existed. The notification itself has a **"Mark complete"
  action** that writes straight to the database via a `BroadcastReceiver`
  (`TaskActionReceiver`), no need to open the app.
- **Overdue digest**: one consolidated notification a day (never one per
  task — Section 29 is explicit about not being annoying), counting
  tasks whose date has passed and are still pending/in-progress
  (`overdueTasks()`, pure, unit-tested).
- **Daily review reminder**: opt-in, quiet nudge at a time you set in
  Settings. Not tied to an actual "Daily Review" screen yet (Section 26
  isn't built) — this is the reminder scaffolding for when it is.
- Three notification channels: task reminders and habit reminders at
  DEFAULT importance (time-sensitive, you asked for a specific moment);
  the overdue digest and daily review share a LOW-importance channel
  (silent, no heads-up — Section 29's "quiet, not annoying").
  `POST_NOTIFICATIONS` (Android 13+) is requested the moment you touch a
  toggle that needs it.
- Settings toggles apply prospectively — turning task reminders off
  doesn't retroactively cancel every already-scheduled one in a single
  pass; each gets cleaned up the next time that specific task is
  edited/completed/deleted. Documented rather than silently left as a
  surprise.

**Phase 10 — Today tasks widget**
- A home-screen widget built with Jetpack Glance (`TodayWidget`), showing
  the same **Top 3** the Today tab shows — reusing `buildTodaySummary()`
  from Phase 3 instead of writing separate widget-only logic — plus a
  `completed / total` progress line.
- **Tap a task to mark it complete** directly from the widget
  (`CompleteTaskAction`, a Glance `ActionCallback` that calls the exact
  same `TaskRepository.toggleComplete()` the in-app checkbox uses).
  Tapping the "Today" header opens the app.
- **Auto-refresh**: `TodayWidgetUpdater.refresh()` is called from
  `TasksViewModel`/`TodayViewModel` after every mutation, so the widget
  updates immediately rather than waiting for its 30-minute periodic
  refresh (the OS-enforced minimum).
- **RTL**: Glance's `Row`/`Column` follow system layout direction the same
  way Compose does, so this needed no special handling.
- **Light/dark**: uses Glance's default (host-provided) text colors rather
  than `GlanceTheme`/`glance-material3` — that dependency's exact API
  couldn't be verified without a real Android build environment, and a
  real CI run confirmed it as written didn't compile
  (`GlanceTheme` unresolved). Removed rather than guessed at twice; see
  "Fixed after a real CI run" below.
- Known simplification: shows the Top 3 only, not every task today — a
  home-screen widget has limited space, and Top 3 is already what
  Section 10 considers "what matters most right now". The widget's Glance
  `ActionCallback`/`provideGlance` plumbing isn't independently
  instrumented-tested (Glance doesn't currently offer a practical way to
  drive a real widget host in a test); the database logic underneath it
  (`toggleComplete`, `buildTodaySummary`) is thoroughly unit-tested on its
  own, which is where the actual risk of a bug would live.

**Phase 11 — Backup & Restore**
- A **`BackupData`** domain type (not a Room entity — deliberately decoupled
  from the live schema) captures a full snapshot: settings, projects,
  tasks, habits, habit check-ins. `BACKUP_FORMAT_VERSION` (currently `1`)
  is the thing that changes when *this shape* changes, independent of the
  app's internal database version (currently v5) — Section 32's "clear
  versioned format so future database changes can be migrated safely".
- **`BackupSerializer`** converts it to/from JSON using plain `org.json`
  (already part of the Android SDK — no new dependency), with every field
  mapped explicitly per entity rather than via reflection, so a future
  schema change can't silently break an old backup file without it being
  visible in one place. A malformed file throws `BackupParseException`
  instead of crashing anything.
- **`BackupValidator`** checks a successfully-parsed file for internal
  consistency (duplicate IDs, a habit check-in whose habit isn't in the
  file, a task whose project isn't). Only an unrecognized format version
  blocks the restore outright; everything else is a warning that
  `BackupRepository.restoreData()` sanitizes automatically (drops an
  orphaned check-in, clears a dangling project reference) rather than
  failing the whole restore over a handful of bad rows.
- **Restore is transactional** (`AppDatabase.withTransaction`): either the
  whole thing lands, or none of it does. **Restore always stops at a
  confirmation screen first** — shows exactly what's about to happen
  (`X projects · Y tasks · Z habits · W check-ins`, plus any warnings)
  before anything is touched (Section 32: "clear confirmation before
  replacing", "prevent accidental data loss").
- Reached from Settings → Backup & Restore. Export uses
  `CreateDocument`/Import uses `OpenDocument` (Storage Access Framework —
  same pattern as the TXT/CSV importer from Phase 4). Fully offline, no
  cloud sync of any kind.
- **Reliability fix caught during this pass**: restoring didn't
  re-schedule WorkManager reminders for the restored data — a restored
  task with a future start time, or a restored habit with a reminder,
  would have silently never notified. `BackupViewModel.confirmRestore()`
  now re-schedules both after every restore. Stale reminders for data
  that *isn't* in the backup don't need explicit cancelling — they're
  already self-cleaning (see `TaskReminderWorker`/`HabitReminderWorker`,
  Phase 9).

**Phase 12 — Polish, performance, accessibility, reliability**

A review pass, not a new-feature pass — everything below is a fix to
something that already existed, not something added:

- **Reliability**: task deletion had *no* confirmation anywhere — one tap
  on the delete icon, gone, no undo. `TaskRow` now confirms first (kept
  local to that one shared composable, so both the Tasks tab and Today
  tab get it automatically). The backup-restore reminder gap above was
  also found during this pass.
- **Accessibility**: the +/− stepper buttons (habit frequency/target,
  quantity check-ins, daily-review reminder time) all had
  `contentDescription = null` — a screen reader would've announced them
  as unlabeled "button". Every one now has a real
  label ("Increase"/"Decrease"). Everywhere else already had proper
  content descriptions, so this was the only gap found.
- **Consistency**: `ImportScreen`'s three states (idle/error/done) used
  24dp screen padding while every other screen in the app uses 16dp —
  normalized.
- **RTL**: reviewed for hardcoded `left`/`right`/`paddingLeft` usage —
  none found; the app already consistently uses direction-aware
  modifiers (`padding(start=...)`, `Row`/`Column` default alignment),
  so RTL correctness was already in place, not something this pass had
  to add.
- **Performance / background work**: reviewed Room query indices,
  streak/recurrence calculation complexity, and every scheduled
  WorkManager job's frequency. Nothing found worth changing — task
  reminders are one-time (self-cleaning), habit/digest/review reminders
  are daily-periodic (the minimum useful frequency for each), the widget
  refreshes on its OS-enforced 30-minute floor plus immediately on
  mutation, and query volumes at this app's realistic scale (a personal
  task/habit list, not a multi-tenant dataset) don't justify additional
  indices.
- Deliberately **not** changed: Project deletion and Habit
  archive/delete have repository methods (`ProjectRepository.deleteProject`,
  `HabitRepository.archiveHabit`) but no UI button wired to either —
  true since Phase 2/6. Adding one now would be a new feature, not
  polish, so it's left as-is per this phase's explicit scope.

## Verification

This sandbox has no Android SDK, Gradle, or access to Google's Maven
repository (`dl.google.com`/`maven.google.com` aren't reachable from
here), so a real `./gradlew assembleDebug` could not be run in this
environment — that's what `.github/workflows/android-ci.yml` is for; it
runs on GitHub's own infrastructure, which has no such restriction.

What *could* be done here, and was: the entire `domain/` package (every
pure business-logic file — parsers, validators, the Matrix classifier,
habit statistics, recurrence math, backup serialization; zero Android
framework dependencies) was compiled with a real Kotlin 2.0.20 compiler
against the actual `TaskEntity`/`ProjectEntity`/`HabitEntity`/
`HabitCheckInEntity`/`SettingsEntity` classes from this repo (Room's
annotations were stubbed just enough to satisfy the compiler — the
entity *field definitions* compiled are the real ones), plus a small
real (not stubbed) `org.json` implementation. It compiled with zero
errors and zero warnings, and a harness exercising 16 cases mirroring
the unit test suite (backup round-trip, habit streaks, Matrix
classification, recurrence, TXT/CSV parsing, import validation, overdue
detection) ran on the JVM and passed 16/16. This is real compiler and
runtime verification of the app's highest-risk logic, not a guess.

The rest of the app (Compose UI, Room DAOs, ViewModels, WorkManager,
Glance) depends on the Android SDK and couldn't be compiled the same way
here — that code was reviewed carefully by hand (consistent patterns
throughout, cross-checked signatures after every repository change) and
will get its real build/test verification from CI on first push.

## Fixed after a real CI run

The first real `./gradlew` build (on GitHub Actions — this sandbox still
can't run one) caught 4 genuine bugs the hand-review above missed, none
of them in the domain layer that got real compiler verification:

1. **`MainActivity.kt`**: a private composable function was named
   `PlannerApp()` — same name, same package, as the `PlannerApp`
   Application class. `setContent { PlannerMvpTheme { PlannerApp() } }`
   became ambiguous between "construct the Application" and "call the
   composable". Present since Phase 1; renamed the composable to
   `AppRoot()`.
2. **`HabitDetailScreen.kt`**: imported `viewModelFactory` from
   `androidx.lifecycle.viewmodel.compose` instead of
   `androidx.lifecycle.viewmodel` (no `.compose`) — the only file with
   this typo; every other ViewModel's companion `Factory` had the
   correct import. Fixed to match.
3. **`ImportScreen.kt`**: imported `androidx.compose.foundation.lazy.item`,
   which doesn't exist as an importable symbol — `item { }` inside a
   `LazyColumn` is a member function of `LazyListScope`, resolved via the
   implicit receiver, exactly like `TodayScreen.kt` already used it
   *without* importing it. Removed the bogus import.
4. **`TodayWidget.kt`**: `GlanceTheme`/`glance-material3` — flagged as a
   real risk in this same README before the CI run ("its exact API
   couldn't be verified without a real Android build environment") —
   turned out to actually be wrong. The CI log also showed
   `actionStartActivity<MainActivity>()` needed an explicit `Intent`
   parameter in this Glance version. Removed the `glance-material3`
   dependency and `GlanceTheme` entirely (plain default colors instead);
   fixed `actionStartActivity` to build a real `Intent` in `provideGlance`
   and pass the resulting `Action` down — using the exact parameter name
   (`intent`) the compiler error itself reported, not a guess.

Everything else in the build log — Room/KSP, Compose compilation up to
these points, resource merging, manifest processing — succeeded. These
4 were genuinely the only compile errors, out of ~28,000 lines across
105 Kotlin files, and 3 of the 4 were import/naming mistakes rather than
logic bugs.

## Round 2: fixed after the first CI run's test results

`compileDebugKotlin` passed on the next run — the 4 fixes above held.
But `testDebugUnitTest` and `compileDebugAndroidTestKotlin` then surfaced
two more real, distinct issues:

1. **All 7 Robolectric-based unit tests failed** with
   `IllegalArgumentException` in Robolectric's `DefaultSdkPicker`. Root
   cause: `compileSdk = 35` (Android 15) outpaces what Robolectric 4.13
   ships prebuilt "android-all" jars for — Robolectric tries to
   auto-pick an SDK matching the module's compileSdk and fails to find
   one it supports. Fixed with `app/src/test/resources/robolectric.properties`
   containing `sdk=34`, pinning every Robolectric test in the module to
   API 34 (which 4.13 does support) regardless of the module's actual
   compileSdk.
2. **`BackupSerializerTest`'s two round-trip tests threw
   `NullPointerException`.** Root cause, once traced: unlike the
   repository tests, `BackupSerializerTest` had no
   `@RunWith(RobolectricTestRunner::class)` — it looked like it didn't
   need one, since it touches no Room/Context. But `org.json` is part of
   the *Android SDK*, not the JVM: a plain unit test only sees the stub
   `android.jar` (method bodies are literally `throw
   RuntimeException("Stub!")` on real devices), and this module sets
   `testOptions.unitTests.isReturnDefaultValues = true` — which makes
   every stub call silently return null/zero/false instead of throwing.
   So every `JSONObject`/`JSONArray` call was quietly doing nothing,
   and the code NPE'd trying to use the bogus "results". Added the same
   `@RunWith(RobolectricTestRunner::class)` the other tests already use,
   which loads the *real* android-all jar and gives `org.json` its
   actual behavior. Checked every other test file for the same gap —
   this was the only one touching `org.json` without it.
3. **All 10 instrumented UI test files failed to compile**, each on a
   subset of `assertExists`, `assertDoesNotExist`, `onNode`, or
   `onAllNodes` — every occurrence of "Unresolved reference" pointed
   directly at an `import androidx.compose.ui.test.X` line, never at the
   call site. Fetched the real Compose UI testing source
   (`SemanticsNodeInteraction.kt`, `SemanticsNodeInteractionsProvider.kt`
   from `androidx/androidx` on GitHub) to check directly rather than
   guess again: all four are **member functions** (of
   `SemanticsNodeInteraction` and `SemanticsNodeInteractionsProvider`
   respectively), not top-level functions — so `composeRule.onNode(...)`
   and `result.assertExists()` are correct calls that need *no* import
   at all, exactly like `TaskRow`'s `item { }` in Phase 12's
   `ImportScreen.kt` fix. Removed the 16 bogus imports across the 10
   files; every actual usage was already correct and needed no other
   change. (`onNodeWithText`, `performClick`, `assertIsOn`/`assertIsOff`,
   etc. — genuinely top-level extension functions — were verified the
   same way and left untouched.)

With real source access this time instead of memory, I'm considerably
more confident in these three than in guesses would have been — but
this environment still can't run a real Gradle build, so the next CI
run is still the actual confirmation.

## Round 3: fixed after the second CI run

Compilation was clean this time (both `compileDebugKotlin` and
`compileDebugAndroidTestKotlin` passed). What surfaced next were runtime
test failures — 36 of 111 unit tests, and 3 of 16 instrumented tests.

**All 36 unit test failures had one root cause.** Every failure was
either `IllegalStateException` at `WorkManagerImpl.java:170` or
`kotlinx.coroutines.test.UncaughtExceptionsBeforeTest` — across
repository tests that don't touch WorkManager or notifications at all
(`TaskRepositoryTest`, `ProjectRepositoryTest`, `SettingsRepositoryTest`,
etc.). The connection: every one of them calls
`ApplicationProvider.getApplicationContext<...>()`, and since
`AndroidManifest.xml` declares `android:name=".PlannerApp"`, Robolectric
constructs the *real* `PlannerApp` for that call — running its
`onCreate()`, which launches an unstructured background coroutine that
calls `WorkManager.getInstance()`. WorkManager isn't guaranteed to be
initialized yet in that environment, so it throws — and because the
coroutine runs on an unscoped `applicationScope` rather than a
test-bound one, the exception surfaces unpredictably against whichever
test happens to be running (or the next one). Fixed by wrapping that
`onCreate()` block in try/catch: in a real installed app this never
triggers (WorkManager auto-initializes via its own manifest
`ContentProvider` before any `Application.onCreate()` runs), so this
purely guards the test environment gap without masking a genuine
production issue.

**The 3 instrumented failures were all the same category of test
mistake, not app bugs.** Each failed with `assertExists` finding 2 nodes
where it expected exactly 1: "Today" (`BottomNavigationTest`), "Habits"
(`BottomNavigationTest`), and a task title that legitimately renders
twice — "Speaking practice" (`TodayScreenTest`). Root cause: four of the
five bottom-nav labels (Today/Projects/Habits/More — every one except
Tasks) are, by design, the *exact same string* as their screen's own
title (`nav_today` == `today_title`, both "Today", etc.) — so once that
screen is showing, its name legitimately appears twice on screen at
once (nav bar + header), and the "Speaking practice" case is the Today
screen correctly showing a single task in *both* its Top 3 section and
its full task list. The app's behavior was correct in all 3 cases; the
tests were just asserting "exactly one" where "at least one" was what
actually mattered. Fixed by switching those specific assertions from
`onNodeWithText(...).assertExists()` to
`onAllNodesWithText(...)[0].assertExists()`. Checked every other
`onNodeWithText` call across all test files for the same pattern —
none of the others check text that's ever duplicated (they all check
freshly-created, unique task/project/habit names, or numeric progress
values) — so no other test needed this same fix. Note for later: this
means `nav_projects`/`projects_title` and `nav_more`/`more_title` carry
the identical latent ambiguity, just not yet exercised by any test that
checks that exact text — worth remembering if a future test ever does.

## Opening the project / installation

1. Open this folder in Android Studio (Ladybird/Koala or newer).
2. Let Gradle sync. **The Gradle wrapper jar (`gradle/wrapper/gradle-wrapper.jar`)
   is intentionally not included** — it's a binary file this environment
   couldn't fetch. Android Studio will offer to generate it automatically
   on first sync ("Gradle wrapper is missing... " prompt → accept), or you
   can run `gradle wrapper` once from a terminal if you have any Gradle
   installed locally. After that `./gradlew` works normally.
3. Run the `app` configuration on an emulator or device (minSdk 26) —
   this builds and installs a debug APK directly, no separate build step
   needed for day-to-day development.

### Building an installable APK

- **From Android Studio**: Build → Build App Bundle(s) / APK(s) →
  Build APK(s). The output path is printed in the "Build" tool window
  when it finishes (typically `app/build/outputs/apk/debug/app-debug.apk`).
- **From the command line**: `./gradlew assembleDebug`, output at
  `app/build/outputs/apk/debug/app-debug.apk`.
- **From CI**: every push/PR to `main` runs `.github/workflows/android-ci.yml`,
  which builds the debug APK and uploads it as a downloadable workflow
  artifact (see "CI" below) — no local setup required at all if you just
  want the APK.
- To install on a device: `adb install app/build/outputs/apk/debug/app-debug.apk`,
  or drag the APK onto a running emulator.

## Running tests locally

```bash
./gradlew testDebugUnitTest        # fast JVM tests (Room via Robolectric)
./gradlew connectedDebugAndroidTest # Compose UI tests, needs an emulator/device
```

## CI

`.github/workflows/android-ci.yml` runs on every push/PR to `main`:
- **unit-tests**: JVM unit tests + `assembleDebug`, uploads the debug APK
  and test report as artifacts.
- **instrumented-tests**: boots a Pixel 6 / API 34 emulator on the runner
  and runs the Compose UI tests (bottom nav, create/complete a task,
  create a project, Today's Top 3 + daily progress, navigating to the
  Import screen, moving a task between Matrix quadrants, creating and
  checking in a habit, editing/rescheduling a task, toggling notification
  settings, navigating to Backup & Restore and round-tripping real data
  through export/restore).

Both jobs must pass before moving to the next phase, per the project's
build rule.

## Known gaps / notes

- Version numbers (AGP 8.6.0, Kotlin 2.0.20, Compose BOM 2024.09.02, etc.)
  were current as of this writing but not verified against a live Gradle
  sync — this sandbox has no Android SDK/Gradle network access. If Android
  Studio's Upgrade Assistant suggests newer stable versions on first sync,
  it's safe to accept them.
- Task date entry is a free-text field (`YYYY-MM-DD`), not a date picker
  yet — no format validation on the manual-entry form (the importer's own
  date parsing is stricter and does validate).
- `AppDatabase` uses `fallbackToDestructiveMigration()` since there's no
  real install to protect yet. Switch to real `Migration` objects before
  any release build.
- CSV parsing is a plain comma split — a comma inside a quoted cell will
  mis-split. Import format detection (TXT vs CSV) is by file extension,
  falling back to a simple heuristic if the extension is missing/unknown.
- The import preview's file-picker round-trip isn't covered by an
  instrumented test (stubbing the system picker needs Espresso-Intents,
  which felt like more setup than value right now); the parsing,
  validation, and database-writing logic underneath it is thoroughly unit
  tested instead (`TxtPlanParserTest`, `CsvPlanParserTest`,
  `ImportValidatorTest`, `ImportRepositoryTest`).
- No launcher icon artwork — a placeholder adaptive icon is included so the
  manifest resolves; swap it for real branding whenever you like.
- Habit reminder notifications are best-effort: if `POST_NOTIFICATIONS`
  is denied (Android 13+), the habit still saves fine and the reminder
  simply won't show — there's no in-app nudge yet to re-enable it from
  system settings.
- Recurring tasks only support Daily/Weekly/Monthly (Section 24's example
  set); nothing like "every 2 weeks" or a custom interval yet.
- Task reminders share the same "best-effort" notification-permission
  behavior as habit reminders (see above).
- `AppDatabase` bumped to v5 for the three new Settings columns — same
  destructive-migration caveat as every prior bump: fine pre-release,
  swap for real `Migration` objects before any real install needs
  protecting.
- Actual notification *delivery* (does a real notification appear in the
  shade at the right time) isn't verified by an automated test — that
  would need driving the emulator's notification shade via UiAutomator,
  which felt like disproportionate machinery for this pass. What's tested
  instead: the pure timing math (`taskReminderInstant`,
  `minutesUntilNextOccurrence`, `overdueTasks`), the repository-level
  scheduling side effects, and the Settings toggles' persistence.
- The widget only has one size/layout (no small vs. large variant); Glance
  doesn't get its own icon in the widget picker beyond the app's launcher
  icon.
- Backup files aren't encrypted or password-protected — they're plain
  JSON on whatever storage location the user picks via the system file
  picker. No cloud sync of any kind, per Phase 11's explicit scope.
- Project deletion and Habit archive/delete have working repository
  methods but no button wired to them in the UI (true since Phase 2/6,
  deliberately left alone in Phase 12 — see that section above).
- `AppDatabase` is still on `fallbackToDestructiveMigration()`. This is
  the single most important thing to change before any real release —
  every schema bump so far has assumed no one has real data to lose yet.

