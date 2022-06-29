package dev.d1s.holesync.properties

import dev.d1s.holesync.constant.INDEX_PROPERTY_PREFIX
import org.springframework.boot.context.properties.ConfigurationProperties
import javax.validation.constraints.NotEmpty

@ConfigurationProperties(INDEX_PROPERTY_PREFIX)
data class IndexConfigurationProperties(

    @NotEmpty
    val mapping: Map<String, String>
)