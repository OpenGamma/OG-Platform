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
public class SecurityTypeExposureFunctionTest {

  @Test
  public void testCash() {
    final ExposureFunction exposureFunction = new SecurityTypeExposureFunction();
    final CashSecurity cash = ExposureFunctionTestHelper.getCash();
    final List<ExternalId> ids = cash.accept(exposureFunction);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ExposureFunction.SECURITY_IDENTIFIER, cash.getSecurityType()), ids.get(0));
  }

  @Test
  public void testFRA() {
    final ExposureFunction exposureFunction = new SecurityTypeExposureFunction();
    final FRASecurity fra = ExposureFunctionTestHelper.getFRA();
    final List<ExternalId> ids = fra.accept(exposureFunction);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ExposureFunction.SECURITY_IDENTIFIER, fra.getSecurityType()), ids.get(0));
  }
}
