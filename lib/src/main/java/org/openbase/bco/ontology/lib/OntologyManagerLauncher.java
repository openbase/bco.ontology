package org.openbase.bco.ontology.lib;

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

import org.openbase.bco.ontology.lib.jp.JPOntologyDBURL;
import org.openbase.bco.ontology.lib.jp.JPOntologyMode;
import org.openbase.bco.ontology.lib.jp.JPOntologyPingURL;
import org.openbase.bco.ontology.lib.jp.JPOntologyRSBScope;
import org.openbase.bco.ontology.lib.jp.JPOntologyURL;
import org.openbase.bco.authentication.lib.BCO;
import org.openbase.jul.pattern.launch.AbstractLauncher;
import org.openbase.jps.core.JPService;
import org.openbase.jps.preset.JPDebugMode;
import org.openbase.jul.exception.InstantiationException;


/**
 * @author agatting on 10.01.17.
 */
public class OntologyManagerLauncher extends AbstractLauncher<OntologyManagerController> {

    /**
     * Constructor for OntologyManagerLauncher.
     *
     * @throws InstantiationException InstantiationException.
     */
    public OntologyManagerLauncher() throws InstantiationException {
        super(OntologyManager.class, OntologyManagerController.class);
    }

    /**
     * JP Environment.
     */
    @Override
    protected void loadProperties() {
        JPService.registerProperty(JPOntologyURL.class);
        JPService.registerProperty(JPOntologyDBURL.class);
        JPService.registerProperty(JPOntologyPingURL.class);
        JPService.registerProperty(JPOntologyRSBScope.class);
        JPService.registerProperty(JPOntologyMode.class);
        JPService.registerProperty(JPDebugMode.class);
    }

    /**
     * Main Method starting ontology application.
     *
     * @param args Arguments from commandline.
     * @throws Throwable Throwable.
     */
    public static void main(final String... args) throws Throwable {
        BCO.printLogo();
        main(BCO.class, OntologyManager.class, args, OntologyManagerLauncher.class);
    }

}
