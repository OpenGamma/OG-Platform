/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve.exposure;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class SecurityExposureFunctionTest {

  @Test
  public void testCash() {
    final ExposureFunction exposureFunction = new SecurityExposureFunction();
    final CashSecurity cash = ExposureFunctionTestHelper.getCash();
    final List<ExternalId> ids = cash.accept(exposureFunction);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(cash.getUniqueId().getScheme(), cash.getUniqueId().getValue()), ids.get(0));
  }

  @Test
  public void testFRA() {
    final ExposureFunction exposureFunction = new SecurityExposureFunction();
    final FRASecurity fra = ExposureFunctionTestHelper.getFRA();
    final List<ExternalId> ids = fra.accept(exposureFunction);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(fra.getUniqueId().getScheme(), fra.getUniqueId().getValue()), ids.get(0));
  }
}
