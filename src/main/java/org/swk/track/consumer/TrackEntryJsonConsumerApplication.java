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
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Exercise: Consumer
 * <p>
 * Consume track entries from a topic.<br/>
 * Using the same or a different groupID can be used to consume in parallel or to broadcast the topics content.
 */
public class TrackEntryJsonConsumerApplication {

    private static final Logger LOG = LoggerFactory.getLogger(TrackEntryJsonConsumerApplication.class);

    public static void main(final String[] args) {
        final String topic = "tracking_" + System.getProperty("user.name", "");

        // TODO define class for key and payload
        // try (final KafkaConsumer<..., ...> consumer = new KafkaConsumer<>(getConsumerConfig())) {
        //     consumer.subscribe(Collections.singleton(topic));
            // TODO poll from subscribed topics
        // }
    }

    private static Properties getConsumerConfig() {
        final Properties props = new Properties();
        // TODO check bootstrap server config
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        // TODO add key and value deserializer (the JsonDeserializer needs a target type)
        // props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ....class);
        // props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ....class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, getGroupId());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }

    private static String getGroupId() {
        try {
            return System.getProperty("user.name") + "@" + InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            LOG.warn(e.getLocalizedMessage());
            return System.getProperty("user.name");
        }
    }
}
