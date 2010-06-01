/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.Calendar;
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
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.test.HibernateTest;
import com.opengamma.util.time.Expiry;

public class HibernateSecurityMasterTest extends HibernateTest {

  private static final Logger s_logger = LoggerFactory.getLogger(HibernateSecurityMasterTest.class);

  private HibernateSecurityMaster _secMaster;

  public HibernateSecurityMasterTest(final String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion);
  }

  @Override
  public Class<?>[] getHibernateMappingClasses() {
    return new Class<?>[] { BondFutureTypeBean.class, BondSecurityBean.class, BusinessDayConventionBean.class, CashRateTypeBean.class, CommodityFutureTypeBean.class,
        CouponTypeBean.class, CurrencyBean.class, DayCountBean.class, EquitySecurityBean.class, ExchangeBean.class, FrequencyBean.class, FutureBundleBean.class,
        FutureSecurityBean.class, GICSCodeBean.class, GuaranteeTypeBean.class, IdentifierAssociationBean.class, IssuerTypeBean.class, MarketBean.class, OptionSecurityBean.class,
        SecurityBean.class, UnitBean.class, YieldConventionBean.class };
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    _secMaster = new HibernateSecurityMaster();
    _secMaster.setSessionFactory(getSessionFactory());
    s_logger.debug("SecMaster initialization complete {}", _secMaster);
  }

  @Test
  public void testNoOp() {
    // deliberate no-op so we can just see the effect of the @Before session
  }

  @Test
  public void testCurrencyBeans() {
    _secMaster.getHibernateTemplate().execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(final Session session) throws HibernateException, SQLException {
        final HibernateSecurityMasterSession secMasterSession = new HibernateSecurityMasterSession(session);
        assertNotNull(secMasterSession.getCurrencyBeans());
        assertEquals(0, secMasterSession.getCurrencyBeans().size());
        final CurrencyBean usdBean = secMasterSession.getOrCreateCurrencyBean("USD");
        List<CurrencyBean> currencyBeans = secMasterSession.getCurrencyBeans();
        assertEquals(1, currencyBeans.size());
        final CurrencyBean currencyBean = currencyBeans.get(0);
        assertEquals("USD", currencyBean.getName());
        assertEquals(usdBean.getId(), currencyBean.getId());
        final Long usdId = currencyBean.getId();
        final CurrencyBean gbpBean = secMasterSession.getOrCreateCurrencyBean("GBP");
        final CurrencyBean eurBean = secMasterSession.getOrCreateCurrencyBean("EUR");
        final CurrencyBean jpyBean = secMasterSession.getOrCreateCurrencyBean("JPY");
        final CurrencyBean nzdBean = secMasterSession.getOrCreateCurrencyBean("NZD");
        currencyBeans = secMasterSession.getCurrencyBeans();
        assertEquals(5, currencyBeans.size());
        assertTrue(currencyBeans.contains(usdBean));
        assertTrue(currencyBeans.contains(gbpBean));
        assertTrue(currencyBeans.contains(eurBean));
        assertTrue(currencyBeans.contains(jpyBean));
        assertTrue(currencyBeans.contains(nzdBean));
        final CurrencyBean usdBean2 = secMasterSession.getOrCreateCurrencyBean("USD");
        assertEquals(usdId, usdBean2.getId());
        return null;
      }
    });

  }

  @Test
  public void testExchangeBeans() {
    _secMaster.getHibernateTemplate().execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(final Session session) throws HibernateException, SQLException {
        final HibernateSecurityMasterSession secMasterSession = new HibernateSecurityMasterSession(session);
        assertNotNull(secMasterSession.getExchangeBeans());
        assertEquals(0, secMasterSession.getExchangeBeans().size());
        final ExchangeBean ukxBean = secMasterSession.getOrCreateExchangeBean("UKX", "FTSE 100");
        List<ExchangeBean> exchangeBeans = secMasterSession.getExchangeBeans();
        assertEquals(1, exchangeBeans.size());
        final ExchangeBean ExchangeBean = exchangeBeans.get(0);
        assertEquals("UKX", ExchangeBean.getName());
        assertEquals("FTSE 100", ExchangeBean.getDescription());
        assertEquals(ukxBean.getId(), ExchangeBean.getId());
        final Long ukxId = ExchangeBean.getId();
        final ExchangeBean tpxBean = secMasterSession.getOrCreateExchangeBean("TPX", "Topix");
        final ExchangeBean spxBean = secMasterSession.getOrCreateExchangeBean("SPX", "S&P 500");
        final ExchangeBean djxBean = secMasterSession.getOrCreateExchangeBean("DJX", "Dow Jones");
        final ExchangeBean ruyBean = secMasterSession.getOrCreateExchangeBean("RUY", "Russell 2000");
        exchangeBeans = secMasterSession.getExchangeBeans();
        assertEquals(5, exchangeBeans.size());
        s_logger.debug("ukxBean: {}", ukxBean);
        s_logger.debug("exchangeBeans: {}", exchangeBeans);
        for (final ExchangeBean bean : exchangeBeans) {
          s_logger.debug("{} hashCode={}", bean, bean.hashCode());
          s_logger.debug("equals={}", bean.equals(ukxBean));
        }
        assertTrue(exchangeBeans.contains(ukxBean));
        assertTrue(exchangeBeans.contains(tpxBean));
        assertTrue(exchangeBeans.contains(spxBean));
        assertTrue(exchangeBeans.contains(djxBean));
        assertTrue(exchangeBeans.contains(ruyBean));
        final ExchangeBean usdBean2 = secMasterSession.getOrCreateExchangeBean("UKX", "FTSE 100");
        assertEquals(ukxId, usdBean2.getId());
        // can't do this test because exception thrown to different thread now we're using a call-back.
        //try {
        //  secMasterSession.getOrCreateExchangeBean("UKX", "FSTE 250");
        //  fail("Getting non-unique shouldn't work.");
        //} catch (Exception e) {
        //  // okay
        //}
        return null;
      }
    });
  }

  @Test
  public void testGICSCodeBeans() {
    _secMaster.getHibernateTemplate().execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(final Session session) throws HibernateException, SQLException {
        final HibernateSecurityMasterSession secMasterSession = new HibernateSecurityMasterSession(session);
        assertNotNull(secMasterSession.getGICSCodeBeans());
        assertEquals(0, secMasterSession.getGICSCodeBeans().size());
        final GICSCodeBean energyBean = secMasterSession.getOrCreateGICSCodeBean("10", "Energy");
        List<GICSCodeBean> gicsBeans = secMasterSession.getGICSCodeBeans();
        assertEquals(1, gicsBeans.size());
        GICSCodeBean gicsBean = gicsBeans.get(0);
        assertEquals("10", gicsBean.getName());
        assertEquals("Energy", gicsBean.getDescription());
        assertEquals(energyBean.getId(), gicsBean.getId());
        final Long energyId = gicsBean.getId();
        final GICSCodeBean materialBean = secMasterSession.getOrCreateGICSCodeBean("15", "Materials");
        final GICSCodeBean chemicalsBean = secMasterSession.getOrCreateGICSCodeBean("151010", "Chemicals");
        final GICSCodeBean commodityChemsBean = secMasterSession.getOrCreateGICSCodeBean("15101010", "Commodity Chemicals");
        final GICSCodeBean diversifiedChemsBean = secMasterSession.getOrCreateGICSCodeBean("15101020", "Diversified Chemicals");
        gicsBeans = secMasterSession.getGICSCodeBeans();
        assertEquals(5, gicsBeans.size());
        assertTrue(gicsBeans.contains(materialBean));
        assertTrue(gicsBeans.contains(chemicalsBean));
        assertTrue(gicsBeans.contains(commodityChemsBean));
        assertTrue(gicsBeans.contains(diversifiedChemsBean));
        gicsBean = secMasterSession.getOrCreateGICSCodeBean("10", "Energy");
        assertEquals(energyId, gicsBean.getId());
        return null;
      }
    });
  }

  @Test
  public void testEquitySecurityBeans() {
    _secMaster.getHibernateTemplate().execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(final Session session) throws HibernateException, SQLException {
        final HibernateSecurityMasterSession secMasterSession = new HibernateSecurityMasterSession(session);
        final ExchangeBean tpxBean = secMasterSession.getOrCreateExchangeBean("TPX", "Topix");
        assertNotNull(tpxBean);
        final ExchangeBean djxBean = secMasterSession.getOrCreateExchangeBean("DJX", "Dow Jones");
        assertNotNull(djxBean);
        final CurrencyBean usdBean = secMasterSession.getOrCreateCurrencyBean("USD");
        assertNotNull(usdBean);
        final CurrencyBean jpyBean = secMasterSession.getOrCreateCurrencyBean("JPY");
        assertNotNull(jpyBean);

        final GICSCodeBean carManuBean = secMasterSession.getOrCreateGICSCodeBean("25102010", "Automobile Manufacturers");
        assertNotNull(carManuBean);
        final GICSCodeBean banksBean = secMasterSession.getOrCreateGICSCodeBean("4010", "Banks");
        assertNotNull(banksBean);

        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2000);

        final EquitySecurityBean nomura = EquitySecurityBeanOperation.INSTANCE.createBean(secMasterSession, cal.getTime(), false, cal.getTime(), null, null, "Nomura", tpxBean,
            "Nomura", jpyBean, banksBean);
        assertNotNull(nomura);
        assertEquals("Nomura", nomura.getDisplayName());
        assertEquals(tpxBean, nomura.getExchange());
        assertEquals(jpyBean, nomura.getCurrency());
        assertEquals("Nomura", nomura.getCompanyName());
        assertEquals(banksBean, nomura.getGICSCode());

        final EquitySecurityBean generalMotors = EquitySecurityBeanOperation.INSTANCE.createBean(secMasterSession, cal.getTime(), false, cal.getTime(), null, null, "GM", djxBean,
            "General Motors", usdBean, carManuBean);
        assertNotNull(generalMotors);
        assertEquals("GM", generalMotors.getDisplayName());
        assertEquals(djxBean, generalMotors.getExchange());
        assertEquals(usdBean, generalMotors.getCurrency());
        assertEquals("General Motors", generalMotors.getCompanyName());
        assertEquals(carManuBean, generalMotors.getGICSCode());

        final List<EquitySecurityBean> allEquitySecurities = secMasterSession.getEquitySecurityBeans();
        assertNotNull(allEquitySecurities);
        assertEquals(2, allEquitySecurities.size());
        assertTrue(allEquitySecurities.contains(nomura));
        assertTrue(allEquitySecurities.contains(generalMotors));

        secMasterSession.getCreateOrUpdateIdentifierAssociationBean(cal.getTime(), "BLOOMBERG", "1311 Equity", nomura);
        secMasterSession.getCreateOrUpdateIdentifierAssociationBean(cal.getTime(), "BLOOMBERG", "GM Equity", generalMotors);
        final List<IdentifierAssociationBean> allAssociations = secMasterSession.getAllAssociations();
        System.err.println(allAssociations);

        final EquitySecurityBean hsbc = EquitySecurityBeanOperation.INSTANCE.createBean(secMasterSession, cal.getTime(), false, cal.getTime(), null, null, "HSBC", djxBean, "HSBC",
            usdBean, null);
        assertNotNull(hsbc);
        assertEquals("HSBC", hsbc.getDisplayName());
        assertEquals(djxBean, hsbc.getExchange());
        assertEquals(usdBean, hsbc.getCurrency());
        assertEquals("HSBC", hsbc.getCompanyName());
        assertNull(hsbc.getGICSCode());

        return null;
      }
    });
    final Identifier nomuraDSID = new Identifier("BLOOMBERG", "1311 Equity");
    final Date now = new Date();
    Security hopefullyNomura = _secMaster.getSecurity(new IdentifierBundle(nomuraDSID));
    assertNotNull(hopefullyNomura);
    assertTrue(hopefullyNomura instanceof EquitySecurity);
    EquitySecurity hopefullyNomuraSecurity = (EquitySecurity) hopefullyNomura;
    assertEquals("TPX", hopefullyNomuraSecurity.getExchangeCode());
    assertEquals("Topix", hopefullyNomuraSecurity.getExchange());
    assertEquals("Nomura", hopefullyNomuraSecurity.getCompanyName());
    assertEquals(Currency.getInstance("JPY"), hopefullyNomuraSecurity.getCurrency());
    IdentifierBundle identifiers = hopefullyNomuraSecurity.getIdentifiers();
    assertNotNull(identifiers);
    assertEquals(1, identifiers.size());
    assertTrue(identifiers.contains(nomuraDSID));
    _secMaster.getHibernateTemplate().execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(final Session session) throws HibernateException, SQLException {
        final HibernateSecurityMasterSession secMasterSession = new HibernateSecurityMasterSession(session);
        final ExchangeBean tpxBean = secMasterSession.getOrCreateExchangeBean("TPX", "Topix");
        final CurrencyBean jpyBean = secMasterSession.getOrCreateCurrencyBean("JPY");
        final List<EquitySecurityBean> allEquities = secMasterSession.getEquitySecurityBeans();
        System.err.println(allEquities);
        final EquitySecurityBean nomura = secMasterSession.getCurrentEquitySecurityBean(now, tpxBean, "Nomura", jpyBean);
        assertNotNull(nomura);
        secMasterSession.getCreateOrUpdateIdentifierAssociationBean(now, "BLOOMBERG", "1311 JP Equity", nomura);
        final List<IdentifierAssociationBean> allAssociations = secMasterSession.getAllAssociations();
        System.err.println(allAssociations);
        return null;
      }
    });
    final Identifier nomuraDSID_2 = new Identifier("BLOOMBERG", "1311 JP Equity");
    hopefullyNomura = _secMaster.getSecurity(new IdentifierBundle(nomuraDSID_2));
    assertNotNull(hopefullyNomura);
    System.err.println(hopefullyNomura.getClass());
    assertTrue(hopefullyNomura instanceof EquitySecurity);
    hopefullyNomuraSecurity = (EquitySecurity) hopefullyNomura;
    assertEquals("TPX", hopefullyNomuraSecurity.getExchangeCode());
    assertEquals("Topix", hopefullyNomuraSecurity.getExchange());
    assertEquals("Nomura", hopefullyNomuraSecurity.getCompanyName());
    assertEquals(Currency.getInstance("JPY"), hopefullyNomuraSecurity.getCurrency());
    identifiers = hopefullyNomuraSecurity.getIdentifiers();
    assertNotNull(identifiers);
    assertEquals(2, identifiers.size());
    assertTrue(identifiers.contains(nomuraDSID));
    assertTrue(identifiers.contains(nomuraDSID_2));
  }

  @Test
  public void testEquityOptionSecurityBeans() {
    final double pointValue = 25;
    final Currency dollar = Currency.getInstance("USD");
    final Currency sterling = Currency.getInstance("GBP");
    final Expiry expiry = new Expiry(ZonedDateTime.ofInstant(OffsetDateTime.ofMidnight(2012, 10, 30, ZoneOffset.UTC), TimeZone.UTC));

    final Identifier americanIdentifier = Identifier.of("BLOOMBERG", "American equity option");
    final UniqueIdentifier americanUnderlyingIdentifier = HibernateSecurityMaster.createUniqueIdentifier("underlying american option id");

    final Identifier europeanIdentifier = Identifier.of("BLOOMBERG", "European equity option");
    final UniqueIdentifier europeanUnderlyingIdentifier = HibernateSecurityMaster.createUniqueIdentifier("underlying european option id");

    final Identifier poweredIdentifier = Identifier.of("BLOOMBERG", "Powered equity option");
    final UniqueIdentifier poweredUnderlyingIdentifier = HibernateSecurityMaster.createUniqueIdentifier("underlying powered option id");

    final Date now = new Date();
    // create an American equity option
    EquityOptionSecurity equityOption = new AmericanVanillaEquityOptionSecurity(OptionType.PUT, 1.23, expiry, americanUnderlyingIdentifier, dollar, pointValue, "DJX");
    equityOption.setIdentifiers(new IdentifierBundle(americanIdentifier));
    final UniqueIdentifier americanUID = _secMaster.putSecurity(now, equityOption);
    // create a European equity option
    equityOption = new EuropeanVanillaEquityOptionSecurity(OptionType.CALL, 4.56, expiry, europeanUnderlyingIdentifier, sterling, pointValue, "UKX");
    equityOption.setIdentifiers(new IdentifierBundle(europeanIdentifier));
    final UniqueIdentifier europeUID = _secMaster.putSecurity(now, equityOption);
    // create a Powered equity option
    equityOption = new PoweredEquityOptionSecurity(OptionType.CALL, 4.56, expiry, 7.89, poweredUnderlyingIdentifier, sterling, pointValue, "UKX");
    equityOption.setIdentifiers(new IdentifierBundle(poweredIdentifier));
    final UniqueIdentifier poweredUID = _secMaster.putSecurity(now, equityOption);
    // retrieve the American option
    Security security = _secMaster.getSecurity(americanUID);
    assertNotNull(security);
    assertTrue(security instanceof AmericanVanillaEquityOptionSecurity);
    final AmericanVanillaEquityOptionSecurity american = (AmericanVanillaEquityOptionSecurity) security;
    assertEquals(OptionType.PUT, american.getOptionType());
    assertEquals(1.23, american.getStrike(), 0);
    assertEquals(expiry, american.getExpiry());
    assertEquals(americanUnderlyingIdentifier, american.getUnderlyingSecurity());
    assertEquals(dollar, american.getCurrency());
    assertEquals("DJX", american.getExchange());
    assertEquals(pointValue, american.getPointValue(), 0);
    //    assertEquals(security, _secMaster.getSecurity(new IdentifierBundle(americanIdentifier)));  // TODO: FIX
    // retrieve the European option
    security = _secMaster.getSecurity(europeUID);
    assertNotNull(security);
    assertTrue(security instanceof EuropeanVanillaEquityOptionSecurity);
    final EuropeanVanillaEquityOptionSecurity european = (EuropeanVanillaEquityOptionSecurity) security;
    assertEquals(OptionType.CALL, european.getOptionType());
    assertEquals(4.56, european.getStrike(), 0);
    assertEquals(expiry, european.getExpiry());
    assertEquals(europeanUnderlyingIdentifier, european.getUnderlyingSecurity());
    assertEquals(sterling, european.getCurrency());
    assertEquals("UKX", european.getExchange());
    assertEquals(pointValue, european.getPointValue(), 0);
    //    assertEquals(security, _secMaster.getSecurity(new IdentifierBundle(europeanIdentifier)));  // TODO: FIX
    // retrieve the Powered option
    security = _secMaster.getSecurity(poweredUID);
    assertNotNull(security);
    assertEquals(PoweredEquityOptionSecurity.class, security.getClass());
    final PoweredEquityOptionSecurity powered = (PoweredEquityOptionSecurity) security;
    assertEquals(OptionType.CALL, powered.getOptionType());
    assertEquals(4.56, powered.getStrike(), 0);
    assertEquals(expiry, powered.getExpiry());
    assertEquals(7.89, powered.getPower(), 0);
    assertEquals(poweredUnderlyingIdentifier, powered.getUnderlyingSecurity());
    assertEquals(sterling, powered.getCurrency());
    assertEquals("UKX", powered.getExchange());
    assertEquals(pointValue, powered.getPointValue(), 0);
    //    assertEquals(security, _secMaster.getSecurity(new IdentifierBundle(poweredIdentifier)));  // TODO: FIX
  }

  @Test
  public void testFutureOptionSecurityBeans() {
    final double pointValue = 25;
    final Currency dollar = Currency.getInstance("USD");
    final Currency sterling = Currency.getInstance("GBP");
    final Expiry expiry = new Expiry(ZonedDateTime.ofInstant(OffsetDateTime.ofMidnight(2012, 10, 30, ZoneOffset.UTC), TimeZone.UTC));
    final Identifier americanIdentifier = new Identifier("BLOOMBERG", "American future option");
    final UniqueIdentifier americanUnderlyingIdentifier = HibernateSecurityMaster.createUniqueIdentifier("underlying american future id");
    final Identifier europeanIdentifier = new Identifier("BLOOMBERG", "European future option");
    final UniqueIdentifier europeanUnderlyingIdentifier = HibernateSecurityMaster.createUniqueIdentifier("underlying european future id");
    final Date now = new Date();
    // create an American future option
    FutureOptionSecurity futureOption = new AmericanVanillaFutureOptionSecurity(OptionType.PUT, 1.23, expiry, americanUnderlyingIdentifier, dollar, pointValue, "DJX", true);
    futureOption.setIdentifiers(new IdentifierBundle(americanIdentifier));
    final UniqueIdentifier americanUID = _secMaster.putSecurity(now, futureOption);
    // create a European future option
    futureOption = new EuropeanVanillaFutureOptionSecurity(OptionType.CALL, 4.56, expiry, europeanUnderlyingIdentifier, sterling, pointValue, "UKX", false);
    futureOption.setIdentifiers(new IdentifierBundle(europeanIdentifier));
    final UniqueIdentifier europeUID = _secMaster.putSecurity(now, futureOption);
    // retrieve the American option
    Security security = _secMaster.getSecurity(americanUID);
    //    Security security = _secMaster.getSecurity(new IdentifierBundle(americanIdentifier));
    assertNotNull(security);
    assertTrue(security instanceof AmericanVanillaFutureOptionSecurity);
    final AmericanVanillaFutureOptionSecurity american = (AmericanVanillaFutureOptionSecurity) security;
    assertEquals(OptionType.PUT, american.getOptionType());
    assertEquals(1.23, american.getStrike(), 0);
    assertEquals(expiry, american.getExpiry());
    assertEquals(americanUnderlyingIdentifier, american.getUnderlyingSecurity());
    assertEquals(dollar, american.getCurrency());
    assertEquals("DJX", american.getExchange());
    assertEquals(true, american.isMargined());
    assertEquals(pointValue, american.getPointValue(), 0);
    // retrieve the European option
    security = _secMaster.getSecurity(europeUID);
    //    security = _secMaster.getSecurity(new IdentifierBundle(europeanIdentifier));
    assertNotNull(security);
    assertTrue(security instanceof EuropeanVanillaFutureOptionSecurity);
    final EuropeanVanillaFutureOptionSecurity european = (EuropeanVanillaFutureOptionSecurity) security;
    assertEquals(OptionType.CALL, european.getOptionType());
    assertEquals(4.56, european.getStrike(), 0);
    assertEquals(expiry, european.getExpiry());
    assertEquals(europeanUnderlyingIdentifier, european.getUnderlyingSecurity());
    assertEquals(sterling, european.getCurrency());
    assertEquals("UKX", european.getExchange());
    assertEquals(false, european.isMargined());
    assertEquals(pointValue, european.getPointValue(), 0);
  }

  @Test
  public void testOTCOptionSecurityBeans() {
    final Date now = new Date();
    final Currency dollar = Currency.getInstance("USD");
    final Currency sterling = Currency.getInstance("GBP");
    final Currency euro = Currency.getInstance("EUR");
    final Expiry expiry = new Expiry(ZonedDateTime.ofInstant(OffsetDateTime.ofMidnight(2012, 10, 30, ZoneOffset.UTC), TimeZone.UTC));
    final Identifier fxIdentifier = new Identifier("BLOOMBERG", "fx option");
    final UniqueIdentifier fxUnderlyingIdentifier = HibernateSecurityMaster.createUniqueIdentifier("underlying identity");
    final OTCOptionSecurity security = new FXOptionSecurity(OptionType.PUT, 1.23, expiry, fxUnderlyingIdentifier, euro, "counterparty", dollar, sterling);
    security.setIdentifiers(new IdentifierBundle(fxIdentifier));
    final UniqueIdentifier uid = _secMaster.putSecurity(now, security);
    final Security sec = _secMaster.getSecurity(uid);
    assertNotNull(sec);
    assertTrue(sec instanceof FXOptionSecurity);
    final FXOptionSecurity fxOptionSecurity = (FXOptionSecurity) sec;
    assertEquals(OptionType.PUT, fxOptionSecurity.getOptionType());
    assertEquals(1.23, fxOptionSecurity.getStrike(), 0);
    assertEquals(expiry, fxOptionSecurity.getExpiry());
    assertEquals(fxUnderlyingIdentifier, fxOptionSecurity.getUnderlyingSecurity());
    assertEquals(euro, fxOptionSecurity.getCurrency());
    assertEquals(dollar, fxOptionSecurity.getPutCurrency());
    assertEquals(sterling, fxOptionSecurity.getCallCurrency());
  }

  @Test
  public void testGovernmentBondSecurityBean() {
    final Date now = new Date();
    final Expiry expiry = new Expiry(ZonedDateTime.ofInstant(OffsetDateTime.ofMidnight(2012, 10, 30, ZoneOffset.UTC), TimeZone.UTC));
    final Currency dollar = Currency.getInstance("USD");
    final YieldConvention usStreet = YieldConventionFactory.INSTANCE.getYieldConvention("US street");
    final Frequency annual = FrequencyFactory.INSTANCE.getFrequency("annual");
    final DayCount act360 = DayCountFactory.INSTANCE.getDayCount("Actual/360");
    final BusinessDayConvention following = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("following");
    final LocalDate announcementDate = LocalDate.of(2008, 1, 1);
    final LocalDate interestAccrualDate = LocalDate.of(2010, 3, 4);
    final LocalDate settlementDate = LocalDate.of(2012, 11, 1);
    final LocalDate firstCouponDate = LocalDate.of(2009, 1, 1);
    final Identifier governmentId = new Identifier("BLOOMBERG", "government bond");
    final BondSecurity bond = new GovernmentBondSecurity("issuer name", "issuer type", "issuer domicile", "market", dollar, usStreet, "guarantee type", expiry, "coupon type", 0.5,
        annual, act360, following, announcementDate, interestAccrualDate, settlementDate, firstCouponDate, 10.0, 100d, 10d, 1d, 10d, 15d);
    bond.setIdentifiers(new IdentifierBundle(governmentId));
    final UniqueIdentifier bondUID = _secMaster.putSecurity(now, bond);
    final Security security = _secMaster.getSecurity(bondUID);
    assertNotNull(security);
    assertTrue(security instanceof GovernmentBondSecurity);
    final GovernmentBondSecurity government = (GovernmentBondSecurity) security;
    assertEquals("issuer name", government.getIssuerName());
    assertEquals("issuer type", government.getIssuerType());
    assertEquals("issuer domicile", government.getIssuerDomicile());
    assertEquals("market", government.getMarket());
    assertEquals(dollar, government.getCurrency());
    assertEquals(usStreet, government.getYieldConvention());
    assertEquals("guarantee type", government.getGuaranteeType());
    assertEquals(expiry, government.getMaturity());
    assertEquals("coupon type", government.getCouponType());
    assertEquals(0.5, government.getCouponRate(), 0);
    assertEquals(annual, government.getCouponFrequency());
    assertEquals(act360, government.getDayCountConvention());
    assertEquals(following, government.getBusinessDayConvention());
    assertEquals(announcementDate, government.getAnnouncementDate());
    assertEquals(interestAccrualDate, government.getInterestAccrualDate());
    assertEquals(settlementDate, government.getSettlementDate());
    assertEquals(firstCouponDate, government.getFirstCouponDate());
    assertEquals(10.0, government.getIssuancePrice(), 0);
    assertEquals(100.0, government.getTotalAmountIssued(), 0);
    assertEquals(10.0, government.getMinimumAmount(), 0);
    assertEquals(1.0, government.getMinimumIncrement(), 0);
    assertEquals(10.0, government.getParAmount(), 0);
    assertEquals(15.0, government.getRedemptionValue(), 0);
  }

  @Test
  public void testSecurityDisplayName() {
    // create a security
    final Date now = new Date();
    final Expiry expiry = new Expiry(ZonedDateTime.ofInstant(OffsetDateTime.ofMidnight(2012, 10, 30, ZoneOffset.UTC), TimeZone.UTC));
    final Currency dollar = Currency.getInstance("USD");
    final Identifier underlying = new Identifier("BLOOMBERG", "underlying identifier");
    final Identifier actual = new Identifier("BLOOMBERG", "future identifier");
    IndexFutureSecurity security = new IndexFutureSecurity(expiry, "DJX", "DJX", dollar, underlying);
    // check getDisplayName is not null
    String displayName = security.getName();
    assertNotNull(displayName);
    s_logger.info("default displayName={}", displayName);
    // give it an identifier
    security.setIdentifiers(new IdentifierBundle(actual));
    // check getDisplayName is not null
    displayName = security.getName();
    assertNotNull(displayName);
    s_logger.info("identifier set displayName={}", displayName);
    // give it a specific name
    security.setName("foo");
    // check getDisplayName is correct
    displayName = security.getName();
    assertEquals("foo", displayName);
    // persist it
    final UniqueIdentifier uid = _secMaster.putSecurity(now, security);
    // get it back
    security = (IndexFutureSecurity) _secMaster.getSecurity(uid);
    assertNotNull(security);
    // check getDisplayName is correct
    assertEquals("foo", displayName);
  }

  @Test
  public void testFutureSecurityBeans() {
    final Date now = new Date();
    final Currency dollar = Currency.getInstance("USD");
    final Currency yen = Currency.getInstance("JPY");
    final Expiry expiry = new Expiry(ZonedDateTime.ofInstant(OffsetDateTime.ofMidnight(2012, 10, 30, ZoneOffset.UTC), TimeZone.UTC));
    final Identifier agricultureId = new Identifier("BLOOMBERG", "agriculture");
    final Identifier bondId = new Identifier("BLOOMBERG", "bond");
    final Identifier energyId = new Identifier("BLOOMBERG", "energy");
    final Identifier fxId = new Identifier("BLOOMBERG", "fx");
    final Identifier interestRateId = new Identifier("BLOOMBERG", "interest rate");
    final Identifier metalId = new Identifier("BLOOMBERG", "metal");
    final Identifier indexId = new Identifier("BLOOMBERG", "index");
    final Identifier stockId = new Identifier("BLOOMBERG", "stock");
    final Identifier underlyingId = new Identifier("SecurityScheme", "underlying ID");
    FutureSecurity future;
    future = new AgricultureFutureSecurity(expiry, "TPX", "DJX", dollar, "Red wheat");
    future.setIdentifiers(new IdentifierBundle(agricultureId));
    final UniqueIdentifier agricultureUID = _secMaster.putSecurity(now, future);
    final Set<BondFutureDeliverable> bondDeliverables = new HashSet<BondFutureDeliverable>();
    bondDeliverables.add(new BondFutureDeliverable(new IdentifierBundle(new Identifier("BLOOMBERG", "corporate bond"), new Identifier("BLOOMBERG", "municipal bond")), 1.5));
    bondDeliverables.add(new BondFutureDeliverable(new IdentifierBundle(new Identifier("BLOOMBERG", "government bond")), 3));
    future = new BondFutureSecurity(expiry, "TPX", "DJX", dollar, "type", bondDeliverables);
    future.setIdentifiers(new IdentifierBundle(bondId));
    final UniqueIdentifier bondUID = _secMaster.putSecurity(now, future);
    future = new EnergyFutureSecurity(expiry, "TPX", "DJX", dollar, "Oil", 1.0, "barrel", underlyingId);
    future.setIdentifiers(new IdentifierBundle(energyId));
    final UniqueIdentifier energyUID = _secMaster.putSecurity(now, future);
    future = new FXFutureSecurity(expiry, "DJX", "DJX", dollar, dollar, yen, 10000.0);
    future.setIdentifiers(new IdentifierBundle(fxId));
    final UniqueIdentifier fxUID = _secMaster.putSecurity(now, future);
    future = new InterestRateFutureSecurity(expiry, "TPX", "TPX", yen, "LIBOR");
    future.setIdentifiers(new IdentifierBundle(interestRateId));
    final UniqueIdentifier rateUID = _secMaster.putSecurity(now, future);
    future = new MetalFutureSecurity(expiry, "DJX", "TPX", dollar, "gold", 100.0, "gram", underlyingId);
    future.setIdentifiers(new IdentifierBundle(metalId));
    final UniqueIdentifier metalUID = _secMaster.putSecurity(now, future);
    future = new IndexFutureSecurity(expiry, "DJX", "TPX", dollar, underlyingId);
    future.setIdentifiers(new IdentifierBundle(indexId));
    final UniqueIdentifier indexUID = _secMaster.putSecurity(now, future);
    future = new StockFutureSecurity(expiry, "DJX", "TPX", dollar, underlyingId);
    future.setIdentifiers(new IdentifierBundle(stockId));
    final UniqueIdentifier stockUID = _secMaster.putSecurity(now, future);
    Security security;
    security = _secMaster.getSecurity(agricultureUID);
    assertNotNull(security);
    assertTrue(security instanceof AgricultureFutureSecurity);
    final AgricultureFutureSecurity agricultureSecurity = (AgricultureFutureSecurity) security;
    assertEquals("TPX", agricultureSecurity.getTradingExchange());
    assertEquals("DJX", agricultureSecurity.getSettlementExchange());
    assertEquals(dollar, agricultureSecurity.getCurrency());
    assertEquals("Red wheat", agricultureSecurity.getCommodityType());
    assertEquals(null, agricultureSecurity.getUnitName());
    assertEquals(null, agricultureSecurity.getUnitNumber());
    security = _secMaster.getSecurity(bondUID);
    assertNotNull(security);
    assertTrue(security instanceof BondFutureSecurity);
    final BondFutureSecurity bondSecurity = (BondFutureSecurity) security;
    assertEquals("TPX", bondSecurity.getTradingExchange());
    assertEquals("DJX", bondSecurity.getSettlementExchange());
    assertEquals(dollar, bondSecurity.getCurrency());
    assertEquals("type", bondSecurity.getBondType());
    final Set<BondFutureDeliverable> deliverables = bondSecurity.getBasket();
    assertNotNull(deliverables);
    assertEquals(bondDeliverables.size(), deliverables.size());
    for (final BondFutureDeliverable deliverable : bondDeliverables) {
      if (!deliverables.contains(deliverable)) {
        s_logger.info("deliverable {} not found in {}", deliverable, deliverables);
        fail("deliverable sets didn't match");
      }
    }
    security = _secMaster.getSecurity(energyUID);
    assertNotNull(security);
    assertTrue(security instanceof EnergyFutureSecurity);
    final EnergyFutureSecurity energySecurity = (EnergyFutureSecurity) security;
    assertEquals("TPX", energySecurity.getTradingExchange());
    assertEquals("DJX", energySecurity.getSettlementExchange());
    assertEquals(dollar, energySecurity.getCurrency());
    assertEquals("Oil", energySecurity.getCommodityType());
    assertEquals(1.0, energySecurity.getUnitNumber(), 0);
    assertEquals("barrel", energySecurity.getUnitName());
    assertEquals(underlyingId, energySecurity.getUnderlyingIdentityKey());
    security = _secMaster.getSecurity(fxUID);
    assertNotNull(security);
    assertTrue(security instanceof FXFutureSecurity);
    final FXFutureSecurity fxSecurity = (FXFutureSecurity) security;
    assertEquals("DJX", fxSecurity.getTradingExchange());
    assertEquals("DJX", fxSecurity.getSettlementExchange());
    assertEquals(dollar, fxSecurity.getCurrency());
    assertEquals(dollar, fxSecurity.getNumerator());
    assertEquals(yen, fxSecurity.getDenominator());
    assertEquals(10000.0, fxSecurity.getMultiplicationFactor(), 0);
    security = _secMaster.getSecurity(rateUID);
    assertNotNull(security);
    assertTrue(security instanceof InterestRateFutureSecurity);
    final InterestRateFutureSecurity interestSecurity = (InterestRateFutureSecurity) security;
    assertEquals("TPX", interestSecurity.getTradingExchange());
    assertEquals("TPX", interestSecurity.getSettlementExchange());
    assertEquals(yen, interestSecurity.getCurrency());
    assertEquals("LIBOR", interestSecurity.getCashRateType());
    security = _secMaster.getSecurity(metalUID);
    assertNotNull(security);
    assertTrue(security instanceof MetalFutureSecurity);
    final MetalFutureSecurity metalSecurity = (MetalFutureSecurity) security;
    assertEquals("DJX", metalSecurity.getTradingExchange());
    assertEquals("TPX", metalSecurity.getSettlementExchange());
    assertEquals(dollar, metalSecurity.getCurrency());
    assertEquals("gold", metalSecurity.getCommodityType());
    assertEquals(100.0, metalSecurity.getUnitNumber(), 0);
    assertEquals("gram", metalSecurity.getUnitName());
    assertEquals(underlyingId, metalSecurity.getUnderlyingIdentityKey());
    security = _secMaster.getSecurity(indexUID);
    assertNotNull(security);
    assertTrue(security instanceof IndexFutureSecurity);
    final IndexFutureSecurity indexSecurity = (IndexFutureSecurity) security;
    assertEquals("DJX", indexSecurity.getTradingExchange());
    assertEquals("TPX", indexSecurity.getSettlementExchange());
    assertEquals(dollar, indexSecurity.getCurrency());
    assertEquals(underlyingId, indexSecurity.getUnderlyingIdentityKey());
    security = _secMaster.getSecurity(stockUID);
    assertNotNull(security);
    assertTrue(security instanceof StockFutureSecurity);
    final StockFutureSecurity stockSecurity = (StockFutureSecurity) security;
    assertEquals("DJX", stockSecurity.getTradingExchange());
    assertEquals("TPX", stockSecurity.getSettlementExchange());
    assertEquals(dollar, stockSecurity.getCurrency());
    assertEquals(underlyingId, stockSecurity.getUnderlyingIdentityKey());
  }

  @Test
  public void testDomainSpecificIdentifierAssociationDateRanges() {
    _secMaster.getHibernateTemplate().execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(final Session session) throws HibernateException, SQLException {
        final HibernateSecurityMasterSession secMasterSession = new HibernateSecurityMasterSession(session);
        final ExchangeBean tpxBean = secMasterSession.getOrCreateExchangeBean("TPX", "Topix");
        final CurrencyBean jpyBean = secMasterSession.getOrCreateCurrencyBean("JPY");
        final GICSCodeBean banksBean = secMasterSession.getOrCreateGICSCodeBean("4010", "Banks");
        final Calendar cal2000 = Calendar.getInstance();
        cal2000.set(Calendar.YEAR, 2000);
        final Calendar cal2001 = Calendar.getInstance();
        cal2001.set(Calendar.YEAR, 2001);
        final Calendar cal2002 = Calendar.getInstance();
        cal2002.set(Calendar.YEAR, 2002);
        final Calendar cal2003 = Calendar.getInstance();
        cal2003.set(Calendar.YEAR, 2003);
        final Calendar cal2004 = Calendar.getInstance();
        cal2004.set(Calendar.YEAR, 2004);
        final EquitySecurityBean nomuraBean = EquitySecurityBeanOperation.INSTANCE.createBean(secMasterSession, cal2000.getTime(), false, cal2000.getTime(), null, null, "Nomura",
            tpxBean, "Nomura", jpyBean, banksBean);
        final EquitySecurityBean notNomuraBean = EquitySecurityBeanOperation.INSTANCE.createBean(secMasterSession, cal2003.getTime(), false, cal2003.getTime(), null, null,
            "Something else", tpxBean, "Not Nomura", jpyBean, banksBean);
        final IdentifierAssociationBean dsiab1 = secMasterSession.getCreateOrUpdateIdentifierAssociationBean(cal2001.getTime(), "BLOOMBERG", "1311 Equity", nomuraBean);
        IdentifierAssociationBean dsiab2 = secMasterSession.getCreateOrUpdateIdentifierAssociationBean(cal2002.getTime(), "BLOOMBERG", "1311 Equity", nomuraBean);
        assertEquals(dsiab1.getId(), dsiab2.getId());
        IdentifierAssociationBean dsiab3 = secMasterSession.getCreateOrUpdateIdentifierAssociationBean(cal2003.getTime(), "BLOOMBERG", "1311 Equity", notNomuraBean);
        if (dsiab1.getId() == dsiab3.getId())
          fail("different association should have been created");
        assertNotNull(dsiab3.getValidStartDate());
        assertNull(dsiab3.getValidEndDate());
        final IdentifierAssociationBean dsiab4 = secMasterSession.getCreateOrUpdateIdentifierAssociationBean(cal2004.getTime(), "BLOOMBERG", "1311 Equity", nomuraBean);
        if (dsiab1.getId() == dsiab4.getId())
          fail("different association should have been created");
        if (dsiab3.getId() == dsiab4.getId())
          fail("different association should have been created");
        assertNotNull(dsiab4.getValidStartDate());
        assertNull(dsiab4.getValidEndDate());

        dsiab2 = secMasterSession.getCreateOrUpdateIdentifierAssociationBean(cal2002.getTime(), "BLOOMBERG", "1311 Equity", nomuraBean);
        dsiab3 = secMasterSession.getCreateOrUpdateIdentifierAssociationBean(cal2003.getTime(), "BLOOMBERG", "1311 Equity", notNomuraBean);
        assertEquals(dsiab1.getId(), dsiab2.getId());
        assertNull(dsiab2.getValidStartDate());
        assertNotNull(dsiab2.getValidEndDate());
        assertNotNull(dsiab3.getValidStartDate());
        assertNotNull(dsiab3.getValidEndDate());
        assertNotNull(dsiab4.getValidStartDate());
        assertNull(dsiab4.getValidEndDate());
        assertEquals(dsiab2.getValidEndDate(), dsiab3.getValidStartDate());
        assertEquals(dsiab3.getValidEndDate(), dsiab4.getValidStartDate());

        return null;
      }
    });
  }

  @Test
  public void testTopLevelFunctionality() {
    final Calendar instance = Calendar.getInstance();
    instance.set(Calendar.YEAR, 2003);
    final Date yesterYear2003 = instance.getTime();
    instance.set(Calendar.YEAR, 2004);
    final Date yesterYear2004 = instance.getTime();
    instance.set(Calendar.YEAR, 2005);
    final Date yesterYear2005 = instance.getTime();
    instance.set(Calendar.YEAR, 2006);
    final Date yesterYear2006 = instance.getTime();

    final EquitySecurity generalMotors = new EquitySecurity();
    generalMotors.setCompanyName("General Motors");
    generalMotors.setName("General Motors");
    generalMotors.setCurrency(Currency.getInstance("USD"));
    generalMotors.setExchangeCode("NYSE");
    generalMotors.setExchange("NEW YORK STOCK EXCHANGE");
    generalMotors.setTicker("GM US Equity");
    generalMotors.setGICSCode(GICSCode.getInstance(25102010));
    generalMotors.setIdentifiers(new IdentifierBundle(Identifier.of("BLOOMBERG", "GM US Equity")));
    //    generalMotors.setUniqueIdentifier(HibernateSecurityMaster.createUniqueIdentifier("1"));

    final EquitySecurity nomura = new EquitySecurity();
    nomura.setCompanyName("Nomura");
    nomura.setName("Nomura - 1311 JP Equity");
    nomura.setCurrency(Currency.getInstance("JPY"));
    nomura.setExchangeCode("TPX");
    nomura.setExchange("TOPIX");
    nomura.setTicker("1311 JP Equity");
    nomura.setGICSCode(GICSCode.getInstance(4010));
    nomura.setIdentifiers(new IdentifierBundle(Identifier.of("BLOOMBERG", "1311 JP Equity")));

    _secMaster.putSecurity(yesterYear2003, generalMotors);
    _secMaster.putSecurity(yesterYear2003, nomura);

    // TODO: Fix ticker from DB, also see below
    //    Security shouldBeNomura = _secMaster.getSecurity(
    //        new IdentifierBundle(new Identifier("BLOOMBERG", "1311 JP Equity")));
    //    assertEquals(nomura, shouldBeNomura);
    //    Security shouldBeGM = _secMaster.getSecurity(
    //        new IdentifierBundle(new Identifier("BLOOMBERG", "GM US Equity")));
    //    assertEquals(generalMotors, shouldBeGM);

    final EquitySecurity generalMotors2 = new EquitySecurity();
    generalMotors2.setCompanyName("General Motors (Govt owned)");
    generalMotors2.setName("General Motors");
    generalMotors2.setCurrency(Currency.getInstance("USD"));
    generalMotors2.setExchangeCode("NYSE");
    generalMotors2.setExchange("NEW YORK STOCK EXCHANGE");
    generalMotors2.setTicker("GM US Equity");
    generalMotors2.setGICSCode(GICSCode.getInstance(25102010));
    generalMotors2.setIdentifiers(new IdentifierBundle(Identifier.of("BLOOMBERG", "GM US Equity")));
    //    generalMotors2.setUniqueIdentifier(HibernateSecurityMaster.createUniqueIdentifier("1"));
    _secMaster.putSecurity(yesterYear2005, generalMotors2);
    _secMaster.getHibernateTemplate().execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(final Session session) throws HibernateException, SQLException {
        final HibernateSecurityMasterSession secMasterSession = new HibernateSecurityMasterSession(session);
        final List<EquitySecurityBean> equitySecurityBeans = secMasterSession.getEquitySecurityBeans();
        s_logger.debug("equitySecurityBeans: {}", equitySecurityBeans);
        final List<IdentifierAssociationBean> allAssociations = secMasterSession.getAllAssociations();
        s_logger.debug("allAssociations: {}", allAssociations);
        return null;
      }
    });
    //    shouldBeGM = _secMaster.getSecurity(yesterYear2004, HibernateSecurityMaster.createUniqueIdentifier("1"), true);
    //    assertEquals(generalMotors, shouldBeGM);
    //    shouldBeGM = _secMaster.getSecurity(yesterYear2006, HibernateSecurityMaster.createUniqueIdentifier("1"), true);
    //    assertEquals(generalMotors2, shouldBeGM);
  }

  @Test
  public void testUpdate() {
    Date date1 = new Date(System.currentTimeMillis() - 2000L);
    Date date2 = new Date(date1.getTime() + 2000L);
    
    final EquitySecurity sec = new EquitySecurity();
    sec.setCompanyName("General Motors");
    sec.setName("General Motors");
    sec.setCurrency(Currency.getInstance("USD"));
    sec.setExchangeCode("NYSE");
    sec.setExchange("NEW YORK STOCK EXCHANGE");
    sec.setTicker("GM US Equity");
    sec.setGICSCode(GICSCode.getInstance(25102010));
    sec.setIdentifiers(new IdentifierBundle(Identifier.of("BLOOMBERG", "GM US Equity")));
    UniqueIdentifier uid1 = _secMaster.putSecurity(date1, sec);
    
    sec.setCompanyName("Big Motors");
    UniqueIdentifier uid2 = _secMaster.putSecurity(date2, sec);
    
    assertEquals(uid1.toLatest(), uid2.toLatest());
    
    EquitySecurity loaded1 = (EquitySecurity) _secMaster.getSecurity(uid1);
    assertEquals("General Motors", loaded1.getCompanyName());
    
    EquitySecurity loaded2 = (EquitySecurity) _secMaster.getSecurity(uid2);
    assertEquals("Big Motors", loaded2.getCompanyName());
  }

}
