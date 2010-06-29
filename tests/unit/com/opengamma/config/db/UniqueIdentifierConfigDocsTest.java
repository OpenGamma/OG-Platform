/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.config.db;


import java.util.Random;

import org.junit.After;
import org.junit.Before;

import com.opengamma.config.ConfigurationDocumentRepo;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.MongoDBConnectionSettings;
import com.opengamma.util.test.MongoDBTestUtils;

/**
 * Test UniqueIdentifier as a configuration document
 *
 */
public class UniqueIdentifierConfigDocsTest extends MongoConfigDocumentRepoTestcase<UniqueIdentifier> {
  
  private Random _random = new Random();
  /**
   * @param entityType
   */
  public UniqueIdentifierConfigDocsTest() {
    super(UniqueIdentifier.class);
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
  public ConfigurationDocumentRepo<UniqueIdentifier> createMongoConfigRepo() {
    MongoDBConnectionSettings settings = MongoDBTestUtils.makeTestSettings(null, true);
//    settings.setHost("127.0.0.1");
//    settings.setPort(27017);
//    String dbName = System.getProperty("user.name") + "-unit";
//    settings.setDatabase(dbName);
    _mongoSettings = settings;
    return new MongoDBConfigurationRepo<UniqueIdentifier>(UniqueIdentifier.class, settings, null);
  }

  @Override
  public UniqueIdentifier makeTestConfigDoc(int version) {
    return UniqueIdentifier.of("TestScheme", "TestID", String.valueOf(version));
  }
  
  public MongoDBConnectionSettings getMongoDBConnectionSettings() {
    return _mongoSettings;
  }

  @Override
  protected UniqueIdentifier makeRandomConfigDoc() {
    return UniqueIdentifier.of("SCHEME" + _random.nextInt(), "ID" + _random.nextInt(), String.valueOf(_random.nextInt(100)));
  }

}
