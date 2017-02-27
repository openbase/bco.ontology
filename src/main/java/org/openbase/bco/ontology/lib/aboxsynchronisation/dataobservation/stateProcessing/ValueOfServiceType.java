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
package org.openbase.bco.ontology.lib.aboxsynchronisation.dataobservation.stateProcessing;

import javafx.util.Pair;
import rst.domotic.state.ActionStateType.ActionState;
import rst.domotic.state.ActivationStateType.ActivationState;
import rst.domotic.state.AlarmStateType.AlarmState;
import rst.domotic.state.BatteryStateType.BatteryState;
import rst.domotic.state.BlindStateType.BlindState;
import rst.domotic.state.BrightnessStateType.BrightnessState;
import rst.domotic.state.ButtonStateType.ButtonState;
import rst.domotic.state.ColorStateType.ColorState;
import rst.domotic.state.ContactStateType.ContactState;
import rst.domotic.state.DoorStateType.DoorState;
import rst.domotic.state.EnablingStateType.EnablingState;
import rst.domotic.state.HandleStateType.HandleState;
import rst.domotic.state.IntensityStateType.IntensityState;
import rst.domotic.state.InventoryStateType.InventoryState;
import rst.domotic.state.MotionStateType.MotionState;
import rst.domotic.state.PassageStateType.PassageState;
import rst.domotic.state.PowerConsumptionStateType;
import rst.domotic.state.PowerStateType.PowerState;


import java.util.HashSet;
import java.util.Set;

/**
 * The class contains methods for the individual stateTypes. Each method gets the stateValues(s) of the stateType and converts the stateValues(s) to the
 * SPARQL codec in string form. Additionally each method decided, if the state value is an literal (category: continuous, {@code false})
 * or an instance (category: discrete, {@code true}).
 *
 * @author agatting on 22.02.17.
 */
public class ValueOfServiceType {

