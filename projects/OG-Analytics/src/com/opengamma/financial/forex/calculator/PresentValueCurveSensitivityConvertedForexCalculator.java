/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.calculator;

import com.opengamma.financial.forex.method.MultipleCurrencyInterestRateCurveSensitivity;
import com.opengamma.financial.forex.method.YieldCurveWithCcyBundle;
import com.opengamma.financial.forex.method.YieldCurveWithFXBundle;
import com.opengamma.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.InterestRateCurveSensitivityUtils;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Calculator of the present value curve sensitivity for Forex derivatives with all the results converted in the currency of the curve using the relevant exchange rates.
 * The relevant exchange rates should be available in the data (YieldCurveWithFXBundle).
 */
public class PresentValueCurveSensitivityConvertedForexCalculator extends AbstractInstrumentDerivativeVisitor<YieldCurveBundle, InterestRateCurveSensitivity> {

  /**
   * The method unique instance.
   */
  private static final PresentValueCurveSensitivityConvertedForexCalculator INSTANCE = new PresentValueCurveSensitivityConvertedForexCalculator();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static PresentValueCurveSensitivityConvertedForexCalculator getInstance() {
    return INSTANCE;
  }

  private final PresentValueCurveSensitivityForexCalculator _pvcsc;

  /**
   * Constructor.
   */
  PresentValueCurveSensitivityConvertedForexCalculator() {
    _pvcsc = PresentValueCurveSensitivityForexCalculator.getInstance();
  }

  /**
   * Constructor.
   * @param pvcsc The present value curve sensitivity calculator (not converted). Not null.
   */
  public PresentValueCurveSensitivityConvertedForexCalculator(final PresentValueCurveSensitivityForexCalculator pvcsc) {
    ArgumentChecker.notNull(pvcsc, "present value curve sensitivity calculator");
    _pvcsc = pvcsc;
  }

  @Override
  public InterestRateCurveSensitivity visit(final InstrumentDerivative derivative, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.notNull(derivative, "derivative");
    ArgumentChecker.isTrue(curves instanceof YieldCurveWithCcyBundle, "FX Conversion can be operated only when the curve currency is indicated.");
    final YieldCurveWithCcyBundle curvesCcy = (YieldCurveWithCcyBundle) curves;
    final MultipleCurrencyInterestRateCurveSensitivity pvcsMulti = _pvcsc.visit(derivative, curvesCcy);
    InterestRateCurveSensitivity result = new InterestRateCurveSensitivity();
    for (final Currency ccy : pvcsMulti.getCurrencies()) {
      final InterestRateCurveSensitivity pvcs = pvcsMulti.getSensitivity(ccy);
      for (final String curve : pvcs.getCurves()) {
        if (curvesCcy.getCcyMap().get(curve) == ccy) { // Identical currencies: no changes
          result = result.plus(curve, pvcs.getSensitivities().get(curve));
        } else { // Different currencies: exchange rate multiplication.
          ArgumentChecker.isTrue(curves instanceof YieldCurveWithFXBundle, "FX Conversion can be operated only if exchange rates are available.");
          final YieldCurveWithFXBundle curveFx = (YieldCurveWithFXBundle) curvesCcy;
          final double fxRate = curveFx.getFxRate(curvesCcy.getCcyMap().get(curve), ccy);
          result = result.plus(curve, InterestRateCurveSensitivityUtils.multiplySensitivity(pvcs.getSensitivities().get(curve), fxRate));
        }
      }
    }
    return result;
  }

}
