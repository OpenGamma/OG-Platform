package com.opengamma.financial.security.db;


import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.time.calendar.LocalDate;
import javax.time.calendar.OffsetDateTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZoneOffset;
import javax.time.calendar.ZonedDateTime;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.opengamma.engine.security.Security;
import com.opengamma.financial.Currency;
import com.opengamma.financial.GICSCode;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.FrequencyFactory;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.financial.security.AgricultureFutureSecurity;
import com.opengamma.financial.security.BondFutureDeliverable;
import com.opengamma.financial.security.BondFutureSecurity;
import com.opengamma.financial.security.BondSecurity;
import com.opengamma.financial.security.EnergyFutureSecurity;
import com.opengamma.financial.security.EquitySecurity;
import com.opengamma.financial.security.FXFutureSecurity;
import com.opengamma.financial.security.FutureSecurity;
import com.opengamma.financial.security.GovernmentBondSecurity;
import com.opengamma.financial.security.IndexFutureSecurity;
import com.opengamma.financial.security.InterestRateFutureSecurity;
import com.opengamma.financial.security.MetalFutureSecurity;
import com.opengamma.financial.security.StockFutureSecurity;
import com.opengamma.financial.security.option.AmericanVanillaEquityOptionSecurity;
import com.opengamma.financial.security.option.AmericanVanillaFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.EuropeanVanillaEquityOptionSecurity;
import com.opengamma.financial.security.option.EuropeanVanillaFutureOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.FutureOptionSecurity;
import com.opengamma.financial.security.option.OTCOptionSecurity;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.financial.security.option.PoweredEquityOptionSecurity;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.util.test.HibernateTest;
import com.opengamma.util.time.Expiry;

public class HibernateSecurityMasterTest extends HibernateTest {

  private static final Logger s_logger = LoggerFactory.getLogger(HibernateSecurityMasterTest.class);
  
  private HibernateSecurityMaster _secMaster;
  
