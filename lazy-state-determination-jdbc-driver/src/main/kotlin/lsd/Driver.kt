package lsd

import lsd.jdbc.LSDConnection
import lsd.util.DriverInfo
import java.sql.Connection
import java.sql.DriverPropertyInfo
import java.sql.SQLException
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger


class Driver : java.sql.Driver {

    private val logger = Logger.getLogger("lsd.driver")

    init {
        logger.log(Level.FINE, DriverInfo.DRIVER_FULL_NAME)
    }

    override fun connect(url: String?, info: Properties?): Connection? {
        // As per the documentation, connect must return null if the driver realises its url is invalid
        if (!acceptsURL(url!!)) return null

        return LSDConnection(convertURL(url), info!!)
    }

    override fun acceptsURL(url: String?): Boolean {
        if (url!!.startsWith(DriverInfo.LSD_URL + DriverInfo.POSTGRES)) {
            return true
        }

        return false
    }

    override fun getPropertyInfo(url: String?, info: Properties?): Array<DriverPropertyInfo> {
        TODO("Not yet implemented")
    }

    override fun getMajorVersion(): Int {
        return DriverInfo.MAJOR_VERSION
    }

    override fun getMinorVersion(): Int {
        return DriverInfo.MINOR_VERSION
    }

    // To return true, this driver must pass a specific test battery.
    // It is irrelevant
    override fun jdbcCompliant(): Boolean {
        return false
    }

    override fun getParentLogger(): Logger {
        return logger.parent
    }

    fun convertURL(url: String): String {
        if (url.startsWith(DriverInfo.LSD_URL + DriverInfo.POSTGRES)) {
            return url.replace(DriverInfo.LSD_URL + DriverInfo.POSTGRES, DriverInfo.POSTGRES_URL)
        }

        throw SQLException("Unconvertible URL: $url")
    }
}