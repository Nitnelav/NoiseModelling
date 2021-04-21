package org.noise_planet.noisemodelling.main.utilities

import java.sql.Connection

interface ScriptI {

    String getTitle();
    String getDescription();
    Map getInputs();
    Map getOutputs();

    String exec(Connection connection, inputs);
}