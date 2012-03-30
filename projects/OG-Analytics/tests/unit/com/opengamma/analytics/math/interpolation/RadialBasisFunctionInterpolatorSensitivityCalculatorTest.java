/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.interpolation.GaussianRadialBasisFunction;
import com.opengamma.analytics.math.interpolation.InterpolatorND;
import com.opengamma.analytics.math.interpolation.InverseMultiquadraticRadialBasisFunction;
import com.opengamma.analytics.math.interpolation.RadialBasisFunctionInterpolatorND;
import com.opengamma.analytics.math.interpolation.data.InterpolatorNDDataBundle;

/**
 * 
 */
public class RadialBasisFunctionInterpolatorSensitivityCalculatorTest extends InterpolatorNDTestCase{
  
  @Test
  public void testFlat() {
    double r0 = 1.0;
    InterpolatorND interpolator = new RadialBasisFunctionInterpolatorND(new GaussianRadialBasisFunction(r0), false);
    
    InterpolatorNDDataBundle dataBundle = interpolator.getDataBundle(FLAT_DATA);
    double[] point = FLAT_DATA.get(5).getFirst();
    Map<double[], Double> res = interpolator.getNodeSensitivitiesForValue(dataBundle, point);
    assertEquals(1.0,res.get(point),0.0);
    res.remove(point);
    
    for(Map.Entry<double[], Double> entry : res.entrySet()) {
      assertEquals(0.0,entry.getValue(),0.0);
    }    
  }
  
  @Test
  public void testInverseMultiquadraticRadialBasisFunction() {
    final double r0 = 2;
    final InterpolatorND interpolator = new RadialBasisFunctionInterpolatorND(new InverseMultiquadraticRadialBasisFunction(r0), true);
    InterpolatorNDDataBundle dataBundle = interpolator.getDataBundle(COS_EXP_DATA);
    double[] point = COS_EXP_DATA.get(3).getFirst();
    Map<double[], Double> res = interpolator.getNodeSensitivitiesForValue(dataBundle, point);
    assertEquals(1.0,res.get(point),1e-13);
    res.remove(point);
    
    for(Map.Entry<double[], Double> entry : res.entrySet()) {
       assertEquals(0.0,entry.getValue(),1e-11);
    }    
  }
}
