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
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class CurrencyExposureFunctionTest {

  @Test
  public void testCash() {
    final SecuritySource securitySource = ExposureFunctionTestHelper.getSecuritySource(null);
    final ExposureFunction currencyExposureFunction = new CurrencyExposureFunction(securitySource);
    final CashSecurity cash = ExposureFunctionTestHelper.getCash();
    final List<ExternalId> ids = cash.accept(currencyExposureFunction);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(Currency.USD.getObjectId().getScheme(), "USD"), ids.get(0));
  }

  @Test
  public void testFRA() {
    final SecuritySource securitySource = ExposureFunctionTestHelper.getSecuritySource(null);
    final ExposureFunction currencyExposureFunction = new CurrencyExposureFunction(securitySource);
    final FRASecurity fra = ExposureFunctionTestHelper.getFRA();
    final List<ExternalId> ids = fra.accept(currencyExposureFunction);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(Currency.USD.getObjectId().getScheme(), "USD"), ids.get(0));
  }
}
