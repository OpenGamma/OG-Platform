/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.cds;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.opengamma.util.money.CurrencyAmount;

/**
 * Test the simple CDS pricining method
 */
public class CDSSimpleMethodTest extends CDSTestSetup {

  @Test
  public void testPresetValue() {
    final CDSSimpleMethod method = new CDSSimpleMethod();
    //final CurrencyAmount result = method.presentValue(cds, curveBundle);

    final CurrencyAmount result = method.presentValue(_simpleTestCDS, _simpleTestCurveBundle);

    Assert.assertEquals(result.getAmount(), 0.06281112880507082);

  }
}
