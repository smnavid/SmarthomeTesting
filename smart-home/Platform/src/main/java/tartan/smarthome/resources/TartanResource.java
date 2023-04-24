package tartan.smarthome.resources;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
import io.dropwizard.hibernate.UnitOfWork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tartan.smarthome.TartanHomeSettings;
import tartan.smarthome.auth.TartanUser;
import tartan.smarthome.core.TartanHome;
import tartan.smarthome.db.HomeDAO;
import tartan.smarthome.views.SmartHomeView;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * The resource class implements the HTTP handlers via Jersey.
 *  @see <a href="https://www.dropwizard.io/1.0.0/docs/getting-started.html#creating-a-resource-class">Dropwizard Resouces</a>
 */
@Path("/smarthome")
@Produces(MediaType.APPLICATION_JSON)
public class TartanResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(TartanResource.class);

    // There is one service per home
    private ArrayList<TartanHomeService> services;

    /**
     * Create and connect to a list of houses
     * @param houses the settings for each hose
     * @param homeDAO the historian
     * @param historyTimer how often to log history
     */
    public TartanResource(List<TartanHomeSettings> houses, HomeDAO homeDAO, Integer historyTimer) {

        this.services = new ArrayList<>(houses.size());
        for (TartanHomeSettings homeSettings : houses) {
            TartanHomeService service = new TartanHomeService(homeDAO);
            service.initializeSettings(homeSettings, historyTimer);

            if (!service.isConnected()) {
                try {

                    service.connect();
                    this.services.add(service);
                    LOGGER.info("Connected to house " + service.getName() + " @ " + service.getAddress());

                } catch (TartanHomeConnectException thce) {
                    LOGGER.error("Could not connect to house " + service.getName() + " @ " + service.getAddress());
                }

                startHistorian(service);
            }
        }
    }

    /**
     * Start the historian
     * @param service the service to start logging
     */
    public void startHistorian(TartanHomeService service) {
        if (service.isConnected()) {
            service.startHistorian();
        }
    }

    /**
     * Fetch the service for a house
     * @param houseName the target house
     * @return the service or null if not found
     */
    private TartanHomeService getHomeService(String houseName) {
        for (TartanHomeService h : services) {
            if (h.getName().equals(houseName)) {
                return h;
            }
        }
        return null;
    }

    /**
     * Fetch the current house state via HTTP GET. Managed by Jersey
     * @param house the house
     * @param user the user allowed to access this house
     * @return a view of the house or null
     */
    @GET
    @Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON})
    @Path("/state/{house}")
    @Timed
    @UnitOfWork
    public SmartHomeView state(@PathParam("house") String house,  @Auth TartanUser user) {
        // There are better ways to check authorization, but this works fine
        if (user.getHouse().equals(house)) {
            LOGGER.info("Received a house GET for house: " + house);
            TartanHomeService service = getHomeService(house);
            if (service == null) return null;

            return new SmartHomeView(service.getState());
        }
        return null;
    }

    /**
     * update the house state via a HTTP POST. Managed by Jersey
     * @param house the house
     * @param user the user allowed to access this house
     * @param h the new state
     * @return either HTTP OK or UNAUTHORIZED
     */
    @POST
    @Path("/update/{house}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed
    public Response update(@PathParam("house") String house, @Auth TartanUser user, TartanHome h) {
        if (user.getHouse().equals(house)) {
            LOGGER.info("Received a house POST to house " + house);
            TartanHomeService service = getHomeService(house);
            if (service != null) {
                // tell the house about the update
                service.setState(h);

                return Response
                        .status(Response.Status.OK)
                        .build();
            }
        }
        return Response
                .status(Response.Status.UNAUTHORIZED)
                .build();
    }
}

