package org.openbase.bco.ontology.lib.jp;

/*-
 * #%L
 * BCO Ontology Library
 * %%
 * Copyright (C) 2016 - 2019 openbase.org
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

import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jps.preset.AbstractJPString;

/**
 * @author agatting on 01.03.17.
 */
public class JPOntologyPingURL extends AbstractJPString {

    /**
     * Command line argument strings.
     */
    public static final String[] COMMAND_IDENTIFIERS = {"--ontology-server-ping-url"};

    /**
     * Constructor for the JPServerPingURL class.
     */
    public JPOntologyPingURL() {
        super(COMMAND_IDENTIFIERS);
    }

    @Override
    protected String getPropertyDefaultValue() throws JPNotAvailableException {
        return JPService.getProperty(JPOntologyURL.class).getValue() + "/$/ping";
    }

    @Override
    public String getDescription() {
        return "ServerPingURL property is used to set the ping URL of the ontology server, which has to be reached.";
    }
}
