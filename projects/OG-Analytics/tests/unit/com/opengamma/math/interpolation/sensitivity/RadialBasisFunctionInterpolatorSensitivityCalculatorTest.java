/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation.sensitivity;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.math.interpolation.GaussianRadialBasisFunction;
import com.opengamma.math.interpolation.InterpolatorND;
import com.opengamma.math.interpolation.InterpolatorNDTestCase;
import com.opengamma.math.interpolation.InverseMultiquadraticRadialBasisFunction;
import com.opengamma.math.interpolation.RadialBasisFunctionInterpolatorND;
import com.opengamma.math.interpolation.data.RadialBasisFunctionInterpolatorDataBundle;

/**
 * 
 */
public class RadialBasisFunctionInterpolatorSensitivityCalculatorTest extends InterpolatorNDTestCase{
  
  private static final RadialBasisFunctionInterpolatorNDSensitivityCalculator CAL = new RadialBasisFunctionInterpolatorNDSensitivityCalculator();
  
  @Test
  public void testFlat() {
    double r0 = 1.0;
    InterpolatorND<RadialBasisFunctionInterpolatorDataBundle> interpolator = new RadialBasisFunctionInterpolatorND(new GaussianRadialBasisFunction(r0), false);
    
    RadialBasisFunctionInterpolatorDataBundle dataBundle = interpolator.getDataBundle(FLAT_DATA);
    double[] point = FLAT_DATA.get(5).getFirst();
    Map<double[], Double> res = CAL.calculate(dataBundle, point);
    assertEquals(1.0,res.get(point),0.0);
    res.remove(point);
    
    for(Map.Entry<double[], Double> entry : res.entrySet()) {
      assertEquals(0.0,entry.getValue(),0.0);
    }    
  }
  
  @Test
  public void testInverseMultiquadraticRadialBasisFunction() {
    final double r0 = 2;
    final InterpolatorND<RadialBasisFunctionInterpolatorDataBundle> interpolator = new RadialBasisFunctionInterpolatorND(new InverseMultiquadraticRadialBasisFunction(r0), true);
    RadialBasisFunctionInterpolatorDataBundle dataBundle = interpolator.getDataBundle(COS_EXP_DATA);
    double[] point = COS_EXP_DATA.get(3).getFirst();
 //   System.out.println(point[0]+" "+point[1]);
    Map<double[], Double> res = CAL.calculate(dataBundle, point);
    assertEquals(1.0,res.get(point),1e-13);
    res.remove(point);
    
    for(Map.Entry<double[], Double> entry : res.entrySet()) {
    // System.out.println(entry.getKey()[0]+" "+entry.getKey()[1]+" "+entry.getValue());
       assertEquals(0.0,entry.getValue(),1e-11);
    }    
  }
}
