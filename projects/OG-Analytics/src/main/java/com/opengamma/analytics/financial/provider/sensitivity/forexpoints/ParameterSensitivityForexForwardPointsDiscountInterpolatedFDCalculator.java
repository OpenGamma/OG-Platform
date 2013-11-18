/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.sensitivity.forexpoints;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveForwardPointsProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveForwardPointsProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.Pairs;

/**
 * For an instrument, computes the sensitivity of a value (often the present value or a par spread) to the parameters used in the curve.
 * The computation is done by shifting each node point in each curve; the curves must be interpolated yield curves for discounting and forward curves.
 * The return format is ParameterSensitivity object.
 * This is a very inefficient way to compute the sensitivities. It should be used only for tests purposes or when speed is irrelevant.
 */
public class ParameterSensitivityForexForwardPointsDiscountInterpolatedFDCalculator {

  /**
   * The value calculator.
   */
  private final InstrumentDerivativeVisitor<MulticurveForwardPointsProviderInterface, MultipleCurrencyAmount> _valueCalculator;
  /**
   * The shift used for finite difference.
   */
  private final double _shift;

  /**
   * Constructor
   * @param valueCalculator The value calculator.
   * @param shift The shift used for finite difference.
   */
  public ParameterSensitivityForexForwardPointsDiscountInterpolatedFDCalculator(final InstrumentDerivativeVisitor<MulticurveForwardPointsProviderInterface, MultipleCurrencyAmount> valueCalculator,
      final double shift) {
    ArgumentChecker.notNull(valueCalculator, "Calculator");
    _valueCalculator = valueCalculator;
    _shift = shift;
  }

