// ------------------------------------------------------------------------------
// --  ______  __________
// --  \    / /_____    /
// --   |  | /      |  |
// --   |  |   --   |  |
// --   |  |  |\/|  |  |
// --   |  |  |/\|  |  |
// --   |  |  |/\|  |  |
// --   |  |   --   |  |
// --   |  |_____ / |  |
// --  /_________/ /____\
// ------------------------------------------------------------------------------
/*
 * MIT License
 *
 * Copyright (c) 2025 MIT Lincoln Laboratory
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package edu.mit.ll.plots;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LineGraph {

    // Use this to create data-set to plot multiple lines over days
    public static XYSeriesCollection createDataset(List<List<Integer>> dataSets,
                                                   List<String> seriesNames) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        for (int j = 0; j < dataSets.size(); j++) {
            List<Integer> data = dataSets.get(j);
            String seriesName = seriesNames.get(j);
            XYSeries series = new XYSeries(seriesName);

            for (int i = 0; i < data.size(); i++) {
                series.add(i + 1, data.get(i)); // Day 1 corresponds to index 0
            }
            dataset.addSeries(series);
        }
        return dataset;
    }

    // Use this to create data-set to plot multiple lines over days
    public static XYSeriesCollection createDataset(HashMap<String, List<Integer>> dataMap) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        for (Map.Entry<String, List<Integer>> entry : dataMap.entrySet()) {
            String seriesName = entry.getKey();
            List<Integer> data = entry.getValue();
            XYSeries series = new XYSeries(seriesName);

            for (int i = 0; i < data.size(); i++) {
                series.add(i + 1, data.get(i)); // Day 1 corresponds to index 0
            }
            dataset.addSeries(series);
        }
        return dataset;
    }

    public static void createLineGraph(XYSeriesCollection dataset,
                                       String title,
                                       String xAxisLabel,
                                       String yAxisLabel,
                                       String outputFileName) throws IOException {

        JFreeChart chart = ChartFactory.createXYLineChart(
                title,
                xAxisLabel,
                yAxisLabel,
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        XYPlot plot = chart.getXYPlot();
        NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
        xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        ChartUtils.saveChartAsPNG(new File(outputFileName), chart, 800, 600);
    }

    public static void createLineGraph(List<Integer> data,
                                       String title,
                                       String xAxisLabel,
                                       String yAxisLabel,
                                       String series_name,
                                       String outputFileName) throws IOException {

        XYSeries series = new XYSeries(series_name);

        for (int i = 0; i < data.size(); i++) {
            series.add(i + 1, data.get(i)); // Day 1 corresponds to index 0
        }

        XYSeriesCollection dataset = new XYSeriesCollection(series);

        JFreeChart chart = ChartFactory.createXYLineChart(
                title,
                xAxisLabel,
                yAxisLabel,
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        // Enforce x-axis to only go up by integer (since we aren't factoring in half a day, etc.)
        XYPlot plot = chart.getXYPlot();
        NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
        xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        // We repeat the same idea for y-axis given that the input is an integer list
        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        ChartUtils.saveChartAsPNG(new File(outputFileName), chart, 800, 600);
    }

    public static void main(String[] args) throws IOException {
        List<Integer> infectedPeople = List.of(10, 20, 30, 40, 50, 60, 70);
        createLineGraph(infectedPeople,
                "COVID-19 Infections Over Time",
                "Day",
                "Number of Infected People",
                "infected people",
                "covid_infections.png");

        List<Integer> recoveredPeople = List.of(5, 15, 25, 35, 45, 55, 65);
        List<List<Integer>> dataSets = List.of(infectedPeople, recoveredPeople);
        List<String> seriesNames = List.of("Infected People", "Recovered People");

        XYSeriesCollection dataset = createDataset(dataSets, seriesNames);

        LineGraph.createLineGraph(dataset,
                "COVID-19 Infections and Recoveries Over Time",
                "Day",
                "Number of People",
                "covid_infections_recoveries.png");
    }
}