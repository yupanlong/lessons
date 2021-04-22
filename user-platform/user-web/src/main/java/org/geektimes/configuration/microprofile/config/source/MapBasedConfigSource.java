package org.geektimes.configuration.microprofile.config.source;

import org.eclipse.microprofile.config.spi.ConfigSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class MapBasedConfigSource implements ConfigSource {

    private final String name;

    private final int ordinal;

    private final Map<String, String> configData;

    protected MapBasedConfigSource(String name, int ordinal) {
        this.name = name;
        this.ordinal = ordinal;
        this.configData = new HashMap<>();
    }

    /**
     * 获取配置数据 Map
     * @return 不可变 Map 类型的配置数据
     */
    @Override
    public Map<String, String> getProperties() {
        try {
            prepareConfigData(configData);
        } catch (Throwable throwable) {
            throw new IllegalStateException("准备配置数据发生错误！",throwable);
        }
        return Collections.unmodifiableMap(configData);
    }

    /**
     * 准备配置数据
     *
     * @param configData
     * @throws Throwable
     */
    protected abstract void prepareConfigData(Map configData) throws Throwable;

    @Override
    public Set<String> getPropertyNames() {
        return configData.keySet();
    }

    @Override
    public String getValue(String propertyName) {
        return configData.get(propertyName);
    }
    @Override
    public String getName() {
        return name;
    }
    @Override
    public final int getOrdinal() {
        return ordinal;
    }

}
