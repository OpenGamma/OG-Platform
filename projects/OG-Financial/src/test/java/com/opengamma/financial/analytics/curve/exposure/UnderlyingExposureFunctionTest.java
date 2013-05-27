/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve.exposure;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.cashflow.CashFlowSecurity;
import com.opengamma.financial.security.deposit.ContinuousZeroDepositSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
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
import com.opengamma.financial.security.option.BondFutureOptionSecurity;
import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.FxFutureOptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class UnderlyingExposureFunctionTest {

  @Test
  public void testAgriculturalFutureSecurity() {
    final SecuritySource securitySource = ExposureFunctionTestHelper.getSecuritySource(null);
    final ExposureFunction exposureFunction = new UnderlyingExposureFunction(securitySource);
    final AgricultureFutureSecurity future = ExposureFunctionTestHelper.getAgricultureFutureSecurity();
    final List<ExternalId> ids = future.accept(exposureFunction);
    assertNull(ids);
  }

  @Test
  public void testBondFutureOptionSecurity() {
    final BondFutureSecurity future = ExposureFunctionTestHelper.getBondFutureSecurity();
    final SecuritySource securitySource = ExposureFunctionTestHelper.getSecuritySource(future);
    final ExposureFunction exposureFunction = new UnderlyingExposureFunction(securitySource);
    final BondFutureOptionSecurity security = ExposureFunctionTestHelper.getBondFutureOptionSecurity();
    final List<ExternalId> ids = security.accept(exposureFunction);
    assertNull(ids);
  }

  @Test
  public void testBondFutureSecurity() {
    final SecuritySource securitySource = ExposureFunctionTestHelper.getSecuritySource(null);
    final ExposureFunction exposureFunction = new UnderlyingExposureFunction(securitySource);
    final BondFutureSecurity future = ExposureFunctionTestHelper.getBondFutureSecurity();
    final List<ExternalId> ids = future.accept(exposureFunction);
    assertNull(ids);
  }

  @Test
  public void testCapFloorCMSSpreadSecurity() {
    final SecuritySource securitySource = ExposureFunctionTestHelper.getSecuritySource(null);
    final ExposureFunction exposureFunction = new UnderlyingExposureFunction(securitySource);
    final CapFloorCMSSpreadSecurity security = ExposureFunctionTestHelper.getCapFloorCMSSpreadSecurity();
    final List<ExternalId> ids = security.accept(exposureFunction);
    assertEquals(2, ids.size());
    assertEquals(ExternalSchemes.syntheticSecurityId("USD 10y Swap"), ids.get(0));
    assertEquals(ExternalSchemes.syntheticSecurityId("USD 15y Swap"), ids.get(1));
  }

  @Test
  public void testCapFloorSecurity() {
    final SecuritySource securitySource = ExposureFunctionTestHelper.getSecuritySource(null);
    final ExposureFunction exposureFunction = new UnderlyingExposureFunction(securitySource);
    final CapFloorSecurity security = ExposureFunctionTestHelper.getCapFloorSecurity();
    final List<ExternalId> ids = security.accept(exposureFunction);
    assertEquals(1, ids.size());
    assertEquals(ExternalSchemes.syntheticSecurityId("USD 6m Libor"), ids.get(0));
  }

  @Test
  public void testCashFlowSecurity() {
    final SecuritySource securitySource = ExposureFunctionTestHelper.getSecuritySource(null);
    final ExposureFunction exposureFunction = new UnderlyingExposureFunction(securitySource);
    final CashFlowSecurity security = ExposureFunctionTestHelper.getCashFlowSecurity();
    final List<ExternalId> ids = security.accept(exposureFunction);
    assertNull(ids);
  }

  @Test
  public void testCashSecurity() {
    final SecuritySource securitySource = ExposureFunctionTestHelper.getSecuritySource(null);
    final ExposureFunction exposureFunction = new UnderlyingExposureFunction(securitySource);
    final CashSecurity cash = ExposureFunctionTestHelper.getCashSecurity();
    final List<ExternalId> ids = cash.accept(exposureFunction);
    assertNull(ids);
  }

  @Test
  public void testContinuousZeroDepositSecurity() {
    final SecuritySource securitySource = ExposureFunctionTestHelper.getSecuritySource(null);
    final ExposureFunction exposureFunction = new UnderlyingExposureFunction(securitySource);
    final ContinuousZeroDepositSecurity security = ExposureFunctionTestHelper.getContinuousZeroDepositSecurity();
    final List<ExternalId> ids = security.accept(exposureFunction);
    assertNull(ids);
  }

  @Test
  public void testCorporateBondSecurity() {
    final SecuritySource securitySource = ExposureFunctionTestHelper.getSecuritySource(null);
    final ExposureFunction exposureFunction = new UnderlyingExposureFunction(securitySource);
    final CorporateBondSecurity security = ExposureFunctionTestHelper.getCorporateBondSecurity();
    final List<ExternalId> ids = security.accept(exposureFunction);
    assertNull(ids);
  }

  @Test
  public void testEnergyFutureOptionSecurity() {
    final EnergyFutureSecurity future = ExposureFunctionTestHelper.getEnergyFutureSecurity();
    final SecuritySource securitySource = ExposureFunctionTestHelper.getSecuritySource(future);
    final ExposureFunction exposureFunction = new UnderlyingExposureFunction(securitySource);
    final CommodityFutureOptionSecurity security = ExposureFunctionTestHelper.getEnergyFutureOptionSecurity();
    final List<ExternalId> ids = security.accept(exposureFunction);
    assertNull(ids);
  }

  @Test
  public void testEnergyFutureSecurity() {
    final SecuritySource securitySource = ExposureFunctionTestHelper.getSecuritySource(null);
    final ExposureFunction exposureFunction = new UnderlyingExposureFunction(securitySource);
    final EnergyFutureSecurity future = ExposureFunctionTestHelper.getEnergyFutureSecurity();
    final List<ExternalId> ids = future.accept(exposureFunction);
    assertNull(ids);
  }

  @Test
  public void testEquityBarrierOptionSecurity() {
    final SecuritySource securitySource = ExposureFunctionTestHelper.getSecuritySource(null);
    final ExposureFunction exposureFunction = new UnderlyingExposureFunction(securitySource);
    final EquityBarrierOptionSecurity security = ExposureFunctionTestHelper.getEquityBarrierOptionSecurity();
    final List<ExternalId> ids = security.accept(exposureFunction);
    assertNull(ids);
  }

  @Test
  public void testEquityFutureSecurity() {
    final SecuritySource securitySource = ExposureFunctionTestHelper.getSecuritySource(null);
    final ExposureFunction exposureFunction = new UnderlyingExposureFunction(securitySource);
    final EquityFutureSecurity future = ExposureFunctionTestHelper.getEquityFutureSecurity();
    final List<ExternalId> ids = future.accept(exposureFunction);
    assertNull(ids);
  }

  @Test
  public void testEquityIndexDividendFutureSecurity() {
    final SecuritySource securitySource = ExposureFunctionTestHelper.getSecuritySource(null);
    final EquityIndexDividendFutureSecurity future = ExposureFunctionTestHelper.getEquityIndexDividendFutureSecurity();
    final ExposureFunction exposureFunction = new UnderlyingExposureFunction(securitySource);
    final List<ExternalId> ids = future.accept(exposureFunction);
    assertNull(ids);
  }

  @Test
  public void testEquitySecurity() {
    final SecuritySource securitySource = ExposureFunctionTestHelper.getSecuritySource(null);
    final ExposureFunction exposureFunction = new UnderlyingExposureFunction(securitySource);
    final EquitySecurity security = ExposureFunctionTestHelper.getEquitySecurity();
    final List<ExternalId> ids = security.accept(exposureFunction);
    assertNull(ids);
  }

  @Test
  public void testFRASecurity() {
    final SecuritySource securitySource = ExposureFunctionTestHelper.getSecuritySource(null);
    final ExposureFunction exposureFunction = new UnderlyingExposureFunction(securitySource);
    final FRASecurity fra = ExposureFunctionTestHelper.getFRASecurity();
    final List<ExternalId> ids = fra.accept(exposureFunction);
    assertEquals(1, ids.size());
    assertEquals(fra.getUnderlyingId(), ids.get(0));
  }

  @Test
  public void testFXFutureOptionSecurity() {
    final FXFutureSecurity future = ExposureFunctionTestHelper.getFXFutureSecurity();
    final SecuritySource securitySource = ExposureFunctionTestHelper.getSecuritySource(future);
    final ExposureFunction exposureFunction = new UnderlyingExposureFunction(securitySource);
    final FxFutureOptionSecurity security = ExposureFunctionTestHelper.getFXFutureOptionSecurity();
    final List<ExternalId> ids = security.accept(exposureFunction);
    assertNull(ids);
  }

  @Test
  public void testFXFutureSecurity() {
    final SecuritySource securitySource = ExposureFunctionTestHelper.getSecuritySource(null);
    final ExposureFunction exposureFunction = new UnderlyingExposureFunction(securitySource);
    final FXFutureSecurity future = ExposureFunctionTestHelper.getFXFutureSecurity();
    final List<ExternalId> ids = future.accept(exposureFunction);
    assertNull(ids);
  }

  @Test
  public void testIndexFutureSecurity() {
    final SecuritySource securitySource = ExposureFunctionTestHelper.getSecuritySource(null);
    final ExposureFunction exposureFunction = new UnderlyingExposureFunction(securitySource);
    final IndexFutureSecurity future = ExposureFunctionTestHelper.getIndexFutureSecurity();
    final List<ExternalId> ids = future.accept(exposureFunction);
    assertNull(ids);
  }

  @Test
  public void testInterestRateFutureSecurity() {
    final SecuritySource securitySource = ExposureFunctionTestHelper.getSecuritySource(null);
    final ExposureFunction exposureFunction = new UnderlyingExposureFunction(securitySource);
    final InterestRateFutureSecurity future = ExposureFunctionTestHelper.getInterestRateFutureSecurity();
    final List<ExternalId> ids = future.accept(exposureFunction);
    assertEquals(1, ids.size());
    assertEquals(ExternalSchemes.syntheticSecurityId("USD 3m Libor"), ids.get(0));
  }

  @Test
  public void testMetalFutureSecurity() {
    final SecuritySource securitySource = ExposureFunctionTestHelper.getSecuritySource(null);
    final ExposureFunction exposureFunction = new UnderlyingExposureFunction(securitySource);
    final MetalFutureSecurity future = ExposureFunctionTestHelper.getMetalFutureSecurity();
    final List<ExternalId> ids = future.accept(exposureFunction);
    assertNull(ids);
  }

  @Test
  public void testStockFutureSecurity() {
    final SecuritySource securitySource = ExposureFunctionTestHelper.getSecuritySource(null);
    final ExposureFunction exposureFunction = new UnderlyingExposureFunction(securitySource);
    final StockFutureSecurity future = ExposureFunctionTestHelper.getStockFutureSecurity();
    final List<ExternalId> ids = future.accept(exposureFunction);
    assertNull(ids);
  }
}
