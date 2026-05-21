# LearnLab — Engineering Rules

Read this before writing code. These rules are shared by all devs and by Claude.
If a rule blocks you, raise it in the team channel — don't silently break it.

---

## 1. What we are building

A teacher-facing Android tablet app for NCERT Science (Grades 6–9 today, Math
and more later). Teachers project experiments in class. Students will be added
in a later phase, so the architecture must make that an additive change, not a
rewrite.

Scale to plan for:
- 4 grades × ~50 chapters/grade × ~6 activities/chapter ≈ **1,200+ activities** for Science alone.
- Multiple subjects later → realistically **2,000+ activities** over time.

You **cannot** hand-code 2,000 Kotlin files. Most activities must be data,
loaded into reusable engines. See §4.

---

## 2. Non-negotiable principles

1. **Offline-first.** The app must launch, run a lesson, and record progress
   with no network. Sync is best-effort, in the background, never blocking UI.
2. **Content is data, not code.** New chapters ship as JSON. New Kotlin only
   when a genuinely new interaction is needed.
3. **Engines, not experiments.** Reuse interaction patterns across activities.
   If two activities differ only in their data, they share one engine.
4. **Teacher-first today, student-ready tomorrow.** Every progress/event
   record has a `userId` + `updatedAt`, even when there's only one local user.
5. **Tablet-first UI.** Landscape, large hit targets, readable from the back
   of a classroom. Phone support is not a goal.
6. **Modules over packages.** Build times and ownership boundaries depend on
   Gradle modules, not folder names.
7. **No premature backend.** Supabase plugs in later via repository swap. Do
   not couple UI or domain code to network types.

---

## 3. Repo structure

### Target module layout

```
LearnLabAndroid/
├── app/                          # Application class, MainActivity, DI wiring
├── core/
│   ├── design/                   # Tokens, LLText, Primitives, Slider
│   ├── engines/                  # Reusable interaction engines (§4)
│   ├── shell/                    # TopBar, Rail, Stage, Welcome, present mode
│   ├── content/                  # Schema, ContentLoader, JSON parser
│   ├── store/                    # AppState, ExperimentControls
│   └── data/                     # Room DB, repositories, sync (later)
├── subjects/
│   ├── science/                  # bespoke Kotlin experiments + science JSON
│   ├── math/                     # later
│   └── ...
└── content-json/                 # Authored JSON; copied into assets/ at build
    ├── grade-6/science/ch02/2.4-grouping-plants.json
    └── ...
```

### Current state (May 2026)

One `:app` module, code under `com.learnlab.*`. Module split is **in
progress** on `claude/repo-structure-decision-6UFbC`. Until split lands,
treat each top-level package (`design`, `engines`, `shell`, `content`,
`store`, `experiments`) as a future module — **do not introduce cross-package
dependencies that wouldn't survive the split.**

### Allowed dependency direction (strict)

```
app  →  subjects/*  →  core/engines, core/shell, core/content, core/data
                       ↓
                    core/design, core/store
```

- `core/design` depends on nothing.
- `core/engines` depends only on `core/design` and `core/content` (schema only).
- `subjects/*` never depend on each other.
- `app` is the only module that knows every subject exists.

Breaking these arrows defeats the modularization. Reviewers reject PRs that do.

---

## 4. Engine system

### The rule

If a new activity can be expressed as a config for an existing engine, **it
must be**. Adding a new bespoke Kotlin file is a last resort, reviewed by the
team.

### Engine catalogue (build in this order)

Generic engines (highest reuse, build first):

| Engine | Purpose | Covers |
|---|---|---|
| `DataTableEngine` | Record observations in rows × cols | Grade 6 "Fill table" — ~80 activities |
| `LabeledDiagramEngine` | Clickable hotspots on 2D/3D diagram | Cells, atoms, circuits, solar system — ~40 activities |
| `LabProtocolEngine` | Multi-step chemical/physical test | Indicators, starch test, rusting — ~30 activities |
| `CircuitBuilderEngine` | Drag components, wire, test | Grade 7 electricity, Grade 8 voltaic cell |
| `ObservationEngine` | Annotated scene with step-through narration | Light, combustion, motion observations |
| `ComparisonEngine` | Side-by-side A vs B | Grouping, sorting, classification comparisons |
| `BucketSortEngine` | Drag items into categories | Plants/animals/materials classification (existing `SortBuckets`) |
| `MeasurementEngine` | Read a scale/ruler/thermometer | Length, temperature, mass, displacement |

Bespoke engine families (one engine, many configs — for richer Grade 8/9 sims):

