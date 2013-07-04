/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.util.test.TestGroup;

/**
 * Test for {@code YieldCurveBundleBuilder}
 */
@Test(groups = TestGroup.UNIT)
public class YieldCurveBundleBuilderTest extends AnalyticsTestBase {

  @Test
  public void test() {
    final DiscountCurve curve = new DiscountCurve("name1", ConstantDoublesCurve.from(1.234));
    final YieldCurveBundle bundle = new YieldCurveBundle();
    bundle.setCurve("name2", curve);
    final YieldCurveBundle bundle2 = cycleObject(YieldCurveBundle.class, bundle);
    assertEquals(bundle, bundle2);
  }
}
