package org.openbase.bco.ontology.lib.manager.abox.configuration;

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

import org.openbase.bco.ontology.lib.utility.StringModifier;
import org.openbase.bco.ontology.lib.utility.sparql.RdfTriple;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntCl;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntExpr;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openbase.type.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;
import org.openbase.type.domotic.unit.UnitConfigType.UnitConfig;

import java.util.ArrayList;
import java.util.List;
import org.openbase.bco.dal.lib.layer.service.Services;

/**
 * @author agatting on 23.12.16.
 */
@SuppressWarnings("checkstyle:multiplestringliterals")
public class OntInstanceMappingImpl implements OntInstanceMapping {

    private static final Logger LOGGER = LoggerFactory.getLogger(OntInstanceMappingImpl.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RdfTriple> getInsertConfigInstances(final List<UnitConfig> unitConfigs) {

        final List<RdfTriple> triples = new ArrayList<>();

        triples.addAll(getInsertUnitInstances(unitConfigs));
        triples.addAll(getInsertStateInstances());
        triples.addAll(getInsertProviderServiceInstances());
        triples.addAll(getInsertStateValueInstances());

        return triples;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RdfTriple> getInsertStateAndServiceAndValueInstances() {

        final List<RdfTriple> triples = new ArrayList<>();

        triples.addAll(getInsertStateInstances());
        triples.addAll(getInsertProviderServiceInstances());
        triples.addAll(getInsertStateValueInstances());

        return triples;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RdfTriple> getInsertUnitInstances(final List<UnitConfig> unitConfigs) {

        MultiException.ExceptionStack exceptionStack = null;
        final List<RdfTriple> triples = new ArrayList<>();

        try {
            for (final UnitConfig unitConfig : unitConfigs) {
                if (unitConfig == null) {
                    assert false;
                    throw new NotAvailableException("UnitConfig is null");
                }

                final String unitTypeName;

                try {
                    switch (unitConfig.getUnitType()) {
                        case LOCATION:
                            unitTypeName = StringModifier.getPascalcaseName(unitConfig.getLocationConfig().getLocationType().name());
                            break;
                        case CONNECTION:
                            unitTypeName = StringModifier.getPascalcaseName(unitConfig.getConnectionConfig().getConnectionType().name());
                            break;
                        default:
                            unitTypeName = StringModifier.getPascalcaseName(unitConfig.getUnitType().name());
                    }

                    triples.add(new RdfTriple(unitConfig.getId(), OntExpr.IS_A.getName(), unitTypeName));
                } catch (NotAvailableException ex) {
                    exceptionStack = MultiException.push(this, ex, exceptionStack);
                }
            }
        } catch (CouldNotPerformException ex) {
            exceptionStack = MultiException.push(this, ex, exceptionStack);
        }

        try {
            MultiException.checkAndThrow(() ->"There are incompletely unitTypes!", exceptionStack);
        } catch (MultiException ex) {
            ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
        }
        return triples;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RdfTriple> getInsertStateInstances() {

        MultiException.ExceptionStack exceptionStack = null;
        final List<RdfTriple> triples = new ArrayList<>();

        for (final ServiceType serviceType : ServiceType.values()) {
            if (serviceType.equals(ServiceType.UNKNOWN)) {
                continue;
            }

            try {
                final String stateName = StringModifier.firstCharToLowerCase(Services.getServiceStateName(serviceType));
                triples.add(new RdfTriple(stateName, OntExpr.IS_A.getName(), OntCl.STATE.getName()));
            } catch (NotAvailableException ex) {
                exceptionStack = MultiException.push(this, ex, exceptionStack);
            }
        }

        try {
            MultiException.checkAndThrow(() ->"There are incompletely stateTypes!", exceptionStack);
        } catch (MultiException ex) {
            ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
        }
        return triples;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RdfTriple> getInsertProviderServiceInstances() {

        MultiException.ExceptionStack exceptionStack = null;
        final List<RdfTriple> triples = new ArrayList<>();

        for (final ServiceType serviceType : ServiceType.values()) {
            if (serviceType.equals(ServiceType.UNKNOWN)) {
                continue;
            }

            try {
                final String serviceName = StringModifier.firstCharToLowerCase(StringModifier.getServiceTypeName(serviceType));
                triples.add(new RdfTriple(serviceName, OntExpr.IS_A.getName(), OntCl.PROVIDER_SERVICE.getName()));
            } catch (NotAvailableException ex) {
                exceptionStack = MultiException.push(this, ex, exceptionStack);
            }
        }

        try {
            MultiException.checkAndThrow(() ->"There are incompletely serviceTypes!", exceptionStack);
        } catch (MultiException ex) {
            ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
        }
        return triples;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RdfTriple> getInsertStateValueInstances() {

        final List<RdfTriple> triples = new ArrayList<>();

        for (final ServiceType serviceType : ServiceType.values()) {
            if (serviceType.equals(ServiceType.UNKNOWN)) {
                continue;
            }
            try {
                for (final Object stateValue : Services.getServiceStateEnumValues(serviceType).toArray()) {
                    final String stateValueName = StringModifier.firstCharToLowerCase(StringModifier.getPascalcaseName(stateValue.toString()));

                    if (stateValueName.equalsIgnoreCase("unknown")) {
                        continue;
                    }
                    triples.add(new RdfTriple(stateValueName, OntExpr.IS_A.getName(), OntCl.STATE_VALUE.getName()));
                }
            } catch (NotAvailableException ex) {
                // ignore, because existing state values are needed only
            }
        }
        return triples;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RdfTriple> getDeleteUnitInstances(final List<UnitConfig> unitConfigs) {

        final List<RdfTriple> triples = new ArrayList<>();
        MultiException.ExceptionStack exceptionStack = null;

        for (final UnitConfig unitConfig : unitConfigs) {
            try {
                triples.add(getDeleteUnitInstance(unitConfig));
            } catch (NotAvailableException ex) {
                exceptionStack = MultiException.push(this, ex, exceptionStack);
            }
        }

        try {
            MultiException.checkAndThrow(() ->"There are incompletely units!", exceptionStack);
        } catch (MultiException ex) {
            ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
        }
        return triples;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RdfTriple> getDeleteProviderServiceInstances(final List<ServiceType> serviceTypes) {

        MultiException.ExceptionStack exceptionStack = null;
        final List<RdfTriple> triples = new ArrayList<>();

        for (final ServiceType serviceType : serviceTypes) {
            try {
                final String serviceName = StringModifier.firstCharToLowerCase(StringModifier.getServiceTypeName(serviceType));
                triples.add(new RdfTriple(serviceName, OntExpr.IS_A.getName(), null));
            } catch (NotAvailableException ex) {
                exceptionStack = MultiException.push(this, ex, exceptionStack);
            }
        }

        try {
            MultiException.checkAndThrow(() ->"There are incompletely services!", exceptionStack);
        } catch (MultiException ex) {
            ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
        }
        return triples;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RdfTriple> getDeleteStateInstances(final List<ServiceType> serviceTypes) {

        MultiException.ExceptionStack exceptionStack = null;
        final List<RdfTriple> triples = new ArrayList<>();

        for (final ServiceType serviceType : serviceTypes) {
            try {
                final String stateName = StringModifier.firstCharToLowerCase(Services.getServiceStateName(serviceType));
                triples.add(new RdfTriple(stateName, OntExpr.IS_A.getName(), null));
            } catch (NotAvailableException ex) {
                exceptionStack = MultiException.push(this, ex, exceptionStack);
            }
        }

        try {
            MultiException.checkAndThrow(() ->"There are incompletely states!", exceptionStack);
        } catch (MultiException ex) {
            ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
        }
        return triples;
    }

    private RdfTriple getDeleteUnitInstance(final UnitConfig unitConfig) throws NotAvailableException {

        if (unitConfig == null) {
            assert false;
            throw new NotAvailableException("UnitConfig is null");
        }
        return new RdfTriple(unitConfig.getId(), OntExpr.IS_A.getName(), null);
    }

}
