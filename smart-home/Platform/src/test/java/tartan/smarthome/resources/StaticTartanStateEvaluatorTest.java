package tartan.smarthome.resources;

import org.junit.Test;
import tartan.smarthome.resources.iotcontroller.IoTConnectManager;
import tartan.smarthome.resources.iotcontroller.IoTConnection;
import tartan.smarthome.resources.iotcontroller.IoTValues;

import java.time.LocalTime;
import java.util.Hashtable;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class StaticTartanStateEvaluatorTest {

    public Map<String, Object> defaultState(){
        Map<String, Object> state = new Hashtable<>();
        state.put(IoTValues.TEMP_READING, 70);
        state.put(IoTValues.TARGET_TEMP, 70);
        state.put(IoTValues.DOOR_STATE, false);
        state.put(IoTValues.LOCK_STATE, false);
        state.put(IoTValues.AWAY_TIMER, false);
        state.put(IoTValues.HUMIDIFIER_STATE, false);
        state.put(IoTValues.HEATER_STATE, false);
        state.put(IoTValues.CHILLER_STATE, false);
        state.put(IoTValues.ALARM_STATE, false);
        state.put(IoTValues.ALARM_ACTIVE, false);
        state.put(IoTValues.HVAC_MODE, "Heater");
        state.put(IoTValues.ALARM_PASSCODE, "1234");
        state.put(IoTValues.GIVEN_PASSCODE, "");
        state.put(IoTValues.PROXIMITY_STATE, false);
        state.put(IoTValues.LIGHT_STATE, false);
        state.put(IoTValues.INTRUDER_DETECT, false);
        state.put(IoTValues.NIGHT_START_TIME, LocalTime.of(21, 30));
        state.put(IoTValues.NIGHT_END_TIME, LocalTime.of(8, 30));
        state.put(IoTValues.NOW, LocalTime.of(12, 30));
        return state;
    }

    //If substring is found, index >=0, if not -1, returns a boolean
    //Source: https://www.javacodeexamples.com/java-stringbuilder-contains-example-stringbuffer/
    // 1542#:~:text=Java%20String%20class%20provides%20contains,is%20contained%20within%20the%20
    // String.&text=This%20method%20returns%20boolean%20depending,do%20not%20provide%20contains%20method.
    public boolean contains(StringBuffer str, String findString){
        return str.indexOf(findString) > -1 ;
    }


    @Test
    public void lightOnVacantTest() {

        //initialize evaluator and log
        StaticTartanStateEvaluator evaluator = new StaticTartanStateEvaluator();
        StringBuffer log = new StringBuffer();

        //default values
        Map<String, Object> vacantState = defaultState();
        vacantState.put(IoTValues.PROXIMITY_STATE, false);
        vacantState.put(IoTValues.LIGHT_STATE, true); //try to turn on light

        Map<String, Object> nextState = evaluator.evaluateState(vacantState, log);

        //assert light cannot be turned on
        assertEquals(false, nextState.get(IoTValues.LIGHT_STATE));
    }


    @Test
    public void heaterOnTest() {
        // if temperature is lower than target temperature, heater is on

        StaticTartanStateEvaluator evaluator = new StaticTartanStateEvaluator();
        StringBuffer log = new StringBuffer();

        //default values
        Map<String, Object> state = defaultState();

        state.put(IoTValues.HEATER_STATE, false);       //set heater to off
        state.put(IoTValues.TARGET_TEMP, 70);           //set target temperature
        state.put(IoTValues.TEMP_READING, 65);          //set current temperature to lower than target temperature

        Map<String, Object> nextState = evaluator.evaluateState(state, log);

        //assert light cannot be turned on
        assertEquals(false, nextState.get(IoTValues.LIGHT_STATE));
    }


    @Test
    public void heaterOffTest() {
        // if the temperature reaches the target temperature, heater is off
        // if the temperature is over the target temperature, heater is off

        StaticTartanStateEvaluator evaluator = new StaticTartanStateEvaluator();
        StringBuffer log = new StringBuffer();

        //default values
        Map<String, Object> state = defaultState();

        state.put(IoTValues.HEATER_STATE, true);     //set heater to on
        state.put(IoTValues.TARGET_TEMP, 70);        //set target temperature
        state.put(IoTValues.TEMP_READING, 70);       //set current temperature to equal than target temperature

        Map<String, Object> nextState = evaluator.evaluateState(state, log);

        //check if heater is off
        assertEquals(false, nextState.get(IoTValues.HEATER_STATE));


        state.put(IoTValues.TEMP_READING, 71);       //set current temperature to higher than target temperature

        //check if heater is off
        assertEquals(false, nextState.get(IoTValues.HEATER_STATE));
    }


    @Test
    public void nightLockTest() {
        // if night mode is on, the door cannot be unlocked

        StaticTartanStateEvaluator evaluator = new StaticTartanStateEvaluator();
        StringBuffer log = new StringBuffer();

        //default values
        Map<String, Object> state = defaultState();

        state.put(IoTValues.NIGHT_START_TIME, LocalTime.of(21, 30));
        state.put(IoTValues.NIGHT_END_TIME, LocalTime.of(8, 30));
        state.put(IoTValues.NOW, LocalTime.of(23, 00));
        state.put(IoTValues.LOCK_STATE, false);     //unlock door

        Map<String, Object> nextState = evaluator.evaluateState(state, log);

        //assert doors are locked
        assertEquals(true, nextState.get(IoTValues.LOCK_STATE));
    }


    @Test
    public void nightModeOffUnlockTest() {
        // if not within night hours, door can be unlocked

        StaticTartanStateEvaluator evaluator = new StaticTartanStateEvaluator();
        StringBuffer log = new StringBuffer();

        //default values
        Map<String, Object> state = defaultState();

        state.put(IoTValues.NIGHT_START_TIME, LocalTime.of(21, 30));
        state.put(IoTValues.NIGHT_END_TIME, LocalTime.of(8, 30));
        state.put(IoTValues.NOW, LocalTime.of(11, 30));
        state.put(IoTValues.LOCK_STATE, false);     //unlock door

        Map<String, Object> nextState = evaluator.evaluateState(state, log);

        assertEquals(false, nextState.get(IoTValues.LOCK_STATE));
    }


    @Test
    public void nightModeOnTest() {
        // if within night hours, night mode is on

        StaticTartanStateEvaluator evaluator = new StaticTartanStateEvaluator();
        StringBuffer log = new StringBuffer();

        //default values
        Map<String, Object> state = defaultState();

        state.put(IoTValues.NIGHT_START_TIME, LocalTime.of(21, 30));
        state.put(IoTValues.NIGHT_END_TIME, LocalTime.of(8, 30));
        state.put(IoTValues.NOW, LocalTime.of(22, 30));

        Map<String, Object> nextState = evaluator.evaluateState(state, log);

        //assert night mode is on
        assertEquals(true, nextState.get(IoTValues.NIGHT_MODE));

        state.put(IoTValues.NIGHT_START_TIME, LocalTime.of(9, 30));
        state.put(IoTValues.NIGHT_END_TIME, LocalTime.of(15, 30));
        state.put(IoTValues.NOW, LocalTime.of(12, 30));

        nextState = evaluator.evaluateState(state, log);

        //assert night mode is on
        assertEquals(true, nextState.get(IoTValues.NIGHT_MODE));
    }


    @Test
    public void nightModeOffTest() {
        // if not within night hours, night mode is off

        StaticTartanStateEvaluator evaluator = new StaticTartanStateEvaluator();
        StringBuffer log = new StringBuffer();

        //default values
        Map<String, Object> state = defaultState();

        state.put(IoTValues.NIGHT_START_TIME, LocalTime.of(21, 30));
        state.put(IoTValues.NIGHT_END_TIME, LocalTime.of(8, 30));
        state.put(IoTValues.NOW, LocalTime.of(12, 30));

        Map<String, Object> nextState = evaluator.evaluateState(state, log);

        assertEquals(false, nextState.get((IoTValues.NIGHT_MODE)));
    }

    
    @Test
    public void correctPasscodeAlarm() {
        // R9: The correct passcode is required to disable the alarm
        StaticTartanStateEvaluator evaluator = new StaticTartanStateEvaluator();
        StringBuffer log = new StringBuffer();
        // ----------------------------------------------------------------------------

        // Check if it CAN be disabled with the right passcode
        Map<String, Object> correctState = defaultState();
        // Correct passcode given
        correctState.put(IoTValues.ALARM_PASSCODE, "12345");    // Set the alarm passcode to 12345
        correctState.put(IoTValues.GIVEN_PASSCODE, "12345");    // Set the given passcode to 12345

        Map<String, Object> correctStateCheck = evaluator.evaluateState(correctState, log);
        assertEquals(correctStateCheck.get(IoTValues.ALARM_STATE), true); // check if disabled
        // ----------------------------------------------------------------------------
    }


    @Test
    public void alarmOnVacantTest() {
        //R4: If the door is closed and the house becomes suddenly occupied, then the alarm will sound
        //Initialize evaluator, log
        StaticTartanStateEvaluator evaluator = new StaticTartanStateEvaluator();
        StringBuffer log = new StringBuffer();

        //Check that the alarm does not turn on with the house vacant
        Map<String, Object> vacantState = defaultState();
        vacantState.put(IoTValues.DOOR_STATE, false);        //door closed
        vacantState.put(IoTValues.ALARM_STATE, true);        //alarm on
        vacantState.put(IoTValues.PROXIMITY_STATE, false);   //house vacant
        Map<String, Object> vacantStateCheck = evaluator.evaluateState(vacantState, log);
        assertEquals(vacantStateCheck.get(IoTValues.ALARM_ACTIVE), false);

        //Check the alarm is active when the house is suddenly occupied
        Map<String, Object> occupiedState = defaultState();
        occupiedState.put(IoTValues.DOOR_STATE, false);     //door closed
        occupiedState.put(IoTValues.ALARM_STATE, true);     //alarm on
        occupiedState.put(IoTValues.PROXIMITY_STATE,true);  //house occupied
        Map<String, Object> occupiedStateCheck = evaluator.evaluateState(occupiedState, log);
        assertEquals(occupiedStateCheck.get(IoTValues.ALARM_ACTIVE), true);
    }


    @Test
    public void handsFreeTest() {
        //initialize evaluator and log
        StaticTartanStateEvaluator evaluator = new StaticTartanStateEvaluator();
        StringBuffer log = new StringBuffer();

        // Default
        Map<String, Object> state = defaultState();

        // Set the proximity state to true
        state.put(IoTValues.PROXIMITY_STATE, true);

        // Unlock the door
        state.put(IoTValues.LOCK_STATE, false);

        Map<String, Object> nextState = evaluator.evaluateState(state, log);

        // Check if doors open when proximity is true
        assertEquals(false, nextState.get(IoTValues.LOCK_STATE));
    }


    @Test
    public void detectIntruderTest() {
        //When sensors in the house detect a possible intruder, the door will lock
        //"Possible intruder detected" message sent to access panels

        // Initialize evaluator, log and startState
        StaticTartanStateEvaluator evaluator = new StaticTartanStateEvaluator();
        StringBuffer log = new StringBuffer();
        Map<String, Object> startState = defaultState();

        //Set the startState and evaluate
        startState.put(IoTValues.INTRUDER_DETECT, true);    //Intruder detected
        startState.put(IoTValues.LOCK_STATE, false);         //Door Unlocked
        Map<String, Object> endState = evaluator.evaluateState(startState, log);

        //Check the door is locked, and message appears at access panels
        assertEquals(true, endState.get(IoTValues.LOCK_STATE));
        assertEquals(true, contains(log,"Possible intruder detected"));
    }


    @Test
    public void allClearTest() {
        // keep door locked until "all clear" signal displayed on access panel

        // Initialize evaluator, log1 and startState1
        StaticTartanStateEvaluator evaluator = new StaticTartanStateEvaluator();
        StringBuffer log1 = new StringBuffer();
        Map<String, Object> startState1 = defaultState();

        // Set startState1 and evaluate
        startState1.put(IoTValues.INTRUDER_DETECT, true);   //Intruder detected
        startState1.put(IoTValues.LOCK_STATE, true);         //Door Locked
        Map<String, Object> endState1 = evaluator.evaluateState(startState1, log1);

        //Check the door is still locked, and no "all clear" message
        assertEquals(true, endState1.get(IoTValues.LOCK_STATE));
        assertEquals(false, contains(log1, "All clear"));
        assertEquals(true, contains(log1,"Possible intruder detected"));

        //----------------------------------------------------------------------------

        // "All clear" signal appears when sensors do not detect intruder

        // Initialize log2 and startState2
        StringBuffer log2 = new StringBuffer();
        Map<String, Object> startState2 = defaultState();

        // Set startState2 and evaluate
        startState2.put(IoTValues.INTRUDER_DETECT, false);  //Intruder not detected
        startState2.put(IoTValues.LOCK_STATE, true);         //Door Locked
        Map<String, Object> endState2 = evaluator.evaluateState(startState2, log2);

        // Check that "all clear" message appears and door should still remain locked
        assertEquals(true, endState2.get(IoTValues.LOCK_STATE));
        assertEquals(true, contains(log2, "All clear"));
        assertEquals(false, contains(log2,"Possible intruder detected"));
    }


    @Test
    public void handleStateUpdateTest() {
        //Receive a str msg from the house and return a converted IoT state
        //The returned state should include the new variables: INTRUDER_DETECT and LOCK_STATE
        //In the str msg, LKS=LOCK_STATE and ID=INTRUDER_DETECT
        IoTConnection connection = mock(IoTConnection.class);
        String stateMsg = "SU:TR=65;HR=100;DS=1;LS=1;PS=1;AS=0;AA=0;HES=0;CHS=0;HM=1;HUS=0;LKS=1;ID=0."; //door locked, all clear
        when(connection.sendMessageToHouse(IoTValues.GET_STATE + IoTValues.MSG_END)).thenReturn(stateMsg);

        IoTConnectManager connMgr = new IoTConnectManager(connection);
        Map<String, Object> state = connMgr.getState();

        assertEquals(true, state.get(IoTValues.LOCK_STATE));
        assertEquals(false, state.get(IoTValues.INTRUDER_DETECT));
    }


}