  /**
   * Compute the sensitivity by finite difference on all points. The curves must be interpolated yield curves.
   * Only the discounting and forward curves sensitivity is computed.
   * @param instrument The instrument.
   * @param multicurveFwdpoints The multi-curve and forward points provider.
   * @return The parameter sensitivity.
   */
  public MultipleCurrencyParameterSensitivity calculateSensitivity(final InstrumentDerivative instrument, final MulticurveForwardPointsProviderDiscount multicurveFwdpoints) {
    MultipleCurrencyParameterSensitivity result = new MultipleCurrencyParameterSensitivity();
    final MultipleCurrencyAmount pvInit = instrument.accept(_valueCalculator, multicurveFwdpoints);
    final int nbCcy = pvInit.size();
    final List<Currency> ccyList = new ArrayList<>();
    for (int loopccy = 0; loopccy < nbCcy; loopccy++) {
      ccyList.add(pvInit.getCurrencyAmounts()[loopccy].getCurrency());
    }
    // Discounting
    final Set<Currency> ccyDiscounting = multicurveFwdpoints.getMulticurveProvider().getCurrencies();
    for (final Currency ccy : ccyDiscounting) {
      final YieldAndDiscountCurve curve = multicurveFwdpoints.getMulticurveProvider().getCurve(ccy);
      ArgumentChecker.isTrue(curve instanceof YieldCurve, "Curve should be a YieldCurve");
      final YieldCurve curveYield = (YieldCurve) curve;
      ArgumentChecker.isTrue(curveYield.getCurve() instanceof InterpolatedDoublesCurve, "Yield curve should be based on InterpolatedDoublesCurve");
      final InterpolatedDoublesCurve curveInt = (InterpolatedDoublesCurve) curveYield.getCurve();
      final int nbNodePoint = curveInt.getXDataAsPrimitive().length;
      final double[][] sensitivity = new double[nbCcy][nbNodePoint];
      for (int loopnode = 0; loopnode < nbNodePoint; loopnode++) {
        final double[] yieldBumpedPlus = curveInt.getYDataAsPrimitive().clone();
        yieldBumpedPlus[loopnode] += _shift;
        final YieldAndDiscountCurve dscBumpedPlus = new YieldCurve(curveInt.getName(), new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumpedPlus, curveInt.getInterpolator(), true));
        final MulticurveForwardPointsProviderDiscount marketDscBumpedPlus = new MulticurveForwardPointsProviderDiscount(multicurveFwdpoints.getMulticurveProvider().withDiscountFactor(ccy,
            dscBumpedPlus),
            multicurveFwdpoints.getForwardPointsCurve(), multicurveFwdpoints.getCurrencyPair());
        final MultipleCurrencyAmount pvBumpedPlus = instrument.accept(_valueCalculator, marketDscBumpedPlus);
        final double[] yieldBumpedMinus = curveInt.getYDataAsPrimitive().clone();
        yieldBumpedMinus[loopnode] -= _shift;
        final YieldAndDiscountCurve dscBumpedMinus = new YieldCurve(curveInt.getName(),
            new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumpedMinus, curveInt.getInterpolator(), true));
        final MulticurveForwardPointsProviderDiscount marketDscBumpedMinus = new MulticurveForwardPointsProviderDiscount(multicurveFwdpoints.getMulticurveProvider().withDiscountFactor(ccy,
            dscBumpedMinus),
            multicurveFwdpoints.getForwardPointsCurve(), multicurveFwdpoints.getCurrencyPair());
        final MultipleCurrencyAmount pvBumpedMinus = instrument.accept(_valueCalculator, marketDscBumpedMinus);
        final MultipleCurrencyAmount pvDiff = pvBumpedPlus.plus(pvBumpedMinus.multipliedBy(-1.0));
        for (int loopccypv = 0; loopccypv < nbCcy; loopccypv++) {
          sensitivity[loopccypv][loopnode] = pvDiff.getAmount(ccyList.get(loopccypv)) / (2 * _shift);
        }
      }
      final String name = multicurveFwdpoints.getMulticurveProvider().getName(ccy);
      for (int loopccypv = 0; loopccypv < nbCcy; loopccypv++) {
        result = result.plus(Pairs.of(name, ccyList.get(loopccypv)), new DoubleMatrix1D(sensitivity[loopccypv]));
      }
    }
    // Forward ON
    final Set<IndexON> indexON = multicurveFwdpoints.getMulticurveProvider().getIndexesON();
    for (final IndexON index : indexON) {
      final YieldAndDiscountCurve curve = multicurveFwdpoints.getCurve(index);
      ArgumentChecker.isTrue(curve instanceof YieldCurve, "Curve should be a YieldCurve");
      final YieldCurve curveYield = (YieldCurve) curve;
      ArgumentChecker.isTrue(curveYield.getCurve() instanceof InterpolatedDoublesCurve, "Yield curve should be based on InterpolatedDoublesCurve");
      final InterpolatedDoublesCurve curveInt = (InterpolatedDoublesCurve) curveYield.getCurve();
      final int nbNodePoint = curveInt.getXDataAsPrimitive().length;
      final double[][] sensitivity = new double[nbCcy][nbNodePoint];
      for (int loopnode = 0; loopnode < nbNodePoint; loopnode++) {
        final double[] yieldBumpedPlus = curveInt.getYDataAsPrimitive().clone();
        yieldBumpedPlus[loopnode] += _shift;
        final YieldAndDiscountCurve dscBumpedPlus = new YieldCurve(curveInt.getName(), new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumpedPlus, curveInt.getInterpolator(), true));
        final MulticurveForwardPointsProviderDiscount marketFwdBumpedPlus = new MulticurveForwardPointsProviderDiscount(multicurveFwdpoints.getMulticurveProvider().withForward(index, dscBumpedPlus),
            multicurveFwdpoints.getForwardPointsCurve(), multicurveFwdpoints.getCurrencyPair());
        final MultipleCurrencyAmount pvBumpedPlus = instrument.accept(_valueCalculator, marketFwdBumpedPlus);
        final double[] yieldBumpedMinus = curveInt.getYDataAsPrimitive().clone();
        yieldBumpedMinus[loopnode] -= _shift;
        final YieldAndDiscountCurve dscBumpedMinus = new YieldCurve(curveInt.getName(),
            new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumpedMinus, curveInt.getInterpolator(), true));
        final MulticurveForwardPointsProviderDiscount marketFwdBumpedMinus = new MulticurveForwardPointsProviderDiscount(
            multicurveFwdpoints.getMulticurveProvider().withForward(index, dscBumpedMinus),
            multicurveFwdpoints.getForwardPointsCurve(), multicurveFwdpoints.getCurrencyPair());
        final MultipleCurrencyAmount pvBumpedMinus = instrument.accept(_valueCalculator, marketFwdBumpedMinus);
        final MultipleCurrencyAmount pvDiff = pvBumpedPlus.plus(pvBumpedMinus.multipliedBy(-1.0));
        for (int loopccypv = 0; loopccypv < nbCcy; loopccypv++) {
          sensitivity[loopccypv][loopnode] = pvDiff.getAmount(ccyList.get(loopccypv)) / (2 * _shift);
        }
      }
      final String name = multicurveFwdpoints.getMulticurveProvider().getName(index);
      for (int loopccypv = 0; loopccypv < nbCcy; loopccypv++) {
        result = result.plus(Pairs.of(name, ccyList.get(loopccypv)), new DoubleMatrix1D(sensitivity[loopccypv]));
      }
    }
    // Forward Ibor
    final Set<IborIndex> indexForward = multicurveFwdpoints.getMulticurveProvider().getIndexesIbor();
    for (final IborIndex index : indexForward) {
      final YieldAndDiscountCurve curve = multicurveFwdpoints.getCurve(index);
      ArgumentChecker.isTrue(curve instanceof YieldCurve, "Curve should be a YieldCurve");
      final YieldCurve curveYield = (YieldCurve) curve;
      ArgumentChecker.isTrue(curveYield.getCurve() instanceof InterpolatedDoublesCurve, "Yield curve should be based on InterpolatedDoublesCurve");
      final InterpolatedDoublesCurve curveInt = (InterpolatedDoublesCurve) curveYield.getCurve();
      final int nbNodePoint = curveInt.getXDataAsPrimitive().length;
      final double[][] sensitivity = new double[nbCcy][nbNodePoint];
      for (int loopnode = 0; loopnode < nbNodePoint; loopnode++) {
        final double[] yieldBumpedPlus = curveInt.getYDataAsPrimitive().clone();
        yieldBumpedPlus[loopnode] += _shift;
        final YieldAndDiscountCurve dscBumpedPlus = new YieldCurve(curveInt.getName(), new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumpedPlus, curveInt.getInterpolator(), true));
        final MulticurveForwardPointsProviderDiscount marketFwdBumpedPlus = new MulticurveForwardPointsProviderDiscount(multicurveFwdpoints.getMulticurveProvider().withForward(index, dscBumpedPlus),
            multicurveFwdpoints.getForwardPointsCurve(), multicurveFwdpoints.getCurrencyPair());
        final MultipleCurrencyAmount pvBumpedPlus = instrument.accept(_valueCalculator, marketFwdBumpedPlus);
        final double[] yieldBumpedMinus = curveInt.getYDataAsPrimitive().clone();
        yieldBumpedMinus[loopnode] -= _shift;
        final YieldAndDiscountCurve dscBumpedMinus = new YieldCurve(curveInt.getName(),
            new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumpedMinus, curveInt.getInterpolator(), true));
        final MulticurveForwardPointsProviderDiscount marketFwdBumpedMinus = new MulticurveForwardPointsProviderDiscount(
            multicurveFwdpoints.getMulticurveProvider().withForward(index, dscBumpedMinus),
            multicurveFwdpoints.getForwardPointsCurve(), multicurveFwdpoints.getCurrencyPair());
        final MultipleCurrencyAmount pvBumpedMinus = instrument.accept(_valueCalculator, marketFwdBumpedMinus);
        final MultipleCurrencyAmount pvDiff = pvBumpedPlus.plus(pvBumpedMinus.multipliedBy(-1.0));
        for (int loopccypv = 0; loopccypv < nbCcy; loopccypv++) {
          sensitivity[loopccypv][loopnode] = pvDiff.getAmount(ccyList.get(loopccypv)) / (2 * _shift);
        }
      }
      final String name = multicurveFwdpoints.getMulticurveProvider().getName(index);
      for (int loopccypv = 0; loopccypv < nbCcy; loopccypv++) {
        result = result.plus(Pairs.of(name, ccyList.get(loopccypv)), new DoubleMatrix1D(sensitivity[loopccypv]));
      }
    }
    return result;
  }
}
