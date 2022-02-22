package com.projecki.economy.data;

import java.util.Properties;

public interface DataEngine {

    Properties getProperties();

    /**
     * Close this engine releasing all resources
     * including connections to databases, threads,
     * I/O streams etc.
     */
    void close();
}
