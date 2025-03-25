 Football Statistics Tracking App (Wear OS & Android)

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Wear OS](https://img.shields.io/badge/Wear%20OS-compatible-informational)](https://developer.android.com/training/wearables)
[![Android](https://img.shields.io/badge/Android-compatible-success)](https://developer.android.com/guide)

This project is a dual-app system designed for tracking football statistics. It comprises a Wear OS app for collecting data during a football match and an Android phone app for storing, managing, and displaying this data.

## Table of Contents

-   [Features](#features)
-   [Project Structure](#project-structure)
-   [Getting Started](#getting-started)
    -   [Prerequisites](#prerequisites)
    -   [Installation](#installation)
-   [Usage](#usage)
    -   [Wear OS App](#wear-os-app)
    -   [Android Phone App](#android-phone-app)
-   [Technology Stack](#technology-stack)
-   [Contributing](#contributing)
-   [License](#license)
-   [Contact](#contact)

## Features

-   **Real-time Location Tracking (Wear OS):** The Wear OS app uses the Health Services API and Location Services to track the wearer's movements during a football match.
-   **Data Synchronization:** Location data collected on the Wear OS device is sent to the Android phone app using the Wearable Data Layer API.
-   **Data Storage (Android):** The phone app uses Room Persistence Library to store match and location data in a SQLite database.
-   **Data Management:** The phone app provides a clean, organized system for managing match and location data.
-   **Data Persistence:** Data is stored even if the apps or devices are restarted.
-   **Foreground Service (Wear OS):** The location tracking service runs as a foreground service on the Wear OS device to ensure continuous data collection.
- **Data sending**: The smartwatch app sends the location and the information about the match to the phone app.
- **Database**: The phone app receives the data and saves it into a local database.
- **Notification**: The phone app lets the user know when new data was received.

## Project Structure

The project is divided into two main modules:

-   **`FootballStatistics_App_WearOS`:** The Wear OS app module.
    -   `presentation/`: Contains the code for the foreground service that collects and sends location data.
    - `data/`: Contains the Room entity of the Location data that is saved on the smartwatch and the App Database.
-   **`FootballStatistics_App_Android`:** The Android phone app module.
    -   `data/`: Contains the Room entities, DAOs, database, and repositories for managing stored data.
    - `viewmodel/`: Contains the ViewModels for interacting with the repositories.
    -   `DataListenerService.kt`: The core service that receives data from the Wear OS app.
    -   `FootballStatisticsApplication.kt`: The application class to instance the database.

## Getting Started

### Prerequisites

-   **Android Studio:** Latest version recommended.
-   **Android SDK:** Properly configured with Wear OS and Android platforms.
-   **Wear OS Device or Emulator:** For testing the Wear OS app.
-   **Android Device or Emulator:** For testing the Android phone app.
-   **Google Play Services:** Installed and up to date on both devices.
-  **Dependencies**: The dependencies needed are:
    * `com.google.android.gms:play-services-wearable:18.1.0`
    * `androidx.health:health-services-client:0.4.0-alpha05`
    * Room Persistence Library dependencies

### Installation

1.  **Clone the Repository:**
bash git clone https://github.com/your-username/your-repo-name.git
2.  **Open in Android Studio:** Import the project into Android Studio.
3.  **Sync Gradle:** Ensure that Gradle syncs successfully.
4.  **Build Project:** Build the project to check for any build errors.

## Usage

### Wear OS App

1.  **Install:** Install the `FootballStatistics_App_WearOS` module on your Wear OS device or emulator.
2.  **Start Exercise:** When you start a new football match, open the app and start a new exercise.
3. **Permissions**: Allow the necessary permisions for the app to work.
4.  **Background Tracking:** The app will track location data in the background.
5.  **End Exercise:** End the exercise to stop tracking.

### Android Phone App

1.  **Install:** Install the `FootballStatistics_App_Android` module on your Android phone or emulator.
2. **Permissions**: Allow the necessary permisions for the app to work.
3.  **Data Transfer:** The app will automatically listen for data from the Wear OS app.
4.  **Data Storage:** Received data is stored in the local SQLite database using Room.

## Technology Stack

-   **Kotlin:** Primary programming language.
-   **Android SDK:** For developing the Android phone app.
-   **Wear OS SDK:** For developing the Wear OS app.
-   **Wearable Data Layer API:** For synchronization between devices.
-   **Health Services API:** For exercise and location data collection on Wear OS.
-   **Room Persistence Library:** For local data storage on the Android phone.
-   **Android Architecture Components:** For building robust and testable apps.
- **Location services**: For obtaining location data from the device.
- **Gson**: To change the Match object into a Json and sent it.

## Contributing

Contributions are welcome! If you'd like to contribute to this project, please follow these steps:

1.  **Fork the Repository:** Fork the project to your GitHub account.
2.  **Create a Branch:** Create a new branch for your feature or bug fix.
3.  **Make Changes:** Implement your changes.
4.  **Test:** Ensure your changes are well-tested.
5.  **Submit a Pull Request:** Submit a pull request to the main repository.

## License

This project is licensed under the [Apache License 2.0](LICENSE) - see the `LICENSE` file for details.

## Contact

If you have any questions or suggestions, feel free to reach out:

-   **GitHub:** [https://github.com/fran028](https://github.com/fran028)
