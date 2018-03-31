package org.openbase.bco.ontology.lib.view;

/*-
 * #%L
 * BCO Ontology Visual
 * %%
 * Copyright (C) 2016 - 2018 openbase.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import org.openbase.jul.visual.javafx.launch.AbstractFXMLApplication;

public class OntologyTestAppLoader extends AbstractFXMLApplication {

    public OntologyTestAppLoader() {
        super();
    }

    @Override
    protected String getDefaultCSS() {
        return "/styles/main-style.css";
    }

    @Override
    protected String getDefaultFXML() {
        return "/src/main/resources/org/openbase/bco/ontology/lib/view/OntologyTriggerMainViewController.fxml";
    }
//    protected String getDefaultFXML() {
//        return "/org/openbase/jul/visual/javafx/geometry/svg/SVGLogoPane.fxml";
//    }

    @Override
    protected void registerProperties() {

    }
}