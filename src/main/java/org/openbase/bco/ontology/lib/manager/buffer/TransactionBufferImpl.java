/**
 * ==================================================================
 * <p>
 * This file is part of org.openbase.bco.ontology.lib.
 * <p>
 * org.openbase.bco.ontology.lib is free software: you can redistribute it and modify
 * it under the terms of the GNU General Public License (Version 3)
 * as published by the Free Software Foundation.
 * <p>
 * org.openbase.bco.ontology.lib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with org.openbase.bco.ontology.lib. If not, see <http://www.gnu.org/licenses/>.
 * ==================================================================
 */
package org.openbase.bco.ontology.lib.manager.buffer;

import org.openbase.bco.ontology.lib.commun.web.WebInterface;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.CouldNotProcessException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.extension.rsb.iface.RSBInformer;
import org.openbase.jul.schedule.GlobalScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.ontology.OntologyChangeType.OntologyChange;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author agatting on 17.01.17.
 */
public class TransactionBufferImpl implements TransactionBuffer {

    //TODO handling, if one element is "defect" and can't be send to server/accepted from server

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionBufferImpl.class);
    private Future taskFuture;
    private boolean isTaskFutureInit = false;
    private final WebInterface webInterface;
    private final Queue<String> queue;
    private final OntologyChange.Category category;

    public TransactionBufferImpl() {
        this.webInterface = new WebInterface();
        this.queue = new ConcurrentLinkedQueue<>();
        this.category = OntologyChange.Category.UNKNOWN; //TODO
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createAndStartQueue(final RSBInformer<OntologyChange> synchronizedInformer) throws CouldNotPerformException {

        try {
            GlobalScheduledExecutorService.scheduleWithFixedDelay(() -> {

                while (queue.peek() != null) {
                    final String sparqlUpdateExpr = queue.peek();

                    try {
                        //TODO need info, if upload to all or single database
                        final int httpResponseCode = webInterface.sparqlUpdate(sparqlUpdateExpr);
                        final boolean httpSuccess = webInterface.httpRequestSuccess(httpResponseCode);

                        if (httpSuccess) {
                            queue.poll();

                            //TODO send rsb message, if queue is empty...
                            if (isTaskFutureInit) {
                                isTaskFutureInit = true;
                                setRSBInformerThread(synchronizedInformer);
                            } else if (!isTaskFutureInit && taskFuture.isCancelled()) {
                                setRSBInformerThread(synchronizedInformer);
                            }
                        } else {
                            throw new CouldNotProcessException("Cause response code is bad!");
                        }

                    } catch (CouldNotProcessException | IOException e) {
                        ExceptionPrinter.printHistory("Could not upload sparql expression!", e, LOGGER, LogLevel.WARN);
                    } catch (NotAvailableException e) {

                    }
                }
            }, 0, 1, TimeUnit.SECONDS);

        } catch (RejectedExecutionException | IllegalArgumentException | CouldNotPerformException e) {
            throw new CouldNotProcessException("Could not process transactionBuffer thread!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createAndStartQueue() throws CouldNotPerformException {

        try {
            GlobalScheduledExecutorService.scheduleAtFixedRate(() -> {

                while (queue.peek() != null) {
                    final String sparqlUpdateExpr = queue.peek();

                    try {
                        final int httpResponseCode = webInterface.sparqlUpdate(sparqlUpdateExpr);
                        final boolean httpSuccess = webInterface.httpRequestSuccess(httpResponseCode);

                        if (!httpSuccess) {
                            queue.poll();
                            LOGGER.warn("Could not upload queue entry, because response code is bad. Dropped to avoid endless loop.");
                        }

                    } catch (IOException e) {
                        throw new CouldNotProcessException("Could not upload sparql expression!", e);
                    }
                }
            }, 0, 1, TimeUnit.SECONDS);

        } catch (RejectedExecutionException | IllegalArgumentException | CouldNotPerformException e) {
            throw new CouldNotProcessException("Could not process transactionBuffer thread!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertData(final String sparqlUpdateExpr) throws CouldNotProcessException {
        boolean isElementInQueue = queue.offer(sparqlUpdateExpr);

        if (!isElementInQueue) {
            throw new CouldNotProcessException("Could not add element to queue!");
        }
        //TODO check size...
    }

    private void setRSBInformerThread(final RSBInformer<OntologyChange> synchronizedInformer) throws NotAvailableException {

        taskFuture = GlobalScheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                final OntologyChange ontologyChange = OntologyChange.newBuilder().addCategory(category).build();

                synchronizedInformer.publish(ontologyChange);
                taskFuture.cancel(true);
            } catch (CouldNotPerformException | InterruptedException e) {
                ExceptionPrinter.printHistory("Could not notify trigger via rsb!", e, LOGGER, LogLevel.ERROR);
            }
        }, 0, OntConfig.SMALL_RETRY_PERIOD_SECONDS, TimeUnit.SECONDS);
    }

}
