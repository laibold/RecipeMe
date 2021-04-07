package de.hs_rm.recipe_me.model.exception

class InvalidBackupFileException(
    message: String = "Backup file is not valid"
) : Exception(message)
