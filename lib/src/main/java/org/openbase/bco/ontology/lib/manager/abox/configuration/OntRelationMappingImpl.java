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

import org.openbase.bco.dal.lib.layer.service.Services;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntProp;
import org.openbase.bco.ontology.lib.utility.StringModifier;
import org.openbase.bco.ontology.lib.utility.sparql.RdfTriple;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.extension.type.processing.LabelProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openbase.type.domotic.service.ServiceConfigType.ServiceConfig;
import org.openbase.type.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;
import org.openbase.type.domotic.state.EnablingStateType.EnablingState.State;
import org.openbase.type.domotic.unit.UnitConfigType.UnitConfig;
import org.openbase.type.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author agatting on 21.12.16.
 */
@SuppressWarnings("checkstyle:multiplestringliterals")
public class OntRelationMappingImpl implements OntRelationMapping {

    private static final Logger LOGGER = LoggerFactory.getLogger(OntRelationMappingImpl.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RdfTriple> getInsertUnitRelations(final List<UnitConfig> unitConfigs) {

        final List<RdfTriple> triples = new ArrayList<>();

        for (final UnitConfig unitConfig : unitConfigs) {
            triples.addAll(getInsertUnitRelations(unitConfig));
        }
        return triples;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RdfTriple> getInsertUnitRelations(final UnitConfig unitConfig) {

        List<RdfTriple> triples = new ArrayList<>();
        try {
            if (unitConfig.getUnitType().equals(UnitType.LOCATION)) {
                triples.addAll(getInsertSubLocationRelation(unitConfig));
                triples.addAll(getInsertHasUnitRelation(unitConfig));
            } else if (unitConfig.getUnitType().equals(UnitType.CONNECTION)) {
                triples.addAll(getInsertConnectionRelation(unitConfig));
            }

            triples.add(getInsertLabelRelation(unitConfig));
            triples.add(getInsertIsEnabledRelation(unitConfig));
            triples.addAll(getInsertProviderServiceRelation(unitConfig));

        } catch (NotAvailableException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }
        return triples;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RdfTriple> getDeleteUnitRelations(final List<UnitConfig> unitConfigs) {

        final List<RdfTriple> triples = new ArrayList<>();

        for (final UnitConfig unitConfig : unitConfigs) {
            triples.addAll(getDeleteUnitRelations(unitConfig));
        }
        return triples;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RdfTriple> getDeleteUnitRelations(final UnitConfig unitConfig) {

        List<RdfTriple> triples = new ArrayList<>();

        if (unitConfig.getUnitType().equals(UnitType.LOCATION)) {
            triples.add(getDeleteSubLocationRelation(unitConfig));
            triples.add(getDeleteUnitRelation(unitConfig));
        } else if (unitConfig.getUnitType().equals(UnitType.CONNECTION)) {
            triples.add(getDeleteConnectionRelation(unitConfig));
        }

//        triples.add(getDeleteStateRelation());
        triples.add(getDeleteLabelRelation(unitConfig));
        triples.add(getDeleteIsEnabledRelation(unitConfig));
        triples.add(getDeleteProviderServiceRelation(unitConfig));

        return triples;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RdfTriple getInsertStateRelation(final ServiceType serviceType) throws NotAvailableException {
        final String serviceTypeName = StringModifier.firstCharToLowerCase(StringModifier.getServiceTypeName(serviceType));
        final String stateTypeName = StringModifier.firstCharToLowerCase(Services.getServiceStateName(serviceType));

        return new RdfTriple(serviceTypeName, OntProp.STATE.getName(), stateTypeName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RdfTriple> getInsertStateRelations(final List<ServiceType> serviceTypes) {

        final List<RdfTriple> triples = new ArrayList<>();
        List<ServiceType> serviceTypesBuf = new ArrayList<>();
        MultiException.ExceptionStack exceptionStack = null;

        if (serviceTypes == null) {
            for (final ServiceType serviceType : ServiceType.values()) {
                if (serviceType.equals(ServiceType.UNKNOWN)) {
                    continue;
                }
                serviceTypesBuf.add(serviceType);
            }
        } else {
            serviceTypesBuf = serviceTypes;
        }

        for (final ServiceType serviceType : serviceTypesBuf) {
            try {
                triples.add(getInsertStateRelation(serviceType));
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
    public RdfTriple getDeleteStateRelation(final ServiceType serviceType) throws NotAvailableException {

        final String serviceTypeName = StringModifier.firstCharToLowerCase(StringModifier.getServiceTypeName(serviceType));
        final String stateTypeName = StringModifier.firstCharToLowerCase(Services.getServiceStateName(serviceType));

        return new RdfTriple(serviceTypeName, OntProp.STATE.getName(), stateTypeName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RdfTriple> getDeleteStateRelation(final List<ServiceType> serviceTypes) {

        final List<RdfTriple> triples = new ArrayList<>();
        MultiException.ExceptionStack exceptionStack = null;

        for (final ServiceType serviceType : serviceTypes) {
            try {
                triples.add(getDeleteStateRelation(serviceType));
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

    private List<RdfTriple> getInsertSubLocationRelation(final UnitConfig unitConfig) {

        final List<RdfTriple> triples = new ArrayList<>();

        // get all child IDs of the unit location
        for (final String childId : unitConfig.getLocationConfig().getChildIdList()) {
            triples.add(new RdfTriple(unitConfig.getId(), OntProp.SUB_LOCATION.getName(), childId));
        }
        return triples;
    }

    private RdfTriple getDeleteSubLocationRelation(final UnitConfig unitConfig) {
        return new RdfTriple(unitConfig.getId(), OntProp.SUB_LOCATION.getName(), null);
    }

    private List<RdfTriple> getInsertHasUnitRelation(final UnitConfig unitConfig) {

        final List<RdfTriple> triples = new ArrayList<>();

        // get all unit IDs, which can be found in the unit location
        for (final String unitId : unitConfig.getLocationConfig().getUnitIdList()) {
            triples.add(new RdfTriple(unitConfig.getId(), OntProp.UNIT.getName(), unitId));
        }
        return triples;
    }

    private RdfTriple getDeleteUnitRelation(final UnitConfig unitConfig) {
        return new RdfTriple(unitConfig.getId(), OntProp.UNIT.getName(), null);
    }

    private List<RdfTriple> getInsertConnectionRelation(final UnitConfig unitConfig) {

        final List<RdfTriple> triples = new ArrayList<>();

        // get all tiles, which contains the connection unit
        for (final String tileIdName : unitConfig.getConnectionConfig().getTileIdList()) {
            triples.add(new RdfTriple(tileIdName, OntProp.CONNECTION.getName(), unitConfig.getId()));
        }
        return triples;
    }

    private RdfTriple getDeleteConnectionRelation(final UnitConfig unitConfig) {
        return new RdfTriple(null, OntProp.CONNECTION.getName(), unitConfig.getId());
    }

    private RdfTriple getInsertLabelRelation(final UnitConfig unitConfig) throws NotAvailableException {
        return new RdfTriple(unitConfig.getId(), OntProp.LABEL.getName(), StringModifier.addQuotationMarks(LabelProcessor.getBestMatch(unitConfig.getLabel())));
    }

    private RdfTriple getDeleteLabelRelation(final UnitConfig unitConfig) {
        return new RdfTriple(unitConfig.getId(), OntProp.LABEL.getName(), null);
    }

    private RdfTriple getInsertIsEnabledRelation(final UnitConfig unitConfig) throws NotAvailableException {

        if (unitConfig.getEnablingState().getValue().equals(State.ENABLED)) {
            return new RdfTriple(unitConfig.getId(), OntProp.IS_ENABLED.getName(), StringModifier.addQuotationMarks("true"));
        } else {
            return new RdfTriple(unitConfig.getId(), OntProp.IS_ENABLED.getName(), StringModifier.addQuotationMarks("false"));
        }
    }

    private RdfTriple getDeleteIsEnabledRelation(final UnitConfig unitConfig) {
        return new RdfTriple(unitConfig.getId(), OntProp.IS_ENABLED.getName(), null);
    }

    private List<RdfTriple> getInsertProviderServiceRelation(final UnitConfig unitConfig) {

        final List<RdfTriple> triples = new ArrayList<>();
        MultiException.ExceptionStack exceptionStack = null;

        for (final ServiceConfig serviceConfig : unitConfig.getServiceConfigList()) {
            try {
                final String serviceTypeName = StringModifier.firstCharToLowerCase(StringModifier.getServiceTypeName(serviceConfig.getServiceDescription().getServiceType()));
                triples.add(new RdfTriple(unitConfig.getId(), OntProp.PROVIDER_SERVICE.getName(), serviceTypeName));
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

    private RdfTriple getDeleteProviderServiceRelation(final UnitConfig unitConfig) {
        return new RdfTriple(unitConfig.getId(), OntProp.PROVIDER_SERVICE.getName(), null);
    }

}
