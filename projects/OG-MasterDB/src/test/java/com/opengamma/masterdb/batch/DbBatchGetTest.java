/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ObjectId;
import com.opengamma.util.test.DbTest;

/**
 * Tests DbBatchGetTest.
 */
public class DbBatchGetTest extends AbstractDbBatchMasterTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(DbBatchGetTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbBatchGetTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetBatchByNullUID() {
    _batchMaster.getRiskRun(null);
  }

  @Test
  public void testGetBatchByUID() {    
    assertNotNull(_batchMaster.getRiskRun(ObjectId.of(DbBatchMaster.BATCH_IDENTIFIER_SCHEME, "1")));
  }
  
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testDeleteBatchByUID() {
    ObjectId id = ObjectId.of(DbBatchMaster.BATCH_IDENTIFIER_SCHEME, "1");
    assertNotNull(_batchMaster.getRiskRun(id));
    _batchMaster.deleteRiskRun(id);
    _batchMaster.getRiskRun(id);
    fail("we should not reach this point due to exception");
  }  
}
