package platform

/**
 * True when running on a mobile platform (e.g. Android), where portrait
 * screens are too narrow for side-by-side panes.
 * This uses Kotlin Multiplatform's expect/actual mechanism.
 */
expect val isMobilePlatform: Boolean
