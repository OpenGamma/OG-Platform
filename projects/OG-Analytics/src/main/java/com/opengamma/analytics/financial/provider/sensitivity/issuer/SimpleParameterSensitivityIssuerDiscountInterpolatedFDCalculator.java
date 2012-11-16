/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.sensitivity.issuer;

import java.util.Set;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.description.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.IssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimpleParameterSensitivity;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * For an instrument, computes the sensitivity of a value (often the par spread) to the parameters used in the curve.
 * The computation is done by shifting each node point in each curve; the curves must be interpolated yield curves for discounting and forward curves.
 * The return format is SimpleParameterSensitivity object.
 * This is a very inefficient way to compute the sensitivities. It should be used only for tests purposes or when speed is irrelevant.
 */
public class SimpleParameterSensitivityIssuerDiscountInterpolatedFDCalculator {

  /**
   * The value calculator.
   */
  private final InstrumentDerivativeVisitor<IssuerProviderInterface, Double> _valueCalculator;
  /**
   * The shift used for finite difference.
   */
  private final double _shift;

  /**
   * Constructor
   * @param valueCalculator The value calculator.
   * @param shift The shift used for finite difference.
   */
  public SimpleParameterSensitivityIssuerDiscountInterpolatedFDCalculator(InstrumentDerivativeVisitor<IssuerProviderInterface, Double> valueCalculator, final double shift) {
    ArgumentChecker.notNull(valueCalculator, "Calculator");
    _valueCalculator = valueCalculator;
    _shift = shift;
  }

