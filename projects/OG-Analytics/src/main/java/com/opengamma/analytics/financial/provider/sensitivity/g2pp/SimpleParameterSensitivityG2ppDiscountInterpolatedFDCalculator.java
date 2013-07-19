/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.sensitivity.g2pp;

import java.util.Set;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.G2ppProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.G2ppProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimpleParameterSensitivity;
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
public class SimpleParameterSensitivityG2ppDiscountInterpolatedFDCalculator {

  /**
   * The value calculator.
   */
  private final InstrumentDerivativeVisitor<G2ppProviderInterface, Double> _valueCalculator;
  /**
   * The shift used for finite difference.
   */
  private final double _shift;

  /**
   * Constructor
   * @param valueCalculator The value calculator.
   * @param shift The shift used for finite difference.
   */
  public SimpleParameterSensitivityG2ppDiscountInterpolatedFDCalculator(final InstrumentDerivativeVisitor<G2ppProviderInterface, Double> valueCalculator, final double shift) {
    ArgumentChecker.notNull(valueCalculator, "Calculator");
    _valueCalculator = valueCalculator;
    _shift = shift;
  }

  /**
   * Compute the sensitivity by finite difference on all points. The curves must be interpolated yield curves.
   * Only the discounting and forward curves sensitivity is computed.
   * @param instrument The instrument.
   * @param g2curves The provider: all discounting, forward and issuer curves should be of the type YieldCurve with InterpolatedDoublesCurve.
   * @return The parameter sensitivity.
   */
  public SimpleParameterSensitivity calculateSensitivity(final InstrumentDerivative instrument, final G2ppProviderDiscount g2curves) {
    SimpleParameterSensitivity result = new SimpleParameterSensitivity();
    // Discounting
    final Set<Currency> ccyDiscounting = g2curves.getMulticurveProvider().getCurrencies();
    for (final Currency ccy : ccyDiscounting) {
      final YieldAndDiscountCurve curve = g2curves.getMulticurveProvider().getCurve(ccy);
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
        final G2ppProviderDiscount marketDscBumpedPlus = new G2ppProviderDiscount(g2curves.getMulticurveProvider().withDiscountFactor(ccy, dscBumpedPlus),
            g2curves.getG2ppParameters(), g2curves.getG2ppCurrency());
        final double valueBumpedPlus = instrument.accept(_valueCalculator, marketDscBumpedPlus);
        final double[] yieldBumpedMinus = curveInt.getYDataAsPrimitive().clone();
        yieldBumpedMinus[loopnode] -= _shift;
        final YieldAndDiscountCurve dscBumpedMinus = new YieldCurve(curveInt.getName(),
            new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumpedMinus, curveInt.getInterpolator(), true));
        final G2ppProviderDiscount marketDscBumpedMinus = new G2ppProviderDiscount(g2curves.getMulticurveProvider().withDiscountFactor(ccy, dscBumpedMinus),
            g2curves.getG2ppParameters(), g2curves.getG2ppCurrency());
        final double valueBumpedMinus = instrument.accept(_valueCalculator, marketDscBumpedMinus);
        sensitivity[loopnode] = (valueBumpedPlus - valueBumpedMinus) / (2 * _shift);
      }
      final String name = g2curves.getMulticurveProvider().getName(ccy);
      result = result.plus(name, new DoubleMatrix1D(sensitivity));
    }
    // Forward ON
    final Set<IndexON> indexON = g2curves.getMulticurveProvider().getIndexesON();
    for (final IndexON index : indexON) {
      final YieldAndDiscountCurve curve = g2curves.getMulticurveProvider().getCurve(index);
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
        final G2ppProviderDiscount marketFwdBumpedPlus = new G2ppProviderDiscount(g2curves.getMulticurveProvider().withForward(index, dscBumpedPlus),
            g2curves.getG2ppParameters(), g2curves.getG2ppCurrency());
        final double valueBumpedPlus = instrument.accept(_valueCalculator, marketFwdBumpedPlus);
        final double[] yieldBumpedMinus = curveInt.getYDataAsPrimitive().clone();
        yieldBumpedMinus[loopnode] -= _shift;
        final YieldAndDiscountCurve dscBumpedMinus = new YieldCurve(curveInt.getName(),
            new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumpedMinus, curveInt.getInterpolator(), true));
        final G2ppProviderDiscount marketFwdBumpedMinus = new G2ppProviderDiscount(g2curves.getMulticurveProvider().withForward(index, dscBumpedMinus),
            g2curves.getG2ppParameters(), g2curves.getG2ppCurrency());
        final double valueBumpedMinus = instrument.accept(_valueCalculator, marketFwdBumpedMinus);
        sensitivity[loopnode] = (valueBumpedPlus - valueBumpedMinus) / (2 * _shift);
      }
      final String name = g2curves.getMulticurveProvider().getName(index);
      result = result.plus(name, new DoubleMatrix1D(sensitivity));
    }
    // Forward Ibor - symmetrical
    final Set<IborIndex> indexForward = g2curves.getMulticurveProvider().getIndexesIbor();
    for (final IborIndex index : indexForward) {
      final YieldAndDiscountCurve curve = g2curves.getMulticurveProvider().getCurve(index);
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
        final G2ppProviderDiscount marketFwdBumpedPlus = new G2ppProviderDiscount(g2curves.getMulticurveProvider().withForward(index, dscBumpedPlus),
            g2curves.getG2ppParameters(), g2curves.getG2ppCurrency());
        final double valueBumpedPlus = instrument.accept(_valueCalculator, marketFwdBumpedPlus);
        final double[] yieldBumpedMinus = curveInt.getYDataAsPrimitive().clone();
        yieldBumpedMinus[loopnode] -= _shift;
        final YieldAndDiscountCurve dscBumpedMinus = new YieldCurve(curveInt.getName(),
            new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumpedMinus, curveInt.getInterpolator(), true));
        final G2ppProviderDiscount marketFwdBumpedMinus = new G2ppProviderDiscount(g2curves.getMulticurveProvider().withForward(index, dscBumpedMinus),
            g2curves.getG2ppParameters(), g2curves.getG2ppCurrency());
        final double valueBumpedMinus = instrument.accept(_valueCalculator, marketFwdBumpedMinus);
        sensitivity[loopnode] = (valueBumpedPlus - valueBumpedMinus) / (2 * _shift);
      }
      final String name = g2curves.getMulticurveProvider().getName(index);
      result = result.plus(name, new DoubleMatrix1D(sensitivity));
    }
    return result;
  }
}
