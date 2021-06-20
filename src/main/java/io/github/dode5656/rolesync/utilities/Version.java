package io.github.dode5656.rolesync.utilities;

public enum Version {

    v1_2_BETA("1.2.0-BETA", true),
    v1_2_BETA_1_1("1.2.0-BETA-1.1", true),
    v1_2_BETA_1_2("1.2.0-BETA-1.2", true),
    v1_2_BETA_1_3("1.2.0-BETA-1.3", false);

    private final String version;
    private final boolean configUpdated;
    Version(String s, boolean configUpdated) {
        this.version = s;
        this.configUpdated = configUpdated;
    }
    public String getVersion() { return version; }
    public boolean configUpdated() { return configUpdated; }

}
