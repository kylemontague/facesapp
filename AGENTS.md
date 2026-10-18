# AGENTS.md — AI Agent Guidelines for Faces Rebuild

This file provides architectural context, strict non-negotiables, design rules, and operational guidelines for AI coding agents modernizing the **Faces** Android & Wear OS codebase.

---

## 1. Project Overview & Philosophy

**Faces** is an artisanal photo-to-watch-face companion application for Android and Wear OS.
- **The Core User Loop**: The user snaps a photo -> extracts a rich 16-color palette via Palette API -> customizes watch face templates (Once Upon A Time, Business Time, Filling Time, Two Time, Space-Time Prism) -> interacts via drag-to-assign and shake-to-shuffle -> previews live ticking in Active and Ambient modes -> syncs directly to their Wear OS watch.
- **Primary Directive**: Rebuild the application using **modern Android & Kotlin practices (2025/2026 standards)** while **religiously preserving the exact visual identity, typography, dark aesthetic, and tactile micro-interactions**.

---

## 2. Non-Negotiable Core Rules

1. **Strict UI/UX Preservation**:
   - **Dark Luxury Aesthetic**: Deep black background (`#080a0d`), high-contrast white text/accents, top and bottom vignette overlays (`top_gradient`, `bottom_gradient`), and translucent bordered buttons (`#33ffffff` fill, `1dp` white border, `4dp` corners).
   - **Typography**: Retain the exact font pairing from `assets/fonts/`:
     - **Quicksand Bold**: Titles, action buttons, mode switches.
     - **Proxima Nova Light / Semibold**: Word clock time strings, digital numerals, subtitles.
   - **Watch Strap Mockup & Depth Carousel**: Centered realistic watch strap graphics (`watch_strap.png`) with round and square bezels, accompanied by depth-scaling page transitions (adjacent slides shrink to 0.75x scale and fade).
   - **Tactile Inputs**:
     - 4x4 circular color swatch grid with white indicator rings.
     - Accelerometer shake detection triggering haptic vibration and palette re-randomization.
     - Real-time 1 Hz ticking preview and Ambient vs. Screen On toggle.

2. **No Deprecated Legacy Artifacts**:
   - ❌ **Zero Java**: All new code must be 100% Kotlin with Coroutines and StateFlow.
   - ❌ **Zero Realm 3.5.0**: Must be replaced completely with **Room** (`androidx.room`).
   - ❌ **Zero `android.support.*`**: Everything must use modern `androidx.*` libraries.
   - ❌ **Zero `CanvasWatchFaceService`**: Wear OS watch face must use modern **AndroidX Watchface** (`androidx.wear.watchface`).
   - ❌ **Zero `wearApp project(':wear')`**: Wear OS is a **standalone** module, communicated with via the Google Play Wearable Data Layer (`Wearable.getDataClient` / `Wearable.getMessageClient`).
   - ❌ **Zero `GoogleApiClient`**: Use Kotlin Coroutines Tasks extension (`await()`) on Google Play Services clients.

3. **Shared Rendering Source of Truth**:
   - The watch face rendering math in `faces_lib/src/main/java/slurpp/faces_lib/WatchDraw.java` must be ported to a shared `:core` module so both the **Mobile Compose preview** and the **Wear OS watch face** share the exact same rendering logic without duplicating drawing routines.

---

## 3. Architecture & Module Structure

```
Faces/
├── gradle/
│   └── libs.versions.toml      # Single source of truth for dependencies and plugins
├── core/                       # Shared Kotlin library (:core)
│   ├── src/main/kotlin/
│   │   ├── watch/facesapp/core/
│   │   │   ├── model/         # FaceConfig, FaceStyle, ColorPalette, FaceMode
│   │   │   ├── render/        # WatchRenderer (Canvas/DrawScope engine for all 5 faces)
│   │   │   ├── mesh/          # Trianglify mesh generator for Prism face
│   │   │   └── util/          # TimeUtils, NumbersToWords, ColorContrast, FaceShareUtils
│   │   └── res/               # Fonts (Quicksand, ProximaNova), SVGs, strap drawables
├── mobile/                     # Phone Application (:mobile)
│   ├── src/main/kotlin/
│   │   ├── watch/facesapp/mobile/
│   │   │   ├── data/          # Room DB (FaceEntity, FaceWearLogEntity, FaceDao), WearSyncManager
│   │   │   ├── camera/        # CameraX integration & Palette extraction
│   │   │   ├── sensor/        # ShakeDetector Flow with HapticFeedback
│   │   │   └── ui/            # Jetpack Compose UI
│   │   │       ├── home/      # Depth carousel, strap mockup, timeline & share sheets
│   │   │       ├── customise/ # Blurred backdrop, 4x4 swatch grid, drag-to-assign
│   │   │       └── theme/     # FacesTheme (Color, Typeface, Gradients)
└── wear/                       # Standalone Wear OS Watch Face (:wear)
    ├── src/main/kotlin/
    │   └── watch/facesapp/wear/
    │       ├── service/       # WatchFaceService (AndroidX Watchface API)
    │       ├── renderer/      # CanvasWatchFaceRenderer adapter using :core renderer
    │       └── datalayer/     # WearableListenerService for real-time sync
```

---

## 4. Key Implementation Patterns

### A. UI Theme & Tokens
- Background: `Color(0xFF080A0D)`
- Surface/Card Translucent: `Color(0x33FFFFFF)`
- Border: `BorderStroke(1.dp, Color.White)` with `RoundedCornerShape(4.dp)`
- Typography: Always load fonts from font resources using `FontFamily(Font(R.font.quicksand_bold))` and `FontFamily(Font(R.font.proximanova_semibold))`. Do not fallback to default sans-serif.

### B. Mobile Home Carousel Transition
In Compose `HorizontalPager`, apply depth transformation:
```kotlin
Modifier.graphicsLayer {
    val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
    if (pageOffset < -1f || pageOffset > 1f) {
        alpha = 0f
    } else if (pageOffset <= 0f) {
        alpha = 1f
        scaleX = 1f
        scaleY = 1f
        translationX = 0f
    } else {
        alpha = 1f - pageOffset
        val scale = 0.75f + (1f - 0.75f) * (1f - pageOffset)
        scaleX = scale
        scaleY = scale
        translationX = size.width * -pageOffset
    }
}
```

### C. Shake Detection & Haptics
- Implement `ShakeDetector` as a Kotlin Coroutine cold `Flow<Unit>` wrapping `SensorManager` (`Sensor.TYPE_ACCELEROMETER`).
- On shake: trigger `HapticFeedbackType.LongPress` or `Vibrator.vibrate(VibrationEffect.createOneShot(200, DEFAULT_AMPLITUDE))` and reshuffle selected swatch indices.

### D. Palette Extraction
- Use `androidx.palette.graphics.Palette.from(bitmap).maximumColorCount(20).generate()`.
- Sort swatches by Chroma/Saturation to yield 16 vibrant colors filling the 4x4 grid.

---

## 5. Agent Workflow Rules

1. **Incremental Execution**: Never overwrite large sections of code blindly. Make focused, testable edits.
2. **Preserve Legacy Assets**: Keep all vector drawables, watch strap PNGs (`watch_strap.png`), and TTF/OTF font files intact.
3. **Continuous Verification**: After writing or modifying Gradle files, verify dependencies and test compilation.
4. **Follow `REBUILD_PLAN.md`**: Refer to `REBUILD_PLAN.md` for task sequence, checklists, and status tracking.
