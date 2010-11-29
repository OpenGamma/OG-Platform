/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.config;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.opengamma.config.ConfigDocument;
import com.opengamma.config.ConfigSearchRequest;
import com.opengamma.config.ConfigTypeMaster;
import com.opengamma.config.mongo.MongoDBConfigTypeMaster;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.MongoDBConnectionSettings;
import com.opengamma.util.test.MongoDBTestUtils;

/**
 *
 */
public class MongoConfigSourceTest {
  
  private static final String EQUITY_OPTION = "EQUITY_OPTION";
  private static final int DATA_SIZE = 10;
  private Random _random = new Random();
  private MongoDBConnectionSettings _mongoSettings;
  
  private MongoDBMasterConfigSource _configSource;
  private Map<String, ConfigDocument<ViewDefinition>> _viewDefinitions;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    MongoDBConnectionSettings settings = MongoDBTestUtils.makeTestSettings("ViewDefinitions", true);
    _mongoSettings = settings;
    MongoDBConfigTypeMaster<ViewDefinition> viewDefinitionConfigMaster = new MongoDBConfigTypeMaster<ViewDefinition>(ViewDefinition.class, settings);
    Map<String, ConfigDocument<ViewDefinition>> viewDefinitions = populateWithViewDefinitions(viewDefinitionConfigMaster);
    _viewDefinitions = viewDefinitions;
    MongoDBMasterConfigSource mongoDBMasterConfigSource = new MongoDBMasterConfigSource();
    mongoDBMasterConfigSource.addConfigMaster(ViewDefinition.class, viewDefinitionConfigMaster);
    _configSource = mongoDBMasterConfigSource;
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    Mongo mongo = new Mongo(_mongoSettings.getHost(), _mongoSettings.getPort());
    DB db = mongo.getDB(_mongoSettings.getDatabase());
    DBCollection dbCollection = db.getCollection(_mongoSettings.getCollectionName());
    dbCollection.drop();
  }
  
  private Map<String, ConfigDocument<ViewDefinition>> populateWithViewDefinitions(ConfigTypeMaster<ViewDefinition> repo) throws Exception {
    HashMap<String, ConfigDocument<ViewDefinition>> viewDefinitionsMap = new HashMap<String, ConfigDocument<ViewDefinition>>();
    //generate unique name for dataset
    Set<String> names = new TreeSet<String>();
    while (names.size() < DATA_SIZE) {
      names.add("RN" + _random.nextInt());
    }
    for (String name : names) {
      ViewDefinition definition = new ViewDefinition(name, UniqueIdentifier.of("PORTFOLIO_SCHEME", "ID" + _random.nextInt(100)), "RandUser" + _random.nextInt(100));
      String configName = "ConfigName" + _random.nextInt();
      definition.addPortfolioRequirementName(configName, EQUITY_OPTION, ValueRequirementNames.DELTA);
      definition.addPortfolioRequirementName(configName, EQUITY_OPTION, ValueRequirementNames.GAMMA);
      definition.addPortfolioRequirementName(configName, EQUITY_OPTION, ValueRequirementNames.RHO);
      definition.addPortfolioRequirementName(configName, EQUITY_OPTION, ValueRequirementNames.FAIR_VALUE);
      
      ConfigDocument<ViewDefinition> configDocument = new ConfigDocument<ViewDefinition>();
      configDocument.setName(name);
      configDocument.setValue(definition);
      viewDefinitionsMap.put(name, repo.add(configDocument));
    }
    return viewDefinitionsMap;
  }
  
  @Test
  public void search() throws Exception {
    Set<String> names = _viewDefinitions.keySet();
    for (String name : names) {
      ConfigSearchRequest request = new ConfigSearchRequest();
      request.setName(name);
      List<ViewDefinition> searchResult = _configSource.search(ViewDefinition.class, request);
      assertNotNull(searchResult);
      assertTrue(searchResult.size() == 1);
      ViewDefinition viewDefinition = searchResult.get(0);
      ConfigDocument<ViewDefinition> configDocument = _viewDefinitions.get(name);
      ViewDefinition expectedViewDefintion = configDocument.getValue();
      assertEquals(expectedViewDefintion, viewDefinition);
    }
  }
  
  @Test
  public void get() throws Exception {
    for (Entry<String, ConfigDocument<ViewDefinition>> entry : _viewDefinitions.entrySet()) {
      ConfigDocument<ViewDefinition> configDocument = entry.getValue();
      UniqueIdentifier uniqueIdentifier = configDocument.getConfigId();
      ViewDefinition viewDefinition = _configSource.get(ViewDefinition.class, uniqueIdentifier);
      assertNotNull(viewDefinition);
      assertEquals(configDocument.getValue(), viewDefinition);
    }
  }

}
