package storage

/**
 * Factory function to create the appropriate PlatformStorage implementation
 * for the current platform. This uses Kotlin Multiplatform's expect/actual mechanism.
 */
expect fun createPlatformStorage(): PlatformStorage