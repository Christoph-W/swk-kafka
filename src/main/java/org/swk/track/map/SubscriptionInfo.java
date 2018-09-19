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

import org.springframework.web.socket.WebSocketSession;

public class SubscriptionInfo {

    private final WebSocketSession session;
    private final int timeZoneOffset;

    public SubscriptionInfo(final WebSocketSession session, final int timeZoneOffset) {
        this.session = session;
        this.timeZoneOffset = timeZoneOffset;
    }

    public WebSocketSession getSession() {
        return session;
    }

    public int getTimeZoneOffset() {
        return timeZoneOffset;
    }
}
