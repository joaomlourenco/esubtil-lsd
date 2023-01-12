package lsd.util

object DriverInfo {
    // Driver name
    val DRIVER_NAME = "LSD JDBC Driver"
    val DRIVER_SHORT_NAME = "lsdJDBC"
    val DRIVER_VERSION = "0.0.1"
    val DRIVER_FULL_NAME = "$DRIVER_NAME $DRIVER_VERSION"

    // Driver version
    val MAJOR_VERSION = 0
    val MINOR_VERSION = 0
    val PATCH_VERSION = 1

    // JDBC specification
    val JDBC_VERSION = "4.2"
    val JDBC_MAJOR_VERSION = JDBC_VERSION[0] - '0'
    val JDBC_MINOR_VERSION = JDBC_VERSION[2] - '0'

    // Supported database connection strings
    val LSD_URL = "jdbc:lsd:"

    // PostgreSQL
    val POSTGRES = "postgresql:"
    val POSTGRES_URL = "jdbc:postgresql:"
}