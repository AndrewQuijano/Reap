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

package edu.mit.anon.reap;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;

import edu.mit.anon.probability.Gaussian;
import edu.mit.anon.probability.Uniform;
import edu.mit.anon.simulation_results.SimulationBlockSummary;
import edu.mit.anon.simulation_results.SimulationOverallSummary;
import edu.mit.anon.sysml.BDDInfo;
import edu.mit.anon.sysml.InstInfo;
import edu.mit.anon.ui.ConfigInput;
import edu.mit.anon.ui.MainMenu;
import edu.mit.anon.ui.SelectBlock;
import edu.mit.anon.ui.SlotInput;

import javax.annotation.CheckForNull;
import javax.swing.*;

import static edu.mit.anon.input_validation.FileIO.*;
import static edu.mit.anon.input_validation.InputValidation.*;
import static edu.mit.anon.plots.LineGraph.createDataset;
import static edu.mit.anon.plots.LineGraph.createLineGraph;
import static edu.mit.anon.plots.Toukey.createBoxAndWhiskerPlot;
import static edu.mit.anon.reap.Reap.selectWorkingDirectory;
import static edu.mit.anon.reap.Reap.showWorkingDirectoryDialog;
import static edu.mit.anon.sysml.BDDInfo.*;
import static edu.mit.anon.sysml.ValueProperty.*;
import static edu.mit.anon.sysml.logging.log;

import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jfree.data.xy.XYSeriesCollection;

public class OpenReap extends MDAction {

    private HashMap<String, HashMap<String, Double>> block_validation;
    private HashMap<String, Object> config;
    private static final String NONE = "None";
    private static final String THREAT_LIKELIHOOD_KEY = "threat_likelihood";
    // Maps: map[block][security_property] = HTTPS, etc.
    private final HashMap<String, HashMap<String, String>> security_property_sheet = new HashMap<>();
    private final Set<Class> selected_blocks = new HashSet<>();
    private final Set<Class> starting_blocks = new HashSet<>();
    private final HashMap<String, Double> threat_likelihood_by_block = new HashMap<>();

    private Project project;
    private DiagramPresentationElement bdd;

