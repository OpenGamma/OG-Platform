/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.callspreadblack;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.forex.calculator.PresentValueBlackVolatilityNodeSensitivityCallSpreadBlackForexCalculator;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilityNodeSensitivityDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.DoubleLabelledMatrix2D;

/**
 * The function calculating the Black volatility sensitivity to the matrix with volatility data.
 */
public class ForexDigitalOptionCallSpreadBlackVegaMatrixFunction extends ForexDigitalOptionCallSpreadBlackSingleValuedFunction {

  private static final DecimalFormat DELTA_FORMATTER = new DecimalFormat("##");

  public ForexDigitalOptionCallSpreadBlackVegaMatrixFunction() {
    super(ValueRequirementNames.VEGA_MATRIX);
  }

  @Override
  protected Set<ComputedValue> getResult(final ForexOptionDigital fxDigital, final double spread, final SmileDeltaTermStructureDataBundle data, final ValueSpecification spec) {
    final PresentValueBlackVolatilityNodeSensitivityCallSpreadBlackForexCalculator calculator = new PresentValueBlackVolatilityNodeSensitivityCallSpreadBlackForexCalculator(spread);
    final PresentValueForexBlackVolatilityNodeSensitivityDataBundle result = calculator.visit(fxDigital, data);
    final double[] expiries = result.getExpiries().getData();
    final double[] delta = result.getDelta().getData();
    final double[][] vega = result.getVega().getData();
    final int nDelta = delta.length;
    final int nExpiries = expiries.length;
    final Double[] rowValues = new Double[nExpiries];
    final String[] rowLabels = new String[nExpiries];
    final Double[] columnValues = new Double[nDelta];
    final String[] columnLabels = new String[nDelta];
    final double[][] values = new double[nDelta][nExpiries];
    for (int i = 0; i < nDelta; i++) {
      columnValues[i] = delta[i];
      columnLabels[i] = "P" + DELTA_FORMATTER.format(delta[i] * 100) + " " + result.getCurrencyPair().getFirst() + "/" + result.getCurrencyPair().getSecond();
      for (int j = 0; j < nExpiries; j++) {
        if (i == 0) {
          rowValues[j] = expiries[j];
          rowLabels[j] = getFormattedExpiry(expiries[j]);
        }
        values[i][j] = vega[j][i];
      }
    }
    return Collections.singleton(new ComputedValue(spec, new DoubleLabelledMatrix2D(rowValues, rowLabels, columnValues, columnLabels, values)));
  }

  private String getFormattedExpiry(final double expiry) {
    if (expiry < 1. / 54) {
      final int days = (int) Math.ceil((365 * expiry));
      return days + "D";
    }
    if (expiry < 1. / 13) {
      final int weeks = (int) Math.ceil((52 * expiry));
      return weeks + "W";
    }
    if (expiry < 0.95) {
      final int months = (int) Math.ceil((12 * expiry));
      return months + "M";
    }
    return ((int) Math.ceil(expiry)) + "Y";
  }
}
