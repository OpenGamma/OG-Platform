/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.text.DecimalFormat;

import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.DoubleLabelledMatrix2D;
import com.opengamma.financial.forex.calculator.ForexDerivative;
import com.opengamma.financial.forex.calculator.PresentValueForexVegaSensitivityCalculator;
import com.opengamma.financial.forex.method.PresentValueVolatilityNodeSensitivityDataBundle;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class ForexVanillaOptionVegaFunction extends ForexVanillaOptionFunction {
  private static final PresentValueForexVegaSensitivityCalculator CALCULATOR = PresentValueForexVegaSensitivityCalculator.getInstance();
  private static final DecimalFormat STRIKE_FORMATTER = new DecimalFormat("###.#####");

  public ForexVanillaOptionVegaFunction(final String putCurveName, final String callCurveName, final String surfaceName) {
    super(putCurveName, callCurveName, surfaceName, ValueRequirementNames.VEGA_MATRIX);
  }

  @Override
  protected Object getResult(final ForexDerivative fxOption, final SmileDeltaTermStructureDataBundle data) {
    final PresentValueVolatilityNodeSensitivityDataBundle result = CALCULATOR.visit(fxOption, data);
    final double[] expiries = result.getExpiries().getData();
    final double[] strikes = result.getStrikes().getData();
    final double[][] vega = result.getVega().getData();
    final int nStrikes = strikes.length;
    final int nExpiries = expiries.length;
    final Double[] rowValues = new Double[nExpiries];
    final String[] rowLabels = new String[nExpiries];
    final Double[] columnValues = new Double[nStrikes];
    final String[] columnLabels = new String[nStrikes];
    final double[][] values = new double[nStrikes][nExpiries];
    for (int i = 0; i < nStrikes; i++) {
      columnValues[i] = strikes[i];
      columnLabels[i] = getFormattedStrike(strikes[i], result.getCurrencyPair());
      for (int j = 0; j < nExpiries; j++) {
        if (i == 0) {
          rowValues[j] = expiries[j];
          rowLabels[j] = getFormattedExpiry(expiries[j]);
        }
        values[i][j] = vega[j][i];
      }
    }
    return new DoubleLabelledMatrix2D(rowValues, rowLabels, columnValues, columnLabels, values);
  }

  private String getFormattedStrike(final double strike, final Pair<Currency, Currency> pair) {
    return STRIKE_FORMATTER.format(1. / strike) + " " + pair.getSecond() + "/" + pair.getFirst();
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
