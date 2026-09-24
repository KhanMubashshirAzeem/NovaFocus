# NovaFocus

A high-performance, minimalist Android launcher built with Jetpack Compose. Features an organic Gaussian curve alphabet scrubber, sub-5ms cold start icon rendering, universal gesture-driven search, and fluid 120 FPS animations.

---

## Architectural Highlights

- **Unidirectional Data Flow (MVI/MVVM)**: State is consolidated into a single immutable `LauncherUiState` emitted via `StateFlow` and consumed by Compose.
- **70/30 Screen Partition**: Touch event domains are strictly separated. The right 30% of screen width is dedicated to the alphabet scrubber, while the left 70% manages app content and vertical search gestures without touch conflicts.
- **Frame 0 Instant Icon Rendering**: Persistent internal disk cache (`IconCache`) eliminates the standard 1-second cold-start icon delay by loading cached app icons synchronously during initialization.
- **Organic Scrubber Physics**: The A–Z index bar deflects toward the user's touch following a Gaussian distribution curve and returns to resting position via a damped spring oscillator on release.
- **Universal Swipe-Up Search**: Pointer events are intercepted in `PointerEventPass.Initial` with vertical angle validation, allowing the search overlay to open from both the home screen and filtered alphabet lists.
- **Task Lifecycle and Backstack Stability**: Launch intents preserve backstack affinity, ensuring that pressing the system Back button in launched applications returns directly to NovaFocus.
- **Pure AMOLED Theming**: Edge-to-edge dark theme with forced dark soft keyboard (IME) palette matching the launcher interface.

---

## Mathematics and Physics

### 1. Gaussian Falloff Displacement
Each item in the scrubber calculates its horizontal deflection toward the finger based on its vertical distance to the active touch point:

$$\Delta X(y_i) = A \cdot \exp\left( -\frac{(y_i - y_{\text{touch}})^2}{2\sigma^2} \right)$$

- **A (Amplitude)**: Peak horizontal displacement at the touch point (~72 dp).
- **sigma (Spread Radius)**: Controls the width of the bell curve (~75 dp).
- Guarantees $C^\infty$ mathematical continuity with zero abrupt inflection points.

### 2. Damped Spring Return
Upon touch release, the bulge amplitude transitions to 0 using a damped spring physics model:

```kotlin
bulgeAnimatable.animateTo(
    targetValue = 0f,
    animationSpec = spring(
        dampingRatio = 0.52f,
        stiffness = Spring.StiffnessMediumLow
    )
)
```

The damping ratio of 0.52 produces a single subtle overshoot before settling to a resting straight line.

---

## Performance Optimizations

1. **Multi-Phase App Loading**:
   - **Phase 0 (Instant, < 5ms)**: Synchronous load of pre-cached favorite app icons from `context.filesDir/fav_icons/` on ViewModel creation.
   - **Phase 1 (Fast, < 30ms)**: Memory lookup of pre-filtered favorite apps to populate launcher rows.
   - **Phase 2 (Background, async)**: Full `PackageManager` scan on `Dispatchers.IO` to catalog all installed packages, extract metadata, and cache icons.

2. **Touch and Render Path Efficiency**:
   - Touch coordinates bypass Compose recomposition loops by driving `Canvas` offsets directly.
   - Reusable `Paint` objects and text bounds avoid garbage collection pressure during continuous 120 FPS scrubbing gestures.

3. **Debounced Search Pipeline**:
   - Search queries are debounced (300ms while typing, 0ms on clear) via Kotlin `Flow`, preventing unnecessary filtering operations on rapid keystrokes.

---

## Project Structure

```
com.mubashshir.novafocus/
├── MainActivity.kt                 # Edge-to-edge launcher activity & night mode configuration
├── data/
│   ├── model/
│   │   ├── AppItem.kt              # App entity (label, package, activity, bitmap)
│   │   └── ScrubberItem.kt         # Scrubber symbols (Star, A–Z, Dot)
│   ├── repository/
│   │   └── AppsRepository.kt       # PackageManager queries, caching, app launch intents
│   └── util/
│       └── IconCache.kt            # High-performance disk cache for app icon bitmaps
├── domain/
│   └── ScrubberMath.kt             # Pure mathematical functions for Gaussian curve and indices
├── ui/
│   ├── components/
│   │   ├── AlphabetScrubber.kt     # Custom Canvas scrubber with dynamic curve & bubble
│   │   ├── AppRowItem.kt           # App row presentation component
│   │   ├── ClockHeader.kt          # Digital clock and calendar date header
│   │   ├── FavoritesSection.kt     # Resting home screen favorites list
│   │   ├── FilteredAppsSection.kt  # Alphabet-filtered app list with swipe-back affordance
│   │   ├── SearchOverlay.kt        # Quick search sheet wrapper
│   │   └── SearchScreen.kt         # Full search UI with recent search history
│   ├── screens/
│   │   └── HomeScreen.kt           # Main composable orchestrator and gesture coordinator
│   ├── theme/
│   │   ├── Color.kt                # AMOLED dark palette definition
│   │   ├── Dimensions.kt           # UI metrics, padding tokens, curve constants
│   │   ├── Theme.kt                # Material3 dark theme setup
│   │   └── Type.kt                 # Typography hierarchy
│   └── viewmodel/
│       ├── LauncherUiState.kt      # Immutable UI state data class
│       └── LauncherViewModel.kt    # State management, search debounce, clock ticker
```

---

## Dependencies

| Dependency | Purpose |
|---|---|
| `androidx.compose.bom:2026.02.01` | Compose dependency management |
| `androidx.activity:activity-compose` | Compose-Activity bridging and back handler |
| `androidx.appcompat:appcompat:1.7.0` | Dark mode theme delegation and IME palette support |
| `androidx.compose.material3:material3` | Material3 foundation and components |
| `androidx.compose.ui:ui` & `graphics` | Canvas rendering, pointer input, graphics layer |
| `androidx.lifecycle:lifecycle-runtime-ktx` | Coroutines, ViewModel scopes, StateFlow |
| `junit:junit:4.13.2` | Unit testing for domain logic and repositories |

*The Gaussian curve deflection, bubble tracking, and spring release animations are custom-built without third-party animation libraries.*

---

## Build and Test

### Compile and Run Unit Tests
```bash
./gradlew test
```

### Build Debug APK
```bash
./gradlew assembleDebug
```

### Install on Device via ADB
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
