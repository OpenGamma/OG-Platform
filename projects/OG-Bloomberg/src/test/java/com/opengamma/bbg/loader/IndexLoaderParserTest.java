/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import static com.opengamma.bbg.util.BloombergSecurityUtils.makeAPVLEquityOptionSecurity;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeCommodityFutureOptionSecurity;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeEURIBORFutureOptionSecurity;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeEURODOLLARFutureOptionSecurity;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeEquityIndexDividendFutureOptionSecurity;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeEquityIndexFutureOptionSecurity;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeExpectedAAPLEquitySecurity;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeFxFutureOptionSecurity;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeInterestRateFuture;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeLIBORFutureOptionSecurity;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeSPXIndexOptionSecurity;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeUSBondFuture;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.referencedata.impl.BloombergReferenceDataProvider;
import com.opengamma.bbg.security.BloombergSecurityProvider;
import com.opengamma.bbg.test.BloombergTestUtils;
import com.opengamma.core.security.Security;
import com.opengamma.financial.security.DefaultSecurityLoader;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.bond.MunicipalBondSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.cashflow.CashFlowSecurity;
import com.opengamma.financial.security.deposit.ContinuousZeroDepositSecurity;
import com.opengamma.financial.security.deposit.PeriodicZeroDepositSecurity;
import com.opengamma.financial.security.deposit.SimpleZeroDepositSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureDeliverable;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexDividendFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.FxFutureOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXDigitalOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.timeseries.exchange.DefaultExchangeDataProvider;
import com.opengamma.financial.timeseries.exchange.ExchangeDataProvider;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.masterdb.security.DbSecurityMaster;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDetailProvider;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterFiles;
import com.opengamma.util.db.DbConnectorFactoryBean;
import com.opengamma.util.db.HibernateMappingFiles;
import com.opengamma.util.test.AbstractDbTest;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT, singleThreaded = true)
public class IndexLoaderParserTest {

  private static final Logger s_logger = LoggerFactory.getLogger(IndexLoaderParserTest.class);

  @Test
  public void testMonths() {
    assertEquals(Tenor.THREE_MONTHS, IndexLoader.decodeTenor("ICE LIBOR USD 3 Months"));
  }
  
  @Test
  public void testOvernight() {
    assertEquals(Tenor.ON, IndexLoader.decodeTenor("EURIBOR Overnight Index"));
    assertEquals(Tenor.ON, IndexLoader.decodeTenor("EURIBOR OVERNIGHT INDEX"));
    assertEquals(Tenor.ON, IndexLoader.decodeTenor("EURIBOR O/N INDEX"));
    assertEquals(Tenor.ON, IndexLoader.decodeTenor("EURIBOR Overnight"));
    assertEquals(Tenor.ON, IndexLoader.decodeTenor("EURIBOR OVERNIGHT"));
    assertEquals(Tenor.ON, IndexLoader.decodeTenor("EURIBOR O/N"));
  }
  
  @Test
  public void testShortMonths() {
    assertEquals(Tenor.SIX_MONTHS, IndexLoader.decodeTenor("EURIBOR 6M"));
  }
  
  @Test
  public void testTomNext() {
    assertEquals(Tenor.TN, IndexLoader.decodeTenor("RANDOM INDEX Tomorrow Next"));
    assertEquals(Tenor.TN, IndexLoader.decodeTenor("RANDOM INDEX Tomorrow/Next"));
    assertEquals(Tenor.TN, IndexLoader.decodeTenor("RANDOM INDEX TOM/NEXT"));
    assertEquals(Tenor.TN, IndexLoader.decodeTenor("RANDOM INDEX T/N TRAILING"));
  }
  
  @Test
  public void testYears() {
    assertEquals(Tenor.THREE_YEARS, IndexLoader.decodeTenor("RANDOM INDEX 3 YEARS"));
  }
  
  @Test
  public void testDays() {
    assertEquals(Tenor.THREE_DAYS, IndexLoader.decodeTenor("RANDOM INDEX 3 Days"));
  }
  
  @Test
  public void testShortDays() {
    assertEquals(Tenor.THREE_DAYS, IndexLoader.decodeTenor("RANDOM INDEX 3 Days"));
  }

}
