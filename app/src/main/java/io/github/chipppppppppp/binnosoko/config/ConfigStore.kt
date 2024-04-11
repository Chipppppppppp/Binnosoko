package io.github.chipppppppppp.binnosoko.config

import androidx.preference.PreferenceDataStore
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.properties.Properties
import kotlinx.serialization.properties.decodeFromMap
import kotlinx.serialization.properties.encodeToMap

class ConfigStore(val config: MutableMap<String, Any>) : PreferenceDataStore() {
    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        inline fun <reified T> from(config: T): ConfigStore {
            return ConfigStore(Properties.encodeToMap(config).toMutableMap())
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    inline fun <reified T> convert(): T {
        return Properties.decodeFromMap(config.toMap())
    }

    override fun putString(key: String, value: String?) {
        if (value == null) {
            config.remove(key)
            return
        }
        config[key] = value
    }

    override fun putStringSet(key: String, values: MutableSet<String>?) {
        if (values == null) {
            config.remove(key)
            return
        }
        config[key] = values
    }

    override fun putInt(key: String, value: Int) {
        config[key] = value
    }

    override fun putLong(key: String, value: Long) {
        config[key] = value
    }

    override fun putFloat(key: String, value: Float) {
        config[key] = value
    }

    override fun putBoolean(key: String, value: Boolean) {
        config[key] = value
    }

    override fun getString(key: String, defValue: String?): String? {
        return config[key] as String? ?: defValue
    }

    override fun getStringSet(key: String, defValues: MutableSet<String>?): MutableSet<String>? {
        @Suppress("UNCHECKED_CAST")
        return config[key] as MutableSet<String>? ?: defValues
    }

    override fun getInt(key: String, defValue: Int): Int {
        return config[key] as Int? ?: defValue
    }

    override fun getLong(key: String, defValue: Long): Long {
        return config[key] as Long? ?: defValue
    }

    override fun getFloat(key: String, defValue: Float): Float {
        return config[key] as Float? ?: defValue
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return config[key] as Boolean? ?: defValue
    }
}