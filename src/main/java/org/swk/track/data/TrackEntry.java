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
package org.swk.track.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrackEntry implements Positionable {

    public Long accountId;
    public Long unitId;
    public Long gpsTime;
    public Double latitude;
    public Double longitude;
    public Double speed;
    public Double roadSpeedLimit;
    public String text;

    public Long getAccountId() {
        return accountId;
    }

    @Override
    public double getLatitude() {
        return latitude;
    }

    @Override
    public double getLongitude() {
        return longitude;
    }

    @Override
    public String toString() {
        return "TrackEntry{" +
            "accountId=" + accountId +
            ", unitId=" + unitId +
            ", gpsTime=" + gpsTime +
            ", latitude=" + latitude +
            ", longitude=" + longitude +
            ", speed=" + speed +
            ", roadSpeedLimit=" + roadSpeedLimit +
            ", text=" + text +
            '}';
    }
}
