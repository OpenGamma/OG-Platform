/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.DoubleLabelledMatrix2D;
import com.opengamma.financial.forex.calculator.ForexDerivative;
import com.opengamma.financial.forex.calculator.PresentValueForexVegaSensitivityCalculator;
import com.opengamma.financial.forex.method.PresentValueVolatilityNodeSensitivityDataBundle;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.DoubleMatrixUtils;

/**
 * 
 */
public class ForexVanillaOptionPresentValueVolatilitySensitivityFunction extends ForexVanillaOptionFunction {
  private static final PresentValueForexVegaSensitivityCalculator CALCULATOR = PresentValueForexVegaSensitivityCalculator.getInstance();

  public ForexVanillaOptionPresentValueVolatilitySensitivityFunction(final String putCurveName, final String callCurveName, final String surfaceName) {
    super(putCurveName, callCurveName, surfaceName, ValueRequirementNames.FX_VOLATILITY_SENSITIVITIES);
  }

  @Override
  protected Object getResult(final ForexDerivative fxOption, final SmileDeltaTermStructureDataBundle data) {
    PresentValueVolatilityNodeSensitivityDataBundle result = CALCULATOR.visit(fxOption, data);
    DoubleMatrix2D m = DoubleMatrixUtils.getTranspose(result.getVega());
    int columns = m.getNumberOfColumns();
    int rows = m.getNumberOfRows();    
    Double[] columnValues = new Double[columns];
    Double[] rowValues = new Double[rows];
    for (int i = 0; i < columns; i++) {
      columnValues[i] = result.getExpiries().getEntry(i);
    }
    for (int i = 0; i < columns; i++) {
      rowValues[i] = result.getStrikes().getEntry(i);
    }
    return new DoubleLabelledMatrix2D(columnValues, rowValues, m.getData()); //TODO use proper labels
  }
}
