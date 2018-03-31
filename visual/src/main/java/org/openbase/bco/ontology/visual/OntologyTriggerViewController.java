package org.openbase.bco.ontology.visual;

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

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import org.openbase.bco.ontology.lib.trigger.Trigger;
import org.openbase.bco.ontology.lib.trigger.TriggerFactory;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.pattern.Observable;
import org.openbase.jul.visual.javafx.control.AbstractFXController;
import org.slf4j.LoggerFactory;
import rst.domotic.ontology.TriggerConfigType.TriggerConfig;
import rst.domotic.state.ActivationStateType.ActivationState;
import rst.domotic.state.ActivationStateType.ActivationState.State;

public class OntologyTriggerViewController extends AbstractFXController {

    @FXML
    private Label triggerStateLabel;

    @FXML
    private TextArea triggerDescription;

    @FXML
    private Pane triggerStateColorPane;

    @Override
    public void initContent() {
        try {
            setTriggerState(State.UNKNOWN);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void registerTrigger(final TriggerConfig triggerConfig) {
        try {
            final Trigger trigger = new TriggerFactory().newInstance(triggerConfig);
            trigger.addObserver((Observable<State> source, ActivationState.State data) -> {
                System.out.println(trigger.getTriggerConfig().getLabel() + " is " + data);
                setTriggerState(data);
            });
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory("Could not register trigger!", ex, LoggerFactory.getLogger(getClass()));
        } catch (InterruptedException ex) {
            System.exit(0);
        }
    }

    private void setTriggerState(final ActivationState.State state) {
        switch (state) {
            case ACTIVE:
                triggerStateColorPane.setStyle("-fx-background-color: #178020");
                break;
            case DEACTIVE:
                triggerStateColorPane.setStyle("-fx-background-color: #807666");
                break;
            default:
                triggerStateColorPane.setStyle("-fx-background-color: #b88312");
        }
        triggerStateLabel.setText(state.name());
    }

    @Override
    public void updateDynamicContent() {
        System.out.println("call updateDynamicContent");
    }

    public void setup(final String description, final TriggerConfig triggerConfig) {
        triggerDescription.setText(description);
        registerTrigger(triggerConfig);
    }
}
