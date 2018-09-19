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
package org.swk.track.map;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.swk.track.data.TrackEntry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * Service to send track entries to subscribed clients. A subscribing client gives a accountId for whom it wants to see
 * all incoming messages.<br/>
 * Therefore the service reads a topic given by <code>map.source.topic</code>.<br/>
 * If this topic is not given, the service fills a topic <code>app.topic</code> (with default value <b>
 * tracking_&lt;username&gt;</b>) with values from JSON data file. This self filled topic will be the source for reading
 * messages.
 */
public class TrackEntryService {

    private static final Logger LOG = LoggerFactory.getLogger(TrackEntryService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final MultiValuedMap<Long, SubscriptionInfo> accountSubscriptions = new ArrayListValuedHashMap<>();
    private final KafkaTemplate<String, TrackEntry> kafkaTemplate;
    private final String appTopic;
    private final boolean fillTopicFromFile;

    public TrackEntryService(final KafkaTemplate<String, TrackEntry> kafkaTemplate,
                             final String mapSourceTopic,
                             final String appTopic) throws IOException
    {
        this.kafkaTemplate = kafkaTemplate;
        this.appTopic = appTopic;
        fillTopicFromFile = StringUtils.isEmpty(mapSourceTopic);
        if (fillTopicFromFile) {
            final TrackEntry[] trackEntries = readAllTracks();
            new Thread(() -> sendTrackEntriesToTopicDelayed(trackEntries), getClass().getName() + " Producer").start();
        }
    }

    public void addAccountSubscription(final long accountId, final SubscriptionInfo subscriptionInfo) {
        synchronized (accountSubscriptions) {
            accountSubscriptions.put(accountId, subscriptionInfo);
        }
    }

    public void removeAccountSubscription(final WebSocketSession session) {
        synchronized (accountSubscriptions) {
            for (final Iterator<Map.Entry<Long, SubscriptionInfo>> iterator = accountSubscriptions.entries().iterator();
                 iterator.hasNext(); ) {
                final Map.Entry<Long, SubscriptionInfo> entry = iterator.next();
                if (session.equals(entry.getValue().getSession())) {
                    iterator.remove();
                    return;
                }
            }
        }
    }

    private static TrackEntry[] readAllTracks() throws IOException {
        try (final InputStream inputStream = ServerApplication.class.getResourceAsStream("/all_tracks.json")) {
            return MAPPER.readValue(inputStream, TrackEntry[].class);
        }
    }

    private void sendTrackEntriesToTopicDelayed(final TrackEntry[] trackEntries) {
        final long offsetMillis = System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(trackEntries[0].gpsTime);
        for (final TrackEntry trackEntry : trackEntries) {
            final long currentTime = System.currentTimeMillis() - offsetMillis;
            final long gpsTimeMillis = TimeUnit.SECONDS.toMillis(trackEntry.gpsTime);
            if (currentTime < gpsTimeMillis) {
                try {
                    Thread.sleep(gpsTimeMillis - currentTime);
                } catch (final InterruptedException e) {
                    return;
                }
            }
            trackEntry.latitude /= 1000000d;
            trackEntry.longitude /= 1000000d;
            kafkaTemplate.send(appTopic, trackEntry);
        }
    }

    @KafkaListener(topics = {"#{'${map.source.topic}' ?: '${app.topic}'}"})
    public void consumeBySendingToSubscribedClients(@Payload final TrackEntry trackEntry) {
        LOG.debug("Received trackEntry='{}'", trackEntry);
        if (trackEntry.accountId != null) {
            synchronized (accountSubscriptions) {
                for (final Iterator<SubscriptionInfo> iterator = accountSubscriptions.get(trackEntry.accountId).iterator();
                     iterator.hasNext(); ) {
                    final SubscriptionInfo subscription = iterator.next();
                    try {
                        sendToSubscribedClient(trackEntry, subscription.getSession(), subscription.getTimeZoneOffset());
                    } catch (final IllegalStateException | IOException e) {
                        // if #sendMessage fails the client is disconnected
                        LOG.debug("Lost client {} cause of {}", subscription.getSession(), e.getMessage());
                        iterator.remove();
                    }
                }
            }
        }
    }

    private void sendToSubscribedClient(final TrackEntry trackEntry,
                                        final WebSocketSession session,
                                        final int timeZoneOffset) throws IOException
    {
        try {
            final String trackEntryMessage = MAPPER.writeValueAsString(fillTopicFromFile
                ? TrackEntryForRestClient.from(trackEntry, timeZoneOffset)
                : trackEntry);
            LOG.debug("Send to client {}", trackEntryMessage);
            session.sendMessage(new TextMessage(trackEntryMessage));
        } catch (final JsonProcessingException e) {
            LOG.warn("Could not send message to client, payload = {}", trackEntry);
        }
    }
}
