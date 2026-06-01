# 🌊 PathoFlow: Offline Pathology Collection Companion (v1.4.0)

Welcome to **PathoFlow** — a high-performance, single-user clinical companion app designed specifically for phlebotomists and clinical laboratory operators. 

PathoFlow is built to work **100% offline**, allowing you to seamlessly register patients, manage diagnostic directories, check out with payment confirmation, and transmit full clinical records directly to your laboratory's WhatsApp in one unified send flow!

> [!NOTE]
> *"This is the first offline Pathology collection entry app which directly sends all the data to the respective lab's WhatsApp. I made this app for my own clinical purpose. This is all vibe-coded! I am not a developer nor a programmer, just trying vibe coding."* — **hastjosh1**

---

## 🛠️ Modular Screen Architecture (Refactored in v1.4.0)
To maximize clean code, maintainability, and compilation speeds, the giant 3,514-line god-file `AppScreens.kt` has been split into **8 modular, dedicated screen components** under `com.example.ui.screens`:

* 🚪 [SplashScreen.kt](file:///Users/hastjoshi/antigravity/PathoFlow/app/src/main/java/com/example/ui/screens/SplashScreen.kt): Vibrant startup entry with vertical gradients and outlines.
* ⚙️ [OnboardingSetupScreen.kt](file:///Users/hastjoshi/antigravity/PathoFlow/app/src/main/java/com/example/ui/screens/OnboardingSetupScreen.kt): Initial clinical configuration panel equipped with automated settings loading fail-safes.
* 📊 [DashboardScreen.kt](file:///Users/hastjoshi/antigravity/PathoFlow/app/src/main/java/com/example/ui/screens/DashboardScreen.kt): Clinic metrics grid, dynamic app version header, and updates notifier.
* 👤 [PatientEntryScreen.kt](file:///Users/hastjoshi/antigravity/PathoFlow/app/src/main/java/com/example/ui/screens/PatientEntryScreen.kt): Detailed patient collection builder featuring search optimizations and mandatory phone validations.
* 🧪 [TestManagementScreen.kt](file:///Users/hastjoshi/antigravity/PathoFlow/app/src/main/java/com/example/ui/screens/TestManagementScreen.kt): Diagnostic catalog controller with bulk quoted CSV synchronization tools.
* 📈 [ReportsScreen.kt](file:///Users/hastjoshi/antigravity/PathoFlow/app/src/main/java/com/example/ui/screens/ReportsScreen.kt): Dynamic daily clinical collections and estimated/realized revenue trackers.
* 🔧 [SettingsScreen.kt](file:///Users/hastjoshi/antigravity/PathoFlow/app/src/main/java/com/example/ui/screens/SettingsScreen.kt): Central administrative control card and manual OTA updates checker.
* 🧱 [SharedComponents.kt](file:///Users/hastjoshi/antigravity/PathoFlow/app/src/main/java/com/example/ui/screens/SharedComponents.kt): Core custom layouts, rounded card shapes, top bars, and status badges.

---

## 🚀 Key Clinical Upgrades (Auth-Removal & Simplification)
PathoFlow is optimized as a **single-user companion app**, stripping vestigial multi-user modules to simplify field phlebotomy:
* **No Authentication Friction**: Removed redundant admin login prompts, Universal backdoor passcodes (`"1234"`), profile cards, and logout fields from settings.
* **Database Preservation**: Retained identical Room DB schema columns (`users` / `adminPin` tables) to prevent migrations and ensure all existing patients are fully preserved.
* **Automatic Collection Sign-Off**: The `collectedBy` parameter dynamically signs entries with your configured laboratory branding instead of a logged-in user session.

---

## 🎨 Premium Visual Aesthetics
* **Vibrant Medical Gradients**: Splash and onboarding setup screens render clinical vertical gradients.
* **Rounded Card Outlines**: Metric cards, patient list items, and form containers use unified `RoundedCornerShape(16.dp)` shapes with soft borders.
* **Primary CTAs**: Main dashboard triggers (like "New Patient Entry") stand out with primary-to-tertiary gradients.
* **Transparent Badge Indicators**: Action items use transparent badges and soft background circle shapes.

---

## 🧪 Comprehensive Unit Testing
We added a robust automated testing suite using **Robolectric** to cover core diagnostic and tracking logic:
* 🧪 [LabViewModelTest.kt](file:///Users/hastjoshi/antigravity/PathoFlow/app/src/test/java/com/example/LabViewModelTest.kt) validates:
  * Patient ID generation formatting (`ALC-YYYYMMDD-suffix`) and uniqueness under load.
  * Diagnostic search selections, automatic price total calculations, and clear-on-select operations.
  
To run the automated test suite locally:
```bash
./gradlew testDebugUnitTest
```

---

## 📦 Building and Packaging

### Requirements
* Android Studio (Koala or newer)
* Android SDK (API 24 to API 36)
* Jetpack Runtime (JBR compiler Java 11+)

### Compile via Terminal
To build and package the production-ready OTA update APK (`PathoFlow.apk`) in your workspace:
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
  "versionCode": 6,
  "versionName": "1.4.0",
  "apkUrl": "https://hastjosh1.github.io/pathoflow/PathoFlow.apk"
}
```
Simply edit this JSON, commit, and push it to your server! PathoFlow will notify all clinicians running older builds instantly.
