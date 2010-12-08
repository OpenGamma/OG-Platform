/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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

import com.opengamma.id.Identifier;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.masterdb.DbMasterTestUtils;
import com.opengamma.util.test.DBTest;

/**
 * Test DbConfigMaster.
 */
public class DbConfigTypeMasterTest extends DBTest {

  private static final Logger s_logger = LoggerFactory.getLogger(DbConfigTypeMasterTest.class);

  private DbConfigTypeMaster<Identifier> _cfgMaster;

  public DbConfigTypeMasterTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    ConfigurableApplicationContext context = DbMasterTestUtils.getContext(getDatabaseType());
    DbConfigMaster master = (DbConfigMaster) context.getBean(getDatabaseType() + "DbConfigMaster");
    _cfgMaster = (DbConfigTypeMaster<Identifier>) master.typed(Identifier.class);
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
    assertNotNull(_cfgMaster.getWorkers());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_equity() throws Exception {
    ConfigDocument<Identifier> addDoc = new ConfigDocument<Identifier>();
    addDoc.setName("Config test");
    addDoc.setValue(Identifier.of("A", "B"));
    ConfigDocument<Identifier> added = _cfgMaster.add(addDoc);
    
    ConfigDocument<Identifier> loaded = _cfgMaster.get(added.getUniqueId());
    assertEquals(added, loaded);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals("DbConfigTypeMaster[DbCfg]", _cfgMaster.toString());
  }

}
