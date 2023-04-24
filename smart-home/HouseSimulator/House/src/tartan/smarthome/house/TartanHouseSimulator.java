package tartan.smarthome.house;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Set;
import java.util.StringTokenizer;
import java.net.UnknownHostException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class TartanHouseSimulator implements Runnable {

    /** Internal state variables */
    private Integer tempReading; // the current temperature
    private Integer humidityReading; // the current humidity
    private Boolean doorState; // the state of the door (true if open, false if closed)
    private Boolean dightState; // the state of the light (true if on, false if off)
    private Boolean proximityState; // the state of the proximity sensor (true of house occupied, false if vacant)
    private Boolean alarmState; // the alarm state (true if enabled, false if disabled)
    private Boolean humidifierState; // the humidifier state (true if on, false if off)
    private Boolean heaterOnState; // the heater state (true if on, false if off)
    private Boolean chillerOnState; // the chiller state (true if on, false if off)
    private Boolean alarmActiveState; // the alarm active state (true if alarm sounding, false if alarm not sounding)
    private String  hvacMode; // the HVAC mode setting, either Heater or Chiller

    /** connection settings */
    private String address = null;
    private Integer port = 5050; // the default port for the house

    /** The connection is private so it can be controlled */
    private Socket houseSocket = null;
    private BufferedWriter out = null;
    private BufferedReader in = null;

    private Boolean isConnected = false;

    // state readings
    private final String TEMP_READING = "TR";
    private final String HUMIDITY_READING = "HR";
    private final String HUMIDIFIER_STATE = "HUS";
    private final String DOOR_STATE = "DS";
    private final String LIGHT_STATE = "LS";
    private final String PROXIMITY_STATE = "PS";
    private final String ALARM_STATE = "AS";
    private final String HVAC_MODE = "HM";
    private final String ALARM_ACTIVE = "AA";
    private final String HEATER_STATE = "HES";
    private final String CHILLER_STATE = "CHS";
    private final String PASSCODE = "PC";

    // protocol control values
    private final String PARAM_DELIM = ";";
    private final String MSG_DELIM = ":";
    private final String PARAM_EQ = "=";
    final String MSG_END = ".";

    // target temperature
    private final String TARGET_TEMP = "TT";

    private final String DOOR_CLOSE = "0";
    private final String DOOR_OPEN = "1";

    private final String LIGHT_ON = "1";
    private final String LIGHT_OFF = "0";

    private final String HUMIDIFIER_ON = "1";
    private final String HUMIDIFIER_OFF = "0";

    private final String ALARM_ENABLED = "1";
    private final String ALARM_DISABLED = "0";

    private final String HEATER_ON = "1";
    private final String HEATER_OFF = "0";

    private final String CHILLER_ON = "1";
    private final String CHILLER_OFF = "0";

    private final String OK = "OK";

    private final String ALARM_DELAY = "ALARM_DELAY";
    private final String ALARM_PASSCODE = "ALARM_PASSCODE";

    private final String GET_STATE = "GS";
    private final String SET_STATE = "SS";
    private final String STATE_UPDATE = "SU";

    /**
     * Get connection state
     * 
     * @return true if connected, false otherwise
     */
    public Boolean isConnected() {
        return isConnected;
    }

    /**
     * Get an existing connection, or make a new one
     * @param addr the house address
     * @return the established connection or null
     */
    public TartanHouseSimulator(Integer port) {
        this.port = port;
    }    

    public void runSimulator() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            houseSocket = serverSocket.accept();
            out = new BufferedWriter(new OutputStreamWriter(houseSocket.getOutputStream()));
            in = new BufferedReader(new InputStreamReader(houseSocket.getInputStream()));
        }
        catch(Exception x) { 
            try {
                serverSocket.close();
            } catch (IOException e) {  }
        }
        new Thread( this). start();

        isConnected = true;
    }

    /**
     * Send a message to the house and get a response
     * 
     * @param msg the message to send
     * @return the response
     */
    public String sendMessage(String msg) {
        
        try {

            out.write(msg, 0, msg.length());
            out.flush();

            return in.readLine();

        } catch (IOException ioe) {
            //ioe.printStackTrace();
        }
        return null;
    }

    /**
         * Get the state from the house
         * 
         * @return the new state of things
         */
        public synchronized Hashtable<String, Object> getState() {

            System.out.println("Requesting state");

            String update = sendMessage(GET_STATE + MSG_END);
                if (update == null) {
                    return null;
                }

                return handleStateUpdate(update);
            
        }

    /**
         * Send a state change request to the house
         * 
         * @param state the new state
         * @return true if the state was accepted; false otherwise
         */
        public synchronized Boolean setState(Hashtable<String, Object> state) {

            StringBuffer newState = new StringBuffer();

            Set<String> keys = state.keySet();
            
            int count = 0;
            for (String key : keys) {

                if (key.equals(DOOR_STATE)) {

                    Boolean newDoorState = (Boolean) state.get(key);
                    newState.append(DOOR_STATE);
                    newState.append(PARAM_EQ);
                    if (newDoorState) {
                        newState.append(DOOR_OPEN);
                    } else {
                        newState.append(DOOR_CLOSE);
                    }
                    count++;
                    if (count < keys.size()) {
                        newState.append(PARAM_DELIM);
                    }
                } else if (key.equals(LIGHT_STATE)) {
                    Boolean newLightState = (Boolean) state.get(key);
                    newState.append(LIGHT_STATE);
                    newState.append(PARAM_EQ);
                    if (newLightState) {
                        newState.append(LIGHT_ON);
                    } else {
                        newState.append(LIGHT_OFF);
                    }
                    count++;
                    if (count < keys.size()) {
                        newState.append(PARAM_DELIM);
                    }
                } else if (key.equals(LIGHT_STATE.toString())) {
                    Boolean newLightState = (Boolean) state.get(key);
                    newState.append(LIGHT_STATE);
                    newState.append(PARAM_EQ);
                    if (newLightState) {
                        newState.append(LIGHT_ON);
                    } else {
                        newState.append(LIGHT_OFF);
                    }
                    count++;
                    if (count < keys.size()) {
                        newState.append(PARAM_DELIM);
                    }
                } else if (key.equals(ALARM_STATE)) {
                    Boolean newAlarmState = (Boolean) state.get(key);
                    newState.append(ALARM_STATE);
                    newState.append(PARAM_EQ);
                    if (newAlarmState) {
                        newState.append(ALARM_ENABLED);
                    } else {
                        newState.append(ALARM_DISABLED);
                    }
                    count++;
                    if (count < keys.size()) {
                        newState.append(PARAM_DELIM);
                    }
                } else if (key.equals(ALARM_STATE)) {
                    Boolean newHumidifierState = (Boolean) state.get(key);
                    newState.append(HUMIDIFIER_STATE);
                    newState.append(PARAM_EQ);
                    if (newHumidifierState) {
                        newState.append(HUMIDIFIER_ON);
                    } else {
                        newState.append(HUMIDIFIER_OFF);
                    }
                    count++;
                    if (count < keys.size()) {
                        newState.append(PARAM_DELIM);
                    }
                } else if (key.equals(CHILLER_STATE)) {
                    Boolean newChillerState = (Boolean) state.get(key);
                    newState.append(CHILLER_STATE);
                    newState.append(PARAM_EQ);
                    if (newChillerState) {
                        newState.append(CHILLER_ON);
                    } else {
                        newState.append(CHILLER_OFF);
                    }
                    count++;
                    if (count < keys.size()) {
                        newState.append(PARAM_DELIM);
                    }
                } else if (key.equals(HEATER_STATE)) {
                    Boolean newHeaterState = (Boolean) state.get(key);
                    newState.append(HEATER_STATE);
                    newState.append(PARAM_EQ);
                    if (newHeaterState) {
                        newState.append(HEATER_ON);
                    } else {
                        newState.append(HEATER_OFF);
                    }
                    count++;
                    if (count < keys.size()) {
                        newState.append(PARAM_DELIM);
                    }
                }
            }

            StringBuffer msg = new StringBuffer(SET_STATE + MSG_DELIM + newState + MSG_END);
            
                    System.out.println("New state for house: " + msg.toString());

            String response =sendMessage(msg.toString());
            
            if(response== null) {
            System.out.println("No response");
            return false;
        }
        System.out.println("Response: "+response);

    return response.equals(OK);
    }

    /**
         * Process the new state reported by the house
         * 
         * @param stateUpdateMsg the new state message
         * @return the new state
         */
        private Hashtable<String, Object> handleStateUpdate(String stateUpdateMsg) {

            if (stateUpdateMsg == null) {
                return null;
            }
            if (stateUpdateMsg.length() == 0) {
                return null;
            }

            System.out.println("State Update: " + stateUpdateMsg);
            Hashtable<String, Object> state = new Hashtable<String, Object>();

            String[] req = stateUpdateMsg.split(MSG_DELIM);

            // invalid state update
            if (req.length != 2) {
                return null;
            }
            // is this a state update
            String cmd = req[0];
            String body = req[1];

            if (!cmd.equals(STATE_UPDATE)) { // only message that comes from house
                return null;
            }

            if (String.valueOf(body.charAt(body.length() - 1)).equals(MSG_END)) {
                body = body.substring(0, body.length() - 1);
            }
            if (body == null) {
                return null;
            }
            StringTokenizer pt = new StringTokenizer(body, PARAM_DELIM);

            // process the new state
            while (pt.hasMoreTokens()) {
                String param = pt.nextToken();
                String data[] = param.split(PARAM_EQ);
                Integer val = Integer.parseInt(data[1]);

                if (data[0].equals(LIGHT_STATE)) {
                    if (val == 1) {
                        state.put(LIGHT_STATE, true);
                    } else {
                        state.put(LIGHT_STATE, false);
                    }
                } else if (data[0].equals(ALARM_STATE)) {
                    if (val == 1) {
                        state.put(ALARM_STATE, true);
                    } else {
                        state.put(ALARM_STATE, false);
                    }
                } else if (data[0].equals(DOOR_STATE)) {
                    if (val == 1) {
                        state.put(DOOR_STATE, true);
                    } else {
                        state.put(DOOR_STATE, false);
                    }
                } else if (data[0].equals(HUMIDIFIER_STATE)) {
                    if (val == 1) {
                        state.put(HUMIDIFIER_STATE, true);
                    } else {
                        state.put(HUMIDIFIER_STATE, false);
                    }
                } else if (data[0].equals(PROXIMITY_STATE)) {
                    if (val == 1) {
                        state.put(PROXIMITY_STATE, true);
                    } else {
                        state.put(PROXIMITY_STATE, false);
                    }
                } else if (data[0].equals(HEATER_STATE)) {
                    if (val == 1) {
                        state.put(HEATER_ON, true);
                    } else {
                        state.put(HEATER_OFF, false);
                    }
                } else if (data[0].equals(CHILLER_STATE)) {
                    if (val == 1) {
                        state.put(CHILLER_ON, true);
                    } else {
                        state.put(CHILLER_OFF, false);
                    }
                } else if (data[0].equals(TEMP_READING)) {
                    state.put(TEMP_READING, val);
                } else if (data[0].equals(HUMIDITY_READING)) {
                    state.put(HUMIDITY_READING, val);
                } else if (data[0].equals(HVAC_MODE)) {
                    if (val == 1) {
                        state.put(HVAC_MODE, "Heater");
                    } else {
                        state.put(HVAC_MODE, "Chiller");
                    }
                }
            }
            return state;
        }

    @Override
    public void run() {
        // TODO: read and write 
    }
}
