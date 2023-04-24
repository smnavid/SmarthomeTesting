package tartan.smarthome.views;

import io.dropwizard.views.View;
import tartan.smarthome.core.TartanHome;

/**
 * A view of the house state.
 * @see <a href="https://www.dropwizard.io/1.0.0/docs/manual/views.html">Dropwizard Views</a>
 */
public class SmartHomeView extends View {
    private TartanHome tartanHome;

    /**
     * Create a new view
     * @param tartanHome the home to view
     */
    public SmartHomeView(TartanHome tartanHome) {
        super("smartHome.ftl");
        this.tartanHome = tartanHome;
    }

    /**
     * Get the home
     * @return the home
     */
    public TartanHome getTartanHome() {
        return tartanHome;
    }
}
