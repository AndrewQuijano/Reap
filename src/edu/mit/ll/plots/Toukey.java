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
import org.jfree.chart.annotations.CategoryTextAnnotation;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static edu.mit.ll.sysml.logging.log;


public class Toukey {

    public static List<Integer> generateRandomNumbers(int count) {
        List<Integer> numbers = new ArrayList<>(count);
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            numbers.add(random.nextInt(0, 100));
        }
        return numbers;
    }

    // Metrics are:
    // 1- Number of times compromised for a block in all simulations
    // 2- Number of days compromised for a block in all simulations
    // 3- First day compromised (we will skip since the nulls can cause issues)
    public static void createBoxAndWhiskerPlot(List<List<Integer>> data) throws IOException {
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        for (int i = 0; i < data.size(); i++) {
            dataset.add(data.get(i), "Component-" + (i + 1), "Component-" + (i + 1));
        }

        JFreeChart chart = ChartFactory.createBoxAndWhiskerChart(
                "Days Compromised",
                "Components", // x-axis label
                "Days",
                dataset,
                true
        );

        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        BoxAndWhiskerRenderer renderer = new BoxAndWhiskerRenderer();
        renderer.setMeanVisible(false); // Hide the mean marker
        plot.setRenderer(renderer);

        // Iterate through all rows in the dataset
        for (int row = 0; row < dataset.getRowCount(); row++) {
            for (int column = 0; column < dataset.getColumnCount(); column++) {
                log("Processing row " + row + ", column " + column);

                // Calculate the five-number summary for each row and column
                Number min = dataset.getMinRegularValue(row, column);
                Number q1 = dataset.getQ1Value(row, column);
                Number median = dataset.getMedianValue(row, column);
                Number q3 = dataset.getQ3Value(row, column);
                Number max = dataset.getMaxRegularValue(row, column);

                if (min == null || q1 == null || median == null || q3 == null || max == null) {
                    System.out.println("Skipping row " + row + ", column " + column + " because it contains null values");
                    continue;
                }

                // Add annotations for the five-number summary
                addAnnotation(plot, "Min: " + min, min.doubleValue(), (String) dataset.getRowKey(row));
                addAnnotation(plot, "Q1: " + q1, q1.doubleValue(), (String) dataset.getRowKey(row));
                addAnnotation(plot, "Median: " + median, median.doubleValue(), (String) dataset.getRowKey(row));
                addAnnotation(plot, "Q3: " + q3, q3.doubleValue(), (String) dataset.getRowKey(row));
                addAnnotation(plot, "Max: " + max, max.doubleValue(), (String) dataset.getRowKey(row));
            }
        }
        ChartUtils.saveChartAsPNG(new File("box_and_whisker_plot.png"), chart, 800, 600);
    }

    // Metrics are:
    // 1- Number of times compromised for a block in all simulations
    // 2- Number of days compromised for a block in all simulations
    // 3- First day compromised (we will skip since the nulls can cause issues)
    public static void createBoxAndWhiskerPlot(String block_name, List<Integer> data,
                                               String plot_title,
                                               String x_axis,
                                               String y_axis,
                                               String output_file_name) throws IOException {
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();

        // I want the component to either be the block name or the simulation number
        dataset.add(data, block_name, block_name);


        JFreeChart chart = ChartFactory.createBoxAndWhiskerChart(
                plot_title, // This should change depending on metric plotted (e.g., number of days compromised)
                x_axis, // x-axis label should only be Components or Simulations
                y_axis, // y-axis label should change depending on metrically plotted (e.g., number of days compromised)
                dataset,
                true
        );

        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        BoxAndWhiskerRenderer renderer = new BoxAndWhiskerRenderer();
        renderer.setMeanVisible(false); // Hide the mean marker
        plot.setRenderer(renderer);

        // Iterate through all rows in the dataset
        for (int row = 0; row < dataset.getRowCount(); row++) {
            for (int column = 0; column < dataset.getColumnCount(); column++) {
                log("Processing row " + row + ", column " + column);

                // Calculate the five-number summary for each row and column
                Number min = dataset.getMinRegularValue(row, column);
                Number q1 = dataset.getQ1Value(row, column);
                Number median = dataset.getMedianValue(row, column);
                Number q3 = dataset.getQ3Value(row, column);
                Number max = dataset.getMaxRegularValue(row, column);

                if (min == null || q1 == null || median == null || q3 == null || max == null) {
                    System.out.println("Skipping row " + row + ", column " + column + " because it contains null values");
                    continue;
                }

                // Add annotations for the five-number summary
                addAnnotation(plot, "Min: " + min, min.doubleValue(), (String) dataset.getRowKey(row));
                addAnnotation(plot, "Q1: " + q1, q1.doubleValue(), (String) dataset.getRowKey(row));
                addAnnotation(plot, "Median: " + median, median.doubleValue(), (String) dataset.getRowKey(row));
                addAnnotation(plot, "Q3: " + q3, q3.doubleValue(), (String) dataset.getRowKey(row));
                addAnnotation(plot, "Max: " + max, max.doubleValue(), (String) dataset.getRowKey(row));
            }
        }
        // Need to save this with an output directory and plot name (probably a combination of block and metric)
        ChartUtils.saveChartAsPNG(new File(output_file_name), chart, 800, 600);
    }

    private static void addAnnotation(CategoryPlot plot, String label, double value, String category) {
        CategoryTextAnnotation annotation = new CategoryTextAnnotation(label, category, value);
        annotation.setFont(new Font("SansSerif", Font.PLAIN, 10));
        annotation.setTextAnchor(TextAnchor.BASELINE_LEFT);
        plot.addAnnotation(annotation);
    }

    public static void main(String[] args) throws IOException {
        List<List<Integer>> randomNumbers = new ArrayList<>();
        randomNumbers.add(generateRandomNumbers(100));
        randomNumbers.add(generateRandomNumbers(100));
        randomNumbers.add(generateRandomNumbers(100));
        randomNumbers.add(generateRandomNumbers(100));
        createBoxAndWhiskerPlot(randomNumbers);
    }
}