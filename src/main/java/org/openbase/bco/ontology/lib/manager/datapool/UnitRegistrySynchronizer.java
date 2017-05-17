/**
 * ==================================================================
 *
 * This file is part of org.openbase.bco.ontology.lib.
 *
 * org.openbase.bco.ontology.lib is free software: you can redistribute it and modify
 * it under the terms of the GNU General Public License (Version 3)
 * as published by the Free Software Foundation.
 *
 * org.openbase.bco.ontology.lib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with org.openbase.bco.ontology.lib. If not, see <http://www.gnu.org/licenses/>.
 * ==================================================================
 */
package org.openbase.bco.ontology.lib.manager.datapool;

import javafx.util.Pair;
import org.openbase.bco.ontology.lib.commun.web.OntModelWeb;
import org.openbase.bco.ontology.lib.commun.web.SparqlUpdateWeb;
import org.openbase.bco.ontology.lib.manager.OntologyToolkit;
import org.openbase.bco.ontology.lib.manager.abox.configuration.OntRelationMappingImpl;
import org.openbase.bco.ontology.lib.manager.sparql.RdfTriple;
import org.openbase.bco.ontology.lib.manager.sparql.SparqlUpdateExpression;
import org.openbase.bco.ontology.lib.manager.tbox.OntClassMapping;
import org.openbase.bco.ontology.lib.manager.tbox.OntClassMappingImpl;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.manager.abox.configuration.OntInstanceMapping;
import org.openbase.bco.ontology.lib.manager.abox.configuration.OntInstanceMappingImpl;
import org.openbase.bco.ontology.lib.manager.abox.configuration.OntRelationMapping;
import org.openbase.bco.ontology.lib.manager.buffer.TransactionBuffer;
import org.openbase.bco.registry.remote.Registries;
import org.openbase.bco.registry.unit.remote.UnitRegistryRemote;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.extension.protobuf.IdentifiableMessageMap;
import org.openbase.jul.extension.protobuf.ProtobufListDiff;
import org.openbase.jul.pattern.ObservableImpl;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.schedule.GlobalCachedExecutorService;
import org.openbase.jul.schedule.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.registry.UnitRegistryDataType.UnitRegistryData;
import rst.domotic.unit.UnitConfigType.UnitConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author agatting on 18.01.17.
 */
public class UnitRegistrySynchronizer {

    //TODO set and list...standardize!
    //TODO get notification about rst types if changed...?!

    public static final ObservableImpl<List<UnitConfig>> newUnitConfigObservable = new ObservableImpl<>();
    //TODO second observable for updated unitConfigs?

    private static final Logger LOGGER = LoggerFactory.getLogger(UnitRegistrySynchronizer.class);
    private final ProtobufListDiff<String, UnitConfig, UnitConfig.Builder> registryDiff;

    private IdentifiableMessageMap<String, UnitConfig, UnitConfig.Builder> identifiableNewMessageMapUnitConfig;
    private IdentifiableMessageMap<String, UnitConfig, UnitConfig.Builder> identifiableUpdatedMessageMapUnitConfig;
//    private IdentifiableMessageMap<String, UnitConfig, UnitConfig.Builder> identifiableRemovedMessageMapUnitConfig;

    private final OntInstanceMapping ontInstanceMapping = new OntInstanceMappingImpl();
    private final OntRelationMapping ontRelationMapping = new OntRelationMappingImpl();
    private UnitRegistryRemote unitRegistryRemote;
    private final TransactionBuffer transactionBufferImpl;
    private final Stopwatch stopwatch;
    private final OntClassMapping ontClassMapping;

