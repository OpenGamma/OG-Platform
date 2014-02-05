/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.marketdata.manipulator.dsl;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.marketdata.manipulator.ScenarioDefinition;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.integration.marketdata.manipulator.dsl.Scenario;
import com.opengamma.integration.marketdata.manipulator.dsl.SimulationUtils;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.scripts.Scriptable;

/**
 * Creates a scenario from a Groovy script and saves it the the config database.
 */
@Scriptable
public class SaveScenario extends AbstractTool<ToolContext> {

  /** Command line option for specifying the scenario script location on the filesystem. */
  private static final String SCRIPT_LOCATION = "scriptlocation";
  /** Command line option for specifying the scenario definition object ID. */
  private static final String SCENARIO_ID = "scenarioid";

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(String[] args) {
    new SaveScenario().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() throws Exception {
    String scriptLocation = getCommandLine().getOptionValue('s');
    ConfigSource configSource = getToolContext().getConfigSource();
    ConfigMaster configMaster = getToolContext().getConfigMaster();

    Scenario scenario = SimulationUtils.createScenarioFromDsl(scriptLocation, null);
    ConfigItem<ScenarioDefinition> configItem = ConfigItem.of(scenario.createDefinition(), scenario.getName());
    if (getCommandLine().hasOption('i')) {
      ObjectId scenarioId = ObjectId.parse(getCommandLine().getOptionValue('i'));
      UniqueId latestScenarioId = configSource.get(scenarioId, VersionCorrection.LATEST).getUniqueId();
      configItem.setUniqueId(latestScenarioId);
      configMaster.update(new ConfigDocument(configItem));
    } else {
      configMaster.add(new ConfigDocument(configItem));
    }
  }

  @Override
  protected Options createOptions(boolean mandatoryConfigResource) {
    Options options = super.createOptions(mandatoryConfigResource);

    Option scriptLocation = new Option("s", SCRIPT_LOCATION, true, "Location of the scenario script on the filesystem");
    scriptLocation.setRequired(true);
    options.addOption(scriptLocation);

    Option scenarioId = new Option("i", SCENARIO_ID, true, "Object ID of the scenario definition. Omit to save as " +
        "a new definition");
    options.addOption(scenarioId);

    return options;
  }
}