| Family | Reuses across |
|---|---|
| `MotionSimEngine` | Linear, circular, oscillatory, kinematics |
| `ForceSimEngine` | Friction, Newton's laws, springs, pulleys |
| `OpticsEngine` | Lenses, mirrors, light through materials (current `ConvexLens` generalizes here) |
| `FieldVisualizationEngine` | Magnetic, electric, gravitational fields (current `EMInduction`) |
| `SeparationTechniqueEngine` | Tyndall → centrifugation |
| `CycleAnimationEngine` | Life cycles, water cycle, oxygen cycle, moon phases |

~14 engines + families covers ~80% of all four Science grades. The other
20% is genuine one-offs.

### Engine contract

Every engine exposes a single Composable with this shape:

```kotlin
@Composable
fun <EngineName>(
    config: <EngineName>Config,           // parsed from JSON
    state: ActivityState,                 // hoisted; engine never owns it
    onEvent: (ActivityEvent) -> Unit,     // user actions go up
)
```

- Engine reads `config`, renders UI, emits events.
- `ActivityState` lives in `core/store` and is observed via `StateFlow`.
- Engines **never** read from Room, network, or DataStore directly. They
  receive props and emit events.
- Engines **never** know what activity they are running. The shell wires the
  registry; engines render configs.

---

## 5. Content as data

### JSON schema (frozen — don't change without team review)

```json
{
  "id": "g6-sci-ch02-2.4",
  "schemaVersion": 1,
  "grade": 6,
  "subject": "science",
  "chapter": { "id": "ch02", "number": 2, "title": "..." },
  "topic": "How to group plants?",
  "activityCode": "2.4",
  "engine": "data-table",
  "title": "...",
  "blurb": "...",
  "outcome": "...",
  "source": "NCERT Grade 6 Science, Activity 2.4",
  "steps": ["...", "..."],
  "config": { /* engine-specific, validated against engine's schema */ }
}
```

### Authoring flow

1. Content author edits the master spreadsheet (one row per activity).
2. CI script converts spreadsheet → JSON files in `content-json/`.
3. Gradle task copies `content-json/` into `app/src/main/assets/content/` at build time.
4. `ContentLoader` reads JSON at runtime, hands `config` to the right engine.

### Rules for content

- `id` is **immutable**. Once shipped, never reuse or rename. Progress
  records reference it.
- `schemaVersion` bumps when schema is breaking. Loader supports the current
  version + one previous.
- All activity text (title, blurb, outcome, steps) lives in JSON. **No
  user-facing strings hardcoded in Kotlin** except shell chrome.
- Existing `content/Experiments.kt` is being migrated to JSON. Do not add new
  activities to it.

---

## 6. Progress tracking

Even though students don't exist yet, progress is per-user from day one.
Schema is sized for cloud sync from day one.

### Local schema (Room)

```kotlin
@Entity(tableName = "progress")
data class ProgressEntity(
    @PrimaryKey val id: String,              // "{userId}:{activityId}"
    val userId: String,                      // local UUID today; auth ID later
    val activityId: String,                  // e.g. "g6-sci-ch02-2.4"
    val status: ProgressStatus,              // NOT_STARTED, IN_PROGRESS, COMPLETED
    val startedAt: Long?,                    // epoch millis
    val completedAt: Long?,
    val timeSpentMs: Long = 0,
    val attempts: Int = 0,
    val score: Int? = null,                  // 0–100 if applicable
    val payload: String? = null,             // engine-specific JSON blob (e.g. table answers)
    val updatedAt: Long,                     // for last-write-wins sync
    val syncedAt: Long? = null,              // null = dirty
)
```

### User identity

On first launch:
- Generate a v4 UUID, store in DataStore as `local_user_id`.
- All progress is written against this ID.
- When real auth lands: prompt to link local UUID to a teacher account; do
  not delete or rewrite existing rows — just associate.

### Progress events (telemetry, also future-cloud)

```kotlin
@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val rowId: Long = 0,
    val userId: String,
    val activityId: String,
    val eventType: String,                   // "started", "step_completed", "answer_submitted", ...
    val payload: String? = null,             // JSON blob
    val occurredAt: Long,
    val syncedAt: Long? = null,
)
```

Write events optimistically; uploader drains them in background when online.

### Repository pattern (mandatory)

```kotlin
interface ProgressRepository {
    suspend fun get(activityId: String): Progress?
    suspend fun save(progress: Progress)
    fun observe(activityId: String): Flow<Progress?>
    fun observeAll(): Flow<List<Progress>>
}
```

Today: `LocalProgressRepository(progressDao)`.
Later: `SyncedProgressRepository(local, remote)` — implements the same
interface. UI never changes.

**UI/engines may only depend on the interface.** Never inject the Dao or the
Supabase client into a ViewModel.

---

## 7. Online sync (deferred, but design for it now)

