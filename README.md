# NovaFocus — Alphabet Launcher

A minimal, high-performance Android launcher featuring a curved vertical A–Z index scrubber with organic spring physics, real-time app filtering, and zero-stutter 60/120 FPS animations.

---

## 📱 Features

1. **Resting Home Screen**:
   - Digital clock and date updating live in real time.
   - Clean, vertically-stacked favorites list (up to 7 apps) with smart matching for popular apps (WhatsApp, Chrome, Camera, Gmail, etc.) and fallback to installed apps.
2. **A–Z Index Bar (Gaussian Scrubber)**:
   - Vertically centered on the right edge with top Star (`☆`), letters `A` to `Z`, and bottom Dot (`•`).
   - Dynamic Gaussian curve displacement: the bar smoothly bends toward the finger as you touch and drag.
   - Enlarged circular **Letter Bubble** tracking the finger horizontally and vertically.
   - Tactile haptic feedback tick each time the active letter changes.
3. **Filtered App Screen**:
   - Replaces resting home content instantaneously with a large letter header (e.g. **K**, **Y**, **D**) and all installed apps starting with that letter.
   - Case-insensitive sorting with clean empty states ("No apps") when no matching apps are found.
4. **Spring Physics Release**:
   - Releasing the touch gesture triggers an organic spring animation with realistic bounce/overshoot settling the bar back to a straight line.
5. **App Launching**:
   - Tapping any app row launches the application immediately via Android `Intent`.
6. **Swipe-up Search (Bonus)**:
   - Swipe up from the home screen opens the quick search overlay with auto-focused keyboard and instant search filtering.

---

## 🧮 How the Curve Animation Works

The curve animation is implemented directly in custom Jetpack Compose `Canvas` with pure mathematics and zero external animation libraries.

### 1. Gaussian Falloff Displacement
For any scrubber item with vertical center $y_i$ and current touch position $y_{\text{touch}}$, the horizontal displacement $\Delta X(y_i)$ (inward towards the left) is calculated as:

$$\Delta X(y_i) = A \cdot \exp\left( -\frac{(y_i - y_{\text{touch}})^2}{2\sigma^2} \right)$$

- **$A$ (Amplitude)**: The peak displacement at the touch point ($\sim 72\,\text{dp}$).
- **$\sigma$ (Sigma / Spread Radius)**: Controls how wide the bulge spreads ($\sim 75\,\text{dp}$).
- Letters closest to the finger deflect the most, while distant letters smoothly fall off towards $0$, creating a continuous, bell-shaped curve with no sharp kinks.

### 2. Spring Physics on Release
When the user releases their finger:
- An `Animatable(0f)` animates the amplitude from $A$ to $0$ using Compose's `spring`:
  ```kotlin
  bulgeAnimatable.animateTo(
      targetValue = 0f,
      animationSpec = spring(
          dampingRatio = 0.52f, // Organic spring overshoot
          stiffness = Spring.StiffnessMediumLow
      )
  )
  ```
- This creates the exact overshoot and settling bounce seen in the reference video.

### 3. Tactile Feedback & Magnified Bubble
- When the closest letter index changes, a haptic feedback tick (`VibrationEffect.EFFECT_TICK`) is dispatched.
- The floating bubble is drawn at:
  $$X_{\text{bubble}} = X_{\text{base}} - A - \text{bubbleRadius} - \text{offset}$$
  $$Y_{\text{bubble}} = \text{clamp}(y_{\text{touch}}, \text{minY}, \text{maxY})$$

---

## ⚡ Performance & Zero-Jank Architecture

- **Zero-allocation on touch/draw path**: Text paints and offsets are reused; Canvas drawing avoids recompositions during 120 FPS scrub gestures.
- **Background `PackageManager` caching**: Installed apps and app icons are resolved once on `Dispatchers.IO` and cached in memory.
- **Pre-rendered `ImageBitmap`**: App icons are converted to Compose `ImageBitmap` in the repository, avoiding `Drawable` rendering costs on the main UI thread.
- **Android 11+ Package Visibility**: `android.permission.QUERY_ALL_PACKAGES` is properly declared to discover all launchable user apps.

---

## 🏗️ Architecture & Project Structure

The project follows Clean Architecture with MVVM / MVI unidirectional state flow:

```
com.mubashshir.novafocus/
├── data/
│   ├── model/
│   │   ├── AppItem.kt              # App entity with label, package, ImageBitmap icon
│   │   └── ScrubberItem.kt         # Star, A-Z letters, Dot
│   └── repository/
│       └── AppsRepository.kt       # PackageManager queries, caching, smart favorites
├── domain/
│   └── ScrubberMath.kt             # Pure Gaussian curve math & item resolution (100% testable)
├── ui/
│   ├── components/
│   │   ├── AlphabetScrubber.kt     # Custom Canvas scrubber with curve & spring physics
│   │   ├── AppRowItem.kt           # App list row with icon, title, and ripple
│   │   ├── ClockHeader.kt          # Live digital clock & date header
│   │   ├── FavoritesSection.kt     # Home resting favorites list
│   │   ├── FilteredAppsSection.kt  # Letter header, alphabetized apps & empty state
│   │   └── SearchOverlay.kt        # Swipe-up app search with auto keyboard
│   ├── screens/
│   │   └── HomeScreen.kt           # Screen orchestrator
│   ├── theme/
│   │   ├── Color.kt                # OLED black palette
│   │   ├── Dimensions.kt           # Design tokens (sizes, paddings, curve constants)
│   │   ├── Theme.kt                # Edge-to-edge system bar theme
│   │   └── Type.kt                 # Minimal typography
│   └── viewmodel/
│       ├── LauncherUiState.kt      # Immutable launcher state
│       └── LauncherViewModel.kt    # StateFlow, clock updates, search & scrubber handling
└── MainActivity.kt                 # Edge-to-edge Launcher activity
```

---

## 📦 Third-Party Libraries Used

In accordance with the assignment guidelines, external libraries are kept minimal and fully justified:

| Library | Version | Purpose |
|---|---|---|
| `androidx.compose.bom` | `2026.02.01` | Jetpack Compose Bill of Materials ensuring version compatibility. |
| `androidx.activity:activity-compose` | `1.13.0` | Activity integration with Compose and `by viewModels()`. |
| `androidx.compose.material3:material3` | BOM | Material3 design system foundation. |
| `androidx.compose.ui:ui` & `graphics` | BOM | Compose UI canvas, pointer gestures, and graphics primitives. |
| `androidx.core:core-ktx` | `1.19.0` | Kotlin extensions for Android Core APIs. |
| `androidx.lifecycle:lifecycle-runtime-ktx` | `2.11.0` | Coroutine lifecycle scopes and Flow utilities. |
| `junit:junit` | `4.13.2` | Unit testing framework for math and repository tests. |

*Note: The curve physics, Gaussian deflection, and spring animations are 100% custom-written without any third-party gesture or animation libraries.*

---

## 🚀 Building & Running

1. Open project in **Android Studio Meerkat / Ladybug or newer**.
2. Run single Gradle sync:
   ```bash
   ./gradlew assembleDebug
   ```
3. Run unit tests:
   ```bash
   ./gradlew test
   ```
4. Install on device or emulator:
   ```bash
   ./gradlew installDebug
   ```
