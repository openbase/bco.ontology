package org.openbase.bco.ontology.lib.utility.rdf;

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

import java.util.List;

/**
 * This class represents a custom data type to save state values with information about the rdf node object type.
 *
 * @author agatting on 26.09.17.
 */
public class RdfNodeObject {
    private final List<String> stateValues;
    private final boolean isLiteral;

    /**
     * Construct an rdf node object, which keeps state value(s) of a state source. Furthermore it describes the type of the node object.
     *
     * @param stateValues are the stateValues saved as string. A state source contains one (e.g. battery level) or multiple (e.g. color hsb) stateValue(s).
     * @param isLiteral describes the kind of this state source (literal or resource) to manage the ontology. A state value based on continuous value(e.g. 0.38)
     *                  should be marked as literal {@code true}. Otherwise as resource {@code false} if the state value based on discrete value (e.g. ON).
     */
    public RdfNodeObject(final List<String> stateValues, final boolean isLiteral) {
        this.stateValues = stateValues;
        this.isLiteral = isLiteral;
    }

    /**
     * Method provides the state value(s) of the state source.
     *
     * @return the state values.
     */
    public List<String> getStateValues() {
        return stateValues;
    }

    /**
     * Method provides the kind of the source.
     *
     * @return {@code true} if the state value(s) based on literal (continuous). Otherwise {@code false} if based on resource (discrete).
     */
    public boolean isLiteral() {
        return isLiteral;
    }
}
