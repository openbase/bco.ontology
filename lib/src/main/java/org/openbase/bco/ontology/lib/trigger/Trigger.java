package org.openbase.bco.ontology.lib.trigger;

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

import org.openbase.jul.iface.Manageable;
import org.openbase.jul.pattern.Observer;
import org.openbase.type.domotic.state.ActivationStateType.ActivationState;
import org.openbase.type.domotic.ontology.TriggerConfigType.TriggerConfig;

/**
 * @author agatting on 21.12.16.
 */
public interface Trigger extends Manageable<TriggerConfig> {

    /**
     * Method registers the given observer to this observable to get informed about value changes.
     *
     * @param observer is the observer to register.
     */
    void addObserver(Observer<Trigger, ActivationState.State> observer);

    /**
     * Method removes the given observer from this observable to finish the observation.
     *
     * @param observer is the observer to remove.
     */
    void removeObserver(Observer<Trigger, ActivationState.State> observer);

    /**
     * Method returns the system of the created trigger.
     *
     * @return The TriggerConfig of the trigger.
     */
    TriggerConfig getTriggerConfig();
}