  public HibernateSecurityMasterTest(String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion);
  }
  
  @Override
  public Class<?>[] getHibernateMappingClasses() {
    return new Class<?>[] {
        BondFutureTypeBean.class,
        BondSecurityBean.class,
        BusinessDayConventionBean.class,
        CashRateTypeBean.class,
        CommodityFutureTypeBean.class,
        CouponTypeBean.class,
        CurrencyBean.class,
        DayCountBean.class,
        EquitySecurityBean.class,
        ExchangeBean.class,
        FrequencyBean.class,
        FutureBundleBean.class,
        FutureSecurityBean.class,
        GICSCodeBean.class,
        GuaranteeTypeBean.class,
        IdentifierAssociationBean.class,
        IssuerTypeBean.class,
        MarketBean.class,
        OptionSecurityBean.class,
        SecurityBean.class,
        UnitBean.class,
        YieldConventionBean.class
        };
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    _secMaster = new HibernateSecurityMaster();
    _secMaster.setSessionFactory(getSessionFactory());
    s_logger.debug ("SecMaster initialization complete {}", _secMaster);
  }
  
  @Test
  public void testNoOp () {
    // deliberate no-op so we can just see the effect of the @Before session
  }

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
        s_logger.debug ("ukxBean: {}", ukxBean);
        s_logger.debug ("exchangeBeans: {}", exchangeBeans);
        for (ExchangeBean bean : exchangeBeans) {
          s_logger.debug ("{} hashCode={}", bean, bean.hashCode ());
          s_logger.debug ("equals={}", bean.equals (ukxBean));
        }
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
  public void testGICSCodeBeans () {
    _secMaster.getHibernateTemplate ().execute (new HibernateCallback () {
      @Override
      public Object doInHibernate (final Session session) throws HibernateException, SQLException {
        final HibernateSecurityMasterSession secMasterSession = new HibernateSecurityMasterSession (session);
        Assert.assertNotNull (secMasterSession.getGICSCodeBeans ());
        Assert.assertEquals (0, secMasterSession.getGICSCodeBeans ().size ());
        GICSCodeBean energyBean = secMasterSession.getOrCreateGICSCodeBean ("10", "Energy");
        List<GICSCodeBean> gicsBeans = secMasterSession.getGICSCodeBeans ();
        Assert.assertEquals (1, gicsBeans.size ());
        GICSCodeBean gicsBean = gicsBeans.get (0);
        Assert.assertEquals ("10", gicsBean.getName ());
        Assert.assertEquals ("Energy", gicsBean.getDescription ());
        Assert.assertEquals (energyBean.getId (), gicsBean.getId ());
        Long energyId = gicsBean.getId ();
        GICSCodeBean materialBean = secMasterSession.getOrCreateGICSCodeBean ("15", "Materials"); 
        GICSCodeBean chemicalsBean = secMasterSession.getOrCreateGICSCodeBean ("151010", "Chemicals"); 
        GICSCodeBean commodityChemsBean = secMasterSession.getOrCreateGICSCodeBean ("15101010", "Commodity Chemicals"); 
        GICSCodeBean diversifiedChemsBean = secMasterSession.getOrCreateGICSCodeBean ("15101020", "Diversified Chemicals");
        gicsBeans = secMasterSession.getGICSCodeBeans ();
        Assert.assertEquals (5, gicsBeans.size ());
        Assert.assertTrue (gicsBeans.contains (materialBean));
        Assert.assertTrue (gicsBeans.contains (chemicalsBean));
        Assert.assertTrue (gicsBeans.contains (commodityChemsBean));
        Assert.assertTrue (gicsBeans.contains (diversifiedChemsBean));
        gicsBean = secMasterSession.getOrCreateGICSCodeBean ("10", "Energy");
        Assert.assertEquals (energyId, gicsBean.getId ());
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
        
        GICSCodeBean carManuBean = secMasterSession.getOrCreateGICSCodeBean ("25102010", "Automobile Manufacturers");
        Assert.assertNotNull (carManuBean);
        GICSCodeBean banksBean = secMasterSession.getOrCreateGICSCodeBean ("4010", "Banks");
        Assert.assertNotNull (banksBean);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2000);
        
        EquitySecurityBean nomura = EquitySecurityBeanOperation.INSTANCE.createBean (secMasterSession, cal.getTime(), false, cal.getTime(), null, null, "Nomura", tpxBean,"Nomura", jpyBean, banksBean);
        Assert.assertNotNull(nomura);
        Assert.assertEquals ("Nomura", nomura.getDisplayName ());
        Assert.assertEquals(tpxBean, nomura.getExchange());
        Assert.assertEquals(jpyBean, nomura.getCurrency());
        Assert.assertEquals("Nomura", nomura.getCompanyName());
        Assert.assertEquals(banksBean, nomura.getGICSCode ());
        
        EquitySecurityBean generalMotors = EquitySecurityBeanOperation.INSTANCE.createBean (secMasterSession, cal.getTime(), false, cal.getTime(), null, null, "GM", djxBean,"General Motors", usdBean, carManuBean);
        Assert.assertNotNull(generalMotors);
        Assert.assertEquals ("GM", generalMotors.getDisplayName ());
        Assert.assertEquals(djxBean, generalMotors.getExchange());
        Assert.assertEquals(usdBean, generalMotors.getCurrency());
        Assert.assertEquals("General Motors", generalMotors.getCompanyName());
        Assert.assertEquals(carManuBean, generalMotors.getGICSCode ());
        
        List<EquitySecurityBean> allEquitySecurities = secMasterSession.getEquitySecurityBeans();
        Assert.assertNotNull(allEquitySecurities);
        Assert.assertEquals(2, allEquitySecurities.size());
        Assert.assertTrue(allEquitySecurities.contains(nomura));
        Assert.assertTrue(allEquitySecurities.contains(generalMotors));
        
        secMasterSession.getCreateOrUpdateIdentifierAssociationBean(cal.getTime (), "BLOOMBERG", "1311 Equity", nomura);
        secMasterSession.getCreateOrUpdateIdentifierAssociationBean(cal.getTime (), "BLOOMBERG", "GM Equity", generalMotors);
        List<IdentifierAssociationBean> allAssociations = secMasterSession.getAllAssociations();
        System.err.println(allAssociations);
        
        EquitySecurityBean hsbc = EquitySecurityBeanOperation.INSTANCE.createBean (secMasterSession, cal.getTime(), false, cal.getTime(), null, null, "HSBC", djxBean,"HSBC", usdBean, null);
        Assert.assertNotNull(hsbc);
        Assert.assertEquals ("HSBC", hsbc.getDisplayName ());
        Assert.assertEquals(djxBean, hsbc.getExchange());
        Assert.assertEquals(usdBean, hsbc.getCurrency());
        Assert.assertEquals("HSBC", hsbc.getCompanyName());
        Assert.assertNull(hsbc.getGICSCode ());
        
        return null;
      }
    });
    Identifier nomuraDSID = new Identifier("BLOOMBERG", "1311 Equity");
    final Date now = new Date();
    Security hopefullyNomura = _secMaster.getSecurity(now, nomuraDSID, false);
    Assert.assertNotNull(hopefullyNomura);
    Assert.assertTrue(hopefullyNomura instanceof EquitySecurity);
    EquitySecurity hopefullyNomuraSecurity = (EquitySecurity)hopefullyNomura;
    Assert.assertEquals("TPX", hopefullyNomuraSecurity.getExchangeCode());
    Assert.assertEquals("Topix", hopefullyNomuraSecurity.getExchange());
    Assert.assertEquals("Nomura", hopefullyNomuraSecurity.getCompanyName());
    Assert.assertEquals(Currency.getInstance("JPY"), hopefullyNomuraSecurity.getCurrency());
    Collection<Identifier> identifiers = hopefullyNomuraSecurity.getIdentifiers();
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
        secMasterSession.getCreateOrUpdateIdentifierAssociationBean(now, "BLOOMBERG", "1311 JP Equity", nomura);
        List<IdentifierAssociationBean> allAssociations = secMasterSession.getAllAssociations();
        System.err.println(allAssociations);
        return null;
      }
    });
    Identifier nomuraDSID_2 = new Identifier("BLOOMBERG", "1311 JP Equity");
    hopefullyNomura = _secMaster.getSecurity(now, nomuraDSID_2, true);
    Assert.assertNotNull(hopefullyNomura);
    System.err.println(hopefullyNomura.getClass());
    Assert.assertTrue(hopefullyNomura instanceof EquitySecurity);
    hopefullyNomuraSecurity = (EquitySecurity)hopefullyNomura;
    Assert.assertEquals("TPX", hopefullyNomuraSecurity.getExchangeCode());
    Assert.assertEquals("Topix", hopefullyNomuraSecurity.getExchange());
    Assert.assertEquals("Nomura", hopefullyNomuraSecurity.getCompanyName());
    Assert.assertEquals(Currency.getInstance("JPY"), hopefullyNomuraSecurity.getCurrency());
    identifiers = hopefullyNomuraSecurity.getIdentifiers();
    Assert.assertNotNull(identifiers);
    Assert.assertEquals(2, identifiers.size());
    Assert.assertTrue(identifiers.contains(nomuraDSID));
    Assert.assertTrue(identifiers.contains(nomuraDSID_2));
  }
  
  @Test
  public void testEquityOptionSecurityBeans() {
    Currency dollar = Currency.getInstance ("USD");
    Currency sterling = Currency.getInstance ("GBP");
    Expiry expiry = new Expiry (ZonedDateTime.fromInstant (OffsetDateTime.midnight (2012, 10, 30, ZoneOffset.UTC), TimeZone.of("UTC")));

    Identifier americanIdentifier = new Identifier ("BLOOMBERG", "American equity option");
    Identifier americanUnderlyingIdentifier = new Identifier(Security.SECURITY_IDENTITY_KEY_DOMAIN, "underlying american option id");
    
    Identifier europeanIdentifier = new Identifier ("BLOOMBERG", "European equity option");
    Identifier europeanUnderlyingIdentifier = new Identifier(Security.SECURITY_IDENTITY_KEY_DOMAIN, "underlying european option id");
    
    Identifier poweredIdentifier = new Identifier ("BLOOMBERG", "Powered equity option");
    Identifier poweredUnderlyingIdentifier = new Identifier(Security.SECURITY_IDENTITY_KEY_DOMAIN, "underlying powered option id");
    
    Date now = new Date ();
    // create an American equity option
    EquityOptionSecurity equityOption = new AmericanVanillaEquityOptionSecurity (OptionType.PUT, 
        1.23, 
        expiry, 
        americanUnderlyingIdentifier, 
        dollar, 
        "DJX");
    equityOption.setIdentifiers (Collections.singleton (americanIdentifier));
    _secMaster.putSecurity (now, equityOption);
    // create a European equity option
    equityOption = new EuropeanVanillaEquityOptionSecurity (OptionType.CALL, 
        4.56, 
        expiry, 
        europeanUnderlyingIdentifier, 
        sterling, 
        "UKX");
    equityOption.setIdentifiers (Collections.singleton (europeanIdentifier));
    _secMaster.putSecurity (now, equityOption);
    // create a Powered equity option
    equityOption = new PoweredEquityOptionSecurity (OptionType.CALL, 
        4.56, 
        expiry,
        7.89,
        poweredUnderlyingIdentifier, 
        sterling, 
        "UKX");
    equityOption.setIdentifiers (Collections.singleton (poweredIdentifier));
    _secMaster.putSecurity (now, equityOption);
    // retrieve the American option
    Security security = _secMaster.getSecurity (now, americanIdentifier, true);
    Assert.assertNotNull (security);
    Assert.assertTrue (security instanceof AmericanVanillaEquityOptionSecurity);
    AmericanVanillaEquityOptionSecurity american = (AmericanVanillaEquityOptionSecurity)security;
    Assert.assertEquals (OptionType.PUT, american.getOptionType ());
    Assert.assertEquals (1.23, american.getStrike (), 0);
    Assert.assertEquals (expiry, american.getExpiry ());
    Assert.assertEquals (americanUnderlyingIdentifier, american.getUnderlyingIdentityKey());
    Assert.assertEquals (dollar, american.getCurrency ());
    Assert.assertEquals ("DJX", american.getExchange ());
    // retrieve the European option
    security = _secMaster.getSecurity (now, europeanIdentifier, true);
    Assert.assertNotNull (security);
    Assert.assertTrue (security instanceof EuropeanVanillaEquityOptionSecurity);
    EuropeanVanillaEquityOptionSecurity european = (EuropeanVanillaEquityOptionSecurity)security;
    Assert.assertEquals (OptionType.CALL, european.getOptionType ());
    Assert.assertEquals (4.56, european.getStrike (), 0);
    Assert.assertEquals (expiry, european.getExpiry ());
    Assert.assertEquals (europeanUnderlyingIdentifier, european.getUnderlyingIdentityKey ());
    Assert.assertEquals (sterling, european.getCurrency ());
    Assert.assertEquals ("UKX", european.getExchange ());
    // retrieve the Powered option
    security = _secMaster.getSecurity (now, poweredIdentifier, true);
    Assert.assertNotNull (security);
    Assert.assertEquals (PoweredEquityOptionSecurity.class, security.getClass ());
    PoweredEquityOptionSecurity powered = (PoweredEquityOptionSecurity)security;
    Assert.assertEquals (OptionType.CALL, powered.getOptionType ());
    Assert.assertEquals (4.56, powered.getStrike (), 0);
    Assert.assertEquals (expiry, powered.getExpiry ());
    Assert.assertEquals (7.89, powered.getPower (), 0);
    Assert.assertEquals (poweredUnderlyingIdentifier, powered.getUnderlyingIdentityKey ());
    Assert.assertEquals (sterling, powered.getCurrency ());
    Assert.assertEquals ("UKX", powered.getExchange ());
  }
  
  @Test
  public void testFutureOptionSecurityBeans () {
    Currency dollar = Currency.getInstance ("USD");
    Currency sterling = Currency.getInstance ("GBP");
    Expiry expiry = new Expiry (ZonedDateTime.fromInstant (OffsetDateTime.midnight (2012, 10, 30, ZoneOffset.UTC), TimeZone.of("UTC")));
    Identifier americanIdentifier = new Identifier ("BLOOMBERG", "American future option");
    Identifier americanUnderlyingIdentifier = new Identifier(Security.SECURITY_IDENTITY_KEY_DOMAIN, "underlying american future id");
    Identifier europeanIdentifier = new Identifier ("BLOOMBERG", "European future option");
    Identifier europeanUnderlyingIdentifier = new Identifier(Security.SECURITY_IDENTITY_KEY_DOMAIN, "underlying european future id");
    Date now = new Date ();
    // create an American future option
    FutureOptionSecurity futureOption = new AmericanVanillaFutureOptionSecurity (OptionType.PUT, 
        1.23, 
        expiry, 
        americanUnderlyingIdentifier, 
        dollar, 
        "DJX",
        true);
    futureOption.setIdentifiers (Collections.singleton (americanIdentifier));
    _secMaster.putSecurity (now, futureOption);
    // create a European future option
    futureOption = new EuropeanVanillaFutureOptionSecurity (OptionType.CALL, 
        4.56, 
        expiry, 
        europeanUnderlyingIdentifier, 
        sterling, 
        "UKX",
        false);
    futureOption.setIdentifiers (Collections.singleton (europeanIdentifier));
    _secMaster.putSecurity (now, futureOption);
    // retrieve the American option
    Security security = _secMaster.getSecurity (now, americanIdentifier, true);
    Assert.assertNotNull (security);
    Assert.assertTrue (security instanceof AmericanVanillaFutureOptionSecurity);
    AmericanVanillaFutureOptionSecurity american = (AmericanVanillaFutureOptionSecurity)security;
    Assert.assertEquals (OptionType.PUT, american.getOptionType ());
    Assert.assertEquals (1.23, american.getStrike (), 0);
    Assert.assertEquals (expiry, american.getExpiry ());
    Assert.assertEquals (americanUnderlyingIdentifier, american.getUnderlyingIdentityKey());
    Assert.assertEquals (dollar, american.getCurrency ());
    Assert.assertEquals ("DJX", american.getExchange ());
    Assert.assertEquals (true, american.isMargined ());
    // retrieve the European option
    security = _secMaster.getSecurity (now, europeanIdentifier, true);
    Assert.assertNotNull (security);
    Assert.assertTrue (security instanceof EuropeanVanillaFutureOptionSecurity);
    EuropeanVanillaFutureOptionSecurity european = (EuropeanVanillaFutureOptionSecurity)security;
    Assert.assertEquals (OptionType.CALL, european.getOptionType ());
    Assert.assertEquals (4.56, european.getStrike (), 0);
    Assert.assertEquals (expiry, european.getExpiry ());
    Assert.assertEquals (europeanUnderlyingIdentifier, european.getUnderlyingIdentityKey ());
    Assert.assertEquals (sterling, european.getCurrency ());
    Assert.assertEquals ("UKX", european.getExchange ());
    Assert.assertEquals (false, european.isMargined ());
  }
  
  @Test
  public void testOTCOptionSecurityBeans () {
    Date now = new Date ();
    Currency dollar = Currency.getInstance ("USD");
    Currency sterling = Currency.getInstance ("GBP");
    Currency euro = Currency.getInstance ("EUR");
    Expiry expiry = new Expiry (ZonedDateTime.fromInstant (OffsetDateTime.midnight (2012, 10, 30, ZoneOffset.UTC), TimeZone.of("UTC")));
    Identifier fxIdentifier = new Identifier ("BLOOMBERG", "fx option");
    Identifier fxUnderlyingIdentifier = new Identifier(Security.SECURITY_IDENTITY_KEY_DOMAIN, "underlying identity");
    OTCOptionSecurity security = new FXOptionSecurity (OptionType.PUT, 1.23, expiry, fxUnderlyingIdentifier, euro, "counterparty", dollar, sterling);
    security.setIdentifiers (Collections.singleton (fxIdentifier));
    _secMaster.putSecurity (now, security);
    Security sec = _secMaster.getSecurity (now, fxIdentifier, true);
    Assert.assertNotNull (sec);
    Assert.assertTrue (sec instanceof FXOptionSecurity);
    FXOptionSecurity fxOptionSecurity = (FXOptionSecurity)sec;
    Assert.assertEquals (OptionType.PUT, fxOptionSecurity.getOptionType ());
    Assert.assertEquals (1.23, fxOptionSecurity.getStrike (), 0);
    Assert.assertEquals (expiry, fxOptionSecurity.getExpiry ());
    Assert.assertEquals (fxUnderlyingIdentifier, fxOptionSecurity.getUnderlyingIdentityKey ());
    Assert.assertEquals (euro, fxOptionSecurity.getCurrency ());
    Assert.assertEquals (dollar, fxOptionSecurity.getPutCurrency ());
    Assert.assertEquals (sterling, fxOptionSecurity.getCallCurrency ());
  }
  
  @Test
  public void testGovernmentBondSecurityBean () {
    final Date now = new Date ();
    Expiry expiry = new Expiry (ZonedDateTime.fromInstant (OffsetDateTime.midnight(2012, 10, 30, ZoneOffset.UTC), TimeZone.of("UTC")));
    Currency dollar = Currency.getInstance ("USD");
    YieldConvention usStreet = YieldConventionFactory.INSTANCE.getYieldConvention ("US street");
    Frequency annual = FrequencyFactory.INSTANCE.getFrequency ("annual");
    DayCount act360 = DayCountFactory.INSTANCE.getDayCount ("Actual/360");
    BusinessDayConvention following = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention ("following");
    LocalDate announcementDate = LocalDate.of (2008, 1, 1);
    LocalDate interestAccrualDate = LocalDate.of (2010, 3, 4);
    LocalDate settlementDate = LocalDate.of (2012, 11, 1);
    LocalDate firstCouponDate = LocalDate.of (2009, 1, 1);
    Identifier governmentId = new Identifier ("BLOOMBERG", "government bond");
    BondSecurity bond = new GovernmentBondSecurity ("issuer name", "issuer type", "issuer domicile", "market", dollar, usStreet, "guarantee type", expiry, "coupon type", 0.5, annual, act360, following, announcementDate, interestAccrualDate, settlementDate, firstCouponDate, 10.0, 100d, 10d, 1d, 10d, 15d);
    bond.setIdentifiers (Collections.singleton (governmentId));
    _secMaster.putSecurity (now, bond);
    Security security = _secMaster.getSecurity (now, governmentId, true);
    Assert.assertNotNull (security);
    Assert.assertTrue (security instanceof GovernmentBondSecurity);
    GovernmentBondSecurity government = (GovernmentBondSecurity)security;
    Assert.assertEquals ("issuer name", government.getIssuerName ());
    Assert.assertEquals ("issuer type", government.getIssuerType ());
    Assert.assertEquals ("issuer domicile", government.getIssuerDomicile ());
    Assert.assertEquals ("market", government.getMarket ());
    Assert.assertEquals (dollar, government.getCurrency ());
    Assert.assertEquals (usStreet, government.getYieldConvention ());
    Assert.assertEquals ("guarantee type", government.getGuaranteeType ());
    Assert.assertEquals (expiry, government.getMaturity ());
    Assert.assertEquals ("coupon type", government.getCouponType ());
    Assert.assertEquals (0.5, government.getCouponRate (), 0);
    Assert.assertEquals (annual, government.getCouponFrequency ());
    Assert.assertEquals (act360, government.getDayCountConvention ());
    Assert.assertEquals (following, government.getBusinessDayConvention ());
    Assert.assertEquals (announcementDate, government.getAnnouncementDate ());
    Assert.assertEquals (interestAccrualDate, government.getInterestAccrualDate ());
    Assert.assertEquals (settlementDate, government.getSettlementDate ());
    Assert.assertEquals (firstCouponDate, government.getFirstCouponDate ());
    Assert.assertEquals (10.0, government.getIssuancePrice (), 0);
    Assert.assertEquals (100.0, government.getTotalAmountIssued (), 0);
    Assert.assertEquals (10.0, government.getMinimumAmount (), 0);
    Assert.assertEquals (1.0, government.getMinimumIncrement (), 0);
    Assert.assertEquals (10.0, government.getParAmount (), 0);
    Assert.assertEquals (15.0, government.getRedemptionValue (), 0);
  }
  
  @Test
  public void testSecurityDisplayName () {
    // create a security
    final Date now = new Date ();
    final Expiry expiry = new Expiry (ZonedDateTime.fromInstant (OffsetDateTime.midnight(2012, 10, 30, ZoneOffset.UTC), TimeZone.of("UTC")));
    final Currency dollar = Currency.getInstance ("USD");
    final Identifier underlying = new Identifier ("BLOOMBERG", "underlying identifier");
    final Identifier actual = new Identifier ("BLOOMBERG", "future identifier");
    IndexFutureSecurity security = new IndexFutureSecurity (expiry, "DJX", "DJX", dollar, underlying);
    // check getDisplayName is not null
    String displayName = security.getDisplayName ();
    Assert.assertNotNull (displayName);
    s_logger.info ("default displayName={}", displayName);
    // give it an identifier
    security.setIdentifiers (Collections.singleton (actual));
    // check getDisplayName is not null
    displayName = security.getDisplayName ();
    Assert.assertNotNull (displayName);
    s_logger.info ("identifier set displayName={}", displayName);
    // give it a specific name
    security.setDisplayName ("foo");
    // check getDisplayName is correct
    displayName = security.getDisplayName ();
    Assert.assertEquals ("foo", displayName);
    // persist it
    _secMaster.putSecurity (now, security);
    // get it back
    security = (IndexFutureSecurity)_secMaster.getSecurity (now, actual, true);
    Assert.assertNotNull (security);
    // check getDisplayName is correct
    Assert.assertEquals ("foo", displayName);
  }
  
  @Test
  public void testFutureSecurityBeans () {
    final Date now = new Date ();
    final Currency dollar = Currency.getInstance ("USD");
    final Currency yen = Currency.getInstance ("JPY");
    final Expiry expiry = new Expiry (ZonedDateTime.fromInstant (OffsetDateTime.midnight (2012, 10, 30, ZoneOffset.UTC), TimeZone.of ("UTC")));
    Identifier agricultureId = new Identifier ("BLOOMBERG", "agriculture");
    Identifier bondId = new Identifier ("BLOOMBERG", "bond");
    Identifier energyId = new Identifier ("BLOOMBERG", "energy");
    Identifier fxId = new Identifier ("BLOOMBERG", "fx");
    Identifier interestRateId = new Identifier ("BLOOMBERG", "interest rate");
    Identifier metalId = new Identifier ("BLOOMBERG", "metal");
    Identifier indexId = new Identifier ("BLOOMBERG", "index");
    Identifier stockId = new Identifier ("BLOOMBERG", "stock");
    Identifier underlyingId = new Identifier (Security.SECURITY_IDENTITY_KEY_DOMAIN, "underlying ID");
    FutureSecurity future;
    future = new AgricultureFutureSecurity (expiry, "TPX", "DJX", dollar, "Red wheat");
    future.setIdentifiers (Collections.singleton (agricultureId));
    _secMaster.putSecurity (now, future);
    Set<BondFutureDeliverable> bondDeliverables = new HashSet<BondFutureDeliverable> ();
    bondDeliverables.add (new BondFutureDeliverable (new IdentifierBundle (new Identifier ("BLOOMBERG", "corporate bond"), new Identifier ("BLOOMBERG", "municipal bond")), 1.5));
    bondDeliverables.add (new BondFutureDeliverable (new IdentifierBundle (new Identifier ("BLOOMBERG", "government bond")), 3));
    future = new BondFutureSecurity (expiry, "TPX", "DJX", dollar, "type", bondDeliverables);
    future.setIdentifiers (Collections.singleton (bondId));
    _secMaster.putSecurity (now, future);
    future = new EnergyFutureSecurity (expiry, "TPX", "DJX", dollar, "Oil", 1.0, "barrel", underlyingId);
    future.setIdentifiers (Collections.singleton (energyId));
    _secMaster.putSecurity (now, future);
    future = new FXFutureSecurity (expiry, "DJX", "DJX", dollar, dollar, yen, 10000.0);
    future.setIdentifiers (Collections.singleton (fxId));
    _secMaster.putSecurity (now, future);
    future = new InterestRateFutureSecurity (expiry, "TPX", "TPX", yen, "LIBOR");
    future.setIdentifiers (Collections.singleton (interestRateId));
    _secMaster.putSecurity (now, future);
    future = new MetalFutureSecurity (expiry, "DJX", "TPX", dollar, "gold", 100.0, "gram", underlyingId);
    future.setIdentifiers (Collections.singleton (metalId));
    _secMaster.putSecurity (now, future);
    future = new IndexFutureSecurity (expiry, "DJX", "TPX", dollar, underlyingId);
    future.setIdentifiers (Collections.singleton (indexId));
    _secMaster.putSecurity (now, future);
    future = new StockFutureSecurity (expiry, "DJX", "TPX", dollar, underlyingId);
    future.setIdentifiers (Collections.singleton (stockId));
    _secMaster.putSecurity (now, future);
    Security security;
    security = _secMaster.getSecurity (now, agricultureId, true);
    Assert.assertNotNull (security);
    Assert.assertTrue (security instanceof AgricultureFutureSecurity);
    AgricultureFutureSecurity agricultureSecurity = (AgricultureFutureSecurity)security;
    Assert.assertEquals ("TPX", agricultureSecurity.getTradingExchange ());
    Assert.assertEquals ("DJX", agricultureSecurity.getSettlementExchange ());
    Assert.assertEquals (dollar, agricultureSecurity.getCurrency ());
    Assert.assertEquals ("Red wheat", agricultureSecurity.getCommodityType ());
    Assert.assertEquals (null, agricultureSecurity.getUnitName ());
    Assert.assertEquals (null, agricultureSecurity.getUnitNumber ());
    security = _secMaster.getSecurity (now, bondId, true);
    Assert.assertNotNull (security);
    Assert.assertTrue (security instanceof BondFutureSecurity);
    BondFutureSecurity bondSecurity = (BondFutureSecurity)security;
    Assert.assertEquals ("TPX", bondSecurity.getTradingExchange ());
    Assert.assertEquals ("DJX", bondSecurity.getSettlementExchange ());
    Assert.assertEquals (dollar, bondSecurity.getCurrency ());
    Assert.assertEquals ("type", bondSecurity.getBondType ());
    Set<BondFutureDeliverable> deliverables = bondSecurity.getBasket ();
    Assert.assertNotNull (deliverables);
    Assert.assertEquals (bondDeliverables.size (), deliverables.size ());
    for (BondFutureDeliverable deliverable : bondDeliverables) {
      if (!deliverables.contains (deliverable)) {
        s_logger.info ("deliverable {} not found in {}", deliverable, deliverables);
        Assert.fail ("deliverable sets didn't match");
      }
    }
    security = _secMaster.getSecurity (now, energyId, true);
    Assert.assertNotNull (security);
    Assert.assertTrue (security instanceof EnergyFutureSecurity);
    EnergyFutureSecurity energySecurity = (EnergyFutureSecurity)security;
    Assert.assertEquals ("TPX", energySecurity.getTradingExchange ());
    Assert.assertEquals ("DJX", energySecurity.getSettlementExchange ());
    Assert.assertEquals (dollar, energySecurity.getCurrency ());
    Assert.assertEquals ("Oil", energySecurity.getCommodityType ());
    Assert.assertEquals (1.0, energySecurity.getUnitNumber (), 0);
    Assert.assertEquals ("barrel", energySecurity.getUnitName ());
    Assert.assertEquals (underlyingId, energySecurity.getUnderlyingIdentityKey ());
    security = _secMaster.getSecurity (now, fxId, true);
    Assert.assertNotNull (security);
    Assert.assertTrue (security instanceof FXFutureSecurity);
    FXFutureSecurity fxSecurity = (FXFutureSecurity)security;
    Assert.assertEquals ("DJX", fxSecurity.getTradingExchange ());
    Assert.assertEquals ("DJX", fxSecurity.getSettlementExchange ());
    Assert.assertEquals (dollar, fxSecurity.getCurrency ());
    Assert.assertEquals (dollar, fxSecurity.getNumerator ());
    Assert.assertEquals (yen, fxSecurity.getDenominator ());
    Assert.assertEquals (10000.0, fxSecurity.getMultiplicationFactor (), 0);
    security = _secMaster.getSecurity (now, interestRateId, true);
    Assert.assertNotNull (security);
    Assert.assertTrue (security instanceof InterestRateFutureSecurity);
    InterestRateFutureSecurity interestSecurity = (InterestRateFutureSecurity)security;
    Assert.assertEquals ("TPX", interestSecurity.getTradingExchange ());
    Assert.assertEquals ("TPX", interestSecurity.getSettlementExchange ());
    Assert.assertEquals (yen, interestSecurity.getCurrency ());
    Assert.assertEquals ("LIBOR", interestSecurity.getCashRateType ());
    security = _secMaster.getSecurity (now, metalId, true);
    Assert.assertNotNull (security);
    Assert.assertTrue (security instanceof MetalFutureSecurity);
    MetalFutureSecurity metalSecurity = (MetalFutureSecurity)security;
    Assert.assertEquals ("DJX", metalSecurity.getTradingExchange ());
    Assert.assertEquals ("TPX", metalSecurity.getSettlementExchange ());
    Assert.assertEquals (dollar, metalSecurity.getCurrency ());
    Assert.assertEquals ("gold", metalSecurity.getCommodityType ());
    Assert.assertEquals (100.0, metalSecurity.getUnitNumber (), 0);
    Assert.assertEquals ("gram", metalSecurity.getUnitName ());
    Assert.assertEquals (underlyingId, metalSecurity.getUnderlyingIdentityKey ());
    security = _secMaster.getSecurity (now, indexId, true);
    Assert.assertNotNull (security);
    Assert.assertTrue (security instanceof IndexFutureSecurity);
    IndexFutureSecurity indexSecurity = (IndexFutureSecurity)security;
    Assert.assertEquals ("DJX", indexSecurity.getTradingExchange ());
    Assert.assertEquals ("TPX", indexSecurity.getSettlementExchange ());
    Assert.assertEquals (dollar, indexSecurity.getCurrency ());
    Assert.assertEquals (underlyingId, indexSecurity.getUnderlyingIdentityKey ());
    security = _secMaster.getSecurity (now, stockId, true);
    Assert.assertNotNull (security);
    Assert.assertTrue (security instanceof StockFutureSecurity);
    StockFutureSecurity stockSecurity = (StockFutureSecurity)security;
    Assert.assertEquals ("DJX", stockSecurity.getTradingExchange ());
    Assert.assertEquals ("TPX", stockSecurity.getSettlementExchange ());
    Assert.assertEquals (dollar, stockSecurity.getCurrency ());
    Assert.assertEquals (underlyingId, stockSecurity.getUnderlyingIdentityKey ());
  }
        
  @Test
  public void testDomainSpecificIdentifierAssociationDateRanges () {
    _secMaster.getHibernateTemplate().execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {
        HibernateSecurityMasterSession secMasterSession = new HibernateSecurityMasterSession(session);
        ExchangeBean tpxBean = secMasterSession.getOrCreateExchangeBean("TPX", "Topix");
        CurrencyBean jpyBean = secMasterSession.getOrCreateCurrencyBean("JPY");
        GICSCodeBean banksBean = secMasterSession.getOrCreateGICSCodeBean ("4010", "Banks");
        Calendar cal2000 = Calendar.getInstance();
        cal2000.set(Calendar.YEAR, 2000);
        Calendar cal2001 = Calendar.getInstance();
        cal2001.set(Calendar.YEAR, 2001);
        Calendar cal2002 = Calendar.getInstance();
        cal2002.set(Calendar.YEAR, 2002);
        Calendar cal2003 = Calendar.getInstance();
        cal2003.set(Calendar.YEAR, 2003);
        Calendar cal2004 = Calendar.getInstance();
        cal2004.set(Calendar.YEAR, 2004);
        EquitySecurityBean nomuraBean = EquitySecurityBeanOperation.INSTANCE.createBean (secMasterSession, cal2000.getTime(), false, cal2000.getTime(), null, null, "Nomura", tpxBean,"Nomura", jpyBean, banksBean);
        EquitySecurityBean notNomuraBean = EquitySecurityBeanOperation.INSTANCE.createBean (secMasterSession, cal2003.getTime(), false, cal2003.getTime(), null, null, "Something else", tpxBean,"Not Nomura", jpyBean, banksBean);
        IdentifierAssociationBean dsiab1 = secMasterSession.getCreateOrUpdateIdentifierAssociationBean(cal2001.getTime (), "BLOOMBERG", "1311 Equity", nomuraBean);
        IdentifierAssociationBean dsiab2 = secMasterSession.getCreateOrUpdateIdentifierAssociationBean(cal2002.getTime (), "BLOOMBERG", "1311 Equity", nomuraBean);
        Assert.assertEquals (dsiab1.getId (), dsiab2.getId ());
        IdentifierAssociationBean dsiab3 = secMasterSession.getCreateOrUpdateIdentifierAssociationBean(cal2003.getTime (), "BLOOMBERG", "1311 Equity", notNomuraBean);
        if (dsiab1.getId () == dsiab3.getId ()) Assert.fail ("different association should have been created");
        Assert.assertNotNull (dsiab3.getValidStartDate ());
        Assert.assertNull (dsiab3.getValidEndDate ());
        IdentifierAssociationBean dsiab4 = secMasterSession.getCreateOrUpdateIdentifierAssociationBean(cal2004.getTime (), "BLOOMBERG", "1311 Equity", nomuraBean);
        if (dsiab1.getId () == dsiab4.getId ()) Assert.fail ("different association should have been created");
        if (dsiab3.getId () == dsiab4.getId ()) Assert.fail ("different association should have been created");
        Assert.assertNotNull (dsiab4.getValidStartDate ());
        Assert.assertNull (dsiab4.getValidEndDate ());
        
        dsiab2 = secMasterSession.getCreateOrUpdateIdentifierAssociationBean(cal2002.getTime (), "BLOOMBERG", "1311 Equity", nomuraBean);
        dsiab3 = secMasterSession.getCreateOrUpdateIdentifierAssociationBean(cal2003.getTime (), "BLOOMBERG", "1311 Equity", notNomuraBean);
        Assert.assertEquals (dsiab1.getId (), dsiab2.getId ());
        Assert.assertNull (dsiab2.getValidStartDate ());
        Assert.assertNotNull (dsiab2.getValidEndDate ());
        Assert.assertNotNull (dsiab3.getValidStartDate ());
        Assert.assertNotNull (dsiab3.getValidEndDate ());
        Assert.assertNotNull (dsiab4.getValidStartDate ());
        Assert.assertNull (dsiab4.getValidEndDate ());
        Assert.assertEquals (dsiab2.getValidEndDate (), dsiab3.getValidStartDate ());
        Assert.assertEquals (dsiab3.getValidEndDate (), dsiab4.getValidStartDate ());
        
        return null;
      }
    });
  }
        
  @Test
  public void testTopLevelFunctionality() {
    Calendar instance = Calendar.getInstance();
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
    generalMotors.setDisplayName("General Motors");
    generalMotors.setCurrency(Currency.getInstance("USD"));
    generalMotors.setExchangeCode("NYSE");
    generalMotors.setExchange("NEW YORK STOCK EXCHANGE");
    generalMotors.setIdentityKey("GM US Equity");
    generalMotors.setTicker("GM US Equity");
    generalMotors.setGICSCode(GICSCode.getInstance (25102010));
    generalMotors.setIdentifiers(Collections.singleton(new Identifier("BLOOMBERG", "GM US Equity")));
    
    EquitySecurity nomura = new EquitySecurity();
    nomura.setCompanyName("Nomura");
    nomura.setDisplayName("Nomura - 1311 JP Equity");
    nomura.setCurrency(Currency.getInstance("JPY"));
    nomura.setExchangeCode("TPX");
    nomura.setExchange("TOPIX");
    nomura.setIdentityKey("1311 JP Equity");
    nomura.setTicker("1311 JP Equity");
    nomura.setGICSCode(GICSCode.getInstance (4010));
    nomura.setIdentifiers(Collections.singleton(new Identifier("BLOOMBERG", "1311 JP Equity")));
    
    _secMaster.putSecurity(yesterYear2003, generalMotors);
    _secMaster.putSecurity(yesterYear2003, nomura);
    
    Security shouldBeNomura = _secMaster.getSecurity(new Identifier("BLOOMBERG", "1311 JP Equity"));
    Assert.assertEquals(nomura, shouldBeNomura);
    Security shouldBeGM = _secMaster.getSecurity(new Identifier("BLOOMBERG", "GM US Equity"));
    Assert.assertEquals(generalMotors, shouldBeGM);

    EquitySecurity generalMotors2 = new EquitySecurity();
    generalMotors2.setCompanyName("General Motors (Govt owned)");
    generalMotors2.setDisplayName("General Motors");
    generalMotors2.setCurrency(Currency.getInstance("USD"));
    generalMotors2.setExchangeCode("NYSE");
    generalMotors2.setExchange("NEW YORK STOCK EXCHANGE");
    generalMotors2.setIdentityKey("GM US Equity");
    generalMotors2.setTicker("GM US Equity");
    generalMotors2.setGICSCode(GICSCode.getInstance (25102010));
    generalMotors2.setIdentifiers(Collections.singleton(new Identifier("BLOOMBERG", "GM US Equity")));
    _secMaster.putSecurity(yesterYear2005, generalMotors2);
    _secMaster.getHibernateTemplate().execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {        
        HibernateSecurityMasterSession secMasterSession = new HibernateSecurityMasterSession(session);
        List<EquitySecurityBean> equitySecurityBeans = secMasterSession.getEquitySecurityBeans();
        s_logger.debug ("equitySecurityBeans: {}", equitySecurityBeans);
        List<IdentifierAssociationBean> allAssociations = secMasterSession.getAllAssociations();
        s_logger.debug ("allAssociations: {}", allAssociations);
        return null;
      }
    });
    shouldBeGM = _secMaster.getSecurity(yesterYear2004, new Identifier("BLOOMBERG", "GM US Equity"), true);
    Assert.assertEquals(generalMotors, shouldBeGM);
    shouldBeGM = _secMaster.getSecurity(yesterYear2006, new Identifier("BLOOMBERG", "GM US Equity"), true);
    Assert.assertEquals(generalMotors2, shouldBeGM);
  }

}
