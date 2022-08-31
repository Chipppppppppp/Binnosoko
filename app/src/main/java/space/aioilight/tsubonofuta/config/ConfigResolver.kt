package space.aioilight.tsubonofuta.config

import android.content.Context
import android.net.Uri
import com.nonnonstop.tsubonofuta.BuildConfig
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import kotlinx.serialization.serializer
import space.aioilight.tsubonofuta.util.Logger
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.createInstance

class ConfigResolver(private val context: Context) {
    companion object {
        private const val TAG = "Futa-ConfigResolver"
    }

    var mainConfig: MainConfig by ConfigProperty("main_config")
    var internalConfig: InternalConfig by ConfigProperty("internal_config")

    init {
        val mainConfigTmp = mainConfig
        if (mainConfigTmp.lastVersion < BuildConfig.VERSION_CODE) {
            Logger.i(TAG, "Start updating process")
            mainConfigTmp.lastVersion = BuildConfig.VERSION_CODE
            mainConfig = mainConfigTmp
            internalConfig = InternalConfig()
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    class ConfigProperty<T>(path: String) {
        companion object {
            private const val AUTHORITY = "space.aioilight.tsubonofuta.provider"
            private val json = Json {
                encodeDefaults = true
                ignoreUnknownKeys = true
            }
            private val serializersModule = json.serializersModule
        }

        private val uri = Uri.parse("content://$AUTHORITY/$path")
        private var cachedValue: T? = null

        operator fun getValue(thisRef: ConfigResolver, property: KProperty<*>): T {
            cachedValue?.let {
                return it
            }

            @Suppress("UNCHECKED_CAST")
            val newValue = try {
                thisRef.context.contentResolver.openInputStream(uri)?.use { stream ->
                    json.decodeFromStream(
                        serializersModule.serializer(property.returnType) as KSerializer<T>,
                        stream
                    )
                }
            } catch (e: Exception) {
                Logger.w(TAG, e)
                null
            } ?: (property.returnType.classifier as KClass<*>).createInstance() as T

            cachedValue = newValue
            return newValue
        }

        operator fun setValue(thisRef: ConfigResolver, property: KProperty<*>, value: T) {
            cachedValue = value
            try {
                thisRef.context.contentResolver.openOutputStream(uri, "wt")?.use { stream ->
                    json.encodeToStream(
                        serializersModule.serializer(property.returnType),
                        value,
                        stream
                    )
                }
            } catch (e: Exception) {
                Logger.w(TAG, e)
            }
        }
    }
}