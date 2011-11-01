package eu.isas.peptideshaker.gui.tabpanels;

import com.compomics.util.Util;
import com.compomics.util.experiment.identification.Identification;
import com.compomics.util.experiment.identification.matches.ProteinMatch;
import com.compomics.util.gui.dialogs.ProgressDialogX;
import com.compomics.util.gui.renderers.AlignedListCellRenderer;
import eu.isas.peptideshaker.gui.ExportGraphicsDialog;
import eu.isas.peptideshaker.gui.HelpDialog;
import eu.isas.peptideshaker.gui.PeptideShakerGUI;
import eu.isas.peptideshaker.myparameters.PSParameter;
import eu.isas.peptideshaker.utils.BareBonesBrowserLaunch;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import no.uib.jsparklines.data.JSparklinesDataSeries;
import no.uib.jsparklines.data.JSparklinesDataset;
import no.uib.jsparklines.extra.HtmlLinksRenderer;
import no.uib.jsparklines.extra.NimbusCheckBoxRenderer;
import no.uib.jsparklines.extra.TrueFalseIconRenderer;
import no.uib.jsparklines.renderers.JSparklinesBarChartTableCellRenderer;
import no.uib.jsparklines.renderers.JSparklinesTableCellRenderer;
import no.uib.jsparklines.renderers.util.BarChartColorRenderer;
import org.apache.commons.math.distribution.HypergeometricDistributionImpl;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryMarker;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer3D;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.Layer;

/**
 * The PeptideShaker GO Enrichment Analysis (GO EA) tab.
 * 
 * @author Harald Barsnes
 */
public class GOEAPanel extends javax.swing.JPanel {

    /**
     * The progress dialog.
     */
    private ProgressDialogX progressDialog;
    /**
     * PeptideShaker GUI parent.
     */
    private PeptideShakerGUI peptideShakerGUI;
    /**
     * The GO mappings table column header tooltips.
     */
    private ArrayList<String> mappingsTableToolTips;
    /**
     * GO table tooltips.
     */
    TreeMap<String, Integer> totalGoTermUsage;
    /**
     * The distribution chart panel.
     */
    ChartPanel distributionChartPanel = null;
    /**
     * The significance chart panel.
     */
    ChartPanel signChartPanel = null;

    /** 
     * Creates a new GOEAPanel.
     * 
     * @param peptideShakerGUI 
     */
    public GOEAPanel(PeptideShakerGUI peptideShakerGUI) {

        this.peptideShakerGUI = peptideShakerGUI;

        initComponents();
        setupGUI();

        // load the go mapping files
        loadMappingFiles();
    }

