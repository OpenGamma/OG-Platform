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

import com.opengamma.util.db.DbConnector;
import com.opengamma.util.db.DbConnectorFactoryBean;

/**
 * DB test involving Hibernate.
 */
public abstract class HibernateTest extends DbTest {

  private SessionFactory _sessionFactory;

  protected HibernateTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, databaseVersion);
  }

  //-------------------------------------------------------------------------
  protected SessionFactory getSessionFactory() {
    return _sessionFactory;
  }

  protected void setSessionFactory(SessionFactory sessionFactory) {
    _sessionFactory = sessionFactory;
  }

  protected abstract Class<?>[] getHibernateMappingClasses();

  @BeforeMethod(groups = {TestGroup.UNIT_DB, TestGroup.INTEGRATION})
  public void setUp() throws Exception {
    super.setUp();
    
    Configuration configuration = getDbTool().getTestHibernateConfiguration();
    for (Class<?> clazz : getHibernateMappingClasses()) {
      configuration.addClass(clazz);
    }
    
    SessionFactory sessionFactory = configuration.buildSessionFactory();
    setSessionFactory(sessionFactory);
  }

  @AfterMethod(groups = {TestGroup.UNIT_DB, TestGroup.INTEGRATION})
  public void tearDown() throws Exception {
    if (_sessionFactory != null) {
      _sessionFactory.close();
    }
    super.tearDown();
  }

  @Override
  public DbConnector getDbConnector() {
    DbConnector dbConnector = super.getDbConnector();
    DbConnectorFactoryBean factory = new DbConnectorFactoryBean(dbConnector);
    factory.setHibernateSessionFactory(getSessionFactory());
    return factory.createObject();
  }

}
