package tartan.smarthome;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * This is Jackson-compatible a configuration class for the initial configuration setting in the primiary
 * YAML confguration file. See that file for definitions
 */
public class TartanHomeSettings {

    @NotEmpty
    @JsonProperty
    private String name;

    @NotEmpty
    @JsonProperty
    private String address;

    @NotEmpty
    @JsonProperty
    private Integer port;

    @NotEmpty
    @JsonProperty
    private String user;

    @NotEmpty
    @JsonProperty
    private String password;

    @NotEmpty
    @JsonProperty
    private String targetTemp;

    @NotEmpty
    @JsonProperty
    private String alarmDelay;

    @NotEmpty
    @JsonProperty
    private String alarmPasscode;

    @NotEmpty
    @JsonProperty
    private String nightStartTime;

    @NotEmpty
    @JsonProperty
    private String nightEndTime;

    @NotEmpty
    @JsonProperty
    private String groupExperiment;

    public String getTargetTemp() {
        return targetTemp;
    }

    public void setTargetTemp(String targetTemp) {
        this.targetTemp = targetTemp;
    }

    public String getAlarmDelay() {
        return this.alarmDelay;
    }

    public void setAlarmDelay(String alarmDelay) {
        this.alarmDelay = alarmDelay;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getPort() {
        return this.port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() { return name; }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlarmPasscode() { return alarmPasscode;  }

    public void setAlarmPasscode(String alarmPasscode) { this.alarmPasscode = alarmPasscode;  }

    public String getNightStartTime() {
        return nightStartTime;
    }

    public void setNightStartTime(String nightStartTime) {
        this.nightStartTime = nightStartTime;
    }

    public String getNightEndTime() {
        return nightEndTime;
    }

    public void setNightEndTime(String nightEndTime) {
        this.nightEndTime = nightEndTime;
    }

    public String getGroupExperiment() {
        return groupExperiment;
    }

    public void setGroupExperiment(String groupExperiment) {
        this.groupExperiment = groupExperiment;
    }
}
