package eu.isas.peptideshaker.gui.preferencesdialogs;

import com.compomics.util.gui.renderers.AlignedListCellRenderer;
import eu.isas.peptideshaker.preferences.ProcessingPreferences;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

/**
 * A simple dialog where the user can view/edit the processing preferences.
 * 
 * @author Marc Vaudel
 */
public class ProcessingPreferencesDialog extends javax.swing.JDialog {

    /**
     * The processing preferences.
     */
    private ProcessingPreferences processingPreferences;

    /**
     * Creates a new parameters dialog.
     *
     * @param parent the parent frame
     * @param editable a boolean indicating whether the processing parameters
     * are editable
     * @param processingPreferences the processing preferences
     */
    public ProcessingPreferencesDialog(java.awt.Frame parent, boolean editable, ProcessingPreferences processingPreferences) {
        super(parent, true);
        initComponents();

        proteinFdrTxt.setText(processingPreferences.getProteinFDR() + "");
        peptideFdrTxt.setText(processingPreferences.getPeptideFDR() + "");
        psmFdrTxt.setText(processingPreferences.getPsmFDR() + "");

        if (processingPreferences.isAScoreCalculated()) {
            ascoreCmb.setSelectedIndex(0);
        } else {
            ascoreCmb.setSelectedIndex(1);
        }

        proteinFdrTxt.setEditable(editable);
        peptideFdrTxt.setEditable(editable);
        psmFdrTxt.setEditable(editable);
        proteinFdrTxt.setEnabled(editable);
        peptideFdrTxt.setEnabled(editable);
        psmFdrTxt.setEnabled(editable);
        ascoreCmb.setEnabled(editable);

        ascoreCmb.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));

        this.processingPreferences = processingPreferences;

        setLocationRelativeTo(parent);
        setVisible(true);
    }

    /**
     * Indicates whether the input is correct.
     *
     * @return a boolean indicating whether the input is correct
     */
    private boolean validateInput() {
        try {
            new Double(proteinFdrTxt.getText().trim());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Please verify the input for the protein FDR.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            new Double(peptideFdrTxt.getText().trim());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Please verify the input for the peptide FDR.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            new Double(psmFdrTxt.getText().trim());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Please verify the input for the PSM FDR.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        backgroundPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        processingParamsPanel = new javax.swing.JPanel();
        proteinFdrLabel = new javax.swing.JLabel();
        peptideFdrLabel = new javax.swing.JLabel();
        psmFdrLabel = new javax.swing.JLabel();
        estimateAScoreLabel = new javax.swing.JLabel();
        ascoreCmb = new javax.swing.JComboBox();
        psmFdrTxt = new javax.swing.JTextField();
        percentLabel = new javax.swing.JLabel();
        peptideFdrTxt = new javax.swing.JTextField();
        percentLabel2 = new javax.swing.JLabel();
        proteinFdrTxt = new javax.swing.JTextField();
        percentLabel3 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Processing");
        setResizable(false);

        backgroundPanel.setBackground(new java.awt.Color(230, 230, 230));

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        processingParamsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Processing Parameters"));
        processingParamsPanel.setOpaque(false);

        proteinFdrLabel.setText("Protein FDR:");

        peptideFdrLabel.setText("Peptide FDR:");

        psmFdrLabel.setText("PSM FDR:");

        estimateAScoreLabel.setText("Estimate A-score:");

        ascoreCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        psmFdrTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        psmFdrTxt.setText("1");

        percentLabel.setText("%");

        peptideFdrTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        peptideFdrTxt.setText("1");

        percentLabel2.setText("%");

        proteinFdrTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        proteinFdrTxt.setText("1");

        percentLabel3.setText("%");

        javax.swing.GroupLayout processingParamsPanelLayout = new javax.swing.GroupLayout(processingParamsPanel);
        processingParamsPanel.setLayout(processingParamsPanelLayout);
        processingParamsPanelLayout.setHorizontalGroup(
            processingParamsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(processingParamsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(processingParamsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(estimateAScoreLabel)
                    .addComponent(proteinFdrLabel)
                    .addComponent(peptideFdrLabel)
                    .addComponent(psmFdrLabel))
                .addGap(10, 10, 10)
                .addGroup(processingParamsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, processingParamsPanelLayout.createSequentialGroup()
                        .addComponent(proteinFdrTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(percentLabel3))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, processingParamsPanelLayout.createSequentialGroup()
                        .addComponent(peptideFdrTxt)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(percentLabel2))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, processingParamsPanelLayout.createSequentialGroup()
                        .addGroup(processingParamsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(ascoreCmb, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(psmFdrTxt))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(percentLabel)))
                .addContainerGap())
        );
        processingParamsPanelLayout.setVerticalGroup(
            processingParamsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(processingParamsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(processingParamsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(proteinFdrTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(percentLabel3)
                    .addComponent(proteinFdrLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(processingParamsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(peptideFdrTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(percentLabel2)
                    .addComponent(peptideFdrLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(processingParamsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(psmFdrLabel)
                    .addComponent(psmFdrTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(percentLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(processingParamsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(estimateAScoreLabel)
                    .addComponent(ascoreCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(processingParamsPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(okButton)))
                .addContainerGap())
        );
        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(processingParamsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(okButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backgroundPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backgroundPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Update the preferences and close the dialog.
     * 
     * @param evt 
     */
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        if (validateInput()) {
            processingPreferences.setProteinFDR(new Double(proteinFdrTxt.getText().trim()));
            processingPreferences.setPeptideFDR(new Double(peptideFdrTxt.getText().trim()));
            processingPreferences.setPsmFDR(new Double(psmFdrTxt.getText().trim()));
            processingPreferences.estimateAScore(ascoreCmb.getSelectedIndex() == 0);
            dispose();
        }
    }//GEN-LAST:event_okButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox ascoreCmb;
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JLabel estimateAScoreLabel;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel peptideFdrLabel;
    private javax.swing.JTextField peptideFdrTxt;
    private javax.swing.JLabel percentLabel;
    private javax.swing.JLabel percentLabel2;
    private javax.swing.JLabel percentLabel3;
    private javax.swing.JPanel processingParamsPanel;
    private javax.swing.JLabel proteinFdrLabel;
    private javax.swing.JTextField proteinFdrTxt;
    private javax.swing.JLabel psmFdrLabel;
    private javax.swing.JTextField psmFdrTxt;
    // End of variables declaration//GEN-END:variables
}