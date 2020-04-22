package education.mahmoud.quranyapp.utils

/*

import org.koin.KoinContext
import org.koin.standalone.StandAloneContext
import kotlin.jvm.internal.Reflection


*/
/**
 * @author @fredy_mederos
 *//*

object KoinJavaUtils {

    */
/**
 * inject lazily given dependency
 *//*

    @JvmOverloads
    @JvmStatic
    fun <T> inject(clazz: Class<T>, name: String = ""): Lazy<T> {
        return lazy { get(clazz, name) }
    }

    */
/**
 * Retrieve given dependency
 *//*

    @JvmOverloads
    @JvmStatic
    fun <T> get(clazz: Class<T>, name: String = ""): T {
        val kclazz = Reflection.getOrCreateKotlinClass(clazz)
        val koinContext = (StandAloneContext.koinContext as KoinContext)

        val beanDefinition = if (name.isBlank())
            koinContext.beanRegistry.searchAll(kclazz)
        else
            koinContext.beanRegistry.searchByName(name)

        return koinContext.resolveInstance(kclazz, { emptyMap() }, { beanDefinition }) as T
    }

    */
/**
 * inject lazily given property
 *//*

    @JvmOverloads
    @JvmStatic
    fun <T> property(key: String, defaultValue: T? = null): Lazy<T?> {
        return lazy { getProperty(key, defaultValue) }
    }

    */
/**
 * Retrieve given property
 *//*

    @Suppress("UNCHECKED_CAST")
    @JvmOverloads
    @JvmStatic
    fun <T> getProperty(key: String, defaultValue: T? = null): T? {
        val koinContext = (StandAloneContext.koinContext as KoinContext)
        return koinContext.propertyResolver.properties[key] as T? ?: defaultValue
    }

}*/
