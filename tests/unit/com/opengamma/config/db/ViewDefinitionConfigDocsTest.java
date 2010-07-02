/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.config.db;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.After;
import org.junit.Before;

import com.opengamma.config.ConfigurationDocumentRepo;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FutureOptionSecurity;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.MongoDBConnectionSettings;
import com.opengamma.util.test.MongoDBTestUtils;

/**
 * TestCase for storing ViewDefinition config document in MongoDB
 *
 */
public class ViewDefinitionConfigDocsTest extends MongoConfigDocumentRepoTestcase<ViewDefinition> {
  
  private Random _random = new Random();
  /**
   * @param entityType
   */
  public ViewDefinitionConfigDocsTest() {
    super(ViewDefinition.class);
  }

  private MongoDBConnectionSettings _mongoSettings;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    super.setUp();
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    super.tearDown();
  }

  @Override
  public ConfigurationDocumentRepo<ViewDefinition> createMongoConfigRepo() {
    //use className as collection so dont set collectionName
    MongoDBConnectionSettings settings = MongoDBTestUtils.makeTestSettings(null, false);
    _mongoSettings = settings;
    return new MongoDBConfigurationRepo<ViewDefinition>(ViewDefinition.class, settings, null);
  }

  @Override
  protected ViewDefinition makeTestConfigDoc(int version) {
    String name = "testName" + version;
    String configName = "configTestName" + version;
    ViewDefinition definition = new ViewDefinition(name + version, UniqueIdentifier.of("PORTFOLIO_SCHEME", "1"), "testUsername");
    definition.addValueDefinition(configName, EquityOptionSecurity.EQUITY_OPTION_TYPE, ValueRequirementNames.DELTA);
    definition.addValueDefinition(configName, EquityOptionSecurity.EQUITY_OPTION_TYPE, ValueRequirementNames.GAMMA);
    definition.addValueDefinition(configName, FutureOptionSecurity.FUTURE_OPTION_TYPE, ValueRequirementNames.RHO);
    definition.addValueDefinition(configName, FutureOptionSecurity.FUTURE_OPTION_TYPE, ValueRequirementNames.FAIR_VALUE);
    return definition;
  }
  
  @Override
  public MongoDBConnectionSettings getMongoDBConnectionSettings() {
    return _mongoSettings;
  }

  @Override
  protected ViewDefinition makeRandomConfigDoc() {
    ViewDefinition definition = new ViewDefinition("RAND" + _random.nextInt(), UniqueIdentifier.of("PORTFOLIO_SCHEME", "ID" + _random.nextInt(100)), "RandUser" + _random.nextInt(100));
    String configName = "ConfigName" + _random.nextInt();
    definition.addValueDefinition(configName, EquityOptionSecurity.EQUITY_OPTION_TYPE, ValueRequirementNames.DELTA);
    definition.addValueDefinition(configName, EquityOptionSecurity.EQUITY_OPTION_TYPE, ValueRequirementNames.GAMMA);
    definition.addValueDefinition(configName, FutureOptionSecurity.FUTURE_OPTION_TYPE, ValueRequirementNames.RHO);
    definition.addValueDefinition(configName, FutureOptionSecurity.FUTURE_OPTION_TYPE, ValueRequirementNames.FAIR_VALUE);
    return definition;
  }

  @Override
  protected void assertConfigDocumentValue(ViewDefinition expected, ViewDefinition actual) {
    assertEquals(expected, actual);
  }

}
