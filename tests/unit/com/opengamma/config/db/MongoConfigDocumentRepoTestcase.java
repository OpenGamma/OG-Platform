/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.config.db;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.time.Instant;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.opengamma.config.ConfigurationDocument;
import com.opengamma.config.ConfigurationDocumentRepo;
import com.opengamma.util.MongoDBConnectionSettings;

/**
 * 
 *
 * @param <T> Configuration Document
 */
public abstract class MongoConfigDocumentRepoTestcase<T extends Serializable> {
  
  private ConfigurationDocumentRepo<T> _configRepo;
  private Class<?> _entityType;
  private Random _random = new Random();
  
  public MongoConfigDocumentRepoTestcase(Class<?> entityType) {
    _entityType = entityType;
  }

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    _configRepo = createMongoConfigRepo();
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    MongoDBConnectionSettings settings = getMongoDBConnectionSettings();
    Mongo mongo = new Mongo(settings.getHost(), settings.getPort());
    DB db = mongo.getDB(settings.getDatabase());
    String collectionName =  settings.getCollectionName() == null ? _entityType.getSimpleName() : settings.getCollectionName();
    DBCollection collection = db.getCollection(collectionName);
    collection.drop();
  }
  
  protected abstract ConfigurationDocumentRepo<T> createMongoConfigRepo();
  protected abstract T makeTestConfigDoc(int version);
  protected abstract MongoDBConnectionSettings getMongoDBConnectionSettings();
  protected abstract T makeRandomConfigDoc();
  protected abstract void assertConfigDocumentValue(T expected, T actual);
  
  @Test
  public void insertNewItem() throws Exception {
    
    Instant before = Instant.nowSystemClock();
    
    addRandomDocs();
    
    T doc = makeTestConfigDoc(1);
    String name = "testName";
    ConfigurationDocument<T> newDoc = _configRepo.insertNewItem(name, doc);
    
    assertNotNull(newDoc);
    assertEquals(name, newDoc.getName());
    
    Instant creationTime = newDoc.getCreationInstant();
    assertNotNull(creationTime);
    Instant lastReadInstant = newDoc.getLastReadInstant();
    assertNotNull(lastReadInstant);
    assertTrue(before.isBefore(creationTime));
    assertEquals(creationTime, lastReadInstant);
    
    String oid = newDoc.getOid();
    assertNotNull(oid);
    assertEquals(1, newDoc.getVersion());
    Thread.sleep(1000);
    ConfigurationDocument<T> findByName = _configRepo.getByName(name);
    assertNotNull(findByName);
    assertConfigDoc(newDoc, findByName);
    assertTrue(findByName.getLastReadInstant().isAfter(lastReadInstant));
  }

  private void assertConfigDoc(ConfigurationDocument<T> expected, ConfigurationDocument<T> actual) {
    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.getName(), actual.getName());
    assertEquals(expected.getOid(), actual.getOid());
    assertConfigDocumentValue(expected.getValue(), actual.getValue());
    assertEquals(expected.getVersion(), actual.getVersion());
    
  }
  
  @Test
  public void insertNewVersion() throws Exception {
    addRandomDocs();
    
    T doc = makeTestConfigDoc(1);
    String name = "testName";
    ConfigurationDocument<T> previousDoc = _configRepo.insertNewItem(name, doc);
    assertNotNull(previousDoc);
    T doc2 = makeTestConfigDoc(2);
    Thread.sleep(1000);
    ConfigurationDocument<T> currentDoc = _configRepo.insertNewVersion(previousDoc.getOid(), doc2);
    Instant lastReadInstant = currentDoc.getLastReadInstant();
    assertNotNull(currentDoc);
    assertNotNull(lastReadInstant);
    
    //look up should return doc2 as current;
    Thread.sleep(1000);
    ConfigurationDocument<T> findByName = _configRepo.getByName(name);
    assertNotNull(findByName);
    assertConfigDoc(currentDoc, findByName);
    assertTrue(findByName.getLastReadInstant().isAfter(lastReadInstant));
    
  }
  
  @Test
  public void getByNameWithEffectiveTime() throws Exception {
    addRandomDocs();
    
    T doc1 = makeTestConfigDoc(1);
    String testName = "testName";
    ConfigurationDocument<T> configDoc1 = _configRepo.insertNewItem(testName, doc1);
    Instant after1 = Instant.nowSystemClock();
    Thread.sleep(1000);
    
    T doc2 = makeTestConfigDoc(2);
    ConfigurationDocument<T> configDoc2 = _configRepo.insertNewVersion(configDoc1.getOid(), doc2);
    Instant after2 = Instant.nowSystemClock();
    Thread.sleep(1000);

    //lets change the name
    T doc3 = makeTestConfigDoc(3);
    String changeOfName = "changeOfName";
    ConfigurationDocument<T> configDoc3 = _configRepo.insertNewVersion(configDoc2.getOid(), changeOfName, doc3);
    Instant after3 = Instant.nowSystemClock();
    Thread.sleep(1000);
    
    //should return version3
    ConfigurationDocument<T> findByName = _configRepo.getByName(changeOfName, after3);
    assertNotNull(findByName);
    assertConfigDoc(configDoc3, findByName);
    Instant lastReadInstant = findByName.getLastReadInstant();
    assertNotNull(lastReadInstant);
    assertTrue(lastReadInstant.isAfter(after3));
    
    
    //should return version2
    findByName = _configRepo.getByName(testName, after3);
    assertNotNull(findByName);
    assertConfigDoc(configDoc2, findByName);
    lastReadInstant = findByName.getLastReadInstant();
    assertNotNull(lastReadInstant);
    assertTrue(lastReadInstant.isAfter(after2));
    
    //should return version2
    findByName = _configRepo.getByName(testName, after2);
    assertNotNull(findByName);
    assertConfigDoc(configDoc2, findByName);
    lastReadInstant = findByName.getLastReadInstant();
    assertNotNull(lastReadInstant);
    assertTrue(lastReadInstant.isAfter(after2));
    
    //should return version1
    findByName = _configRepo.getByName(testName, after1);
    assertNotNull(findByName);
    assertConfigDoc(configDoc1, findByName);
    lastReadInstant = findByName.getLastReadInstant();
    assertNotNull(lastReadInstant);
    assertTrue(lastReadInstant.isAfter(after1));
    
    //should return null
    findByName = _configRepo.getByName(changeOfName, after1);
    assertNull(findByName);
    
    //should return null
    findByName = _configRepo.getByName("Unknown", Instant.nowSystemClock());
    assertNull(findByName);
    
  }
  
  @Test
  public void getByOid() throws Exception {
    addRandomDocs();
    
    T doc = makeTestConfigDoc(1);
    String name = "testName";
    ConfigurationDocument<T> configDoc1 = _configRepo.insertNewItem(name, doc);
    assertNotNull(configDoc1);
    
    ConfigurationDocument<T> byOid = _configRepo.getByOid(configDoc1.getOid(), configDoc1.getVersion());
    assertNotNull(byOid);
    assertConfigDoc(configDoc1, byOid);
    Instant lastReadInstant = byOid.getLastReadInstant();
    assertNotNull(lastReadInstant);
    assertTrue(lastReadInstant.isAfter(configDoc1.getLastReadInstant()));
    
    T doc2 = makeTestConfigDoc(2);
    Thread.sleep(1000);
    ConfigurationDocument<T> configDoc2 = _configRepo.insertNewVersion(configDoc1.getOid(), doc2);
    assertNotNull(configDoc1);
    
    byOid = _configRepo.getByOid(configDoc2.getOid(), configDoc2.getVersion());
    assertNotNull(byOid);
    assertConfigDoc(configDoc2, byOid);
    lastReadInstant = byOid.getLastReadInstant();
    assertNotNull(lastReadInstant);
    assertTrue(lastReadInstant.isAfter(configDoc2.getLastReadInstant()));
    
  }
  
  @Test
  public void getSequence() throws Exception {
    addRandomDocs();
    
    Instant start = Instant.nowSystemClock();
    Thread.sleep(1000);
    String name = "testName";
    T doc1 = makeTestConfigDoc(1);
    ConfigurationDocument<T> configDoc1 = _configRepo.insertNewItem(name, doc1);
    Thread.sleep(1000);
    Instant after1 = Instant.nowSystemClock();
    
    T doc2 = makeTestConfigDoc(2);
    ConfigurationDocument<T> configDoc2 = _configRepo.insertNewVersion(configDoc1.getOid(), doc2);
    Thread.sleep(1000);
    Instant after2 = Instant.nowSystemClock();

    T doc3 = makeTestConfigDoc(3);
    ConfigurationDocument<T> configDoc3 = _configRepo.insertNewVersion(configDoc2.getOid(), doc3);
    Thread.sleep(1000);
    Instant after3 = Instant.nowSystemClock();
    
    List<ConfigurationDocument<T>> allDocs = _configRepo.getSequence(configDoc1.getOid(), start, after3);
    assertNotNull(allDocs);
    assertEquals(3, allDocs.size());
    assertTrue(allDocs.contains(configDoc1));
    assertTrue(allDocs.contains(configDoc2));
    assertTrue(allDocs.contains(configDoc3));
    
    allDocs = _configRepo.getSequence(configDoc1.getOid(), start, null);
    assertNotNull(allDocs);
    assertEquals(3, allDocs.size());
    assertTrue(allDocs.contains(configDoc1));
    assertTrue(allDocs.contains(configDoc2));
    assertTrue(allDocs.contains(configDoc3));
    
    allDocs = _configRepo.getSequence(configDoc1.getOid(), start, start);
    assertNotNull(allDocs);
    assertTrue(allDocs.isEmpty());
    
    allDocs = _configRepo.getSequence(configDoc1.getOid(), start, after1);
    assertNotNull(allDocs);
    assertEquals(1, allDocs.size());
    assertTrue(allDocs.contains(configDoc1));
    
    allDocs = _configRepo.getSequence(configDoc1.getOid(), after1, after3);
    assertNotNull(allDocs);
    assertEquals(2, allDocs.size());
    assertTrue(allDocs.contains(configDoc2));
    assertTrue(allDocs.contains(configDoc3));
    
    allDocs = _configRepo.getSequence(configDoc1.getOid(), after1, after2);
    assertNotNull(allDocs);
    assertEquals(1, allDocs.size());
    assertTrue(allDocs.contains(configDoc2));
  }
  
  @Test
  public void getNames() throws Exception {
    
    Set<String> fromDbNames = _configRepo.getNames();
    assertNotNull(fromDbNames);
    assertTrue(fromDbNames.isEmpty());
    
    Set<String> expectedNames = new HashSet<String>();
    while (expectedNames.size() < 10) {
      expectedNames.add("RandName" + _random.nextInt());
    }
    
    for (String name : expectedNames) {
      T doc1 = makeTestConfigDoc(1);
      _configRepo.insertNewItem(name, doc1);
    }
    
    fromDbNames = _configRepo.getNames();
    assertNotNull(fromDbNames);
    assertEquals(expectedNames, fromDbNames);
    
  }
  
  private void addRandomDocs() {
    for (int i = 0; i < 10; i++) {
      String name = "RandName" + _random.nextInt();
      T doc = makeRandomConfigDoc();
      _configRepo.insertNewItem(name, doc);
    }
  }
  
}

  

 
