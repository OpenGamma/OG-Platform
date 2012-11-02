/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.sensitivity;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.analytics.financial.curve.sensitivity.ParameterSensitivity;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.market.description.CurveSensitivityMarket;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderInterface;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * For an instrument, computes the sensitivity of double value (often par spread) to the parameters used in the curve.
 * The meaning of "parameters" will depend of the way the curve is stored (interpolated yield, function parameters, etc.).
 * The return format is DoubleMatrix1D object.
 */
public class ParameterSensitivityMatrixProviderCalculator extends AbstractParameterSensitivityMatrixProviderCalculator {

  /**
   * Constructor
   * @param curveSensitivityCalculator The curve sensitivity calculator.
   */
  public ParameterSensitivityMatrixProviderCalculator(InstrumentDerivativeVisitor<MulticurveProviderInterface, CurveSensitivityMarket> curveSensitivityCalculator) {
    super(curveSensitivityCalculator);
  }

  /**
   * Computes the sensitivity with respect to the parameters from the point sensitivities.
   * @param sensitivity The point sensitivity.
   * @param fixedCurves The fixed curves names (for which the parameter sensitivity are not computed even if they are necessary for the instrument pricing).
   * The curve in the list may or may not be in the bundle. Not null.
   * @param multicurve The multi-curve provider. Not null.
   * @return The sensitivity (as a Matrix). The order of the sensitivity is by curve as provided by the getAllNames method of the MulticurveProviderInterface.
   */
  @Override
  public DoubleMatrix1D pointToParameterSensitivity(final CurveSensitivityMarket sensitivity, final Set<String> fixedCurves, final MulticurveProviderInterface multicurve) {
    Currency ccy = Currency.AUD;
    // TODO: the currency is there only for using the ParameterSensitivity object. This should be changed.
    ParameterSensitivity ps = new ParameterSensitivity();
    // Discounting
    Map<String, List<DoublesPair>> sensitivityDsc = sensitivity.getYieldDiscountingSensitivities();
    Set<String> curvesNamesDsc = sensitivityDsc.keySet();
    for (final Currency ccyMarket : multicurve.getCurrencies()) {
      String name = multicurve.getName(ccyMarket);
      if ((!fixedCurves.contains(name)) && curvesNamesDsc.contains(name)) {
        ps = ps.plus(new ObjectsPair<String, Currency>(name, ccy), new DoubleMatrix1D(multicurve.parameterSensitivity(ccyMarket, sensitivityDsc.get(name))));
      }
    }
    // Forward ON
    Map<String, List<ForwardSensitivity>> sensitivityFwd = sensitivity.getForwardSensitivities();
    Set<String> curvesNamesFwd = sensitivityFwd.keySet();
    for (final IndexON index : multicurve.getIndexesON()) {
      String name = multicurve.getName(index);
      if ((!fixedCurves.contains(name)) && curvesNamesFwd.contains(name)) {
        ps = ps.plus(new ObjectsPair<String, Currency>(name, ccy), new DoubleMatrix1D(multicurve.parameterSensitivity(index, sensitivityFwd.get(name))));
      }
    }
    // Forward Ibor
    for (final IborIndex index : multicurve.getIndexesIbor()) {
      String name = multicurve.getName(index);
      if ((!fixedCurves.contains(name)) && curvesNamesFwd.contains(name)) {
        ps = ps.plus(new ObjectsPair<String, Currency>(name, ccy), new DoubleMatrix1D(multicurve.parameterSensitivity(index, sensitivityFwd.get(name))));
      }
    }
    // By curve name
    double[] result = new double[0];
    for (String name : multicurve.getAllNames()) {
      if (!fixedCurves.contains(name)) {
        DoubleMatrix1D sensi = ps.getSensitivity(name, ccy);
        if (sensi != null) {
          result = ArrayUtils.addAll(result, sensi.getData());
        } else {
          result = ArrayUtils.addAll(result, new double[multicurve.getNumberOfParameters(name)]);
        }
      }
    }
    return new DoubleMatrix1D(result);
  }

}
