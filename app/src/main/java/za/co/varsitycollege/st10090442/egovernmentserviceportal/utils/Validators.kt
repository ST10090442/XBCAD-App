package za.co.varsitycollege.st10090442.egovernmentserviceportal.utils

object Validators {
    fun isValidSouthAfricanId(idNumber: String): Boolean {
        return idNumber.matches(Regex("^\\d{13}$"))
    }
} 