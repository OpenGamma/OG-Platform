/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch.document;

import com.opengamma.id.UniqueId;
import com.opengamma.util.test.DbTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNull;
import static org.testng.AssertJUnit.assertNotNull;

/**
 * Tests DbBatchDocumentGetTest.
 */
public class DbBatchDocumentGetTest extends AbstractDbBatchDocumentMasterTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(DbBatchDocumentGetTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbBatchDocumentGetTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetBatchByNullUID() {
    _batchMaster.get(null);
  }

  @Test
  public void testGetBatchByPK() {
    assertNotNull(_batchMaster.getRiskRunById(1L));
  }

  @Test
  public void testGetBatchByUID() {    
    assertNotNull(_batchMaster.get(UniqueId.of(DbBatchDocumentMaster.IDENTIFIER_SCHEME_DEFAULT, "1")));
  }
  
  @Test
  public void testDeleteBatchByUID() {
    UniqueId uid = UniqueId.of(DbBatchDocumentMaster.IDENTIFIER_SCHEME_DEFAULT, "1");
    _batchMaster.delete(uid);
    assertNull(_batchMaster.get(uid));
  }  
}
