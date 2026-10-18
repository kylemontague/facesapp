# Faces Modernization & Rebuild Plan

This document outlines the detailed step-by-step engineering roadmap for rebuilding the **Faces** Android & Wear OS application using modern Android development practices (Kotlin 2.x, Jetpack Compose, Room, CameraX, AndroidX Watchface API) while rigorously preserving its vintage dark luxury aesthetic, typography, and tactile interactions.

---

## 1. Architecture & Technology Stack Comparison

| Component | Legacy Architecture (~2015–2018) | Modern Rebuilt Architecture (2025/2026) |
| :--- | :--- | :--- |
| **Language** | Java 7/8 | **100% Kotlin** + Coroutines & StateFlow |
| **Build Tooling** | Gradle 4.1, AGP 3.0.1, Groovy DSL, `jcenter` | **Gradle 8.7+**, AGP 8.5+, Kotlin DSL (`.gradle.kts`), Version Catalog (`libs.versions.toml`) |
| **Target Platforms** | Mobile + Embedded Wear App (SDK 18–25) | Mobile (Min SDK 26, Target SDK 34/35) + **Standalone Wear OS 3–5** |
| **UI Framework** | Imperative XML, `android.support.*`, `ViewPager` | **Jetpack Compose** + Material 3 custom tokens |
| **Image Processing** | Legacy `TextureView` Camera, `support:palette-v7` | **CameraX** (`androidx.camera`), `androidx.palette:palette-ktx` |
| **Local Storage** | Realm Java 3.5.0 (`io.realm:realm-gradle-plugin`) | **Room Database** (`androidx.room`) with Flow & Coroutines |
| **Sensors & Haptics** | `com.squareup:seismic:1.0.2` & raw `Vibrator` | Kotlin Coroutines `SensorManager` Flow + `HapticFeedbackConstants` |
| **Wear OS Watchface** | `CanvasWatchFaceService` & `WearableSupportLibrary` | **AndroidX Watchface API** (`androidx.wear.watchface:watchface`) |
| **Wearable Sync** | `GoogleApiClient` (MessageApi / DataApi) | **Wearable.getDataClient()** & `getMessageClient()` via `kotlinx-coroutines-play-services` |
| **Shared Code** | `:faces_lib` (Android Library with Realm plugin) | `:core` (Clean Kotlin Android library, zero database coupling) |

---

## 2. Target Project Structure

