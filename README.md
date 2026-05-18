# LearnLab — native Android

1:1 port of the React `LLTest` web app to Kotlin + Jetpack Compose.
Same shell, same rail, same 21 experiments listed across 4 chapters,
same colour tokens, same dark/light theme toggle.

## What's running

- **Full shell**: TopBar (logo + count + theme toggle), ExperimentRail (chapter sections + experiment cards), ExperimentStage (source/title/outcome header + progress + prev/next), procedure strip.
- **Theme system**: light and dark palettes match `src/design/tokens.css` value-for-value. Toggle persists across launches via DataStore.
- **All 21 experiments** appear in the rail and load into the stage.
- **Ported activities** (real, working):
  - Plant Sorter, Animal Movement, Habitat Sorter, India Food Map → `SortBuckets` engine (long-press drag, hover highlight, reset, reveal).
  - Iodine Starch Test, Oily Patch Fat Test, Protein Violet Test → `LabReagent` engine (predict → react → reveal, observation log, badge pills).
- **Remaining 11 experiments** (Scientific Method, Leaf Venation, Root System, Seed Dissection, Plant Pattern, Adaptation Lab, Deficiency Matchup, Balanced Thali, Junk vs Nutri, Food Miles, Projectile Motion, Simple Pendulum, Convex Lens, EM Induction): the rail loads them and the stage renders a `ComingSoonExperiment` card that shows the source citation, outcome, and the procedure they'll follow. Same pattern the React app uses for unfinished experiments — the shell remains honest.

## Code map

```
app/src/main/java/com/learnlab/
├── app/MainActivity.kt           ← theme + window + kiosk fullscreen
├── content/
│   ├── Schema.kt                 ← Experiment + Chapter data classes
│   └── Experiments.kt            ← all 21 entries, exact text from React
├── design/
│   ├── Tokens.kt                 ← light + dark palettes (mirrors tokens.css)
│   ├── LLText.kt                 ← typography wrapper
│   └── Primitives.kt             ← Card, PrimaryButton, SecondaryButton, GhostButton, ProgressBar, NumberPip
├── shell/
│   ├── Shell.kt                  ← grid layout (TopBar + rail + stage)
│   ├── TopBar.kt
│   ├── ExperimentRail.kt
│   ├── ExperimentStage.kt        ← header + progress + nav + activity surface
│   ├── InstructionBanner.kt
│   └── Welcome.kt
├── store/
│   ├── AppState.kt               ← currentExperimentId, theme, prev/next, DataStore persistence
│   └── ExperimentControls.kt
├── engines/
│   ├── Registry.kt               ← id → Composable
│   ├── SortBuckets.kt            ← long-press drag/drop engine
│   └── LabReagent.kt             ← predict → react → reveal engine
└── experiments/
    ├── PlantSorter.kt
    ├── AnimalMovement.kt
    ├── HabitatSorter.kt
    ├── IndiaFoodMap.kt
    ├── ReagentTests.kt           ← Iodine + Fat + Protein
    └── ComingSoonExperiment.kt
```

## Run it

1. Open Android Studio (Koala/Ladybug+, AGP 8.5+, Kotlin 2.0+).
2. `File → Open…` → select this `LearnLabAndroid` folder.
3. Wait for the first Gradle sync (downloads Gradle 8.9 + AndroidX + Compose).
4. Plug in the IFP via USB-debugging, or `adb connect <ip>:5555` for wireless. Tablet works the same way.
5. Pick the device, hit **Run** (Shift+F10).

The APK installs as `com.learnlab.android` (debug suffix `.debug`), landscape only, fullscreen kiosk.

## What's different from the prototype

- The earlier `com.learnlab.plantsorter` package is gone. Everything now lives under `com.learnlab.*` with a real layered architecture.
- The app no longer opens straight into Plant Sorter. It opens on the Welcome screen, same as the web — teacher picks an experiment from the rail.
- Theme toggle, persisted preference, dark/light tokens are all live.
- Adding the remaining 11 experiments now means editing one file each + one line in `engines/Registry.kt`. Same workflow as the React `ExperimentRegistry.ts`.
