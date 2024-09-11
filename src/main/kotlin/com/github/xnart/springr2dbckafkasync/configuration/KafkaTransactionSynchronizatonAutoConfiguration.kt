package com.github.xnart.springr2dbckafkasync.configuration

import com.github.xnart.springr2dbckafkasync.util.KafkaSender
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaTemplate

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@AutoConfigureAfter(KafkaAutoConfiguration::class)
class KafkaTransactionSynchronizatonAutoConfiguration {

    @Value("\${spring.kafka.producer.reactive.transaction-id-prefix}")
    private lateinit var txIdPrefix: String

    @Bean
    @ConditionalOnBean(KafkaTemplate::class)
    fun <K, V> kafkaSender(template: KafkaTemplate<K, V>) = KafkaSender(txIdPrefix, template)
}