```
Faces/
├── gradle/
│   └── libs.versions.toml                 # Centralized dependency catalog
├── build.gradle.kts                       # Root build script
├── settings.gradle.kts                    # Module inclusion (:core, :mobile, :wear)
│
├── core/                                  # Shared domain, algorithms & rendering
│   ├── build.gradle.kts
│   └── src/main/
│       ├── kotlin/com/faces/core/
│       │   ├── model/
│       │   │   ├── FaceStyle.kt           # Enum: OUA_TIME, FILL_TIME, BUSINESS_TIME, TWO_TIME, PRISM_TIME
│       │   │   ├── FaceConfig.kt          # Configuration data classes (colors, bounds, ambient state)
│       │   │   └── ColorPalette.kt        # 16-color quantized swatch container
│       │   ├── render/
│       │   │   ├── WatchRenderer.kt       # Canvas drawing dispatcher
│       │   │   ├── faces/
│       │   │   │   ├── OUATRenderer.kt    # "Once upon a time..." word-clock renderer
│       │   │   │   ├── BusinessRenderer.kt# Minimalist analog hands renderer
│       │   │   │   ├── FillRenderer.kt    # Sweeping filled arcs / sectors
│       │   │   │   ├── TwoTimeRenderer.kt # Dual dual-tone split time display
│       │   │   │   └── PrismRenderer.kt   # Triangulated polygonal mesh face
│       │   ├── mesh/
│       │   │   ├── Trianglify.kt          # Delaunay/polygon mesh generator for Prism face
│       │   │   └── PointGenerators.kt     # Regular & perturbed point distribution
│       │   └── util/
│       │       ├── NumbersToWords.kt      # Word conversion ("nine forty seven")
│       │       ├── TimeUtils.kt           # Calendar & formatting helpers
│       │       └── ColorContrast.kt       # WCAG readability ratio checks
│       └── res/
│           ├── font/                      # quicksand_bold.otf, proximanova_*.otf
│           ├── drawable/                  # watch_strap.png, watch_strap_square.png
│           └── values/                    # Core strings, dimension tokens
│
├── mobile/                                # Handheld phone application
│   ├── build.gradle.kts
│   └── src/main/
│       ├── kotlin/com/faces/mobile/
│       │   ├── data/
│       │   │   ├── db/
│       │   │   │   ├── FacesDatabase.kt   # Room database definition
│       │   │   │   ├── FaceEntity.kt      # Face settings entity (replaces Realm FaceSettings)
│       │   │   │   └── FaceDao.kt         # Reactive Flow-based queries
│       │   │   └── sync/
│       │   │       └── WearSyncManager.kt # DataClient / MessageClient sync to Wear OS
│       │   ├── camera/
│       │   │   ├── CameraManager.kt       # CameraX lifecycle & bitmap capture
│       │   │   └── PaletteExtractor.kt    # 16-swatch extraction and sorting
│       │   ├── sensor/
│       │   │   └── ShakeDetector.kt       # Accelerometer Flow for shake gesture
│       │   └── ui/
│       │       ├── theme/                 # Dark luxury theme, colors, typography
│       │       ├── components/
│       │       │   ├── FacesButton.kt     # Translucent frosted button with white border
│       │       │   ├── WatchPreview.kt    # Compose custom canvas invoking WatchRenderer
│       │       │   └── SwatchGrid.kt      # 4x4 circular color chips with marker rings
│       │       ├── home/
│       │       │   ├── HomeViewModel.kt   # Carousel state, ambient toggle, favorites
│       │       │   └── HomeScreen.kt      # HorizontalPager with depth transform
│       │       ├── camera/
│       │       │   └── CameraScreen.kt    # Fullscreen CameraX viewfinder
│       │       └── customise/
│       │           ├── CustomiseViewModel.kt# Palette state, drag markers, shake action
│       │           └── CustomiseScreen.kt # Blurred backdrop, swatch grid, save
│       └── AndroidManifest.xml
│
└── wear/                                  # Standalone Wear OS Watch Face
    ├── build.gradle.kts
    └── src/main/
        ├── kotlin/com/faces/wear/
        │   ├── service/
        │   │   └── FacesWatchFaceService.kt# AndroidX Watchface service
        │   ├── renderer/
        │   │   └── FacesCanvasRenderer.kt # CanvasWatchFaceRenderer adapting :core WatchRenderer
        │   └── datalayer/
        │       └── WearMessageListener.kt # Receives live palette JSON / DataItems
        ├── res/
        │   ├── xml/watch_face.xml
        │   └── values/
        └── AndroidManifest.xml            # Standalone Wear OS manifest
```

---

## 3. File-by-File Migration Matrix

