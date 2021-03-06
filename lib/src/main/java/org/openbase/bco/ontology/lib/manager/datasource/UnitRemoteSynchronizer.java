package org.openbase.bco.ontology.lib.manager.datasource;

/*-
 * #%L
 * BCO Ontology Library
 * %%
 * Copyright (C) 2016 - 2021 openbase.org
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

import org.openbase.bco.dal.lib.layer.unit.UnitRemote;
import org.openbase.bco.dal.remote.layer.unit.Units;
import org.openbase.bco.ontology.lib.OntologyManagerController;
import org.openbase.bco.ontology.lib.manager.abox.observation.StateObservation;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.pattern.ObservableImpl;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.pattern.provider.DataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openbase.type.domotic.state.EnablingStateType.EnablingState.State;
import org.openbase.type.domotic.unit.UnitConfigType.UnitConfig;
import org.openbase.type.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author agatting on 07.02.17.
 */
public class UnitRemoteSynchronizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnitRemoteSynchronizer.class);

    private static final ObservableImpl<DataProvider<UnitConfig>, UnitConfig> UNIT_REMOTE_OBSERVABLE = new ObservableImpl<>();
    private final List<UnitRemote> loadedUnitRemotes;

    private int successfullyRemotesNum = 0;
    private int failedRemotesNum = 0;
    private int potentialRemotesNum = 0;

    /**
     * Constructor initiates the observers, which are used to create for each unit an observation of their state data.
     */
    public UnitRemoteSynchronizer() {
        this.loadedUnitRemotes = new ArrayList<>();

        final Observer<DataProvider<List<UnitConfig>>, List<UnitConfig>> newUnitConfigObserver = (source, unitConfigs) -> loadUnitRemotes(unitConfigs);
        final Observer<DataProvider<List<UnitConfig>>, List<UnitConfig>> removedUnitConfigObserver = (source, unitConfigs) -> removeUnitRemotes(unitConfigs);
        final Observer<DataProvider<UnitConfig>, UnitConfig> unitRemoteObserver = (source, unitConfig) -> setStateObservation(unitConfig);

        OntologyManagerController.NEW_UNIT_CONFIG_OBSERVABLE.addObserver(newUnitConfigObserver);
        OntologyManagerController.REMOVED_UNIT_CONFIG_OBSERVABLE.addObserver(removedUnitConfigObserver);
        UnitRemoteSynchronizer.UNIT_REMOTE_OBSERVABLE.addObserver(unitRemoteObserver);
    }

    private void loadUnitRemotes(final List<UnitConfig> unitConfigs) throws CouldNotPerformException {
        this.potentialRemotesNum = 0;
        this.successfullyRemotesNum = 0;
        this.failedRemotesNum = 0;

        final List<UnitConfig> unitConfigsBuf = new ArrayList<>();

        for (final UnitConfig unitConfig : unitConfigs) {
            if (unitConfig.getUnitType() != UnitType.DEVICE && unitConfig.getEnablingState().getValue() == State.ENABLED) {
                unitConfigsBuf.add(unitConfig);
                potentialRemotesNum++;
            }
        }

        LOGGER.info("Try to set state observation(s) of " + potentialRemotesNum + " potential unit remote(s).");

        for (final UnitConfig unitConfig : unitConfigsBuf) {
            UNIT_REMOTE_OBSERVABLE.notifyObservers(unitConfig);
        }
    }

    private synchronized void addLoadedUnitRemote(final UnitRemote unitRemote) {
        loadedUnitRemotes.add(unitRemote);
    }

    private synchronized List<UnitRemote> getLoadedUnitRemotes() {
        return loadedUnitRemotes;
    }
    private synchronized void incrementFailedRemotesNum() {
        failedRemotesNum++;
    }

    private synchronized int getFailedRemotesNum() {
        return failedRemotesNum;
    }

    private synchronized void checkDone() {
        if (getSuccessfullyRemotesNum() + getFailedRemotesNum() == potentialRemotesNum) {
            LOGGER.info("UnitRemoteSynchronizer is finished with current unitConfigs! There are " + getFailedRemotesNum() + " failed unitRemotes.");
        }
    }

    private synchronized int getSuccessfullyRemotesNum() {
        return successfullyRemotesNum;
    }

    private synchronized int incrementSuccessfullyRemotesNum(final String unitLabel) {
        successfullyRemotesNum++;
        LOGGER.info(unitLabel + " is loaded. Number " + successfullyRemotesNum + " of " + potentialRemotesNum + " state observations successfully started.");
        return successfullyRemotesNum;
    }

    private void setStateObservation(final UnitConfig unitConfig) throws InterruptedException {
        try {
            final UnitRemote unitRemote = Units.getUnit(unitConfig, true);
            addLoadedUnitRemote(unitRemote);
            identifyUnitRemote(unitRemote);
            incrementSuccessfullyRemotesNum(unitRemote.getLabel());
        } catch (NotAvailableException | InstantiationException ex) {
            incrementFailedRemotesNum();
            LOGGER.warn("Could not get unitRemote of " + unitConfig.getLabel() + ". Dropped.");
        }
        checkDone();
    }

    private void removeUnitRemotes(final List<UnitConfig> unitConfigs) throws NotAvailableException {
        for (final UnitConfig unitConfig : unitConfigs) {
            for (final UnitRemote unitRemote : getLoadedUnitRemotes()) {
                if (unitRemote.getId().equals(unitConfig.getId())) {
                    unitRemote.shutdown();
                }
            }
        }
    }

    private void identifyUnitRemote(final UnitRemote unitRemote) throws InstantiationException, NotAvailableException {

        final UnitType unitType = unitRemote.getUnitType();

        //TODO currently problematic unitTypes...fix in future
        switch (unitType) {
            case AUDIO_SINK:
//                new StateObservation(unitRemote);
                return;
            case AUDIO_SOURCE:
//                new StateObservation(unitRemote);
                return;
            case CONNECTION:
//                new StateObservation(unitRemote);
                return;
            case DEVICE:
//                new StateObservation(unitRemote);
                return;
            case LOCATION:
//                new StateObservation(unitRemote);
                return;
            default:
                new StateObservation(unitRemote);
        }
    }
}
