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

package reap.input_validation;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.HashMap;

import static reap.sysml.logging.log;
import static reap.input_validation.FileIO.read_config_json;
import static reap.input_validation.FileIO.read_validating_json;


public class InputValidation {

    public static final String CONFIG_FILE = "config.json";
    public static final String BLOCKS_JSON = "blocks.json";

    // Check that config.json is built as expected
    public static boolean check_config() throws IOException {
        HashMap<String, Object> config_json;
        config_json = read_config_json(System.getProperty("user.dir") + FileSystems.getDefault().getSeparator() + CONFIG_FILE);

        // Iterate over keys, it is a messy check, but it works...
        for (String key : config_json.keySet()) {
            if (!check_config_key(key, config_json.get(key))) {
                return false;
            }
        }
        log("Whoa you survived my input validation!!!");
        return true;
    }

    public static boolean check_config_key(String key, Object value) {
        final String invalid_type_message = "Invalid data type on " + key + ": " + value.getClass().getName();
        final String invalid_value_message = "Invalid value on " + key + ": " + value;
        final String unknown_key = "I am not expecting this key value: " + key + "! Why is it here?";
        switch (key) {
            case "populate_blocks" -> {
                if (!(value instanceof Boolean)) {
                    log(invalid_type_message, "Warning", "warn");
                    JOptionPane.showMessageDialog(null, invalid_type_message);
                    return false;
                }
                else {
                    return true;
                }
            }
            case "distribution" -> {
                if (!(value instanceof String string_value)) {
                    log(invalid_type_message, "Warning", "warn");
                    JOptionPane.showMessageDialog(null, invalid_type_message);
                    return false;
                }
                else {
                    if (string_value.equals("normal")) {
                        log("Normal Distribution selected", "Warning", "warn");
                        return true;
                    } else if (string_value.equals("uniform")) {
                        log("Uniform Distribution selected", "Warning", "warn");
                        return true;
                    } else {
                        log(invalid_value_message, "Warning", "warn");
                        JOptionPane.showMessageDialog(null, invalid_value_message);
                        return false;
                    }
                }
            }
            case "threat_likelihood", "patch_likelihood" -> {
                if (!(value instanceof Double)) {
                    log(invalid_type_message, "Warning", "warn");
                    JOptionPane.showMessageDialog(null, invalid_type_message);
                    return false;
                } else {
                    double probability = (Double) value;
                    if (probability < 0 || probability > 1) {
                        log(invalid_value_message, "Warning", "warn");
                        JOptionPane.showMessageDialog(null, invalid_value_message);
                        return false;
                    } else {
                        return true;
                    }
                }
            }
            case "days", "simulations", "uniform_size", "backup_frequency", "rto", "rpo" -> {
                if (!(value instanceof Integer)) {
                    log(invalid_type_message, "Warning", "warn");
                    JOptionPane.showMessageDialog(null, invalid_type_message);
                    return false;
                } else {
                    int number = (Integer) value;
                    if (number < 0) {
                        log(invalid_value_message, "Warning", "warn");
                        JOptionPane.showMessageDialog(null, invalid_value_message);
                        return false;
                    } else {
                        return true;
                    }
                }
            }
            default -> {
                log(unknown_key, "Warning", "warn");
                JOptionPane.showMessageDialog(null, unknown_key);
                return false;
            }
        }
    }

    // Check that the Block followed Input Validation
    // Check that config.json is built as expected
    public static boolean check_blocks_json() throws IOException {

        HashMap<String, HashMap<String, Double>> blocks_json;
        blocks_json = read_validating_json(System.getProperty("user.dir") + FileSystems.getDefault().getSeparator() + BLOCKS_JSON);

        // Iterate over keys, it is a messy check, but it works...
        for (String security_property_name : blocks_json.keySet()) {
            HashMap<String, Double> security_properties_and_weights = blocks_json.get(security_property_name);

            // Check if the entry has at least one sub-entry
            if (security_properties_and_weights.isEmpty()) {
                log("No sub-entries found for " + security_property_name, "Warning", "warn");
                return false;
            }

            // Check if the entry contains "None": 1.0, This means no security property set
            // So just default no threat likelihood reduction for this block
            if (!security_properties_and_weights.containsKey("None") || security_properties_and_weights.get("None") != 1.0) {
                log("\"None\": 1.0 entry missing or incorrect for " + security_property_name, "Warning", "warn");
                return false;
            }

            for (String sub_key : security_properties_and_weights.keySet()) {
                // Make sure each sub input has a weight between [0, 1].
                // The idea is that a bad security property will NOT improve your score (x1)
                // But if you improve security, you multiply with a value less than 1 to decrease the likelihood of being compromised
                // Somehow if you fully mitigate a threat, then just having a value of 0 to make threat likelihood zero is OK too, I guess
                double multiplier = security_properties_and_weights.get(sub_key);
                if (multiplier < 0 || multiplier > 1) {
                    log("Invalid multiplier selected for " + sub_key, "Warning", "warn");
                    return false;
                }
            }
        }
        return true;
    }
}
