# 🌊 PathoFlow: Offline Pathology Collection Companion (v1.3.1)

Welcome to **PathoFlow** — a high-performance clinical companion app designed specifically for phlebotomists and clinical laboratory operators. 

PathoFlow is built to work **100% offline**, allowing you to seamlessly register patients, manage diagnostic directories, check out with payment confirmation, and transmit full clinical records directly to your laboratory's WhatsApp in one unified send flow!

> [!NOTE]
> *"This is the first offline Pathology collection entry app which directly sends all the data to the respective lab's WhatsApp. I made this app for my own clinical purpose. This is all vibe-coded! I am not a developer nor a programmer, just trying vibe coding."* — **hastjosh1**

---

## 🎨 Premium Visual Aesthetics (v1.3.1 Upgrades)
PathoFlow is designed with a premium, state-of-the-art medical clinic layout:
* **Vertical Gradients**: SplashScreen and onboarding setup screens render dynamic gradients blending clinician-teal and slate color palettes.
* **Unified Rounded Cards**: Every patient entry, metric grid, and text form utilizes cohesive `RoundedCornerShape(16.dp)` configurations and thin borders.
* **Vibrant Quick Actions**: Primary buttons (like "New Patient Entry") utilize primary-to-tertiary linear gradients to guide phlebotomists intuitively.
* **Transparent Action Badges**: Actions (Edit, Recopy, WhatsApp export) feature circular semi-transparent backgrounds to reduce visual clutter.

---

## 🚀 Key Clinical Features

### 1. 🔍 Buttery-Smooth Diagnostic search & Selected Chips
* **Zero Lag Catalog**: The massive 100+ diagnostic test directory is completely hidden by default when the search is blank, preventing rendering lag on mobile devices.
* **Favorites & Smart Shortcuts**: Displays a static "Pinned Favorites" chip row and a dynamic "Frequently Used" row calculated automatically from your actual usage count!
* **Selected Tests Chips**: Displays active diagnostic items as checked filter chips. Tap any chip to instantly remove the test and update the running total.
* **Clear-on-Select**: Typing search results, selecting an item, and adding it instantly clears the search query to make room for your next selection.

### 2. 📲 Multi-Device Self-Hosted OTA Updates
* **Immediate Check on Startup**: When launched, the app initiates an asynchronous background query to check a custom cloud updates URL.
* **passcode PIN Locked Config**: Settings are fully locked via an administrative PIN passcode to prevent unauthorized modifications.
* **Manual Check for Update Button**: Includes a manual check tool with real-time progress (`Checking...`, `Up to date (v1.3.1)`, `Network connection failed`, or `Update Available`).
* **Contextual In-App Installation**: If an update is detected, an **"Install Update"** action button displays inside the settings card to download and install the package without leaving the app.

### 3. 💳 Dynamic UPI QR & Custom Merchant QR Uploads
* **Offline Dynamic QR**: Automatically draws a vector Canvas QR code using the patient's exact bill and your static UPI parameters offline.
* **Custom static QR Image**: Admin settings allow you to upload your laboratory's static merchant QR code image directly from the gallery. If uploaded, PathoFlow displays this custom business image on checkout screens dynamically!

### 4. 📷 Native Transaction Screenshot Capture
* **Physical Payment Proof**: Added a **"Take Photo of Transaction Screen"** action inside checkout success dialogs.
* **FileProvider Integration**: Securely launches the native system camera and caches the confirmation thumbnail.
* **Multimodal WhatsApp Share**: Attaches the payment confirmation photograph alongside the beautifully structured markdown patient details in one single transmit command!

### 5. 📂 Automated Factory Pricing Seeding
* **Quoted CSV Importing**: Optimized CSV directory reader handles complex quoted lists containing inner commas without breaking schemas.
* **Smart Keyword Mapping**: Imported diagnostics automatically map to medical categories (Hematology, Biochemistry, Hormones, Packages, Serology) based on test naming structures.

---

## 🛠️ Technology Stack
* **Language**: Kotlin 1.9+ (Jetpack Compose)
* **Design Guidelines**: Material Design 3 (Clinician Theme)
* **Database**: Room Database (SQLite Engine)
* **Multithreading**: Kotlin Coroutines & Flows
* **Build tool**: Gradle Kotlin DSL (`.kts`)

---

## 📦 Building and Packaging

### Requirements
* Android Studio (Koala or newer)
* Android SDK (API 24 to API 36)
* Jetpack Runtime (JBR compiler Java 11+)

### Compile via Terminal
To build and package a signed clinical binary (`PathoFlow.apk`) locally in your workspace:
```bash
# Set JBR compiler environment and run Gradle build
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" gradle assembleDebug

# Copy output artifact to workspace root
cp app/build/outputs/apk/debug/app-debug.apk ./PathoFlow.apk
```

---

## 🌐 Self-Hosted OTA Server Configuration
PathoFlow checks for updates by fetching a simple `version.json` file hosted on your server or GitHub Pages:

### version.json Schema
```json
{
  "versionCode": 5,
  "versionName": "1.3.1",
  "apkUrl": "https://hastjosh1.github.io/pathoflow/PathoFlow.apk"
}
```
Simply edit this JSON, commit, and push it to your server! PathoFlow will notify all clinicians running older builds instantly.
