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
 * DISTRIBUTION STATEMENT A. Approved for public release. Distribution is unlimited.
 * This material is based upon work supported by the Dept of the Navy under Air
 * Force Contract No. FA8702-15-D-0001 or FA8702-25-D-B002.
 * Any opinions, findings, conclusions or recommendations expressed in this material
 * are those of the author(s) and do not necessarily reflect the views of the Dept
 * of the Navy.
 * (c) 2024 Massachusetts Institute of Technology.
 * The software/firmware is provided to you on an As-Is basis.
 * Delivered to the U.S. Government with Unlimited Rights, as defined in DFARS Part
 * 252.227-7013 or 7014 (Feb 2014).
 * Notwithstanding any copyright notice, U.S. Government rights in this work are
 * defined by DFARS 252.227-7013 or DFARS 252.227-7014 as detailed above.
 * Use of this work other than as specifically authorized by the U.S. Government may
 * violate any copyrights that exist in this work.
 */

package edu.mit.ll.simulation_results;

import org.apache.poi.ss.usermodel.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// I want this class to track general results of a specific block over all simulations
// This will be exported to one worksheet for each block in the final workbook
public class SimulationBlockSummary {

    double min;
    double q1;
    double q2;
    double q3;
    double max;
    public double average;

    private final List<Integer> total_days_compromised = new ArrayList<>();
    private final List<Integer> first_day_compromised = new ArrayList<>();
    private final List<Integer> times_compromised = new ArrayList<>();
    private final List<Integer> days_data_lost = new ArrayList<>();

    private int simulations_rto_passed = 0;
    private int simulations_rpo_passed = 0;

    private final int rto;
    private final int rpo;

    public SimulationBlockSummary(int rto, int rpo) {
        this.rto = rto;
        this.rpo = rpo;
    }

    public int get_simulations_rto_passed() {
        return simulations_rto_passed;
    }

    public int get_simulations_rpo_passed() {
        return simulations_rpo_passed;
    }

    public List<Integer> get_days_compromised() {
        return this.total_days_compromised;
    }

    public List<Integer> get_first_day_compromised() {
        return this.first_day_compromised;
    }

    public List<Integer> get_days_data_lost() {
        return this.days_data_lost;
    }

    public String toString() {
        assert total_days_compromised.size() == first_day_compromised.size()
                && total_days_compromised.size() == times_compromised.size()
                : "Sizes of days_compromised, first_day_compromised, and times_compromised do not match";

        StringBuilder output = new StringBuilder();
        output.append("First Day Compromised,Days Compromised,Times Compromised,Total Days of Data Lost\n");

        for (int i = 0; i < total_days_compromised.size(); i++) {
            output.append(first_day_compromised.get(i)).append(",");
            output.append(total_days_compromised.get(i)).append(",");
            output.append(times_compromised.get(i)).append(",");
            output.append(days_data_lost.get(i));
            output.append("\n");
        }
        return output.toString();
    }

    public void toExcelSheet(Workbook workbook, String block_name, int rpo, int rto) {
        assert total_days_compromised.size() == first_day_compromised.size()
                && total_days_compromised.size() == times_compromised.size()
                : "Sizes of days_compromised, first_day_compromised, and times_compromised do not match";

        Sheet sheet = workbook.createSheet(block_name);

        // Create the header row
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("First Day Compromised");
        headerRow.createCell(1).setCellValue("Days Compromised (RTO)");
        headerRow.createCell(2).setCellValue("Times Compromised");
        headerRow.createCell(3).setCellValue("Total Days of Data Lost (RPO)");

        // Write each row
        for (int i = 0; i < total_days_compromised.size(); i++) {
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(first_day_compromised.get(i));

            // Create the cell for days_compromised
            Cell cellDaysCompromised = row.createCell(1);
            cellDaysCompromised.setCellValue(total_days_compromised.get(i)); // RTO

            // Create a new CellStyle for days_compromised
            CellStyle styleDaysCompromised = workbook.createCellStyle();

            // Set the cell color based on the value of days_compromised
            // This is confirming if you are RTO compliant - not above minimum downtime
            if (total_days_compromised.get(i) > rto) {
                styleDaysCompromised.setFillForegroundColor(IndexedColors.RED.getIndex());
            } else {
                styleDaysCompromised.setFillForegroundColor(IndexedColors.GREEN.getIndex());
            }
            styleDaysCompromised.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Apply the style to the cell for days_compromised
            cellDaysCompromised.setCellStyle(styleDaysCompromised);

            // Create cell for number of times compromised
            row.createCell(2).setCellValue(times_compromised.get(i));

            // Create the cell for days_data_lost
            Cell cellDaysDataLost = row.createCell(3);
            cellDaysDataLost.setCellValue(days_data_lost.get(i)); // RPO

            // Create a new CellStyle for days_data_lost
            CellStyle styleDaysDataLost = workbook.createCellStyle();

            // Set the cell color based on the value of days_data_lost
            // This is confirming if you are RPO compliant - the not above minimum data lost
            if (days_data_lost.get(i) > rpo) {
                styleDaysDataLost.setFillForegroundColor(IndexedColors.RED.getIndex());
            } else {
                styleDaysDataLost.setFillForegroundColor(IndexedColors.GREEN.getIndex());
            }
            styleDaysDataLost.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Apply the style to the cell for days_data_lost
            cellDaysDataLost.setCellStyle(styleDaysDataLost);
        }
    }

