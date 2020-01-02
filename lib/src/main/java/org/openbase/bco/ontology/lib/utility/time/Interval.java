package org.openbase.bco.ontology.lib.utility.time;

/*-
 * #%L
 * BCO Ontology Library
 * %%
 * Copyright (C) 2016 - 2020 openbase.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import org.openbase.jul.exception.NotAvailableException;

/**
 * @author agatting on 23.06.17.
 */
public class Interval {

    private final long startMillis;
    private final long endMillis;

    /**
     * Method creates an object of a time-interval, based on a start and end time.
     *
     * @param startMillis is the start time in milliseconds. Must be less or equal than the end time.
     * @param endMillis is the end time in milliseconds. Must be greater or equal than the start time.
     * @throws NotAvailableException is thrown in case the start time is bigger than the end time.
     */
    public Interval(final long startMillis, final long endMillis) throws NotAvailableException {
        this.startMillis = startMillis;
        this.endMillis = endMillis;

        if (startMillis >= endMillis) {
            throw new NotAvailableException("Start value of the time interval is bigger or equal than end value!");
        }
    }

    /**
     * Getter for start time.
     *
     * @return the start time in milliseconds.
     */
    public long getStartMillis() {
        return startMillis; }

    /**
     * Getter for end time.
     *
     * @return the end time in milliseconds.
     */
    public long getEndMillis() {
        return endMillis; }

    /**
     * Method returns the duration of interval time in milliseconds.
     *
     * @return the duration of the interval time in milliseconds.
     */
    public long getDurationMillis() {
        return endMillis - startMillis;
    }

    /**
     * Method returns the overlap time of this interval and the input interval, if there is an overlap. Otherwise returns null.
     *
     * @param interval is the another interval.
     * @return a interval object, which is the overlap of the intervals. Returns null if there is no overlap.
     * @throws NotAvailableException is thrown in case the input interval is null or the start/end times are invalid.
     */
    public Interval getOverlap(final Interval interval) throws NotAvailableException {

        if (overlaps(interval)) {
            final long newStart = Math.max(startMillis, interval.getStartMillis());
            final long newEnd = Math.min(endMillis, interval.getEndMillis());

            return new Interval(newStart, newEnd);
        }

        return null;
    }

    /**
     * Method compares this interval with the input interval, if there is an overlap.
     *
     * @param interval is the another time interval.
     * @return true if the intervals overlap. Otherwise false.
     * @throws NotAvailableException is thrown in case the input interval is null.
     */
    public boolean overlaps(final Interval interval) throws NotAvailableException {

        if (interval == null) {
            assert false;
            throw new NotAvailableException("Interval is null.");
        }

        return (startMillis < interval.getEndMillis() && interval.getStartMillis() < endMillis);
    }

}
