package com.opengamma.financial.security.db;


import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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
import com.opengamma.financial.security.AmericanVanillaEquityOptionSecurity;
import com.opengamma.financial.security.BondSecurity;
import com.opengamma.financial.security.CorporateBondSecurity;
import com.opengamma.financial.security.EquityOptionSecurity;
import com.opengamma.financial.security.EquitySecurity;
import com.opengamma.financial.security.EuropeanVanillaEquityOptionSecurity;
import com.opengamma.financial.security.GovernmentBondSecurity;
import com.opengamma.financial.security.MunicipalBondSecurity;
import com.opengamma.financial.security.OptionType;
import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.util.test.HibernateTest;
import com.opengamma.util.time.Expiry;

public class HibernateSecurityMasterTest extends HibernateTest {
  
  private HibernateSecurityMaster _secMaster;
  
  public HibernateSecurityMasterTest(String databaseType) {
    super(databaseType);
  }
  
  @Override
  public Class<?>[] getHibernateMappingClasses() {
    return new Class<?>[] { DomainSpecificIdentifierBean.class, ExchangeBean.class, CurrencyBean.class, SecurityBean.class, GICSCodeBean.class, FrequencyBean.class, DayCountBean.class, BusinessDayConventionBean.class };
  }

  @SuppressWarnings("unused")
  private static final Logger s_logger = LoggerFactory.getLogger(HibernateSecurityMasterTest.class);
  
