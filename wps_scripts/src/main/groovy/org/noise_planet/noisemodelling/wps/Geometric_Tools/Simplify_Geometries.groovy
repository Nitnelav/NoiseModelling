/**
 * @Author Tomáš Anda
 */

package org.noise_planet.noisemodelling.wps.Geometric_Tools

import geoserver.GeoServer
import geoserver.catalog.Store
import org.geotools.jdbc.JDBCDataStore

import java.sql.Connection

script = new org.noise_planet.noisemodelling.main.geometric_tools.Simplify_Geometries()

title = script.getTitle()
description = script.getDescription()

inputs = script.getInputs()
outputs = script.getOutputs()

static Connection openGeoserverDataStoreConnection(String dbName) {
    if (dbName == null || dbName.isEmpty()) {
        dbName = new GeoServer().catalog.getStoreNames().get(0)
    }
    Store store = new GeoServer().catalog.getStore(dbName)
    JDBCDataStore jdbcDataStore = (JDBCDataStore) store.getDataStoreInfo().getDataStore(null)
    return jdbcDataStore.getDataSource().getConnection()
}

def run(input) {

    // Get name of the database
    // by default an embedded h2gis database is created
    // Advanced user can replace this database for a postGis or h2Gis server database.
    String dbName = "h2gisdb"

    // Open connection
    openGeoserverDataStoreConnection(dbName).withCloseable {
        Connection connection ->
            return [result: script.exec(connection, input)]
    }
}