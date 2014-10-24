/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.sensitivity.multicurve;

import java.util.Set;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * For an instrument, computes the sensitivity of a value (often the par spread) to the parameters used in the curve.
 * The computation is done by shifting each node point in each curve; the curves must be interpolated yield curves for discounting and forward curves.
 * The return format is SimpleParameterSensitivity object.
 * This is a very inefficient way to compute the sensitivities. It should be used only for tests purposes or when speed is irrelevant.
 */
public class SimpleParameterSensitivityMulticurveDiscountInterpolatedFDCalculator {

  /**
   * The value calculator.
   */
  private final InstrumentDerivativeVisitor<ParameterProviderInterface, Double> _valueCalculator;
  /**
   * The shift used for finite difference.
   */
  private final double _shift;

  /**
   * Constructor
   * @param valueCalculator The value calculator.
   * @param shift The shift used for finite difference.
   */
  public SimpleParameterSensitivityMulticurveDiscountInterpolatedFDCalculator(final InstrumentDerivativeVisitor<ParameterProviderInterface, Double> valueCalculator, final double shift) {
    ArgumentChecker.notNull(valueCalculator, "Calculator");
    _valueCalculator = valueCalculator;
    _shift = shift;
  }

  /**
   * Compute the sensitivity by finite difference on all points. The curves must be interpolated yield curves.
   * Only the discounting and forward curves sensitivity is computed.
   * @param instrument The instrument.
   * @param multicurve The market (all discounting and forward curves should be of the type YieldCurve with InterpolatedDoublesCurve.
   * @return The parameter sensitivity.
   */
  public SimpleParameterSensitivity calculateSensitivity(final InstrumentDerivative instrument, final MulticurveProviderDiscount multicurve) {
    SimpleParameterSensitivity result = new SimpleParameterSensitivity();
    // Discounting
    final Set<Currency> ccyDiscounting = multicurve.getCurrencies();
    for (final Currency ccy : ccyDiscounting) {
      final YieldAndDiscountCurve curve = multicurve.getCurve(ccy);
      ArgumentChecker.isTrue(curve instanceof YieldCurve, "Curve should be a YieldCurve");
      final YieldCurve curveYield = (YieldCurve) curve;
      ArgumentChecker.isTrue(curveYield.getCurve() instanceof InterpolatedDoublesCurve, "Yield curve should be based on InterpolatedDoublesCurve");
      final InterpolatedDoublesCurve curveInt = (InterpolatedDoublesCurve) curveYield.getCurve();
      final int nbNodePoint = curveInt.getXDataAsPrimitive().length;
      final double[] sensitivity = new double[nbNodePoint];
      for (int loopnode = 0; loopnode < nbNodePoint; loopnode++) {
        final double[] yieldBumpedPlus = curveInt.getYDataAsPrimitive().clone();
        yieldBumpedPlus[loopnode] += _shift;
        final YieldAndDiscountCurve dscBumpedPlus = new YieldCurve(curveInt.getName(), new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumpedPlus, curveInt.getInterpolator(), true));
        final MulticurveProviderDiscount marketDscBumpedPlus = multicurve.withDiscountFactor(ccy, dscBumpedPlus);
        final Double valueBumpedPlus = instrument.accept(_valueCalculator, marketDscBumpedPlus);
        final double[] yieldBumpedMinus = curveInt.getYDataAsPrimitive().clone();
        yieldBumpedMinus[loopnode] -= _shift;
        final YieldAndDiscountCurve dscBumpedMinus = new YieldCurve(curveInt.getName(), new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumpedMinus,
            curveInt.getInterpolator(), true));
        final MulticurveProviderDiscount marketDscBumpedMinus = multicurve.withDiscountFactor(ccy, dscBumpedMinus);
        final Double valueBumpedMinus = instrument.accept(_valueCalculator, marketDscBumpedMinus);
        final Double valueDiff = valueBumpedPlus - valueBumpedMinus;
        sensitivity[loopnode] = valueDiff / (2 * _shift);
      }
      final String name = multicurve.getName(ccy);
      result = result.plus(name, new DoubleMatrix1D(sensitivity));
    }
    // Forward ON
    final Set<IndexON> indexON = multicurve.getIndexesON();
    for (final IndexON index : indexON) {
      final YieldAndDiscountCurve curve = multicurve.getCurve(index);
      ArgumentChecker.isTrue(curve instanceof YieldCurve, "Curve should be a YieldCurve");
      final YieldCurve curveYield = (YieldCurve) curve;
      ArgumentChecker.isTrue(curveYield.getCurve() instanceof InterpolatedDoublesCurve, "Yield curve should be based on InterpolatedDoublesCurve");
      final InterpolatedDoublesCurve curveInt = (InterpolatedDoublesCurve) curveYield.getCurve();
      final int nbNodePoint = curveInt.getXDataAsPrimitive().length;
      final double[] sensitivity = new double[nbNodePoint];
      for (int loopnode = 0; loopnode < nbNodePoint; loopnode++) {
        final double[] yieldBumpedPlus = curveInt.getYDataAsPrimitive().clone();
        yieldBumpedPlus[loopnode] += _shift;
        final YieldAndDiscountCurve fwdBumpedPlus = new YieldCurve(curveInt.getName(), new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumpedPlus, curveInt.getInterpolator(), true));
        final MulticurveProviderDiscount marketFwdBumpedPlus = multicurve.withForward(index, fwdBumpedPlus);
        final Double valueBumpedPlus = instrument.accept(_valueCalculator, marketFwdBumpedPlus);
        final double[] yieldBumpedMinus = curveInt.getYDataAsPrimitive().clone();
        yieldBumpedMinus[loopnode] -= _shift;
        final YieldAndDiscountCurve fwdBumpedMinus = new YieldCurve(curveInt.getName(), new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumpedMinus,
            curveInt.getInterpolator(), true));
        final MulticurveProviderDiscount marketFwdBumpedMinus = multicurve.withForward(index, fwdBumpedMinus);
        final Double valueBumpedMinus = instrument.accept(_valueCalculator, marketFwdBumpedMinus);
        final Double valueDiff = valueBumpedPlus - valueBumpedMinus;
        sensitivity[loopnode] = valueDiff / (2 * _shift);
      }
      final String name = multicurve.getName(index);
      result = result.plus(name, new DoubleMatrix1D(sensitivity));
    }
    // Forward Ibor - symmetrical
    final Set<IborIndex> indexForward = multicurve.getIndexesIbor();
    for (final IborIndex index : indexForward) {
      final YieldAndDiscountCurve curve = multicurve.getCurve(index);
      ArgumentChecker.isTrue(curve instanceof YieldCurve, "Curve should be a YieldCurve");
      final YieldCurve curveYield = (YieldCurve) curve;
      ArgumentChecker.isTrue(curveYield.getCurve() instanceof InterpolatedDoublesCurve, "Yield curve should be based on InterpolatedDoublesCurve");
      final InterpolatedDoublesCurve curveInt = (InterpolatedDoublesCurve) curveYield.getCurve();
      final int nbNodePoint = curveInt.getXDataAsPrimitive().length;
      final double[] sensitivity = new double[nbNodePoint];
      for (int loopnode = 0; loopnode < nbNodePoint; loopnode++) {
        final double[] yieldBumpedPlus = curveInt.getYDataAsPrimitive().clone();
        yieldBumpedPlus[loopnode] += _shift;
        final YieldAndDiscountCurve fwdBumpedPlus = new YieldCurve(curveInt.getName(), new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumpedPlus, curveInt.getInterpolator(), true));
        final MulticurveProviderDiscount marketFwdBumpedPlus = multicurve.withForward(index, fwdBumpedPlus);
        final Double valueBumpedPlus = instrument.accept(_valueCalculator, marketFwdBumpedPlus);
        final double[] yieldBumpedMinus = curveInt.getYDataAsPrimitive().clone();
        yieldBumpedMinus[loopnode] -= _shift;
        final YieldAndDiscountCurve fwdBumpedMinus = new YieldCurve(curveInt.getName(), new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumpedMinus,
            curveInt.getInterpolator(), true));
        final MulticurveProviderDiscount marketFwdBumpedMinus = multicurve.withForward(index, fwdBumpedMinus);
        final Double valueBumpedMinus = instrument.accept(_valueCalculator, marketFwdBumpedMinus);
        final Double valueDiff = valueBumpedPlus - valueBumpedMinus;
        sensitivity[loopnode] = valueDiff / (2 * _shift);
      }
      final String name = multicurve.getName(index);
      result = result.plus(name, new DoubleMatrix1D(sensitivity));
    }
    return result;
  }
}
