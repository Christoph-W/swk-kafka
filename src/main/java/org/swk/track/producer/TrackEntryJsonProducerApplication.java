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
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * Exercise: Producer
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

        // TODO define class for payload
        // try (final KafkaProducer<..., ...> producer = new KafkaProducer<>(getProducerConfig())) {
            long count = 0;
        //     for (final ... trackEntry : readAllTracks()) {
                // TODO send payload to topic
                count++;
        //     }
            LOG.info("Sent {} track entries", count);
        // }
    }

    private static Properties getProducerConfig() {
        final Properties props = new Properties();
        // TODO check bootstrap server config
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        // TODO add key and value serializer
        // props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ....class);
        // props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ....class);
        return props;
    }

    // TODO read payload via Jackson from json file
    // private static ...[] readAllTracks() throws IOException {
    //     final ObjectMapper mapper = new ObjectMapper();
    //     try (final InputStream inputStream = TrackEntryJsonProducerApplication.class.getResourceAsStream("/all_tracks.json")) {
    //         return mapper.readValue(inputStream, ...[].class);
    //     }
    // }

    private static void delayRelative(final long offsetMillis, final long messageTime) throws InterruptedException {
        final long currentTime = System.currentTimeMillis() - offsetMillis;
        final long gpsTimeMillis = TimeUnit.SECONDS.toMillis(messageTime);
        if (currentTime < gpsTimeMillis) {
            Thread.sleep(gpsTimeMillis - currentTime);
        }
    }
}
