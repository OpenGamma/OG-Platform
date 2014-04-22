/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg.security;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Set;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.opengamma.bbg.BloombergConnector;
import com.opengamma.bbg.BloombergConstants;
import com.opengamma.bbg.referencedata.impl.BloombergReferenceDataProvider;
import com.opengamma.bbg.test.BloombergTestUtils;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.security.Security;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.timeseries.exchange.DefaultExchangeDataProvider;
import com.opengamma.financial.timeseries.exchange.ExchangeDataProvider;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.provider.security.SecurityProvider;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Base case for testing BloombergSecuritySource.
 */
@Test(groups = TestGroup.INTEGRATION)
public class BloombergBpipeSecurityProviderTest {

  private static final String IBM_TICKER = "IBM US Equity";
  private static final String AUTH_OPTION = "app=opengamma:OpenGamma Application B-Pipe BPS";
  private BloombergReferenceDataProvider _refDataProvider;
  private SecurityProvider _securityProvider;

  @BeforeClass
  public void setupSecurityProvider() throws Exception {
    _securityProvider = createSecurityProvider();
  }

  protected SecurityProvider createSecurityProvider() throws Exception {
    BloombergConnector connector = BloombergTestUtils.getBloombergBipeConnector();
    BloombergReferenceDataProvider refDataProvider = new BloombergReferenceDataProvider(connector, AUTH_OPTION, 0.002);
    refDataProvider.start();
    _refDataProvider = refDataProvider;
    ExchangeDataProvider exchangeProvider = DefaultExchangeDataProvider.getInstance();
    return new BloombergSecurityProvider(refDataProvider, exchangeProvider);
  }

  @AfterClass
  public void terminateSecurityProvider() throws Exception {
    stopSecurityProvider(_securityProvider);
    _securityProvider = null;
  }

  protected void stopSecurityProvider(SecurityProvider provider) throws Exception {
    if (_refDataProvider != null) {
      BloombergReferenceDataProvider dataProvider = _refDataProvider;
      _refDataProvider = null;
      dataProvider.stop();
    }
  }

  @Test(enabled = false)
  public void ibmEquity() throws Exception {
    assertIbm(_securityProvider.getSecurity(ExternalIdBundle.of(ExternalSchemes.bloombergTickerSecurityId(IBM_TICKER))));
    Thread.sleep(10 * 1000);
    assertIbm(_securityProvider.getSecurity(ExternalIdBundle.of(ExternalSchemes.bloombergTickerSecurityId(IBM_TICKER))));
  }

  protected void assertIbm(Security sec) {
    assertNotNull(sec);
    assertTrue(sec instanceof EquitySecurity);
    EquitySecurity finSecurity = (EquitySecurity) sec;
    assertNotNull(finSecurity.getPermissions());
    Set<String> permissions = finSecurity.getPermissions();
    assertEquals(1, permissions.size());
    assertTrue(permissions.contains(BloombergConstants.BLOOMBERG_DATA_SOURCE_NAME + ":" + 39491));
    assertEquals("INTL BUSINESS MACHINES CORP", finSecurity.getName());
    assertEquals("EQUITY", finSecurity.getSecurityType());
    assertEquals("IBM", finSecurity.getShortName());
    assertEquals("NEW YORK STOCK EXCHANGE, INC.", finSecurity.getExchange());
    assertEquals("XNYS", finSecurity.getExchangeCode());
    assertEquals(Currency.USD, finSecurity.getCurrency());
    //    assertEquals("45102010", finSecurity.getGicsCode().toString());
    assertEquals(IBM_TICKER, finSecurity.getExternalIdBundle().getExternalId(ExternalSchemes.BLOOMBERG_TICKER).getValue());
    assertEquals("EQ0010080100001000", finSecurity.getExternalIdBundle().getExternalId(ExternalSchemes.BLOOMBERG_BUID).getValue());
    assertEquals("459200101", finSecurity.getExternalIdBundle().getExternalId(ExternalSchemes.CUSIP).getValue());
    assertEquals("US4592001014", finSecurity.getExternalIdBundle().getExternalId(ExternalSchemes.ISIN).getValue());
    assertEquals("2005973", finSecurity.getExternalIdBundle().getExternalId(ExternalSchemes.SEDOL1).getValue());
  }

}
