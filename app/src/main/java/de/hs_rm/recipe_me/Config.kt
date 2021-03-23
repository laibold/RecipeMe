package de.hs_rm.recipe_me

/**
 * Config object to differentiate between prod and test database.
 * For UI tests set env to Environments.TEST before accessing database
 */
object Config {
    const val DATABASE_PROD = "test_db"
    const val DATABASE_TEST = "android_test_db"

    var env = Environments.PROD

    /**
     * Returns name of database depending on environment (default PROD)
     */
    fun getDatabaseName(): String {
        return if (env == Environments.PROD) {
            DATABASE_PROD
        } else {
            DATABASE_TEST
        }
    }

    enum class Environments {
        PROD, TEST
    }
}