  /**
   * Compute the sensitivity by finite difference on all points. The curves must be interpolated yield curves.
   * Only the discounting and forward curves sensitivity is computed.
   * @param instrument The instrument.
   * @param issuercurves The provider: all discounting, forward and issuer curves should be of the type YieldCurve with InterpolatedDoublesCurve.
   * @return The parameter sensitivity.
   */
  public SimpleParameterSensitivity calculateSensitivity(final InstrumentDerivative instrument, final IssuerProviderDiscount issuercurves) {
    SimpleParameterSensitivity result = new SimpleParameterSensitivity();
    final Double valueInit = _valueCalculator.visit(instrument, issuercurves);
    final Double valueInitMinus = -valueInit;
    // Discounting
    Set<Currency> ccyDiscounting = issuercurves.getMulticurveProvider().getCurrencies();
    for (Currency ccy : ccyDiscounting) {
      YieldAndDiscountCurve curve = issuercurves.getMulticurveProvider().getCurve(ccy);
      ArgumentChecker.isTrue(curve instanceof YieldCurve, "Curve should be a YieldCurve");
      YieldCurve curveYield = (YieldCurve) curve;
      ArgumentChecker.isTrue(curveYield.getCurve() instanceof InterpolatedDoublesCurve, "Yield curve should be based on InterpolatedDoublesCurve");
      InterpolatedDoublesCurve curveInt = (InterpolatedDoublesCurve) curveYield.getCurve();
      int nbNodePoint = curveInt.getXDataAsPrimitive().length;
      double[] sensitivity = new double[nbNodePoint];
      for (int loopnode = 0; loopnode < nbNodePoint; loopnode++) {
        double[] yieldBumped = curveInt.getYDataAsPrimitive().clone();
        yieldBumped[loopnode] += _shift;
        YieldAndDiscountCurve dscBumped = new YieldCurve(curveInt.getName(), new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumped, curveInt.getInterpolator(), true));
        IssuerProviderDiscount marketDscBumped = new IssuerProviderDiscount(issuercurves.getMulticurveProvider().withDiscountFactor(ccy, dscBumped), issuercurves.getIssuerCurves());
        Double valueBumped = _valueCalculator.visit(instrument, marketDscBumped);
        Double valueDiff = valueBumped + valueInitMinus;
        sensitivity[loopnode] = valueDiff / _shift;
      }
      String name = issuercurves.getMulticurveProvider().getName(ccy);
      result = result.plus(name, new DoubleMatrix1D(sensitivity));
    }
    // Forward ON
    Set<IndexON> indexON = issuercurves.getMulticurveProvider().getIndexesON();
    for (IndexON index : indexON) {
      YieldAndDiscountCurve curve = issuercurves.getMulticurveProvider().getCurve(index);
      ArgumentChecker.isTrue(curve instanceof YieldCurve, "Curve should be a YieldCurve");
      YieldCurve curveYield = (YieldCurve) curve;
      ArgumentChecker.isTrue(curveYield.getCurve() instanceof InterpolatedDoublesCurve, "Yield curve should be based on InterpolatedDoublesCurve");
      InterpolatedDoublesCurve curveInt = (InterpolatedDoublesCurve) curveYield.getCurve();
      int nbNodePoint = curveInt.getXDataAsPrimitive().length;
      double[] sensitivity = new double[nbNodePoint];
      for (int loopnode = 0; loopnode < nbNodePoint; loopnode++) {
        double[] yieldBumped = curveInt.getYDataAsPrimitive().clone();
        yieldBumped[loopnode] += _shift;
        YieldAndDiscountCurve fwdBumped = new YieldCurve(curveInt.getName(), new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumped, curveInt.getInterpolator(), true));
        IssuerProviderDiscount marketFwdBumped = new IssuerProviderDiscount(issuercurves.getMulticurveProvider().withForward(index, fwdBumped), issuercurves.getIssuerCurves());
        Double valueBumped = _valueCalculator.visit(instrument, marketFwdBumped);
        Double valueDiff = valueBumped + valueInitMinus;
        sensitivity[loopnode] = valueDiff / _shift;
      }
      String name = issuercurves.getMulticurveProvider().getName(index);
      result = result.plus(name, new DoubleMatrix1D(sensitivity));
    }
    // Forward Ibor - symmetrical
    Set<IborIndex> indexForward = issuercurves.getMulticurveProvider().getIndexesIbor();
    for (IborIndex index : indexForward) {
      YieldAndDiscountCurve curve = issuercurves.getMulticurveProvider().getCurve(index);
      ArgumentChecker.isTrue(curve instanceof YieldCurve, "Curve should be a YieldCurve");
      YieldCurve curveYield = (YieldCurve) curve;
      ArgumentChecker.isTrue(curveYield.getCurve() instanceof InterpolatedDoublesCurve, "Yield curve should be based on InterpolatedDoublesCurve");
      InterpolatedDoublesCurve curveInt = (InterpolatedDoublesCurve) curveYield.getCurve();
      int nbNodePoint = curveInt.getXDataAsPrimitive().length;
      double[] sensitivity = new double[nbNodePoint];
      for (int loopnode = 0; loopnode < nbNodePoint; loopnode++) {
        double[] yieldBumpedPlus = curveInt.getYDataAsPrimitive().clone();
        yieldBumpedPlus[loopnode] += _shift;
        YieldAndDiscountCurve fwdBumpedPlus = new YieldCurve(curveInt.getName(), new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumpedPlus, curveInt.getInterpolator(), true));
        IssuerProviderDiscount marketFwdBumpedPlus = new IssuerProviderDiscount(issuercurves.getMulticurveProvider().withForward(index, fwdBumpedPlus), issuercurves.getIssuerCurves());
        Double valueBumpedPlus = _valueCalculator.visit(instrument, marketFwdBumpedPlus);
        double[] yieldBumpedMinus = curveInt.getYDataAsPrimitive().clone();
        yieldBumpedMinus[loopnode] -= _shift;
        YieldAndDiscountCurve fwdBumpedMinus = new YieldCurve(curveInt.getName(), new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumpedMinus, curveInt.getInterpolator(), true));
        IssuerProviderDiscount marketFwdBumpedMinus = new IssuerProviderDiscount(issuercurves.getMulticurveProvider().withForward(index, fwdBumpedMinus), issuercurves.getIssuerCurves());
        Double valueBumpedMinus = _valueCalculator.visit(instrument, marketFwdBumpedMinus);
        Double valueDiff = valueBumpedPlus - valueBumpedMinus;
        sensitivity[loopnode] = valueDiff / (2 * _shift);
      }
      String name = issuercurves.getMulticurveProvider().getName(index);
      result = result.plus(name, new DoubleMatrix1D(sensitivity));
    }
    // Discounting issuer
    Set<Pair<String, Currency>> issuerCcies = issuercurves.getIssuersCurrencies();
    for (Pair<String, Currency> ic : issuerCcies) {
      YieldAndDiscountCurve curve = issuercurves.getCurve(ic);
      ArgumentChecker.isTrue(curve instanceof YieldCurve, "Curve should be a YieldCurve");
      YieldCurve curveYield = (YieldCurve) curve;
      ArgumentChecker.isTrue(curveYield.getCurve() instanceof InterpolatedDoublesCurve, "Yield curve should be based on InterpolatedDoublesCurve");
      InterpolatedDoublesCurve curveInt = (InterpolatedDoublesCurve) curveYield.getCurve();
      int nbNodePoint = curveInt.getXDataAsPrimitive().length;
      double[] sensitivity = new double[nbNodePoint];
      for (int loopnode = 0; loopnode < nbNodePoint; loopnode++) {
        double[] yieldBumped = curveInt.getYDataAsPrimitive().clone();
        yieldBumped[loopnode] += _shift;
        YieldAndDiscountCurve icBumped = new YieldCurve(curveInt.getName(), new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumped, curveInt.getInterpolator(), true));
        IssuerProviderDiscount providerIcBumped = issuercurves.withIssuerCurrency(ic, icBumped);
        Double valueBumped = _valueCalculator.visit(instrument, providerIcBumped);
        Double valueDiff = valueBumped + valueInitMinus;
        sensitivity[loopnode] = valueDiff / _shift;
      }
      String name = issuercurves.getName(ic);
      result = result.plus(name, new DoubleMatrix1D(sensitivity));
    }
    return result;
  }
}