    /**
     * Constructor for UnitRegistrySynchronizer.
     *
     * @param transactionBuffer transactionBuffer
     * @throws InterruptedException InterruptedException
     * @throws CouldNotPerformException CouldNotPerformException
     * @throws JPServiceException JPServiceException
     */
    public UnitRegistrySynchronizer(final TransactionBuffer transactionBuffer) throws InterruptedException, CouldNotPerformException, JPServiceException {
        this.transactionBufferImpl = transactionBuffer;
        this.registryDiff = new ProtobufListDiff<>();
        this.stopwatch = new Stopwatch();
        this.ontClassMapping = new OntClassMappingImpl();

        // for init get the whole unitConfigList
        final List<UnitConfig> unitConfigList = getUnitConfigList();

        // upload ontModel
        OntModelWeb.addOntModelViaRetry(OntologyToolkit.loadOntModelFromFile(null, null));
        // fill abox initial with whole registry unitConfigs
        aBoxSynchInitUnits(unitConfigList); //TODO

        // start thread to synch tbox and abox changes by observer
        startUpdateObserver();
        LOGGER.info("UnitRegistrySynchronizer started successfully.");
    }

    private List<UnitConfig> getUnitConfigList() throws NotAvailableException, InterruptedException {
        List<UnitConfig> unitConfigList = null;

        while (unitConfigList == null) {
            try {
                unitRegistryRemote = Registries.getUnitRegistry();
                unitRegistryRemote.waitForData(2, TimeUnit.SECONDS);

                unitConfigList = unitRegistryRemote.getUnitConfigs();
            } catch (CouldNotPerformException e) {
                ExceptionPrinter.printHistory("Could not get UnitConfigs. Retry...", e, LOGGER, LogLevel.ERROR);
                stopwatch.waitForStart(OntConfig.SMALL_RETRY_PERIOD_MILLISECONDS);
            }
        }
        return unitConfigList;
    }

    private void startUpdateObserver() {
        final Observer<UnitRegistryData> unitRegistryObserver = (observable, unitRegistryData) -> GlobalCachedExecutorService.submit(() -> {

            final List<UnitConfig> unitConfigs = unitRegistryData.getUnitGroupUnitConfigList();


            registryDiff.diff(unitConfigs);
            identifiableNewMessageMapUnitConfig = registryDiff.getNewMessageMap();
            identifiableUpdatedMessageMapUnitConfig = registryDiff.getUpdatedMessageMap();
//            identifiableRemovedMessageMap = registryDiff.getRemovedMessageMap();

            try {
                if (!identifiableNewMessageMapUnitConfig.isEmpty()) {
                    final List<UnitConfig> unitConfigsBuf = new ArrayList<>(identifiableNewMessageMapUnitConfig.getMessages());
                    aBoxSynchNewUnits(unitConfigsBuf);
                    newUnitConfigObservable.notifyObservers(unitConfigsBuf);
                }

                if (!identifiableUpdatedMessageMapUnitConfig.isEmpty()) {
                    final List<UnitConfig> unitConfigsBuf = new ArrayList<>(identifiableUpdatedMessageMapUnitConfig.getMessages());
                    aBoxSynchUpdateUnits(unitConfigsBuf);
                }

            } catch (InterruptedException | JPServiceException | NotAvailableException | MultiException e) {
                ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
            }
//            if (!identifiableRemovedMessageMap.isEmpty()) {
//                final List<UnitConfig> unitConfigsBuf = new ArrayList<>(identifiableRemovedMessageMap.getMessages());
//                aBoxSynchRemoveUnits(unitConfigsBuf);
//            }
        });

        this.unitRegistryRemote.addDataObserver(unitRegistryObserver);
    }

    private void aBoxSynchInitUnits(final List<UnitConfig> unitConfigs) throws InstantiationException {
        try {
            final List<RdfTriple> insertTriples = new ArrayList<>();

            //TODO insert all config first...

            //TODO delete first
            // insert tbox
            insertTriples.addAll(ontClassMapping.getUnitTypeClasses());
            // insert instances
            insertTriples.addAll(ontInstanceMapping.getInsertConfigInstances(unitConfigs));
            insertTriples.addAll(ontInstanceMapping.getInsertStateAndServiceInstances());
            // insert relations
            insertTriples.addAll(ontRelationMapping.getInsertConfigRelations(unitConfigs));
            insertTriples.addAll(ontRelationMapping.getInsertStateRelations(null));

            // convert to sparql expression and upload...or save in buffer, if no server connection
            convertToSparqlExprAndUpload(null, insertTriples);
        } catch (JPServiceException | IllegalArgumentException e) {
            throw new InstantiationException(this, e);
        }
    }

