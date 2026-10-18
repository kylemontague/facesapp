# Google Play Store Listing & Registration Metadata

This document contains all copy-paste ready text, descriptions, questionnaire responses, and policy disclosures required to publish **Faces** on the Google Play Console for both Phone and Wear OS.

---

## 1. Store Listing Details

### App Title (Max 30 characters)
```text
Faces: Watch Face Studio
```
*(Character count: 24 / 30)*

### Short Description (Max 80 characters)
```text
Artisanal photo-to-watch-face studio with color extraction for Wear OS.
```
*(Character count: 70 / 80)*

### Full Description (Max 4,000 characters)
```text
Faces transforms your favourite photographs and color moments into artisanal, contemporary watch faces for Wear OS.

Snap a photo with your camera or select from your gallery: Faces extracts a vibrant 16-color palette using on-device color sampling, giving you a custom color story on your wrist in seconds.

✨ 5 TIMELESS WATCH FACE TEMPLATES

• Once Upon A Time: A typographic word clock that tells time in natural language. Clean, literary, and contemporary.
• Business Time: Bold, modern typography and clean digital metrics designed for the executive day.
• Filling Time: An expressive, graphical watch face that visually fills with color each minute and hour.
• Two Time: Dual-toned, architectural time display featuring high-contrast split palettes.
• Space-Time Prism: A dynamic triangulated geometric mesh generated dynamically from your photo’s palette.

🎨 TACTILE & PLAYFUL CUSTOMIZATION

• 4×4 Palette Grid: Drag and assign colors directly to background, hour, minute, and second dial elements.
• Shake to Shuffle: Give your phone a physical shake to instantly reshuffle and randomize color assignments with crisp haptic feedback.
• Live Ticking Preview: Test your watch face in real-time at 1 Hz directly on your phone, complete with realistic watch bezels and strap mockups.
• Ambient Mode Simulation: Switch between Active and Battery-Saving Ambient views with a single tap to see how your face looks when your watch screen dims.

⌚ WEAR OS COMPANION & STANDALONE

• Direct Sync: Broadcast your customized watch face to your Wear OS watch instantly over Bluetooth or Wi-Fi.
• Ambient Optimization: Deep `#000000` AMOLED backgrounds and low-bit hollow strokes protect battery life and prevent screen burn-in.
• Complications Support: Compatible with standard Wear OS complication providers so you can keep battery, step count, and date right on your dial.
• Hardware Adaptive: Automatically conforms to both circular (round) and rectangular (square) watch displays.

🔒 PRIVATE & ON-DEVICE

Faces processes all photos locally in memory on your phone. No images are ever uploaded to external servers, and no personal data is collected or tracked.

Elevate your wristwear. Create your custom watch face with Faces today.
```
*(Character count: 2,130 / 4,000)*

---

## 2. Categorization & Store Settings

| Setting | Value |
|---|---|
| **Application Type** | App |
| **Category** | Personalisation |
| **Tags** | Watch Face, Wear OS, Personalisation, Photography, Clock |
| **Email Address** | Your support email (e.g. `support@facesapp.watch`) |
| **Website URL** | `https://facesapp.watch` |
| **Privacy Policy URL** | `https://facesapp.watch/privacy` (or your GitHub raw markdown URL) |

---

## 3. Data Safety Form Responses

When completing the Google Play Console **Data Safety questionnaire**:

1. **Does your app collect or share any user data?**
   - Select: **No**

2. **Does the app use the Camera?**
   - The app uses `android.permission.CAMERA` solely for **ephemeral on-device palette extraction**:
     - *Is camera data collected?* **No** (Processed locally in RAM only, not saved to disk or network).
     - *Is camera data shared?* **No**.

3. **Does the app access physical sensors?**
   - The app uses the accelerometer (`Sensor.TYPE_ACCELEROMETER`) solely for the **shake-to-shuffle** color gesture on your phone:
     - *Is sensor data collected or transmitted?* **No**.

4. **Security Practices**:
   - Does your app provide a way for users to request that their data is deleted?
     - Select: **Not applicable** (no user account or personal data is collected).

---

## 4. Content Rating (IARC Questionnaire)

When taking the IARC questionnaire in Play Console:
- **Category**: Utility, Productivity, Communication, or Other
- **Violence**: No
- **Sexuality / Nudity**: No
- **Offensive Language**: No
- **Controlled Substances**: No
- **User Interactions**: Does the app allow users to exchange content?
  - Select **No** (sharing watch faces via deep link/QR uses the device's native share sheet and contains only color hex codes and template IDs, not user content/messages).
- **Resulting Rating**: **Everyone / PEGI 3 / USK 0**

---

## 5. Target Audience & App Content

- **Target Age**: Select **18 and older** or **13–17** (Do *not* check under 13 to avoid unnecessary COPPA/Designed for Families restrictions).
- **Ads**: Select **No, my app does not contain ads**.
- **Financial Features**: Select **None**.

---

## 6. Ready-to-Publish Privacy Policy

You can copy this text and host it at `https://facesapp.watch/privacy` or on a GitHub Gist:

```markdown
# Privacy Policy for Faces

Last Updated: October 2026

Faces ("we", "our", or "the app") values and respects your privacy. This policy outlines how information is handled when you use the Faces mobile companion app and Wear OS watch face.

### 1. Data Collection & Transmission
Faces does NOT collect, store, sell, or transmit any personally identifiable information (PII). All user-created watch faces and customized color palettes are stored strictly on your local device storage.

### 2. Camera Usage
Faces requests access to your device's Camera (`android.permission.CAMERA`) to allow you to sample colors from your real-world surroundings.
- Image frames are processed ephemerally in device memory using the Android Palette API to calculate dominant color swatches.
- Photographs taken within the app are NOT transmitted over the internet to any server.

### 3. Motion Sensors
Faces accesses the device accelerometer (`Sensor.TYPE_ACCELEROMETER`) solely to detect physical shaking gestures for reshuffling color assignments. Sensor readings are processed locally and discarded immediately.

### 4. Wear OS Communication
Watch face configurations, color swatches, and complication settings are communicated directly between your phone and your paired Wear OS smartwatch via Google Play Services Wearable Data Layer over local Bluetooth and Wi-Fi.

### 5. Contact
If you have any questions or feedback regarding this Privacy Policy, please contact us at:
support@facesapp.watch
```

---

## 7. Wear OS Form Factor Submission Checklist

In Play Console under **Release** → **Setup** → **Advanced settings** → **Form factors**:
- [x] **Wear OS form factor added**
- [x] **Standalone watch face service declared**: `com.google.android.wearable.standalone = true`
- [x] **Ambient Mode supported**: Hollow-stroke monochrome battery-saving mode implemented
- [x] **Screenshots uploaded**: 454 × 454 px circular watch face renders in `store_assets/wear/`
