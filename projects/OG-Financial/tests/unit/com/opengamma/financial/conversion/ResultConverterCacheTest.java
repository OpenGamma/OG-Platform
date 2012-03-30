/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.conversion;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.timeseries.date.ArrayDateDoubleTimeSeries;

/**
 * 
 */
public class ResultConverterCacheTest {
  
  @Test
  public void get() {
    ResultConverterCache cache = new ResultConverterCache();
    
    ResultConverter<?> converter = cache.getConverter(new Double(5.5));
    assertNotNull(converter);
    assertTrue(converter instanceof DoubleConverter);
    
    converter = cache.getConverter(new DoubleMatrix1D(new double[0]));
    assertNotNull(converter);
    assertTrue(converter instanceof DoubleMatrix1DConverter);
    
    converter = cache.getConverter(new DoubleMatrix2D(new double[0][0]));
    assertNotNull(converter);
    assertTrue(converter instanceof DoubleMatrix2DConverter);
    
    converter = cache.getConverter(new ArrayDateDoubleTimeSeries());
    assertNotNull(converter);
    assertTrue(converter instanceof TimeSeriesConverter);
    
    converter = cache.getConverter(new DiscountCurve(new ConstantDoublesCurve(2.5)));
    assertNotNull(converter);
    assertTrue(converter instanceof YieldAndDiscountCurveConverter);
  }

}
