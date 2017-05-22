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
package org.openbase.bco.ontology.lib.manager.abox.configuration;

import org.openbase.bco.dal.lib.layer.service.Service;
import org.openbase.bco.ontology.lib.utility.StringModifier;
import org.openbase.bco.ontology.lib.utility.RdfTriple;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntCl;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntExpr;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;
import rst.domotic.unit.UnitConfigType.UnitConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author agatting on 23.12.16.
 */
public class OntInstanceMappingImpl implements OntInstanceMapping {

    private static final Logger LOGGER = LoggerFactory.getLogger(OntInstanceMappingImpl.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RdfTriple> getInsertConfigInstances(List<UnitConfig> unitConfigs) {

        final List<RdfTriple> triples = new ArrayList<>();

        triples.addAll(getInsertUnitInstances(unitConfigs));
        triples.addAll(getInsertStateInstances());
        triples.addAll(getInsertProviderServiceInstances());

        return triples;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RdfTriple> getInsertStateAndServiceInstances() {

        final List<RdfTriple> triples = new ArrayList<>();

        triples.addAll(getInsertStateInstances());
        triples.addAll(getInsertProviderServiceInstances());

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
                    switch (unitConfig.getType()) {
                        case LOCATION:
                            unitTypeName = StringModifier.getCamelCaseName(unitConfig.getLocationConfig().getType().name());
                            break;
                        case CONNECTION:
                            unitTypeName = StringModifier.getCamelCaseName(unitConfig.getConnectionConfig().getType().name());
                            break;
                        default:
                            unitTypeName = StringModifier.getCamelCaseName(unitConfig.getType().name());
                    }

                    triples.add(new RdfTriple(unitConfig.getId(), OntExpr.A.getName(), unitTypeName));
                } catch (NotAvailableException e) {
                    exceptionStack = MultiException.push(this, e, exceptionStack);
                }
            }
        } catch (CouldNotPerformException e) {
            exceptionStack = MultiException.push(this, e, exceptionStack);
        }

        try {
            MultiException.checkAndThrow("There are incompletely unitTypes!", exceptionStack);
        } catch (MultiException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
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
                final String stateName = StringModifier.firstCharToLowerCase(Service.getServiceStateName(serviceType));
                triples.add(new RdfTriple(stateName, OntExpr.A.getName(), OntCl.STATE.getName()));
            } catch (NotAvailableException e) {
                exceptionStack = MultiException.push(this, e, exceptionStack);
            }
        }

        try {
            MultiException.checkAndThrow("There are incompletely stateTypes!", exceptionStack);
        } catch (MultiException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
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
                triples.add(new RdfTriple(serviceName, OntExpr.A.getName(), OntCl.PROVIDER_SERVICE.getName()));
            } catch (NotAvailableException e) {
                exceptionStack = MultiException.push(this, e, exceptionStack);
            }
        }

        try {
            MultiException.checkAndThrow("There are incompletely serviceTypes!", exceptionStack);
        } catch (MultiException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
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
            } catch (NotAvailableException e) {
                exceptionStack = MultiException.push(this, e, exceptionStack);
            }
        }

        try {
            MultiException.checkAndThrow("There are incompletely units!", exceptionStack);
        } catch (MultiException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
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
                triples.add(new RdfTriple(serviceName, OntExpr.A.getName(), null));
            } catch (NotAvailableException e) {
                exceptionStack = MultiException.push(this, e, exceptionStack);
            }
        }

        try {
            MultiException.checkAndThrow("There are incompletely services!", exceptionStack);
        } catch (MultiException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
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
                final String stateName = StringModifier.firstCharToLowerCase(Service.getServiceStateName(serviceType));
                triples.add(new RdfTriple(stateName, OntExpr.A.getName(), null));
            } catch (NotAvailableException e) {
                exceptionStack = MultiException.push(this, e, exceptionStack);
            }
        }

        try {
            MultiException.checkAndThrow("There are incompletely states!", exceptionStack);
        } catch (MultiException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }
        return triples;
    }

    private RdfTriple getDeleteUnitInstance(final UnitConfig unitConfig) throws NotAvailableException {

        if (unitConfig == null) {
            assert false;
            throw new NotAvailableException("UnitConfig is null");
        }
        return new RdfTriple(unitConfig.getId(), OntExpr.A.getName(), null);
    }

}
