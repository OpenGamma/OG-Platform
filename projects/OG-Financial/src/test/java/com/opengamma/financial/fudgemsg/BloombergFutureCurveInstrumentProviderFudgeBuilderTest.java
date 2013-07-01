/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.financial.analytics.fudgemsg.AnalyticsTestBase;
import com.opengamma.financial.analytics.ircurve.BloombergFutureCurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
import com.opengamma.util.test.TestGroup;

/**
 *
 */
@Test(groups = TestGroup.UNIT)
public class BloombergFutureCurveInstrumentProviderFudgeBuilderTest extends AnalyticsTestBase {

  @Test
  public void test() {
    BloombergFutureCurveInstrumentProvider provider = new BloombergFutureCurveInstrumentProvider("AB", "Sector");
    assertEquals(provider, cycleObject(BloombergFutureCurveInstrumentProvider.class, provider));
    provider = new BloombergFutureCurveInstrumentProvider("AB", "Sector", "LAST_CLOSE", DataFieldType.POINTS);
    assertEquals(provider, cycleObject(BloombergFutureCurveInstrumentProvider.class, provider));
  }
}
