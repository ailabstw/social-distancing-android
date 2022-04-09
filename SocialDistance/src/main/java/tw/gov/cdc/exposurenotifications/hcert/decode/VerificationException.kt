package tw.gov.cdc.exposurenotifications.hcert.decode

class VerificationException(
    val error: Error,
    message: String? = null,
    cause: Throwable? = null,
    val details: Map<String, String>? = null
) : Exception(message, cause)
