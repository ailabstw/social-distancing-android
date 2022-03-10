package tw.gov.cdc.exposurenotifications.hcert.data

//This is actually a platform type
interface CborObject{
    fun toJsonString():String
    fun getVersionString():String?
}
