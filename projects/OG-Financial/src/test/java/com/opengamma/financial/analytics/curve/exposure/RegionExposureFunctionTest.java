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
import com.opengamma.id.ExternalId;
import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class RegionExposureFunctionTest {

  @Test
  public void testCash() {
    final SecuritySource securitySource = ExposureFunctionTestHelper.getSecuritySource(null);
    final ExposureFunction regionExposureFunction = new RegionExposureFunction(securitySource);
    final CashSecurity cash = ExposureFunctionTestHelper.getCash();
    final List<ExternalId> ids = cash.accept(regionExposureFunction);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of("Test", "US"), ids.get(0));
  }
}
