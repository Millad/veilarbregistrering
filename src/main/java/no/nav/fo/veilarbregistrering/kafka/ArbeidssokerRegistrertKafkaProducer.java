package no.nav.fo.veilarbregistrering.kafka;

import no.nav.arbeid.soker.registrering.ArbeidssokerRegistrertEvent;
import no.nav.fo.veilarbregistrering.besvarelse.DinSituasjonSvar;
import no.nav.fo.veilarbregistrering.bruker.AktorId;
import no.nav.fo.veilarbregistrering.registrering.bruker.ArbeidssokerRegistrertProducer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static no.nav.fo.veilarbregistrering.kafka.ArbeidssokerRegistrertMapper.map;
import static no.nav.log.MDCConstants.MDC_CALL_ID;

class ArbeidssokerRegistrertKafkaProducer implements ArbeidssokerRegistrertProducer {

    private static final Logger LOG = LoggerFactory.getLogger(ArbeidssokerRegistrertKafkaProducer.class);

    private final KafkaProducer producer;
    private final String topic;

    ArbeidssokerRegistrertKafkaProducer(KafkaProducer kafkaProducer, String topic) {
        this.producer = kafkaProducer;
        this.topic = topic;
    }

    @Override
    public void publiserArbeidssokerRegistrert(
            AktorId aktorId,
            DinSituasjonSvar brukersSituasjon,
            LocalDateTime opprettetDato) {

        try {
            ArbeidssokerRegistrertEvent arbeidssokerRegistrertEvent = map(aktorId, brukersSituasjon, opprettetDato);
            ProducerRecord<String, ArbeidssokerRegistrertEvent> record = new ProducerRecord<>(topic, aktorId.asString(), arbeidssokerRegistrertEvent);
            record.headers().add(new RecordHeader(MDC_CALL_ID, MDC.get(MDC_CALL_ID).getBytes(StandardCharsets.UTF_8)));
            producer.send(record).get(2, TimeUnit.SECONDS);
            LOG.info("Arbeidssoker registrert-event publisert på topic, {}", topic);

        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            LOG.warn("Sending av arbeidssokerRegistrertEvent til Kafka feilet", e);

        } catch (Exception e) {
            LOG.error("Sending av arbeidssokerRegistrertEvent til Kafka feilet", e);
        }
    }
}
