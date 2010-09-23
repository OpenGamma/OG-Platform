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

import com.opengamma.config.ConfigDocument;
import com.opengamma.config.ConfigMaster;
import com.opengamma.config.DefaultConfigDocument;
import com.opengamma.config.test.MongoDBConfigMasterTestCase;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.MongoDBConnectionSettings;
import com.opengamma.util.test.MongoDBTestUtils;

/**
 * Test UniqueIdentifier as a configuration document
 *
 */
public class UniqueIdentifierConfigMasterTest extends MongoDBConfigMasterTestCase<UniqueIdentifier> {

  private Random _random = new Random();
  private MongoDBConnectionSettings _mongoSettings;
  
  /**
   * @param entityType
   */
  public UniqueIdentifierConfigMasterTest() {
    super(UniqueIdentifier.class);
  }

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
  public ConfigMaster<UniqueIdentifier> createMongoConfigMaster() {
    //use className as collection so do not set collectionName
    MongoDBConnectionSettings settings = MongoDBTestUtils.makeTestSettings(null, false);
    _mongoSettings = settings;
    return new MongoDBConfigMaster<UniqueIdentifier>(UniqueIdentifier.class, settings, true);
  }

  @Override
  public ConfigDocument<UniqueIdentifier> makeTestConfigDoc(int version) {
    DefaultConfigDocument<UniqueIdentifier> doc = new DefaultConfigDocument<UniqueIdentifier>();
    doc.setName("TestName" + version);
    doc.setValue(UniqueIdentifier.of("TestScheme", "TestID", String.valueOf(version)));
    return doc;
  }

  public MongoDBConnectionSettings getMongoDBConnectionSettings() {
    return _mongoSettings;
  }

  @Override
  protected UniqueIdentifier makeRandomConfigDoc() {
    return UniqueIdentifier.of("SCHEME" + _random.nextInt(), "ID" + _random.nextInt(), String.valueOf(_random.nextInt(100)));
  }

  @Override
  protected void assertConfigDocumentValue(UniqueIdentifier expected, UniqueIdentifier actual) {
    assertEquals(expected.getValue(), actual.getValue());
  }

}
