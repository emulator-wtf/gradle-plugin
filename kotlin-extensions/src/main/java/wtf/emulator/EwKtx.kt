package wtf.emulator

import com.android.build.api.dsl.ManagedDevices
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.plugins.ExtensionAware
import wtf.emulator.gmd.EwManagedDevice

private const val EW_DEVICES_CONTAINER_KEY = "ewDevicesContainer"

val ManagedDevices.ewDevices: NamedDomainObjectContainer<EwManagedDevice>
  get() {
    val extensionAwareSelf = this as ExtensionAware
    val extraProperties = extensionAwareSelf.extensions.extraProperties
    @Suppress("UNCHECKED_CAST")
    return extraProperties.get(EW_DEVICES_CONTAINER_KEY) as NamedDomainObjectContainer<EwManagedDevice>
  }
