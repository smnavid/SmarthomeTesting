package tartan.smarthome.resources.iotcontroller;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

import tartan.smarthome.resources.TartanStateEvaluator;
import tartan.smarthome.utils.TartanTimeUtils;

/**
 * Controls the state of the IoT house. This class manages house state; it is
 * the focal point for changing settings adding users, and ensuring that the
 * house remains in a consistent state
 *
 * Project: LG Exec Ed Program Copyright: 2015 Jeffrey S. Gennari Versions: 1.0
 * November 2015 - initial version
 */

public class IoTControlManager {

    /** connection to the house */
    private IoTConnectManager connMgr;

    /** the user settings */
    private Hashtable<String, Object> userSettings;

    private Vector<UserLoginInfo> users = new Vector<UserLoginInfo>();

    /** the path to user settings and credentials */
    private String settingsPath;

    /** the log messages */
    private Vector<String> logMessages;

    private LoginHandler loginHandler;

    /** Thread to manage state updates */
    private Thread updateThread;

    /** Handle updates to the house state */
    private TartanStateEvaluator stateEvaluator;

    private Map<String, Object> lastState;  

    /**
     * Constructor for the controller
     *
     * @param user     the user name
     * @param password the password
     */
    public IoTControlManager(String user, String password, TartanStateEvaluator evaluator) {

        logMessages = new Vector<String>();

        userSettings = new Hashtable<String, Object>();

        settingsPath = null;

        users.add(new UserLoginInfo(user, password));

        this.stateEvaluator = evaluator;

        loginHandler = new LoginHandler(this.users);

        connMgr = null;

        lastState = new Hashtable<>();
    }

