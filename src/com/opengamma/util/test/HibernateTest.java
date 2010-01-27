package com.opengamma.util.test;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class HibernateTest {
  private static final Logger s_logger = LoggerFactory
      .getLogger(HibernateTest.class);
  private static final String PROPS_FILE_NAME = "tests.properties";
  protected SessionFactory _sessionFactory;
  private static Properties _props;
  private static boolean _isDerby;
  private static int testCount = 0;
  
  public SessionFactory getSessionFactory() {
    return _sessionFactory;
  }

  public void setSessionFactory(SessionFactory sessionFactory) {
    _sessionFactory = sessionFactory;
  }

  private static void recursiveDelete(File file) {
    if (file.isDirectory()) {
      File[] list = file.listFiles();
      for (File entry : list) {
        if (entry.isDirectory()) {
          recursiveDelete(entry);
        }
        if (!entry.delete()) {
          s_logger.warn("Could not delete file:" + file.getAbsolutePath());
          // throw new
          // OpenGammaRuntimeException("Could not delete file:"+entry.getAbsolutePath());
        } else {
          System.err.println("Deleted " + entry.getAbsolutePath());
        }
      }
    }
    if (!file.delete()) {
      s_logger.warn("Could not delete file:" + file.getAbsolutePath());
    } else {
      System.err.println("Deleted " + file.getAbsolutePath());
    }

  }

  private String getDBUrl(boolean createFrom) {
    if (_isDerby) {
      File blankDbDir = new File("blank");
      String core = "/" + this.getClass().getSimpleName() + "-test-"
          + testCount;
      if (createFrom) {
        return core + ";createFrom=" + blankDbDir.getAbsolutePath();
      } else {
        return core;
      }

    } else {
      return "/OpenGammaTests";
    }
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
    Properties props = new Properties();
    File file = new File(PROPS_FILE_NAME);
    System.err.println(file.getAbsoluteFile());
    props.load(new FileInputStream(file));
    _props = props;

    String jdbcDriverClass = ((String) _props.get("jdbc.driver.classname")).trim();
    _isDerby = jdbcDriverClass.equals("org.apache.derby.jdbc.EmbeddedDriver");

    if (_isDerby) {
      recursiveDelete(new File("derby-db"));
    }

    Class.forName(jdbcDriverClass).newInstance(); // load driver.
  }

  public abstract Class<?>[] getHibernateMappingClasses();

  @Before
  public void setUp() throws Exception {
    String createFromUrl = _props.getProperty("jdbc.url") + getDBUrl(true);
    System.err.println("Connecting with data source URL " + createFromUrl);

    String user = _props.getProperty("jdbc.username");
    String password = _props.getProperty("jdbc.password");

    if (_isDerby) {
      Connection conn = DriverManager.getConnection(createFromUrl, user,
          password);
      // this will create a copy of the blank database, using
      conn.close(); // that should do the copy... we do it like this because I'm
                    // unsure if we can be sure the App Context will release the
                    // resources if we used that.
      System.err.println("closed connection, starting App Context");
    }

    Configuration configuration = new Configuration();
    configuration.setProperty(Environment.DRIVER, _props.getProperty("jdbc.driver.classname"));
    configuration.setProperty(Environment.URL, _props.getProperty("jdbc.url") + getDBUrl(false));
    configuration.setProperty(Environment.USER, _props.getProperty("jdbc.username"));
    configuration.setProperty(Environment.DIALECT, _props.getProperty("hibernate.dialect"));
    configuration.setProperty(Environment.SHOW_SQL, "true");
    configuration.setProperty(Environment.HBM2DDL_AUTO, "create-drop");
    
    for (Class<?> clazz : getHibernateMappingClasses()) {
      configuration.addClass(clazz);
    }

    SessionFactory sessionFactory = configuration.buildSessionFactory();
    setSessionFactory(sessionFactory);

    testCount++;
  }
}
