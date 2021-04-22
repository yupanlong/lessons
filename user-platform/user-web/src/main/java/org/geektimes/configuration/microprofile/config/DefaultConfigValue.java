package org.geektimes.configuration.microprofile.config;

import org.eclipse.microprofile.config.ConfigValue;
import org.eclipse.microprofile.config.spi.ConfigSource;

public class DefaultConfigValue implements ConfigValue {

    private final ConfigSource configSource;
    private final String name;

    public DefaultConfigValue(ConfigSource configSource, String name) {
        this.configSource = configSource;
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getValue() {
        return configSource.getValue(this.name);
    }
    @Override
    public String getRawValue() {
        return configSource.getValue(this.name);
    }
    @Override
    public String getSourceName() {
        return configSource.getName();
    }
    @Override
    public int getSourceOrdinal() {
        return configSource.getOrdinal();
    }
}