Sync is **not implemented yet**. Do not add Supabase code without team agreement.

When we add it, this is the plan — design current code to fit:

### Connectivity model

- Default state: **assume offline**. App works fully.
- Background `WorkManager` job runs when network is available:
  1. Push: upload all rows with `syncedAt < updatedAt` to Supabase.
  2. Pull: fetch rows updated since last successful pull; merge into Room.
  3. Conflict resolution: **last-write-wins by `updatedAt`** (acceptable
     because one user rarely uses two devices simultaneously).
- Sync failures are silent retries with exponential backoff. Never surface
  network errors to teachers mid-lesson.

### Content sync

Content has two layers:
1. **Bundled JSON in `assets/`** — ships with APK, always available.
2. **Remote overlay** — Supabase `experiments` table. On launch, check
   `content_version` per activity; if newer, fetch and cache in Room.
   Loader prefers Room → bundled JSON.

This gives hot-fix capability without compromising offline use.

### What goes where (mandatory split)

- **Supabase**: text content, progress, events, auth, teacher metadata.
- **Play Asset Delivery** (when needed): 3D models, large textures, audio.
- **APK assets**: small images, icons, bundled baseline JSON.
- **Never** put binary assets in Supabase Storage. Cost and latency are wrong.

---

## 8. Code conventions

### Kotlin

- Kotlin 2.x, Compose, single-activity app.
- Coroutines + StateFlow for reactive state. **No RxJava, no LiveData.**
- ViewModel-per-screen. ViewModels hold StateFlow; UI collects with
  `collectAsStateWithLifecycle`.
- DI: Hilt (when we add it; currently manual wiring in `app/`).
- Serialization: `kotlinx.serialization` for JSON.
- Persistence: Room (progress, events, content cache), DataStore (prefs only).

### File organisation

- One Composable per file when it's a screen or engine.
- Tiny helper Composables can live alongside.
- `*Engine.kt` files implement engines; `*Config.kt` files define their
  config data classes.
- Bespoke experiments in `subjects/<subject>/experiments/<Name>.kt`.

### Naming

- IDs are kebab-case with grade-subject-chapter prefix: `g6-sci-ch02-2.4`.
- Engine names are PascalCase + "Engine" suffix.
- JSON file names mirror the activity code: `2.4-grouping-plants.json`.

### Testing

- Engines: unit-test the config → state reducer logic. UI tests are
  Compose preview-based; full instrumentation only for shell.
- Repositories: test with in-memory Room.
- No tests on bespoke physics simulations unless there's a regression.

### Comments

- Write code so comments aren't needed. Add a comment only when the **why**
  is non-obvious — a workaround, an invariant, an NCERT-specific quirk.
- No file headers, no banner comments, no TODO without an owner + date.

---

## 9. What NOT to do

- **Don't add a new bespoke experiment file** if an existing engine + config
  would do. Ask the team if unsure.
- **Don't hardcode activity text in Kotlin.** It goes in JSON.
- **Don't call Supabase from a ViewModel or Composable.** Repositories only.
- **Don't introduce cross-subject dependencies.** `subjects/math` cannot
  import from `subjects/science`. Share via `core/`.
- **Don't add LiveData, RxJava, Dagger (use Hilt), or Retrofit** — Supabase
  client covers HTTP needs.
- **Don't add Flutter / React Native / KMM** without a written ADR.
- **Don't break offline mode** by adding required network calls on launch.
- **Don't change the JSON schema or progress schema** without bumping
  `schemaVersion` and writing a migration.
- **Don't push to `main` directly.** Feature branches + PR review.
- **Don't add a feature flag system** until we have a second feature flag.
- **Don't add analytics SDKs** (Firebase, Mixpanel, etc.). Telemetry goes
  through our own `events` table → Supabase. Privacy and ownership matter
  more than dashboards.

---

## 10. Build & run

```
./gradlew :app:assembleDebug          # build APK
./gradlew :app:installDebug           # install on connected device
./gradlew test                        # unit tests
./gradlew :core:engines:test          # specific module
```

Target tablet: any Android 8+ device, landscape, projected via HDMI or
mirrored. Test on a real tablet before merging shell/UI changes.

---

## 11. Branching

- `main` is always shippable.
- Feature branches: `<author>/<short-topic>`. Squash-merge PRs.
- Claude works on `claude/<topic>` branches and pushes to them; humans
  review before merge to `main`.

---

## 12. When in doubt

- Read this file. Then read the existing engine that's closest to what you
  want to do. Then ask the team.
- Architectural changes (new modules, schema changes, new top-level
  dependencies) require team agreement, not a solo PR.
- If you're tempted to break a rule "just this once," the rule is wrong or
  the design is wrong. Raise it; don't bypass it.
