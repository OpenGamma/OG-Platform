/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.sensitivity.hullwhite;

import java.util.Set;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.description.HullWhiteOneFactorProviderDiscount;
import com.opengamma.analytics.financial.provider.description.HullWhiteOneFactorProviderInterface;
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
public class SimpleParameterSensitivityHullWhiteDiscountInterpolatedFDCalculator {

  /**
   * The value calculator.
   */
  private final InstrumentDerivativeVisitor<HullWhiteOneFactorProviderInterface, Double> _valueCalculator;
  /**
   * The shift used for finite difference.
   */
  private final double _shift;

  /**
   * Constructor
   * @param valueCalculator The value calculator.
   * @param shift The shift used for finite difference.
   */
  public SimpleParameterSensitivityHullWhiteDiscountInterpolatedFDCalculator(InstrumentDerivativeVisitor<HullWhiteOneFactorProviderInterface, Double> valueCalculator, final double shift) {
    ArgumentChecker.notNull(valueCalculator, "Calculator");
    _valueCalculator = valueCalculator;
    _shift = shift;
  }

  /**
   * Compute the sensitivity by finite difference on all points. The curves must be interpolated yield curves.
   * Only the discounting and forward curves sensitivity is computed.
   * @param instrument The instrument.
   * @param hwcurves The provider: all discounting, forward and issuer curves should be of the type YieldCurve with InterpolatedDoublesCurve.
   * @return The parameter sensitivity.
   */
  public SimpleParameterSensitivity calculateSensitivity(final InstrumentDerivative instrument, final HullWhiteOneFactorProviderDiscount hwcurves) {
    SimpleParameterSensitivity result = new SimpleParameterSensitivity();
    // Discounting
    Set<Currency> ccyDiscounting = hwcurves.getMulticurveProvider().getCurrencies();
    for (Currency ccy : ccyDiscounting) {
      YieldAndDiscountCurve curve = hwcurves.getMulticurveProvider().getCurve(ccy);
      ArgumentChecker.isTrue(curve instanceof YieldCurve, "Curve should be a YieldCurve");
      YieldCurve curveYield = (YieldCurve) curve;
      ArgumentChecker.isTrue(curveYield.getCurve() instanceof InterpolatedDoublesCurve, "Yield curve should be based on InterpolatedDoublesCurve");
      InterpolatedDoublesCurve curveInt = (InterpolatedDoublesCurve) curveYield.getCurve();
      int nbNodePoint = curveInt.getXDataAsPrimitive().length;
      double[] sensitivity = new double[nbNodePoint];
      for (int loopnode = 0; loopnode < nbNodePoint; loopnode++) {
        double[] yieldBumpedPlus = curveInt.getYDataAsPrimitive().clone();
        yieldBumpedPlus[loopnode] += _shift;
        YieldAndDiscountCurve dscBumpedPlus = new YieldCurve(curveInt.getName(), new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumpedPlus, curveInt.getInterpolator(), true));
        HullWhiteOneFactorProviderDiscount marketDscBumpedPlus = new HullWhiteOneFactorProviderDiscount(hwcurves.getMulticurveProvider().withDiscountFactor(ccy, dscBumpedPlus),
            hwcurves.getHullWhiteParameters(), hwcurves.getHullWhiteCurrency());
        double valueBumpedPlus = _valueCalculator.visit(instrument, marketDscBumpedPlus);
        double[] yieldBumpedMinus = curveInt.getYDataAsPrimitive().clone();
        yieldBumpedMinus[loopnode] -= _shift;
        YieldAndDiscountCurve dscBumpedMinus = new YieldCurve(curveInt.getName(), new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumpedMinus, curveInt.getInterpolator(), true));
        HullWhiteOneFactorProviderDiscount marketDscBumpedMinus = new HullWhiteOneFactorProviderDiscount(hwcurves.getMulticurveProvider().withDiscountFactor(ccy, dscBumpedMinus),
            hwcurves.getHullWhiteParameters(), hwcurves.getHullWhiteCurrency());
        double valueBumpedMinus = _valueCalculator.visit(instrument, marketDscBumpedMinus);
        sensitivity[loopnode] = (valueBumpedPlus - valueBumpedMinus) / (2 * _shift);
        String name = hwcurves.getMulticurveProvider().getName(ccy);
        result = result.plus(name, new DoubleMatrix1D(sensitivity));
      }
    }
    // Forward ON
    Set<IndexON> indexON = hwcurves.getMulticurveProvider().getIndexesON();
    for (IndexON index : indexON) {
      YieldAndDiscountCurve curve = hwcurves.getMulticurveProvider().getCurve(index);
      ArgumentChecker.isTrue(curve instanceof YieldCurve, "Curve should be a YieldCurve");
      YieldCurve curveYield = (YieldCurve) curve;
      ArgumentChecker.isTrue(curveYield.getCurve() instanceof InterpolatedDoublesCurve, "Yield curve should be based on InterpolatedDoublesCurve");
      InterpolatedDoublesCurve curveInt = (InterpolatedDoublesCurve) curveYield.getCurve();
      int nbNodePoint = curveInt.getXDataAsPrimitive().length;
      double[] sensitivity = new double[nbNodePoint];
      for (int loopnode = 0; loopnode < nbNodePoint; loopnode++) {
        double[] yieldBumpedPlus = curveInt.getYDataAsPrimitive().clone();
        yieldBumpedPlus[loopnode] += _shift;
        YieldAndDiscountCurve dscBumpedPlus = new YieldCurve(curveInt.getName(), new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumpedPlus, curveInt.getInterpolator(), true));
        HullWhiteOneFactorProviderDiscount marketFwdBumpedPlus = new HullWhiteOneFactorProviderDiscount(hwcurves.getMulticurveProvider().withForward(index, dscBumpedPlus),
            hwcurves.getHullWhiteParameters(), hwcurves.getHullWhiteCurrency());
        double valueBumpedPlus = _valueCalculator.visit(instrument, marketFwdBumpedPlus);
        double[] yieldBumpedMinus = curveInt.getYDataAsPrimitive().clone();
        yieldBumpedMinus[loopnode] -= _shift;
        YieldAndDiscountCurve dscBumpedMinus = new YieldCurve(curveInt.getName(), new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumpedMinus, curveInt.getInterpolator(), true));
        HullWhiteOneFactorProviderDiscount marketFwdBumpedMinus = new HullWhiteOneFactorProviderDiscount(hwcurves.getMulticurveProvider().withForward(index, dscBumpedMinus),
            hwcurves.getHullWhiteParameters(), hwcurves.getHullWhiteCurrency());
        double valueBumpedMinus = _valueCalculator.visit(instrument, marketFwdBumpedMinus);
        sensitivity[loopnode] = (valueBumpedPlus - valueBumpedMinus) / (2 * _shift);
      }
      String name = hwcurves.getMulticurveProvider().getName(index);
      result = result.plus(name, new DoubleMatrix1D(sensitivity));
    }
    // Forward Ibor - symmetrical
    Set<IborIndex> indexForward = hwcurves.getMulticurveProvider().getIndexesIbor();
    for (IborIndex index : indexForward) {
      YieldAndDiscountCurve curve = hwcurves.getMulticurveProvider().getCurve(index);
      ArgumentChecker.isTrue(curve instanceof YieldCurve, "Curve should be a YieldCurve");
      YieldCurve curveYield = (YieldCurve) curve;
      ArgumentChecker.isTrue(curveYield.getCurve() instanceof InterpolatedDoublesCurve, "Yield curve should be based on InterpolatedDoublesCurve");
      InterpolatedDoublesCurve curveInt = (InterpolatedDoublesCurve) curveYield.getCurve();
      int nbNodePoint = curveInt.getXDataAsPrimitive().length;
      double[] sensitivity = new double[nbNodePoint];
      for (int loopnode = 0; loopnode < nbNodePoint; loopnode++) {
        double[] yieldBumpedPlus = curveInt.getYDataAsPrimitive().clone();
        yieldBumpedPlus[loopnode] += _shift;
        YieldAndDiscountCurve dscBumpedPlus = new YieldCurve(curveInt.getName(), new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumpedPlus, curveInt.getInterpolator(), true));
        HullWhiteOneFactorProviderDiscount marketFwdBumpedPlus = new HullWhiteOneFactorProviderDiscount(hwcurves.getMulticurveProvider().withForward(index, dscBumpedPlus),
            hwcurves.getHullWhiteParameters(), hwcurves.getHullWhiteCurrency());
        double valueBumpedPlus = _valueCalculator.visit(instrument, marketFwdBumpedPlus);
        double[] yieldBumpedMinus = curveInt.getYDataAsPrimitive().clone();
        yieldBumpedMinus[loopnode] -= _shift;
        YieldAndDiscountCurve dscBumpedMinus = new YieldCurve(curveInt.getName(), new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumpedMinus, curveInt.getInterpolator(), true));
        HullWhiteOneFactorProviderDiscount marketFwdBumpedMinus = new HullWhiteOneFactorProviderDiscount(hwcurves.getMulticurveProvider().withForward(index, dscBumpedMinus),
            hwcurves.getHullWhiteParameters(), hwcurves.getHullWhiteCurrency());
        double valueBumpedMinus = _valueCalculator.visit(instrument, marketFwdBumpedMinus);
        sensitivity[loopnode] = (valueBumpedPlus - valueBumpedMinus) / (2 * _shift);
      }
      String name = hwcurves.getMulticurveProvider().getName(index);
      result = result.plus(name, new DoubleMatrix1D(sensitivity));
    }
    return result;
  }
}
