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

import java.util.Properties;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Exercise: stream processing
 * <p>
 * Supplying a topic of area violations consuming 2 topics: the known track entries <code>input-track-entry</code> and a
 * topic of valid areas <code>input-valid-area</code>. If the position of the track entry is outside of the valid area
 * (circle defined by position and max distance) a violation with the content of the track entry is published to topic
 * <code>output-area-violation</code>.
 */
public class AreaViolationStreamProcessingApplication {

    private static final Logger LOG = LoggerFactory.getLogger(AreaViolationStreamProcessingApplication.class);

    public static void main(final String[] args) {
        final StreamsBuilder builder = new StreamsBuilder();

        // TODO define serdes (serializer & deserializer) streams, join, filter
        // builder.stream("input-track-entry", Consumed.with(...)
        // GPSUtil.distance will do the calculation part
        // ending with:
        // resulting_KStream.to("output-area-violation", Produced.with(...));

        final KafkaStreams kstreams = new KafkaStreams(builder.build(), getStreamsConfig());
        kstreams.cleanUp();
        kstreams.start();
        LOG.info("stream processing started");
        Runtime.getRuntime().addShutdownHook(new Thread(kstreams::close));
    }

    private static Properties getStreamsConfig() {
        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "area-violation-streaming");
        // TODO check bootstrap server config
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1500);
        return props;
    }
}
