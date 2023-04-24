package tartan.smarthome.auth;

import java.security.Principal;

/**
 * Simple class to represent an Authenticated users
 */
public class TartanUser implements Principal {
    // Users are identified by name and house
    private String name=null;
    private String house = null;

    /**
     * Create a new TartanUser. Note that this is the only place to set name and house
     * @param name The user name
     * @param house The user house
     */
    public TartanUser(String name, String house) {
        this.name = name;
        this.house = house;
    }

    /**
     * Get the user name
     * @return the user name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Get the user house
     * @return the user house
     */
    public String getHouse() {
        return house;
    }
}
