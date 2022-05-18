package tw.gov.cdc.exposurenotifications.hcert.decode.data

data class CwtDecodeResult(
    val cborObj: CborObject,
    val expired: Boolean,
    val issuedAtMilliSeconds: Long,
)
