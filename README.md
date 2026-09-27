# Tile Territory

An elegant, local two-player strategy game for Android built entirely with modern **Jetpack Compose** and **Material 3**. Players take turns expanding their connected territory across an 8×8 grid, navigating fixed obstacles, blocking opponents, and competing for board dominance.

---

## 🎮 Game Rules & Gameplay

- **Grid Layout**: 8×8 board with 6 fixed symmetrical stone obstacles.
- **Starting Positions**:
  - **Player 1**: Starts at `(6, 1)`
  - **Player 2**: Starts at `(1, 6)`
- **Expansion Rules**:
  - Players take turns claiming one empty cell per turn.
  - Newly claimed cells must be orthogonally connected (Up, Down, Left, Right) to your existing territory. Diagonal connections are not allowed.
  - Blocked stone cells cannot be claimed.
  - Opponent territory cannot be captured — every claim is permanent.
- **Passing & Game End**:
  - If a player has no legal moves, their turn passes automatically.
  - Two consecutive passes or a fully filled board ends the match.
  - The player controlling the most tiles at the end wins!

---

## ✨ Features

- 🎯 **Local 2-Player Strategy**: Play head-to-head on the same device.
- 🎨 **Multiple Board Themes**: Classic, Dark, and High Contrast palettes.
- 🌙 **System Theme Integration**: Full Light, Dark, and System default mode support.
- 💡 **Helpful Visual Indicators**: Toggleable legal move hints and last move highlighting.
- ↩️ **Undo & Restart**: Revert moves or restart matches on demand.
- 🔊 **Audio & Haptics**: Procedurally generated sound effects and tactile feedback.
- 📊 **Statistics Tracking**: Persists games played, win counts, draws, and highest territory score using Jetpack DataStore.
- 📱 **Adaptive Layouts**: Responsive UI designed for both portrait and landscape screen orientations.
- ♿ **Accessibility Ready**: Built with Compose Semantics and LiveRegions for screen readers.

---

## 🛠️ Tech Stack & Architecture

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with **Material 3**
- **Navigation**: [Jetpack Navigation Compose](https://developer.android.com/guide/navigation/navigation-compose)
- **Architecture**: Unidirectional Data Flow (UDF) with `ViewModel` and `StateFlow`
- **Persistence**: [Jetpack DataStore Preferences](https://developer.android.com/topic/libraries/architecture/datastore)
- **Audio**: Custom Android `AudioTrack` procedural synthesizer
- **Target SDK**: Android 16 (API 37) | **Min SDK**: Android 8.0 (API 26)

---

## 📁 Project Structure

```text
TileTerritory/
├── app/
│   └── src/
│       ├── main/
│       │   └── java/com/example/tileterritory/
│       │       ├── audio/            # Audio synthesis & sound playback
│       │       ├── data/             # Repository & DataStore preferences
│       │       ├── game/             # Pure game models & TileTerritory rules engine
│       │       ├── navigation/       # Compose Navigation graph
│       │       └── ui/               # ViewModels, Composables, Screens, and Themes
│       │           ├── components/   # Reusable UI components (Board, Buttons, etc.)
│       │           ├── screens/      # Game, Home, Settings, How to Play, About, Results
│       │           └── theme/        # Material 3 colors, typography, & board palettes
│       └── test/                     # Unit tests for rules & game session logic
```

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio**: Ladybug / 2024.2.1 or newer recommended
- **JDK**: Java 11 or higher
- **Android SDK**: API level 37 (Compile SDK)

### Building and Running

1. **Clone the repository**:
   ```bash
   git clone https://github.com/abdullah-ateeq/Tile-Territory.git
   cd Tile-Territory
   ```

2. **Build the project**:
   ```bash
   ./gradlew assembleDebug
   ```

3. **Run Unit Tests**:
   ```bash
   ./gradlew test
   ```

---

## 🧪 Testing

The pure game logic (`TileTerritoryRules`) and match state transitions (`GameSession`) are fully covered by unit tests. You can run them using:

```bash
./gradlew testDebugUnitTest
```

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.
