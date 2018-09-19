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

import org.swk.track.data.Positionable;


public abstract class GPSUtil {

    /**
     * Use <i>haversine</i> formula to calculate the great-circle distance between two points.
     *
     * @param position1
     * @param position2
     * @return distance in km
     */
    public static double distance(final Positionable position1, final Positionable position2) {
        final double radius = 6371;
        final double phi1 = Math.toRadians(position1.getLatitude());
        final double phi2 = Math.toRadians(position2.getLatitude());
        final double delta_phi = Math.toRadians(position2.getLatitude() - position1.getLatitude());
        final double delta_lambda = Math.toRadians(position2.getLongitude() - position1.getLongitude());

        final double a = Math.sin(delta_phi / 2) * Math.sin(delta_phi / 2) +
            Math.cos(phi1) * Math.cos(phi2) * Math.sin(delta_lambda / 2) * Math.sin(delta_lambda / 2);
        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return radius * c;
    }
}
