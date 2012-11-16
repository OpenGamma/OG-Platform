/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.sensitivity.sabr;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.description.SABRSwaptionProviderDiscount;
import com.opengamma.analytics.financial.provider.description.SABRSwaptionProviderInterface;
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
public class ParameterSensitivitySABRSwptDiscountInterpolatedFDCalculator {

  /**
   * The value calculator.
   */
  private final InstrumentDerivativeVisitor<SABRSwaptionProviderInterface, MultipleCurrencyAmount> _valueCalculator;
  /**
   * The shift used for finite difference.
   */
  private final double _shift;

  /**
   * Constructor
   * @param valueCalculator The value calculator.
   * @param shift The shift used for finite difference.
   */
  public ParameterSensitivitySABRSwptDiscountInterpolatedFDCalculator(InstrumentDerivativeVisitor<SABRSwaptionProviderInterface, MultipleCurrencyAmount> valueCalculator, final double shift) {
    ArgumentChecker.notNull(valueCalculator, "Calculator");
    _valueCalculator = valueCalculator;
    _shift = shift;
  }

  /**
   * Compute the sensitivity by finite difference on all points. The curves must be interpolated yield curves.
   * Only the discounting and forward curves sensitivity is computed.
   * @param instrument The instrument.
   * @param sabr The market (all discounting and forward curves should be of the type YieldCurve with InterpolatedDoublesCurve.
   * @return The parameter sensitivity.
   */
  public MultipleCurrencyParameterSensitivity calculateSensitivity(final InstrumentDerivative instrument, final SABRSwaptionProviderDiscount sabr) {
    MultipleCurrencyParameterSensitivity result = new MultipleCurrencyParameterSensitivity();
    final MultipleCurrencyAmount pvInit = _valueCalculator.visit(instrument, sabr);
    int nbCcy = pvInit.size();
    List<Currency> ccyList = new ArrayList<Currency>();
    for (int loopccy = 0; loopccy < nbCcy; loopccy++) {
      ccyList.add(pvInit.getCurrencyAmounts()[loopccy].getCurrency());
    }
    // Discounting
    Set<Currency> ccyDiscounting = sabr.getMulticurveProvider().getCurrencies();
    for (Currency ccy : ccyDiscounting) {
      YieldAndDiscountCurve curve = sabr.getMulticurveProvider().getCurve(ccy);
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
        SABRSwaptionProviderDiscount marketDscBumpedPlus = new SABRSwaptionProviderDiscount(sabr.getMulticurveProvider().withDiscountFactor(ccy, dscBumpedPlus), sabr.getSABRParameter(),
            sabr.getSABRGenerator());
        MultipleCurrencyAmount pvBumpedPlus = _valueCalculator.visit(instrument, marketDscBumpedPlus);
        double[] yieldBumpedMinus = curveInt.getYDataAsPrimitive().clone();
        yieldBumpedMinus[loopnode] -= _shift;
        YieldAndDiscountCurve dscBumpedMinus = new YieldCurve(curveInt.getName(), new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumpedMinus, curveInt.getInterpolator(), true));
        SABRSwaptionProviderDiscount marketDscBumpedMinus = new SABRSwaptionProviderDiscount(sabr.getMulticurveProvider().withDiscountFactor(ccy, dscBumpedMinus), sabr.getSABRParameter(),
            sabr.getSABRGenerator());
        MultipleCurrencyAmount pvBumpedMinus = _valueCalculator.visit(instrument, marketDscBumpedMinus);
        MultipleCurrencyAmount pvDiff = pvBumpedPlus.plus(pvBumpedMinus.multipliedBy(-1.0));
        for (int loopccypv = 0; loopccypv < nbCcy; loopccypv++) {
          sensitivity[loopccypv][loopnode] = pvDiff.getAmount(ccyList.get(loopccypv)) / (2 * _shift);
        }
      }
      String name = sabr.getMulticurveProvider().getName(ccy);
      for (int loopccypv = 0; loopccypv < nbCcy; loopccypv++) {
        result = result.plus(new ObjectsPair<String, Currency>(name, ccyList.get(loopccypv)), new DoubleMatrix1D(sensitivity[loopccypv]));
      }
    }
    // Forward ON
    Set<IndexON> indexON = sabr.getMulticurveProvider().getIndexesON();
    for (IndexON index : indexON) {
      YieldAndDiscountCurve curve = sabr.getMulticurveProvider().getCurve(index);
      ArgumentChecker.isTrue(curve instanceof YieldCurve, "Curve should be a YieldCurve");
      YieldCurve curveYield = (YieldCurve) curve;
      ArgumentChecker.isTrue(curveYield.getCurve() instanceof InterpolatedDoublesCurve, "Yield curve should be based on InterpolatedDoublesCurve");
      InterpolatedDoublesCurve curveInt = (InterpolatedDoublesCurve) curveYield.getCurve();
      int nbNodePoint = curveInt.getXDataAsPrimitive().length;
      double[][] sensitivity = new double[nbCcy][nbNodePoint];
      for (int loopnode = 0; loopnode < nbNodePoint; loopnode++) {
        double[] yieldBumpedPlus = curveInt.getYDataAsPrimitive().clone();
        yieldBumpedPlus[loopnode] += _shift;
        YieldAndDiscountCurve fwdBumpedPlus = new YieldCurve(curveInt.getName(), new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumpedPlus, curveInt.getInterpolator(), true));
        SABRSwaptionProviderDiscount marketFwdBumpedPlus = new SABRSwaptionProviderDiscount(sabr.getMulticurveProvider().withForward(index, fwdBumpedPlus), sabr.getSABRParameter(),
            sabr.getSABRGenerator());
        MultipleCurrencyAmount pvBumpedPlus = _valueCalculator.visit(instrument, marketFwdBumpedPlus);
        double[] yieldBumpedMinus = curveInt.getYDataAsPrimitive().clone();
        yieldBumpedMinus[loopnode] -= _shift;
        YieldAndDiscountCurve fwdBumpedMinus = new YieldCurve(curveInt.getName(), new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumpedMinus, curveInt.getInterpolator(), true));
        SABRSwaptionProviderDiscount marketFwdBumpedMinus = new SABRSwaptionProviderDiscount(sabr.getMulticurveProvider().withForward(index, fwdBumpedMinus), sabr.getSABRParameter(),
            sabr.getSABRGenerator());
        MultipleCurrencyAmount pvBumpedMinus = _valueCalculator.visit(instrument, marketFwdBumpedMinus);
        MultipleCurrencyAmount pvDiff = pvBumpedPlus.plus(pvBumpedMinus.multipliedBy(-1.0));
        for (int loopccypv = 0; loopccypv < nbCcy; loopccypv++) {
          sensitivity[loopccypv][loopnode] = pvDiff.getAmount(ccyList.get(loopccypv)) / (2 * _shift);
        }
      }
      String name = sabr.getMulticurveProvider().getName(index);
      for (int loopccypv = 0; loopccypv < nbCcy; loopccypv++) {
        result = result.plus(new ObjectsPair<String, Currency>(name, ccyList.get(loopccypv)), new DoubleMatrix1D(sensitivity[loopccypv]));
      }
    }
    // Forward Ibor
    Set<IborIndex> indexForward = sabr.getMulticurveProvider().getIndexesIbor();
    for (IborIndex index : indexForward) {
      YieldAndDiscountCurve curve = sabr.getMulticurveProvider().getCurve(index);
      ArgumentChecker.isTrue(curve instanceof YieldCurve, "Curve should be a YieldCurve");
      YieldCurve curveYield = (YieldCurve) curve;
      ArgumentChecker.isTrue(curveYield.getCurve() instanceof InterpolatedDoublesCurve, "Yield curve should be based on InterpolatedDoublesCurve");
      InterpolatedDoublesCurve curveInt = (InterpolatedDoublesCurve) curveYield.getCurve();
      int nbNodePoint = curveInt.getXDataAsPrimitive().length;
      double[][] sensitivity = new double[nbCcy][nbNodePoint];
      for (int loopnode = 0; loopnode < nbNodePoint; loopnode++) {
        double[] yieldBumpedPlus = curveInt.getYDataAsPrimitive().clone();
        yieldBumpedPlus[loopnode] += _shift;
        YieldAndDiscountCurve fwdBumpedPlus = new YieldCurve(curveInt.getName(), new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumpedPlus, curveInt.getInterpolator(), true));
        SABRSwaptionProviderDiscount marketFwdBumpedPlus = new SABRSwaptionProviderDiscount(sabr.getMulticurveProvider().withForward(index, fwdBumpedPlus), sabr.getSABRParameter(),
            sabr.getSABRGenerator());
        MultipleCurrencyAmount pvBumpedPlus = _valueCalculator.visit(instrument, marketFwdBumpedPlus);
        double[] yieldBumpedMinus = curveInt.getYDataAsPrimitive().clone();
        yieldBumpedMinus[loopnode] -= _shift;
        YieldAndDiscountCurve fwdBumpedMinus = new YieldCurve(curveInt.getName(), new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumpedMinus, curveInt.getInterpolator(), true));
        SABRSwaptionProviderDiscount marketFwdBumpedMinus = new SABRSwaptionProviderDiscount(sabr.getMulticurveProvider().withForward(index, fwdBumpedMinus), sabr.getSABRParameter(),
            sabr.getSABRGenerator());
        MultipleCurrencyAmount pvBumpedMinus = _valueCalculator.visit(instrument, marketFwdBumpedMinus);
        MultipleCurrencyAmount pvDiff = pvBumpedPlus.plus(pvBumpedMinus.multipliedBy(-1.0));
        for (int loopccypv = 0; loopccypv < nbCcy; loopccypv++) {
          sensitivity[loopccypv][loopnode] = pvDiff.getAmount(ccyList.get(loopccypv)) / (2 * _shift);
        }
      }
      String name = sabr.getMulticurveProvider().getName(index);
      for (int loopccypv = 0; loopccypv < nbCcy; loopccypv++) {
        result = result.plus(new ObjectsPair<String, Currency>(name, ccyList.get(loopccypv)), new DoubleMatrix1D(sensitivity[loopccypv]));
      }
    }
    return result;
  }
}
