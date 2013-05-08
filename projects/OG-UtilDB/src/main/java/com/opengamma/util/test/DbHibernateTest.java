/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import com.opengamma.util.db.DbConnectorFactoryBean;
import com.opengamma.util.db.HibernateMappingFiles;

/**
 * DB test involving Hibernate.
 */
public abstract class DbHibernateTest extends DbTest {

  protected DbHibernateTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, databaseVersion);
  }

  //-------------------------------------------------------------------------
  protected abstract HibernateMappingFiles[] getHibernateMappingFiles();

  @Override
  protected void initDbConnectorFactory(DbConnectorFactoryBean factory) {
    factory.setHibernateMappingFiles(getHibernateMappingFiles());
  }

}
