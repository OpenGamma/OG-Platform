/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.config.test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.List;
import java.util.Random;

import javax.time.Instant;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.opengamma.config.ConfigDocument;
import com.opengamma.config.ConfigSearchHistoricRequest;
import com.opengamma.config.ConfigSearchHistoricResult;
import com.opengamma.config.ConfigSearchRequest;
import com.opengamma.config.ConfigSearchResult;
import com.opengamma.config.DefaultConfigDocument;
import com.opengamma.config.db.MongoDBConfigMaster;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.MongoDBConnectionSettings;

/**
 * 
 *
 * @param <T> Configuration Document
 */
public abstract class MongoDBConfigMasterTestCase<T extends Serializable> {
  
  private MongoDBConfigMaster<T> _configMaster;
  private Class<?> _entityType;
  private Random _random = new Random();
  
  public MongoDBConfigMasterTestCase(Class<?> entityType) {
    _entityType = entityType;
  }

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    _configMaster = createMongoConfigMaster();
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    _configMaster.close();
    MongoDBConnectionSettings settings = getMongoDBConnectionSettings();
    Mongo mongo = new Mongo(settings.getHost(), settings.getPort());
    DB db = mongo.getDB(settings.getDatabase());
    String collectionName =  settings.getCollectionName() == null ? _entityType.getSimpleName() : settings.getCollectionName();
    DBCollection collection = db.getCollection(collectionName);
    collection.drop();
    mongo.close();
  }
  
  protected abstract MongoDBConfigMaster<T> createMongoConfigMaster();
  protected abstract ConfigDocument<T> makeTestConfigDoc(int version);
  protected abstract MongoDBConnectionSettings getMongoDBConnectionSettings();
  protected abstract T makeRandomConfigDoc();
  protected abstract void assertConfigDocumentValue(T expected, T actual);
  
  @Test
  public void add() throws Exception {
    
    Instant before = Instant.nowSystemClock();
    
    addRandomDocs();
    
    ConfigDocument<T> doc1 = makeTestConfigDoc(1);
    String name = doc1.getName();
    
    doc1 = _configMaster.add(doc1);
    
    assertNotNull(doc1);
    assertEquals(name, doc1.getName());
    
    Instant creationTime = doc1.getCreationInstant();
    assertNotNull(creationTime);
    Instant lastReadInstant = doc1.getLastReadInstant();
    assertNotNull(lastReadInstant);
    assertTrue(before.isBefore(creationTime));
    assertEquals(creationTime, lastReadInstant);
    
    String oid = doc1.getOid();
    assertNotNull(oid);
    assertEquals(1, doc1.getVersion());
    Thread.sleep(1000);
    
    ConfigDocument<T> findByName = searchByName(name);
    assertNotNull(findByName);
    assertConfigDoc(doc1, findByName);
    assertTrue(findByName.getLastReadInstant().isAfter(lastReadInstant));
    
  }

  /**
   * @param name
   * @return
   */
  private ConfigDocument<T> searchByName(String name) {
    ConfigSearchRequest request = new ConfigSearchRequest();
    request.setName(name);
    ConfigSearchResult<T> searchResult = _configMaster.search(request);
    List<ConfigDocument<T>> documents = searchResult.getDocuments();
    if (documents.size() == 1) {
      return documents.get(0);
    } else {
      return null;
    }
  }

  private void assertConfigDoc(ConfigDocument<T> expected, ConfigDocument<T> actual) {
    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.getName(), actual.getName());
    assertEquals(expected.getOid(), actual.getOid());
    assertConfigDocumentValue(expected.getValue(), actual.getValue());
    assertEquals(expected.getVersion(), actual.getVersion());
    
  }
  
  @Test
  public void update() throws Exception {
    addRandomDocs();
    
    ConfigDocument<T> version1 = makeTestConfigDoc(1);
    version1 = _configMaster.add(version1);
    assertNotNull(version1);
    ConfigDocument<T> version2 = makeTestConfigDoc(2);
    
    Thread.sleep(1000);
    DefaultConfigDocument<T> doc = (DefaultConfigDocument<T>) version2;
    doc.setName(version1.getName());
    doc.setOid(version1.getOid());
    doc.setVersion(version1.getVersion());
    
    version2 = _configMaster.update(doc);
    assertNotNull(version2);
    Instant lastReadInstant = version2.getLastReadInstant();
    assertNotNull(lastReadInstant);
    
    //look up should return persistedVersion2 as current;
    Thread.sleep(1000);
    ConfigDocument<T> findByName = searchByName(version2.getName());
    
    assertNotNull(findByName);
    assertConfigDoc(version2, findByName);
    assertTrue(findByName.getLastReadInstant().isAfter(lastReadInstant));
    
    assertEquals(version1.getOid(), version2.getOid());
    assertEquals(1, version1.getVersion());
    assertEquals(2, version2.getVersion());
    
  }
  
  @Test
  public void getByNameWithEffectiveTime() throws Exception {
    addRandomDocs();
    
    ConfigDocument<T> doc1 = makeTestConfigDoc(1);
    doc1 = _configMaster.add(doc1);
    Instant after1 = Instant.nowSystemClock();
    Thread.sleep(1000);
    
    ConfigDocument<T> doc2 = makeTestConfigDoc(2);
    DefaultConfigDocument<T> configDoc = (DefaultConfigDocument<T>) doc2;
    configDoc.setOid(doc1.getOid());
    configDoc.setVersion(doc1.getVersion());
    configDoc.setName(doc1.getName());
    
    doc2 = _configMaster.update(doc2);
    Instant after2 = Instant.nowSystemClock();
    Thread.sleep(1000);

    //lets change the name
    ConfigDocument<T> doc3 = makeTestConfigDoc(3);
    configDoc = (DefaultConfigDocument<T>) doc3;
    String changeOfName = "changeOfName";
    configDoc.setName(changeOfName);
    configDoc.setOid(doc2.getOid());
    configDoc.setVersion(doc2.getVersion());
    
    doc3 = _configMaster.update(configDoc);
    Instant after3 = Instant.nowSystemClock();
    Thread.sleep(1000);
    
    //should return version3
    ConfigDocument<T> findByName = searchByNameWithEffectiveTime(changeOfName, after3);
    assertNotNull(findByName);
    assertConfigDoc(doc3, findByName);
    Instant lastReadInstant = findByName.getLastReadInstant();
    assertNotNull(lastReadInstant);
    assertTrue(lastReadInstant.isAfter(after3));
    
    
    //should return version2
    findByName = searchByNameWithEffectiveTime(doc2.getName(), after3);
    assertNotNull(findByName);
    assertConfigDoc(doc2, findByName);
    lastReadInstant = findByName.getLastReadInstant();
    assertNotNull(lastReadInstant);
    assertTrue(lastReadInstant.isAfter(after2));
    
    //should return version2
    findByName = searchByNameWithEffectiveTime(doc2.getName(), after2);
    assertNotNull(findByName);
    assertConfigDoc(doc2, findByName);
    lastReadInstant = findByName.getLastReadInstant();
    assertNotNull(lastReadInstant);
    assertTrue(lastReadInstant.isAfter(after2));
    
    //should return version1
    findByName = searchByNameWithEffectiveTime(doc2.getName(), after1);
    assertNotNull(findByName);
    assertConfigDoc(doc1, findByName);
    lastReadInstant = findByName.getLastReadInstant();
    assertNotNull(lastReadInstant);
    assertTrue(lastReadInstant.isAfter(after1));
    
    //should return null
    findByName = searchByNameWithEffectiveTime(changeOfName, after1);
    assertNull(findByName);
    
    //should return null
    findByName = searchByNameWithEffectiveTime("Unknown", Instant.nowSystemClock());
    assertNull(findByName);
    
  }

  private ConfigDocument<T> searchByNameWithEffectiveTime(String name, Instant time) {
    ConfigSearchRequest request = new ConfigSearchRequest();
    request.setName(name);
    request.setEffectiveTime(time);
    ConfigSearchResult<T> searchResult = _configMaster.search(request);
    List<ConfigDocument<T>> documents = searchResult.getDocuments();
    if (documents.isEmpty()) {
      return null;
    } else {
      assertTrue(documents.size() == 1);
      return documents.get(0);
    }
  }

    
  @Test
  public void getByUniqueIdentifier() throws Exception {
    addRandomDocs();
    
    ConfigDocument<T> doc1 = _configMaster.add(makeTestConfigDoc(1));
    assertNotNull(doc1);
    
    // Make sure at least some time elapses so that we can check the lastReadInstant later
    try {
      Thread.sleep(1);
    } catch (InterruptedException e) {
    }

    ConfigDocument<T> findById = _configMaster.get(UniqueIdentifier.of(MongoDBConfigMaster.IDENTIFIER_SCHEME_DEFAULT, doc1.getOid(), String.valueOf(doc1.getVersion())));
    assertNotNull(findById);
    assertConfigDoc(doc1, findById);
    Instant lastReadInstant = findById.getLastReadInstant();
    assertNotNull(lastReadInstant);
    assertTrue(lastReadInstant.isAfter(doc1.getLastReadInstant()));
    
    ConfigDocument<T> doc2 = makeTestConfigDoc(2);
    DefaultConfigDocument<T> configDoc = (DefaultConfigDocument<T>) doc2;
    configDoc.setName(doc1.getName());
    configDoc.setOid(doc1.getOid());
    configDoc.setVersion(doc1.getVersion());
    Thread.sleep(1000);
    doc2 = _configMaster.update(configDoc);
    assertNotNull(doc2);

    // Make sure at least some time elapses so that we can check the lastReadInstant later
    try {
      Thread.sleep(1);
    } catch (InterruptedException e) {
    }

    findById = _configMaster.get(UniqueIdentifier.of(MongoDBConfigMaster.IDENTIFIER_SCHEME_DEFAULT, doc2.getOid(), String.valueOf(doc2.getVersion())));
    assertNotNull(findById);
    assertConfigDoc(doc2, findById);
    lastReadInstant = findById.getLastReadInstant();
    assertNotNull(lastReadInstant);
    assertTrue(lastReadInstant.isAfter(doc2.getLastReadInstant()));
    
  }
  
  @Test
  public void searchHistoric() throws Exception {
    addRandomDocs();
    
    Instant start = Instant.nowSystemClock();
    Thread.sleep(100);
    ConfigDocument<T> doc1 = _configMaster.add(makeTestConfigDoc(1));
    Thread.sleep(100);
    Instant after1 = Instant.nowSystemClock();
    
    ConfigDocument<T> doc2 = makeTestConfigDoc(2);
    DefaultConfigDocument<T> configDoc = (DefaultConfigDocument<T>) doc2;
    configDoc.setOid(doc1.getOid());
    configDoc.setVersion(doc1.getVersion());
    configDoc.setName(doc1.getName());
    
    Thread.sleep(100);
    doc2 = _configMaster.update(doc2);
    Thread.sleep(100);
    Instant after2 = Instant.nowSystemClock();

    ConfigDocument<T> doc3 = makeTestConfigDoc(3);
    configDoc = (DefaultConfigDocument<T>) doc3;
    configDoc.setOid(doc2.getOid());
    configDoc.setVersion(doc2.getVersion());
    configDoc.setName(doc2.getName());
    Thread.sleep(100);
    doc3 = _configMaster.update(doc3);
    Thread.sleep(100);
    Instant after3 = Instant.nowSystemClock();
    
    ConfigSearchHistoricRequest historicRequest = new ConfigSearchHistoricRequest();
    historicRequest.setOid(doc1.getOid());
    historicRequest.setStartTime(start);
    historicRequest.setEndTime(after3);
    
    ConfigSearchHistoricResult<T> searchHistoric = _configMaster.searchHistoric(historicRequest);
    List<ConfigDocument<T>> allDocs = searchHistoric.getDocuments();
    
    assertNotNull(allDocs);
    assertEquals(3, allDocs.size());
    assertTrue(allDocs.contains(doc1));
    assertTrue(allDocs.contains(doc2));
    assertTrue(allDocs.contains(doc3));
    
    historicRequest.setStartTime(start);
    historicRequest.setEndTime(null);
    searchHistoric = _configMaster.searchHistoric(historicRequest);
    allDocs = searchHistoric.getDocuments();
    assertNotNull(allDocs);
    assertEquals(3, allDocs.size());
    assertTrue(allDocs.contains(doc1));
    assertTrue(allDocs.contains(doc2));
    assertTrue(allDocs.contains(doc3));
    
    historicRequest.setStartTime(start);
    historicRequest.setEndTime(start);
    searchHistoric = _configMaster.searchHistoric(historicRequest);
    allDocs = searchHistoric.getDocuments();
    assertNotNull(allDocs);
    assertTrue(allDocs.isEmpty());
    
    historicRequest.setStartTime(start);
    historicRequest.setEndTime(after1);
    searchHistoric = _configMaster.searchHistoric(historicRequest);
    allDocs = searchHistoric.getDocuments();
    assertNotNull(allDocs);
    assertEquals(1, allDocs.size());
    assertTrue(allDocs.contains(doc1));
    
    historicRequest.setStartTime(after1);
    historicRequest.setEndTime(after3);
    searchHistoric = _configMaster.searchHistoric(historicRequest);
    allDocs = searchHistoric.getDocuments();
    assertNotNull(allDocs);
    assertEquals(2, allDocs.size());
    assertTrue(allDocs.contains(doc2));
    assertTrue(allDocs.contains(doc3));
    
    historicRequest.setStartTime(after1);
    historicRequest.setEndTime(after2);
    searchHistoric = _configMaster.searchHistoric(historicRequest);
    allDocs = searchHistoric.getDocuments();
    assertNotNull(allDocs);
    assertEquals(1, allDocs.size());
    assertTrue(allDocs.contains(doc2));
  }
  
  @Test
  public void remove() throws Exception {
    
    addRandomDocs();
    
    ConfigDocument<T> doc = makeTestConfigDoc(1);
    String name = doc.getName();
    assertNull(doc.getOid());
    doc = _configMaster.add(doc);
    assertNotNull(doc);
    assertEquals(name, doc.getName());
    assertNotNull(doc.getOid());
    assertEquals(1, doc.getVersion());
    
    ConfigDocument<T> findByName = searchByName(name);
    assertNotNull(findByName);
    assertConfigDoc(doc, findByName);
    
    _configMaster.remove(doc.getUniqueIdentifier());
    
    findByName = searchByName(name);
    assertNull(findByName);
  }
  
//  @Test
//  public void getNames() throws Exception {
//    
//    Set<String> fromDbNames = _configRepo.getNames();
//    assertNotNull(fromDbNames);
//    assertTrue(fromDbNames.isEmpty());
//    
//    Set<String> expectedNames = new HashSet<String>();
//    while (expectedNames.size() < 10) {
//      expectedNames.add("RandName" + _random.nextInt());
//    }
//    
//    for (String name : expectedNames) {
//      T doc1 = makeTestConfigDoc(1);
//      _configRepo.insertNewItem(name, doc1);
//    }
//    
//    fromDbNames = _configRepo.getNames();
//    assertNotNull(fromDbNames);
//    assertEquals(expectedNames, fromDbNames);
//    
//  }
  
  private void addRandomDocs() {
    for (int i = 0; i < 10; i++) {
      String name = "RandName" + _random.nextInt();
      T doc = makeRandomConfigDoc();
      DefaultConfigDocument<T> document = new DefaultConfigDocument<T>();
      document.setValue(doc);
      document.setName(name);
      _configMaster.add(document);
    }
  }
  
}

  

 
