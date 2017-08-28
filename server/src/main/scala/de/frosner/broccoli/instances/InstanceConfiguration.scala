package de.frosner.broccoli.instances

import de.frosner.broccoli.instances.storage.StorageConfiguration
import de.frosner.broccoli.models.ParameterType

/**
  * Instance Configuration
  *
  * @param parameters Configuration for parameters
  * @param storage Configuration specific to the instance storage type
  */
final case class InstanceConfiguration(parameters: InstanceConfiguration.Parameters, storage: StorageConfiguration)

object InstanceConfiguration {
  final case class Parameters(defaultType: ParameterType)
}
