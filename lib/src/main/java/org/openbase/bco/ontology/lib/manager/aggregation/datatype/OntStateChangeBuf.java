package org.openbase.bco.ontology.lib.manager.aggregation.datatype;

/*-
 * #%L
 * BCO Ontology Library
 * %%
 * Copyright (C) 2016 - 2018 openbase.org
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

import org.apache.jena.rdf.model.RDFNode;

import java.util.List;

/**
 * @author agatting on 25.03.17.
 */
public class OntStateChangeBuf {

    private final List<RDFNode> stateValues;
    private final String timestamp;

    /**
     * Method creates a ontology stateChange data type, which describes the state values to a specific time.
     *
     * @param stateValues are the values of an state change to a specific time.
     * @param timestamp is the specific time in format: yyyy-MM-dd'T'HH:mm:ss.SSSXXX.
     */
    public OntStateChangeBuf(final List<RDFNode> stateValues, final String timestamp) {
        this.stateValues = stateValues;
        this.timestamp = timestamp;
    }

    /**
     * Getter for observation data: stateValues.
     *
     * @return the state values.
     */
    public List<RDFNode> getStateValues() {
        return stateValues;
    }

    /**
     * Getter for observation data: timestamp.
     *
     * @return the timestamp in format: yyyy-MM-dd'T'HH:mm:ss.SSSXXX
     */
    public String getTimestamp() {
        return timestamp;
    }

}
