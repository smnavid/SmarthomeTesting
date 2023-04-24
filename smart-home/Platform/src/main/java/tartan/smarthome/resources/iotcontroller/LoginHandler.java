package tartan.smarthome.resources.iotcontroller;

import java.util.Vector;

/**
 * Validate username and password
 *
 * Project: LG Exec Ed Program
 * Copyright: Copyright (c) 2015 Jeffrey S. Gennari
 * Versions:
 * 1.0 November 2015 - initial version
 */
public class LoginHandler {

    /** the number of unsuccessful login attempts */
    private int times;

    /** the list of valid users */
    Vector<UserLoginInfo> validUsers;

    /**
     * The constructor for the login handler
     * @param vu list of valid users
     */
    public LoginHandler(Vector<UserLoginInfo> vu) {
        times = 0;
        validUsers = vu;
    }

    public void resetHandler() {
        times = 0;
    }

    /**
     * Authenticate the username and password
     * @param username the user name
     * @param password the password
     * @return true if authenticated, false otherwise
     * @throws LoginAttemptsExceededException
     */
    public Boolean authenticate(String username, String password) throws LoginAttemptsExceededException {

        if (times > 3) throw new LoginAttemptsExceededException();

        for (UserLoginInfo uli : validUsers) {
            String un = uli.getUserName();
            String pw = uli.getPassword();
            if (username.equals(un) && password.equals(pw)) {
                return true;
            }
        }
        // increment the invalid login count
        times++;
        return false;
    }
}

