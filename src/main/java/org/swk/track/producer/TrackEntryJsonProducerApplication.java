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
package org.swk.track.producer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.serialization.LongSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.swk.track.data.TrackEntry;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * Example solution for exercise: Producer
 * <p>
 * Read file <code>src/main/resources/all_tracks.json</code> with array of 4190 track entries. Send these entries to a
 * topic.<br/>
 * The written topic can be shown in the map via <code>./gradlew bootRun -Pargs=--map.source.topic=my_topic</code>.<br/>
 * The source contains latitude and longitude with factor 10^6 without fraction. A consumer expects valid values.<br/>
 * Writing the topic 4K messages at once is no real world scenario. Entries should be fed continuously. Therefore
 * delaying seems necessary.
 */
public class TrackEntryJsonProducerApplication {

    private static final Logger LOG = LoggerFactory.getLogger(TrackEntryJsonProducerApplication.class);

    public static void main(final String[] args) throws IOException {
        final String topic = "tracking_" + System.getProperty("user.name", "");

        try (final KafkaProducer<Long, TrackEntry> producer = new KafkaProducer<>(getProducerConfig())) {
            final TrackEntry[] trackEntries = readAllTracks();
            final long offsetMillis = System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(trackEntries[0].gpsTime);

            long count = 0;
            for (final TrackEntry trackEntry : trackEntries) {
                try {
                    delayRelative(offsetMillis, trackEntry.gpsTime);
                } catch (final InterruptedException e) {
                    break;
                }
                trackEntry.latitude /= 1000000d;
                trackEntry.longitude /= 1000000d;
                producer.send(new ProducerRecord<>(topic, trackEntry.accountId, trackEntry));
                count++;
            }
            LOG.info("Sent {} track entries", count);
            producer.metrics().entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(MetricName::name)))
                .forEach(e -> LOG.info("{} :{} ({})", e.getValue().metricName(), e.getKey().name(), e.getKey().description()));
        }
    }

    private static Properties getProducerConfig() {
        final Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return props;
    }

    private static TrackEntry[] readAllTracks() throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        try (final InputStream inputStream = TrackEntryJsonProducerApplication.class.getResourceAsStream("/all_tracks.json")) {
            return mapper.readValue(inputStream, TrackEntry[].class);
        }
    }

    private static void delayRelative(final long offsetMillis, final long messageTime) throws InterruptedException {
        final long currentTime = System.currentTimeMillis() - offsetMillis;
        final long gpsTimeMillis = TimeUnit.SECONDS.toMillis(messageTime);
        if (currentTime < gpsTimeMillis) {
            Thread.sleep(gpsTimeMillis - currentTime);
        }
    }
}
