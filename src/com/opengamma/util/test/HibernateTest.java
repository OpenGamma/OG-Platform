package com.opengamma.util.test;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

public abstract class HibernateTest {
  
  private static final String PROPS_FILE_NAME = "tests.properties";
  protected SessionFactory _sessionFactory;
  private static Properties _props;
  private static int testCount = 0;
  
  public SessionFactory getSessionFactory() {
    return _sessionFactory;
  }

  public void setSessionFactory(SessionFactory sessionFactory) {
    _sessionFactory = sessionFactory;
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
    Properties props = new Properties();
    File file = new File(PROPS_FILE_NAME);
    System.err.println(file.getAbsoluteFile());
    props.load(new FileInputStream(file));
    _props = props;
    
    String dbHost = _props.getProperty("jdbc.url");
    String user = _props.getProperty("jdbc.username");
    String password = _props.getProperty("jdbc.password");

    DBTool dbtool = new DBTool(dbHost, user, password);
    dbtool.dropTestSchema(); // make sure it's empty if it already existed
    dbtool.createTestSchema();
    dbtool.createTestTables();
  }

  public abstract Class<?>[] getHibernateMappingClasses();

  @Before
  public void setUp() throws Exception {
    String dbHost = _props.getProperty("jdbc.url");
    String user = _props.getProperty("jdbc.username");
    String password = _props.getProperty("jdbc.password");

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
