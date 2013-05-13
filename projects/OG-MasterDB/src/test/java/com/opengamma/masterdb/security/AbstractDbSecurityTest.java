/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security;

import org.testng.annotations.Test;

import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterFiles;
import com.opengamma.util.db.DbConnectorFactoryBean;
import com.opengamma.util.db.HibernateMappingFiles;
import com.opengamma.util.test.AbstractDbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Base tests for DbSecurityMaster.
 */
@Test(groups = TestGroup.UNIT_DB)
public abstract class AbstractDbSecurityTest extends AbstractDbTest {

  public AbstractDbSecurityTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
  }

  //-------------------------------------------------------------------------
  @Override
  protected Class<?> dbConnectorScope() {
    return AbstractDbSecurityTest.class;
  }

  @Override
  protected void initDbConnectorFactory(DbConnectorFactoryBean factory) {
    factory.setHibernateMappingFiles(new HibernateMappingFiles[] {new HibernateSecurityMasterFiles() });
  }

}
