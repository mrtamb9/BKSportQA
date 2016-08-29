package org.bksport.qa.mvc.view.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import org.bksport.qa.mvc.ApplicationFacade;
import org.bksport.qa.util.LayoutUtil;
import org.bksport.sparql.Query;

/**
 * 
 * @author congnh
 * 
 */
public class ContainerFrame extends JFrame {

  private static final long serialVersionUID = 0x2020202;

  private JButton           parseButton;
  private JButton           runButton;
  private JLabel            inputLabel;
  private JLabel            resultLabel;
  private JLabel            statusLabel;
  private JMenuBar          menuBar;
  private JMenu             fileMenu;
  private JMenu             helpMenu;
  private JMenuItem         exitMenuItem;
  private JMenuItem         aboutMenuItem;
  private JTable            resultTable;
  private JTextField        inputNLQTextField;
  private JTextArea         outputSPARQLTextArea;
  private JTextArea         logTextArea;
  private JProgressBar      progressBar;

  private Query             query            = null;

  public ContainerFrame() {
    super("BKSport QA");
    initComponents();
  }

  private void initComponents() {
    parseButton = new JButton("Parse");
    runButton = new JButton("Run");
    menuBar = new JMenuBar();
    fileMenu = new JMenu("File");
    helpMenu = new JMenu("Help");
    aboutMenuItem = new JMenuItem("About");
    exitMenuItem = new JMenuItem("Exit");
    inputLabel = new JLabel("Input natural language question:");
    statusLabel = new JLabel();
    resultLabel = new JLabel("Result:");
    progressBar = new JProgressBar();
    resultTable = new JTable();
    inputNLQTextField = new JTextField();
    outputSPARQLTextArea = new JTextArea();
    logTextArea = new JTextArea();

    exitMenuItem.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        ApplicationFacade.getInstance().shutdown();
      }
    });
    fileMenu.add(exitMenuItem);
    helpMenu.add(aboutMenuItem);
    menuBar.add(fileMenu);
    menuBar.add(helpMenu);

    parseButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent evt) {
        parseButtonActionPerformed(evt);
      }
    });
    runButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent evt) {
        runButtonActionPerformed(evt);
      }
    });
    outputSPARQLTextArea.setLineWrap(true);

    JPanel topPanel = new JPanel();
    topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
    topPanel.setBorder(new EmptyBorder(LayoutUtil.MARGIN, LayoutUtil.MARGIN,
        LayoutUtil.MARGIN, LayoutUtil.MARGIN));
    JPanel linePanel = new JPanel();
    linePanel.setLayout(new BoxLayout(linePanel, BoxLayout.X_AXIS));
    linePanel.add(inputNLQTextField);
    linePanel.add(Box.createHorizontalStrut(LayoutUtil.HGAP));
    linePanel.add(parseButton);
    linePanel.add(Box.createHorizontalStrut(LayoutUtil.HGAP));
    linePanel.add(runButton);
    inputLabel.setAlignmentX(0);
    linePanel.setAlignmentX(0);
    topPanel.add(inputLabel);
    topPanel.add(linePanel);

    JPanel centerPanel = new JPanel();
    centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
    centerPanel.setBorder(new EmptyBorder(new Insets(0, LayoutUtil.MARGIN, 0,
        LayoutUtil.MARGIN)));
    JScrollPane scrollPane = new JScrollPane(outputSPARQLTextArea);
    JScrollPane scrollPane1 = new JScrollPane(logTextArea);
    scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
    scrollPane1.setAlignmentX(Component.LEFT_ALIGNMENT);
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    splitPane.setLeftComponent(scrollPane);
    splitPane.setRightComponent(scrollPane1);
    splitPane.setResizeWeight(0.50f);
    // splitPane.invalidate();
    // centerPanel.add(scrollPane);
    // centerPanel.add(scrollPane1);
    centerPanel.add(splitPane);
    JPanel resultPanel = new JPanel();
    resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
    resultLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    resultPanel.add(resultLabel);
    resultPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    JScrollPane scrollPane2 = new JScrollPane(resultTable);
    scrollPane2.setAlignmentX(Component.LEFT_ALIGNMENT);
    resultPanel.add(scrollPane2);
    resultPanel.setVisible(false);
    centerPanel.add(resultPanel);

    JPanel bottomPanel = new JPanel();
    bottomPanel.setLayout(new GridBagLayout());
    bottomPanel.setBorder(new EmptyBorder(LayoutUtil.MARGIN, LayoutUtil.MARGIN,
        LayoutUtil.MARGIN, LayoutUtil.MARGIN));
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.weightx = 1.0;
    constraints.anchor = GridBagConstraints.SOUTH;
    constraints.ipady = LayoutUtil.VGAP;
    constraints.gridwidth = 3;
    constraints.gridx = 0;
    constraints.gridy = 0;
    bottomPanel.add(new JSeparator(), constraints);
    constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.gridy = 1;
    bottomPanel.add(statusLabel, constraints);
    constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.weightx = 1.0;
    constraints.gridx = 1;
    constraints.gridy = 1;
    bottomPanel.add(Box.createHorizontalGlue(), constraints);
    constraints = new GridBagConstraints();
    constraints.anchor = GridBagConstraints.LAST_LINE_END;
    constraints.gridx = 2;
    constraints.gridy = 1;
    progressBar.setVisible(false);
    bottomPanel.add(progressBar, constraints);
    // grid layout
    // bottomPanel.setLayout(new GridLayout(1, 3));
    // bottomPanel.setBorder(new EmptyBorder(LayoutUtil.MARGIN,
    // LayoutUtil.MARGIN
    // , LayoutUtil.MARGIN, LayoutUtil.MARGIN));
    // bottomPanel.add(statusLabel);
    // bottomPanel.add(Box.createHorizontalGlue());
    // progressBar.setMaximumSize(new Dimension(144, 14));
    // progressBar.setAlignmentX(1.0f);
    // bottomPanel.add(progressBar);
    // box layout
    // bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
    // bottomPanel.setBorder(new EmptyBorder(LayoutUtil.MARGIN,
    // LayoutUtil.MARGIN
    // , LayoutUtil.MARGIN, LayoutUtil.MARGIN));
    // progressBar.setAlignmentX(1.0f);
    // progressBar.setMaximumSize(new Dimension(144, 14));
    // bottomPanel.add(statusLabel);
    // bottomPanel.add(new Box.Filler(new Dimension(0, 14)
    // , new Dimension(LayoutUtil.HGAP, 14), new Dimension(LayoutUtil.HGAP,
    // 14)));
    // bottomPanel.add(progressBar);

    // setLayout(new BorderLayout(LayoutUtil.HGAP, LayoutUtil.VGAP));
    // ((BorderLayout)getLayout()).setHgap(LayoutUtil.HGAP);
    // ((BorderLayout)getLayout()).setVgap(LayoutUtil.VGAP);
    add(topPanel, BorderLayout.NORTH);
    add(centerPanel, BorderLayout.CENTER);
    add(bottomPanel, BorderLayout.SOUTH);
    setJMenuBar(menuBar);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }

  public void updateStatus(String status) {
    int STATUS_LENGTH = 30;
    if (status != null) {
      if (!status.isEmpty()) {
        if (status.length() > STATUS_LENGTH) {
          statusLabel.setText(status.substring(0, STATUS_LENGTH) + "...");
          statusLabel.setToolTipText(status);
        } else {
          statusLabel.setText(status);
          statusLabel.setToolTipText(status);
        }
      } else {
        statusLabel.setText("");
        statusLabel.setToolTipText("");
      }
    } else {
      statusLabel.setText("");
      statusLabel.setToolTipText("");
    }
  }

  public void updateProgress(int percent) {
    if (percent == -1) {
      progressBar.setIndeterminate(true);
      progressBar.setVisible(true);
    }
    if (percent > 0) {
      progressBar.setIndeterminate(false);
      progressBar.setValue(percent > 100 ? 100 : percent);
      progressBar.setVisible(true);
    }
    if (percent == 0) {
      progressBar.setIndeterminate(false);
      progressBar.setVisible(false);
    }
  }

  public void setQuery(Query query) {
    this.query = query;
    if (query != null) {
      outputSPARQLTextArea.setText(query.toString());
    } else {
      outputSPARQLTextArea.setText("Unable parsing query \""
          + inputNLQTextField.getText() + "\"");
    }
  }

  public void setResult(List<Object[]> result) {
    if (result.isEmpty()) {
      resultTable.setModel(new DefaultTableModel());
    } else {
      DefaultTableModel model = new DefaultTableModel();
      for (int i = 0; i < result.get(0).length; i++) {
        model.addColumn(result.get(0)[i]);
      }
      for (int i = 1; i < result.size(); i++) {
        model.addRow(result.get(i));
      }
      resultTable.setModel(model);
    }
    showResult();
  }

  public void setLog(String log) {
    logTextArea.setText(log);
  }

  public void parseButtonActionPerformed(ActionEvent evt) {
    hideResult();
    ApplicationFacade.getInstance().sendNotification(
        ApplicationFacade.PARSE_QUESTION_CMD, inputNLQTextField.getText());
  }

  public void runButtonActionPerformed(ActionEvent evt) {
    ApplicationFacade.getInstance().sendNotification(
        ApplicationFacade.QUERY_RESULT_CMD, query);
  }

  private void hideResult() {
    resultLabel.getParent().setVisible(false);
  }

  private void showResult() {
    resultLabel.getParent().setVisible(true);
  }

}
