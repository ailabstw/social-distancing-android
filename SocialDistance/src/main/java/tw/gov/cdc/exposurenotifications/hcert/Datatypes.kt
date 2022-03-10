package tw.gov.cdc.exposurenotifications.hcert


enum class CoseHeaderKeys(val intVal: Int, val stringVal: String) {
    ALGORITHM(1, "alg"),
    CRITICAL_HEADERS(2, "crit"),
    CONTENT_TYPE(3, "content type"),
    KID(4, "kid"),
    IV(5, "IV"),
    PARTIAL_IV(6, "Partial IV"),
    TRUSTLIST_VERSION(42, "tlv"),
    BUSINESS_RULES_VERSION(-65537, "brv"),
    VALUE_SET_VERSION(-65538, "vsv"),
}

enum class CwtHeaderKeys(val intVal: Int, val stringVal: String) {
    ISSUER(1, "iss"),
    SUBJECT(2, "sub"),
    AUDIENCE(3, "aud"),
    EXPIRATION(4, "exp"),
    NOT_BEFORE(5, "nbf"),
    ISSUED_AT(6, "iat"),
    CWT_ID(7, "cti"),
    HCERT(-260, "hcert"),
    EUDGC_IN_HCERT(1, "eu_dgc_v1"),
}


enum class CwtAlgorithm(val intVal: Int, val stringVal: String) {
    ECDSA_256(-7, "ES256"),
    ECDSA_384(-35, "ES384"),
    RSA_PSS_256(-37, "PS256"),
}
