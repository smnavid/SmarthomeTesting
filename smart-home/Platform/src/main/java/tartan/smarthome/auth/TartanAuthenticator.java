package tartan.smarthome.auth;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.AbstractMap;
import tartan.smarthome.TartanHomeConfiguration;
import tartan.smarthome.TartanHomeSettings;

/***
 * Authentication class for the Tartan Home System. A simple username and password is required
 */
public class TartanAuthenticator implements Authenticator<BasicCredentials, TartanUser> {


    private  Map<Map.Entry<String, String>, String> VALID_USERS = new HashMap<>();

    /**
     * Empty constructor
     */
    public TartanAuthenticator() {   }

    /**
     * Set the list of valid users from the configuration
     * @param config the configuration
     */
    public void setValidUsers(TartanHomeConfiguration config) {
        List<TartanHomeSettings> houses = config.getHouses();
        for (TartanHomeSettings h : houses) {
            Map.Entry<String,String> key =
                    new AbstractMap.SimpleEntry<>(h.getUser(), h.getPassword());

            VALID_USERS.put(key, h.getName());
        }
    }

    /**
     * Authenticate the user
     * @param credentials the user login information
     * @return the authenticated user on sucess
     * @throws AuthenticationException failed authentication
     */
    @Override
    public Optional<TartanUser> authenticate(BasicCredentials credentials) throws AuthenticationException {
        Map.Entry<String,String> p =
                new AbstractMap.SimpleEntry<>(credentials.getUsername(), credentials.getPassword());

        if (VALID_USERS.containsKey(p)) {
            return Optional.of(new TartanUser(credentials.getUsername(), VALID_USERS.get(p)));
        }
        return Optional.empty();
    }
}
