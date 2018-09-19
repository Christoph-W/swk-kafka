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
package org.swk.track.consumer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.swk.track.data.TrackEntry;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Example solution for exercise: Consumer
 * <p>
 * Consume track entries from a topic.<br/>
 * Using the same or a different groupID can be used to consume in parallel or to broadcast the topics content.
 */
public class TrackEntryJsonConsumerApplication {

    private static final Logger LOG = LoggerFactory.getLogger(TrackEntryJsonConsumerApplication.class);

    public static void main(final String[] args) {
        final String topic = "tracking_" + System.getProperty("user.name", "");

        try (final KafkaConsumer<Long, TrackEntry> consumer = new KafkaConsumer<>(getConsumerConfig())) {
            long count = 0;
            consumer.subscribe(Collections.singleton(topic));
            while (true) {
                final ConsumerRecords<Long, TrackEntry> poll = consumer.poll(TimeUnit.MINUTES.toMillis(5));
                if (poll.isEmpty()) {
                    break;
                }
                count += poll.count();
            }
            LOG.info("Read {} track entries", count);
            consumer.metrics().entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(MetricName::name)))
                .forEach(e -> LOG.info("{} :{} ({})", e.getValue().metricValue(), e.getKey().name(), e.getKey().description()));
        }
    }

    private static Properties getConsumerConfig() {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, TrackEntryJsonDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, getGroupId());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }

    private static String getGroupId() {
        try {
            return System.getProperty("user.name") + "@" + InetAddress.getLocalHost().getHostName();
        } catch (final UnknownHostException e) {
            LOG.warn(e.getLocalizedMessage());
            return System.getProperty("user.name");
        }
    }
}
