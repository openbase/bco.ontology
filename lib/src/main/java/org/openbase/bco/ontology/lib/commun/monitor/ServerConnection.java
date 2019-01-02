package org.openbase.bco.ontology.lib.commun.monitor;

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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.openbase.bco.ontology.lib.commun.web.SparqlHttp;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.pattern.ObservableImpl;
import org.openbase.jul.pattern.Remote;
import org.openbase.jul.pattern.Remote.ConnectionState;
import org.openbase.jul.schedule.GlobalScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author agatting on 27.02.17.
 */
public final class ServerConnection {

    /**
     * Informs about the server connection state.
     */
    public static final ObservableImpl<Remote, ConnectionState> SERVER_STATE_OBSERVABLE = new ObservableImpl<>(false);

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerConnection.class);

    private ServerConnection() throws NotAvailableException {
    }

    /**
     * Method creates a monitoring thread to observe the connection state between ontology manager and ontology server. In case the server can't be
     * reached, an observable informs.
     *
     * @throws NotAvailableException is thrown in case there is no thread available.
     */
    public static void newServerConnectionObservable() throws NotAvailableException {
        GlobalScheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                try {
                    final HttpClient httpclient = HttpClients.createDefault();
                    final HttpGet httpGet = new HttpGet(OntConfig.getOntologyPingUrl());
                    final HttpResponse httpResponse = httpclient.execute(httpGet);

                    SparqlHttp.checkHttpRequest(httpResponse, null);
                    SERVER_STATE_OBSERVABLE.notifyObservers(ConnectionState.CONNECTED);
                } catch (IOException | CouldNotPerformException ex) {
                    SERVER_STATE_OBSERVABLE.notifyObservers(ConnectionState.DISCONNECTED);
                }
            } catch (CouldNotPerformException ex) {
                ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
            }
        }, 0, 1, TimeUnit.SECONDS);
    }
}
