package tartan.smarthome;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import org.hibernate.validator.constraints.NotEmpty;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Configuration settings read from YAML file config.yml. See that file for field definitions
 * @see <a href="https://www.dropwizard.io/1.0.0/docs/manual/core.html#configuration">Dropwizard Configuration</a>
 *
 */
public class TartanHomeConfiguration extends Configuration {

    @NotEmpty
    @JsonProperty
    private List<TartanHomeSettings> houses;

    @NotEmpty
    @JsonProperty
    private String historyTimer;

    @Valid
    @NotNull
    private DataSourceFactory database = new DataSourceFactory();

    @JsonProperty
    public List<TartanHomeSettings> getHouses() {
        return houses;
    }

    @JsonProperty
    public void setHouses(List<TartanHomeSettings> houses) {
        this.houses = houses;
    }

    @JsonProperty("database")
    public DataSourceFactory getDataSourceFactory() {
        return database;
    }

    @JsonProperty("database")
    public void setDataSourceFactory(DataSourceFactory dataSourceFactory) {
        this.database = dataSourceFactory;
    }

    @JsonProperty
    public String getHistoryTimer() {
        return historyTimer;
    }
}
