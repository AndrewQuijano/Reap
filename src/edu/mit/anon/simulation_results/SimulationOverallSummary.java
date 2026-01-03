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

package edu.mit.anon.simulation_results;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

// I want this class to track general results of an entire simulation
// This will be exported to one worksheet on Simulation workbook
public class SimulationOverallSummary {

    private final List<Integer> total_compromised_blocks = new ArrayList<>();
    private final List<Integer> total_in_scope_blocks = new ArrayList<>();
    // Header will be block name, status will be the column
    private final HashMap<String, List<Boolean>> block_and_cyber_states = new HashMap<>();

    public void addStatus(String block_name, Boolean compromised) {
        List<Boolean> status = block_and_cyber_states.get(block_name);
        if (status == null) {
            status = new ArrayList<>();
        }
        status.add(compromised);
        block_and_cyber_states.put(block_name, status);
    }

    public List<Boolean> get_block_status(String block_name) {
        return block_and_cyber_states.get(block_name);
    }

    public void add_number_compromised_blocks(int compromised_blocks) {
        total_compromised_blocks.add(compromised_blocks);
    }

    public void add_in_scope_blocks(int in_scope_blocks) {
        total_in_scope_blocks.add(in_scope_blocks);
    }

    public Set<String> getAllBlocks() {
        return this.block_and_cyber_states.keySet();
    }

    public List<Integer> getTotal_compromised_blocks() {
        return this.total_compromised_blocks;
    }

    public List<Integer> getTotal_in_scope_blocks() {
        return this.total_in_scope_blocks;
    }

    public String toString() {
        assert total_compromised_blocks.size() == total_in_scope_blocks.size()
                && total_compromised_blocks.size() == block_and_cyber_states.size()
                : "Sizes of total_compromised_blocks, total_in_scope_blocks, and block_and_cyber_states do not match";

        StringBuilder output;

        // write the header
        output = new StringBuilder("Day,Compromised Blocks,In Scope Blocks");
        for (String block_name : block_and_cyber_states.keySet()) {
            output.append(",").append(block_name);
        }
        output.append('\n');

        // write each row now
        for (int day = 0; day < total_compromised_blocks.size(); day++) {
            output.append(day).append(",");
            output.append(total_compromised_blocks.get(day)).append(",");
            output.append(total_in_scope_blocks.get(day)).append(",");

            for (String block_name : block_and_cyber_states.keySet()) {
                List<Boolean> status = block_and_cyber_states.get(block_name);
                output.append(status.get(day)).append(",");
            }
            output.setLength(output.length() - 1); // Remove the trailing comma
            output.append('\n');
        }
        return output.toString();
    }

    public void toExcelSheet(Workbook workbook, int simulation) {
        assert total_compromised_blocks.size() == total_in_scope_blocks.size()
                && total_compromised_blocks.size() == block_and_cyber_states.size()
                : "Sizes of total_compromised_blocks, total_in_scope_blocks, and block_and_cyber_states do not match";

        Sheet sheet = workbook.createSheet("Simulation " + simulation + " Summary");

        // Create the header row
        Row headerRow = sheet.createRow(0);
        int cellIndex = 0;
        headerRow.createCell(cellIndex++).setCellValue("Day");
        headerRow.createCell(cellIndex++).setCellValue("Compromised Blocks");
        headerRow.createCell(cellIndex++).setCellValue("In Scope Blocks");
        for (String block_name : block_and_cyber_states.keySet()) {
            headerRow.createCell(cellIndex++).setCellValue(block_name);
        }

        // Write each row
        for (int day = 0; day < total_compromised_blocks.size(); day++) {
            Row row = sheet.createRow(day + 1);
            cellIndex = 0;
            row.createCell(cellIndex++).setCellValue(day);
            row.createCell(cellIndex++).setCellValue(total_compromised_blocks.get(day));
            row.createCell(cellIndex++).setCellValue(total_in_scope_blocks.get(day));

            for (String block_name : block_and_cyber_states.keySet()) {
                List<Boolean> status = block_and_cyber_states.get(block_name);
                row.createCell(cellIndex++).setCellValue(status.get(day));
            }
        }
    }

    public static SimulationOverallSummary fromExcelSheet(Workbook workbook, int simulation) {
        SimulationOverallSummary summary = new SimulationOverallSummary();
        Sheet sheet = workbook.getSheet("Simulation " + simulation + " Summary");

        if (sheet == null) {
            throw new IllegalArgumentException("Sheet for simulation " + simulation + " not found");
        }

        // Read the header row
        Row headerRow = sheet.getRow(0);
        int cellIndex = 3; // Start after "Day", "Compromised Blocks", "In Scope Blocks"
        List<String> block_names = new ArrayList<>();
        while (cellIndex < headerRow.getLastCellNum()) {
            block_names.add(headerRow.getCell(cellIndex++).getStringCellValue());
        }

        // Read each row
        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }

            int compromisedBlocks = (int) row.getCell(1).getNumericCellValue();
            int inScopeBlocks = (int) row.getCell(2).getNumericCellValue();

            summary.add_number_compromised_blocks(compromisedBlocks);
            summary.add_in_scope_blocks(inScopeBlocks);

            cellIndex = 3;
            for (String blockName : block_names) {
                boolean status = row.getCell(cellIndex++).getBooleanCellValue();
                summary.addStatus(blockName, status);
            }
        }
        return summary;
    }

    public static List<SimulationOverallSummary> readSummariesFromWorkbook(String filePath) throws IOException {
        List<SimulationOverallSummary> summaries = new ArrayList<>();
        Workbook workbook = WorkbookFactory.create(new File(filePath));

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            String sheetName = sheet.getSheetName();

            if (sheetName.startsWith("Simulation ") && sheetName.endsWith(" Summary")) {
                int simulationNumber = Integer.parseInt(sheetName.split(" ")[1]);
                SimulationOverallSummary summary = SimulationOverallSummary.fromExcelSheet(workbook, simulationNumber);
                summaries.add(summary);
            }
        }

        workbook.close();
        return summaries;
    }
}
