package org.openbase.bco.ontology.lib.manager.tbox;

/*-
 * #%L
 * BCO Ontology Library
 * %%
 * Copyright (C) 2016 - 2020 openbase.org
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

import org.openbase.bco.ontology.lib.utility.StringModifier;
import org.openbase.bco.ontology.lib.utility.sparql.RdfTriple;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntExpr;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntCl;
import org.openbase.bco.registry.lib.util.UnitConfigProcessor;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openbase.type.domotic.unit.UnitConfigType.UnitConfig;
import org.openbase.type.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;
import org.openbase.type.domotic.unit.connection.ConnectionConfigType.ConnectionConfig.ConnectionType;
import org.openbase.type.domotic.unit.location.LocationConfigType.LocationConfig.LocationType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author agatting on 20.02.17.
 */
@SuppressWarnings("checkstyle:multiplestringliterals")
public class OntClassMappingImpl implements OntClassMapping {

    private static final Logger LOGGER = LoggerFactory.getLogger(OntClassMappingImpl.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RdfTriple> getUnitTypeClasses(final List<UnitConfig> unitConfigs) {

        final List<RdfTriple> triples = new ArrayList<>();
        MultiException.ExceptionStack exceptionStack = null;

        for (final UnitConfig unitConfig : unitConfigs) {
            try {
                if (unitConfig == null) {
                    assert false;
                    throw new NotAvailableException("UnitConfig is null");
                }
                if (unitConfig.getUnitType().equals(UnitType.UNKNOWN)) {
                    continue;
                }

                triples.add(getUnitTypeClass(unitConfig.getUnitType()));

                switch (unitConfig.getUnitType()) {
                    case LOCATION:
                        triples.addAll(getLocationTypeClasses(unitConfig));
                        break;
                    case CONNECTION:
                        triples.addAll(getConnectionTypeClasses(unitConfig));
                        break;
                    default:
                }
            } catch (NotAvailableException ex) {
                exceptionStack = MultiException.push(this, ex, exceptionStack);
            }
        }

        try {
            MultiException.checkAndThrow(() ->"There are incompletely unit types!", exceptionStack);
        } catch (MultiException ex) {
            ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
        }
        return triples;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RdfTriple> getUnitTypeClasses() {

        MultiException.ExceptionStack exceptionStack = null;
        final List<RdfTriple> triples = new ArrayList<>();
        final UnitType[] unitTypes = UnitType.values();

        for (final UnitType unitType : unitTypes) {
            try {
                if (unitType.equals(UnitType.UNKNOWN)) {
                    continue;
                }

                triples.add(getUnitTypeClass(unitType));
            } catch (NotAvailableException ex) {
                exceptionStack = MultiException.push(this, ex, exceptionStack);
            }
        }

        triples.addAll(getLocationTypeClasses(null));
        triples.addAll(getConnectionTypeClasses(null));

        try {
            MultiException.checkAndThrow(() ->"There are incompletely unit types!", exceptionStack);
        } catch (MultiException ex) {
            ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
        }
        return triples;
    }

    private RdfTriple getUnitTypeClass(final UnitType unitType) throws NotAvailableException {

        final RdfTriple triple;
        final String unitTypeName = StringModifier.getPascalcaseName(unitType.name());

        if (UnitConfigProcessor.isDalUnit(unitType)) {
            triple = new RdfTriple(unitTypeName, OntExpr.SUB_CLASS_OF.getName(), OntCl.DAL_UNIT.getName());
        } else if (UnitConfigProcessor.isBaseUnit(unitType)) {
            triple = new RdfTriple(unitTypeName, OntExpr.SUB_CLASS_OF.getName(), OntCl.BASE_UNIT.getName());
        } else if (UnitConfigProcessor.isHostUnit(unitType))  {
            triple = new RdfTriple(unitTypeName, OntExpr.SUB_CLASS_OF.getName(), OntCl.HOST_UNIT.getName());
        } else {
            throw new NotAvailableException("Could not identify unit type: " + unitType);
        }
        return triple;
    }

    private List<RdfTriple> getLocationTypeClasses(final UnitConfig unitConfig) {

        final List<RdfTriple> triples = new ArrayList<>();

        if (unitConfig == null) {
            MultiException.ExceptionStack exceptionStack = null;
            final LocationType[] locationTypes = LocationType.values();

            for (final LocationType locationType : locationTypes) {
                try {
                    if (locationType.equals(LocationType.UNKNOWN)) {
                        continue;
                    }

                    final String locationTypeName = StringModifier.getPascalcaseName(locationType.name());
                    triples.add(new RdfTriple(locationTypeName, OntExpr.SUB_CLASS_OF.getName(), OntCl.LOCATION.getName()));
                } catch (NotAvailableException ex) {
                    exceptionStack = MultiException.push(this, ex, exceptionStack);
                }
            }

            try {
                MultiException.checkAndThrow(() ->"There are incompletely location types!", exceptionStack);
            } catch (MultiException ex) {
                ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
            }
        } else {
            try {
                final String locationTypeName = StringModifier.getPascalcaseName(unitConfig.getConnectionConfig().getConnectionType().name());
                triples.add(new RdfTriple(locationTypeName, OntExpr.SUB_CLASS_OF.getName(), OntCl.LOCATION.getName()));
            } catch (NotAvailableException ex) {
                ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
            }
        }
        return triples;
    }

    private List<RdfTriple> getConnectionTypeClasses(final UnitConfig unitConfig) {

        final List<RdfTriple> triples = new ArrayList<>();

        if (unitConfig == null) {
            MultiException.ExceptionStack exceptionStack = null;
            final ConnectionType[] connectionTypes = ConnectionType.values();

            for (final ConnectionType connectionType : connectionTypes) {
                try {
                    if (connectionType.equals(ConnectionType.UNKNOWN)) {
                        continue;
                    }

                    final String connectionTypeName = StringModifier.getPascalcaseName(connectionType.name());
                    triples.add(new RdfTriple(connectionTypeName, OntExpr.SUB_CLASS_OF.getName(), OntCl.CONNECTION.getName()));
                } catch (NotAvailableException ex) {
                    exceptionStack = MultiException.push(this, ex, exceptionStack);
                }
            }

            try {
                MultiException.checkAndThrow(() ->"There are incompletely connection types!", exceptionStack);
            } catch (MultiException ex) {
                ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
            }
        } else {
            try {
                final String connectionTypeName = StringModifier.getPascalcaseName(unitConfig.getConnectionConfig().getConnectionType().name());
                triples.add(new RdfTriple(connectionTypeName, OntExpr.SUB_CLASS_OF.getName(), OntCl.CONNECTION.getName()));
            } catch (NotAvailableException ex) {
                ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
            }
        }
        return triples;
    }

}
