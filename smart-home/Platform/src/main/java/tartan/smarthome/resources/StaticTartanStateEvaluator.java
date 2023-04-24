package tartan.smarthome.resources;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import tartan.smarthome.resources.iotcontroller.IoTValues;
import tartan.smarthome.utils.TartanTimeUtils;

public class StaticTartanStateEvaluator implements TartanStateEvaluator {

    private String formatLogEntry(String entry) {
        Long timeStamp = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
        return "[" + sdf.format(new Date(timeStamp)) + "]: " + entry + "\n";
    }

    /**
     * Ensure the requested state is permitted. This method checks each state
     * variable to ensure that the house remains in a consistent state.
     *
     * @param inState The new state to evaluate
     * @param log The log of state evaluations
     * @return The evaluated state
     */
    @Override
    public Map<String, Object> evaluateState(Map<String, Object> inState, StringBuffer log) {

        if (!inState.containsKey(IoTValues.NOW)) {
            // Hack: Hardcode timezone to mountain time
            LocalTime now = LocalTime.now(ZoneId.of("Canada/Mountain"));
            inState.put(IoTValues.NOW, now);
        }

        // These are the state variables that reflect the current configuration of the house

        Integer tempReading = null; // the current temperature
        Integer targetTempSetting = null; // the user-desired temperature setting
        Integer humidityReading = null; // the current humidity
        Boolean doorLock = false; // state of door lock (true if locked, false if not)
        Boolean nightMode = false; // state of night mode (true if on, false if off)
        Boolean doorState = null; // the state of the door (true if open, false if closed)
        Boolean lightState = null; // the state of the light (true if on, false if off)
        Boolean proximityState = null; // the state of the proximity sensor (true of house occupied, false if vacant)
        Boolean alarmState = null; // the alarm state (true if enabled, false if disabled)
        Boolean humidifierState = null; // the humidifier state (true if on, false if off)
        Boolean heaterOnState = null; // the heater state (true if on, false if off)
        Boolean chillerOnState = null; // the chiller state (true if on, false if off)
        Boolean alarmActiveState = null; // the alarm active state (true if alarm sounding, false if alarm not sounding)
        Boolean awayTimerState = false;  // assume that the away timer did not trigger this evaluation
        String alarmPassCode = null;
        String hvacSetting = null; // the HVAC mode setting, either Heater or Chiller
        String givenPassCode = "";
        Boolean intruderDetect = null; // the state of the sensor to detect intruders (true if detected, false if not detected)

        System.out.println("Evaluating new state statically");

        Set<String> keys = inState.keySet();
        for (String key : keys) {

            if (key.equals(IoTValues.TEMP_READING)) {
                tempReading = (Integer) inState.get(key);
            } else if (key.equals(IoTValues.HUMIDITY_READING)) {
                humidityReading = (Integer) inState.get(key);
            } else if (key.equals(IoTValues.TARGET_TEMP)) {
                targetTempSetting = (Integer) inState.get(key);
            } else if (key.equals(IoTValues.HUMIDIFIER_STATE)) {
                humidifierState = (Boolean) inState.get(key);
            } else if (key.equals(IoTValues.LOCK_STATE)) {
                doorLock = (Boolean) inState.get(key);
            } else if (key.equals(IoTValues.NIGHT_MODE)) {
                nightMode = (Boolean) inState.get(key);
            } else if (key.equals(IoTValues.DOOR_STATE)) {
                doorState = (Boolean) inState.get(key);
            } else if (key.equals(IoTValues.LIGHT_STATE)) {
                lightState = (Boolean) inState.get(key);
            } else if (key.equals(IoTValues.PROXIMITY_STATE)) {
                proximityState = (Boolean) inState.get(key);
            } else if (key.equals(IoTValues.ALARM_STATE)) {
                alarmState = (Boolean) inState.get(key);
            } else if (key.equals(IoTValues.HEATER_STATE)) {
                heaterOnState = (Boolean) inState.get(key);
            } else if (key.equals(IoTValues.CHILLER_STATE)) {
                chillerOnState = (Boolean) inState.get(key);
            } else if (key.equals(IoTValues.HVAC_MODE)) {
                hvacSetting = (String) inState.get(key);
            } else if (key.equals(IoTValues.ALARM_PASSCODE)) {
                alarmPassCode = (String) inState.get(key);
            } else if (key.equals(IoTValues.GIVEN_PASSCODE)) {
                givenPassCode = (String) inState.get(key);
            } else if (key.equals(IoTValues.AWAY_TIMER)) {
                // This is a hack!
                awayTimerState = (Boolean) inState.getOrDefault(key, false);
            } else if (key.equals(IoTValues.ALARM_ACTIVE)) {
                alarmActiveState = (Boolean) inState.get(key);
            } else if (key.equals(IoTValues.INTRUDER_DETECT)){
                intruderDetect = (Boolean) inState.get(key);
            }
        }

        // Set night mode
        LocalTime now = (LocalTime) inState.get(IoTValues.NOW);
        LocalTime nightStartTime = (LocalTime) inState.get(IoTValues.NIGHT_START_TIME);
        LocalTime nightEndTime = (LocalTime) inState.get(IoTValues.NIGHT_END_TIME);
        nightMode = TartanTimeUtils.isBetween(now, nightStartTime, nightEndTime);

        if (lightState) {
            // The light was activated
            if (!proximityState) {
                log.append(formatLogEntry("Cannot turn on light because user not home"));
                    lightState = false;
            }
            else {
                log.append(formatLogEntry("Light on"));
            }
        } else {
            log.append(formatLogEntry("Light off"));
        }

        // If proximity is true, unlock the door
        if  (proximityState) {
            doorLock = false;
            log.append(formatLogEntry("Door is unlocked"));
        } else {
            log.append(formatLogEntry("Door is locked"));
        }


        // The door is now open
        if (doorState) {        
            if (!proximityState && alarmState) {

                // door open and no one home and the alarm is set - sound alarm
                log.append(formatLogEntry("Break in detected: Activating alarm"));
                alarmActiveState = true;
            }
            // House vacant, close the door
            else if (!proximityState) {
                // close the door
                doorState = false;
                log.append(formatLogEntry("Closed door because house vacant"));
            } else {
                log.append(formatLogEntry("Door open"));
            }

            // The door is open the alarm is to be set and somebody is home - this is not
            // allowed so discard the processStateUpdate

        } else {
            // the door is closed - if the house is suddenly occupied this is a break-in
            if (alarmState && proximityState) {
                log.append(formatLogEntry("Break in detected: Activating alarm"));
                alarmActiveState = true;
            } else {
                log.append(formatLogEntry("Closed door"));
            }
        }
        
        // Auto lock the house
        if (awayTimerState == true) {
            lightState = false;
            doorState = false;
            alarmState = true;
            awayTimerState = false;
        }

        // the user has arrived
        if (proximityState) {
            log.append(formatLogEntry("House is occupied"));
            // if the alarm has been disabled, then turn on the light for the user

            if (!lightState && !alarmState) {
                lightState = true;
                log.append(formatLogEntry("Turning on light"));
            }
            
        }

        // set the alarm
        if (alarmState) {
            log.append(formatLogEntry("Alarm enabled"));
            

        } else if (!alarmState) { // attempt to disable alarm

            if (!proximityState) { 
                alarmState = true;

                log.append(formatLogEntry("Cannot disable the alarm, house is empty"));
            }

            if (alarmActiveState) {
                if (givenPassCode.length()>0  && givenPassCode.compareTo(alarmPassCode) < 0) {
                    log.append(formatLogEntry("Cannot disable alarm, invalid passcode given"));
                    alarmState = true;

                } else {
                    log.append(formatLogEntry("Correct passcode entered, disabled alarm"));
                    alarmActiveState = false;
                }
            }
        }

        if (!alarmState) {
            log.append(formatLogEntry("Alarm disabled"));
        }

        if (!alarmState) { // alarm disabled
            alarmActiveState = false;
        }       
        

        // determine if the alarm should sound. There are two cases
        // 1. the door is opened when no one is home
        // 2. the house is suddenly occupied
        try {
            if ((alarmState && !doorState && proximityState) || (alarmState && doorState && !proximityState)) {
                log.append(formatLogEntry("Activating alarm"));
                alarmActiveState = true;
            }
        } catch (NullPointerException npe) {
            // Not enough information to evaluate alarm
            log.append(formatLogEntry("Warning: Not enough information to evaluate alarm"));
        }

       
        // Is the heater needed?
        if (tempReading < targetTempSetting) {
            log.append(formatLogEntry("Turning on heater, target temperature = " + targetTempSetting
                    + "F, current temperature = " + tempReading + "F"));
            heaterOnState = true;

            // Heater already on
        } else {
            // Heater not needed
            heaterOnState = false;
        }

        if (tempReading > targetTempSetting) {
            // Is the heater needed?
            if (chillerOnState != null) {
                if (!chillerOnState) {
                    log.append(formatLogEntry("Turning on air conditioner target temperature = " + targetTempSetting
                            + "F, current temperature = " + tempReading + "F"));
                    chillerOnState = true;
                } // AC already on
            }
        }
        // AC not needed
        else {
            chillerOnState = false;
        }
        

        if (chillerOnState) {
            hvacSetting = "Chiller";
        } else if (heaterOnState) {
            hvacSetting = "Heater";
        }
        // manage the HVAC control

        if (hvacSetting.equals("Heater")) {

            if (chillerOnState == true) {
                log.append(formatLogEntry("Turning off air conditioner"));
            }

            chillerOnState = false; // can't run AC
            humidifierState = false; // can't run dehumidifier with heater
        }

        if (hvacSetting.equals("Chiller")) {

            if (heaterOnState == true) {
                log.append(formatLogEntry("Turning off heater"));
            }

            heaterOnState = false; // can't run heater when the A/C is on
        }
        
        if (humidifierState && hvacSetting.equals("Chiller")) {
            log.append(formatLogEntry("Enabled Dehumidifier"));
        } else {
            log.append(formatLogEntry("Automatically disabled dehumidifier when running heater"));
            humidifierState = false;
        }


        // If nightmode is on, lock the door
        if  (nightMode) {
            log.append(formatLogEntry("Night mode is on"));
            // There is an attempt to unlock the door
            if (!doorLock) {
                log.append(formatLogEntry("Door cannot be unlocked at night, relocking door"));
                doorLock = true;
            }
        } else {
            log.append(formatLogEntry("Night mode is off"));
        }
      
        //If intruder is detected by the sensor, lock the door
        // and send "possible intruder detected" signal to access panels
        if (intruderDetect){
            if (!doorLock){
                doorLock = true;
            }
            log.append(formatLogEntry("Possible intruder detected"));
        } else {
            // Intruder no longer detected, send all clear message
            log.append(formatLogEntry("All clear"));
        }


        if (doorLock) {
            log.append(formatLogEntry("Door is locked"));
        } else {
            log.append(formatLogEntry("Door is unlocked"));
        }
        

        Map<String, Object> newState = new Hashtable<>();
        newState.put(IoTValues.NIGHT_MODE, nightMode);
        newState.put(IoTValues.LOCK_STATE, doorLock);
        newState.put(IoTValues.DOOR_STATE, doorState);
        newState.put(IoTValues.AWAY_TIMER, awayTimerState);
        newState.put(IoTValues.LIGHT_STATE, lightState);
        newState.put(IoTValues.PROXIMITY_STATE, proximityState);
        newState.put(IoTValues.ALARM_STATE, alarmState);
        newState.put(IoTValues.HUMIDIFIER_STATE, humidifierState);
        newState.put(IoTValues.HEATER_STATE, heaterOnState);
        newState.put(IoTValues.CHILLER_STATE, chillerOnState);
        newState.put(IoTValues.ALARM_ACTIVE, alarmActiveState);
        newState.put(IoTValues.HVAC_MODE, hvacSetting);
        newState.put(IoTValues.ALARM_PASSCODE, alarmPassCode);
        newState.put(IoTValues.GIVEN_PASSCODE, givenPassCode);
        newState.put(IoTValues.INTRUDER_DETECT, intruderDetect);
        
        return newState; 
    }
}