package i.valerii_timakov.serial_monitor.services;

import i.valerii_timakov.serial_monitor.dto.Propery;
import i.valerii_timakov.serial_monitor.utils.Log;
import lombok.Getter;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

public class SettingsService {

    private static final String PATH = "app.properties";

    private static final Map<Propery, Object> DEFAULT_VALUES = Map.of(
        Propery.Delimiter, "\r\n",
        Propery.MaybeConversationLogPath, Optional.of("C:/Users/valti/Projects/conversation.log"),
        Propery.MaxMessageWaitSeconds, 30,
        Propery.AddTimestampToTextMessageLog, true,
        Propery.AddDirectionToTextMessageLog, true,
        Propery.MessageDelay, 100L
    );

    private final Map<Propery, Object> properties = new HashMap<>();

    public void init() {
        properties.putAll(DEFAULT_VALUES);
        try (InputStream is = new FileInputStream(PATH)) {
            Properties loadProperties = new Properties();
            loadProperties.load(is);
            loadProperties.stringPropertyNames().forEach(k -> {
                Propery propery = Propery.valueOf(k);
                Object value = propery.getDeserializer() != null
                    ? propery.getDeserializer().apply(loadProperties.getProperty(k))
                    : loadProperties.getProperty(k);
                properties.put(propery, value);
            });
        } catch (IOException e) {
            Log.error("Error loading properties from file!", e);
        }
    }

    private static final class StringEntry implements Map.Entry<String, String> {

        @Getter
        private final String key;
        @Getter
        private final String value;

        public StringEntry(Map.Entry<Propery, Object> entry) {
            this.key = entry.getKey().name();
            this.value = entry.getKey().getSerializer() != null
                    ? entry.getKey().getSerializer().apply(entry.getValue())
                    : (String) entry.getValue();
        }

        @Override
        public String setValue(String value) {
            throw new UnsupportedOperationException("Entry is immutable!");
        }
    }

    public void save() {
        Properties propsToSave = new Properties();
        propsToSave.putAll(properties.entrySet().stream().map(StringEntry::new).collect(Collectors.toMap(
            StringEntry::getKey, StringEntry::getValue
        )));
        File f = new File(PATH);
        try (OutputStream os = new FileOutputStream(f)) {
            Log.debug("Saving properties to: " + f.getAbsolutePath());
            propsToSave.store(os, "");
        } catch (IOException e) {
            Log.error("Error loading properties from file!", e);
        }
    }

    public <T> T get(Propery propery) {
        return (T) properties.get(propery);
    }

    public <T> void set(Propery propery, T value) {
        properties.put(propery, value);
    }

}
