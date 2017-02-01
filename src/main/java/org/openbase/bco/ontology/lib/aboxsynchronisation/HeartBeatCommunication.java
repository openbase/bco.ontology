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
package org.openbase.bco.ontology.lib.aboxsynchronisation;

import org.apache.commons.lang.time.DateUtils;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.openbase.bco.ontology.lib.ConfigureSystem;
import org.openbase.bco.ontology.lib.sparql.SparqlUpdateExpression;
import org.openbase.bco.ontology.lib.sparql.TripleArrayList;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author agatting on 31.01.17.
 */
public class HeartBeatCommunication extends SparqlUpdateExpression {

    //TODO situation handling: ontologyManager active, but no connection to server (buffer?)
    //TODO reduce to one SimpleDateFormat => sparql update doesn't accept "+" in instance name....

    private static final Logger LOGGER = LoggerFactory.getLogger(HeartBeatCommunication.class);
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ConfigureSystem.DATE_TIME, Locale.ENGLISH);
    private final SimpleDateFormat simpleDateFormatWithoutTimeZone = new SimpleDateFormat(ConfigureSystem
            .DATE_TIME_WITHOUT_TIME_ZONE, Locale.ENGLISH);

    private final static String queryLastTimeStampOfCurrentHeartBeat =
            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
            + "PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#> "
            + "SELECT ?blackout ?lastTimeStamp { "
                + "?blackout a NS:HeartBeatPhase . "
                + "?blackout NS:hasFirstHeartBeat ?firstTimeStamp . "
                + "?blackout NS:hasLastHeartBeat ?lastTimeStamp . "
            + "} "
            + "ORDER BY DESC(?lastTimeStamp) LIMIT 1";

    public HeartBeatCommunication() {

        final List<TripleArrayList> deleteTripleArrayLists = new ArrayList<>();
        final List<TripleArrayList> insertTripleArrayLists = new ArrayList<>();

        //generate new heartbeat phase
        setNewHeartBeatPhase();

        //observe current heartbeat now, refresh or start new heartbeat phase
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                // get recent heartbeat phase instance name and lastHeartBeat timestamp
                ResultSet resultSet = null;
                try {
                    resultSet = sparqlQuerySelect(queryLastTimeStampOfCurrentHeartBeat);
                } catch (CouldNotPerformException e) {
                    ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
                }

                if (resultSet != null && resultSet.hasNext()) { // in this case, resultSet has one solution only
                    final QuerySolution querySolution = resultSet.next();
                    final Date now = new Date();
                    final String dateTimeNow = simpleDateFormat.format(now);
                    String dateTimeQuery = "";
                    String heartBeatNameQuery = "";

                    final Iterator<String> stringIterator = querySolution.varNames();

                    while (stringIterator.hasNext()) {

                        final RDFNode rdfNode = querySolution.get(stringIterator.next());

                        if (rdfNode.isLiteral()) {
                            dateTimeQuery = rdfNode.asLiteral().getLexicalForm();
                        } else {
                            //TODO investigate why .getLocalName() of jena doesn't work here...
                            heartBeatNameQuery = rdfNode.asResource().toString();
                            heartBeatNameQuery = heartBeatNameQuery.substring(ConfigureSystem.NS.length()
                                    , heartBeatNameQuery.length());
                        }
                    }

                    Date dateLastTimeStamp = null;
                    try {
                        dateLastTimeStamp = simpleDateFormat.parse(dateTimeQuery + "+01:00");
                        dateLastTimeStamp = DateUtils.addSeconds(dateLastTimeStamp, 35);
                    } catch (ParseException e) {
                        ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR); //TODO
                    }

                    if (dateLastTimeStamp != null && dateLastTimeStamp.compareTo(now) >= 0) {

                        // last heartbeat is within the frequency => replace last timestamp of current blackout with
                        // refreshed timestamp
                        deleteTripleArrayLists.clear();
                        deleteTripleArrayLists.add(new TripleArrayList(heartBeatNameQuery
                                , ConfigureSystem.OntProp.HAS_LAST_HEARTBEAT.getName(), null));
                        insertTripleArrayLists.clear();
                        insertTripleArrayLists.add(new TripleArrayList(heartBeatNameQuery
                                , ConfigureSystem.OntProp.HAS_LAST_HEARTBEAT.getName()
                                , "\"" + dateTimeNow + "\"^^xsd:dateTime"));
                        //TODO ...
                        String test =
                                "NS:" + heartBeatNameQuery + " NS:hasLastHeartBeat " + "?object";

                        // sparql update to replace last heartbeat timestamp
                        final String sparqlUpdate = getSparqlBundleUpdateDeleteAndInsertEx(deleteTripleArrayLists
                                , insertTripleArrayLists, test);
                        System.out.println(sparqlUpdate);

                        try {
                            sparqlUpdate(sparqlUpdate);
                            //TODO responseCode...
                        } catch (CouldNotPerformException e) {
                            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR); //TODO
                        }
                    } else {
                        // lastHeartBeat timestamp isn't in time. start with new heartBeat phase
                        setNewHeartBeatPhase();
                    }
                }
            }
        }, 3000, 5000);
    }

    private void setNewHeartBeatPhase() {
        final Date now = new Date();
        final String dateTimeNowInstance = simpleDateFormatWithoutTimeZone.format(now);
        final String dateTimeNow = simpleDateFormat.format(now);

        final String subject = "heartBeatPhase" + dateTimeNowInstance;

        final List<TripleArrayList> insertTripleArrayLists = new ArrayList<>();
        // set initial current heartbeat phase with first and last timestamp (identical)
        insertTripleArrayLists.add(new TripleArrayList(subject, ConfigureSystem.OntExpr.A.getName()
                , ConfigureSystem.OntClass.HEARTBEAT_PHASE.getName()));
        insertTripleArrayLists.add(new TripleArrayList(subject, ConfigureSystem.OntProp.HAS_FIRST_HEARTBEAT.getName()
                , "\"" + dateTimeNow + "\"^^xsd:dateTime"));
        insertTripleArrayLists.add(new TripleArrayList(subject, ConfigureSystem.OntProp.HAS_LAST_HEARTBEAT.getName()
                , "\"" + dateTimeNow + "\"^^xsd:dateTime"));

        final String sparqlUpdate = getSparqlBundleUpdateInsertEx(insertTripleArrayLists);
        System.out.println(sparqlUpdate);

        boolean httpSuccess = false;
        try {
            while (!httpSuccess) { //TODO maybe endless loop
                final int responseCode = sparqlUpdate(sparqlUpdate);
                httpSuccess = httpRequestSuccess(responseCode);
            }

        } catch (CouldNotPerformException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR); //TODO
        }
    }

}
