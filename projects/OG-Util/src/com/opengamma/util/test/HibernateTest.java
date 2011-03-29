/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import com.opengamma.util.db.DbSource;

/**
 * DB test involving Hibernate.
 */
public abstract class HibernateTest extends DBTest {
  
  private SessionFactory _sessionFactory;
  private static int testCount = 0;
  
  protected HibernateTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
  }
  
  public SessionFactory getSessionFactory() {
    return _sessionFactory;
  }

  public void setSessionFactory(SessionFactory sessionFactory) {
    _sessionFactory = sessionFactory;
  }
  
  public abstract Class<?>[] getHibernateMappingClasses();

  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    
    Configuration configuration = getDbTool().getTestHibernateConfiguration();
    for (Class<?> clazz : getHibernateMappingClasses()) {
      configuration.addClass(clazz);
    }

    SessionFactory sessionFactory = configuration.buildSessionFactory();
    setSessionFactory(sessionFactory);

    testCount++;
  }

  @AfterMethod
  public void tearDown() throws Exception {
    if (_sessionFactory != null) {
      _sessionFactory.close();
    }
    super.tearDown();
  }

  @Override
  public DbSource getDbSource() {
    DbSource source = super.getDbSource();
    return new DbSource(
        source.getName(),
        source.getDataSource(),
        source.getDialect(),
        getSessionFactory(),
        source.getTransactionDefinition(),
        source.getTransactionManager());
  }

}
