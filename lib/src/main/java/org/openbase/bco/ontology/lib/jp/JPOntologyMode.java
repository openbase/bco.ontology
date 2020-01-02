package org.openbase.bco.ontology.lib.jp;

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

import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jps.preset.AbstractJPBoolean;

/**
 * @author agatting on 06.06.17.
 */
public class JPOntologyMode extends AbstractJPBoolean {

    /**
     * Command line argument strings.
     */
    public static final String[] COMMAND_IDENTIFIERS = {"--ontology-mode"};

    /**
     * Constructor for the JPOntologyMode class.
     */
    public JPOntologyMode() {
        super(COMMAND_IDENTIFIERS);
    }

    @Override
    protected Boolean getPropertyDefaultValue() throws JPNotAvailableException {
        return false;
    }

    @Override
    public String getDescription() {
        return "Mode property is used to set the mode, which affects the state value range of the ontology. If bool property is true, state "
                + "values over time are stored in the ontology. Otherwise (DEFAULT false) current state values are stored only.";
    }
}
