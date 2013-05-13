/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import org.testng.annotations.Test;

import com.opengamma.util.db.DbConnectorFactoryBean;
import com.opengamma.util.db.HibernateMappingFiles;
import com.opengamma.util.test.AbstractDbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Base tests for DbBatchMaster.
 */
@Test(groups = TestGroup.UNIT_DB)
public abstract class AbstractDbBatchTest extends AbstractDbTest {

  public AbstractDbBatchTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
  }

  //-------------------------------------------------------------------------
  @Override
  protected Class<?> dbConnectorScope() {
    return AbstractDbBatchTest.class;
  }

  @Override
  protected void initDbConnectorFactory(DbConnectorFactoryBean factory) {
    factory.setHibernateMappingFiles(new HibernateMappingFiles[] {new HibernateBatchDbFiles() });
  }

}
