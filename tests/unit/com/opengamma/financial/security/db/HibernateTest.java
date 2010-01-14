package com.opengamma.financial.security.db;


import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.security.Security;
import com.opengamma.financial.Currency;
import com.opengamma.financial.security.EquitySecurity;
import com.opengamma.id.DomainSpecificIdentifier;

public class HibernateTest {
  private static final Logger s_logger = LoggerFactory.getLogger(HibernateTest.class);
  private static final String PROPS_FILE_NAME = "tests.properties";
  private ApplicationContext _context;
  protected HibernateSecurityMaster _secMaster;
  private static Properties _props; 
  private static int testCount = 0;
  
  private static void recursiveDelete(File file) {
    if (file.isDirectory()) {
      File[] list = file.listFiles();
      for (File entry : list) {
        if (entry.isDirectory()) {
          recursiveDelete(entry);
        }
        if (!entry.delete()) {
          s_logger.warn("Could not delete file:"+file.getAbsolutePath());
          //throw new OpenGammaRuntimeException("Could not delete file:"+entry.getAbsolutePath());
        } else {
          System.err.println("Deleted "+entry.getAbsolutePath());
        }
      }
    }
    if (!file.delete()) {
      s_logger.warn("Could not delete file:"+file.getAbsolutePath());   
    } else {
      System.err.println("Deleted "+file.getAbsolutePath());
    }
    
  }
  
  private String getDBUrl(boolean createFrom) {
    File blankDbDir = new File("blank");
    String core = "derby-db/"+this.getClass().getSimpleName()+"-test-"+testCount;
    if (createFrom) {
      return core + ";createFrom="+blankDbDir.getAbsolutePath();
    } else {
      return core;
    }
  }
  
  public static void setUpClass() throws Exception {
    recursiveDelete(new File("derby-db"));
    Properties props = new Properties();
    File file = new File(PROPS_FILE_NAME);
    System.err.println(file.getAbsoluteFile());
    props.load(new FileInputStream(file)); 
    _props = props;
    Class.forName((String) _props.get("jdbc.driver.classname")).newInstance(); // load driver.
  }
  
  public void setUp() throws Exception {
    String createFromUrl = _props.getProperty("jdbc.url") + getDBUrl(true);
    System.err.println("Connecting with data source URL "+createFromUrl);
    Connection conn = DriverManager.getConnection(createFromUrl);
    // this will create a copy of the blank database, using
    conn.close(); // that should do the copy...  we do it like this because I'm unsure if we can be sure the App Context will release the resources if we used that.
    System.err.println("closed connection, starting App Context");
    System.setProperty("jdbc.url", _props.getProperty("jdbc.url") + getDBUrl(false)); 
    System.setProperty("jdbc.driver.classname", _props.getProperty("jdbc.driver.classname"));
    // the idea is that this SYSTEM property (the others above are not system props) is picked up by the PropertyPlaceholderConfigurer during startup
    // and injected into the 
    _context = new ClassPathXmlApplicationContext("com/opengamma/financial/security/db/security-master-testing-context.xml");
    BasicDataSource dataSource = (BasicDataSource) _context.getBean("myDataSource");
    System.err.println(ToStringBuilder.reflectionToString(dataSource));
    _secMaster = (HibernateSecurityMaster) _context.getBean("myHibernateSecurityMaster");
    System.err.println("Sec Master initialization complete:" + _secMaster);
    testCount++;
  }
}
