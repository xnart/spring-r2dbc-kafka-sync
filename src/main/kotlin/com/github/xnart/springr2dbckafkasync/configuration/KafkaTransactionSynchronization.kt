package com.github.xnart.springr2dbckafkasync.configuration

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.kafka.core.KafkaResourceHolder
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.core.ProducerFactoryUtils.DEFAULT_CLOSE_TIMEOUT
import org.springframework.transaction.reactive.ReactiveResourceSynchronization
import org.springframework.transaction.reactive.TransactionSynchronizationManager
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Suppress("UNCHECKED_CAST")
suspend fun <K, V> getKafkaSenderResourceHolder(senderFactory: ProducerFactory<K, V>, txIdPrefix: String): KafkaResourceHolder<K, V> {
    val currTx = TransactionSynchronizationManager.forCurrentTransaction().awaitSingle()

    var resourceHolder = currTx.getResource(senderFactory) as KafkaResourceHolder<K, V>?
    if (resourceHolder == null) {
        val sender = senderFactory.createProducer(txIdPrefix)
        runCatching {
            sender.beginTransaction()
        }.onFailure {
            sender.close(DEFAULT_CLOSE_TIMEOUT)
            throw it
        }

        resourceHolder = KafkaResourceHolder(sender, DEFAULT_CLOSE_TIMEOUT)
        currTx.bindResource(senderFactory, resourceHolder)
        resourceHolder.isSynchronizedWithTransaction = true
        if (currTx.isSynchronizationActive) {
            currTx.registerSynchronization(ReactiveKafkaResourceSynchronization(resourceHolder, senderFactory, currTx))
        }
    }

    return resourceHolder
}

class ReactiveKafkaResourceSynchronization<K, V>(
    private val resourceHolder: KafkaResourceHolder<K, V>,
    resourceKey: ProducerFactory<K, V>,
    synchronizationManager: TransactionSynchronizationManager
) : ReactiveResourceSynchronization<KafkaResourceHolder<K, V>, ProducerFactory<K, V>>(resourceHolder, resourceKey, synchronizationManager) {

    override fun shouldReleaseBeforeCompletion(): Boolean {
        return false
    }

    override fun releaseResource(resourceHolder: KafkaResourceHolder<K, V>, resourceKey: ProducerFactory<K, V>): Mono<Void> {
        return Mono.fromRunnable { resourceHolder.close() }
    }

    override fun processResourceAfterCommit(resourceHolder: KafkaResourceHolder<K, V>): Mono<Void> {
        return Mono.fromRunnable { resourceHolder.commit() }
    }

    override fun afterCompletion(status: Int): Mono<Void> {
        return when (status) {
            STATUS_COMMITTED -> resourceHolder.commit()
            else -> resourceHolder.rollback()
        }.toMono().then(super.afterCompletion(status))
    }
}
