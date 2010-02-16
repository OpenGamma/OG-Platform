package com.opengamma.util.test;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.junit.After;
import org.junit.Before;

public abstract class HibernateTest extends DBTest {
  
  private SessionFactory _sessionFactory;
  private static int testCount = 0;
  
  protected HibernateTest(String databaseType) {
    super(databaseType);
  }
  
  public SessionFactory getSessionFactory() {
    return _sessionFactory;
  }

  public void setSessionFactory(SessionFactory sessionFactory) {
    _sessionFactory = sessionFactory;
  }
  
  public abstract Class<?>[] getHibernateMappingClasses();

  @Before
  public void setUp() throws Exception {
    super.setUp();
    
    Configuration configuration = new Configuration();
    configuration.setProperty(Environment.DRIVER, getDbTool().getJDBCDriverClass().getName());
    configuration.setProperty(Environment.URL, getDbTool().getTestDatabaseURL());
    configuration.setProperty(Environment.USER, getDbTool().getUser());
    configuration.setProperty(Environment.PASS, getDbTool().getPassword());
    configuration.setProperty(Environment.DIALECT, getDbTool().getHibernateDialect().getClass().getName());
    configuration.setProperty(Environment.SHOW_SQL, "true");
    
    for (Class<?> clazz : getHibernateMappingClasses()) {
      configuration.addClass(clazz);
    }

    SessionFactory sessionFactory = configuration.buildSessionFactory();
    setSessionFactory(sessionFactory);

    testCount++;
  }
  
  @After
  public void tearDown() throws Exception {
    if (_sessionFactory != null) {
      _sessionFactory.close();
    }
    super.tearDown();
  }
}
