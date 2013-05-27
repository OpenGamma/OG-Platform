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
import com.opengamma.id.ExternalId;
import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class ContractCategoryExposureFunctionTest {

  @Test
  public void testAgriculturalFutureSecurity() {
    final ExposureFunction exposureFunction = new ContractCategoryExposureFunction(ExposureFunctionTestHelper.getSecuritySource(null));
    final AgricultureFutureSecurity future = ExposureFunctionTestHelper.getAgricultureFutureSecurity();
    final List<ExternalId> ids = future.accept(exposureFunction);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ExposureFunction.CONTRACT_IDENTIFIER, "Commodity"), ids.get(0));
  }

  @Test
  public void testBondFutureSecurity() {
    final ExposureFunction exposureFunction = new ContractCategoryExposureFunction(ExposureFunctionTestHelper.getSecuritySource(null));
    final BondFutureSecurity future = ExposureFunctionTestHelper.getBondFutureSecurity();
    final List<ExternalId> ids = future.accept(exposureFunction);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ExposureFunction.CONTRACT_IDENTIFIER, "Financial"), ids.get(0));
  }

  @Test
  public void testCapFloorCMSSpreadSecurity() {
    final ExposureFunction exposureFunction = new ContractCategoryExposureFunction(ExposureFunctionTestHelper.getSecuritySource(null));
    final CapFloorCMSSpreadSecurity security = ExposureFunctionTestHelper.getCapFloorCMSSpreadSecurity();
    final List<ExternalId> ids = security.accept(exposureFunction);
    assertNull(ids);
  }

  @Test
  public void testCapFloorSecurity() {
    final ExposureFunction exposureFunction = new ContractCategoryExposureFunction(ExposureFunctionTestHelper.getSecuritySource(null));
    final CapFloorSecurity security = ExposureFunctionTestHelper.getCapFloorSecurity();
    final List<ExternalId> ids = security.accept(exposureFunction);
    assertNull(ids);
  }

  @Test
  public void testCashFlowSecurity() {
    final ExposureFunction exposureFunction = new ContractCategoryExposureFunction(ExposureFunctionTestHelper.getSecuritySource(null));
    final CashFlowSecurity security = ExposureFunctionTestHelper.getCashFlowSecurity();
    final List<ExternalId> ids = security.accept(exposureFunction);
    assertNull(ids);
  }

  @Test
  public void testCashSecurity() {
    final ExposureFunction exposureFunction = new ContractCategoryExposureFunction(ExposureFunctionTestHelper.getSecuritySource(null));
    final CashSecurity cash = ExposureFunctionTestHelper.getCashSecurity();
    final List<ExternalId> ids = cash.accept(exposureFunction);
    assertNull(ids);
  }

  @Test
  public void testEnergyFutureSecurity() {
    final ExposureFunction exposureFunction = new ContractCategoryExposureFunction(ExposureFunctionTestHelper.getSecuritySource(null));
    final EnergyFutureSecurity future = ExposureFunctionTestHelper.getEnergyFutureSecurity();
    final List<ExternalId> ids = future.accept(exposureFunction);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ExposureFunction.CONTRACT_IDENTIFIER, "Energy"), ids.get(0));
  }

  @Test
  public void testEquityFutureSecurity() {
    final ExposureFunction exposureFunction = new ContractCategoryExposureFunction(ExposureFunctionTestHelper.getSecuritySource(null));
    final EquityFutureSecurity future = ExposureFunctionTestHelper.getEquityFutureSecurity();
    final List<ExternalId> ids = future.accept(exposureFunction);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ExposureFunction.CONTRACT_IDENTIFIER, "Equity"), ids.get(0));
  }

  @Test
  public void testEquityIndexDividendFutureSecurity() {
    final ExposureFunction exposureFunction = new ContractCategoryExposureFunction(ExposureFunctionTestHelper.getSecuritySource(null));
    final EquityIndexDividendFutureSecurity future = ExposureFunctionTestHelper.getEquityIndexDividendFutureSecurity();
    final List<ExternalId> ids = future.accept(exposureFunction);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ExposureFunction.CONTRACT_IDENTIFIER, "Equity Index"), ids.get(0));
  }

  @Test
  public void testFRASecurity() {
    final ExposureFunction exposureFunction = new ContractCategoryExposureFunction(ExposureFunctionTestHelper.getSecuritySource(null));
    final FRASecurity fra = ExposureFunctionTestHelper.getFRASecurity();
    final List<ExternalId> ids = fra.accept(exposureFunction);
    assertNull(ids);
  }

  @Test
  public void testFXFutureSecurity() {
    final ExposureFunction exposureFunction = new ContractCategoryExposureFunction(ExposureFunctionTestHelper.getSecuritySource(null));
    final FXFutureSecurity future = ExposureFunctionTestHelper.getFXFutureSecurity();
    final List<ExternalId> ids = future.accept(exposureFunction);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ExposureFunction.CONTRACT_IDENTIFIER, "Currency"), ids.get(0));
  }

  @Test
  public void testIndexFutureSecurity() {
    final ExposureFunction exposureFunction = new ContractCategoryExposureFunction(ExposureFunctionTestHelper.getSecuritySource(null));
    final IndexFutureSecurity future = ExposureFunctionTestHelper.getIndexFutureSecurity();
    final List<ExternalId> ids = future.accept(exposureFunction);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ExposureFunction.CONTRACT_IDENTIFIER, "Equity Index"), ids.get(0));
  }

  @Test
  public void testInterestRateFutureSecurity() {
    final ExposureFunction exposureFunction = new ContractCategoryExposureFunction(ExposureFunctionTestHelper.getSecuritySource(null));
    final InterestRateFutureSecurity future = ExposureFunctionTestHelper.getInterestRateFutureSecurity();
    final List<ExternalId> ids = future.accept(exposureFunction);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ExposureFunction.CONTRACT_IDENTIFIER, "Financial"), ids.get(0));
  }

  @Test
  public void testMetalFutureSecurity() {
    final ExposureFunction exposureFunction = new ContractCategoryExposureFunction(ExposureFunctionTestHelper.getSecuritySource(null));
    final MetalFutureSecurity future = ExposureFunctionTestHelper.getMetalFutureSecurity();
    final List<ExternalId> ids = future.accept(exposureFunction);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ExposureFunction.CONTRACT_IDENTIFIER, "Commodity"), ids.get(0));
  }

  @Test
  public void testStockFutureSecurity() {
    final ExposureFunction exposureFunction = new ContractCategoryExposureFunction(ExposureFunctionTestHelper.getSecuritySource(null));
    final StockFutureSecurity future = ExposureFunctionTestHelper.getStockFutureSecurity();
    final List<ExternalId> ids = future.accept(exposureFunction);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ExposureFunction.CONTRACT_IDENTIFIER, "Equity"), ids.get(0));
  }
}
