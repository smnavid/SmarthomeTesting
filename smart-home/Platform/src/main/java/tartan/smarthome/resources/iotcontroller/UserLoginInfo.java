package tartan.smarthome.resources.iotcontroller;

/**
 * Represents user login information
 */
public class UserLoginInfo {

    /** the user credentials */
    private String userName, password;

    public UserLoginInfo(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    /**
     *  Get the username
     * @return the user name
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Set the username
     * @param userName
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Get password
     * @return
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set password
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
