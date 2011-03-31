/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.config;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.util.test.DBTest;


/**
 * Test getTypes() in DbConfigMaster
 */
public class QueryConfigDbConfigMasterWorkerGetTypesTest extends AbstractDbConfigMasterWorkerTest {
  
  private static final Logger s_logger = LoggerFactory.getLogger(QueryConfigDbConfigMasterWorkerGetTypesTest.class);

  @Factory(dataProvider = "databasesMoreVersions", dataProviderClass = DBTest.class)
  public QueryConfigDbConfigMasterWorkerGetTypesTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }
  
  @Test
  public void test_getTypes() {
    List<String> types = _cfgMaster.getTypes();
    assertNotNull(types);
    assertEquals(2, types.size());
    assertTrue(types.contains(Identifier.class.getName()));
    assertTrue(types.contains(IdentifierBundle.class.getName()));
  }

}
