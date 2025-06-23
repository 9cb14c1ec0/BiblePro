# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

BiblePro is a cross-platform Bible application built with Kotlin Multiplatform Compose. It features multiple Bible translations, Strong's concordance integration, search functionality, reading tracking, notes system, and dark mode support.

## Build and Development Commands

### Building the Application
```bash
./gradlew build
```

### Running the Application
```bash
./gradlew run
```

### Packaging for Distribution
```bash
./gradlew packageDistributionForCurrentOS
```

### Creating Native Distributions
```bash
./gradlew createDistributable
```

### Android Development
```bash
# Build Android APK
./gradlew assembleDebug

# Build Android release APK
./gradlew assembleRelease

# Copy resources to Android assets (runs automatically during build)
./gradlew copyResourcesToAndroidAssets
```

## Architecture

### MVVM Pattern
The application follows an MVVM (Model-View-ViewModel) architecture:
- **ViewModels**: Located in `src/commonMain/kotlin/viewmodels/` - handle business logic and state management using StateFlow
- **Views**: Compose UI components in `src/commonMain/kotlin/` directory and `src/desktopMain/kotlin/` for desktop-specific components
- **Models**: Bible data structures in `src/commonMain/kotlin/bibles/`

### Key Components

#### Main Application Structure
- `src/desktopMain/kotlin/Main.kt`: Desktop entry point that launches the application window
- `src/commonMain/kotlin/App.kt`: Main app component with multi-pane layout system supporting Bible, search, and notes panes
- `src/commonMain/kotlin/BiblePane.kt`: Main Bible reading interface with translation selection and verse display
- `src/commonMain/kotlin/SearchPane.kt`: Search functionality across Bible translations
- `src/commonMain/kotlin/GlobalNotesView.kt`: Notes management system

#### Data Layer
- `bibles/IBible.kt`: Bible data structures (Bible, Testament, Book, Chapter, Verse)
- `bibles/BibleList.kt`: Available Bible translations and XML parser
- `bibles/Lexicon.kt`: Strong's concordance integration
- Bible XML files stored in `src/commonMain/resources/` and copied to `src/androidMain/assets/` for Android

#### ViewModels
- `BibleViewModel.kt`: Bible loading, book/chapter selection, verse data
- `SearchViewModel.kt`: Search functionality and results
- `GlobalNotesViewModel.kt`: Notes creation and management
- `VerseViewModel.kt`: Individual verse state and interactions

#### Localization
- `locale/locale.kt`: Main localization system with `L.current.l()` function
- Language files in `locale/` directory (french.kt, german.kt, etc.)
- `locale/BibleBooksLocalization.kt`: Bible book name translations

#### Features
- **Theme System**: `theme/Theme.kt` with dark/light mode support
- **Phonetics**: `phonetics/` directory for phonetic display of text
- **Reading Tracking**: `ReadingTracker.kt` and `ReadingPlan.kt` for reading progress
- **Cross References**: `CrossReferenceTracker.kt` for Bible cross-references
- **Storage Abstraction**: `storage/PlatformStorage.kt` interface with platform-specific implementations for file operations

### Multi-Pane System
The application uses a dynamic pane system where users can open multiple Bible panes, search panes, and notes panes simultaneously. Each pane type has its own state management and can be closed independently.

### Bible Data Loading
Bible translations are stored as XML files in resources and parsed on-demand. The BibleViewModel caches loaded Bibles to avoid re-parsing. Bible data follows a hierarchical structure: Bible → Testament → Book → Chapter → Verse.

### State Management
Uses Kotlin StateFlow for reactive state management in ViewModels. UI components collect state changes and recompose accordingly. Local state is managed with Compose's remember and mutableStateOf for transient UI state.

### Kotlin Multiplatform Structure
The application is structured as a Kotlin Multiplatform project:
- **commonMain**: Shared code for all platforms including UI, business logic, and data models
- **desktopMain**: Desktop-specific implementations (Main.kt entry point, file storage)
- **androidMain**: Android-specific implementations (MainActivity.kt, Android storage, manifest)

### Resource Loading
Resources (Bible XML files, lexicon data) are loaded differently per platform:
- **Desktop**: Uses `ResourceLoader` to load from JAR resources
- **Android**: Resources are copied from `commonMain/resources` to `androidMain/assets` during build
- Both platforms use the same `ResourceLoader` interface for consistent resource access

## Important Patterns

### Adding New Bible Translations
See `docs/adding_bible_translations.md` for detailed instructions on adding new Bible translations to the application.

### Localization
Use `L.current.l("key")` for all user-facing strings. New language support requires creating a language file in the `locale/` directory following the existing pattern.

### Testing
Test files are located in `src/test/kotlin/` but no specific test framework is currently configured in build.gradle.kts.

### Platform-Specific Development
When adding platform-specific functionality:
- Use the `storage/PlatformStorage.kt` interface for file operations
- Implement platform-specific versions in `desktopMain` and `androidMain`
- Use `PlatformStorageFactory` to get the appropriate implementation
- Keep shared logic in `commonMain` whenever possible

### Resource Management
- Bible translations and lexicon data are loaded lazily and cached
- Use `BibleXmlParser().parseFromResource()` to load Bible translations
- Bible caching is handled in `BibleViewModel` to avoid re-parsing
- All user-facing strings must use the localization system with `L.current.l("key")`