/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve.exposure;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.cashflow.CashFlowSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;
import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class CurrencyExposureFunctionTest {

  @Test
  public void testAgricultureFutureSecurity() {
    final SecuritySource securitySource = ExposureFunctionTestHelper.getSecuritySource(null);
    final ExposureFunction exposureFunction = new CurrencyExposureFunction(securitySource);
    final AgricultureFutureSecurity future = ExposureFunctionTestHelper.getAgricultureFutureSecurity();
    final List<ExternalId> ids = future.accept(exposureFunction);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(Currency.EUR.getObjectId().getScheme(), "EUR"), ids.get(0));
  }

  @Test
  public void testBondFutureSecurity() {
    final SecuritySource securitySource = ExposureFunctionTestHelper.getSecuritySource(null);
    final ExposureFunction exposureFunction = new CurrencyExposureFunction(securitySource);
    final BondFutureSecurity future = ExposureFunctionTestHelper.getBondFutureSecurity();
    final List<ExternalId> ids = future.accept(exposureFunction);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(Currency.EUR.getObjectId().getScheme(), "EUR"), ids.get(0));
  }

  @Test
  public void testCapFloorCMSSpreadSecurity() {
    final SecuritySource securitySource = ExposureFunctionTestHelper.getSecuritySource(null);
    final ExposureFunction exposureFunction = new CurrencyExposureFunction(securitySource);
    final CapFloorCMSSpreadSecurity security = ExposureFunctionTestHelper.getCapFloorCMSSpreadSecurity();
    final List<ExternalId> ids = security.accept(exposureFunction);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(Currency.EUR.getObjectId().getScheme(), "EUR"), ids.get(0));
  }

  @Test
  public void testCapFloorSecurity() {
    final SecuritySource securitySource = ExposureFunctionTestHelper.getSecuritySource(null);
    final ExposureFunction exposureFunction = new CurrencyExposureFunction(securitySource);
    final CapFloorSecurity security = ExposureFunctionTestHelper.getCapFloorSecurity();
    final List<ExternalId> ids = security.accept(exposureFunction);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(Currency.USD.getObjectId().getScheme(), "USD"), ids.get(0));
  }

  @Test
  public void testCashFlowSecurity() {
    final SecuritySource securitySource = ExposureFunctionTestHelper.getSecuritySource(null);
    final ExposureFunction exposureFunction = new CurrencyExposureFunction(securitySource);
    final CashFlowSecurity security = ExposureFunctionTestHelper.getCashFlowSecurity();
    final List<ExternalId> ids = security.accept(exposureFunction);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(Currency.EUR.getObjectId().getScheme(), "EUR"), ids.get(0));
  }

  @Test
  public void testCashSecurity() {
    final SecuritySource securitySource = ExposureFunctionTestHelper.getSecuritySource(null);
    final ExposureFunction exposureFunction = new CurrencyExposureFunction(securitySource);
    final CashSecurity cash = ExposureFunctionTestHelper.getCashSecurity();
    final List<ExternalId> ids = cash.accept(exposureFunction);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(Currency.USD.getObjectId().getScheme(), "USD"), ids.get(0));
  }

  @Test
  public void testEnergyFutureOptionSecurity() {
    final SecuritySource securitySource = ExposureFunctionTestHelper.getSecuritySource(null);
    final ExposureFunction exposureFunction = new CurrencyExposureFunction(securitySource);
    final CommodityFutureOptionSecurity security = ExposureFunctionTestHelper.getEnergyFutureOptionSecurity();
    final List<ExternalId> ids = security.accept(exposureFunction);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(Currency.EUR.getObjectId().getScheme(), "EUR"), ids.get(0));
  }

  @Test
  public void testEnergyFutureSecurity() {
    final SecuritySource securitySource = ExposureFunctionTestHelper.getSecuritySource(null);
    final ExposureFunction exposureFunction = new CurrencyExposureFunction(securitySource);
    final EnergyFutureSecurity future = ExposureFunctionTestHelper.getEnergyFutureSecurity();
    final List<ExternalId> ids = future.accept(exposureFunction);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(Currency.USD.getObjectId().getScheme(), "USD"), ids.get(0));
  }

  @Test
  public void testEquityFutureSecurity() {
    final SecuritySource securitySource = ExposureFunctionTestHelper.getSecuritySource(null);
    final ExposureFunction exposureFunction = new CurrencyExposureFunction(securitySource);
    final EquityFutureSecurity future = ExposureFunctionTestHelper.getEquityFutureSecurity();
    final List<ExternalId> ids = future.accept(exposureFunction);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(Currency.USD.getObjectId().getScheme(), "USD"), ids.get(0));
  }

  @Test
  public void testEquityIndexDividendFutureSecurity() {
    final SecuritySource securitySource = ExposureFunctionTestHelper.getSecuritySource(null);
    final ExposureFunction exposureFunction = new CurrencyExposureFunction(securitySource);
    final EquityIndexDividendFutureSecurity future = ExposureFunctionTestHelper.getEquityIndexDividendFutureSecurity();
    final List<ExternalId> ids = future.accept(exposureFunction);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(Currency.EUR.getObjectId().getScheme(), "USD"), ids.get(0));
  }

  @Test
  public void testFRASecurity() {
    final SecuritySource securitySource = ExposureFunctionTestHelper.getSecuritySource(null);
    final ExposureFunction exposureFunction = new CurrencyExposureFunction(securitySource);
    final FRASecurity fra = ExposureFunctionTestHelper.getFRASecurity();
    final List<ExternalId> ids = fra.accept(exposureFunction);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(Currency.USD.getObjectId().getScheme(), "USD"), ids.get(0));
  }

  @Test
  public void testFXFutureSecurity() {
    final SecuritySource securitySource = ExposureFunctionTestHelper.getSecuritySource(null);
    final ExposureFunction exposureFunction = new CurrencyExposureFunction(securitySource);
    final FXFutureSecurity future = ExposureFunctionTestHelper.getFXFutureSecurity();
    final List<ExternalId> ids = future.accept(exposureFunction);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(Currency.EUR.getObjectId().getScheme(), "EUR"), ids.get(0));
  }

  @Test
  public void testIndexFutureSecurity() {
    final SecuritySource securitySource = ExposureFunctionTestHelper.getSecuritySource(null);
    final ExposureFunction exposureFunction = new CurrencyExposureFunction(securitySource);
    final IndexFutureSecurity future = ExposureFunctionTestHelper.getIndexFutureSecurity();
    final List<ExternalId> ids = future.accept(exposureFunction);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(Currency.EUR.getObjectId().getScheme(), "EUR"), ids.get(0));
  }

  @Test
  public void testInterestRateFutureSecurity() {
    final SecuritySource securitySource = ExposureFunctionTestHelper.getSecuritySource(null);
    final ExposureFunction exposureFunction = new CurrencyExposureFunction(securitySource);
    final InterestRateFutureSecurity future = ExposureFunctionTestHelper.getInterestRateFutureSecurity();
    final List<ExternalId> ids = future.accept(exposureFunction);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(Currency.USD.getObjectId().getScheme(), "USD"), ids.get(0));
  }

  @Test
  public void testMetalFutureSecurity() {
    final SecuritySource securitySource = ExposureFunctionTestHelper.getSecuritySource(null);
    final ExposureFunction exposureFunction = new CurrencyExposureFunction(securitySource);
    final MetalFutureSecurity future = ExposureFunctionTestHelper.getMetalFutureSecurity();
    final List<ExternalId> ids = future.accept(exposureFunction);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(Currency.USD.getObjectId().getScheme(), "USD"), ids.get(0));
  }

  @Test
  public void testStockFutureSecurity() {
    final SecuritySource securitySource = ExposureFunctionTestHelper.getSecuritySource(null);
    final ExposureFunction exposureFunction = new CurrencyExposureFunction(securitySource);
    final StockFutureSecurity future = ExposureFunctionTestHelper.getStockFutureSecurity();
    final List<ExternalId> ids = future.accept(exposureFunction);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(Currency.USD.getObjectId().getScheme(), "USD"), ids.get(0));
  }

}
