/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.sensitivity.hullwhite;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.description.HullWhiteOneFactorProviderDiscount;
import com.opengamma.analytics.financial.provider.description.HullWhiteOneFactorProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * For an instrument, computes the sensitivity of a value (often the present value or a par spread) to the parameters used in the curve.
 * The computation is done by shifting each node point in each curve; the curves must be interpolated yield curves for discounting and forward curves.
 * The return format is ParameterSensitivity object.
 * This is a very inefficient way to compute the sensitivities. It should be used only for tests purposes or when speed is irrelevant.
 */
public class ParameterSensitivityHullWhiteDiscountInterpolatedFDCalculator {

  /**
   * The value calculator.
   */
  private final InstrumentDerivativeVisitor<HullWhiteOneFactorProviderInterface, MultipleCurrencyAmount> _valueCalculator;
  /**
   * The shift used for finite difference.
   */
  private final double _shift;

  /**
   * Constructor
   * @param valueCalculator The value calculator.
   * @param shift The shift used for finite difference.
   */
  public ParameterSensitivityHullWhiteDiscountInterpolatedFDCalculator(InstrumentDerivativeVisitor<HullWhiteOneFactorProviderInterface, MultipleCurrencyAmount> valueCalculator, final double shift) {
    ArgumentChecker.notNull(valueCalculator, "Calculator");
    _valueCalculator = valueCalculator;
    _shift = shift;
  }

