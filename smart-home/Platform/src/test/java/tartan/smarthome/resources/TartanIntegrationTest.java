package tartan.smarthome.resources;

//import io.dropwizard.testing.ResourceHelpers;
//import io.dropwizard.testing.junit.DropwizardAppRule;
//import org.glassfish.jersey.client.JerseyClientBuilder;
//import org.junit.ClassRule;
//import org.junit.Test;
//import tartan.smarthome.TartanHomeApplication;
//import tartan.smarthome.TartanHomeConfiguration;
//import tartan.smarthome.core.TartanHome;
//
//import javax.ws.rs.client.Client;
//import javax.ws.rs.client.Entity;
//import javax.ws.rs.core.Response;
//
//import static org.junit.Assert.*;
//
//public class TartanIntegrationTest {
//    @ClassRule
//    public static final DropwizardAppRule<TartanHomeConfiguration> RULE =
//            new DropwizardAppRule<TartanHomeConfiguration>(
//                    TartanHomeApplication.class,
//                    ResourceHelpers.resourceFilePath("testconfig.yml")
//            );
//
//    public void handleHouseUpdateTest() {
//        Client client = new JerseyClientBuilder().build();
//
//        TartanHome home = new TartanHome();
//        home.setNightStartTime("22:00");
//        home.setNightEndTime("8:00");
//        home.setAlarmDelay("25");
//
//        Response response = client.target(
//                String.format("http://localhost:%d/smarthome/update/mse", RULE.getLocalPort()))
//                .request()
//                .post(Entity.json(home));
//
//        assertEquals(200, response.getStatus());
//    }
//}
