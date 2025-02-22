package com.wire.android.migration.failure

sealed interface UserMigrationStatus {
    val value: Int

    /**
     * No need to migrate the user.
     * user had no local databse to migrate from when updating from scala
     */
    object NoNeed : UserMigrationStatus {
        override val value: Int = 0
    }

    /**
     * User migration has not started yet.
     * it can be that the user is not logged in or the migration has not started yet
     */
    object NotStarted : UserMigrationStatus {
        override val value: Int = 1
    }

    /**
     * User migration has completed.
     */
    object Completed : UserMigrationStatus {
        override val value: Int = 2
    }

    companion object {
        fun fromInt(value: Int): UserMigrationStatus = when (value) {
            0 -> NoNeed
            1 -> NotStarted
            2 -> Completed
            else -> throw IllegalArgumentException("Invalid value for UserMigrationStatus: $value")
        }
    }
}
