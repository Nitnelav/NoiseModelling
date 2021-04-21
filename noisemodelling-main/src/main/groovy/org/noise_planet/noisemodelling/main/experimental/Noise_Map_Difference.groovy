/**
 * NoiseModelling is an open-source tool designed to produce environmental noise maps on very large urban areas. It can be used as a Java library or be controlled through a user friendly web interface.
 *
 * This version is developed by Université Gustave Eiffel and CNRS
 * <http://noise-planet.org/noisemodelling.html>
 *
 * NoiseModelling is distributed under GPL 3 license. You can read a copy of this License in the file LICENCE provided with this software.
 *
 * Contact: contact@noise-planet.org
 *
 */
/**
 * @Author Valentin Le Bescond, Université Gustave Eiffel
 */

package org.noise_planet.noisemodelling.main.experimental

import groovy.sql.Sql
import org.h2gis.utilities.wrapper.ConnectionWrapper
import org.noise_planet.noisemodelling.main.utilities.ScriptI
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.sql.Connection

class Noise_Map_Difference implements ScriptI {

    String title = 'Map Difference'
    String description = 'Map Difference.'

    Map inputs = [
            mainMapTable : [
                    name: 'Primary map table name',
                    title: 'Primary map table name',
                    description: 'Name of the table containing the primary noise map data.' +
                            '<br/>The table must contain the following fields : ' +
                            '<br/>PK, THE_GEOM, HZ63, HZ125, HZ250, HZ500, HZ1000, HZ2000, HZ4000, HZ8000, LAEQ, LEQ',
                    type: String.class
            ],
            secondMapTable: [
                    name: 'Secondary map table name',
                    title: 'Secondary map table name',
                    description: 'Name of the table containing the second noise map data.' +
                            '<br/>The table must contain the following fields : ' +
                            '<br/>PK, THE_GEOM, HZ63, HZ125, HZ250, HZ500, HZ1000, HZ2000, HZ4000, HZ8000, LAEQ, LEQ',
                    type: String.class
            ],
            invert: [
                    name: 'Invert the substraction',
                    title: 'Invert the substraction ?',
                    description: 'Invert the substraction ?' +
                            '<br/>False (default) : Primary map - Second map' +
                            '<br/>True : Second map - Primary map',
                    min: 0,
                    max: 1,
                    type: Boolean.class
            ],
            outTable: [
                    name: 'Output table name',
                    title: 'Name of created table',
                    description: 'Name of the table you want to create' +
                            '<br/>The table will contain the following fields : ' +
                            '<br/>PK, THE_GEOM, HZ63, HZ125, HZ250, HZ500, HZ1000, HZ2000, HZ4000, HZ8000, LAEQ, LEQ',
                    type: String.class
            ]
    ]

    Map outputs = [
            result: [
                    name: 'Result output string',
                    title: 'Result output string',
                    description: 'This type of result does not allow the blocks to be linked together.',
                    type: String.class
            ]
    ]

    // main function of the script
    String exec(Connection connection, input) {

        connection = new ConnectionWrapper(connection)

        Sql sql = new Sql(connection)

        String resultString

        Logger logger = LoggerFactory.getLogger("org.noise_planet.noisemodelling")
        logger.info('Start : Noise_Map_Difference')
        logger.info("inputs {}", input)

        String mainMapTable = input['mainMapTable']
        String secondMapTable = input['secondMapTable']

        boolean invert = false;
        if (input['invert']) {
            invert = input['invert'] as boolean;
        }

        String outTable = input['outTable']

        sql.execute(String.format("DROP TABLE IF EXISTS %s", outTable))

        String query = "CREATE TABLE " + outTable + '''(
                PK integer PRIMARY KEY,
                THE_GEOM geometry,
                HZ63 double precision,
                HZ125 double precision,
                HZ250 double precision,
                HZ500 double precision,
                HZ1000 double precision,
                HZ2000 double precision,
                HZ4000 double precision,
                HZ8000 double precision,
                LAEQ double precision,
                LEQ double precision
            ) AS
            SELECT mmt.IDRECEIVER, mmt.THE_GEOM,
            ''' + (invert ? "- " : "") + '''(mmt.HZ63 - smt.HZ63) as HZ63,
            ''' + (invert ? "- " : "") + '''(mmt.HZ125 - smt.HZ125) as HZ125,
            ''' + (invert ? "- " : "") + '''(mmt.HZ250 - smt.HZ250) as HZ250,
            ''' + (invert ? "- " : "") + '''(mmt.HZ500 - smt.HZ500) as HZ500,
            ''' + (invert ? "- " : "") + '''(mmt.HZ1000 - smt.HZ1000) as HZ1000,
            ''' + (invert ? "- " : "") + '''(mmt.HZ2000 - smt.HZ2000) as HZ2000,
            ''' + (invert ? "- " : "") + '''(mmt.HZ4000 - smt.HZ4000) as HZ4000,
            ''' + (invert ? "- " : "") + '''(mmt.HZ8000 - smt.HZ8000) as HZ8000,
            ''' + (invert ? "- " : "") + '''(mmt.LEQA - smt.LEQA) as LAEQ,
            ''' + (invert ? "- " : "") + '''(mmt.LEQ - smt.LEQ) as LEQ
            FROM ''' + mainMapTable + ''' mmt
            LEFT JOIN ''' + secondMapTable + ''' smt
            ON mmt.IDRECEIVER = smt.IDRECEIVER
        ;'''

        sql.execute(query)

        logger.info('End : Noise_Map_Difference')
        resultString = "Process done. Table " + outTable + " created !"
        logger.info('Result : ' + resultString)
        return resultString;
    }
}