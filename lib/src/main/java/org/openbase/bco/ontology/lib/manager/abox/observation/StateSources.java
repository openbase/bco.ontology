package org.openbase.bco.ontology.lib.manager.abox.observation;

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

import org.apache.poi.util.NotImplemented;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.utility.rdf.RdfNodeObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openbase.type.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;
import org.openbase.type.domotic.state.ActionStateType.ActionState;
import org.openbase.type.domotic.state.ActivationStateType.ActivationState;
import org.openbase.type.domotic.state.ActivityStateType.ActivityState;
import org.openbase.type.domotic.state.AlarmStateType.AlarmState;
import org.openbase.type.domotic.state.BatteryStateType.BatteryState;
import org.openbase.type.domotic.state.BlindStateType.BlindState;
import org.openbase.type.domotic.state.BrightnessStateType.BrightnessState;
import org.openbase.type.domotic.state.ButtonStateType.ButtonState;
import org.openbase.type.domotic.state.ColorStateType.ColorState;
import org.openbase.type.domotic.state.ContactStateType.ContactState;
import org.openbase.type.domotic.state.DoorStateType.DoorState;
import org.openbase.type.domotic.state.EmphasisStateType.EmphasisState;
import org.openbase.type.domotic.state.EnablingStateType.EnablingState;
import org.openbase.type.domotic.state.HandleStateType.HandleState;
import org.openbase.type.domotic.state.IlluminanceStateType.IlluminanceState;
import org.openbase.type.domotic.state.InventoryStateType.InventoryState;
import org.openbase.type.domotic.state.MotionStateType.MotionState;
import org.openbase.type.domotic.state.PassageStateType.PassageState;
import org.openbase.type.domotic.state.PowerConsumptionStateType.PowerConsumptionState;
import org.openbase.type.domotic.state.PowerStateType.PowerState;
import org.openbase.type.domotic.state.PresenceStateType.PresenceState;
import org.openbase.type.domotic.state.RFIDStateType.RFIDState;
import org.openbase.type.domotic.state.SmokeStateType.SmokeState;
import org.openbase.type.domotic.state.StandbyStateType.StandbyState;
import org.openbase.type.domotic.state.SwitchStateType.SwitchState;
import org.openbase.type.domotic.state.TamperStateType.TamperState;
import org.openbase.type.domotic.state.TemperatureStateType.TemperatureState;
import org.openbase.type.domotic.state.UserTransitStateType.UserTransitState;
import org.openbase.type.domotic.state.WindowStateType.WindowState;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class implements methods to provide the state values of the state sources. A state source is part of each individual state type. For example is the
 * batteryState a individual state type. All state types contains at least one (sub) state source, like the batterState the sources "level" (0-100 energy) and
 * "state" (OK, CRITICAL, ...).
 * Additionally, the individual state types are assigned to their appropriate service types.
 * Further individual state types and their (sub) state sources should be implemented here. Furthermore, a suitable entry in the assignation must be done. If
 * any changes of the current BCO states were appeared, the new value can be easily pushed to the declared list in the affected method. Only in case of a
 * literal data (e.g. id's, names, etc.) the ontology model must be adapted, whether the literal data type (e.g. "Hue") isn't available. Take a look at the
 * available methods in this class for examples.
 *
 * @author agatting on 22.02.17.
 */
@SuppressWarnings("checkstyle:multiplestringliterals")
public class StateSources {

    private static final Logger LOGGER = LoggerFactory.getLogger(StateSources.class);

    /**
     * Method identifies the input service type and assigns the input state object.
     *
     * @param serviceType is the service type to identify the state object.
     * @param stateObject is the state object, which contains the needed state value(s).
     *
     * @return ontology state values of the input state object, which include the state values itself and literal or resource information for each entry. By
     * incomplete or missing information NULL is returned.
     */
    List<RdfNodeObject> identifyStateType(final ServiceType serviceType, final Object stateObject) {

        switch (serviceType) {
            case ACTIVATION_STATE_SERVICE:
                return activationStateSources((ActivationState) stateObject);
            case BATTERY_STATE_SERVICE:
                return batteryStateSources((BatteryState) stateObject);
            case BLIND_STATE_SERVICE:
                return blindStateSources((BlindState) stateObject);
            case BRIGHTNESS_STATE_SERVICE:
                return brightnessStateSources((BrightnessState) stateObject);
            case BUTTON_STATE_SERVICE:
                return buttonStateSources((ButtonState) stateObject);
            case COLOR_STATE_SERVICE:
                return colorStateSources((ColorState) stateObject);
            case CONTACT_STATE_SERVICE:
                return contactStateSources((ContactState) stateObject);
            case DOOR_STATE_SERVICE:
                return doorStateSources((DoorState) stateObject);
            case EARTHQUAKE_ALARM_STATE_SERVICE:
                return null;
            case EMPHASIS_STATE_SERVICE:
                return emphasisStateSources((EmphasisState) stateObject);
            case FIRE_ALARM_STATE_SERVICE:
                return null;
            case HANDLE_STATE_SERVICE:
                return handleStateSources((HandleState) stateObject);
            case ILLUMINANCE_STATE_SERVICE:
                return illuminanceStateSources((IlluminanceState) stateObject);
            case INTRUSION_ALARM_STATE_SERVICE:
                return null;
            case MEDICAL_EMERGENCY_ALARM_STATE_SERVICE:
                return null;
            case MOTION_STATE_SERVICE:
                return motionStateSources((MotionState) stateObject);
            case PASSAGE_STATE_SERVICE:
                return passageStateSources((PassageState) stateObject);
            case POWER_CONSUMPTION_STATE_SERVICE:
                return powerConsumptionStateSources((PowerConsumptionState) stateObject);
            case POWER_STATE_SERVICE:
                return powerStateSources((PowerState) stateObject);
            case PRESENCE_STATE_SERVICE:
                return presenceStateSources((PresenceState) stateObject);
            case R_F_I_D_STATE_SERVICE:
                return rfidStateSources((RFIDState) stateObject);
            case SMOKE_ALARM_STATE_SERVICE:
                return null;
            case SMOKE_STATE_SERVICE:
                return smokeStateSources((SmokeState) stateObject);
            case STANDBY_STATE_SERVICE:
                return standbyStateSources((StandbyState) stateObject);
            case SWITCH_STATE_SERVICE:
                return switchStateSources((SwitchState) stateObject);
            case TAMPER_STATE_SERVICE:
                return tamperStateSources((TamperState) stateObject);
            case TARGET_TEMPERATURE_STATE_SERVICE:
                return null;
            case TEMPERATURE_ALARM_STATE_SERVICE:
                return null;
            case TEMPERATURE_STATE_SERVICE:
                return temperatureStateSources((TemperatureState) stateObject);
            case TEMPEST_ALARM_STATE_SERVICE:
                return null;
            case ACTIVITY_MULTI_STATE_SERVICE:
                return activityStateSources((List<ActivityState>) stateObject);
            case USER_TRANSIT_STATE_SERVICE:
                return userTransitStateSources((UserTransitState) stateObject);
            case WATER_ALARM_STATE_SERVICE:
                return null;
            case WINDOW_STATE_SERVICE:
                return windowStateSources((WindowState) stateObject);
            case UNKNOWN:
                LOGGER.error("Could not assign state object, because input service type is UNKNOWN!");
                return null;
            // todo: how to handle multi activation states?
            default:
                LOGGER.error("Could not identify stateType. Please check implementation or rather integrate " + serviceType + " to method body.");
                return null;
        }
    }

    /**
     * Method returns the state source(s) result(s) (contains state value(s)) of the input actionState.
     *
     * @param actionState is the ActionState.
     *
     * @return state source(s) result(s) of the input state.
     */
    private List<RdfNodeObject> actionStateSources(final ActionState actionState) {

        final List<RdfNodeObject> stateSources = new ArrayList<>();
        final String actionStateVal = actionState.getValue().toString();
        stateSources.add(new RdfNodeObject(new ArrayList<String>() {{
            add(actionStateVal);
        }}, false));

        return stateSources;
    }

    /**
     * Method returns the state source(s) result(s) (contains state value(s)) of the input activationState.
     *
     * @param activationState The ActivationState.
     *
     * @return state source(s) result(s) of the input state.
     */
    private List<RdfNodeObject> activationStateSources(final ActivationState activationState) {

        final List<RdfNodeObject> stateSources = new ArrayList<>();
        final String activationStateVal = activationState.getValue().toString();
        stateSources.add(new RdfNodeObject(new ArrayList<String>() {{
            add(activationStateVal);
        }}, false));

        return stateSources;
    }

    /**
     * Method returns the state source(s) result(s) (contains state value(s)) of the input alarmState.
     *
     * @param alarmState The AlarmState.
     *
     * @return state source(s) result(s) of the input state.
     */
    private List<RdfNodeObject> alarmStateSources(final AlarmState alarmState) {

        final List<RdfNodeObject> stateSources = new ArrayList<>();
        final String alarmStateVal = alarmState.getValue().toString();
        stateSources.add(new RdfNodeObject(new ArrayList<String>() {{
            add(alarmStateVal);
        }}, false));

        return stateSources;
    }

    /**
     * Method returns the state source(s) result(s) (contains state value(s)) of the input batteryState.
     *
     * @param batteryState The BatteryState.
     *
     * @return state source(s) result(s) of the input state.
     */
    private List<RdfNodeObject> batteryStateSources(final BatteryState batteryState) {

        final List<RdfNodeObject> stateSources = new ArrayList<>();
        final String batteryStateVal = batteryState.getValue().toString();
        stateSources.add(new RdfNodeObject(new ArrayList<String>() {{
            add(batteryStateVal);
        }}, false));
        final String batteryLevelVal = "\"" + batteryState.getLevel() + "\"^^NS:Percent";
        stateSources.add(new RdfNodeObject(new ArrayList<String>() {{
            add(batteryLevelVal);
        }}, true));

        return stateSources;
    }

    /**
     * Method returns the state source(s) result(s) (contains state value(s)) of the input blindState.
     *
     * @param blindState The BlindState.
     *
     * @return state source(s) result(s) of the input state.
     */
    private List<RdfNodeObject> blindStateSources(final BlindState blindState) {

        final List<RdfNodeObject> stateSources = new ArrayList<>();

        final String blindMovementStateVal = blindState.getValue().toString();
        stateSources.add(new RdfNodeObject(new ArrayList<String>() {{
            add(blindMovementStateVal);
        }}, false));
        final String blindOpeningRationVal = "\"" + blindState.getOpeningRatio() + "\"^^NS:Percent";
        stateSources.add(new RdfNodeObject(new ArrayList<String>() {{
            add(blindOpeningRationVal);
        }}, true));

        return stateSources;
    }

    /**
     * Method returns the state source(s) result(s) (contains state value(s)) of the input brightnessState.
     *
     * @param brightnessState The BrightnessState.
     *
     * @return state source(s) result(s) of the input state. Return null if dataUnit could not be identified.
     */
    private List<RdfNodeObject> brightnessStateSources(final BrightnessState brightnessState) {

        final List<RdfNodeObject> stateSources = new ArrayList<>();
        final BrightnessState.DataUnit dataUnit = brightnessState.getBrightnessDataUnit();

        switch (dataUnit) {
            case PERCENT:
                final String brightnessVal = "\"" + brightnessState.getBrightness() + "\"^^NS:Percent";
                stateSources.add(new RdfNodeObject(new ArrayList<String>() {{
                    add(brightnessVal);
                }}, true));
                break;
            case UNKNOWN:
                LOGGER.warn("Dropped brightness state value, cause dataUnit is UNKNOWN.");
                return null;
            default:
                LOGGER.warn("DataUnit of brightness state could not be detected. Please add " + dataUnit + " to ontologyManager implementation.");
                return null;
        }
        return stateSources;
    }

    /**
     * Method returns the state source(s) result(s) (contains state value(s)) of the input buttonState.
     *
     * @param buttonState The ButtonState.
     *
     * @return state source(s) result(s) of the input state.
     */
    private List<RdfNodeObject> buttonStateSources(final ButtonState buttonState) {

        final List<RdfNodeObject> stateSources = new ArrayList<>();
        final String buttonStateVal = buttonState.getValue().toString();
        stateSources.add(new RdfNodeObject(new ArrayList<String>() {{
            add(buttonStateVal);
        }}, false));

        return stateSources;
    }

    /**
     * Method returns the state source(s) result(s) (contains state value(s)) of the input colorState.
     *
     * @param colorState The ColorState.
     *
     * @return state source(s) result(s) of the input state.
     */
    private List<RdfNodeObject> colorStateSources(final ColorState colorState) {

        final List<RdfNodeObject> stateSources = new ArrayList<>();

        if (colorState.getColor().hasHsbColor()) {
            final String hue = "\"" + colorState.getColor().getHsbColor().getHue() + "\"^^NS:Hue";
            final String saturation = "\"" + colorState.getColor().getHsbColor().getSaturation() + "\"^^NS:Saturation";
            final String brightness = "\"" + colorState.getColor().getHsbColor().getBrightness() + "\"^^NS:Brightness";

            stateSources.add(new RdfNodeObject(new ArrayList<String>() {{
                addAll(Arrays.asList(hue, saturation, brightness));
            }}, true));
        } else if (colorState.getColor().hasRgbColor()) {
            final int red = ((int) (colorState.getColor().getRgbColor().getRed() * 255));
            final int green = ((int) (colorState.getColor().getRgbColor().getGreen() * 255));
            final int blue = ((int) (colorState.getColor().getRgbColor().getBlue() * 255));

            final float[] hsb = Color.RGBtoHSB(red, green, blue, null);
            final String hue = "\"" + hsb[0] + "\"^^NS:Hue";
            final String saturation = "\"" + hsb[1] + "\"^^NS:Saturation";
            final String brightness = "\"" + hsb[2] + "\"^^NS:Brightness";

            stateSources.add(new RdfNodeObject(new ArrayList<String>() {{
                addAll(Arrays.asList(brightness, saturation, hue));
            }}, true));
        } else {
            LOGGER.error("Could not set colorValue of colorState. Color is not set!");
        }

        return stateSources;
    }

    /**
     * Method returns the state source(s) result(s) (contains state value(s)) of the input contactState.
     *
     * @param contactState The ContactState.
     *
     * @return state source(s) result(s) of the input state.
     */
    private List<RdfNodeObject> contactStateSources(final ContactState contactState) {

        final List<RdfNodeObject> stateSources = new ArrayList<>();
        final String contactStateVal = contactState.getValue().toString();
        stateSources.add(new RdfNodeObject(new ArrayList<String>() {{
            add(contactStateVal);
        }}, false));

        return stateSources;
    }

    /**
     * Method returns the state source(s) result(s) (contains state value(s)) of the input doorState.
     *
     * @param doorState The DoorState.
     *
     * @return state source(s) result(s) of the input state.
     */
    private List<RdfNodeObject> doorStateSources(final DoorState doorState) {

        final List<RdfNodeObject> stateSources = new ArrayList<>();
        final String doorStateVal = doorState.getValue().toString();
        stateSources.add(new RdfNodeObject(new ArrayList<String>() {{
            add(doorStateVal);
        }}, false));

        return stateSources;
    }

    /**
     * Method returns the state source(s) result(s) (contains state value(s)) of the input emphasisState.
     *
     * @param emphasisState The EnablingState.
     *
     * @return state source(s) result(s) of the input state.
     */
    private List<RdfNodeObject> emphasisStateSources(final EmphasisState emphasisState) {

        final List<RdfNodeObject> stateSources = new ArrayList<>();
        final String comfortVal = "\"" + emphasisState.getComfort() + "\"^^xsd:double";
        stateSources.add(new RdfNodeObject(new ArrayList<String>() {{
            add(comfortVal);
        }}, true));
        final String economyVal = "\"" + emphasisState.getEconomy() + "\"^^xsd:double";
        stateSources.add(new RdfNodeObject(new ArrayList<String>() {{
            add(economyVal);
        }}, true));
        final String securityVal = "\"" + emphasisState.getSecurity() + "\"^^xsd:double";
        stateSources.add(new RdfNodeObject(new ArrayList<String>() {{
            add(securityVal);
        }}, true));

        return stateSources;
    }

    /**
     * Method returns the state source(s) result(s) (contains state value(s)) of the input enablingState.
     *
     * @param enablingState The EnablingState.
     *
     * @return state source(s) result(s) of the input state.
     */
    private List<RdfNodeObject> enablingStateSources(final EnablingState enablingState) {

        final List<RdfNodeObject> stateSources = new ArrayList<>();
        final String enablingStateVal = enablingState.getValue().toString();
        stateSources.add(new RdfNodeObject(new ArrayList<String>() {{
            add(enablingStateVal);
        }}, false));

        return stateSources;
    }

    /**
     * Method returns the state source(s) result(s) (contains state value(s)) of the input handleState.
     *
     * @param handleState The HandleState.
     *
     * @return state source(s) result(s) of the input state.
     */
    private List<RdfNodeObject> handleStateSources(final HandleState handleState) {

        final List<RdfNodeObject> stateSources = new ArrayList<>();
        final String handlePositionVal = "\"" + handleState.getPosition() + "\"^^xsd:double";
        stateSources.add(new RdfNodeObject(new ArrayList<String>() {{
            add(handlePositionVal);
        }}, true));

        return stateSources;
    }

    /**
     * Method returns the state source(s) result(s) (contains state value(s)) of the input illuminanceState.
     *
     * @param illuminanceState The IlluminanceState.
     *
     * @return state source(s) result(s) of the input state.
     */
    private List<RdfNodeObject> illuminanceStateSources(final IlluminanceState illuminanceState) {

        final List<RdfNodeObject> stateSources = new ArrayList<>();
        final IlluminanceState.DataUnit dataUnit = illuminanceState.getIlluminanceDataUnit();

        switch (dataUnit) {
            case LUX:
                final String illuminanceStateVal = "\"" + illuminanceState.getIlluminance() + "\"^^NS:Lux";
                stateSources.add(new RdfNodeObject(new ArrayList<String>() {{
                    add(illuminanceStateVal);
                }}, true));
                break;
            default:
                LOGGER.warn("DataUnit of intensity state could not be detected. Please add " + dataUnit + " to ontologyManager implementation.");
                return null;
        }
        return stateSources;
    }

    /**
     * Method returns the state source(s) result(s) (contains state value(s)) of the input inventoryState.
     *
     * @param inventoryState The InventoryState.
     *
     * @return state source(s) result(s) of the input state.
     */
    @NotImplemented
    private List<RdfNodeObject> inventoryStateValue(final InventoryState inventoryState) {

        final List<RdfNodeObject> stateSources = new ArrayList<>();
        final String inventoryStateVal = inventoryState.getValue().toString();
        stateSources.add(new RdfNodeObject(new ArrayList<String>() {{
            add(inventoryStateVal);
        }}, false));
//        final String inventoryLocationId = "\"" + inventoryState.getLocationId() + "\"^^xsd:string";
//        final String inventoryOwnerId = "\"" + inventoryState.getOwnerId() + "\"^^xsd:string";
//        stateSources.add(new RdfNodeObject(new ArrayList<String>() {{add(inventoryLocationId);}}, true));
//        stateSources.add(new RdfNodeObject(new ArrayList<String>() {{add(inventoryOwnerId);}}, true));

        return stateSources;
    }

    /**
     * Method returns the state source(s) result(s) (contains state value(s)) of the input motionState.
     *
     * @param motionState The MotionState.
     *
     * @return state source(s) result(s) of the input state.
     */
    private List<RdfNodeObject> motionStateSources(final MotionState motionState) {

        final List<RdfNodeObject> stateSources = new ArrayList<>();
        final String motionStateVal = motionState.getValue().toString();
        stateSources.add(new RdfNodeObject(new ArrayList<String>() {{
            add(motionStateVal);
        }}, false));

        return stateSources;
    }

    /**
     * Method returns the state source(s) result(s) (contains state value(s)) of the input passageState.
     *
     * @param passageState The PassageState.
     *
     * @return state source(s) result(s) of the input state.
     */
    @NotImplemented
    private List<RdfNodeObject> passageStateSources(final PassageState passageState) {

        final List<RdfNodeObject> stateSources = new ArrayList<>();

        return stateSources;
    }

    /**
     * Method returns the state source(s) result(s) (contains state value(s)) of the input powerConsumptionState.
     *
     * @param powerConsumptionState The PowerConsumptionState.
     *
     * @return state source(s) result(s) of the input state.
     */
    private List<RdfNodeObject> powerConsumptionStateSources(final PowerConsumptionState powerConsumptionState) {

        final List<RdfNodeObject> stateSources = new ArrayList<>();
        final String voltageVal = "\"" + powerConsumptionState.getVoltage() + "\"^^NS:Voltage";
        final String consumptionVal = "\"" + powerConsumptionState.getConsumption() + "\"^^NS:Watt";
        final String currentVal = "\"" + powerConsumptionState.getCurrent() + "\"^^NS:Ampere";
        stateSources.add(new RdfNodeObject(new ArrayList<String>() {{
            addAll(Arrays.asList(voltageVal, consumptionVal, currentVal));
        }}, true));

        return stateSources;
    }

    /**
     * Method returns the state source(s) result(s) (contains state value(s)) of the input powerState.
     *
     * @param powerState The PowerState.
     *
     * @return state source(s) result(s) of the input state.
     */
    private List<RdfNodeObject> powerStateSources(final PowerState powerState) {

        final List<RdfNodeObject> stateSources = new ArrayList<>();
        final String powerStateVal = powerState.getValue().toString();
        stateSources.add(new RdfNodeObject(new ArrayList<String>() {{
            add(powerStateVal);
        }}, false));

        return stateSources;
    }

    /**
     * Method returns the state source(s) result(s) (contains state value(s)) of the input presenceState.
     *
     * @param presenceState The PresenceState.
     *
     * @return state source(s) result(s) of the input state.
     */
    private List<RdfNodeObject> presenceStateSources(final PresenceState presenceState) {

        final List<RdfNodeObject> stateSources = new ArrayList<>();
        final String presenceStateVal = presenceState.getValue().toString();
        stateSources.add(new RdfNodeObject(new ArrayList<String>() {{
            add(presenceStateVal);
        }}, false));

        return stateSources;
    }

    /**
     * Method returns the state source(s) result(s) (contains state value(s)) of the input rfidState.
     *
     * @param rfidState The RFIDState.
     *
     * @return state source(s) result(s) of the input state.
     */
    private List<RdfNodeObject> rfidStateSources(final RFIDState rfidState) {

        final List<RdfNodeObject> stateSources = new ArrayList<>();
        final String rfidData = "\"" + rfidState.getData().toString() + "\"^^xsd:string";
        stateSources.add(new RdfNodeObject(new ArrayList<String>() {{
            add(rfidData);
        }}, true));

        return stateSources;
    }

    /**
     * Method returns the state source(s) result(s) (contains state value(s)) of the input smokeState.
     *
     * @param smokeState The SmokeState.
     *
     * @return state source(s) result(s) of the input state.
     */
    private List<RdfNodeObject> smokeStateSources(final SmokeState smokeState) {

        final List<RdfNodeObject> stateSources = new ArrayList<>();
        final String smokeStateVal = smokeState.getValue().toString();
        stateSources.add(new RdfNodeObject(new ArrayList<String>() {{
            add(smokeStateVal);
        }}, false));
        final String smokeLevelVal = "\"" + smokeState.getSmokeLevel() + "\"^^NS:Percent";
        stateSources.add(new RdfNodeObject(new ArrayList<String>() {{
            add(smokeLevelVal);
        }}, true));

        return stateSources;
    }

    /**
     * Method returns the state source(s) result(s) (contains state value(s)) of the input standbyState.
     *
     * @param standbyState The StandbyState.
     *
     * @return state source(s) result(s) of the input state.
     */
    private List<RdfNodeObject> standbyStateSources(final StandbyState standbyState) {

        final List<RdfNodeObject> stateSources = new ArrayList<>();
        final String standbyStateVal = standbyState.getValue().toString();
        stateSources.add(new RdfNodeObject(new ArrayList<String>() {{
            add(standbyStateVal);
        }}, false));

        return stateSources;
    }

    /**
     * Method returns the state source(s) result(s) (contains state value(s)) of the input switchState.
     *
     * @param switchState The SwitchState.
     *
     * @return state source(s) result(s) of the input state.
     */
    private List<RdfNodeObject> switchStateSources(final SwitchState switchState) {

        final List<RdfNodeObject> stateSources = new ArrayList<>();
        final String switchPositionVal = "\"" + switchState.getPosition() + "\"^^xsd:double";
        stateSources.add(new RdfNodeObject(new ArrayList<String>() {{
            add(switchPositionVal);
        }}, true));

        return stateSources;
    }

    /**
     * Method returns the state source(s) result(s) (contains state value(s)) of the input tamperState.
     *
     * @param tamperState The TamperState.
     *
     * @return state source(s) result(s) of the input state.
     */
    private List<RdfNodeObject> tamperStateSources(final TamperState tamperState) {

        final List<RdfNodeObject> stateSources = new ArrayList<>();
        final String tamperStateVal = tamperState.getValue().toString();
        stateSources.add(new RdfNodeObject(new ArrayList<String>() {{
            add(tamperStateVal);
        }}, false));

        return stateSources;
    }

    /**
     * Method returns the state source(s) result(s) (contains state value(s)) of the input temperatureState.
     *
     * @param temperatureState The TemperatureState.
     *
     * @return state source(s) result(s) of the input state.
     */
    private List<RdfNodeObject> temperatureStateSources(final TemperatureState temperatureState) {

        final List<RdfNodeObject> stateSources = new ArrayList<>();
        final TemperatureState.DataUnit dataUnit = temperatureState.getTemperatureDataUnit();
        double temperature;
        String temperatureVal;

        switch (dataUnit) {
            case CELSIUS:
                temperature = temperatureState.getTemperature();
                temperatureVal = "\"" + temperature + "\"^^NS:Celsius";
                stateSources.add(new RdfNodeObject(new ArrayList<String>() {{
                    add(temperatureVal);
                }}, true));
                break;
            case FAHRENHEIT:
                temperature = ((temperatureState.getTemperature() - OntConfig.FREEZING_POINT_FAHRENHEIT) / OntConfig.FAHRENHEIT_DIVISOR);
                temperatureVal = "\"" + temperature + "\"^^NS:Celsius";
                stateSources.add(new RdfNodeObject(new ArrayList<String>() {{
                    add(temperatureVal);
                }}, true));
                break;
            case KELVIN:
                temperature = temperatureState.getTemperature() - OntConfig.ABSOLUTE_ZERO_POINT_CELSIUS;
                temperatureVal = "\"" + temperature + "\"^^NS:Celsius";
                stateSources.add(new RdfNodeObject(new ArrayList<String>() {{
                    add(temperatureVal);
                }}, true));
                break;
            case UNKNOWN:
                LOGGER.warn("Dropped temperature state value, cause dataUnit is UNKNOWN.");
                return null;
            default:
                LOGGER.warn("DataUnit of temperature state could not be detected. Please add " + dataUnit + " to ontologyManager implementation.");
                return null;
        }

        return stateSources;
    }

    /**
     * Method returns the state source(s) result(s) (contains state value(s)) of the input activityState.
     *
     * @param activityStateList The ActivityState List.
     *
     * @return state source(s) result(s) of the input state.
     */
    private List<RdfNodeObject> activityStateSources(final List<ActivityState> activityStateList) {

        final List<RdfNodeObject> stateSources = new ArrayList<>();
        for (final ActivityState activityState : activityStateList) {
            final String activityIdVal = "\"" + activityState.getActivityId() + "\"^^NS:ActivityId";
            stateSources.add(new RdfNodeObject(new ArrayList<String>() {{
                add(activityIdVal);
            }}, true));
        }
        return stateSources;
    }

    /**
     * Method returns the state source(s) result(s) (contains state value(s)) of the input userTransitState.
     *
     * @param userTransitState The UserTransitState.
     *
     * @return state source(s) result(s) of the input state.
     */
    private List<RdfNodeObject> userTransitStateSources(final UserTransitState userTransitState) {

        final List<RdfNodeObject> stateSources = new ArrayList<>();
        final String userTransitStateVal = userTransitState.getValue().toString();
        stateSources.add(new RdfNodeObject(new ArrayList<String>() {{
            add(userTransitStateVal);
        }}, false));

        return stateSources;
    }

    /**
     * Method returns the state source(s) result(s) (contains state value(s)) of the input windowState.
     *
     * @param windowState The WindowState.
     *
     * @return state source(s) result(s) of the input state.
     */
    private List<RdfNodeObject> windowStateSources(final WindowState windowState) {

        final List<RdfNodeObject> stateSources = new ArrayList<>();
        final String windowStateVal = windowState.getValue().toString();
        stateSources.add(new RdfNodeObject(new ArrayList<String>() {{
            add(windowStateVal);
        }}, false));

        return stateSources;
    }
}
