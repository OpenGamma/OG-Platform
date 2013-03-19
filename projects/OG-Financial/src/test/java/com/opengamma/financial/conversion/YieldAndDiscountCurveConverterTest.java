/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.conversion;

import static org.testng.AssertJUnit.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class YieldAndDiscountCurveConverterTest {
  
  private final YieldAndDiscountCurveConverter _converter = new YieldAndDiscountCurveConverter();
  
  @Test
  public void convertNonEmpty() {
    final Map<Double, Double> map = new HashMap<Double, Double>();
    map.put(1., 0.03);
    map.put(2., 0.04);
    map.put(3.5, 0.05);
    
    Map<String, Double> expected = new HashMap<String, Double>();
    expected.put("Foo[1.0]", 0.03);
    expected.put("Foo[2.0]", 0.04);
    expected.put("Foo[3.5]", 0.05);
    
    Map<String, Double> actual = _converter.convert("Foo", 
        YieldCurve.from(InterpolatedDoublesCurve.from(map, Interpolator1DFactory.getInterpolator("Linear"))));
    
    assertEquals(expected, actual);
  }

  @Test
  public void getConvertedClass() {
    assertEquals(YieldAndDiscountCurve.class, _converter.getConvertedClass());  
  }

}
