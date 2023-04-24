package tartan.smarthome;

import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import tartan.smarthome.auth.TartanAuthenticator;
import tartan.smarthome.auth.TartanUser;
import tartan.smarthome.core.TartanHomeData;
import tartan.smarthome.db.HomeDAO;
import tartan.smarthome.resources.TartanResource;

/**
 * This is the driver for the program.
 * @see <a href="https://www.dropwizard.io/1.0.0/docs/manual/core.html#application">Dropwizard Applications</a>
 */
public class TartanHomeApplication extends Application<TartanHomeConfiguration> {

    private final HibernateBundle<TartanHomeConfiguration> hibernateBundle =
            new HibernateBundle<TartanHomeConfiguration>(TartanHomeData.class) {
                @Override
                public DataSourceFactory getDataSourceFactory(TartanHomeConfiguration configuration) {
                    return configuration.getDataSourceFactory();
                }
            };

    /**
     * The driver
     * @param args command line arguments
     * @throws Exception a catch all exception
     */
    public static void main(final String[] args) throws Exception {
        new TartanHomeApplication().run(args);
    }

    /**
     * Get the application name. This is the core URL for the system
     * @return the name
     */
    @Override
    public String getName() {
        return "smarthome";
    }

    /**
     * Initialize the system
     * @param bootstrap the initial settings from from the YAML file
     */
    @Override
    public void initialize(final Bootstrap<TartanHomeConfiguration> bootstrap) {
        // We need the view bundle for rendering
        bootstrap.addBundle(new ViewBundle());
        bootstrap.addBundle(hibernateBundle);
    }

    /**
     * Run the system.
     * @param configuration system settings
     * @param environment system environment
     */
    @Override
    public void run(final TartanHomeConfiguration configuration,
                    final Environment environment) {
        HomeDAO dao = new HomeDAO(hibernateBundle.getSessionFactory());

        TartanAuthenticator auth = new TartanAuthenticator();
        auth.setValidUsers(configuration);

        final TartanResource resource = new TartanResource(configuration.getHouses(),
                dao, Integer.parseInt(configuration.getHistoryTimer()));

        environment.jersey().register(resource);
        environment.jersey().register(new AuthDynamicFeature(new BasicCredentialAuthFilter.Builder<TartanUser>()
                .setAuthenticator(auth)
                .buildAuthFilter()));
        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(TartanUser.class));
    }
}
