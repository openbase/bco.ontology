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
package org.openbase.bco.ontology.lib;

import org.apache.jena.ontology.OntModel;
import org.openbase.bco.ontology.lib.aboxsynchronisation.dataobservation.TransactionBuffer;
import org.openbase.bco.ontology.lib.aboxsynchronisation.dataobservation.TransactionBufferImpl;
import org.openbase.bco.ontology.lib.commun.rsb.RsbCommunication;
import org.openbase.bco.ontology.lib.commun.web.ServerOntologyModel;
import org.openbase.bco.ontology.lib.config.OntologyChange;
import org.openbase.bco.ontology.lib.config.OntConfig;
import org.openbase.bco.ontology.lib.config.jp.JPRsbScope;
import org.openbase.bco.ontology.lib.datapool.UnitRegistrySynchronizer;
import org.openbase.bco.ontology.lib.datapool.UnitRemoteSynchronizer;
import org.openbase.bco.ontology.lib.tboxsynchronisation.TBoxLoader;
import org.openbase.bco.ontology.lib.trigger.Trigger;
import org.openbase.bco.ontology.lib.trigger.TriggerConfig;
import org.openbase.bco.ontology.lib.trigger.TriggerFactory;
import org.openbase.bco.ontology.lib.trigger.sparql.AskQueryExamples;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jps.preset.JPDebugMode;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.extension.rsb.iface.RSBInformer;
import org.openbase.jul.iface.Launchable;
import org.openbase.jul.iface.VoidInitializable;
import org.openbase.jul.pattern.Observable;
import org.openbase.jul.schedule.Stopwatch;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import rst.domotic.state.ActivationStateType.ActivationState;

import java.util.ArrayList;
import java.util.List;

/**
 * @author agatting on 20.10.16.
 */
public final class OntologyManagerController implements Launchable<Void>, VoidInitializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(OntologyManagerController.class);

    private static final String QUERY =
            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
                + "ASK { "
                + "?x a NS:Device . "
                + "} ";

    @Override
    public void activate() throws CouldNotPerformException, InterruptedException {
//        HeartBeatCommunication heartBeatCommunication = new HeartBeatCommunication();
//        new TBoxSynchronizer();

        OntModel ontModel = TBoxLoader.loadOntModelFromFile(null); //TODO catch
        ServerOntologyModel.addOntologyModel(ontModel, OntConfig.getOntDatabaseUri());
        Stopwatch stopwatch = new Stopwatch();
        try {

//        LightDataType.LightData lightData = LightDataType.LightData.newBuilder().setId("").build(); //TODO

            final RSBInformer<String> rsbInformer = RsbCommunication.createInformer(JPService.getProperty(JPRsbScope.class).getValue());
            final TransactionBuffer transactionBuffer = new TransactionBufferImpl();
            transactionBuffer.createAndStartQueue();
            new UnitRegistrySynchronizer(transactionBuffer);

            stopwatch.waitForStart(2000);
            new UnitRemoteSynchronizer(transactionBuffer, rsbInformer);
        } catch (JPNotAvailableException e) {
            throw new CouldNotPerformException("Could not get rsb scope from jp service.", e);
        }

        stopwatch.waitForStart(10000);
        System.out.println("Erstelle Trigger...");
        final List<OntologyChange.Category> changeCategories = new ArrayList<>();
        changeCategories.add(OntologyChange.Category.UNIT);

        final TriggerConfig triggerConfig = new TriggerConfig("trigger0", AskQueryExamples.QUERY_0, changeCategories);

        final TriggerFactory triggerFactory = new TriggerFactory();
        final Trigger trigger = triggerFactory.newInstance(triggerConfig);
        trigger.addObserver((Observable<ActivationState.State> source, ActivationState.State data) -> {
            System.out.println(trigger.getConfig().getLabel() + " is " + data);
            // do useful stuff
        });

//        WebInterface webInterface = new WebInterface();

//        final QueryOntology queryOntology = new QueryOntology(ontology.getModel());
//        queryOntology.queryModel();

//        ontology.getModel().close();
//        if (ontology.getModel().isClosed()) {
////            LOGGER.info(APP_NAME + " finished!");
//            System.exit(0);
//        }

        try {
            if (JPService.getProperty(JPDebugMode.class).getValue()) {
                LOGGER.info("Debug Mode");
            }
        } catch (JPNotAvailableException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }
    }

    @Override
    public void deactivate() throws CouldNotPerformException, InterruptedException {
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public void init() throws InitializationException, InterruptedException {
    }
}