    // Note to avoid issues with '-1' from days compromised if it never occurred, just remove it
    public void compute_summary(List<Integer> results) {
        results.removeIf(n -> n < 0);
        Collections.sort(results);
        average = computeAverage(results);

        if (results.isEmpty()) {
            min = 0;
            q1 = 0;
            q2 = 0;
            q3 = 0;
            max = 0;
            return;
        }
        else if (results.size() == 1) {
            min = results.get(0);
            q1 = results.get(0);
            q2 = results.get(0);
            q3 = results.get(0);
            max = results.get(0);
            return;
        }

        int n = results.size();
        min = results.get(0);
        q1 = median(results, 0, n / 2);
        q2 = median(results, 0, n);
        q3 = median(results, (n + 1) / 2, n);
        max = results.get(n - 1);
    }

    public static double computeAverage(List<Integer> list) {
        if (list == null) {
            throw new IllegalArgumentException("List must not be null");
        }
        if (list.isEmpty()) {
            return 0.0;
        }

        double sum = 0;
        for (int num : list) {
            sum += num;
        }

        double average = sum / list.size();
        return Math.round(average * 100.0) / 100.0;
    }

    private static double median(List<Integer> sortedList, int start, int end) {
        int length = end - start;
        if (length % 2 == 0) {
            return (sortedList.get(start + length / 2 - 1) + sortedList.get(start + length / 2)) / 2.0;
        }
        else {
            return sortedList.get(start + length / 2);
        }
    }

    public void first_day_compromised(List<Boolean> is_compromised) {
        int day = 0;

        // Check the first day it was compromised
        for (Boolean status : is_compromised) {
            if (status) {
                first_day_compromised.add(day);
                return;
            }
            ++day;
        }
        // It was never compromised! Yay!
        first_day_compromised.add(-1);
    }

    public void days_compromised(List<Boolean> is_compromised) {
        int days_compromised = 0;

        // Check total days it was compromised
        for (Boolean status : is_compromised) {
            if (status) {
                ++days_compromised;
            }
        }
        if (this.rto > days_compromised) {
            ++simulations_rto_passed;
        }
        total_days_compromised.add(days_compromised);
    }

    public void times_compromised(List<Boolean> is_compromised) {
        int number_of_times_compromised = 0;

        // Check the number of state changes
        Boolean previous_state = Boolean.FALSE;
        for (Boolean status: is_compromised) {
            if (status != previous_state) {
                // I only want to count the number of times I got pwned
                // Not include the number of times I got patched
                if (previous_state == Boolean.FALSE) {
                    ++number_of_times_compromised;
                }
            }
            previous_state = status;
        }

        // Place results
        times_compromised.add(number_of_times_compromised);
    }

    // Note, we treat groups of cyber_state=True as part of one down-time
    public void total_days_lost(int backup_frequency, List<Boolean> is_compromised) {
        int total_days_of_data_lost = 0;
        int last_backup_day = 0;
        int days = is_compromised.size();

        for (int day = 0; day < days;) {
            if (day % backup_frequency == 0) {
                last_backup_day = day;
            }

            if (is_compromised.get(day)) {
                int outage_start = day;
                int outage_end = day;
                while (outage_end < days && is_compromised.get(outage_end)) {
                    outage_end++;
                }
                int data_lost = outage_start - last_backup_day;
                total_days_of_data_lost += data_lost;
                // Skip to the end of compromised state
                day = outage_end;
                // Update last backup day to avoid double counting
                last_backup_day = outage_end - (outage_end % backup_frequency);
            } else {
                day++;
            }
        }

        // Place results
        days_data_lost.add(total_days_of_data_lost);
        if (rpo > total_days_of_data_lost) {
            ++simulations_rpo_passed;
        }
    }
}
