package tw.gov.cdc.exposurenotifications.vitalfix

/**
 * Source: https://github.com/kswlee/android-vital-fix
 */

/**
 * object Reflection
 *
 * Utility static class to wrap reflection methods
 */
object Reflection {
    fun getClassMember(clzName: String, fieldName: String, inst: Any? = null): Any? {
        val clz = Class.forName(clzName)
        return getClassMember(clz as Class<Any>, fieldName, inst)
    }

    private fun getClassMember(clz: Class<Any>, fieldName: String, inst: Any? = null): Any? {
        val field = clz.getDeclaredField(fieldName)
        field.isAccessible = true
        return field.get(inst)
    }

    fun setClassMember(clz: Class<Any>, fieldName: String, inst: Any? = null, value: Any? = null) {
        val field = clz.getDeclaredField(fieldName)
        field.isAccessible = true
        field.set(inst, value)
    }
}