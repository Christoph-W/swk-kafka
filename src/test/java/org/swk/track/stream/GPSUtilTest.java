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

import org.junit.Test;
import org.swk.track.data.Positionable;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class GPSUtilTest {

    private static class Position implements Positionable
    {

        public final double latitude;
        public final double longitude;

        public Position(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        public double getLatitude() {
            return latitude;
        }

        @Override
        public double getLongitude() {
            return longitude;
        }
    }

    @Test
    public void testDistance() {
        final Position leipzig = new Position(51.3396, 12.3824);
        final Position bitterfeld = new Position(51.6265, 12.3133);
        final Position markkleeberg = new Position(51.2755,12.3690);
        final Position magdeburg = new Position(52.1277,11.6292);

        final Stream<Position> pos1 = Stream.of(leipzig, bitterfeld, markkleeberg, magdeburg);
        pos1.forEach(p1 -> Stream.of(leipzig, bitterfeld, markkleeberg, magdeburg).forEach(
                p2 -> System.out.println(GPSUtil.distance(p1, p2))
        ));

        assertThat(GPSUtil.distance(leipzig, bitterfeld)).isBetween(32d, 33d);
        assertThat(GPSUtil.distance(leipzig, markkleeberg)).isBetween(7d, 7.5d);
        assertThat(GPSUtil.distance(leipzig, magdeburg)).isBetween(101d, 102d);
        assertThat(GPSUtil.distance(leipzig, magdeburg)).isEqualTo(GPSUtil.distance(magdeburg, leipzig));
    }
}