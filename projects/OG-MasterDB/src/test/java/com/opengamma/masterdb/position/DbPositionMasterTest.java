/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.position;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractDbTest;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Test DbPositionMaster.
 */
@Test(groups = TestGroup.UNIT_DB)
public class DbPositionMasterTest extends AbstractDbTest {

  private static final Logger s_logger = LoggerFactory.getLogger(DbPositionMasterTest.class);

  private DbPositionMaster _posMaster;

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbPositionMasterTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doSetUp() {
    _posMaster = new DbPositionMaster(getDbConnector());
  }

  @Override
  protected void doTearDown() {
    _posMaster = null;
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_basics() throws Exception {
    assertNotNull(_posMaster);
    assertEquals(true, _posMaster.getUniqueIdScheme().equals("DbPos"));
    assertNotNull(_posMaster.getDbConnector());
    assertNotNull(_posMaster.getClock());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals("DbPositionMaster[DbPos]", _posMaster.toString());
  }

}
