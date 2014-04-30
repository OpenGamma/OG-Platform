/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.examples.bloomberg.install;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.opengamma.component.ComponentRepository;
import com.opengamma.component.tool.ToolContextUtils;
import com.opengamma.examples.bloomberg.tool.ExampleConfigDatabaseCreator;
import com.opengamma.examples.bloomberg.tool.ExampleDatabaseChecker;
import com.opengamma.examples.bloomberg.tool.ExampleDatabaseCreator;
import com.opengamma.examples.bloomberg.tool.ExampleEmptyDatabaseCreator;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.integration.regression.DatabaseRestore;
import com.opengamma.integration.tool.IntegrationToolContext;
import com.opengamma.scripts.Scriptable;
import com.opengamma.util.ResourceUtils;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.db.tool.DbToolContext;
import com.opengamma.util.db.tool.DbUpgradeOperation;

/**
 * 
 */
@Scriptable
public class ExampleDatabaseCreatorGui {

  /**
   * Logger that is attached to the feedback loop.
   */
  private static final Logger s_feedbackLogger = LoggerFactory.getLogger(ExampleDatabaseCreator.class);

  /** Shared database URL. */
  private static final String KEY_SHARED_URL = "db.standard.url";
  /** Shared database user name. */
  private static final String KEY_SHARED_USER_NAME = "db.standard.username";
  /** Shared database password. */
  private static final String KEY_SHARED_PASSWORD = "db.standard.password";

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ExampleDatabaseCreatorGui.class);

  private static final String CMD_GUI_OPTION = "gui";

  private static final String CMD_CONFIG_OPTION = "config";

  public static void main(String[] args) {
    Options options = createOptions();
    CommandLineParser parser = new PosixParser();
    CommandLine line;
    try {
      line = parser.parse(options, args);
      // if no command line arguments, then use default arguments suitable for development in an IDE
      String configFile = line.hasOption(CMD_CONFIG_OPTION) ? line.getOptionValue(CMD_CONFIG_OPTION) : "classpath:/toolcontext/toolcontext-examplesbloomberg.properties";
      if (line.hasOption(CMD_GUI_OPTION)) {
        List<String> tables = ExampleDatabaseChecker.run(configFile);
        boolean dbExists = !tables.isEmpty();
        showUI(dbExists, configFile);
      } else {
        createCompleteDatabase(configFile);
      }
      System.exit(0);
    } catch (ParseException e) {
      usage(options);
      System.exit(1);
    } catch (final Exception ex) {
      s_logger.error("Caught exception", ex);
      ex.printStackTrace();
      System.exit(1);
    }

  }

  private static Options createOptions() {
    Options options = new Options();
    Option guiOption = new Option(CMD_GUI_OPTION,
                                  CMD_GUI_OPTION,
                                  false,
                                  "flag to indicate to run the tool in gui mode");
    guiOption.setArgName(CMD_GUI_OPTION);
    guiOption.setRequired(false);
    options.addOption(guiOption);

    Option cfgOption = new Option(CMD_CONFIG_OPTION, CMD_CONFIG_OPTION, true, "configuration file");
    cfgOption.setArgName(CMD_CONFIG_OPTION);
    cfgOption.setRequired(false);
    options.addOption(cfgOption);
    return options;
  }

  private static void usage(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.setWidth(120);
    formatter.printHelp("java " + ExampleDatabaseCreatorGui.class.getName(), options, true);
  }

  public static void showUI(boolean databaseExists, final String configFile) {

    final Dialog dialog = new Dialog((Frame) null);
    final CheckboxGroup group = new CheckboxGroup();

    dialog.addWindowListener(new WindowAdapter() {

      private boolean _zOrderUpdated;

      @Override
      public void windowActivated(final WindowEvent e) {
        if (!_zOrderUpdated) {
          _zOrderUpdated = true;
          s_feedbackLogger.info("#fixZOrder");
        }
      }

      @Override
      public void windowClosing(WindowEvent e) {
        dialog.dispose();
        System.exit(-1);
      }
    });

    dialog.setAlwaysOnTop(true);
    dialog.setLocationByPlatform(true);
    dialog.setModal(true);
    dialog.setTitle("Database setup.");
    dialog.setResizable(false);
    BorderLayout layout = new BorderLayout(30, 30);
    dialog.setLayout(layout);

    Panel p = new Panel();
    p.setLayout(new GridLayout(0, 1));

    Label label = new Label("Choose, one of the following options:");
    label.setAlignment(Label.CENTER);

    p.add(label);

    final Button cancellationButton = new Button("Exit");
    final Button confiramtionButton = new Button("OK");

    confiramtionButton.setEnabled(false);

    ItemListener radiobuttonChangeListener = new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if (group.getSelectedCheckbox() != null) {
          confiramtionButton.setEnabled(true);
        } else {
          confiramtionButton.setEnabled(false);
        }
      }
    };

    cancellationButton.addActionListener(new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        System.exit(0);
      }
    });

    //
    final JFileChooser fileDialog = new JFileChooser("Choose the location of db restore directory.");
    fileDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    fileDialog.setApproveButtonText("Select");
    //

    confiramtionButton.addActionListener(new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          int option = ((Checkbox2) group.getSelectedCheckbox()).getOption();
          switch (option) {
            case 1:
              s_logger.debug("Leaving database as it is");
              dialog.dispose();
              upgradeDatabase(configFile);
              break;
            case 2:
              s_logger.debug("Creating blank database with config data");
              dialog.dispose();
              createBlankDatabaseWithConfigData(configFile);
              break;
            case 3:
              s_logger.debug("Creating complete database");
              dialog.dispose();
              createCompleteDatabase(configFile);
              break;
            case 4:
              s_logger.debug("Creating complete database without any data");
              dialog.dispose();
              createBlankDatabaseWithoutAnyData(configFile);
              break;
            case 5:
              s_logger.debug("Creating complete database without any data");
              dialog.dispose();
              createBlankDatabaseWithoutAnyData(configFile);
              restoreDatabaseFromFiles(fileDialog.getSelectedFile(), configFile);
              break;
          }
        } catch (Exception ex) {
          s_logger.error("Caught exception", ex);
          ex.printStackTrace();
          System.exit(1);
        }
      }
    });

    p.add(new Panel());

    if (databaseExists) {
      p.add(new Checkbox2(1, "Leave the current database as it is.", group, radiobuttonChangeListener));
    }

    p.add(new Checkbox2(4, "Create blank database, schema only without data.", group, radiobuttonChangeListener));
    p.add(new Checkbox2(2, "Create blank database, populated only with configuration data.", group, radiobuttonChangeListener));
    p.add(new Checkbox2(3, "Create database, populated with configuration and with example portfolio.", group, radiobuttonChangeListener));


    Panel dBRestorePannel = new Panel(new BorderLayout());
    p.add(dBRestorePannel);


    final Checkbox2 dbRestoreOption = new Checkbox2(5,
                                                    "Restore database from files.",
                                                    group,
                                                    radiobuttonChangeListener);


    final Button restoreDbButton = new Button("Restore DB location");
    dBRestorePannel.add(restoreDbButton, BorderLayout.EAST);
    dBRestorePannel.add(dbRestoreOption, BorderLayout.CENTER);

    restoreDbButton.addActionListener(new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        int returnVal = fileDialog.showOpenDialog(dialog);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          File directory = fileDialog.getSelectedFile();
          if (!directory.exists()) {
            group.setSelectedCheckbox(null);
            confiramtionButton.setEnabled(false);
          } else {
            group.setSelectedCheckbox(dbRestoreOption);
            confiramtionButton.setEnabled(true);
          }
        } else {
          group.setSelectedCheckbox(null);
          confiramtionButton.setEnabled(false);
        }
      }
    });

    dbRestoreOption.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        int returnVal = fileDialog.showOpenDialog(dialog);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          File directory = fileDialog.getSelectedFile();
          if (!directory.exists()) {
            group.setSelectedCheckbox(null);
            confiramtionButton.setEnabled(false);
          } else {
            group.setSelectedCheckbox(dbRestoreOption);
            confiramtionButton.setEnabled(true);
          }
        } else {
          group.setSelectedCheckbox(null);
          confiramtionButton.setEnabled(false);
        }
      }
    });

    p.add(new Panel());


    Panel buttonPannel = new Panel(new BorderLayout());
    p.add(buttonPannel, BorderLayout.CENTER);

    buttonPannel.add(confiramtionButton, BorderLayout.EAST);
    buttonPannel.add(cancellationButton, BorderLayout.WEST);


    dialog.add(new Panel() { { setSize(new Dimension(100, 100)); } }, BorderLayout.NORTH);
    dialog.add(new Panel() { { setSize(new Dimension(100, 100)); } }, BorderLayout.SOUTH);
    dialog.add(new Panel() { { setSize(new Dimension(100, 100)); } }, BorderLayout.EAST);
    dialog.add(new Panel() { { setSize(new Dimension(100, 100)); } }, BorderLayout.WEST);
    dialog.add(p, BorderLayout.CENTER);

    dialog.setSize(800, 600);
    dialog.pack();
    dialog.setLocationRelativeTo(null);
    s_feedbackLogger.info("Waiting for the installation/upgrade mode to be selected");
    dialog.setVisible(true);
  }

  private static void upgradeDatabase(String configFile) throws Exception {
    Resource res = ResourceUtils.createResource(configFile);
    Properties props = new Properties();
    try (InputStream in = res.getInputStream()) {
      if (in == null) {
        throw new FileNotFoundException(configFile);
      }
      props.load(in);
    }

    ToolContext toolContext = ToolContextUtils.getToolContext(configFile, IntegrationToolContext.class);

    ComponentRepository componentRepository = (ComponentRepository) toolContext.getContextManager();

    DbConnector dbConnector = componentRepository.getInstance(DbConnector.class, "cfg");

    String jdbcUrl = Objects.requireNonNull(props.getProperty(KEY_SHARED_URL));
    String user = props.getProperty(KEY_SHARED_USER_NAME, "");
    String password = props.getProperty(KEY_SHARED_PASSWORD, "");

    DbToolContext dbToolContext = DbToolContext.from(dbConnector, jdbcUrl, user, password);

    DbUpgradeOperation upgradeOp = new DbUpgradeOperation(dbToolContext, false, null);
    upgradeOp.execute();
    if (!upgradeOp.isUpgradeRequired()) {
      s_logger.info("Database up-to-date");
    } else {
      s_logger.info("No Database upgrade operation required");
    }
  }

  private static void createBlankDatabaseWithConfigData(String configFile) throws Exception {
    new ExampleConfigDatabaseCreator().run(configFile);
  }

  private static void createBlankDatabaseWithoutAnyData(String configFile) throws Exception {
    new ExampleEmptyDatabaseCreator().run(configFile);
  }

  private static void createCompleteDatabase(String configFile) throws Exception {
    new ExampleDatabaseCreator().run(configFile);
  }

  private static void restoreDatabaseFromFiles(File dataDir, String configFile) throws Exception {

    ToolContext toolContext = ToolContextUtils.getToolContext(configFile, IntegrationToolContext.class);
    DatabaseRestore databaseRestore = new DatabaseRestore(dataDir,
                                                          toolContext.getSecurityMaster(),
                                                          toolContext.getPositionMaster(),
                                                          toolContext.getPortfolioMaster(),
                                                          toolContext.getConfigMaster(),
                                                          toolContext.getHistoricalTimeSeriesMaster(),
                                                          toolContext.getHolidayMaster(),
                                                          toolContext.getExchangeMaster(),
                                                          toolContext.getMarketDataSnapshotMaster(),
                                                          toolContext.getLegalEntityMaster(),
                                                          toolContext.getConventionMaster());
    databaseRestore.restoreDatabase();
  }

  static class Checkbox2 extends Checkbox {

    private final int _option;

    Checkbox2(int option, String label, CheckboxGroup group, ItemListener itemListener) throws HeadlessException {
      super(label, group, false);
      addItemListener(itemListener);
      _option = option;
    }

    int getOption() {
      return _option;
    }
  }
}
