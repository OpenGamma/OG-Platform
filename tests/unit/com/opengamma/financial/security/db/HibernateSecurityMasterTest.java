package com.opengamma.financial.security.db;


import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.opengamma.engine.security.Security;
import com.opengamma.financial.Currency;
import com.opengamma.financial.security.EquitySecurity;
import com.opengamma.id.DomainSpecificIdentifier;

public class HibernateSecurityMasterTest extends HibernateTest {
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
  @BeforeClass
  public static void setUpClass() throws Exception {
    recursiveDelete(new File("derby-db"));
    Properties props = new Properties();
    File file = new File(PROPS_FILE_NAME);
    System.err.println(file.getAbsoluteFile());
    props.load(new FileInputStream(file)); 
    _props = props;
    Class.forName((String) _props.get("jdbc.driver.classname")).newInstance(); // load driver.
  }
  
  @Before
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
  @SuppressWarnings("unused")
  private static final Logger s_logger = LoggerFactory.getLogger(HibernateSecurityMasterTest.class);
  

  @Test
  public void testCurrencyBeans() {
    _secMaster.getHibernateTemplate().execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {
        HibernateSecurityMasterSession secMasterSession = new HibernateSecurityMasterSession(session);
        Assert.assertNotNull(secMasterSession.getCurrencyBeans());
        Assert.assertEquals(0, secMasterSession.getCurrencyBeans().size());
        CurrencyBean usdBean = secMasterSession.getOrCreateCurrencyBean("USD");
        List<CurrencyBean> currencyBeans = secMasterSession.getCurrencyBeans();
        Assert.assertEquals(1, currencyBeans.size());
        CurrencyBean currencyBean = currencyBeans.get(0);
        Assert.assertEquals("USD", currencyBean.getName());
        Assert.assertEquals(usdBean.getId(), currencyBean.getId());
        Long usdId = currencyBean.getId();
        CurrencyBean gbpBean = secMasterSession.getOrCreateCurrencyBean("GBP");
        CurrencyBean eurBean = secMasterSession.getOrCreateCurrencyBean("EUR");
        CurrencyBean jpyBean = secMasterSession.getOrCreateCurrencyBean("JPY");
        CurrencyBean nzdBean = secMasterSession.getOrCreateCurrencyBean("NZD");
        currencyBeans = secMasterSession.getCurrencyBeans();
        Assert.assertEquals(5, currencyBeans.size());
        Assert.assertTrue(currencyBeans.contains(usdBean));
        Assert.assertTrue(currencyBeans.contains(gbpBean));
        Assert.assertTrue(currencyBeans.contains(eurBean));
        Assert.assertTrue(currencyBeans.contains(jpyBean));
        Assert.assertTrue(currencyBeans.contains(nzdBean));
        CurrencyBean usdBean2 = secMasterSession.getOrCreateCurrencyBean("USD");
        Assert.assertEquals(usdId, usdBean2.getId());
        return null;
      }
    });

  }
  
  @Test
  public void testExchangeBeans() {
    _secMaster.getHibernateTemplate().execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {
        HibernateSecurityMasterSession secMasterSession = new HibernateSecurityMasterSession(session);
        Assert.assertNotNull(secMasterSession.getExchangeBeans());
        Assert.assertEquals(0, secMasterSession.getExchangeBeans().size());
        ExchangeBean ukxBean = secMasterSession.getOrCreateExchangeBean("UKX", "FTSE 100");
        List<ExchangeBean> exchangeBeans = secMasterSession.getExchangeBeans();
        Assert.assertEquals(1, exchangeBeans.size());
        ExchangeBean ExchangeBean = exchangeBeans.get(0);
        Assert.assertEquals("UKX", ExchangeBean.getName());
        Assert.assertEquals("FTSE 100", ExchangeBean.getDescription());
        Assert.assertEquals(ukxBean.getId(), ExchangeBean.getId());
        Long ukxId = ExchangeBean.getId();
        ExchangeBean tpxBean = secMasterSession.getOrCreateExchangeBean("TPX", "Topix");
        ExchangeBean spxBean = secMasterSession.getOrCreateExchangeBean("SPX", "S&P 500");
        ExchangeBean djxBean = secMasterSession.getOrCreateExchangeBean("DJX", "Dow Jones");
        ExchangeBean ruyBean = secMasterSession.getOrCreateExchangeBean("RUY", "Russell 2000");
        exchangeBeans = secMasterSession.getExchangeBeans();
        Assert.assertEquals(5, exchangeBeans.size());
        System.err.println(ukxBean);
        System.err.println(exchangeBeans);
        for (ExchangeBean bean : exchangeBeans) { System.err.println(bean +" hashCode:"+bean.hashCode()); System.err.println(bean.equals(ukxBean)); }
        Assert.assertTrue(exchangeBeans.contains(ukxBean));
        Assert.assertTrue(exchangeBeans.contains(tpxBean));
        Assert.assertTrue(exchangeBeans.contains(spxBean));
        Assert.assertTrue(exchangeBeans.contains(djxBean));
        Assert.assertTrue(exchangeBeans.contains(ruyBean));
        ExchangeBean usdBean2 = secMasterSession.getOrCreateExchangeBean("UKX", "FTSE 100");
        Assert.assertEquals(ukxId, usdBean2.getId());
        // can't do this test because exception thrown to different thread now we're using a call-back.
        //try {
        //  secMasterSession.getOrCreateExchangeBean("UKX", "FSTE 250");
        //  Assert.fail("Getting non-unique shouldn't work.");
        //} catch (Exception e) {
        //  // okay
        //}
        return null;
      }
    });
  }
  
  @Test
  public void testEquitySecurityBeans() {
    _secMaster.getHibernateTemplate().execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {
        HibernateSecurityMasterSession secMasterSession = new HibernateSecurityMasterSession(session);
        ExchangeBean tpxBean = secMasterSession.getOrCreateExchangeBean("TPX", "Topix");
        Assert.assertNotNull(tpxBean);
        ExchangeBean djxBean = secMasterSession.getOrCreateExchangeBean("DJX", "Dow Jones");
        Assert.assertNotNull(djxBean);
        CurrencyBean usdBean = secMasterSession.getOrCreateCurrencyBean("USD");
        Assert.assertNotNull(usdBean);
        CurrencyBean jpyBean = secMasterSession.getOrCreateCurrencyBean("JPY");
        Assert.assertNotNull(jpyBean);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2000);
        
        EquitySecurityBean nomura = secMasterSession.createEquitySecurityBean(cal.getTime(), false, cal.getTime(), null, null, tpxBean,"Nomura", jpyBean);
        Assert.assertNotNull(nomura);
        Assert.assertEquals(tpxBean, nomura.getExchange());
        Assert.assertEquals(jpyBean, nomura.getCurrency());
        Assert.assertEquals("Nomura", nomura.getCompanyName());
        
        EquitySecurityBean generalMotors = secMasterSession.createEquitySecurityBean(cal.getTime(), false, cal.getTime(), null, null, djxBean,"General Motors", usdBean);
        Assert.assertNotNull(generalMotors);
        Assert.assertEquals(djxBean, generalMotors.getExchange());
        Assert.assertEquals(usdBean, generalMotors.getCurrency());
        Assert.assertEquals("General Motors", generalMotors.getCompanyName());
        
        List<EquitySecurityBean> allEquitySecurities = secMasterSession.getEquitySecurityBeans();
        Assert.assertNotNull(allEquitySecurities);
        Assert.assertEquals(2, allEquitySecurities.size());
        Assert.assertTrue(allEquitySecurities.contains(nomura));
        Assert.assertTrue(allEquitySecurities.contains(generalMotors));
        
        secMasterSession.getOrCreateDomainSpecificIdentifierAssociationBean("BLOOMBERG", "1311 Equity", nomura);
        secMasterSession.getOrCreateDomainSpecificIdentifierAssociationBean("BLOOMBERG", "GM Equity", generalMotors);
        List<DomainSpecificIdentifierAssociationBean> allAssociations = secMasterSession.getAllAssociations();
        System.err.println(allAssociations);
        return null;
      }
    });
    DomainSpecificIdentifier nomuraDSID = new DomainSpecificIdentifier("BLOOMBERG", "1311 Equity");
    final Date now = new Date();
    Security hopefullyNomura = _secMaster.getSecurity(now, nomuraDSID, false);
    Assert.assertNotNull(hopefullyNomura);
    Assert.assertTrue(hopefullyNomura instanceof EquitySecurity);
    EquitySecurity hopefullyNomuraSecurity = (EquitySecurity)hopefullyNomura;
    Assert.assertEquals("TPX", hopefullyNomuraSecurity.getExchange());
    Assert.assertEquals("Nomura", hopefullyNomuraSecurity.getCompanyName());
    Assert.assertEquals(Currency.getInstance("JPY"), hopefullyNomuraSecurity.getCurrency());
    Collection<DomainSpecificIdentifier> identifiers = hopefullyNomuraSecurity.getIdentifiers();
    Assert.assertNotNull(identifiers);
    Assert.assertEquals(1, identifiers.size());
    Assert.assertTrue(identifiers.contains(nomuraDSID));
    _secMaster.getHibernateTemplate().execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {        
        HibernateSecurityMasterSession secMasterSession = new HibernateSecurityMasterSession(session);
        ExchangeBean tpxBean = secMasterSession.getOrCreateExchangeBean("TPX", "Topix");
        CurrencyBean jpyBean = secMasterSession.getOrCreateCurrencyBean("JPY");
        List<EquitySecurityBean> allEquities = secMasterSession.getEquitySecurityBeans();
        System.err.println(allEquities);
        EquitySecurityBean nomura = secMasterSession.getCurrentEquitySecurityBean(now, tpxBean, "Nomura", jpyBean);
        Assert.assertNotNull(nomura);
        secMasterSession.getOrCreateDomainSpecificIdentifierAssociationBean("BLOOMBERG", "1311 JP Equity", nomura);
        List<DomainSpecificIdentifierAssociationBean> allAssociations = secMasterSession.getAllAssociations();
        System.err.println(allAssociations);
        return null;
      }
    });
    DomainSpecificIdentifier nomuraDSID_2 = new DomainSpecificIdentifier("BLOOMBERG", "1311 JP Equity");
    hopefullyNomura = _secMaster.getSecurity(now, nomuraDSID_2, true);
    Assert.assertNotNull(hopefullyNomura);
    System.err.println(hopefullyNomura.getClass());
    Assert.assertTrue(hopefullyNomura instanceof EquitySecurity);
    hopefullyNomuraSecurity = (EquitySecurity)hopefullyNomura;
    Assert.assertEquals("TPX", hopefullyNomuraSecurity.getExchange());
    Assert.assertEquals("Nomura", hopefullyNomuraSecurity.getCompanyName());
    Assert.assertEquals(Currency.getInstance("JPY"), hopefullyNomuraSecurity.getCurrency());
    identifiers = hopefullyNomuraSecurity.getIdentifiers();
    Assert.assertNotNull(identifiers);
    Assert.assertEquals(2, identifiers.size());
    Assert.assertTrue(identifiers.contains(nomuraDSID));
    Assert.assertTrue(identifiers.contains(nomuraDSID_2));
  }
  
  @Test
  public void testTopLevelFunctionality() {
    Calendar instance = Calendar.getInstance();
    Date now = instance.getTime();
    instance.set(Calendar.YEAR, 2003);
    Date yesterYear2003 = instance.getTime();
    instance.set(Calendar.YEAR, 2004);    
    Date yesterYear2004 = instance.getTime();
    instance.set(Calendar.YEAR, 2005);
    Date yesterYear2005 = instance.getTime();
    instance.set(Calendar.YEAR, 2006);
    Date yesterYear2006 = instance.getTime();
    
    EquitySecurity generalMotors = new EquitySecurity();
    generalMotors.setCompanyName("General Motors");
    generalMotors.setCurrency(Currency.getInstance("USD"));
    generalMotors.setExchange("NYSE");
    generalMotors.setIdentityKey("GM US Equity");
    generalMotors.setTicker("GM US Equity");
    generalMotors.setIdentifiers(Collections.singleton(new DomainSpecificIdentifier("BLOOMBERG", "GM US Equity")));
    
    EquitySecurity nomura = new EquitySecurity();
    nomura.setCompanyName("Nomura");
    nomura.setCurrency(Currency.getInstance("JPY"));
    nomura.setExchange("TOPIX");
    nomura.setIdentityKey("1311 JP Equity");
    nomura.setTicker("1311 JP Equity");
    nomura.setIdentifiers(Collections.singleton(new DomainSpecificIdentifier("BLOOMBERG", "1311 JP Equity")));
    
    _secMaster.persistEquitySecurity(yesterYear2003, generalMotors);
    _secMaster.persistEquitySecurity(yesterYear2003, nomura);
    
    Security shouldBeNomura = _secMaster.getSecurity("1311 JP Equity");
    Assert.assertEquals(nomura, shouldBeNomura);
    Security shouldBeGM = _secMaster.getSecurity("GM US Equity");
    Assert.assertEquals(generalMotors, shouldBeGM);

    EquitySecurity generalMotors2 = new EquitySecurity();
    generalMotors2.setCompanyName("General Motors (Govt owned)");
    generalMotors2.setCurrency(Currency.getInstance("USD"));
    generalMotors2.setExchange("NYSE");
    generalMotors2.setIdentityKey("GM US Equity");
    generalMotors2.setTicker("GM US Equity");
    generalMotors2.setIdentifiers(Collections.singleton(new DomainSpecificIdentifier("BLOOMBERG", "GM US Equity")));
    _secMaster.persistEquitySecurity(yesterYear2005, generalMotors2);
    _secMaster.getHibernateTemplate().execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {        
        HibernateSecurityMasterSession secMasterSession = new HibernateSecurityMasterSession(session);
        List<EquitySecurityBean> equitySecurityBeans = secMasterSession.getEquitySecurityBeans();
        System.err.println(equitySecurityBeans);
        List<DomainSpecificIdentifierAssociationBean> allAssociations = secMasterSession.getAllAssociations();
        System.err.println(allAssociations);
        return null;
      }
    });
    shouldBeGM = _secMaster.getSecurity(yesterYear2004, new DomainSpecificIdentifier("BLOOMBERG", "GM US Equity"), true);
    Assert.assertEquals(generalMotors, shouldBeGM);
    shouldBeGM = _secMaster.getSecurity(yesterYear2006, new DomainSpecificIdentifier("BLOOMBERG", "GM US Equity"), true);
    Assert.assertEquals(generalMotors2, shouldBeGM);
  }

}
