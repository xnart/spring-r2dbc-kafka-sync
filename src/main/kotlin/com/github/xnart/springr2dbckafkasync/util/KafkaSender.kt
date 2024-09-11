package com.github.xnart.springr2dbckafkasync.util

import com.github.xnart.springr2dbckafkasync.configuration.getKafkaSenderResourceHolder
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.springframework.kafka.core.KafkaTemplate
import java.util.concurrent.Future

class KafkaSender<K, V>(
    private val txIdPrefix: String,
    private val kafkaTemplate: KafkaTemplate<K, V>
) {

    suspend fun sendTx(topic: String, key: K? = null, data: V): Future<RecordMetadata> {
        val producer = getKafkaSenderResourceHolder(kafkaTemplate.producerFactory, txIdPrefix).producer
        return producer.send(ProducerRecord(topic, key, data))
    }

    suspend fun sendTx(topic: String, data: V): Future<RecordMetadata> {
        return sendTx(topic, null, data)
    }
}
