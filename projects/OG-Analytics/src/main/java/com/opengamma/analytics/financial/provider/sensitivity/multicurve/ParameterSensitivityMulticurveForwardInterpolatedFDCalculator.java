/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.sensitivity.multicurve;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderForward;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.Pairs;

/**
 * For an instrument, computes the sensitivity of a value (often the present value or a par spread) to the parameters used in the curve.
 * The computation is done by shifting each node point in each curve; the curves must be interpolated yield curves for discount curve and DoublesCurve for forward curves.
 * The return format is ParameterSensitivity object.
 * This is a very inefficient way to compute the sensitivities. It should be used only for tests purposes or when speed is irrelevant.
 */
public class ParameterSensitivityMulticurveForwardInterpolatedFDCalculator {

  /**
   * The value calculator.
   */
  private final InstrumentDerivativeVisitor<ParameterProviderInterface, MultipleCurrencyAmount> _valueCalculator;
  /**
   * The shift used for finite difference.
   */
  private final double _shift;

  /**
   * Constructor
   * @param valueCalculator The value calculator.
   * @param shift The shift used for finite difference.
   */
  public ParameterSensitivityMulticurveForwardInterpolatedFDCalculator(final InstrumentDerivativeVisitor<ParameterProviderInterface, MultipleCurrencyAmount> valueCalculator, final double shift) {
    ArgumentChecker.notNull(valueCalculator, "Calculator");
    _valueCalculator = valueCalculator;
    _shift = shift;
  }

