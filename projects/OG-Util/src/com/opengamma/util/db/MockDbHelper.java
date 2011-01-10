/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import java.sql.Driver;

import org.hibernate.dialect.Dialect;

/**
 * Mock helper.
 */
public class MockDbHelper extends DbHelper {

  @Override
  public Class<? extends Driver> getJDBCDriverClass() {
    return null;
  }

  @Override
  protected Dialect createHibernateDialect() {
    return null;
  }

}
