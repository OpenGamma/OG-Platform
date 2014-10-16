/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve.inflation.generator;

import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurveSimple;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderInterface;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class GeneratorPriceIndexCurveConstant extends GeneratorPriceIndexCurve {

  @Override
  public int getNumberOfParameter() {
    return 1;
  }

  @Override
  public PriceIndexCurveSimple generateCurve(String name, double[] parameters) {
    ArgumentChecker.isTrue(parameters.length == 1, "Constant curve should have one parameter");
    return new PriceIndexCurveSimple(new ConstantDoublesCurve(parameters[0], name));
  }

  @Override
  public PriceIndexCurveSimple generateCurve(String name, InflationProviderInterface inflation, double[] parameters) {
    return generateCurve(name, parameters);
  }

}