  @Before
  public void setUp() throws Exception {
    super.setUp();
    _secMaster = new HibernateSecurityMaster();
    _secMaster.setSessionFactory(getSessionFactory());
    System.err.println("Sec Master initialization complete:" + _secMaster);
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
        
        EquitySecurityBean nomura = EquitySecurityBeanOperation.INSTANCE.createBean (secMasterSession, cal.getTime(), false, cal.getTime(), null, null, tpxBean,"Nomura", jpyBean, banksBean);
        Assert.assertNotNull(nomura);
        Assert.assertEquals(tpxBean, nomura.getExchange());
        Assert.assertEquals(jpyBean, nomura.getCurrency());
        Assert.assertEquals("Nomura", nomura.getCompanyName());
        Assert.assertEquals(banksBean, nomura.getGICSCode ());
        
        EquitySecurityBean generalMotors = EquitySecurityBeanOperation.INSTANCE.createBean (secMasterSession, cal.getTime(), false, cal.getTime(), null, null, djxBean,"General Motors", usdBean, carManuBean);
        Assert.assertNotNull(generalMotors);
        Assert.assertEquals(djxBean, generalMotors.getExchange());
        Assert.assertEquals(usdBean, generalMotors.getCurrency());
        Assert.assertEquals("General Motors", generalMotors.getCompanyName());
        Assert.assertEquals(carManuBean, generalMotors.getGICSCode ());
        
        List<EquitySecurityBean> allEquitySecurities = secMasterSession.getEquitySecurityBeans();
        Assert.assertNotNull(allEquitySecurities);
        Assert.assertEquals(2, allEquitySecurities.size());
        Assert.assertTrue(allEquitySecurities.contains(nomura));
        Assert.assertTrue(allEquitySecurities.contains(generalMotors));
        
        secMasterSession.getCreateOrUpdateDomainSpecificIdentifierAssociationBean(cal.getTime (), "BLOOMBERG", "1311 Equity", nomura);
        secMasterSession.getCreateOrUpdateDomainSpecificIdentifierAssociationBean(cal.getTime (), "BLOOMBERG", "GM Equity", generalMotors);
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
        secMasterSession.getCreateOrUpdateDomainSpecificIdentifierAssociationBean(now, "BLOOMBERG", "1311 JP Equity", nomura);
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
  public void testEquityOptionSecurityBeans() {
    Currency dollar = Currency.getInstance ("USD");
    Currency sterling = Currency.getInstance ("GBP");
    Expiry expiry = new Expiry (ZonedDateTime.fromInstant (OffsetDateTime.dateMidnight (2012, 10, 30, ZoneOffset.UTC), TimeZone.timeZone ("UTC")));
    DomainSpecificIdentifier americanIdentifier = new DomainSpecificIdentifier ("BLOOMBERG", "American equity option");
    DomainSpecificIdentifier europeanIdentifier = new DomainSpecificIdentifier ("BLOOMBERG", "European equity option");
    Date now = new Date ();
    // create an American equity option
    EquityOptionSecurity equityOption = new AmericanVanillaEquityOptionSecurity (OptionType.PUT, 1.23, expiry, "underlying american option id", dollar, "DJX");
    equityOption.setIdentifiers (Collections.singleton (americanIdentifier));
    _secMaster.persistSecurity (now, equityOption);
    // create a European equity option
    equityOption = new EuropeanVanillaEquityOptionSecurity (OptionType.CALL, 4.56, expiry, "underlying european option id", sterling, "UKX");
    equityOption.setIdentifiers (Collections.singleton (europeanIdentifier));
    _secMaster.persistSecurity (now, equityOption);
    // retrieve the American option
    Security security = _secMaster.getSecurity (now, americanIdentifier, true);
    Assert.assertNotNull (security);
    Assert.assertTrue (security instanceof AmericanVanillaEquityOptionSecurity);
    AmericanVanillaEquityOptionSecurity american = (AmericanVanillaEquityOptionSecurity)security;
    Assert.assertEquals (OptionType.PUT, american.getOptionType ());
    Assert.assertEquals (1.23, american.getStrike (), 0);
    Assert.assertEquals (expiry, american.getExpiry ());
    Assert.assertEquals ("underlying american option id", american.getUnderlyingIdentityKey ());
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
    Assert.assertEquals ("underlying european option id", european.getUnderlyingIdentityKey ());
    Assert.assertEquals (sterling, european.getCurrency ());
    Assert.assertEquals ("UKX", european.getExchange ());
  }
  
  @Test
  public void testBondSecurityBeans () {
    final Date now = new Date ();
    Expiry expiry = new Expiry (ZonedDateTime.fromInstant (OffsetDateTime.dateMidnight (2012, 10, 30, ZoneOffset.UTC), TimeZone.timeZone ("UTC")));
    Currency dollar = Currency.getInstance ("USD");
    Frequency annually = FrequencyFactory.INSTANCE.getFrequency ("annually");
    Frequency monthly = FrequencyFactory.INSTANCE.getFrequency ("monthly");
    Frequency quarterly = FrequencyFactory.INSTANCE.getFrequency ("quarterly");
    DayCount act360 = DayCountFactory.INSTANCE.getDayCount ("Actual/360");
    DayCount actact = DayCountFactory.INSTANCE.getDayCount ("Act/Act");
    BusinessDayConvention following = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention ("following");
    BusinessDayConvention modified = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention ("modified");
    BusinessDayConvention preceding = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention ("preceding");
    DomainSpecificIdentifier corporateId = new DomainSpecificIdentifier ("BLOOMBERG", "corporate bond");
    DomainSpecificIdentifier municipalId = new DomainSpecificIdentifier ("BLOOMBERG", "municipal bond");
    DomainSpecificIdentifier governmentId = new DomainSpecificIdentifier ("BLOOMBERG", "government bond");
    // create a Corporate bond
    BondSecurity bond = new CorporateBondSecurity (expiry, 1.23, annually, "country 1", "credit rating 1", dollar, "issuer 1", act360, following);
    bond.setIdentifiers (Collections.singleton (corporateId));
    _secMaster.persistSecurity (now, bond);
    // create a Municipal bond
    bond = new MunicipalBondSecurity (expiry, 4.56, monthly, "country 2", "credit rating 2", dollar, "issuer 2", actact, modified);
    bond.setIdentifiers (Collections.singleton (municipalId));
    _secMaster.persistSecurity (now, bond);
    // create a Government bond
    bond = new GovernmentBondSecurity (expiry, 7.89, quarterly, "country 3", "credit rating 3", dollar, "issuer 3", act360, preceding);
    bond.setIdentifiers (Collections.singleton (governmentId));
    _secMaster.persistSecurity (now, bond);
    // retrieve the Corporate bond
    Security security = _secMaster.getSecurity (now, corporateId, true);
    Assert.assertNotNull (security);
    Assert.assertTrue (security instanceof CorporateBondSecurity);
    CorporateBondSecurity corporate = (CorporateBondSecurity)security;
    Assert.assertEquals (expiry, corporate.getMaturity ());
    Assert.assertEquals (1.23, corporate.getCoupon (), 0);
    Assert.assertEquals (annually, corporate.getFrequency ());
    Assert.assertEquals ("country 1", corporate.getCountry ());
    Assert.assertEquals ("credit rating 1", corporate.getCreditRating ());
    Assert.assertEquals (dollar, corporate.getCurrency ());
    Assert.assertEquals ("issuer 1", corporate.getIssuer ());
    Assert.assertEquals (act360, corporate.getDayCountConvention ());
    Assert.assertEquals (following, corporate.getBusinessDayConvention ());
    // retrieve the Municipal bond
    security = _secMaster.getSecurity (now, municipalId, true);
    Assert.assertNotNull (security);
    Assert.assertTrue (security instanceof MunicipalBondSecurity);
    MunicipalBondSecurity municipal = (MunicipalBondSecurity)security;
    Assert.assertEquals (expiry, municipal.getMaturity ());
    Assert.assertEquals (4.56, municipal.getCoupon (), 0);
    Assert.assertEquals (monthly, municipal.getFrequency ());
    Assert.assertEquals ("country 2", municipal.getCountry ());
    Assert.assertEquals ("credit rating 2", municipal.getCreditRating ());
    Assert.assertEquals (dollar, municipal.getCurrency ());
    Assert.assertEquals ("issuer 2", municipal.getIssuer ());
    Assert.assertEquals (actact, municipal.getDayCountConvention ());
    Assert.assertEquals (modified, municipal.getBusinessDayConvention ());
    // retrieve the Government bond
    security = _secMaster.getSecurity (now, governmentId, true);
    Assert.assertNotNull (security);
    Assert.assertTrue (security instanceof GovernmentBondSecurity);
    GovernmentBondSecurity government = (GovernmentBondSecurity)security;
    Assert.assertEquals (expiry, government.getMaturity ());
    Assert.assertEquals (7.89, government.getCoupon (), 0);
    Assert.assertEquals (quarterly, government.getFrequency ());
    Assert.assertEquals ("country 3", government.getCountry ());
    Assert.assertEquals ("credit rating 3", government.getCreditRating ());
    Assert.assertEquals (dollar, government.getCurrency ());
    Assert.assertEquals ("issuer 3", government.getIssuer ());
    Assert.assertEquals (act360, government.getDayCountConvention ());
    Assert.assertEquals (preceding, government.getBusinessDayConvention ());
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
        EquitySecurityBean nomuraBean = EquitySecurityBeanOperation.INSTANCE.createBean (secMasterSession, cal2000.getTime(), false, cal2000.getTime(), null, null, tpxBean,"Nomura", jpyBean, banksBean);
        EquitySecurityBean notNomuraBean = EquitySecurityBeanOperation.INSTANCE.createBean (secMasterSession, cal2003.getTime(), false, cal2003.getTime(), null, null, tpxBean,"Not Nomura", jpyBean, banksBean);
        DomainSpecificIdentifierAssociationBean dsiab1 = secMasterSession.getCreateOrUpdateDomainSpecificIdentifierAssociationBean(cal2001.getTime (), "BLOOMBERG", "1311 Equity", nomuraBean);
        DomainSpecificIdentifierAssociationBean dsiab2 = secMasterSession.getCreateOrUpdateDomainSpecificIdentifierAssociationBean(cal2002.getTime (), "BLOOMBERG", "1311 Equity", nomuraBean);
        Assert.assertEquals (dsiab1.getId (), dsiab2.getId ());
        DomainSpecificIdentifierAssociationBean dsiab3 = secMasterSession.getCreateOrUpdateDomainSpecificIdentifierAssociationBean(cal2003.getTime (), "BLOOMBERG", "1311 Equity", notNomuraBean);
        if (dsiab1.getId () == dsiab3.getId ()) Assert.fail ("different association should have been created");
        Assert.assertNotNull (dsiab3.getValidStartDate ());
        Assert.assertNull (dsiab3.getValidEndDate ());
        DomainSpecificIdentifierAssociationBean dsiab4 = secMasterSession.getCreateOrUpdateDomainSpecificIdentifierAssociationBean(cal2004.getTime (), "BLOOMBERG", "1311 Equity", nomuraBean);
        if (dsiab1.getId () == dsiab4.getId ()) Assert.fail ("different association should have been created");
        if (dsiab3.getId () == dsiab4.getId ()) Assert.fail ("different association should have been created");
        Assert.assertNotNull (dsiab4.getValidStartDate ());
        Assert.assertNull (dsiab4.getValidEndDate ());
        
        dsiab2 = secMasterSession.getCreateOrUpdateDomainSpecificIdentifierAssociationBean(cal2002.getTime (), "BLOOMBERG", "1311 Equity", nomuraBean);
        dsiab3 = secMasterSession.getCreateOrUpdateDomainSpecificIdentifierAssociationBean(cal2003.getTime (), "BLOOMBERG", "1311 Equity", notNomuraBean);
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
    generalMotors.setCurrency(Currency.getInstance("USD"));
    generalMotors.setExchange("NYSE");
    generalMotors.setIdentityKey("GM US Equity");
    generalMotors.setTicker("GM US Equity");
    generalMotors.setGICSCode(GICSCode.getInstance (25102010));
    generalMotors.setIdentifiers(Collections.singleton(new DomainSpecificIdentifier("BLOOMBERG", "GM US Equity")));
    
    EquitySecurity nomura = new EquitySecurity();
    nomura.setCompanyName("Nomura");
    nomura.setCurrency(Currency.getInstance("JPY"));
    nomura.setExchange("TOPIX");
    nomura.setIdentityKey("1311 JP Equity");
    nomura.setTicker("1311 JP Equity");
    nomura.setGICSCode(GICSCode.getInstance (4010));
    nomura.setIdentifiers(Collections.singleton(new DomainSpecificIdentifier("BLOOMBERG", "1311 JP Equity")));
    
    _secMaster.persistSecurity(yesterYear2003, generalMotors);
    _secMaster.persistSecurity(yesterYear2003, nomura);
    
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
    generalMotors2.setGICSCode(GICSCode.getInstance (25102010));
    generalMotors2.setIdentifiers(Collections.singleton(new DomainSpecificIdentifier("BLOOMBERG", "GM US Equity")));
    _secMaster.persistSecurity(yesterYear2005, generalMotors2);
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
