/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.forex.calculator.ForexDerivative;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureDataBundle;

/**
 * 
 */
public class ForexVanillaOptionVegaFunction extends ForexVanillaOptionFunction {

  public ForexVanillaOptionVegaFunction(String putCurveName, String callCurveName, String surfaceName) {
    super(putCurveName, callCurveName, surfaceName, "VEGA_MATRIX"); //TODO use value requirement name
  }

  @Override
  protected Object getResult(ForexDerivative fxOption, SmileDeltaTermStructureDataBundle data) {
    return null;
  }

}
