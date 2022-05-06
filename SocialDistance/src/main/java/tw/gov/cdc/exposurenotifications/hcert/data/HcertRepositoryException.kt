package tw.gov.cdc.exposurenotifications.hcert.data

class HcertRepositoryException(
    val error: HcertRepositoryError,
    message: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause)

enum class HcertRepositoryError {
    LIMIT_REACHED,
    DUPLICATED,
    INVALID_HCERT
}
