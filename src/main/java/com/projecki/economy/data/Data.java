package com.projecki.economy.data;

import org.tinylog.Logger;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

import static com.google.common.base.Preconditions.*;

public final class Data {

    private static File dir;
    private static final Map<String, DataEngine> LOADED_ENGINES = new HashMap<>();
    private static final List<String> TEMPLATES = List.of("mysql-config.properties");

    public static boolean isInitialized() {
        return dir != null;
    }

    /**
     * Initialize the config files to the configs folder
     * saving all the defaults if they do not already exist.
     *
     * @param configDir The directory to save the configs to.
     */
    public static void initialize(File configDir) {

        checkState(dir == null, "data is already initialized");
        dir = new File(configDir, "configs");
        Logger.info("Data directory: {}", dir);
        Logger.debug("Checking default templates...");
        if (!dir.exists() && dir.mkdirs()) {
            // Save these only if the directory doesn't exist
            // That will allow for changing of default directory
            // names without new ones being created
            Logger.debug("Configuration directory did not exist. Created.");
            saveResource("mysql-config.properties",
                    new File(dir, "mysql-config.properties"));
        }

        Logger.info("Data has been initialized");
    }

    /**
     * Get the engine that is keyed to the given key. If there
     * is no engine in existence for the key then it will be used
     * as the name or the configuration properties file ({@code key.properties})
     * and the appropriate {@link MySQLEngine} will be created.
     *
     * @param key The key to get the {@link MySQLEngine engine} for.
     * @return The engine mapped to the key.
     * @throws ClassCastException If the type is not the correct type of data
     *                            engine that is saved or the properties file
     *                            is for a different type.
     * @throws RuntimeException If the properties file for the key could not be found.
     * @see MySQLEngine
     */
    public static synchronized <E extends DataEngine> E getEngine(String key) {
        checkState(dir != null, "not initialized");
        // Load from primary folder
        return getEngine(dir, key);
    }

    /**
     * Close all the loaded {@link DataEngine DataEngines}
     */
    public static void close() {
        LOADED_ENGINES.values().forEach(DataEngine::close);
    }

    private static <E extends DataEngine> E getEngine(File configs, String key) {

        DataEngine engine = LOADED_ENGINES.get(key);
        if (engine != null) {

            try {
                return (E) engine;
            } catch (ClassCastException e) {
                throw new ClassCastException("An engine exists under '" + key + "', but it is of the type " +
                        engine.getClass().getSimpleName());
            }
        }

        File target = new File(configs, key + ".properties");
        checkArgument(target.exists(),
                "Engine file not found for key %s in directory %s",
                key, configs.getAbsolutePath());
        try (InputStream stream = new BufferedInputStream(new FileInputStream(target))) {
            Properties config = new Properties();
            config.load(stream);
            EngineType type = EngineType.identify(target.getName(), config);
            checkArgument(type != null, "Invalid or unknown engine for type %s", key);
            config.remove("engine.type");
            engine = type.create(config);
            LOADED_ENGINES.put(key, engine);
            return (E) engine;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void saveResource(String resourcePath, File target) {

        checkArgument(resourcePath != null && !resourcePath.isEmpty(),
                "path cannot be null or empty");
        resourcePath = resourcePath.replace('\\', '/');
        if (target.exists()) {
            Logger.error("Could not save {} to {} because {} already exists.",
                    target.getName(), target, target.getName());
            return;
        }

        try (OutputStream out = new FileOutputStream(target);
             InputStream in = getResource(resourcePath)) {

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static InputStream getResource(String filename) {

        checkNotNull(filename);
        try {
            URL url = Data.class.getClassLoader().getResource(filename);
            checkArgument(url != null, "resource not found %s", filename);
            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            return connection.getInputStream();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public enum EngineType {

        MYSQL(MySQLEngine::new, "mysql", "maria");

        private final List<String> identifiers;
        private final Function<Properties, DataEngine> creator;

        EngineType(Function<Properties, DataEngine> creator, String... identifiers) {
            this.creator = creator;
            this.identifiers = List.of(identifiers);
        }

        public DataEngine create(Properties properties) {
            return creator.apply(properties);
        }

        public static EngineType identify(String name, Properties properties) {
            EngineType identify = identify(name);
            return identify != null ? identify : identify(properties);
        }

        public static EngineType identify(String name) {

            name = name.toLowerCase();
            for (EngineType type : values()) {

                for (String identifier : type.identifiers) {

                    if (name.contains(identifier)) {
                        return type;
                    }
                }
            }

            return null;
        }

        public static EngineType identify(Properties properties) {

            if (properties.containsKey("engine.type")) {
                return identify(properties.getProperty("engine.type"));
            }

            if (properties.containsKey("dataSource.serverName")) {
                return MYSQL;
            }

            return null;
        }
    }
}
