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

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swk.track.data.TrackEntry;

import com.fasterxml.jackson.annotation.JsonInclude;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrackEntryForRestClient {

    private static final Logger LOG = LoggerFactory.getLogger(TrackEntryForRestClient.class);

    public Long unitId;
    public Long gpsTime;
    public Double latitude;
    public Double longitude;
    public String text;

    public static TrackEntryForRestClient from(final TrackEntry sourceEntry, final int timeZoneOffset) {
        final TrackEntryForRestClient trackEntryRest = new TrackEntryForRestClient();
        trackEntryRest.unitId = sourceEntry.unitId;
        trackEntryRest.gpsTime = sourceEntry.gpsTime;
        trackEntryRest.latitude = sourceEntry.latitude;
        trackEntryRest.longitude = sourceEntry.longitude;
        final ZoneId clientZoneId = ZoneId.ofOffset("UTC", ZoneOffset.ofTotalSeconds((int) TimeUnit.MINUTES.toSeconds(-timeZoneOffset)));
        LOG.trace("clientZoneId = {}", clientZoneId);
        trackEntryRest.text = sourceEntry.text != null
            ? sourceEntry.text
            : String.format("Unit: %d\nSpeed: %s\nSent at %3$tF %3$tR",
            sourceEntry.unitId,
            sourceEntry.speed,
            Instant.ofEpochSecond(sourceEntry.gpsTime).atZone(clientZoneId));
        LOG.trace("text = {}", trackEntryRest.text);
        return trackEntryRest;
    }

    @Override
    public String toString() {
        return "TrackEntryForRestClient{" +
            "unitId=" + unitId +
            ", gpsTime=" + gpsTime +
            ", latitude=" + latitude +
            ", longitude=" + longitude +
            ", text=" + text +
            '}';
    }
}
