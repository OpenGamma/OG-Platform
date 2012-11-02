/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.sensitivity;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opengamma.analytics.financial.curve.sensitivity.ParameterSensitivity;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.market.description.MultipleCurrencyCurveSensitivityMarket;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderInterface;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * For an instrument, computes the sensitivity of a value (often the present value or a par spread) to the parameters used in the curve.
 * The meaning of "parameters" will depend of the way the curve is stored (interpolated yield, function parameters, etc.).
 * The return format is ParameterSensitivity object.
 */
public class ParameterSensitivityProviderCalculator extends AbstractParameterSensitivityProviderCalculator {

  /**
   * Constructor
   * @param curveSensitivityCalculator The curve sensitivity calculator.
   */
  public ParameterSensitivityProviderCalculator(InstrumentDerivativeVisitor<MulticurveProviderInterface, MultipleCurrencyCurveSensitivityMarket> curveSensitivityCalculator) {
    super(curveSensitivityCalculator);
  }

  /**
   * Computes the sensitivity with respect to the parameters from the point sensitivities to the continuously compounded rate.
   * @param sensitivity The point sensitivity.
   * @param fixedCurves The fixed curves names (for which the parameter sensitivity are not computed even if they are necessary for the instrument pricing).
   * The curve in the list may or may not be in the bundle. Not null.
   * @param multicurve The multi-curve provider. Not null.
   * @return The sensitivity (as a ParameterSensitivity).
   */
  @Override
  public ParameterSensitivity pointToParameterSensitivity(final MultipleCurrencyCurveSensitivityMarket sensitivity, final Set<String> fixedCurves, final MulticurveProviderInterface multicurve) {
    ParameterSensitivity result = new ParameterSensitivity();
    // Discounting
    for (Currency ccySensi : sensitivity.getCurrencies()) {
      Map<String, List<DoublesPair>> sensitivityDsc = sensitivity.getSensitivity(ccySensi).getYieldDiscountingSensitivities();
      Set<String> curvesNames = sensitivityDsc.keySet();
      for (final Currency ccyMarket : multicurve.getCurrencies()) {
        String name = multicurve.getName(ccyMarket);
        if ((!fixedCurves.contains(name)) && curvesNames.contains(name)) {
          result = result.plus(new ObjectsPair<String, Currency>(name, ccySensi), new DoubleMatrix1D(multicurve.parameterSensitivity(ccyMarket, sensitivityDsc.get(name))));
        }
      }
    }
    // Forward ON/Ibor
    for (Currency ccySensi : sensitivity.getCurrencies()) {
      Map<String, List<ForwardSensitivity>> sensitivityFwd = sensitivity.getSensitivity(ccySensi).getForwardSensitivities();
      Set<String> curvesNames = sensitivityFwd.keySet();
      for (final IndexON index : multicurve.getIndexesON()) {
        String name = multicurve.getName(index);
        if ((!fixedCurves.contains(name)) && curvesNames.contains(name)) {
          result = result.plus(new ObjectsPair<String, Currency>(name, ccySensi), new DoubleMatrix1D(multicurve.parameterSensitivity(index, sensitivityFwd.get(name))));
        }
      }
      for (final IborIndex index : multicurve.getIndexesIbor()) {
        String name = multicurve.getName(index);
        if ((!fixedCurves.contains(name)) && curvesNames.contains(name)) {
          result = result.plus(new ObjectsPair<String, Currency>(name, ccySensi), new DoubleMatrix1D(multicurve.parameterSensitivity(index, sensitivityFwd.get(name))));
        }
      }
    }
    return result;
  }
}
