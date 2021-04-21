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

package org.noise_planet.noisemodelling.main.experimental_matsim

import groovy.sql.Sql
import org.h2gis.utilities.wrapper.ConnectionWrapper
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.chart.JFreeChart
import org.jfree.chart.plot.XYPlot
import org.jfree.data.xy.XYSeries
import org.jfree.data.xy.XYSeriesCollection
import org.noise_planet.noisemodelling.main.utilities.ScriptI
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.swing.JFrame
import java.awt.BorderLayout
import java.sql.*

class Plot_Exposition_Distribution implements ScriptI {

    String title = 'Plot Exposition Distribution'
    String description = 'Plot a graph displaying the distribution of a chosen field in a previously calculated Matsim agents noise exposition table. Will display a Graph Window on the server'

    Map inputs = [
        expositionsTableName : [
            name: 'Name of the table containing the expositions',
            title: 'Name of the table containing the expositions',
            description: 'Name of the table containing the expositions' +
                    '<br/>The table must contain the following fields : ' +
                    '<br/>PK, PERSON_ID, HOME_FACILITY, HOME_GEOM, WORK_FACILITY, WORK_GEOM, LAEQ, HOME_LAEQ, DIFF_LAEQ',
            type: String.class
        ],
        expositionField: [
            name: 'Field containing noise exposition',
            title: 'Field containing noise exposition',
            description: 'Field containing noise exposition',
            type: String.class
        ],
        otherExpositionField: [
            name: 'Other field containing noise exposition',
            title: 'Other field containing noise exposition',
            description: 'Other field containing noise exposition',
            min: 0,
            max: 1,
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

        String resultString = null

        Logger logger = LoggerFactory.getLogger("org.noise_planet.noisemodelling")
        logger.info('Start : Plot_Exposition_Distribution')
        logger.info("inputs {}", input)

        String expositionsTableName = input['expositionsTableName']
        String expositionField = input['expositionField']

        String otherExpositionField = ""
        if (input['otherExpositionField']) {
            otherExpositionField = input['otherExpositionField']
        }

        Statement stmt = connection.createStatement();

        int startLAeq = 20
        int endLAeq = 100

        XYSeries expositionSeries = new XYSeries("AGENTS Exposition");
        XYSeries otherExpositionSeries = new XYSeries("OTHER Exposition");

        for (int level = startLAeq; level <= endLAeq; level++) {
            String query = '''
                SELECT COUNT(''' + expositionField + ''') * 100 / ( SELECT COUNT(*) FROM ''' + expositionsTableName + ''' WHERE ''' + expositionField + ''' >= 20) as NB_AGENTS
                FROM ''' + expositionsTableName + '''
                WHERE ''' + expositionField + ''' >= ''' + Double.toString(level) + '''
            ''';
            /*
            String query = '''
                SELECT COUNT(''' + expositionField + ''') as NB_AGENTS
                FROM ''' + expositionsTableName + '''
                WHERE ''' + expositionField + ''' >= ''' + Double.toString(level) + '''
            ''';
            */
            ResultSet result = stmt.executeQuery(query);

            while (result.next()) {
                expositionSeries.add(level, result.getInt("NB_AGENTS"))
            }
        }

        if (otherExpositionField != "") {
            for (int level = startLAeq; level <= endLAeq; level++) {

                String query = '''
                    SELECT COUNT(''' + otherExpositionField + ''') * 100 / ( SELECT COUNT(*) FROM ''' + expositionsTableName + ''' WHERE ''' + otherExpositionField + ''' >= 20) as NB_AGENTS
                    FROM ''' + expositionsTableName + '''
                    WHERE ''' + otherExpositionField + ''' >= ''' + Double.toString(level) + '''
                ''';
                /*
                String query = '''
                    SELECT COUNT(''' + expositionField + ''') as NB_AGENTS
                    FROM ''' + expositionsTableName + '''
                    WHERE ''' + otherExpositionField + ''' >= ''' + Double.toString(level) + '''
                ''';
                */
                ResultSet result = stmt.executeQuery(query);

                while (result.next()) {
                    otherExpositionSeries.add(level, result.getInt("NB_AGENTS"))
                }
            }
        }

        XYSeriesCollection dataset = new XYSeriesCollection();

        if (otherExpositionField != "") {
            dataset.addSeries(otherExpositionSeries);
        }
        dataset.addSeries(expositionSeries);
        JFreeChart chart = ChartFactory.createXYAreaChart("Exposition", "LAeq", "% Agents", dataset);
        ChartPanel chartPanel = new ChartPanel(chart);
        XYPlot plot = chart.getXYPlot();
        JFrame f = new JFrame("RESULT");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setLayout(new BorderLayout(0, 5));
        f.add(chartPanel, BorderLayout.CENTER);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);


        logger.info('End : Plot_Exposition_Distribution')
        resultString = "Process done."
        logger.info('Result : ' + resultString)
        return resultString
    }
}