    private void aBoxSynchUpdateUnits(final List<UnitConfig> unitConfigs) throws InterruptedException, JPServiceException, NotAvailableException {

        final List<RdfTriple> deleteTriples = new ArrayList<>();
        final List<RdfTriple> insertTriples = new ArrayList<>();

        // delete unit and states instances
        deleteTriples.addAll(ontInstanceMapping.getDeleteUnitInstances(unitConfigs));
        // delete providerService instances
        //TODO delete providerService (?)
        // delete unit properties
        deleteTriples.addAll(ontRelationMapping.getDeleteUnitRelations(unitConfigs));

        // insert tbox changes
        insertTriples.addAll(ontClassMapping.getUnitTypeClasses(unitConfigs));
        // insert instances
        insertTriples.addAll(ontInstanceMapping.getInsertConfigInstances(unitConfigs));
        // insert properties
        insertTriples.addAll(ontRelationMapping.getInsertConfigRelations(unitConfigs));

        // convert to sparql expression and upload...or save in buffer, if no server connection
        convertToSparqlExprAndUpload(deleteTriples, insertTriples);
    }

    private void aBoxSynchNewUnits(final List<UnitConfig> unitConfigs) throws InterruptedException, JPServiceException {

        final List<RdfTriple> triples = new ArrayList<>();

        // insert tbox changes
        triples.addAll(ontClassMapping.getUnitTypeClasses(unitConfigs));
        // insert instances
        triples.addAll(ontInstanceMapping.getInsertConfigInstances(unitConfigs));
        // insert properties
        triples.addAll(ontRelationMapping.getInsertConfigRelations(unitConfigs));

        // convert to sparql expression and upload...or save in buffer, if no server connection
        convertToSparqlExprAndUpload(null, triples);
    }

//    private void aBoxSynchRemoveUnits(final List<UnitConfig> unitConfigList) {
//
//        final List<RdfTriple> tripleArrayLists = new ArrayList<>();
//
//        // delete unit and states instances
//        tripleArrayLists.addAll(ontInstanceMapping.getDeleteTripleOfUnitsAndStates(unitConfigList));
//        // delete providerService instances
//        //TODO delete providerService (?)
//        // delete unit properties
//        tripleArrayLists.addAll(ontRelationMapping.getDeleteUnitRelations(unitConfigList));
//
//        //convert to sparql expression and upload...or save, if no server connection
//        convertToSparqlExprAndUpload(tripleArrayLists, null);
//    }

    private void convertToSparqlExprAndUpload(final List<RdfTriple> deleteTriple, final List<RdfTriple> insertTriple) throws JPServiceException {
        String multiExprUpdate = "";

        try {
            if (deleteTriple == null) {
                // convert triples to single sparql update expression (insert)
                multiExprUpdate = SparqlUpdateExpression.getSparqlUpdateInsertBundleExpr(insertTriple);
            } else if (insertTriple == null) {
                // convert triples to single sparql update expression (delete)
                multiExprUpdate = SparqlUpdateExpression.getSparqlUpdateDeleteAndInsertBundleExpr(deleteTriple, null, null);
            } else {
                // convert triples to single sparql update expression (delete and insert)
                multiExprUpdate = SparqlUpdateExpression.getSparqlUpdateDeleteAndInsertBundleExpr(deleteTriple, insertTriple, null);
            }

            // upload to ontology server
            final boolean isHttpSuccess = SparqlUpdateWeb.sparqlUpdateToAllDataBases(multiExprUpdate, OntConfig.ServerServiceForm.UPDATE);

            if (!isHttpSuccess) {
                transactionBufferImpl.insertData(new Pair<>(multiExprUpdate, true));
            } else {
                //TODO rsb notification
            }
        } catch (CouldNotPerformException e) {
            transactionBufferImpl.insertData(new Pair<>(multiExprUpdate, true));
        } catch (IllegalArgumentException e) {
            ExceptionPrinter.printHistory("Defect sparql update expression! Dropped.", e, LOGGER, LogLevel.ERROR);
        }
    }

}
