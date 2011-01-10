/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.After;
import org.junit.Before;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.impl.MongoDBConfigTypeMaster;
import com.opengamma.master.config.impl.MongoDBConfigTypeMasterTestCase;
import com.opengamma.util.MongoDBConnectionSettings;
import com.opengamma.util.test.MongoDBTestUtils;

/**
 * Test UniqueIdentifier as a configuration document.
 */
public class UniqueIdentifierConfigTypeMasterTest extends MongoDBConfigTypeMasterTestCase<UniqueIdentifier> {

  private Random _random = new Random();
  private MongoDBConnectionSettings _mongoSettings;

  public UniqueIdentifierConfigTypeMasterTest() {
    super(UniqueIdentifier.class);
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();
  }

  //-------------------------------------------------------------------------
  @Override
  public MongoDBConfigTypeMaster<UniqueIdentifier> createMongoConfigMaster() {
    // use className as collection so do not set collectionName
    MongoDBConnectionSettings settings = MongoDBTestUtils.makeTestSettings(null, false);
    _mongoSettings = settings;
    return new MongoDBConfigTypeMaster<UniqueIdentifier>(UniqueIdentifier.class, settings);
  }

  @Override
  public ConfigDocument<UniqueIdentifier> makeTestConfigDoc(int version) {
    ConfigDocument<UniqueIdentifier> doc = new ConfigDocument<UniqueIdentifier>();
    doc.setName("TestName" + version);
    doc.setValue(UniqueIdentifier.of("TestScheme", "TestID", String.valueOf(version)));
    return doc;
  }

  @Override
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
