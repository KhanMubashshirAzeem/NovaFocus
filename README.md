# NovaFocus

NovaFocus is a high-performance, minimalist Android launcher built entirely with Jetpack Compose and Kotlin Coroutines. It demonstrates modern Android architecture, custom gesture handling, reactive state management, and fluid UI performance.

---

## Tech Stack

- **Language**: Kotlin (100%)
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM / MVI with Clean Architecture & Unidirectional Data Flow (UDF)
- **State Management**: Kotlin Coroutines, `StateFlow`, `MutableStateFlow`
- **Reactive Streams**: Kotlin `Flow` (debounced search pipeline)
- **Graphics & Gestures**: Compose `Canvas`, `PointerInput`, `Animatable`
- **Persistence & Caching**: Android Internal File Storage (Bitmap disk caching) & `SharedPreferences`
- **Build System**: Gradle Kotlin DSL (`build.gradle.kts`) with Compose BOM

---

## Core Features

- **Interactive Alphabet Scrubber**: A vertical A–Z index bar on the right screen edge with dynamic touch deflection, magnified letter bubble, haptic feedback, and a spring return animation on release.
- **Instant Cold Start (Frame 0 Icons)**: Dedicated disk cache (`IconCache`) stores compressed icon bitmaps in internal app storage. On launch, favorite icons load synchronously in under 5ms, avoiding the typical 1-second `PackageManager` query delay.
- **Universal Swipe-Up Search**: Gesture-driven search overlay accessible via an upward swipe from both the home screen and filtered alphabet lists.
- **Debounced App Search**: Real-time app search using Kotlin Flow operators (`debounce`, `distinctUntilChanged`, `collectLatest`), with automatic tracking for the 3 most recent searches.
- **Backstack Lifecycle Management**: Applications launch with intent configurations that maintain backstack affinity to NovaFocus, ensuring system Back button presses return directly to the launcher.
- **AMOLED Dark Theme**: Edge-to-edge dark interface with synchronized dark mode theming for the soft keyboard (IME).

---

## Architecture Overview

The codebase is organized following Clean Architecture principles:

- **Data Layer**:
  - `AppsRepository`: Interfaces with Android's `PackageManager` to discover, filter, and launch installed applications.
  - `IconCache`: Manages internal disk storage caching of application icon bitmaps for instant retrieval.
  - Models: `AppItem` (app metadata and bitmap) and `ScrubberItem` (scrubber symbols).

- **Domain Layer**:
  - `ScrubberMath`: Pure Kotlin utility functions handling touch coordinates, clamping, and index calculations. Contains zero Android dependencies for fast JVM unit testing.

- **Presentation Layer**:
  - `LauncherViewModel`: Manages UI state via an immutable `LauncherUiState` emitted through `StateFlow`. Handles search debouncing, clock updates, and user intent dispatching.
  - `HomeScreen`: Root composable coordinating screen partitions, gesture interception, and overlay transitions.
  - Components: Modular, single-responsibility composables (`AlphabetScrubber`, `AppRowItem`, `ClockHeader`, `FavoritesSection`, `FilteredAppsSection`, `SearchScreen`).

---

## Project Structure

```
com.mubashshir.novafocus/
├── MainActivity.kt                 # Launcher activity with dark theme configuration
├── data/
│   ├── model/
│   │   ├── AppItem.kt              # App data model
│   │   └── ScrubberItem.kt         # Alphabet index model
│   ├── repository/
│   │   └── AppsRepository.kt       # PackageManager queries and launch intents
│   └── util/
│       └── IconCache.kt            # High-performance disk cache for app icons
├── domain/
│   └── ScrubberMath.kt             # Pure Kotlin math and index calculation
├── ui/
│   ├── components/
│   │   ├── AlphabetScrubber.kt     # Custom Canvas scrubber component
│   │   ├── AppRowItem.kt           # App row list item
│   │   ├── ClockHeader.kt          # Live digital clock and date
│   │   ├── FavoritesSection.kt     # Home screen favorites view
│   │   ├── FilteredAppsSection.kt  # Alphabet-filtered app list
│   │   ├── SearchOverlay.kt        # Search overlay container
│   │   └── SearchScreen.kt         # Search input and recent searches UI
│   ├── screens/
│   │   └── HomeScreen.kt           # Main launcher screen composable
│   ├── theme/
│   │   ├── Color.kt                # Color palette definition
│   │   ├── Dimensions.kt           # Spacing, sizing, and layout metrics
│   │   ├── Theme.kt                # Material3 theme setup
│   │   └── Type.kt                 # Typography hierarchy
│   └── viewmodel/
│       ├── LauncherUiState.kt      # Immutable UI state class
│       └── LauncherViewModel.kt    # StateFlow and business logic coordinator
```

---

## Dependencies

| Dependency | Purpose |
|---|---|
| `androidx.compose.bom:2026.02.01` | Bill of Materials for Compose version alignment |
| `androidx.activity:activity-compose:1.13.0` | Activity integration and back press dispatcher |
| `androidx.appcompat:appcompat:1.7.0` | Theme delegation and IME night mode support |
| `androidx.compose.material3:material3` | Material Design 3 UI components |
| `androidx.compose.ui:ui` & `graphics` | Canvas rendering, touch input, graphics primitives |
| `androidx.lifecycle:lifecycle-runtime-ktx` | Lifecycle-aware coroutine scopes and StateFlow |
| `junit:junit:4.13.2` | Unit testing framework |

---

## Building and Testing

### Run Unit Tests
```bash
./gradlew test
```

### Build Debug APK
```bash
./gradlew assembleDebug
```

### Install via ADB
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