    /**
     * Load the registered users from a database (file).
     * 
     * @return the set of valid users
     */
    public Vector<UserLoginInfo> loadUsers() {
        Vector<UserLoginInfo> users = new Vector<UserLoginInfo>();

        try {
            File file = new File(settingsPath + File.separator + IoTValues.USERS_DB);

            BufferedReader br = new BufferedReader(new FileReader(file));

            String line;
            while ((line = br.readLine()) != null) {
                String[] entry = line.split("=");
                users.add(new UserLoginInfo(entry[0], entry[1]));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return users;
    }

    /**
     * Load the user preferences from a database (file).
     */
    public void loadSettings() {
        Properties props = new Properties();
        File f = new File(settingsPath + File.separator + IoTValues.SETTINGS_FILE);

        // First try loading from the current directory
        try (InputStream is = new FileInputStream(f)){
            props.load(is);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String alarmPassCode = props.getProperty(IoTValues.ALARM_PASSCODE, "passcode");
        Integer alarmDelay = Integer.parseInt(props.getProperty(IoTValues.ALARM_DELAY, "5"));

        Map<String, Object> initialSettings = new Hashtable<String, Object>();
        initialSettings.put(IoTValues.ALARM_DELAY, alarmDelay);
        initialSettings.put(IoTValues.ALARM_PASSCODE, alarmPassCode);
        
        // The away timer is not set to start
        lastState.put(IoTValues.AWAY_TIMER, false);

        // update the settings
        updateSettings(initialSettings);
    }

    /**
     * Update user settings (the alarm delay)
     *
     * @param newSettings the new user settings.
     */
    public void updateSettings(Map<String, Object> newSettings) {
        if (userSettings != null && newSettings != null) {
            userSettings.putAll(newSettings);
        }
    }

    /**
     * Fetch the user settings
     * 
     * @return the user settings
     */
    public Hashtable<String, Object> getUserSettings() {
        return new Hashtable<String, Object>(userSettings);
    }

    public Thread getUpdateThread() {
        return updateThread;
    }

    /**
     * User-initiated state update
     * @param stateUpdate
     */
    public void processStateUpdate(Map<String, Object> stateUpdate) {

        StringBuffer log = new StringBuffer();

        // User settings are part of the state
        Map<String, Object> completeState = new Hashtable<>();
        completeState.putAll(fetchState());
        completeState.putAll(stateUpdate);     
        Map<String, Object> newState = stateEvaluator.evaluateState(completeState, log);
        logMessages.add(log.toString());
        synchronized(connMgr) {
            connMgr.setState(newState);
        }
        this.lastState.putAll(newState);
    }

    public Map<String, Object> getCurrentState() {
        return fetchState();
    }

    /**
     * Fetch the complete state from the house
     * @return
     */
    private Map<String, Object> fetchState() {
        // Map<String, Object> state = null;
        synchronized (connMgr) {
            if (connMgr.isConnected() == false) {
                return null;
            }
            lastState = connMgr.getState();
        }

        // The away timer is controlled here
        lastState.put(IoTValues.AWAY_TIMER, false);

        // The state includes the user settings 
        lastState.putAll(userSettings);
        // lastState.putAll(state);
        return lastState;
    }

    /**
     * Start a thread to poll the house state
     */
    private void startHouseUpdateThread() {

        updateThread = new Thread(new Runnable() {
            @Override
            public void run() {

                Integer missedUpdates = 0;
                while (true) {

                    Map<String, Object> currentState = fetchState();
                    if (currentState != null) {
                        StringBuffer log = new StringBuffer();
                        Map<String, Object> newState = stateEvaluator.evaluateState(currentState, log);
                        logMessages.add(log.toString());
                        
                        // save this state 
                        IoTControlManager.this.lastState.putAll(newState);

                        synchronized (connMgr) {
                            connMgr.setState(newState);
                        }
                        
                        // Must handle away timer here
                        if (true == (Boolean) newState.getOrDefault(IoTValues.AWAY_TIMER, false)) {
                            startAwayTimer();
                        }
                        else 
                        missedUpdates = 0;

                    } else {
                        missedUpdates++;
                    }

                    if (missedUpdates > 6) { // 6 missed updates is 30 seconds
                        revertState();
                    }

                    // currently a 5sec delay
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ie) {
                    }
                }
            }
        });
        updateThread.start();
        updateLog("Started update monitor");
    }

    private void revertState() {
        synchronized (connMgr) {
            connMgr.setState(this.lastState);
        }
    }

    /**
     * Connect to a house
     *
     * @param houseAddress the network address of the house. Once connected, this
     *                     method starts a new thread to update house state
     * @return true if connected, false otherwise
     */
    public Boolean connectToHouse(String houseAddress, Integer housePort, String user, String password) {
        updateLog("Connecting");
        try {
            loginHandler.authenticate(user, password);
        } catch (LoginAttemptsExceededException e) {
            return false;
        }

        IoTConnection conn = new IoTConnection(houseAddress, housePort);
        conn.connect();
        connMgr = new IoTConnectManager(conn);

        if (connMgr.isConnected()) {
            startHouseUpdateThread();
            return true;
        }
        updateLog("Connected!");
        return false;
    }

    /**
     * Disconnect from a house
     */
    public void disconnectFromHouse() {
        if (connMgr.isConnected()) {
            connMgr.disconnectFromHouse();
        }
    }

    /**
     * Add a log entry
     *
     * @param logEntry the new log entry
     */
    public void updateLog(String logEntry) {
        Long timeStamp = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
        logMessages.add("[" + sdf.format(new Date(timeStamp)) + "]: " + logEntry + "\n");
    }

    public Vector<String> getLogMessages() {
        return logMessages;
    }

    /**
     * Start a timer when the house becomes unoccupied. When the timer expires, lock
     * the house down
     */
    private void startAwayTimer() {
        Timer t = new Timer();

        Integer awayTimeout = (Integer) userSettings.get(IoTValues.ALARM_DELAY);

        t.schedule(new TimerTask() {

            /**
             * This anonymous thread updates the state when the house is vacant
             */
            @Override
            public void run() {

                // signal that the away timer has fired
                IoTControlManager.this.lastState.put(IoTValues.AWAY_TIMER, true);

                synchronized (connMgr) {

                    StringBuffer log = new StringBuffer();
                    Map<String, Object> newState = stateEvaluator.evaluateState(IoTControlManager.this.lastState, log);
                    logMessages.add(log.toString());
                    connMgr.setState(newState);
                    IoTControlManager.this.lastState.putAll(newState);
                }
            }
        }, awayTimeout * 1000);
    }

    /**
     * Get the connected state
     *
     * @return true if connected to the house, false otherwise
     */
    public Boolean isConnected() {
        if (connMgr == null) {
            return false;
        }
        return connMgr.isConnected();
    }
}