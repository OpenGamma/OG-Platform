/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.masterdb.DbMasterTestUtils;
import com.opengamma.util.test.DBTest;

/**
 * Test DbConfigMaster.
 */
public class DbConfigMasterTest extends DBTest {

  private static final Logger s_logger = LoggerFactory.getLogger(DbConfigMasterTest.class);

  private DbConfigMaster _cfgMaster;

  public DbConfigMasterTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    ConfigurableApplicationContext context = DbMasterTestUtils.getContext(getDatabaseType());
    DbConfigMaster master = (DbConfigMaster) context.getBean(getDatabaseType() + "DbConfigMaster");
    _cfgMaster = master;
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();
    _cfgMaster = null;
  }


  //-------------------------------------------------------------------------
  @Test
  public void test_basics() throws Exception {
    assertNotNull(_cfgMaster);
    assertEquals(true, _cfgMaster.getIdentifierScheme().equals("DbCfg"));
    assertNotNull(_cfgMaster.getDbSource());
    assertNotNull(_cfgMaster.getTimeSource());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_single_type() throws Exception {
    ConfigDocument<Identifier> addDoc = new ConfigDocument<Identifier>();
    addDoc.setName("Config test");
    addDoc.setValue(Identifier.of("A", "B"));
    ConfigDocument<Identifier> added = (ConfigDocument<Identifier>) _cfgMaster.add(addDoc);
    
    ConfigDocument<Identifier> loaded = _cfgMaster.get(added.getUniqueId(), Identifier.class);
    assertEquals(added, loaded);
    
    ConfigDocument<Identifier> loadedType = _cfgMaster.get(added.getUniqueId(), Identifier.class);
    assertEquals(added, loadedType);
  }
  
  //-------------------------------------------------------------------------
  @Test
  public void test_multiple_types() throws Exception {
    ConfigDocument<Identifier> identifierDoc = new ConfigDocument<Identifier>();
    identifierDoc.setName("Identifier test");
    identifierDoc.setValue(Identifier.of("A", "B"));
    
    ConfigDocument<Identifier> addedIdentifier =  (ConfigDocument<Identifier>) _cfgMaster.add(identifierDoc);
    
    ConfigDocument<IdentifierBundle> bundleDoc = new ConfigDocument<IdentifierBundle>();
    bundleDoc.setName("Bundle test");
    bundleDoc.setValue(IdentifierBundle.of(Identifier.of("A", "B"), Identifier.of("C", "D")));
    ConfigDocument<IdentifierBundle> addedBundle =  (ConfigDocument<IdentifierBundle>) _cfgMaster.add(bundleDoc);
    
    ConfigDocument<Identifier> loadedIdentifier = _cfgMaster.get(addedIdentifier.getUniqueId(), Identifier.class);
    assertEquals(addedIdentifier, loadedIdentifier);
    
    ConfigDocument<IdentifierBundle> loadedBundle = _cfgMaster.get(addedBundle.getUniqueId(), IdentifierBundle.class);
    assertEquals(addedBundle, loadedBundle);
  }
  
  //-------------------------------------------------------------------------
  @Test(expected = DataNotFoundException.class)
  public void test_get_invalid_type() throws Exception {
    ConfigDocument<Identifier> identifierDoc = new ConfigDocument<Identifier>();
    identifierDoc.setName("Identifier test");
    identifierDoc.setValue(Identifier.of("A", "B"));
    
    _cfgMaster.add(identifierDoc);
    
    ConfigDocument<IdentifierBundle> bundleDoc = new ConfigDocument<IdentifierBundle>();
    bundleDoc.setName("Bundle test");
    bundleDoc.setValue(IdentifierBundle.of(Identifier.of("A", "B"), Identifier.of("C", "D")));
    ConfigDocument<IdentifierBundle> addedBundle =  (ConfigDocument<IdentifierBundle>) _cfgMaster.add(bundleDoc);
    
    _cfgMaster.get(addedBundle.getUniqueId(), Identifier.class);    
  }
  
  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals("DbConfigMaster[DbCfg]", _cfgMaster.toString());
  }

}
