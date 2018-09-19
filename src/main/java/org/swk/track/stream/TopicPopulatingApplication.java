/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.swk.track.stream;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.function.Function;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.swk.track.data.TrackEntry;
import org.swk.track.data.ValidArea;

import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * Read files <code>src/main/resources/all_tracks.json</code> and <code>src/main/resources/all_valid-areas.json</code>
 * to fill topics <code>input-track-entry</code> and <code>input-valid-area</code>.
 */
public class TopicPopulatingApplication {

    private static final Logger LOG = LoggerFactory.getLogger(TopicPopulatingApplication.class);

    public static void main(final String[] args) throws IOException {
        final String trackTopic = "input-track-entry";
        final String areaTopic = "input-valid-area";

        try (final KafkaProducer<Long, Object> producer = new KafkaProducer<>(getProducerConfig())) {
            final TrackEntry[] trackEntries = readJsonFile("/all_tracks.json", TrackEntry[].class);
            for (final TrackEntry trackEntry : trackEntries) {
                trackEntry.latitude /= 1000000d;
                trackEntry.longitude /= 1000000d;
            }
            fillTopic(producer, trackTopic, trackEntries, TrackEntry::getAccountId);
            fillTopic(producer, areaTopic, readJsonFile("/all_valid-areas.json", ValidArea[].class), ValidArea::getAccountId);
        }
    }

    private static Properties getProducerConfig() {
        final Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return props;
    }

    private static <T> void fillTopic(final KafkaProducer<Long, Object> producer,
                                      final String topic,
                                      final T[] entries,
                                      final Function<T, Long> keySupplier)
    {
        long count = 0;
        for (final T trackEntry : entries) {
            producer.send(new ProducerRecord<>(topic, keySupplier.apply(trackEntry), trackEntry));
            count++;
        }
        LOG.info("Sent {} entries to topic {}", count, topic);
    }

    private static <T> T[] readJsonFile(final String filename, final Class<T[]> resultType) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        try (final InputStream inputStream = TopicPopulatingApplication.class.getResourceAsStream(filename)) {
            return mapper.readValue(inputStream, resultType);
        }
    }
}
