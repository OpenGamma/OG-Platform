/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.config;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.config.ConfigMetaDataRequest;
import com.opengamma.master.config.ConfigMetaDataResult;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Test metaData() in DbConfigMaster
 */
@Test(groups = TestGroup.UNIT_DB)
public class QueryConfigDbConfigMasterWorkerMetaDataTest extends AbstractDbConfigMasterWorkerTest {

  private static final Logger s_logger = LoggerFactory.getLogger(QueryConfigDbConfigMasterWorkerMetaDataTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public QueryConfigDbConfigMasterWorkerMetaDataTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, true);
    s_logger.info("running testcases for {}", databaseType);
  }

  @Test
  public void test_metaData() {
    ConfigMetaDataRequest request = new ConfigMetaDataRequest();
    ConfigMetaDataResult result = _cfgMaster.metaData(request);
    assertNotNull(result);
    assertEquals(2, result.getConfigTypes().size());
    assertTrue(result.getConfigTypes().contains(ExternalId.class));
    assertTrue(result.getConfigTypes().contains(ExternalIdBundle.class));
  }

  public void test_metaData_noTypes() {
    ConfigMetaDataRequest request = new ConfigMetaDataRequest();
    request.setConfigTypes(false);
    ConfigMetaDataResult result = _cfgMaster.metaData(request);
    assertNotNull(result);
    assertEquals(0, result.getConfigTypes().size());
  }

}
