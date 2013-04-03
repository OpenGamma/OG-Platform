/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.financial.analytics.ircurve.BloombergFutureCurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.StaticCurveInstrumentProvider;
import com.opengamma.id.ExternalId;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CurveInstrumentProviderFudgeEncodingTest extends FinancialTestBase {

  @Test
  public void testStaticCurveInstrumentProvider() {
    final CurveInstrumentProvider cip = new StaticCurveInstrumentProvider(ExternalId.of("JIM", "BO"));
    assertEquals(cip, cycleObject(CurveInstrumentProvider.class, cip));
  }

  @Test
  public void testBloombergFutureCurveInstrumentProvider() {
    final CurveInstrumentProvider cip = new BloombergFutureCurveInstrumentProvider("ED", "Curncy");
    assertEquals(cip, cycleObject(CurveInstrumentProvider.class, cip));
  }

}
