/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;


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
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigTypeMaster;
import com.opengamma.util.MongoDBConnectionSettings;
import com.opengamma.util.test.MongoDBTestUtils;

/**
 *
 */
public class MongoConfigSourceTest {
  
  private static final int DATA_SIZE = 10;
  private Random _random = new Random();
  private MongoDBConnectionSettings _mongoSettings;
  
  private MongoDBMasterConfigSource _configSource;
  private Map<String, ConfigDocument<MockViewDefinition>> _viewDefinitions;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    MongoDBConnectionSettings settings = MongoDBTestUtils.makeTestSettings("ViewDefinitions", true);
    _mongoSettings = settings;
    MongoDBConfigTypeMaster<MockViewDefinition> viewDefinitionConfigMaster = new MongoDBConfigTypeMaster<MockViewDefinition>(MockViewDefinition.class, settings);
    Map<String, ConfigDocument<MockViewDefinition>> viewDefinitions = populateWithViewDefinitions(viewDefinitionConfigMaster);
    _viewDefinitions = viewDefinitions;
    MongoDBMasterConfigSource mongoDBMasterConfigSource = new MongoDBMasterConfigSource();
    mongoDBMasterConfigSource.addConfigMaster(MockViewDefinition.class, viewDefinitionConfigMaster);
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
  
  private Map<String, ConfigDocument<MockViewDefinition>> populateWithViewDefinitions(ConfigTypeMaster<MockViewDefinition> repo) throws Exception {
    HashMap<String, ConfigDocument<MockViewDefinition>> viewDefinitionsMap = new HashMap<String, ConfigDocument<MockViewDefinition>>();
    //generate unique name for dataset
    Set<String> names = new TreeSet<String>();
    while (names.size() < DATA_SIZE) {
      names.add("RN" + _random.nextInt());
    }
    for (String name : names) {
      MockViewDefinition definition = new MockViewDefinition(name, UniqueIdentifier.of("PORTFOLIO_SCHEME", "ID" + _random.nextInt(100)));
      ConfigDocument<MockViewDefinition> configDocument = new ConfigDocument<MockViewDefinition>();
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
      List<MockViewDefinition> searchResult = _configSource.search(MockViewDefinition.class, request);
      assertNotNull(searchResult);
      assertTrue(searchResult.size() == 1);
      MockViewDefinition viewDefinition = searchResult.get(0);
      ConfigDocument<MockViewDefinition> configDocument = _viewDefinitions.get(name);
      MockViewDefinition expectedViewDefintion = configDocument.getValue();
      assertEquals(expectedViewDefintion, viewDefinition);
    }
  }
  
  @Test
  public void get() throws Exception {
    for (Entry<String, ConfigDocument<MockViewDefinition>> entry : _viewDefinitions.entrySet()) {
      ConfigDocument<MockViewDefinition> configDocument = entry.getValue();
      UniqueIdentifier uniqueId = configDocument.getUniqueId();
      MockViewDefinition viewDefinition = _configSource.get(MockViewDefinition.class, uniqueId);
      assertNotNull(viewDefinition);
      assertEquals(configDocument.getValue(), viewDefinition);
    }
  }

}
