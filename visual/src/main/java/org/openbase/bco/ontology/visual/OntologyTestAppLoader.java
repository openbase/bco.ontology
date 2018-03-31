package org.openbase.bco.ontology.lib.view;

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
