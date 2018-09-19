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

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.swk.track.data.AreaViolation;
import org.swk.track.data.TrackEntry;
import org.swk.track.data.ValidArea;


/**
 * Example solution for exercise: stream processing
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

        final Serde<Long> accountSerde = Serdes.Long();
        final Serde<TrackEntry> trackEntrySerde = Serdes.serdeFrom(new JsonSerializer<>(), new JsonDeserializer<>(TrackEntry.class));
        final KStream<Long, TrackEntry> trackEntryStream = builder.stream("input-track-entry", Consumed.with(accountSerde, trackEntrySerde));

        final Serde<ValidArea> validAreaSerde = Serdes.serdeFrom(new JsonSerializer<>(), new JsonDeserializer<>(ValidArea.class));
        final GlobalKTable<Long, ValidArea> validAreaTable = builder.globalTable("input-valid-area", Consumed.with(accountSerde, validAreaSerde));

        final Serde<AreaViolation> areaViolationSerde = Serdes.serdeFrom(new JsonSerializer<>(), new JsonDeserializer<>(AreaViolation.class));
        final KStream<Long, AreaViolation> areaViolationStream = trackEntryStream
            .join(validAreaTable, (key, value) -> value.accountId, (trackEntry, validArea) -> {
                if (GPSUtil.distance(trackEntry, validArea) > validArea.maxValidDistance) {
                    return AreaViolation.from(trackEntry);
                }
                return null;
            })
            .filter((k, v) -> v != null);
        areaViolationStream.to("output-area-violation", Produced.with(accountSerde, areaViolationSerde));

        final KafkaStreams kstreams = new KafkaStreams(builder.build(), getStreamsConfig());
        kstreams.cleanUp();
        kstreams.start();
        LOG.info("stream processing started");
        Runtime.getRuntime().addShutdownHook(new Thread(kstreams::close));
    }

    private static Properties getStreamsConfig() {
        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "area-violation-streaming");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1500);
        return props;
    }
}