  /**
   * Compute the sensitivity by finite difference on all points. The curves must be interpolated yield curves.
   * Only the discounting and forward curves sensitivity is computed.
   * @param instrument The instrument.
   * @param market The market (all discounting and forward curves should be of the type YieldCurve with InterpolatedDoublesCurve.
   * @return The parameter sensitivity.
   */
  public MultipleCurrencyParameterSensitivity calculateSensitivity(final InstrumentDerivative instrument, final MulticurveProviderForward market) {
    MultipleCurrencyParameterSensitivity result = new MultipleCurrencyParameterSensitivity();
    final MultipleCurrencyAmount pvInit = instrument.accept(_valueCalculator, market);
    final MultipleCurrencyAmount pvInitMinus = pvInit.multipliedBy(-1.0);
    final int nbCcy = pvInit.size();
    final List<Currency> ccyList = new ArrayList<>();
    for (int loopccy = 0; loopccy < nbCcy; loopccy++) {
      ccyList.add(pvInit.getCurrencyAmounts()[loopccy].getCurrency());
    }
    // Discounting
    final Set<Currency> ccyDiscounting = market.getCurrencies();
    for (final Currency ccy : ccyDiscounting) {
      final YieldAndDiscountCurve curve = market.getCurve(ccy);
      ArgumentChecker.isTrue(curve instanceof YieldCurve, "Curve should be a YieldCurve");
      final YieldCurve curveYield = (YieldCurve) curve;
      ArgumentChecker.isTrue(curveYield.getCurve() instanceof InterpolatedDoublesCurve, "Yield curve should be based on InterpolatedDoublesCurve");
      final InterpolatedDoublesCurve curveInt = (InterpolatedDoublesCurve) curveYield.getCurve();
      final int nbNodePoint = curveInt.getXDataAsPrimitive().length;
      final double[][] sensitivity = new double[nbCcy][nbNodePoint];
      for (int loopnode = 0; loopnode < nbNodePoint; loopnode++) {
        final double[] yieldBumped = curveInt.getYDataAsPrimitive().clone();
        yieldBumped[loopnode] += _shift;
        final YieldAndDiscountCurve dscBumped = new YieldCurve(curveInt.getName(), new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumped, curveInt.getInterpolator(), true));
        final MulticurveProviderForward marketDscBumped = market.withDiscountFactor(ccy, dscBumped);
        final MultipleCurrencyAmount pvBumped = instrument.accept(_valueCalculator, marketDscBumped);
        final MultipleCurrencyAmount pvDiff = pvBumped.plus(pvInitMinus);
        for (int loopccypv = 0; loopccypv < nbCcy; loopccypv++) {
          sensitivity[loopccypv][loopnode] = pvDiff.getAmount(ccyList.get(loopccypv)) / _shift;
        }
      }
      final String name = market.getName(ccy);
      for (int loopccypv = 0; loopccypv < nbCcy; loopccypv++) {
        result = result.plus(Pairs.of(name, ccyList.get(loopccypv)), new DoubleMatrix1D(sensitivity[loopccypv]));
      }
    }
    // Forward ON
    final Set<IndexON> indexON = market.getIndexesON();
    for (final IndexON index : indexON) {
      final YieldAndDiscountCurve curve = market.getCurve(index);
      ArgumentChecker.isTrue(curve instanceof YieldCurve, "Curve should be a YieldCurve");
      final YieldCurve curveYield = (YieldCurve) curve;
      ArgumentChecker.isTrue(curveYield.getCurve() instanceof InterpolatedDoublesCurve, "Yield curve should be based on InterpolatedDoublesCurve");
      final InterpolatedDoublesCurve curveInt = (InterpolatedDoublesCurve) curveYield.getCurve();
      final int nbNodePoint = curveInt.getXDataAsPrimitive().length;
      final double[][] sensitivity = new double[nbCcy][nbNodePoint];
      for (int loopnode = 0; loopnode < nbNodePoint; loopnode++) {
        final double[] yieldBumped = curveInt.getYDataAsPrimitive().clone();
        yieldBumped[loopnode] += _shift;
        final YieldAndDiscountCurve fwdBumped = new YieldCurve(curveInt.getName(), new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumped, curveInt.getInterpolator(), true));
        final MulticurveProviderForward marketFwdBumped = market.withForward(index, fwdBumped);
        final MultipleCurrencyAmount pvBumped = instrument.accept(_valueCalculator, marketFwdBumped);
        final MultipleCurrencyAmount pvDiff = pvBumped.plus(pvInitMinus);
        for (int loopccypv = 0; loopccypv < nbCcy; loopccypv++) {
          sensitivity[loopccypv][loopnode] = pvDiff.getAmount(ccyList.get(loopccypv)) / _shift;
        }
      }
      final String name = market.getName(index);
      for (int loopccypv = 0; loopccypv < nbCcy; loopccypv++) {
        result = result.plus(Pairs.of(name, ccyList.get(loopccypv)), new DoubleMatrix1D(sensitivity[loopccypv]));
      }
    }
    // Forward Ibor
    final Set<IborIndex> indexForward = market.getIndexesIbor();
    for (final IborIndex index : indexForward) {
      final DoublesCurve curve = market.getCurve(index);
      ArgumentChecker.isTrue(curve instanceof InterpolatedDoublesCurve, "Forward curve should be based on InterpolatedDoublesCurve");
      final InterpolatedDoublesCurve curveInt = (InterpolatedDoublesCurve) curve;
      final int nbNodePoint = curveInt.getXDataAsPrimitive().length;
      final double[][] sensitivity = new double[nbCcy][nbNodePoint];
      for (int loopnode = 0; loopnode < nbNodePoint; loopnode++) {
        final double[] yieldBumped = curveInt.getYDataAsPrimitive().clone();
        yieldBumped[loopnode] += _shift;
        final DoublesCurve fwdBumped = new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumped, curveInt.getInterpolator(), true);
        final MulticurveProviderForward marketFwdBumped = market.withForward(index, fwdBumped);
        final MultipleCurrencyAmount pvBumped = instrument.accept(_valueCalculator, marketFwdBumped);
        final MultipleCurrencyAmount pvDiff = pvBumped.plus(pvInitMinus);
        for (int loopccypv = 0; loopccypv < nbCcy; loopccypv++) {
          sensitivity[loopccypv][loopnode] = pvDiff.getAmount(ccyList.get(loopccypv)) / _shift;
        }
      }
      final String name = market.getName(index);
      for (int loopccypv = 0; loopccypv < nbCcy; loopccypv++) {
        result = result.plus(Pairs.of(name, ccyList.get(loopccypv)), new DoubleMatrix1D(sensitivity[loopccypv]));
      }
    }
    return result;
  }

}