    public OpenReap() {
        super("reap", "Run Risk Evaluation Assessment Plugin", null, null);
        this.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("res/export.png"))));
        // Kyle said remove this for now.
        // List<KeyStroke> shortcut = new ArrayList<>();
        // shortcut.add(KeyStroke.getKeyStroke(';', InputEvent.SHIFT_DOWN_MASK));
        // this.setShortcuts(shortcut);
    }


    public boolean validate_blocks(InstInfo instInfo) throws IOException, ReadOnlyElementException {

        for (Class block: instInfo.blockMultiplicity.keySet()) {
            for (String security_property_key : block_validation.keySet()) {

                // First, I need to confirm the tag name exists
                Object value = getValueProperty(block, security_property_key);
                if (value == null) {
                    JOptionPane.showMessageDialog(null, "The block " + block.getName() + " is missing the " + security_property_key + " tag!");
                    return false;
                }

                // I will assume all properties we need customers to set are strings...
                if (!(value instanceof String tag_value)) {
                    JOptionPane.showMessageDialog(null, "The block " + block.getName() + " has an invalid " + security_property_key + " tag (It isn't a String)!");
                    return false;
                }
                else {
                    // The Hashmap contains all possible values for a specific security property and its weight adjustment
                    HashMap<String, Double> validating_values = block_validation.get(security_property_key);
                    if (!validating_values.containsKey(tag_value)) {
                        JOptionPane.showMessageDialog(null, "The block " + block.getName() + " has an invalid value " + value + " on the tag name: " + security_property_key + "!");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    // Run your simulation
    public void simulation(Project project, InstInfo instInfo,
                           String output_directory)
            throws IOException, ReadOnlyElementException {

        // Parse out stuff from config file
        // This should have been validated earlier
        int number_of_simulations = (Integer) config.get("simulations");
        int days = (Integer) config.get("days");
        String distribution = (String) config.get("distribution");
        double patch_probability = (Double) config.get("patch_likelihood");
        int uniform_size = (Integer) config.get("uniform_size");
        int rto = (Integer) config.get("rto");
        int rpo = (Integer) config.get("rpo");
        boolean populate_blocks = (Boolean) config.get("populate_blocks");
        Uniform uniform = new Uniform(uniform_size);

        // Based on Security property, change default threat_likelihood
        adjust_threat(project, instInfo);

        // For each block, I want the column summary of Simulations too
        List<SimulationOverallSummary> simulation_results = new ArrayList<>();
        List<Class> all_new_targets = new ArrayList<>();
        HashMap<String, Boolean> current_block_states = new HashMap<>();

        // For each simulation...
        for (int simulation = 0; simulation < number_of_simulations; simulation++) {

            SimulationOverallSummary current_simulation = new SimulationOverallSummary();

            // For EVERY block, track if this block is compromised or not
            // Assume no block is compromised on the first day
            for (Class block: instInfo.blockMultiplicity.keySet()) {
                current_block_states.put(block.getName(), Boolean.FALSE);
            }

            // For each in-scope block, run the simulation for each day
            for (int i = 0; i < days; i++) {
                // For each day I need to check if a new block is in-scope of getting compromised now
                for (Class in_scope_block : selected_blocks) {
                    Boolean is_compromised = current_block_states.get(in_scope_block.getName());

                    // If the block is compromised, add all neighbors to be in-scope
                    if (is_compromised) {
                        List<Class> new_neighbors = searchConnectedBlocks(in_scope_block);
                        StringBuilder debug = new StringBuilder();
                        for (Class block : new_neighbors) {
                            debug.append(block.getName()).append(";");
                        }
                        log("Block " + in_scope_block.getName() + " is compromised, adding neighbors: " + debug);
                        all_new_targets.addAll(new_neighbors);
                    }
                }

                // Remember, you can't update a list while iterating in a for loop!
                // For good measure, I'll clear the new_targets list once I am done
                // I also record the number of blocks that can now be attacked for this day
                selected_blocks.addAll(all_new_targets);
                current_simulation.add_in_scope_blocks(selected_blocks.size());
                all_new_targets.clear();

                // Iterate through every block for the day i
                for (Class block: instInfo.blockMultiplicity.keySet()) {
                    double pwned_probability;
                    String block_name = block.getName();

                    // Otherwise, check the current status, likely false
                    Boolean is_compromised = current_block_states.get(block_name);

                    // If you are in-scope, roll the die if you got compromised
                    if (selected_blocks.contains(block)) {
                        // Attacker Model
                        // If you aren't hacked already, I am going after you
                        if (!is_compromised) {
                            pwned_probability = threat_likelihood_by_block.get(block_name);

                            // Update the block with the new cyber_state
                            if (distribution.equals("normal")) {
                                if(Gaussian.eventOccurred(pwned_probability, 0, 1)) {
                                    is_compromised = Boolean.TRUE;
                                }
                            }
                            else if (distribution.equals("uniform")) {
                                if (uniform.eventOccurred(pwned_probability)) {
                                    is_compromised = Boolean.TRUE;
                                }
                            }
                        }
                    }

                    // TODO: Race condition, a block can be patched and compromised in the same day
                    // Defender Model, applied to all blocks
                    // If P (Clean) happens, set Clean regardless.
                    // Consider these regular patching updates or already planned vulnerability fixes
                    if (distribution.equals("normal")) {
                        if (Gaussian.eventOccurred(patch_probability, 0, 1)) {
                            is_compromised = Boolean.FALSE;
                        }
                    }
                    else if (distribution.equals("uniform")) {
                        if (uniform.eventOccurred(patch_probability)) {
                            is_compromised = Boolean.FALSE;
                        }
                    }

                    // Collect the status of the block for the specific day
                    current_simulation.addStatus(block_name, is_compromised);
                    current_block_states.put(block_name, is_compromised);
                }

                // I need to update the in-scope blocks
                // You may lose in-scope blocks because some blocks got patched,
                // But you should ALWAYS have your initial block be vulnerable
                selected_blocks.clear();
                selected_blocks.addAll(starting_blocks);
                for (Class block: instInfo.blockMultiplicity.keySet()) {
                    // If the block is compromised,
                    // next day, we will check its neighbors to attempt to infect
                    if (current_block_states.get(block.getName())) {
                        selected_blocks.add(block);
                    }
                }

                // At the end of the day, check some last piece of data for overall simulation results
                int compromised_blocks = count_compromised_blocks(current_block_states);
                current_simulation.add_number_compromised_blocks(compromised_blocks);
            }
            // for loop, days for the block in-scope
            simulation_results.add(current_simulation);
            log("Finished running simulation " + simulation + " for all blocks in-scope");
        }

        // Should have same number of simulations
        assert number_of_simulations == simulation_results.size();

        // Write out a new Excel sheet of simulation results
        try (Workbook workbook = new XSSFWorkbook()) {
            for (int simulation = 0; simulation < number_of_simulations; simulation++) {
                SimulationOverallSummary sim = simulation_results.get(simulation);

                // Get the worksheet and add it to the workbook
                sim.toExcelSheet(workbook, simulation);

                XYSeriesCollection plot = createDataset(
                        List.of(sim.getTotal_in_scope_blocks(), sim.getTotal_compromised_blocks()),
                        List.of("In-Scope Blocks", "Compromised Blocks")
                );

                createLineGraph(
                        plot,
                        "Simulation " + simulation + " block compromised state",
                        "Day",
                        "Number of Blocks",
                        output_directory
                                + FileSystems.getDefault().getSeparator()
                                + "simulation_" + simulation + ".png"
                );
            }

            // Save the workbook to a file
            try (FileOutputStream fileOut = new FileOutputStream(output_directory
                    + FileSystems.getDefault().getSeparator() + "simulation_summary.xlsx")) {
                workbook.write(fileOut);
            }
        }

        // Write out a new Excel sheet of simulation data - by block though
        HashMap<String, SimulationBlockSummary> simulation_block_data = compute_block_summary(simulation_results, output_directory);

        // Suggesting the blocks should record average results of simulation on the block.
        for (Class block: instInfo.blockMultiplicity.keySet()) {
            SimulationBlockSummary block_summary = simulation_block_data.get(block.getName());
            block_summary.compute_summary(block_summary.get_first_day_compromised());
            if (populate_blocks) {
                createValueProperty(project, block, "Average_Times_Pwned", block_summary.average);
            }

            // Check if RPO compliant, minimum acceptable data loss
            block_summary.compute_summary(block_summary.get_days_data_lost());
            if (populate_blocks) {
                createValueProperty(project, block, "Average_Days_Data_Lost", block_summary.average);
            }

            if (rpo >= block_summary.average) {
                if (populate_blocks) {
                    createValueProperty(project, block, "RPO_Compliant", Boolean.TRUE);
                }
            }
            else {
                if (populate_blocks) {
                    createValueProperty(project, block, "RPO_Compliant", Boolean.FALSE);
                }
            }

            // Check if RTO compliant, minimum acceptable downtime
            block_summary.compute_summary(block_summary.get_days_compromised());
            if (populate_blocks) {
                createValueProperty(project, block, "Average_Days_Pwned", block_summary.average);
            }
            if (rto >= block_summary.average) {
                if (populate_blocks) {
                    createValueProperty(project, block, "RTO_Compliant", Boolean.TRUE);
                }
            }
            else {
                if (populate_blocks) {
                    createValueProperty(project, block, "RTO_Compliant", Boolean.FALSE);
                }
            }

            // For each block I want to box plot days compromised
            String output_graph = Paths.get(output_directory,
                    block.getName() + " days compromised.png").toString();

            createBoxAndWhiskerPlot(block.getName(), block_summary.get_days_compromised(),
                    "Days compromised for " + block.getName() + " over all simulations",
                    "Block",
                    "Days",
                    output_graph);

            // For each block, I want to box plot times compromised
            output_graph = Paths.get(output_directory,
                    block.getName() + " times compromised.png").toString();

            createBoxAndWhiskerPlot(block.getName(), block_summary.get_days_compromised(),
                    "Number of times compromised for " + block.getName() + " over all simulations",
                    "Block",
                    "Amount of times hacked",
                    output_graph);
        }
    }

    private HashMap<String, SimulationBlockSummary> compute_block_summary(
            List<SimulationOverallSummary> simulation_results,
                                       String output_directory) throws IOException {

        HashMap<String, SimulationBlockSummary> simulation_block_data = new HashMap<>();
        int rto = (Integer) config.get("rto");
        int rpo = (Integer) config.get("rpo");
        int backup_frequency = (Integer) config.get("backup_frequency");
        Set<String> all_blocks = simulation_results.get(0).getAllBlocks();
        int number_of_simulations = simulation_results.size();
        String suffix = "_rto=" + rto + "_rpo=" + rpo + "_backup_freq=" + backup_frequency;

        // Finished the simulation, collect the results of this simulation
        // with the perspective for each block
        for (SimulationOverallSummary current_simulation : simulation_results) {
            for (String block_name : all_blocks) {
                // If not initialized, do so
                if (simulation_block_data.get(block_name) == null) {
                    simulation_block_data.put(block_name, new SimulationBlockSummary(rto, rpo));
                }

                List<Boolean> block_states = current_simulation.get_block_status(block_name);
                simulation_block_data.get(block_name).days_compromised(block_states); // RTO
                // You can get -1 if no first day ever occurred
                simulation_block_data.get(block_name).first_day_compromised(block_states);
                simulation_block_data.get(block_name).times_compromised(block_states);
                simulation_block_data.get(block_name).total_days_lost(backup_frequency, block_states); // RPO
            }
        }

        // Create overall RTO/RPO sheet
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("RTO-RPO Analysis");

            // Create the header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Block Name");
            headerRow.createCell(1).setCellValue("RPO Compliant out of " + number_of_simulations + " simulations");
            headerRow.createCell(2).setCellValue("RTO Compliant out of " + number_of_simulations + " simulations");

            int i = 0;
            for (String block_name : all_blocks) {
                SimulationBlockSummary block_summary = simulation_block_data.get(block_name);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(block_name);
                row.createCell(1).setCellValue(block_summary.get_simulations_rpo_passed());
                row.createCell(2).setCellValue(block_summary.get_simulations_rto_passed());
                i++;
            }

            // Save the workbook to a file
            try (FileOutputStream fileOut = new FileOutputStream(
                    output_directory
                            + FileSystems.getDefault().getSeparator() +
                            "RPO_RTO_Compliance" + suffix + ".xlsx")) {
                workbook.write(fileOut);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Write out a new Excel sheet of simulation results, but based on blocks
        try (Workbook workbook = new XSSFWorkbook()) {
            for (String block_name : all_blocks) {
                SimulationBlockSummary block_summary = simulation_block_data.get(block_name);
                block_summary.toExcelSheet(workbook, block_name, rpo, rto);
            }

            // Save the workbook to a file
            try (FileOutputStream fileOut = new FileOutputStream(output_directory
                    + FileSystems.getDefault().getSeparator() + "simulation_block_view" + suffix + ".xlsx")) {
                workbook.write(fileOut);
            }
        }
        return simulation_block_data;
    }

    public int count_compromised_blocks(HashMap<String, Boolean> current_block_states) {
        int compromised_blocks = 0;
        for (String current_block: current_block_states.keySet()) {
            if (current_block_states.get(current_block)) {
                ++compromised_blocks;
            }
        }
        return compromised_blocks;
    }

    public void init_blocks(Project project, InstInfo instInfo) throws IOException {
        // Make sure all blocks have cyber_state to false
        for(Class block: instInfo.blockMultiplicity.keySet()) {
            try {
                Object o;
                double pwned_probability;
                o = config.get(THREAT_LIKELIHOOD_KEY);
                pwned_probability = (Double) o;
                createValueProperty(project, block, THREAT_LIKELIHOOD_KEY, pwned_probability);
                security_property_sheet.put(block.getName(), new HashMap<>());

                // Set some security properties now too, if it doesn't exist...
                for (String security_property_key : block_validation.keySet()) {
                    o = getValueProperty(block, security_property_key);
                    if (o == null) {
                        createValueProperty(project, block, security_property_key, NONE);
                        security_property_sheet.get(block.getName()).put(security_property_key, NONE);
                    }
                    else {
                        security_property_sheet.get(block.getName()).put(security_property_key, (String) o);
                    }
                }
            }
            catch (ReadOnlyElementException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void adjust_threat(Project project, InstInfo instInfo) {
        // Make sure all blocks have cyber_state to false
        for(Class block: instInfo.blockMultiplicity.keySet()) {
            try {
                // Get the updated value of threat_likelihood
                Object o = getValueProperty(block, THREAT_LIKELIHOOD_KEY);
                if (o == null) {
                    continue;
                }
                double pwned_likelihood = (Double) o;

                for (Map.Entry<String, HashMap<String, Double>> security_properties : block_validation.entrySet()) {
                    String security_property_name = security_properties.getKey();
                    HashMap<String, Double> security_property_values_and_weights = security_properties.getValue();

                    // Get the Security Property and multiply the correct value
                    String security_property_value = (String) getValueProperty(block, security_property_name);
                    double modifier = security_property_values_and_weights.get(security_property_value);
                    log("Updating threat likelihood for " + security_property_name + " with modifier " + modifier, "Warning", "warn");
                    pwned_likelihood *= modifier;
                }

                // Write the update value to the block
                threat_likelihood_by_block.put(block.getName(), pwned_likelihood);
                createValueProperty(project, block, THREAT_LIKELIHOOD_KEY, pwned_likelihood);
            }
            catch (ReadOnlyElementException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // Consider this your main function that executes the moment button is pressed
    @Override
    public void actionPerformed(@CheckForNull ActionEvent actionEvent) {

        Project project = Application.getInstance().getProject();
        if(project != null) {
            this.project = project;
            log("There is a project open.", "Warning", "warn");
            DiagramPresentationElement bdd = project.getActiveDiagram();
            if(bdd == null) {
                JOptionPane.showMessageDialog(null, "There is no active diagram open!");
                return;
            }
            else {
                this.bdd = bdd;
                log("There is an active diagram", "Warning", "warn");
            }

            showWorkingDirectoryDialog();
            String current_directory = System.getProperty("user.dir");

            try {
                if (check_blocks_json()) {
                    if(!check_config()) {
                        log(current_directory + FileSystems.getDefault().getSeparator() + CONFIG_FILE + " is not found/invalid!", "Error", "error");
                        JOptionPane.showMessageDialog(null, current_directory + FileSystems.getDefault().getSeparator() + CONFIG_FILE + " is not found/invalid!");
                        return;
                    }
                }
                else {
                    log(current_directory + FileSystems.getDefault().getSeparator() + BLOCKS_JSON + " is not found/invalid!", "Error", "error");
                    JOptionPane.showMessageDialog(null, current_directory + FileSystems.getDefault().getSeparator() + BLOCKS_JSON + " is not found/invalid!");
                    return;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Read initial files from configuration
            try {
                block_validation = read_validating_json(System.getProperty("user.dir") + FileSystems.getDefault().getSeparator() + BLOCKS_JSON);
                config = read_config_json(System.getProperty("user.dir") + FileSystems.getDefault().getSeparator() + CONFIG_FILE);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
            showMainMenu();
        }
        else {
            JOptionPane.showMessageDialog(null, "There is no open project");
            log("There is no project open.", "Warning", "warn");
        }
    }

    private void showMainMenu() {
        boolean exit = false;
        while (!exit) {
            MainMenu mainMenu = new MainMenu();
            mainMenu.setVisible(true);

            // Handle actions based on user selection
            if (mainMenu.isAdjustConfigSelected()) {
                adjustConfig();
            } else if (mainMenu.isAdjustWeightsSelected()) {
                adjustWeights();
            } else if (mainMenu.isSetBlocksSelected()) {
                setBlocks();
            } else if (mainMenu.isRunSimulationSelected()) {
                // Get all the current information of blocks on the current diagram
                if (starting_blocks == null || starting_blocks.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "No blocks selected! Click select Entry Point first!");
                    continue;
                }
                long startTime = System.currentTimeMillis();
                runSimulation();
                starting_blocks.clear();
                selected_blocks.clear();
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;

                long seconds = (duration / 1000) % 60;
                long minutes = (duration / (1000 * 60)) % 60;
                long hours = (duration / (1000 * 60 * 60)) % 24;

                String message = String.format("Execution time: %02d hours, %02d minutes, %02d seconds", hours, minutes, seconds);
                JOptionPane.showMessageDialog(null, message);
            } else if (mainMenu.isRecomputeSelected()) {
                recompute_outputs();
            } else if (mainMenu.exitSelected()) {
                exit = true;
            }
        }
    }

    private void adjustConfig() {
        ConfigInput update_config = new ConfigInput(config);
        update_config.setVisible(true);
        config = update_config.getConfig();
    }

    private void adjustWeights() {
        SlotInput security = new SlotInput(block_validation);
        security.setVisible(true);
        block_validation = security.getCurrentWeights();
    }

    private void setBlocks() {
        starting_blocks.clear();
        SelectBlock block_input = new SelectBlock(bdd);
        block_input.setVisible(true);
        starting_blocks.addAll(block_input.getSelectedBlocks());
        selected_blocks.addAll(starting_blocks);
    }

    /**
     * The goal of this function is to read results from past simulations.
     * I want to check if with different RPO/RTO than I used, would it still be compliant or not?
     */
    public void recompute_outputs() {
        // Input: Get a directory with existing results
        String simulation_directory = "";
        List<SimulationOverallSummary> all_sims = null;

        // Get all simulations and block names
        boolean fileFound = false;
        while (!fileFound) {
            simulation_directory = selectWorkingDirectory(
                    """
                    Select Working Directory
                    Ideally, this folder has information of a past simulation run.
                    """
            );

            try {
                all_sims = SimulationOverallSummary.readSummariesFromWorkbook(
                        simulation_directory +
                                FileSystems.getDefault().getSeparator() + "simulation_summary.xlsx"
                );
                fileFound = true; // File found, exit the loop
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null,
                        "File simulation_summary.xlsx not found in the selected directory. " +
                        "Please select another directory.");
            }
        }
        try {
            compute_block_summary(all_sims, simulation_directory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void runSimulation() {

        // Your existing simulation code here
        BDDInfo bddInfo = getAllBlockInfo(bdd);
        bddInfo.removeDerivedBlocks();

        // For each block I found in the diagram, recursively find any other child blocks
        InstInfo instInfo = new InstInfo();
        for (Class parent : bddInfo.getBlocks()) {
            instInfo.blockMultiplicity.put(parent, 1);
            instInfo.instCounts.put(parent, 1);
            instInfo.ignore.addAll(bddInfo.removedDerived);
            InstInfo.searchBlocks(instInfo, parent);
        }

        // Make sure all blocks have certain Value properties set
        try {
            init_blocks(project, instInfo);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Get the current timestamp
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

        // Create the directory name with the timestamp
        String directoryName = now.format(formatter);
        Path path = Paths.get(System.getProperty("user.dir"), directoryName);
        String pickedBlocksPath = Paths.get(directoryName, "starting_blocks.txt").toString();

        try {
            Files.createDirectory(path);
            // Note I am storing the initial blocks selected
            // once simulation starts, this can now extend to neighbors, so to avoid confusion,
            // let's just save this info now.
            writeListToFile(pickedBlocksPath, starting_blocks);
            System.out.println("Directory created: " + path.toAbsolutePath());
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to create directory");
        }

        // Do your input validation on any Blocks or Edges
        // if all input validations are OK, run your simulation
        try {
            if (validate_blocks(instInfo)) {
                simulation(project, instInfo, directoryName);
            }
        } catch (ReadOnlyElementException | IOException e) {
            throw new RuntimeException(e);
        }

        // Save all simulation results
        String configPath = Paths.get(directoryName, "config.json").toString();
        String blockPath = Paths.get(directoryName, "blocks.json").toString();

        try {
            // Dump inputs to the folder
            dumpHashMapToJson(configPath, config);
            dumpHashMapToJson(blockPath, block_validation);

            // Save all security properties of all blocks
            writeHashMapToExcel(security_property_sheet,
                    Paths.get(directoryName, "security_properties.xlsx").toString());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