| Legacy File (Java) | Modern Replacement (Kotlin) | Notes / Refactoring Strategy |
| :--- | :--- | :--- |
| `slurpp.faces_lib.WatchDraw` | `com.faces.core.render.WatchRenderer` | Break down 770-line monolithic class into modular renderers per face style. Port Canvas math directly. |
| `slurpp.faces_lib.templates.OUATFace` | `com.faces.core.render.faces.OUATRenderer` | Once Upon A Time word-clock logic. Extract text scaling and time conversion. |
| `slurpp.faces_lib.templates.FacesTrianglifyDrawable` | `com.faces.core.mesh.Trianglify` | Decouple from outdated `com.manolovn:trianglify` 1.1.0; write clean Kotlin 2D triangulation. |
| `slurpp.faces_lib.utils.NumbersToWords` | `com.faces.core.util.NumbersToWords` | Pure Kotlin utility converting hours (1..12) and minutes (0..59) to english words. |
| `slurpp.faces_lib.v2.models.FaceSettings` (Realm) | `com.faces.mobile.data.db.FaceEntity` (Room) | Replace `RealmObject` with standard immutable data class and Room `@Entity`. |
| `slurpp.faces.activities.Home` | `com.faces.mobile.ui.home.HomeScreen` + `HomeViewModel` | Replace `ViewPager`, `WatchPageAdapter`, and `DepthPageTransformer` with Compose `HorizontalPager` + depth `graphicsLayer`. |
| `slurpp.faces.activities.CustomiseActivity` | `com.faces.mobile.ui.customise.CustomiseScreen` + `CustomiseViewModel` | Replace `GridView` and `SwatchAdapter` with Compose `LazyVerticalGrid` + stateful marker selection. |
| `slurpp.faces.activities.CameraActivity` | `com.faces.mobile.ui.camera.CameraScreen` | Replace raw `TextureView` camera with modern **CameraX** `PreviewView`. |
| `slurpp.faces.controls.SwatchAdapter` | `com.faces.mobile.ui.components.SwatchGrid` | Compose declarative item rendering with animated stroke selection ring. |
| `slurpp.faces.services.MobileWearableService` | `com.faces.mobile.data.sync.WearSyncManager` | Modern Coroutine-based `DataClient.putDataItem()` and `MessageClient.sendMessage()`. |
| `slurpp.faces.watchfaces.MorphWatchFaceService` | `com.faces.wear.service.FacesWatchFaceService` | Replace deprecated `CanvasWatchFaceService` with `androidx.wear.watchface.WatchFaceService`. |

---

## 4. Phase-by-Phase Implementation Roadmap

### Phase 1: Build System & Gradle Modernization
- [x] **1.1 Version Catalog Setup**: Create `gradle/libs.versions.toml` defining AGP 8.5+, Kotlin 2.0+, Compose BOM, Room 2.6+, CameraX 1.3+, AndroidX Wear Watchface 1.2+, Coroutines, and test dependencies.
- [x] **1.2 Root Build Scripts**: Migrate `build.gradle` and `settings.gradle` to `build.gradle.kts` and `settings.gradle.kts`. Configure repository mirrors (`google()`, `mavenCentral()`). Purge `jcenter()`.
- [x] **1.3 Module Structure**: Configure `:core`, `:mobile`, and `:wear` subprojects with unified Kotlin JVM targets (Java 17 / JVM 17) and SDK versions (`minSdk = 26`, `targetSdk = 34`).

### Phase 2: `:core` Shared Rendering Engine
- [x] **2.1 Domain Models**: Implement `FaceStyle`, `FaceConfig`, `ColorPalette`, `FaceMode` (Time, Date, Steps).
- [x] **2.2 Typography & Asset Migration**: Move `Quicksand-Bold.otf` and `ProximaNova-*.otf` into `core/src/main/res/font/`. Move `watch_strap.png` and `watch_strap_square.png` into `core/src/main/res/drawable/`.
- [x] **2.3 Face Renderers**:
  - Implement `OUATRenderer` (word clock with text wrapping and sizing).
  - Implement `BusinessRenderer` (clean analog hour, minute, second hands, ambient stroke).
  - Implement `FillRenderer` (filled background sector with contrasting clock hands).
  - Implement `TwoTimeRenderer` (split-color digital typography).
  - Implement `PrismRenderer` with native Kotlin `Trianglify` polygonal mesh background generation.
- [x] **2.4 Unified WatchRenderer**: Provide a single render function:
  `fun render(canvas: Canvas, bounds: Rect, style: FaceStyle, config: FaceConfig, time: Calendar, isAmbient: Boolean, shape: WatchShape)`
- [x] **2.5 Unit Testing**: Verify `NumbersToWords` and color contrast computations with JUnit tests.

### Phase 3: Mobile Persistence & Wear Sync Layer
- [x] **3.1 Room Database**:
  - Define `FaceEntity` with primary key UUID, style enum, JSON/list color array converter, favorite flag, timestamp.
  - Implement `FaceDao` with queries for favorites, recent familiar faces, and CRUD operations.
  - Populate stock presets on initial database creation.
