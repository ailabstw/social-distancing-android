package tw.gov.cdc.exposurenotifications.hcert.decode.data

//This is actually a platform type
interface CborObject{
    fun toJsonString():String
    fun getVersionString():String?
}
