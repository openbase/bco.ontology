package org.openbase.bco.ontology.lib.manager.aggregation.datatype;

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

import org.openbase.bco.ontology.lib.utility.Preconditions;
import org.openbase.jul.exception.MultiException;

import java.util.HashMap;

/**
 * This class is part of a data structure to provide the BCO ontology data (state values of sensors and actuators). The
 * custom data type {@link OntUnits} expresses the top-level of the data structure (highest granularity), which includes
 * a quantity of elements (1:N - relation). Consider in addition the data types {@link OntProviderServices} and
 * {@link OntStateChange}.
 *
 *           1       :      N                       1       :      N
 * {@link OntUnits} --- (includes) --- {@link OntProviderServices} --- (includes) --- {@link OntStateChange}
 *
 * @author agatting on 15.09.17.
 */
public class OntUnits {

    private final HashMap<String, OntProviderServices> ontUnits;

    /**
     * Constructor creates a hashMap to describe the unitId's as key and their related {@link OntProviderServices} as
     * value.
     */
    public OntUnits() {
        this.ontUnits = new HashMap<>();
    }

    /**
     * Method returns the unitIds with the related set of providerServices.
     *
     * @return the hashMap with unitIds and related providerServices.
     */
    public HashMap<String, OntProviderServices> getOntUnits() {
        return ontUnits;
    }

    /**
     * Method provides the providerServices object based on the input unitId.
     *
     * @param unitId is the unit, which associated providerServices are needed.
     * @return the {@link OntProviderServices} by match - a mapping of providerServices. Otherwise null.
     */
    public OntProviderServices getOntProviderServices(final String unitId) {
        return (unitId == null) ? null : ontUnits.get(unitId);
    }

    /**
     * Method adds a new entry to the ontUnits hashMap. If there is an existing unitId entry, the ontProviderService
     * with ontStateChange will be added to the related {@link OntProviderServices} value. Otherwise a new
     * {@link OntProviderServices} will be created.
     *
     * @param unitId is the unitId extracted from the ontology.
     * @param ontProviderService is the providerService extracted from the ontology.
     * @param ontStateChange is the stateChange extracted from the ontology.
     * @throws MultiException is thrown in case at least one parameter is null.
     */
    public void addOntProviderService(final String unitId, final String ontProviderService,
                                      final OntStateChange ontStateChange) throws MultiException
    {
        Preconditions.multipleCheckNotNullAndThrow(this, unitId);

        if (ontUnits.containsKey(unitId)) {
            ontUnits.get(unitId).addOntStateChange(ontProviderService, ontStateChange);
        } else {
            ontUnits.put(unitId, new OntProviderServices(ontProviderService, ontStateChange));
        }
    }

}
