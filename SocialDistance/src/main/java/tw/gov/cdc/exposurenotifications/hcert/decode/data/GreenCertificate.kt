package tw.gov.cdc.exposurenotifications.hcert.decode.data

//import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class GreenCertificate(
    @SerialName("ver")
    val schemaVersion: String,

    @SerialName("nam")
    val subject: Person,

    @SerialName("dob")
    val dateOfBirthString: String,

    @SerialName("v")
    val vaccinations: Array<Vaccination?>? = null,

    @SerialName("r")
    val recoveryStatements: Array<RecoveryStatement?>? = null,

    @SerialName("t")
    val tests: Array<Test?>? = null,
) {

    @Transient
    lateinit var rawString: String

    @Transient
    var isExpired = false

    @Transient
    var issuedAtMilliSeconds = 0L
//    /**
//     * For [dateOfBirthString] ("dob"), month and day are optional in eu-dcc-schema 1.2.1,
//     * so we may not be able to get a valid [LocalDate] from it.
//     * Be lenient, i.e. strip a timestamp, if it is included
//     */
//    @Transient
//    val dateOfBirth: LocalDate? = try {
//        LocalDate.parse(dateOfBirthString.substringBefore("T"))
//    } catch (e: Throwable) {
//        null
//    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as GreenCertificate

        if (schemaVersion != other.schemaVersion) return false
        if (subject != other.subject) return false
        if (dateOfBirthString != other.dateOfBirthString) return false
        if (vaccinations != null) {
            if (other.vaccinations == null) return false
            if (!vaccinations.contentEquals(other.vaccinations)) return false
        } else if (other.vaccinations != null) return false
        if (recoveryStatements != null) {
            if (other.recoveryStatements == null) return false
            if (!recoveryStatements.contentEquals(other.recoveryStatements)) return false
        } else if (other.recoveryStatements != null) return false
        if (tests != null) {
            if (other.tests == null) return false
            if (!tests.contentEquals(other.tests)) return false
        } else if (other.tests != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = schemaVersion.hashCode()
        result = 31 * result + subject.hashCode()
        result = 31 * result + dateOfBirthString.hashCode()
        result = 31 * result + (vaccinations?.contentHashCode() ?: 0)
        result = 31 * result + (recoveryStatements?.contentHashCode() ?: 0)
        result = 31 * result + (tests?.contentHashCode() ?: 0)
        return result
    }

}
