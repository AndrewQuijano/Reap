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

package edu.mit.ll.input_validation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.Map;

public class FileIO {

    // Read the blocks.json
    public static HashMap<String, HashMap<String, Double>> read_validating_json(String json_file) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        // Read JSON file and convert to Map<String, HashMap<String, Double>>
        return objectMapper.readValue(new File(json_file), new TypeReference<>() {
        });
    }

    // Read the config.json
    public static HashMap<String, Object> read_config_json(String json_file) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        // Read JSON file and convert to HashMap<String, (Double, Integer, String)>
        return objectMapper.readValue(new File(json_file), new TypeReference<>() {
        });
    }

    public static void dumpHashMapToJson(String filePath, HashMap<?, ?> data) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(data);
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(json);
        }
    }

    public static void writeListToFile(String filePath, Set<Class> block_list) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            for (Class block : block_list) {
                writer.write(block.getName() + '\n');
            }
        }
    }

    public static void writeHashMapToExcel(HashMap<String, HashMap<String, String>> data,
                                           String filePath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Security Properties");

            // Write header row
            Row headerRow = sheet.createRow(0);
            int cellIndex = 1; // Start from 1 to leave the first cell for row keys
            for (String innerKey : data.values().iterator().next().keySet()) {
                Cell cell = headerRow.createCell(cellIndex++);
                cell.setCellValue(innerKey);
            }

            // Write data rows
            int rowIndex = 1;
            for (Map.Entry<String, HashMap<String, String>> outerEntry : data.entrySet()) {
                Row row = sheet.createRow(rowIndex++);
                Cell rowKeyCell = row.createCell(0);
                rowKeyCell.setCellValue(outerEntry.getKey());

                cellIndex = 1;
                for (String value : outerEntry.getValue().values()) {
                    Cell cell = row.createCell(cellIndex++);
                    cell.setCellValue(value);
                }
            }

            // Write the output to a file
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }
        }
    }
}
