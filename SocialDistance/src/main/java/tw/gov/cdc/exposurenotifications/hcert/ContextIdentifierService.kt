package tw.gov.cdc.exposurenotifications.hcert


/**
 * Appends/drops the Context identifier prefix from input, e.g. "HC1:"
 */
class ContextIdentifierService(private val prefix: String = "HC1:") {

     fun decode(input: String) = when {
        input.startsWith(prefix) -> input.drop(prefix.length)
        else -> throw VerificationException(
            Error.INVALID_SCHEME_PREFIX,
            "No context prefix '$prefix'",
            details = mapOf("prefix" to prefix)
        )
    }

}
