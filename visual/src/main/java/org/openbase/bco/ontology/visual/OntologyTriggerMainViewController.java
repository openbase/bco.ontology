package org.openbase.bco.ontology.lib.view;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import org.openbase.bco.ontology.lib.trigger.sparql.AskQueryExample.QueryExample;
import org.openbase.jul.visual.javafx.control.AbstractFXController;
import org.openbase.jul.visual.javafx.fxml.FXMLProcessor;
import rst.domotic.ontology.TriggerConfigType.TriggerConfig;

public class OntologyTriggerMainViewController extends AbstractFXController {

    @FXML
    private VBox mainPane;

    @Override
    public void initContent() {
        try {
            for (QueryExample example : QueryExample.values()) {
                Pair<Pane, OntologyTriggerViewController> paneOntologyTriggerViewControllerPair = FXMLProcessor.loadFxmlPaneAndControllerPair("/org/openbase/sandbox/fx/OntologyTriggerViewController.fxml", OntologyTriggerViewController.class, getClass());

                // mockup
//                final OntologyChange ontologyChange = OntologyChange.newBuilder().addCategory(Category.UNKNOWN)
//                        .addUnitType(UnitType.COLORABLE_LIGHT).addServiceType(ServiceType.POWER_STATE_SERVICE).build();
//                .setDependingOntologyChange(ontologyChange)

                final TriggerConfig triggerConfig = TriggerConfig.newBuilder().setLabel("trigger0").setQuery(example.getQuery()).build();

                // setup controller
                paneOntologyTriggerViewControllerPair.getValue().setup(example.getDescription(), triggerConfig);

                // setup pane
                mainPane.getChildren().add(paneOntologyTriggerViewControllerPair.getKey());
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void updateDynamicContent() {
        System.out.println("call updateDynamicContent");
    }
}
