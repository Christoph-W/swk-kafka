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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TrackEntryWebSocketHandler extends TextWebSocketHandler {

    private static final Logger LOG = LoggerFactory.getLogger(TrackEntryWebSocketHandler.class);
    private static final Pattern START_MESSAGE_PATTERN = Pattern.compile("^start accountId (\\d{3}) timeZoneOffset (-?\\d+)$");

    private final TrackEntryService trackEntryService;

    @Autowired
    public TrackEntryWebSocketHandler(final TrackEntryService trackEntryService) {
        this.trackEntryService = trackEntryService;
    }

    @Override
    public void afterConnectionEstablished(final WebSocketSession session) {
        LOG.debug("Opened new session in instance {}", this);
        LOG.debug("with attributes: {}", session.getAttributes().toString());
    }

    @Override
    public void handleTextMessage(final WebSocketSession session, final TextMessage message) throws Exception {
        final String payload = message.getPayload();
        if (payload != null && !"ping".equals(payload)) {
            final Matcher payloadMatcher = START_MESSAGE_PATTERN.matcher(payload);
            if (payloadMatcher.matches()) {
                connectConsumer(session, Long.parseLong(payloadMatcher.group(1)), Integer.parseInt(payloadMatcher.group(2)));
            }
        }
    }

    private void connectConsumer(final WebSocketSession session, final long accountId, final int timeZoneOffset) {
        trackEntryService.addAccountSubscription(accountId, new SubscriptionInfo(session, timeZoneOffset));
    }

    @Override
    public void handleTransportError(final WebSocketSession session, final Throwable exception) throws Exception {
        trackEntryService.removeAccountSubscription(session);
        session.close(CloseStatus.SERVER_ERROR);
    }
}