- [x] **3.2 Wearable Sync Manager**:
  - Implement `WearSyncManager` using `Wearable.getDataClient(context)` and `Wearable.getMessageClient(context)`.
  - Serialise `CustomFace` payload via Kotlinx Serialization or Moshi to transmit current face configuration to Wear OS.

### Phase 4: Mobile Handheld UI in Jetpack Compose
- [x] **4.1 Design System & Theme**:
  - `FacesTheme`: Black background (`#080a0d`), frosted glass tokens (`#33ffffff`), font definitions for Quicksand and Proxima Nova.
  - `FacesPillButton`: Translucent rounded button matching `button_transparent.xml`.
  - `VignetteOverlays`: Top and bottom gradient scrims matching `top_gradient.xml` and `bottom_gradient.xml`.
- [x] **4.2 Home Carousel (`HomeScreen`)**:
  - `HorizontalPager` with custom `graphicsLayer` depth transition (0.75 scale down + fade).
  - Centered watchstrap graphic with custom Compose `Canvas` rendering the live ticking watchface via `WatchRenderer`.
  - Top information pill displaying relative time ("10 minutes ago", "On yer wrist!", "Faces").
  - Actions: Ambient / Screen On mode switch, Favorite toggle, Delete, Customize / Select Face.
- [x] **4.3 Camera Experience (`CameraScreen`)**:
  - CameraX `PreviewView` embedded in Compose.
  - Circular shutter button with tap-to-capture.
  - On capture: downsample bitmap, compute `Palette`, extract 16 sorted vibrant swatches, navigate to Customize screen.
- [x] **4.4 Customize & Swatch Screen (`CustomiseScreen`)**:
  - Background: Photo with Gaussian blur and desaturation (`RenderEffect` / `ColorFilter`).
  - 4x4 circular swatch grid with animated marker indicators (Background, Hour, Minute, Second, Accent).
  - Tap / Drag interaction to reassign colors.
  - Shake detection via `SensorManager` Flow: trigger haptic feedback and re-randomize color assignments.
  - Live preview syncing to Wear OS / local preview.
  - "Save Face" action committing to Room and pushing to watch.

### Phase 5: Wear OS Module Modernization
- [ ] **5.1 Manifest & Standalone Setup**: Mark `android:standalone="true"` in Wear OS `AndroidManifest.xml`.
- [ ] **5.2 AndroidX Watchface Service**:
  - Create `FacesWatchFaceService : WatchFaceService()`.
  - Implement `FacesCanvasRenderer : CanvasWatchFaceRenderer` delegating directly to `:core`'s `WatchRenderer`.
  - Handle interactive mode (1 Hz invalidate) and ambient mode (minute tick, zero seconds, low-power monochrome).
- [ ] **5.3 Data Layer Listener**:
  - Implement `WearMessageListener : WearableListenerService` to receive real-time palette configurations from handheld and trigger watchface refresh.

### Phase 6: Visual Parity & Quality Verification
- [ ] **Visual Parity**: Verify side-by-side screenshots with the original app (gradient depths, button padding, font kerning, clock hand ratios).
- [ ] **Ambient Mode Behavior**: Validate power-saving contrast in ambient state on AMOLED watch display.
- [ ] **Haptics & Sensorial Polish**: Test shake sensitivity and vibration feedback.

---

## 5. Risk Assessment & Mitigations

| Risk | Impact | Mitigation |
| :--- | :--- | :--- |
| **Wear OS Watch Face Format (WFF) requirement on Wear OS 5** | Medium | Google Play requires WFF for new submissions on Wear OS 5+, but AndroidX Watchface API in Kotlin remains fully functional for devices up to Wear OS 4 and developer sideloading. We keep the Kotlin Canvas renderer in `:core` for 100% parity with mobile preview, and can output a WFF XML generator if Play Store submission requires it later. |
| **Trianglify 1.1.0 dependency is broken on modern Gradle** | High | The legacy library is unmaintained. We implement our own lightweight Kotlin Delaunay mesh generator (~120 LOC) in `:core.mesh` which gives faster render times and zero external dependency risk. |
| **Camera aspect ratio & rotation fragmentation** | Low | CameraX automatically handles sensor orientation and aspect ratio matching across diverse Android OEMs. |