    /**
     * Set up the GUI details.
     */
    private void setupGUI() {

        JTableHeader header = goMappingsTable.getTableHeader();
        header.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                if (peptideShakerGUI.getIdentification() != null) {
                    updateGoPlots();
                }
            }
        });


        goMappingFilesJComboBox.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));

        proteinGoMappingsScrollPane.getViewport().setOpaque(false);
        goMappingsTable.getTableHeader().setReorderingAllowed(false);
        goMappingsTable.setAutoCreateRowSorter(true);

        // the index column
        goMappingsTable.getColumn("").setMaxWidth(60);
        goMappingsTable.getColumn("").setMinWidth(60);
        goMappingsTable.getColumn(" ").setMaxWidth(30);
        goMappingsTable.getColumn(" ").setMinWidth(30);
        goMappingsTable.getColumn("  ").setMaxWidth(30);
        goMappingsTable.getColumn("  ").setMinWidth(30);

        // cell renderers
        goMappingsTable.getColumn(" ").setCellRenderer(new TrueFalseIconRenderer(
                new ImageIcon(this.getClass().getResource("/icons/accept.png")),
                null,
                "Significant", "Not Significant"));
        goMappingsTable.getColumn("  ").setCellRenderer(new NimbusCheckBoxRenderer());
        goMappingsTable.getColumn("GO Accession").setCellRenderer(new HtmlLinksRenderer(peptideShakerGUI.getSelectedRowHtmlTagFontColor(), peptideShakerGUI.getNotSelectedRowHtmlTagFontColor()));
        goMappingsTable.getColumn("Frequency All (%)").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 100.0, Color.RED));
        ((JSparklinesBarChartTableCellRenderer) goMappingsTable.getColumn("Frequency All (%)").getCellRenderer()).showNumberAndChart(true, peptideShakerGUI.getLabelWidth());
        goMappingsTable.getColumn("Frequency Dataset (%)").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 100.0, peptideShakerGUI.getSparklineColor()));
        ((JSparklinesBarChartTableCellRenderer) goMappingsTable.getColumn("Frequency Dataset (%)").getCellRenderer()).showNumberAndChart(true, peptideShakerGUI.getLabelWidth());
        goMappingsTable.getColumn("p-value").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 1.0, peptideShakerGUI.getSparklineColor()));
        ((JSparklinesBarChartTableCellRenderer) goMappingsTable.getColumn("p-value").getCellRenderer()).showNumberAndChart(true, peptideShakerGUI.getLabelWidth());
        goMappingsTable.getColumn("Log2 Diff").setCellRenderer(new JSparklinesBarChartTableCellRenderer(
                PlotOrientation.HORIZONTAL, -10.0, 10.0, Color.RED, peptideShakerGUI.getSparklineColor(), Color.GRAY, 0));
        ((JSparklinesBarChartTableCellRenderer) goMappingsTable.getColumn("Log2 Diff").getCellRenderer()).showNumberAndChart(true, peptideShakerGUI.getLabelWidth());
        goMappingsTable.getColumn("Frequency (%)").setCellRenderer(new JSparklinesTableCellRenderer(
                JSparklinesTableCellRenderer.PlotType.barChart,
                PlotOrientation.HORIZONTAL, 0.0, 100.0));

        // make the tabs in the tabbed pane go from right to left
        goPlotsTabbedPane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        // set up the table header tooltips
        mappingsTableToolTips = new ArrayList<String>();
        mappingsTableToolTips.add(null);
        mappingsTableToolTips.add("Gene Ontology Accession");
        mappingsTableToolTips.add("Gene Ontology Term");
        mappingsTableToolTips.add("Gene Ontology Domain");
        mappingsTableToolTips.add("Frequency All (%)");
        mappingsTableToolTips.add("Frequency Dataset (%)");
        mappingsTableToolTips.add("Frequency (%) (All & Dataset))");
        mappingsTableToolTips.add("Log2 Difference (Dataset / All)");
        mappingsTableToolTips.add("Hypergeometic Test p-value");
        mappingsTableToolTips.add("Significance after Multiple Hypothesis Testing Correction");
        mappingsTableToolTips.add("Selected for Plots");
    }

    /**
     * Load the mapping files.
     */
    private void loadMappingFiles() {

        String mappingsFolderPath = peptideShakerGUI.getJarFilePath() + File.separator + "conf"
                + File.separator + "gene_ontology" + File.separator;

        File mappingsFolder = new File(mappingsFolderPath);
        File[] files = mappingsFolder.listFiles();

        Vector<String> mappingFiles = new Vector<String>();

        for (int i = 0; i < files.length; i++) {
            if (files[i].getAbsolutePath().endsWith(".go_mappings")) {
                mappingFiles.add(files[i].getName());
            }
        }

        goMappingFilesJComboBox.setModel(new DefaultComboBoxModel(mappingFiles));
    }

    /**
     * Update the GO mappings.
     * 
     * @param progressDialog    the progress dialog
     * @param closeProgressBar  if true the progress bar will be closed when the method finishes
     */
    public void displayResults(ProgressDialogX progressDialog, boolean closeProgressBar) {

        progressDialog.setIndeterminate(true);
        progressDialog.setTitle("Getting GO Mapping Files. Please Wait...");

        // clear old table
        while (goMappingsTable.getRowCount() > 0) {
            ((DefaultTableModel) goMappingsTable.getModel()).removeRow(0);
        }

        String selectedMappings = (String) goMappingFilesJComboBox.getSelectedItem();
        String goMappingsPath = peptideShakerGUI.getJarFilePath() + File.separator + "conf"
                + File.separator + "gene_ontology" + File.separator + selectedMappings;
        String selectedGoDomains = selectedMappings.substring(0, selectedMappings.indexOf(".")) + ".go_domains";
        String goDomainsPath = peptideShakerGUI.getJarFilePath() + File.separator + "conf"
                + File.separator + "gene_ontology" + File.separator + selectedGoDomains;


        File goMappingsFile = new File(goMappingsPath);

        if (!goMappingsFile.exists()) {
            JOptionPane.showMessageDialog(this, "Mapping file \"" + goMappingsFile.getName() + "\" not found!", "File Not Found", JOptionPane.ERROR_MESSAGE);
            return;
        }

        totalGoTermUsage = new TreeMap<String, Integer>();
        TreeMap<String, Integer> datasetGoTermUsage = new TreeMap<String, Integer>();
        HashMap<String, String> goTermToAccessionMap = new HashMap<String, String>();
        HashMap<String, ArrayList<String>> proteinToGoMappings = new HashMap<String, ArrayList<String>>();
        HashMap<String, String> goDomainMap = new HashMap<String, String>();

        int totalNumberOfProteins = 0;

        try {

            File goDomainsFile = new File(goDomainsPath);

            if (!goDomainsFile.exists()) {
                JOptionPane.showMessageDialog(this, "GO domains file \"" + goDomainsFile.getName() + "\" not found!\n"
                        + "Continuing without GO domains.", "File Not Found", JOptionPane.ERROR_MESSAGE);
            } else {

                progressDialog.setTitle("Getting GO Domains. Please Wait...");

                // read the GO domains
                FileReader r = new FileReader(goDomainsPath);
                BufferedReader br = new BufferedReader(r);

                String line = br.readLine();

                while (line != null) {
                    String[] elements = line.split(",");
                    goDomainMap.put(elements[0], elements[1]);
                    line = br.readLine();
                }

                br.close();
                r.close();
            }

            progressDialog.setTitle("Getting GO Mappings. Please Wait...");

            // read the GO mappings
            FileReader r = new FileReader(goMappingsFile);
            BufferedReader br = new BufferedReader(r);

            // read and ignore the header
            br.readLine();

            String line = br.readLine();

            while (line != null) {

                if (!line.startsWith(",,")) {

                    String[] elements = line.split(",");

                    if (elements.length == 3 && !line.endsWith("\",")) {
                        String goAccession = elements[0];
                        String goTerm = elements[1].toLowerCase();
                        String proteinAccession = elements[2];

                        if (!proteinToGoMappings.containsKey(proteinAccession)) {
                            ArrayList<String> proteinGoMappings = new ArrayList<String>();
                            proteinGoMappings.add(goTerm);
                            proteinToGoMappings.put(proteinAccession, proteinGoMappings);
                        } else {
                            proteinToGoMappings.get(proteinAccession).add(goTerm);
                        }

                        goTermToAccessionMap.put(goTerm, goAccession);

                        if (totalGoTermUsage.containsKey(goTerm)) {
                            totalGoTermUsage.put(goTerm, totalGoTermUsage.get(goTerm) + 1);
                        } else {
                            totalGoTermUsage.put(goTerm, 1);
                        }

                        totalNumberOfProteins++;

                    } else if (line.indexOf("\"") != -1) {

                        if (!line.endsWith("\",")) {

                            String goAccession = line.substring(0, line.indexOf(","));
                            String goTerm = line.substring(line.indexOf(",") + 1, line.lastIndexOf(",")).toLowerCase();

                            if (goTerm.startsWith("\"")) {
                                goTerm = goTerm.substring(1);
                            }
                            if (goTerm.endsWith("\"")) {
                                goTerm = goTerm.substring(0, goTerm.length() - 1);
                            }

                            String proteinAccession = line.substring(line.lastIndexOf(",") + 1);

                            if (!proteinToGoMappings.containsKey(proteinAccession)) {
                                ArrayList<String> proteinGoMappings = new ArrayList<String>();
                                proteinGoMappings.add(goTerm);
                                proteinToGoMappings.put(proteinAccession, proteinGoMappings);
                            } else {
                                proteinToGoMappings.get(proteinAccession).add(goTerm);
                            }

                            goTermToAccessionMap.put(goTerm, goAccession);

                            if (totalGoTermUsage.containsKey(goTerm)) {
                                totalGoTermUsage.put(goTerm, totalGoTermUsage.get(goTerm) + 1);
                            } else {
                                totalGoTermUsage.put(goTerm, 1);
                            }

                            totalNumberOfProteins++;
                        }

                    } else {
                        // ignore, as there are no protein mappings
                        //System.out.println(line);
                    }
                }

                line = br.readLine();
            }


            // get go term for dataset
            Identification identification = peptideShakerGUI.getIdentification();
            ArrayList<String> allProjectProteins = identification.getProteinIdentification();
            PSParameter proteinPSParameter = new PSParameter();
            String mainAccession;

            progressDialog.setTitle("Mapping GO Terms. Please Wait...");
            progressDialog.setIndeterminate(false);
            progressDialog.setValue(0);
            progressDialog.setMax(identification.getProteinIdentification().size());


            for (String matchKey : identification.getProteinIdentification()) {

                progressDialog.incrementValue();

                try {
                    proteinPSParameter = (PSParameter) identification.getMatchParameter(matchKey, proteinPSParameter);

                    if (proteinPSParameter.isValidated() && !ProteinMatch.isDecoy(matchKey)) {
                        if (ProteinMatch.getNProteins(matchKey) > 1) {
                            mainAccession = identification.getProteinMatch(matchKey).getMainMatch();
                        } else {
                            mainAccession = matchKey;
                        }
                        if (proteinToGoMappings.containsKey(mainAccession)) {

                            ArrayList<String> goTerms = proteinToGoMappings.get(mainAccession);

                            for (int j = 0; j < goTerms.size(); j++) {
                                if (datasetGoTermUsage.containsKey(goTerms.get(j))) {
                                    datasetGoTermUsage.put(goTerms.get(j), datasetGoTermUsage.get(goTerms.get(j)) + 1);
                                } else {
                                    datasetGoTermUsage.put(goTerms.get(j), 1);
                                }
                            }
                        } else {
                            // ignore, does not map to any GO terms in the current GO slim
                        }
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }

            progressDialog.setTitle("Creating GO Plots. Please Wait...");
            progressDialog.setValue(0);
            progressDialog.setMax(totalGoTermUsage.entrySet().size());



            // update the table
            Double maxLog2Diff = 0.0;
            ArrayList<Integer> indexes = new ArrayList<Integer>();
            ArrayList<Double> pValues = new ArrayList<Double>();

            for (Map.Entry<String, Integer> entry : totalGoTermUsage.entrySet()) {

                progressDialog.incrementValue();

                String goTerm = entry.getKey();
                Integer frequencyAll = entry.getValue();

                String goAccession = goTermToAccessionMap.get(goTerm);
                Integer frequencyDataset = 0;
                Double percentDataset = 0.0;

                if (datasetGoTermUsage.get(goTerm) != null) {
                    frequencyDataset = datasetGoTermUsage.get(goTerm);
                    percentDataset = ((double) frequencyDataset / allProjectProteins.size()) * 100;
                }

                Double percentAll = ((double) frequencyAll / proteinToGoMappings.size()) * 100;
                Double pValue = new HypergeometricDistributionImpl(proteinToGoMappings.size(), frequencyAll, allProjectProteins.size()).probability(frequencyDataset);
                Double log2Diff = Math.log(percentDataset / percentAll) / Math.log(2);

                if (!log2Diff.isInfinite() && Math.abs(log2Diff) > maxLog2Diff) {
                    maxLog2Diff = Math.abs(log2Diff);
                }

                String goDomain = "-";

                if (goDomainMap.get(goAccession) != null) {
                    goDomain = goDomainMap.get(goAccession);
                }

                // add the data points for the first data series 
                ArrayList<Double> dataAll = new ArrayList<Double>();
                dataAll.add(percentAll);
                ArrayList<Double> dataDataset = new ArrayList<Double>();
                dataDataset.add(percentDataset);

                // create a JSparklineDataSeries  
                JSparklinesDataSeries sparklineDataseriesAll = new JSparklinesDataSeries(dataAll, Color.RED, "All");
                JSparklinesDataSeries sparklineDataseriesDataset = new JSparklinesDataSeries(dataDataset, peptideShakerGUI.getSparklineColor(), "Dataset");

                // add the data series to JSparklineDataset 
                ArrayList<JSparklinesDataSeries> sparkLineDataSeries = new ArrayList<JSparklinesDataSeries>();
                sparkLineDataSeries.add(sparklineDataseriesAll);
                sparkLineDataSeries.add(sparklineDataseriesDataset);

                JSparklinesDataset dataset = new JSparklinesDataset(sparkLineDataSeries);

                pValues.add(pValue);
                indexes.add(goMappingsTable.getRowCount());
                
                ((DefaultTableModel) goMappingsTable.getModel()).addRow(new Object[]{
                            goMappingsTable.getRowCount() + 1,
                            addGoLink(goAccession),
                            goTerm,
                            goDomain,
                            percentAll,
                            percentDataset,
                            dataset,
                            log2Diff,
                            pValue,
                            pValue < (Double) significanceJSpinner.getValue() / totalGoTermUsage.keySet().size(), // bonferroni correction
                            true
                        });
            }
            
            // correct the p-values for multiple testing using benjamini-hochberg
            sortPValues(pValues, indexes);
           
            goMappingsTable.setValueAt(pValues.get(0) < (Double) significanceJSpinner.getValue(), 
                    indexes.get(0), goMappingsTable.getColumn(" ").getModelIndex());
            
            for (int i=1; i<pValues.size(); i++) {
                goMappingsTable.setValueAt(pValues.get(i)*pValues.size()/(pValues.size() - i - 1) < (Double) significanceJSpinner.getValue(), 
                        indexes.get(i), goMappingsTable.getColumn(" ").getModelIndex());
            }
            
            br.close();
            r.close();

            progressDialog.setIndeterminate(true);

            // invoke later to give time for components to update
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    // set the preferred size of the accession column
                    int width = peptideShakerGUI.getPreferredColumnWidth(goMappingsTable, goMappingsTable.getColumn("GO Accession").getModelIndex(), 6);
                    goMappingsTable.getColumn("GO Accession").setMinWidth(width);
                    goMappingsTable.getColumn("GO Accession").setMaxWidth(width);
                }
            });

            maxLog2Diff = Math.ceil(maxLog2Diff);

            goMappingsTable.getColumn("Log2 Diff").setCellRenderer(new JSparklinesBarChartTableCellRenderer(
                    PlotOrientation.HORIZONTAL, -maxLog2Diff, maxLog2Diff, Color.RED, peptideShakerGUI.getSparklineColor(), Color.GRAY, 0));
            ((JSparklinesBarChartTableCellRenderer) goMappingsTable.getColumn("Log2 Diff").getCellRenderer()).showNumberAndChart(true, peptideShakerGUI.getLabelWidth());

            // update the plots
            updateGoPlots();

            // enable the contextual export options
            exportMappingsJButton.setEnabled(true);
            exportPlotsJButton.setEnabled(true);


            if (progressDialog != null) {
                progressDialog.setVisible(false);
                progressDialog.dispose();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();

            if (progressDialog != null) {
                progressDialog.setVisible(false);
                progressDialog.dispose();
            }
        } catch (IOException e) {
            e.printStackTrace();

            if (progressDialog != null) {
                progressDialog.setVisible(false);
                progressDialog.dispose();
            }
        } catch (IndexOutOfBoundsException e) {
            // ignore as this only happens when the gui cannot keep up with the user

            if (progressDialog != null) {
                progressDialog.setVisible(false);
                progressDialog.dispose();
            }
        }
    }

    /**
     * Update the GO plots.
     */
    private void updateGoPlots() {

        DefaultCategoryDataset frquencyPlotDataset = new DefaultCategoryDataset();
        DefaultCategoryDataset significancePlotDataset = new DefaultCategoryDataset();
        ArrayList<Color> significanceColors = new ArrayList<Color>();
        Double maxLog2Diff = 0.0;

        for (int i = 0; i < goMappingsTable.getRowCount(); i++) {

            boolean selected = (Boolean) goMappingsTable.getValueAt(i, goMappingsTable.getColumn("  ").getModelIndex());
            boolean significant = (Boolean) goMappingsTable.getValueAt(i, goMappingsTable.getColumn(" ").getModelIndex());

            if (selected) {

                String goTerm = (String) goMappingsTable.getValueAt(i, goMappingsTable.getColumn("GO Term").getModelIndex());
                Double percentAll = (Double) goMappingsTable.getValueAt(i, goMappingsTable.getColumn("Frequency All (%)").getModelIndex());
                Double percentDataset = (Double) goMappingsTable.getValueAt(i, goMappingsTable.getColumn("Frequency Dataset (%)").getModelIndex());
                Double log2Diff = (Double) goMappingsTable.getValueAt(i, goMappingsTable.getColumn("Log2 Diff").getModelIndex());

                frquencyPlotDataset.addValue(percentAll, "All", goTerm);
                frquencyPlotDataset.addValue(percentDataset, "Dataset", goTerm);

                if (!log2Diff.isInfinite()) {
                    significancePlotDataset.addValue(log2Diff, "Difference", goTerm);
                } else {
                    significancePlotDataset.addValue(0, "Difference", goTerm);
                }

                if (significant) {
                    if (log2Diff > 0) {
                        significanceColors.add(peptideShakerGUI.getSparklineColor());
                    } else {
                        significanceColors.add(new Color(255, 51, 51));
                    }
                } else {
                    significanceColors.add(Color.lightGray);
                }

                if (!log2Diff.isInfinite() && Math.abs(log2Diff) > maxLog2Diff) {
                    maxLog2Diff = Math.abs(log2Diff);
                }
            }
        }

        maxLog2Diff = Math.ceil(maxLog2Diff);


        JFreeChart distributionChart = ChartFactory.createBarChart(null, "GO Terms", "Frequency (%)", frquencyPlotDataset, PlotOrientation.VERTICAL, false, true, true);
        distributionChartPanel = new ChartPanel(distributionChart);

        ((CategoryPlot) distributionChartPanel.getChart().getPlot()).getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_90);

        BarRenderer3D renderer = new BarRenderer3D(0, 0);
        renderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
        renderer.setSeriesPaint(0, Color.RED);
        renderer.setSeriesPaint(1, peptideShakerGUI.getSparklineColor());
        distributionChart.getCategoryPlot().setRenderer(renderer);
        
        // add mouse listener
        distributionChartPanel.addChartMouseListener(new ChartMouseListener() {

            @Override
            public void chartMouseClicked(ChartMouseEvent cme) {
                
                if (cme.getEntity() instanceof CategoryItemEntity) {
                    CategoryItemEntity categoryItem = (CategoryItemEntity) cme.getEntity();
                    String columnKey = (String) categoryItem.getColumnKey();

                    // select and highlight category
                    boolean categoryFound = false;
                    
                    for (int i=0; i<goMappingsTable.getRowCount() && !categoryFound; i++) {
                        if (((String) goMappingsTable.getValueAt(
                                i, goMappingsTable.getColumn("GO Term").getModelIndex())).equalsIgnoreCase(columnKey)) {
                            goMappingsTable.setRowSelectionInterval(i, i);
                            goMappingsTable.scrollRectToVisible(goMappingsTable.getCellRect(i, 0, false));
                            goMappingsTableMouseReleased(null);
                        }
                    }
                }
            }

            @Override
            public void chartMouseMoved(ChartMouseEvent cme) {
                // do nothing
            }
        });

        // set background color
        distributionChart.getPlot().setBackgroundPaint(Color.WHITE);
        distributionChart.setBackgroundPaint(Color.WHITE);
        distributionChartPanel.setBackground(Color.WHITE);

        // hide the outline
        distributionChart.getPlot().setOutlineVisible(false);

        goFrequencyPlotPanel.removeAll();
        goFrequencyPlotPanel.add(distributionChartPanel);
        goFrequencyPlotPanel.revalidate();
        goFrequencyPlotPanel.repaint();



        JFreeChart significanceChart = ChartFactory.createBarChart(null, "GO Terms", "Log2 Difference", significancePlotDataset, PlotOrientation.VERTICAL, false, true, true);
        signChartPanel = new ChartPanel(significanceChart);

        ((CategoryPlot) signChartPanel.getChart().getPlot()).getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_90);

        ((CategoryPlot) signChartPanel.getChart().getPlot()).getRangeAxis().setUpperBound(maxLog2Diff);
        ((CategoryPlot) signChartPanel.getChart().getPlot()).getRangeAxis().setLowerBound(-maxLog2Diff);

        BarChartColorRenderer signRenderer = new BarChartColorRenderer(significanceColors);
        signRenderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
        significanceChart.getCategoryPlot().setRenderer(signRenderer);

        
        // add mouse listener
        signChartPanel.addChartMouseListener(new ChartMouseListener() {

            @Override
            public void chartMouseClicked(ChartMouseEvent cme) {
                
                if (cme.getEntity() instanceof CategoryItemEntity) {
                    CategoryItemEntity categoryItem = (CategoryItemEntity) cme.getEntity();
                    String columnKey = (String) categoryItem.getColumnKey();

                    // select and highlight category
                    boolean categoryFound = false;
                    
                    for (int i=0; i<goMappingsTable.getRowCount() && !categoryFound; i++) {
                        if (((String) goMappingsTable.getValueAt(
                                i, goMappingsTable.getColumn("GO Term").getModelIndex())).equalsIgnoreCase(columnKey)) {
                            goMappingsTable.setRowSelectionInterval(i, i);
                            goMappingsTable.scrollRectToVisible(goMappingsTable.getCellRect(i, 0, false));
                            goMappingsTableMouseReleased(null);
                        }
                    }
                }
            }

            @Override
            public void chartMouseMoved(ChartMouseEvent cme) {
                // do nothing
            }
        });

        // set background color
        significanceChart.getPlot().setBackgroundPaint(Color.WHITE);
        significanceChart.setBackgroundPaint(Color.WHITE);
        signChartPanel.setBackground(Color.WHITE);

        // hide the outline
        significanceChart.getPlot().setOutlineVisible(false);

        goSignificancePlotPanel.removeAll();
        goSignificancePlotPanel.add(signChartPanel);
        goSignificancePlotPanel.revalidate();
        goSignificancePlotPanel.repaint();

        updatePlotMarkers();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        selectTermsJPopupMenu = new javax.swing.JPopupMenu();
        selectAllMenuItem = new javax.swing.JMenuItem();
        deselectAllMenuItem = new javax.swing.JMenuItem();
        selectSignificantMenuItem = new javax.swing.JMenuItem();
        mappingsTableLayeredPane = new javax.swing.JLayeredPane();
        mappingsPanel = new javax.swing.JPanel();
        proteinGoMappingsScrollPane = new javax.swing.JScrollPane();
        goMappingsTable = new JTable() {
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
                    public String getToolTipText(MouseEvent e) {
                        String tip = null;
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex = columnModel.getColumn(index).getModelIndex();
                        tip = (String) mappingsTableToolTips.get(realIndex);
                        return tip;
                    }
                };
            }
        };
        goMappingsFileJLabel = new javax.swing.JLabel();
        goMappingFilesJComboBox = new javax.swing.JComboBox();
        significanceJLabel = new javax.swing.JLabel();
        significanceJSpinner = new javax.swing.JSpinner();
        mappingsHelpJButton = new javax.swing.JButton();
        exportMappingsJButton = new javax.swing.JButton();
        contextMenuMappingsBackgroundPanel = new javax.swing.JPanel();
        plotLayeredPane = new javax.swing.JLayeredPane();
        plotPanel = new javax.swing.JPanel();
        goPlotsTabbedPane = new javax.swing.JTabbedPane();
        goFrequencyPlotPanel = new javax.swing.JPanel();
        goSignificancePlotPanel = new javax.swing.JPanel();
        plotHelpJButton = new javax.swing.JButton();
        exportPlotsJButton = new javax.swing.JButton();
        contextMenuPlotsBackgroundPanel = new javax.swing.JPanel();

        selectAllMenuItem.setText("Select All");
        selectAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllMenuItemActionPerformed(evt);
            }
        });
        selectTermsJPopupMenu.add(selectAllMenuItem);

        deselectAllMenuItem.setText("Deselect All");
        deselectAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deselectAllMenuItemActionPerformed(evt);
            }
        });
        selectTermsJPopupMenu.add(deselectAllMenuItem);

        selectSignificantMenuItem.setText("Select Significant");
        selectSignificantMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectSignificantMenuItemActionPerformed(evt);
            }
        });
        selectTermsJPopupMenu.add(selectSignificantMenuItem);

        setBackground(new java.awt.Color(255, 255, 255));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        mappingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Gene Ontology Mappings"));
        mappingsPanel.setOpaque(false);

        goMappingsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "", "GO Accession", "GO Term", "GO Domain", "Frequency All (%)", "Frequency Dataset (%)", "Frequency (%)", "Log2 Diff", "p-value", " ", "  "
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Double.class, java.lang.Double.class, java.lang.Object.class, java.lang.Double.class, java.lang.Double.class, java.lang.Boolean.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        goMappingsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        goMappingsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                goMappingsTableMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                goMappingsTableMouseReleased(evt);
            }
        });
        goMappingsTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                goMappingsTableMouseMoved(evt);
            }
        });
        goMappingsTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                goMappingsTableKeyReleased(evt);
            }
        });
        proteinGoMappingsScrollPane.setViewportView(goMappingsTable);

        goMappingsFileJLabel.setText("GO Mappings File:");

        goMappingFilesJComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        significanceJLabel.setText("Significance Level:");

        significanceJSpinner.setModel(new javax.swing.SpinnerNumberModel(0.05d, 0.0d, 1.0d, 0.01d));
        significanceJSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                significanceJSpinnerStateChanged(evt);
            }
        });

        javax.swing.GroupLayout mappingsPanelLayout = new javax.swing.GroupLayout(mappingsPanel);
        mappingsPanel.setLayout(mappingsPanelLayout);
        mappingsPanelLayout.setHorizontalGroup(
            mappingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mappingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mappingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(proteinGoMappingsScrollPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 987, Short.MAX_VALUE)
                    .addGroup(mappingsPanelLayout.createSequentialGroup()
                        .addComponent(goMappingsFileJLabel)
                        .addGap(18, 18, 18)
                        .addComponent(goMappingFilesJComboBox, 0, 690, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(significanceJLabel)
                        .addGap(18, 18, 18)
                        .addComponent(significanceJSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        mappingsPanelLayout.setVerticalGroup(
            mappingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mappingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mappingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(goMappingsFileJLabel)
                    .addComponent(goMappingFilesJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(significanceJLabel)
                    .addComponent(significanceJSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(proteinGoMappingsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 281, Short.MAX_VALUE)
                .addContainerGap())
        );

        mappingsPanel.setBounds(0, 0, 1019, 368);
        mappingsTableLayeredPane.add(mappingsPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);

        mappingsHelpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame_grey.png"))); // NOI18N
        mappingsHelpJButton.setToolTipText("Help");
        mappingsHelpJButton.setBorder(null);
        mappingsHelpJButton.setBorderPainted(false);
        mappingsHelpJButton.setContentAreaFilled(false);
        mappingsHelpJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame.png"))); // NOI18N
        mappingsHelpJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                mappingsHelpJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                mappingsHelpJButtonMouseExited(evt);
            }
        });
        mappingsHelpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mappingsHelpJButtonActionPerformed(evt);
            }
        });
        mappingsHelpJButton.setBounds(990, 0, 10, 25);
        mappingsTableLayeredPane.add(mappingsHelpJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        exportMappingsJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/export_no_frame_grey.png"))); // NOI18N
        exportMappingsJButton.setToolTipText("Copy to Clipboard");
        exportMappingsJButton.setBorder(null);
        exportMappingsJButton.setBorderPainted(false);
        exportMappingsJButton.setContentAreaFilled(false);
        exportMappingsJButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/export_no_frame_grey.png"))); // NOI18N
        exportMappingsJButton.setEnabled(false);
        exportMappingsJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/export_no_frame.png"))); // NOI18N
        exportMappingsJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                exportMappingsJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                exportMappingsJButtonMouseExited(evt);
            }
        });
        exportMappingsJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportMappingsJButtonActionPerformed(evt);
            }
        });
        exportMappingsJButton.setBounds(980, 0, 10, 25);
        mappingsTableLayeredPane.add(exportMappingsJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        contextMenuMappingsBackgroundPanel.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout contextMenuMappingsBackgroundPanelLayout = new javax.swing.GroupLayout(contextMenuMappingsBackgroundPanel);
        contextMenuMappingsBackgroundPanel.setLayout(contextMenuMappingsBackgroundPanelLayout);
        contextMenuMappingsBackgroundPanelLayout.setHorizontalGroup(
            contextMenuMappingsBackgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 30, Short.MAX_VALUE)
        );
        contextMenuMappingsBackgroundPanelLayout.setVerticalGroup(
            contextMenuMappingsBackgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );

        contextMenuMappingsBackgroundPanel.setBounds(980, 0, 30, 20);
        mappingsTableLayeredPane.add(contextMenuMappingsBackgroundPanel, javax.swing.JLayeredPane.POPUP_LAYER);

        plotPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Gene Ontology - Enrichment Analysis"));
        plotPanel.setOpaque(false);

        goPlotsTabbedPane.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);

        goFrequencyPlotPanel.setOpaque(false);
        goFrequencyPlotPanel.setLayout(new javax.swing.BoxLayout(goFrequencyPlotPanel, javax.swing.BoxLayout.LINE_AXIS));
        goPlotsTabbedPane.addTab("Distribution", goFrequencyPlotPanel);

        goSignificancePlotPanel.setOpaque(false);
        goSignificancePlotPanel.setLayout(new javax.swing.BoxLayout(goSignificancePlotPanel, javax.swing.BoxLayout.LINE_AXIS));
        goPlotsTabbedPane.addTab("Significance", goSignificancePlotPanel);

        goPlotsTabbedPane.setSelectedIndex(1);

        javax.swing.GroupLayout plotPanelLayout = new javax.swing.GroupLayout(plotPanel);
        plotPanel.setLayout(plotPanelLayout);
        plotPanelLayout.setHorizontalGroup(
            plotPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(plotPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(goPlotsTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 987, Short.MAX_VALUE)
                .addContainerGap())
        );
        plotPanelLayout.setVerticalGroup(
            plotPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, plotPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(goPlotsTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE)
                .addContainerGap())
        );

        plotPanel.setBounds(0, 0, 1019, 370);
        plotLayeredPane.add(plotPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);

        plotHelpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame_grey.png"))); // NOI18N
        plotHelpJButton.setToolTipText("Help");
        plotHelpJButton.setBorder(null);
        plotHelpJButton.setBorderPainted(false);
        plotHelpJButton.setContentAreaFilled(false);
        plotHelpJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame.png"))); // NOI18N
        plotHelpJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                plotHelpJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                plotHelpJButtonMouseExited(evt);
            }
        });
        plotHelpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plotHelpJButtonActionPerformed(evt);
            }
        });
        plotHelpJButton.setBounds(990, 0, 10, 25);
        plotLayeredPane.add(plotHelpJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        exportPlotsJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/export_no_frame_grey.png"))); // NOI18N
        exportPlotsJButton.setToolTipText("Copy to Clipboard");
        exportPlotsJButton.setBorder(null);
        exportPlotsJButton.setBorderPainted(false);
        exportPlotsJButton.setContentAreaFilled(false);
        exportPlotsJButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/export_no_frame_grey.png"))); // NOI18N
        exportPlotsJButton.setEnabled(false);
        exportPlotsJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/export_no_frame.png"))); // NOI18N
        exportPlotsJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                exportPlotsJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                exportPlotsJButtonMouseExited(evt);
            }
        });
        exportPlotsJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportPlotsJButtonActionPerformed(evt);
            }
        });
        exportPlotsJButton.setBounds(980, 0, 10, 25);
        plotLayeredPane.add(exportPlotsJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        contextMenuPlotsBackgroundPanel.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout contextMenuPlotsBackgroundPanelLayout = new javax.swing.GroupLayout(contextMenuPlotsBackgroundPanel);
        contextMenuPlotsBackgroundPanel.setLayout(contextMenuPlotsBackgroundPanelLayout);
        contextMenuPlotsBackgroundPanelLayout.setHorizontalGroup(
            contextMenuPlotsBackgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 30, Short.MAX_VALUE)
        );
        contextMenuPlotsBackgroundPanelLayout.setVerticalGroup(
            contextMenuPlotsBackgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );

        contextMenuPlotsBackgroundPanel.setBounds(980, 0, 30, 20);
        plotLayeredPane.add(contextMenuPlotsBackgroundPanel, javax.swing.JLayeredPane.POPUP_LAYER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(plotLayeredPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 1019, Short.MAX_VALUE)
                    .addComponent(mappingsTableLayeredPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 1019, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mappingsTableLayeredPane, javax.swing.GroupLayout.PREFERRED_SIZE, 365, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(plotLayeredPane, javax.swing.GroupLayout.DEFAULT_SIZE, 372, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Changes the cursor back to the default cursor a hand.
     *
     * @param evt
     */
    private void goMappingsTableMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_goMappingsTableMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_goMappingsTableMouseExited

    /**
     * Changes the cursor into a hand cursor if the table cell contains an
     * html link.
     *
     * @param evt
     */
    private void goMappingsTableMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_goMappingsTableMouseMoved
        int row = goMappingsTable.rowAtPoint(evt.getPoint());
        int column = goMappingsTable.columnAtPoint(evt.getPoint());

        if (column == goMappingsTable.getColumn("GO Accession").getModelIndex() && goMappingsTable.getValueAt(row, column) != null) {

            String tempValue = (String) goMappingsTable.getValueAt(row, column);

            if (tempValue.lastIndexOf("<html>") != -1) {
                this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            } else {
                this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            }
        } else {
            this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        }
    }//GEN-LAST:event_goMappingsTableMouseMoved

    /**
     * If the user clicks the go term column the go term is opened in the web browser.
     *
     * @param evt
     */
    private void goMappingsTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_goMappingsTableMouseReleased

        int row = goMappingsTable.getSelectedRow();
        int column = goMappingsTable.getSelectedColumn();

        if (row != -1) {
            this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

            if (evt == null || evt.getButton() == MouseEvent.BUTTON1) {

                // open protein link in web browser
                if (column == goMappingsTable.getColumn("GO Accession").getModelIndex() && evt != null && evt.getButton() == MouseEvent.BUTTON1
                        && ((String) goMappingsTable.getValueAt(row, column)).lastIndexOf("<html>") != -1) {

                    String link = (String) goMappingsTable.getValueAt(row, column);
                    link = link.substring(link.indexOf("\"") + 1);
                    link = link.substring(0, link.indexOf("\""));

                    this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
                    BareBonesBrowserLaunch.openURL(link);
                    this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                } else if (column == goMappingsTable.getColumn("  ").getModelIndex()) {
                    updateGoPlots();
                }

                updatePlotMarkers();
            }

            this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        }

        if (evt != null && goMappingsTable.getRowCount() > 0 && evt.getButton() == MouseEvent.BUTTON3) {
            selectTermsJPopupMenu.show(goMappingsTable, evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_goMappingsTableMouseReleased

    /**
     * Update the analysis with the new significance threshold.
     * 
     * @param evt 
     */
    private void significanceJSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_significanceJSpinnerStateChanged
        if (peptideShakerGUI.getIdentification() != null) {

            // invoke later to give time for components to update
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {

                    progressDialog = new ProgressDialogX(peptideShakerGUI, peptideShakerGUI, true);

                    new Thread(new Runnable() {

                        public void run() {
                            progressDialog.setIndeterminate(true);
                            progressDialog.setTitle("GO Analysis. Please Wait...");
                            progressDialog.setVisible(true);
                        }
                    }, "ProgressDialog").start();

                    displayResults(progressDialog, true);
                }
            });
        }
    }//GEN-LAST:event_significanceJSpinnerStateChanged

    /**
     * Select all the mappings and update the plot.
     * 
     * @param evt 
     */
    private void selectAllMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllMenuItemActionPerformed
        for (int i = 0; i < goMappingsTable.getRowCount(); i++) {
            ((DefaultTableModel) goMappingsTable.getModel()).setValueAt(true, i, goMappingsTable.getColumn("  ").getModelIndex());
        }

        updateGoPlots();
    }//GEN-LAST:event_selectAllMenuItemActionPerformed

    /**
     * Deselect all the mappings and update the plot.
     * 
     * @param evt 
     */
    private void deselectAllMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deselectAllMenuItemActionPerformed
        for (int i = 0; i < goMappingsTable.getRowCount(); i++) {
            ((DefaultTableModel) goMappingsTable.getModel()).setValueAt(false, i, goMappingsTable.getColumn("  ").getModelIndex());
        }

        updateGoPlots();
    }//GEN-LAST:event_deselectAllMenuItemActionPerformed

    /**
     * Select all the significant mappings and update the plot.
     * 
     * @param evt 
     */
    private void selectSignificantMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectSignificantMenuItemActionPerformed
        for (int i = 0; i < goMappingsTable.getRowCount(); i++) {
            if ((Boolean) goMappingsTable.getModel().getValueAt(i, goMappingsTable.getColumn(" ").getModelIndex())) {
                ((DefaultTableModel) goMappingsTable.getModel()).setValueAt(true, i, goMappingsTable.getColumn("  ").getModelIndex());
            } else {
                ((DefaultTableModel) goMappingsTable.getModel()).setValueAt(false, i, goMappingsTable.getColumn("  ").getModelIndex());
            }
        }

        updateGoPlots();
    }//GEN-LAST:event_selectSignificantMenuItemActionPerformed

    /**
     * Update the plot markers.
     * 
     * @param evt 
     */
    private void goMappingsTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_goMappingsTableKeyReleased
        updatePlotMarkers();
    }//GEN-LAST:event_goMappingsTableKeyReleased

    /**
     * Resize the layered panes.
     * 
     * @param evt 
     */
    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized

        // resize the layered panels
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {

                // move the icons
                mappingsTableLayeredPane.getComponent(0).setBounds(
                        mappingsTableLayeredPane.getWidth() - mappingsTableLayeredPane.getComponent(0).getWidth() - 10,
                        -3,
                        mappingsTableLayeredPane.getComponent(0).getWidth(),
                        mappingsTableLayeredPane.getComponent(0).getHeight());

                mappingsTableLayeredPane.getComponent(1).setBounds(
                        mappingsTableLayeredPane.getWidth() - mappingsTableLayeredPane.getComponent(1).getWidth() - 20,
                        -3,
                        mappingsTableLayeredPane.getComponent(1).getWidth(),
                        mappingsTableLayeredPane.getComponent(1).getHeight());

                mappingsTableLayeredPane.getComponent(2).setBounds(
                        mappingsTableLayeredPane.getWidth() - mappingsTableLayeredPane.getComponent(2).getWidth() - 5,
                        -3,
                        mappingsTableLayeredPane.getComponent(2).getWidth(),
                        mappingsTableLayeredPane.getComponent(2).getHeight());

                // resize the plot area
                mappingsTableLayeredPane.getComponent(3).setBounds(0, 0, mappingsTableLayeredPane.getWidth(), mappingsTableLayeredPane.getHeight());
                mappingsTableLayeredPane.revalidate();
                mappingsTableLayeredPane.repaint();


                // move the icons
                plotLayeredPane.getComponent(0).setBounds(
                        plotLayeredPane.getWidth() - plotLayeredPane.getComponent(0).getWidth() - 10,
                        -3,
                        plotLayeredPane.getComponent(0).getWidth(),
                        plotLayeredPane.getComponent(0).getHeight());

                plotLayeredPane.getComponent(1).setBounds(
                        plotLayeredPane.getWidth() - plotLayeredPane.getComponent(1).getWidth() - 20,
                        -3,
                        plotLayeredPane.getComponent(1).getWidth(),
                        plotLayeredPane.getComponent(1).getHeight());

                plotLayeredPane.getComponent(2).setBounds(
                        plotLayeredPane.getWidth() - plotLayeredPane.getComponent(2).getWidth() - 5,
                        -3,
                        plotLayeredPane.getComponent(2).getWidth(),
                        plotLayeredPane.getComponent(2).getHeight());

                // resize the plot area
                plotLayeredPane.getComponent(3).setBounds(0, 0, plotLayeredPane.getWidth(), plotLayeredPane.getHeight());
                plotLayeredPane.revalidate();
                plotLayeredPane.repaint();
            }
        });
    }//GEN-LAST:event_formComponentResized

    /**
     * Change the cursor to a hand cursor.
     * 
     * @param evt 
     */
    private void mappingsHelpJButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mappingsHelpJButtonMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_mappingsHelpJButtonMouseEntered

    /**
     * Change the cursor back to the default cursor.
     * 
     * @param evt 
     */
    private void mappingsHelpJButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mappingsHelpJButtonMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_mappingsHelpJButtonMouseExited

    /**
     * Open the help dialog.
     * 
     * @param evt 
     */
    private void mappingsHelpJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mappingsHelpJButtonActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new HelpDialog(peptideShakerGUI, getClass().getResource("/helpFiles/GOEA.html"), "GO_Mappings");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_mappingsHelpJButtonActionPerformed

    /**
     * Change the cursor to a hand cursor.
     * 
     * @param evt 
     */
    private void exportMappingsJButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_exportMappingsJButtonMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_exportMappingsJButtonMouseEntered

    /**
     * Change the cursor back to the default cursor.
     * 
     * @param evt 
     */
    private void exportMappingsJButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_exportMappingsJButtonMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_exportMappingsJButtonMouseExited

    /**
     * Export the table contents.
     * 
     * @param evt 
     */
    private void exportMappingsJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportMappingsJButtonActionPerformed

        progressDialog = new ProgressDialogX(peptideShakerGUI, peptideShakerGUI, true);
        progressDialog.doNothingOnClose();

        new Thread(new Runnable() {

            public void run() {
                progressDialog.setIndeterminate(true);
                progressDialog.setTitle("Copying to Clipboard. Please Wait...");
                progressDialog.setVisible(true);
            }
        }, "ProgressDialog").start();

        new Thread("ExportThread") {

            @Override
            public void run() {
                try {
                    String clipboardString = "";

                    clipboardString = Util.tableToText(goMappingsTable, "\t", progressDialog, true);

                    StringSelection stringSelection = new StringSelection(clipboardString);
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(stringSelection, peptideShakerGUI);

                    progressDialog.setVisible(false);
                    progressDialog.dispose();
                    JOptionPane.showMessageDialog(peptideShakerGUI, "Table content copied to clipboard.", "Copied to Clipboard", JOptionPane.INFORMATION_MESSAGE);

                } catch (Exception e) {
                    progressDialog.setVisible(false);
                    progressDialog.dispose();
                    JOptionPane.showMessageDialog(peptideShakerGUI, "An error occurred while generating the output.", "Output Error.", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        }.start();
    }//GEN-LAST:event_exportMappingsJButtonActionPerformed

    /**
     * Change the cursor to a hand cursor.
     * 
     * @param evt 
     */
    private void plotHelpJButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_plotHelpJButtonMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_plotHelpJButtonMouseEntered

    /**
     * Change the cursor back to the default cursor.
     * 
     * @param evt 
     */
    private void plotHelpJButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_plotHelpJButtonMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_plotHelpJButtonMouseExited

    /**
     * Open the help dialog.
     * 
     * @param evt 
     */
    private void plotHelpJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plotHelpJButtonActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new HelpDialog(peptideShakerGUI, getClass().getResource("/helpFiles/GOEA.html"), "GO_Plots");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_plotHelpJButtonActionPerformed

    /**
     * Change the cursor to a hand cursor.
     * 
     * @param evt 
     */
    private void exportPlotsJButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_exportPlotsJButtonMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_exportPlotsJButtonMouseEntered

    /**
     * Change the cursor back to the default cursor.
     * 
     * @param evt 
     */
    private void exportPlotsJButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_exportPlotsJButtonMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_exportPlotsJButtonMouseExited

    /**
     * Export the table contents.
     * 
     * @param evt 
     */
    private void exportPlotsJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportPlotsJButtonActionPerformed
        
        int index = goPlotsTabbedPane.getSelectedIndex();
        
        if (index == 0) {
            new ExportGraphicsDialog(peptideShakerGUI, true, (ChartPanel) goFrequencyPlotPanel.getComponent(0));
        } else {
            new ExportGraphicsDialog(peptideShakerGUI, true, (ChartPanel) goSignificancePlotPanel.getComponent(0));
        }
        
    }//GEN-LAST:event_exportPlotsJButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel contextMenuMappingsBackgroundPanel;
    private javax.swing.JPanel contextMenuPlotsBackgroundPanel;
    private javax.swing.JMenuItem deselectAllMenuItem;
    private javax.swing.JButton exportMappingsJButton;
    private javax.swing.JButton exportPlotsJButton;
    private javax.swing.JPanel goFrequencyPlotPanel;
    private javax.swing.JComboBox goMappingFilesJComboBox;
    private javax.swing.JLabel goMappingsFileJLabel;
    private javax.swing.JTable goMappingsTable;
    private javax.swing.JTabbedPane goPlotsTabbedPane;
    private javax.swing.JPanel goSignificancePlotPanel;
    private javax.swing.JButton mappingsHelpJButton;
    private javax.swing.JPanel mappingsPanel;
    private javax.swing.JLayeredPane mappingsTableLayeredPane;
    private javax.swing.JButton plotHelpJButton;
    private javax.swing.JLayeredPane plotLayeredPane;
    private javax.swing.JPanel plotPanel;
    private javax.swing.JScrollPane proteinGoMappingsScrollPane;
    private javax.swing.JMenuItem selectAllMenuItem;
    private javax.swing.JMenuItem selectSignificantMenuItem;
    private javax.swing.JPopupMenu selectTermsJPopupMenu;
    private javax.swing.JLabel significanceJLabel;
    private javax.swing.JSpinner significanceJSpinner;
    // End of variables declaration//GEN-END:variables

    /**
     * Returns the GO accession number as a web link to the given 
     * GO term at QuickGO.
     * 
     * @param goAccession
     * @return 
     */
    private String addGoLink(String goAccession) {
        return "<html><a href=\"" + getGoAccessionLink(goAccession)
                + "\"><font color=\"" + peptideShakerGUI.getNotSelectedRowHtmlTagFontColor() + "\">"
                + goAccession + "</font></a></html>";
    }

    /**
     * Returns the GO accession number as a web link to the given 
     * GO term at QuickGO.
     * 
     * @param goAccession  the GO accession number
     * @return             the GO accession web link
     */
    private String getGoAccessionLink(String goAccession) {
        return "http://www.ebi.ac.uk/QuickGO/GTerm?id=" + goAccession;
    }

    /**
     * Update the plot markers.
     */
    private void updatePlotMarkers() {

        if (signChartPanel != null && goMappingsTable.getSelectedRow() != -1) {

            removePlotMarkers();

            signChartPanel.getChart().getCategoryPlot().addDomainMarker(
                    new CategoryMarker((String) goMappingsTable.getValueAt(goMappingsTable.getSelectedRow(), goMappingsTable.getColumn("GO Term").getModelIndex()),
                    Color.LIGHT_GRAY, new BasicStroke(1.0f), Color.LIGHT_GRAY, new BasicStroke(1.0f), 0.2f), Layer.BACKGROUND);

            distributionChartPanel.getChart().getCategoryPlot().addDomainMarker(
                    new CategoryMarker((String) goMappingsTable.getValueAt(goMappingsTable.getSelectedRow(), goMappingsTable.getColumn("GO Term").getModelIndex()),
                    Color.LIGHT_GRAY, new BasicStroke(1.0f), Color.LIGHT_GRAY, new BasicStroke(1.0f), 0.2f), Layer.BACKGROUND);
        }
    }

    /**
     * Removes the plot markers.
     */
    private void removePlotMarkers() {
        if (signChartPanel != null && signChartPanel.getChart().getCategoryPlot().getDomainMarkers(Layer.BACKGROUND) != null) {

            Iterator iterator = signChartPanel.getChart().getCategoryPlot().getDomainMarkers(Layer.BACKGROUND).iterator();

            // store the keys in a list first to escape a ConcurrentModificationException
            ArrayList<CategoryMarker> tempMarkers = new ArrayList<CategoryMarker>();

            while (iterator.hasNext()) {
                tempMarkers.add((CategoryMarker) iterator.next());
            }

            for (int i = 0; i < tempMarkers.size(); i++) {
                signChartPanel.getChart().getCategoryPlot().removeDomainMarker(i, tempMarkers.get(i), Layer.BACKGROUND);
            }

            iterator = distributionChartPanel.getChart().getCategoryPlot().getDomainMarkers(Layer.BACKGROUND).iterator();

            // store the keys in a list first to escape a ConcurrentModificationException
            tempMarkers = new ArrayList<CategoryMarker>();

            while (iterator.hasNext()) {
                tempMarkers.add((CategoryMarker) iterator.next());
            }

            for (int i = 0; i < tempMarkers.size(); i++) {
                distributionChartPanel.getChart().getCategoryPlot().removeDomainMarker(i, tempMarkers.get(i), Layer.BACKGROUND);
            }

            goPlotsTabbedPane.repaint();
        }
    }
    
    /**
     * Sort the p-values and make the same changes to the table indexes.
     * 
     * @param pValues
     * @param tableIndexes 
     */
    private void sortPValues (ArrayList<Double> pValues, ArrayList<Integer> tableIndexes) {
        
        // iterate p-value list
        for(int i=0; i<pValues.size(); i++) {

            double maxValue = Double.MIN_VALUE;
            int maxIndex = -1;
            
            // find max p-value in sublist
            for (int j=i; j<pValues.size(); j++) {
                if (pValues.get(j) > maxValue) {
                    maxValue = pValues.get(j);
                    maxIndex = j;
                }
            }
            
            // move in both lists
            double oldValue = pValues.get(i);
            pValues.set(i, pValues.get(maxIndex));
            pValues.set(maxIndex, oldValue);
            
            int oldIntValue = tableIndexes.get(i);
            tableIndexes.set(i, tableIndexes.get(maxIndex));
            tableIndexes.set(maxIndex, oldIntValue);
        }
    }
}
