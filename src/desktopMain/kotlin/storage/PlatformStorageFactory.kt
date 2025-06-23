package storage

/**
 * Desktop implementation of the platform storage factory.
 * Returns a DesktopPlatformStorage instance.
 */
actual fun createPlatformStorage(): PlatformStorage {
    return DesktopPlatformStorage()
}