# AGENTS.md

This file provides guidance for agentic coding agents working with the BiblePro Kotlin Multiplatform Compose project.

## Build Commands
- `./gradlew build` - Build the entire project
- `./gradlew run` - Run the desktop application
- `./gradlew assembleDebug` - Build Android debug APK
- `./gradlew packageDistributionForCurrentOS` - Package for current OS distribution
- `./gradlew copyResourcesToAndroidAssets` - Copy resources to Android assets

## Code Style Guidelines
- **Imports**: Group by type (androidx, kotlinx, local packages), alphabetical within groups
- **Naming**: PascalCase for classes/composables, camelCase for functions/variables, SCREAMING_SNAKE_CASE for constants
- **Types**: Use explicit types for public APIs, infer for local variables
- **Composables**: Use `@Composable` annotation, follow Material Design patterns
- **State Management**: Use StateFlow for ViewModels, remember/mutableStateOf for local UI state
- **Error Handling**: Use try-catch for resource loading, null-safe operators for optional data
- **Comments**: KDoc for public APIs, inline comments for complex logic only
- **Architecture**: Follow MVVM pattern with ViewModels in `viewmodels/` package
- **Localization**: Use `L.current.l("key")` for all user-facing strings
- **Resources**: Load Bible XML files via `BibleXmlParser().parseFromResource()`
- **Platform Code**: Use `commonMain` for shared code, platform-specific directories for platform code