    /**
     * Method returns state values of the given actionState.
     *
     * @param actionState The ActionState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    protected Set<Pair<String, Boolean>> actionStateValue(final ActionState actionState) {

        final Set<Pair<String, Boolean>> actionValuePairSet = new HashSet<>();
        actionValuePairSet.add(new Pair<>(actionState.getValue().toString(), false));

        return actionValuePairSet;
    }

    /**
     * Method returns state values of the given activationState.
     *
     * @param activationState The ActivationState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    protected Set<Pair<String, Boolean>> activationStateValue(final ActivationState activationState) {

        final Set<Pair<String, Boolean>> activationValuePairSet = new HashSet<>();
        activationValuePairSet.add(new Pair<>(activationState.getValue().toString(), false));

        return activationValuePairSet;
    }

    /**
     * Method returns state values of the given alarmState.
     *
     * @param alarmState The AlarmState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    protected Set<Pair<String, Boolean>> alarmStateValue(final AlarmState alarmState) {

        final Set<Pair<String, Boolean>> alarmValuePairSet = new HashSet<>();
        alarmValuePairSet.add(new Pair<>(alarmState.getValue().toString(), false));

        return alarmValuePairSet;
    }

    /**
     * Method returns state values of the given batteryState.
     *
     * @param batteryState The BatteryState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    protected Set<Pair<String, Boolean>> batteryStateValue(final BatteryState batteryState) {

        final Set<Pair<String, Boolean>> batteryValuesPairSet = new HashSet<>();

        batteryValuesPairSet.add(new Pair<>(batteryState.getValue().toString(), false));
        batteryValuesPairSet.add(new Pair<>(String.valueOf(batteryState.getLevel()), true));

        return batteryValuesPairSet;
    }

    /**
     * Method returns state values of the given blindState.
     *
     * @param blindState The BlindState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    protected Set<Pair<String, Boolean>> blindStateValue(final BlindState blindState) {

        final Set<Pair<String, Boolean>> blindValuePairSet = new HashSet<>();
        blindValuePairSet.add(new Pair<>(blindState.getMovementState().toString(), false));
        blindValuePairSet.add(new Pair<>(String.valueOf(blindState.getOpeningRatio()), true));

        return blindValuePairSet;
    }

    /**
     * Method returns state values of the given brightnessState.
     *
     * @param brightnessState The BrightnessState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    protected Set<Pair<String, Boolean>> brightnessStateValue(final BrightnessState brightnessState) {

        final Set<Pair<String, Boolean>> brightnessValuePairSet = new HashSet<>();
        brightnessValuePairSet.add(new Pair<>(String.valueOf(brightnessState.getBrightness()), true));
//        brightnessValuePairSet.add(new Pair<>(brightnessState.getBrightnessDataUnit()., false)); //TODO get brightness dataUnit?

        return brightnessValuePairSet;
    }

    /**
     * Method returns state values of the given buttonState.
     *
     * @param buttonState The ButtonState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    protected Set<Pair<String, Boolean>> buttonStateValue(final ButtonState buttonState) {

        final Set<Pair<String, Boolean>> buttonValuePairSet = new HashSet<>();
        buttonValuePairSet.add(new Pair<>(buttonState.getValue().toString(), false));
//        buttonState.getLastPressed() //

        return buttonValuePairSet;
    }

    /**
     * Method returns state values of the given colorState.
     *
     * @param colorState The ColorState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    protected Set<Pair<String, Boolean>> colorStateValue(final ColorState colorState) {

        final Set<Pair<String, Boolean>> hsbValuesPairSet = new HashSet<>();

        final double brightness = colorState.getColor().getHsbColor().getBrightness();
        hsbValuesPairSet.add(new Pair<>("\"" + brightness + "\"^^NS:Brightness", true));

        final double hue = colorState.getColor().getHsbColor().getHue();
        hsbValuesPairSet.add(new Pair<>("\"" + hue + "\"^^NS:Hue", true));

        final double saturation = colorState.getColor().getHsbColor().getSaturation();
        hsbValuesPairSet.add(new Pair<>("\"" + saturation + "\"^^NS:Saturation", true));

        return hsbValuesPairSet;
    }

    /**
     * Method returns state values of the given contactState.
     *
     * @param contactState The ContactState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    protected Set<Pair<String, Boolean>> contactStateValue(final ContactState contactState) {

        final Set<Pair<String, Boolean>> contactValuePairSet = new HashSet<>();
        contactValuePairSet.add(new Pair<>(contactState.getValue().toString(), false));

        return contactValuePairSet;
    }

    /**
     * Method returns state values of the given doorState.
     *
     * @param doorState The DoorState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    protected Set<Pair<String, Boolean>> doorStateValue(final DoorState doorState) {

        final Set<Pair<String, Boolean>> doorValuePairSet = new HashSet<>();
        doorValuePairSet.add(new Pair<>(doorState.getValue().toString(), false));

        return doorValuePairSet;
    }

    /**
     * Method returns state values of the given enablingState.
     *
     * @param enablingState The EnablingState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    protected Set<Pair<String, Boolean>> enablingStateValue(final EnablingState enablingState) {

        final Set<Pair<String, Boolean>> enablingValuePairSet = new HashSet<>();
        enablingValuePairSet.add(new Pair<>(enablingState.getValue().toString(), false));

        return enablingValuePairSet;
    }

    /**
     * Method returns state values of the given handleState.
     *
     * @param handleState The HandleState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    protected Set<Pair<String, Boolean>> handleStateValue(final HandleState handleState) {

        final Set<Pair<String, Boolean>> handleValuePairSet = new HashSet<>();
        handleValuePairSet.add(new Pair<>(String.valueOf(handleState.getPosition()), true));

        return handleValuePairSet;
    }

    /**
     * Method returns state values of the given intensityState.
     *
     * @param intensityState The IntensityState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    protected Set<Pair<String, Boolean>> intensityStateValue(final IntensityState intensityState) {

        final Set<Pair<String, Boolean>> intensityValuePairSet = new HashSet<>();
        intensityValuePairSet.add(new Pair<>(String.valueOf(intensityState.getIntensity()), true));
//        intensityState.getIntensityDataUnit(). //TODO dataUnit?

        return intensityValuePairSet;
    }

    /**
     * Method returns state values of the given inventoryState.
     *
     * @param inventoryState The InventoryState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    protected Set<Pair<String, Boolean>> inventoryStateValue(final InventoryState inventoryState) {
        //TODO ID's literal or instance?
        final Set<Pair<String, Boolean>> inventoryValuePairSet = new HashSet<>();
        inventoryValuePairSet.add(new Pair<>(inventoryState.getValue().toString(), false));

        return inventoryValuePairSet;
    }

    /**
     * Method returns state values of the given motionState.
     *
     * @param motionState The MotionState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    protected Set<Pair<String, Boolean>> motionStateValue(final MotionState motionState) {

        final Set<Pair<String, Boolean>> motionValuePairSet = new HashSet<>();
        motionValuePairSet.add(new Pair<>(motionState.getValue().toString(), false));

        return motionValuePairSet;
    }

    /**
     * Method returns state values of the given passageState.
     *
     * @param passageState The PassageState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    protected Set<Pair<String, Boolean>> passageStateValue(final PassageState passageState) {

        final Set<Pair<String, Boolean>> passageValuePairSet = new HashSet<>();
//        passageValuePairSet.add(new Pair<>(passageState..., false));

        return passageValuePairSet;
    }

    /**
     * Method returns state values of the given powerState.
     *
     * @param powerState The PowerState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    protected Set<Pair<String, Boolean>> powerStateValue(final PowerState powerState) {

        final Set<Pair<String, Boolean>> powerValuePairSet = new HashSet<>();
        powerValuePairSet.add(new Pair<>(powerState.getValue().toString(), false));

        return powerValuePairSet;
    }

}
