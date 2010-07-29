/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.config.db;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.time.Instant;
import javax.time.TimeSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.config.ConfigDocument;
import com.opengamma.config.ConfigMaster;
import com.opengamma.config.ConfigSearchRequest;
import com.opengamma.config.ConfigSearchResult;
import com.opengamma.config.DefaultConfigDocument;
import com.opengamma.config.db.MongoDBConfigMaster;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FutureOptionSecurity;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.MongoDBConnectionSettings;

@Ignore("Just for finding out how expensive to update lastAccessedTime when you do a lookup")
public class ViewDefinitionLastAccessedTimePerformanceTest {
  private static final Logger s_logger = LoggerFactory.getLogger(ViewDefinitionLastAccessedTimePerformanceTest.class);
  private static final int DATA_SIZE = 100000;
  private static final int FIND_SIZE = 100;
  private static final int THREAD_POOL_SIZE = 2;
  private static final String COLLECTION_NAME = "TestViewDefinitions";
  private MongoDBConnectionSettings _mongoSettings;
  private TimeSource _timeSource = TimeSource.system();
  private Random _random = new Random();
  private Mongo _mongo;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    MongoDBConnectionSettings mongoDBConnectionSettings = new MongoDBConnectionSettings();
    mongoDBConnectionSettings.setDatabase("test");
    mongoDBConnectionSettings.setCollectionName(COLLECTION_NAME);
    _mongoSettings = mongoDBConnectionSettings;
    Mongo mongo = new Mongo(_mongoSettings.getHost(), _mongoSettings.getPort());
    _mongo = mongo;
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    DB db = _mongo.getDB(_mongoSettings.getDatabase());
    DBCollection dbCollection = db.getCollection(COLLECTION_NAME);
    dbCollection.drop();
  }
  
  private Map<String, ConfigDocument<ViewDefinition>> populateWithViewDefinitions(ConfigMaster<ViewDefinition> repo) throws Exception {
    HashMap<String, ConfigDocument<ViewDefinition>> viewDefinitionsMap = new HashMap<String, ConfigDocument<ViewDefinition>>();
    //generate unique name for dataset
    Set<String> names = new TreeSet<String>();
    while (names.size() < DATA_SIZE) {
      names.add("RN" + _random.nextInt());
    }
    for (String name : names) {
      ViewDefinition definition = new ViewDefinition(name, UniqueIdentifier.of("PORTFOLIO_SCHEME", "ID" + _random.nextInt(100)), "RandUser" + _random.nextInt(100));
      String configName = "ConfigName" + _random.nextInt();
      definition.addValueDefinition(configName, EquityOptionSecurity.SECURITY_TYPE, ValueRequirementNames.DELTA);
      definition.addValueDefinition(configName, EquityOptionSecurity.SECURITY_TYPE, ValueRequirementNames.GAMMA);
      definition.addValueDefinition(configName, FutureOptionSecurity.SECURITY_TYPE, ValueRequirementNames.RHO);
      definition.addValueDefinition(configName, FutureOptionSecurity.SECURITY_TYPE, ValueRequirementNames.FAIR_VALUE);
      
      DefaultConfigDocument<ViewDefinition> configDocument = new DefaultConfigDocument<ViewDefinition>();
      configDocument.setName(name);
      configDocument.setValue(definition);
      viewDefinitionsMap.put(name, repo.add(configDocument));
    }
    return viewDefinitionsMap;
  }
    
  @Test
  public void ReadWithUpdate() throws Exception {
    ConfigMaster<ViewDefinition> repo = new MongoDBConfigMaster<ViewDefinition>(ViewDefinition.class, _mongoSettings, true);
//    doTest(repo, "Average ReadWithUpdate took {}ms");
    doConcurrentTest(repo, "Average ReadWithUpdate took {}ms");
  }

  @Test
  public void ReadWithoutUpdate() throws Exception {
    ConfigMaster<ViewDefinition> repo = new MongoDBConfigMaster<ViewDefinition>(ViewDefinition.class, _mongoSettings, false);
//    doTest(repo, "Average ReadWithoutUpdate took {}ms");
    doConcurrentTest(repo, "Average ReadWithoutUpdate took {}ms");
  }
  
  private void doTest(ConfigMaster<ViewDefinition> repo, String msg) throws Exception {
    Map<String, ConfigDocument<ViewDefinition>> viewDefinitions = populateWithViewDefinitions(repo);
    
    //choose random names to lookup
    Set<String> lookUpNames = buildUpNamesToLookup(viewDefinitions.keySet());
    //do some read and check we have them
    for (String name : lookUpNames) {
      ConfigSearchRequest request = new ConfigSearchRequest();
      request.setName(name);
      ConfigSearchResult<ViewDefinition> searchResult = repo.search(request);
      ConfigDocument<ViewDefinition> findByName = searchResult.getDocuments().get(0);
      assertNotNull(findByName);
    }
    //collect stats
    double sum = 0.0;
    for (String name : lookUpNames) {
      ConfigSearchRequest request = new ConfigSearchRequest();
      request.setName(name);
      Instant before = _timeSource.instant();
      repo.search(request);
      Instant after = _timeSource.instant();
      long time = after.toEpochMillisLong() - before.toEpochMillisLong();
      sum += time;
    }
    s_logger.info("sum = {} dataSize = {}", sum, FIND_SIZE);
    s_logger.info(msg, sum/FIND_SIZE);
  }
  
  private Set<String> buildUpNamesToLookup(Set<String> names) {
    if (FIND_SIZE > names.size()) {
      throw new OpenGammaRuntimeException("num of names to lookup, " + FIND_SIZE + " is > than available names, " + names.size());
    }
    Set<String> result = new TreeSet<String>();
    List<String> namesInList = new ArrayList<String>(names);
    while (result.size() < FIND_SIZE) {
      result.add(namesInList.get(_random.nextInt(names.size())));
    }
    return result;
  }

  private void doConcurrentTest(ConfigMaster<ViewDefinition> repo, String msg) throws Exception {
    
    Map<String, ConfigDocument<ViewDefinition>> viewDefinitions = populateWithViewDefinitions(repo);
    //choose random names to lookup
    Set<String> lookUpNames = buildUpNamesToLookup(viewDefinitions.keySet());
    //do some read and check we have them
    for (String name : lookUpNames) {
      ConfigSearchRequest request = new ConfigSearchRequest();
      request.setName(name);
      ConfigSearchResult<ViewDefinition> searchResult = repo.search(request);
      ConfigDocument<ViewDefinition> findByName = searchResult.getDocuments().get(0);
      assertNotNull(findByName);
    }
    
    ExecutorService pool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    List<Future<Void>> lookupResult = new ArrayList<Future<Void>>();

    //submit name lookup task
    for (String name : lookUpNames) {
      lookupResult.add(pool.submit(new ViewDefinitionLookupTask(repo, name)));
    }
    
    //collect stats
    Instant start = _timeSource.instant();
    for (Future<Void> result : lookupResult) {
      result.get();
    }
    Instant end = _timeSource.instant();
    double totalTime = end.toEpochMillisLong() - start.toEpochMillisLong();
    s_logger.info("totalTime = {} dataSize = {}", totalTime, FIND_SIZE);
    s_logger.info(msg, totalTime/FIND_SIZE);
  }
  
  private class ViewDefinitionLookupTask implements Callable<Void> {
    private final ConfigMaster<ViewDefinition> _repo;
    private final ConfigSearchRequest _request = new ConfigSearchRequest();
    
    public ViewDefinitionLookupTask(ConfigMaster<ViewDefinition> repo, String name) {
      _request.setName(name);
      _repo = repo;
    }

    @Override
    public Void call() throws Exception {
      _repo.search(_request);
      return null;
    }
  
  }
  
}
