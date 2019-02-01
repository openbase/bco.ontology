package org.openbase.bco.ontology.lib.trigger;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openbase.bco.ontology.lib.commun.trigger.OntologyRemote;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.pattern.ObservableImpl;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.pattern.controller.Remote;
import org.openbase.type.domotic.state.ConnectionStateType.ConnectionState;
import org.openbase.jul.pattern.provider.DataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import org.openbase.type.domotic.ontology.OntologyChangeType.OntologyChange;
import org.openbase.type.domotic.ontology.TriggerConfigType.TriggerConfig;
import org.openbase.type.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;
import org.openbase.type.domotic.state.ActivationStateType.ActivationState;
import org.openbase.type.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;

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

    private final ObservableImpl<Trigger, ActivationState.State> activationObservable;
    private boolean active;
    private TriggerConfig triggerConfig;
    private final OntologyRemote ontologyRemote;
    private final Observer<Remote, ConnectionState.State> connectionObserver;
    private final Observer<DataProvider<OntologyChange>, OntologyChange> ontologyObserver;
    private ConnectionState.State hasConnection;
    private final OntologyChange.Category category;

    /**
     * Constructor for TriggerImpl.
     *
     * @param ontologyRemote ontologyRemote
     */
    public TriggerImpl(final OntologyRemote ontologyRemote) {
        this.category = OntologyChange.Category.UNKNOWN; //TODO
        this.hasConnection = ConnectionState.State.UNKNOWN;
        this.ontologyRemote = ontologyRemote;
        this.activationObservable = new ObservableImpl<Trigger, ActivationState.State>(true, this);

        final OntologyChange ontologyChange = OntologyChange.newBuilder().addCategory(category).build();

        this.connectionObserver = (source, data) -> {
            switch (data) {
                case CONNECTED:
                    hasConnection = ConnectionState.State.CONNECTED;
                    notifyOntologyChange(ontologyChange);
                    break;
                case DISCONNECTED:
                    hasConnection = ConnectionState.State.DISCONNECTED;
                    activationObservable.notifyObservers(ActivationState.State.UNKNOWN);
                case UNKNOWN:
                    hasConnection = ConnectionState.State.UNKNOWN;
                    activationObservable.notifyObservers(ActivationState.State.UNKNOWN);
                default:
            }
        };
        this.ontologyObserver = (source, data) -> {
            if (!hasConnection.equals(ConnectionState.State.DISCONNECTED)) {
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
    public void addObserver(final Observer<Trigger, ActivationState.State> observer) {
        activationObservable.addObserver(observer);
    }

    @Override
    public void removeObserver(final Observer<Trigger, ActivationState.State> observer) {
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
                    activationObservable.notifyObservers(ActivationState.State.INACTIVE);
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
