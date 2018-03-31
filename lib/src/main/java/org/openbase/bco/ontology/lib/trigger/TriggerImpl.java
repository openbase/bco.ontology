package org.openbase.bco.ontology.lib.trigger;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openbase.bco.ontology.lib.commun.trigger.OntologyRemote;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.pattern.Observable;
import org.openbase.jul.pattern.ObservableImpl;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.pattern.Remote.ConnectionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import rst.domotic.ontology.OntologyChangeType.OntologyChange;
import rst.domotic.ontology.TriggerConfigType.TriggerConfig;
import rst.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;
import rst.domotic.state.ActivationStateType.ActivationState;
import rst.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;

/**
 * @author agatting on 21.12.16.
 */
public class TriggerImpl implements Trigger {

    private static final List<OntologyChange.Category> UNKNOWN_CHANGE = new ArrayList<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(TriggerImpl.class);

    static {
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(OntologyChange.getDefaultInstance()));
    }

    static {
        UNKNOWN_CHANGE.add(OntologyChange.Category.UNKNOWN);
    }

    private final ObservableImpl<ActivationState.State> activationObservable;
    private boolean active;
    private TriggerConfig triggerConfig;
    private final OntologyRemote ontologyRemote;
    private final Observer<ConnectionState> connectionObserver;
    private final Observer<OntologyChange> ontologyObserver;
    private ConnectionState hasConnection;
    private final OntologyChange.Category category;

    /**
     * Constructor for TriggerImpl.
     *
     * @param ontologyRemote ontologyRemote
     */
    public TriggerImpl(final OntologyRemote ontologyRemote) {
        this.category = OntologyChange.Category.UNKNOWN; //TODO
        this.hasConnection = ConnectionState.UNKNOWN;
        this.ontologyRemote = ontologyRemote;
        this.activationObservable = new ObservableImpl<>(true, this);

        final OntologyChange ontologyChange = OntologyChange.newBuilder().addCategory(category).build();

        this.connectionObserver = (source, data) -> {
            switch (data) {
                case CONNECTED:
                    hasConnection = ConnectionState.CONNECTED;
                    notifyOntologyChange(ontologyChange);
                    break;
                case DISCONNECTED:
                    hasConnection = ConnectionState.DISCONNECTED;
                    activationObservable.notifyObservers(ActivationState.State.UNKNOWN);
                case UNKNOWN:
                    hasConnection = ConnectionState.UNKNOWN;
                    activationObservable.notifyObservers(ActivationState.State.UNKNOWN);
                default:
            }
        };
        this.ontologyObserver = (Observable<OntologyChange> source, OntologyChange data) -> {
            if (!hasConnection.equals(ConnectionState.DISCONNECTED)) {
//                if (Measurement.measurementWatch.isRunning()) {
//                    Measurement.measurementWatch.stop();
//                    Measurement.triggerImplFromRSB.add(Measurement.measurementWatch.getTime());
//                    Measurement.measurementWatch.restart();
//                }
                notifyOntologyChange(data);
            }
        };
    }

    @Override
    public void addObserver(final Observer<ActivationState.State> observer) {
        activationObservable.addObserver(observer);
    }

    @Override
    public void removeObserver(final Observer<ActivationState.State> observer) {
        activationObservable.removeObserver(observer);
    }

    @Override
    public void init(final TriggerConfig triggerConfig) throws InitializationException, InterruptedException {
        try {
            this.triggerConfig = triggerConfig;
            activationObservable.notifyObservers(ActivationState.State.UNKNOWN);
        } catch (CouldNotPerformException ex) {
            throw new InitializationException(this, ex);
        }
    }

    @Override
    public void activate() throws CouldNotPerformException, InterruptedException {
        active = true;
        ontologyRemote.addConnectionStateObserver(connectionObserver);
        ontologyRemote.addOntologyObserver(ontologyObserver);
    }

    @Override
    public void deactivate() throws CouldNotPerformException, InterruptedException {
        active = false;
        ontologyRemote.removeConnectionStateObserver(connectionObserver);
        ontologyRemote.removeOntologyObserver(ontologyObserver);
    }

    private void notifyOntologyChange(final OntologyChange ontologyChange) throws CouldNotPerformException {
        if (isRelatedChange(ontologyChange)) {
            try {
                if (ontologyRemote.match(triggerConfig.getQuery())) {
                    activationObservable.notifyObservers(ActivationState.State.ACTIVE);
                } else {
                    activationObservable.notifyObservers(ActivationState.State.DEACTIVE);
                }
            } catch (IOException ex) {
                ExceptionPrinter.printHistory("Could not send query to server. Waiting of notification from ServerConnection", ex, LOGGER, LogLevel.WARN);
            }
        }
    }

    private boolean isRelatedChange(final OntologyChange ontologyChange) {

        final List<OntologyChange.Category> categories = ontologyChange.getCategoryList();
        final List<UnitType> unitTypes = ontologyChange.getUnitTypeList();
        final List<ServiceType> serviceTypes = ontologyChange.getServiceTypeList();

        for (final OntologyChange.Category category : categories) {
            if (triggerConfig.getDependingOntologyChange().getCategoryList().contains(category)) {
                return true;
            }
        }

        for (final UnitType unitType : unitTypes) {
            if (triggerConfig.getDependingOntologyChange().getUnitTypeList().contains(unitType)) {
                return true;
            }
        }

        for (final ServiceType serviceType : serviceTypes) {
            if (triggerConfig.getDependingOntologyChange().getServiceTypeList().contains(serviceType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Getter for trigger config, which includes unit types, service types and categories.
     *
     * @return the trigger config.
     */
    public TriggerConfig getTriggerConfig() {
        return triggerConfig;
    }

    @Override
    public boolean isActive() {
        return active;
    }
}