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

package reap.input_validation;

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
