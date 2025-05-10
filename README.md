# NotesApp 📒

A beautifully designed, minimalistic notes app built using **Kotlin** with **Firebase** for seamless real-time data storage. This app is perfect for keeping track of your thoughts, ideas, and tasks, all in one place.

---

## ✨ Key Features

* 🔐 **Secure Authentication** - Sign up and sign in with Firebase Auth.
* 📝 **Effortless Note Management** - Create, edit, and delete notes.
* 📅 **Organized Layout** - Simple, clean UI for quick note access.
* 🔄 **Real-time Sync** - Instant updates with Firebase Realtime Database.
* 🚀 **Lightweight and Fast** - Built with efficiency and performance in mind.

---

## 📂 Project Structure

```
NotesApp/
│
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── com/example/notesapp/
│   │       │       ├── MainActivity.kt
│   │       │       ├── RegisterActivity.kt
│   │       │       ├── LoginActivity.kt
│   │       │       ├── HomeActivity.kt
│   │       │       ├── model/
│   │       │       │   └── Note.kt
│   │       │       ├── adapter/
│   │       │       │   └── NotesAdapter.kt
│   │       │       ├── utils/
│   │       │       │   └── FirebaseHelper.kt
│   │       └── res/
│   │           ├── layout/
│   │           ├── values/
│   │           └── drawable/
│   └── build.gradle.kts
│
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## 🚀 Getting Started

1. **Clone the repository:**

   ```bash
   git clone https://github.com/Janani-M11/NotesApp.git
   cd NotesApp
   ```

2. **Open the project in Android Studio.**

3. **Add Firebase Configuration:**

   * Download the `google-services.json` file from your Firebase console.
   * Place it in the `app/` directory.

4. **Build and run the project:**

   ```bash
   ./gradlew clean build
   ```

---

## 💡 How It Works

* **Authentication:** User data is securely managed using Firebase Auth.
* **Database:** Notes are stored and synchronized using Firebase Realtime Database.
* **UI:** Uses a modern, intuitive design for a seamless user experience.

---

## 🎨 Screenshots

### Login Screen
<img src="app/src/main/res/screenshots/notesapplogin.jpg" width="300" height="auto" alt="Login Screen">

### Register Screen
<img src="app/src/main/res/screenshots/notesappregister.jpg" width="300" height="auto" alt="Register Screen">

### Home Screen
<img src="app/src/main/res/screenshots/notesapphome.jpg" width="300" height="auto" alt="Home Screen">

### Notes Management
<img src="app/src/main/res/screenshots/notesappfunctions.jpg" width="300" height="auto" alt="Notes Management">


---

## 📬 Contact

For any queries, feel free to reach out:

* GitHub: [Janani-M11](https://github.com/Janani-M11)