  /**
   * Compute the sensitivity by finite difference on all points. The curves must be interpolated yield curves.
   * Only the discounting and forward curves sensitivity is computed.
   * @param instrument The instrument.
   * @param hullWhite The market (all discounting and forward curves should be of the type YieldCurve with InterpolatedDoublesCurve.
   * @return The parameter sensitivity.
   */
  public MultipleCurrencyParameterSensitivity calculateSensitivity(final InstrumentDerivative instrument, final HullWhiteOneFactorProviderDiscount hullWhite) {
    MultipleCurrencyParameterSensitivity result = new MultipleCurrencyParameterSensitivity();
    final MultipleCurrencyAmount pvInit = _valueCalculator.visit(instrument, hullWhite);
    int nbCcy = pvInit.size();
    List<Currency> ccyList = new ArrayList<Currency>();
    for (int loopccy = 0; loopccy < nbCcy; loopccy++) {
      ccyList.add(pvInit.getCurrencyAmounts()[loopccy].getCurrency());
    }
    // Discounting
    Set<Currency> ccyDiscounting = hullWhite.getMulticurveProvider().getCurrencies();
    for (Currency ccy : ccyDiscounting) {
      YieldAndDiscountCurve curve = hullWhite.getCurve(ccy);
      ArgumentChecker.isTrue(curve instanceof YieldCurve, "Curve should be a YieldCurve");
      YieldCurve curveYield = (YieldCurve) curve;
      ArgumentChecker.isTrue(curveYield.getCurve() instanceof InterpolatedDoublesCurve, "Yield curve should be based on InterpolatedDoublesCurve");
      InterpolatedDoublesCurve curveInt = (InterpolatedDoublesCurve) curveYield.getCurve();
      int nbNodePoint = curveInt.getXDataAsPrimitive().length;
      double[][] sensitivity = new double[nbCcy][nbNodePoint];
      for (int loopnode = 0; loopnode < nbNodePoint; loopnode++) {
        double[] yieldBumpedPlus = curveInt.getYDataAsPrimitive().clone();
        yieldBumpedPlus[loopnode] += _shift;
        YieldAndDiscountCurve dscBumpedPlus = new YieldCurve(curveInt.getName(), new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumpedPlus, curveInt.getInterpolator(), true));
        HullWhiteOneFactorProviderDiscount marketDscBumpedPlus = new HullWhiteOneFactorProviderDiscount(hullWhite.getMulticurveProvider().withDiscountFactor(ccy, dscBumpedPlus),
            hullWhite.getHullWhiteParameters(), hullWhite.getHullWhiteCurrency());
        MultipleCurrencyAmount pvBumpedPlus = _valueCalculator.visit(instrument, marketDscBumpedPlus);
        double[] yieldBumpedMinus = curveInt.getYDataAsPrimitive().clone();
        yieldBumpedMinus[loopnode] -= _shift;
        YieldAndDiscountCurve dscBumpedMinus = new YieldCurve(curveInt.getName(), new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumpedMinus, curveInt.getInterpolator(), true));
        HullWhiteOneFactorProviderDiscount marketDscBumpedMinus = new HullWhiteOneFactorProviderDiscount(hullWhite.getMulticurveProvider().withDiscountFactor(ccy, dscBumpedMinus),
            hullWhite.getHullWhiteParameters(), hullWhite.getHullWhiteCurrency());
        MultipleCurrencyAmount pvBumpedMinus = _valueCalculator.visit(instrument, marketDscBumpedMinus);
        MultipleCurrencyAmount pvDiff = pvBumpedPlus.plus(pvBumpedMinus.multipliedBy(-1.0));
        for (int loopccypv = 0; loopccypv < nbCcy; loopccypv++) {
          sensitivity[loopccypv][loopnode] = pvDiff.getAmount(ccyList.get(loopccypv)) / (2 * _shift);
        }
      }
      String name = hullWhite.getMulticurveProvider().getName(ccy);
      for (int loopccypv = 0; loopccypv < nbCcy; loopccypv++) {
        result = result.plus(new ObjectsPair<String, Currency>(name, ccyList.get(loopccypv)), new DoubleMatrix1D(sensitivity[loopccypv]));
      }
    }
    // Forward ON
    Set<IndexON> indexON = hullWhite.getMulticurveProvider().getIndexesON();
    for (IndexON index : indexON) {
      YieldAndDiscountCurve curve = hullWhite.getCurve(index);
      ArgumentChecker.isTrue(curve instanceof YieldCurve, "Curve should be a YieldCurve");
      YieldCurve curveYield = (YieldCurve) curve;
      ArgumentChecker.isTrue(curveYield.getCurve() instanceof InterpolatedDoublesCurve, "Yield curve should be based on InterpolatedDoublesCurve");
      InterpolatedDoublesCurve curveInt = (InterpolatedDoublesCurve) curveYield.getCurve();
      int nbNodePoint = curveInt.getXDataAsPrimitive().length;
      double[][] sensitivity = new double[nbCcy][nbNodePoint];
      for (int loopnode = 0; loopnode < nbNodePoint; loopnode++) {
        double[] yieldBumpedPlus = curveInt.getYDataAsPrimitive().clone();
        yieldBumpedPlus[loopnode] += _shift;
        YieldAndDiscountCurve dscBumpedPlus = new YieldCurve(curveInt.getName(), new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumpedPlus, curveInt.getInterpolator(), true));
        HullWhiteOneFactorProviderDiscount marketFwdBumpedPlus = new HullWhiteOneFactorProviderDiscount(hullWhite.getMulticurveProvider().withForward(index, dscBumpedPlus),
            hullWhite.getHullWhiteParameters(), hullWhite.getHullWhiteCurrency());
        MultipleCurrencyAmount pvBumpedPlus = _valueCalculator.visit(instrument, marketFwdBumpedPlus);
        double[] yieldBumpedMinus = curveInt.getYDataAsPrimitive().clone();
        yieldBumpedMinus[loopnode] -= _shift;
        YieldAndDiscountCurve dscBumpedMinus = new YieldCurve(curveInt.getName(), new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumpedMinus, curveInt.getInterpolator(), true));
        HullWhiteOneFactorProviderDiscount marketFwdBumpedMinus = new HullWhiteOneFactorProviderDiscount(hullWhite.getMulticurveProvider().withForward(index, dscBumpedMinus),
            hullWhite.getHullWhiteParameters(), hullWhite.getHullWhiteCurrency());
        MultipleCurrencyAmount pvBumpedMinus = _valueCalculator.visit(instrument, marketFwdBumpedMinus);
        MultipleCurrencyAmount pvDiff = pvBumpedPlus.plus(pvBumpedMinus.multipliedBy(-1.0));
        for (int loopccypv = 0; loopccypv < nbCcy; loopccypv++) {
          sensitivity[loopccypv][loopnode] = pvDiff.getAmount(ccyList.get(loopccypv)) / (2 * _shift);
        }
      }
      String name = hullWhite.getMulticurveProvider().getName(index);
      for (int loopccypv = 0; loopccypv < nbCcy; loopccypv++) {
        result = result.plus(new ObjectsPair<String, Currency>(name, ccyList.get(loopccypv)), new DoubleMatrix1D(sensitivity[loopccypv]));
      }
    }
    // Forward Ibor
    Set<IborIndex> indexForward = hullWhite.getMulticurveProvider().getIndexesIbor();
    for (IborIndex index : indexForward) {
      YieldAndDiscountCurve curve = hullWhite.getCurve(index);
      ArgumentChecker.isTrue(curve instanceof YieldCurve, "Curve should be a YieldCurve");
      YieldCurve curveYield = (YieldCurve) curve;
      ArgumentChecker.isTrue(curveYield.getCurve() instanceof InterpolatedDoublesCurve, "Yield curve should be based on InterpolatedDoublesCurve");
      InterpolatedDoublesCurve curveInt = (InterpolatedDoublesCurve) curveYield.getCurve();
      int nbNodePoint = curveInt.getXDataAsPrimitive().length;
      double[][] sensitivity = new double[nbCcy][nbNodePoint];
      for (int loopnode = 0; loopnode < nbNodePoint; loopnode++) {
        double[] yieldBumpedPlus = curveInt.getYDataAsPrimitive().clone();
        yieldBumpedPlus[loopnode] += _shift;
        YieldAndDiscountCurve dscBumpedPlus = new YieldCurve(curveInt.getName(), new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumpedPlus, curveInt.getInterpolator(), true));
        HullWhiteOneFactorProviderDiscount marketFwdBumpedPlus = new HullWhiteOneFactorProviderDiscount(hullWhite.getMulticurveProvider().withForward(index, dscBumpedPlus),
            hullWhite.getHullWhiteParameters(), hullWhite.getHullWhiteCurrency());
        MultipleCurrencyAmount pvBumpedPlus = _valueCalculator.visit(instrument, marketFwdBumpedPlus);
        double[] yieldBumpedMinus = curveInt.getYDataAsPrimitive().clone();
        yieldBumpedMinus[loopnode] -= _shift;
        YieldAndDiscountCurve dscBumpedMinus = new YieldCurve(curveInt.getName(), new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumpedMinus, curveInt.getInterpolator(), true));
        HullWhiteOneFactorProviderDiscount marketFwdBumpedMinus = new HullWhiteOneFactorProviderDiscount(hullWhite.getMulticurveProvider().withForward(index, dscBumpedMinus),
            hullWhite.getHullWhiteParameters(), hullWhite.getHullWhiteCurrency());
        MultipleCurrencyAmount pvBumpedMinus = _valueCalculator.visit(instrument, marketFwdBumpedMinus);
        MultipleCurrencyAmount pvDiff = pvBumpedPlus.plus(pvBumpedMinus.multipliedBy(-1.0));
        for (int loopccypv = 0; loopccypv < nbCcy; loopccypv++) {
          sensitivity[loopccypv][loopnode] = pvDiff.getAmount(ccyList.get(loopccypv)) / (2 * _shift);
        }
      }
      String name = hullWhite.getMulticurveProvider().getName(index);
      for (int loopccypv = 0; loopccypv < nbCcy; loopccypv++) {
        result = result.plus(new ObjectsPair<String, Currency>(name, ccyList.get(loopccypv)), new DoubleMatrix1D(sensitivity[loopccypv]));
      }
    }
    return result;
  }
}
