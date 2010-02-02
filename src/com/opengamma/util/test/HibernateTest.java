package com.opengamma.util.test;

import java.util.ArrayList;
import java.util.Collection;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.opengamma.util.ArgumentChecker;

@RunWith(Parameterized.class)
public abstract class HibernateTest {
  
  private String _databaseType;
  private SessionFactory _sessionFactory;
  private static int testCount = 0;
  
  protected HibernateTest(String databaseType) {
    ArgumentChecker.checkNotNull(databaseType, "Database type");
    _databaseType = databaseType;                
  }
  
  @Parameters
  public static Collection<Object[]> getDatabaseTypes() {
    String databaseType = System.getProperty("test.database.type");
    if (databaseType == null) {
      databaseType = "derby"; // If you run from Eclipse, use Derby only
    }
    
    ArrayList<Object[]> returnValue = new ArrayList<Object[]>();
    for (String db : TestProperties.getDatabaseTypes(databaseType)) {
      returnValue.add(new Object[] { db });      
    }
    return returnValue;
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
    String dbHost = TestProperties.getDbHost(_databaseType);
    String user = TestProperties.getDbUsername(_databaseType);
    String password = TestProperties.getDbPassword(_databaseType);
    
    DBTool dbtool = new DBTool(dbHost, user, password);
    dbtool.clearTestTables();
    
    Configuration configuration = new Configuration();
    configuration.setProperty(Environment.DRIVER, dbtool.getJDBCDriverClass().getName());
    configuration.setProperty(Environment.URL, dbtool.getTestDatabaseURL());
    configuration.setProperty(Environment.USER, user);
    configuration.setProperty(Environment.PASS, password);
    configuration.setProperty(Environment.DIALECT, dbtool.getHibernateDialect().getClass().getName());
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
  }
}
