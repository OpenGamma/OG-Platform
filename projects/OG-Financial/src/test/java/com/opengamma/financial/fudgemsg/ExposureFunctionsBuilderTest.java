/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.financial.analytics.curve.exposure.ExposureFunctions;
import com.opengamma.id.ExternalId;
import com.opengamma.util.test.TestGroup;

/**
 *
 */
@Test(groups = TestGroup.UNIT)
public class ExposureFunctionsBuilderTest extends FinancialTestBase {

  @Test
  public void test() {
    final String name = "Default";
    final List<String> exposureFunctions = Arrays.asList("Currency", "Security", "Security Type", "Region");
    final Map<ExternalId, String> idsToNames = new HashMap<>();
    idsToNames.put(ExternalId.of("SecurityType", "SWAP_USD"), "CurveConfig1");
    idsToNames.put(ExternalId.of("Currency", "USD"), "CurveConfig2");
    idsToNames.put(ExternalId.of("Region", "SWAP_US"), "CurveConfig3");
    final ExposureFunctions ef = new ExposureFunctions(name, exposureFunctions, idsToNames);
    assertEquals(ef, cycleObject(ExposureFunctions.class, ef));
  }
}
