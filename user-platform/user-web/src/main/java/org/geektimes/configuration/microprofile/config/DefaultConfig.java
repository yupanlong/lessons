package org.geektimes.configuration.microprofile.config;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigValue;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.Converter;

import java.util.*;

public class DefaultConfig implements Config {

    private final List<ConfigSource> configSourceList =  new LinkedList<>();
    private Comparator<ConfigSource>  configSourceComparator = new Comparator<ConfigSource>() {
        @Override
        public int compare(ConfigSource o1, ConfigSource o2) {
            return Integer.compare(o1.getOrdinal(),o2.getOrdinal());
        }
    };
    public DefaultConfig() {
        ClassLoader classLoader = getClass().getClassLoader();
        ServiceLoader<ConfigSource> configSources = ServiceLoader.load(ConfigSource.class, classLoader);
        configSources.forEach(configSourceList::add);
        // 排序
        configSourceList.sort(configSourceComparator);
    }

    @Override
    public <T> T getValue(String propertyName, Class<T> propertyType) {
        String propertyValue = getPropertyValue(propertyName);
        // propertyValue ::   String --> propertyType
        // 做类型转换
        return null;
    }

    @Override
    public ConfigValue getConfigValue(String propertyName) {

        return null;
    }
    protected String getPropertyValue(String propertyName){
        String propertyValue = null;
        for (ConfigSource configSource : configSourceList) {
            propertyValue= configSource.getValue(propertyName);
            if (propertyValue != null ){
                break;
            }
        }
        return propertyValue;
    }

    @Override
    public <T> Optional<T> getOptionalValue(String propertyName, Class<T> propertyType) {
        return Optional.empty();
    }

    @Override
    public Iterable<String> getPropertyNames() {
        return null;
    }

    @Override
    public Iterable<ConfigSource> getConfigSources() {
        // 内部可变的集合不要直接暴露出去
        return Collections.unmodifiableList(configSourceList);
    }

    @Override
    public <T> Optional<Converter<T>> getConverter(Class<T> forType) {
        return Optional.empty();
    }

    @Override
    public <T> T unwrap(Class<T> type) {
        return null;
    }
}
