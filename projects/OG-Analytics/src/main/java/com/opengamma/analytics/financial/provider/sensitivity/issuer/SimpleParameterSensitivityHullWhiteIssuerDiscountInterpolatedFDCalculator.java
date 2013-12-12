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
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteIssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
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
public class SimpleParameterSensitivityHullWhiteIssuerDiscountInterpolatedFDCalculator {

  /**
   * The value calculator.
   */
  private final InstrumentDerivativeVisitor<HullWhiteIssuerProviderInterface, Double> _valueCalculator;
  /**
   * The shift used for finite difference.
   */
  private final double _shift;

  /**
   * Constructor
   * @param valueCalculator The value calculator.
   * @param shift The shift used for finite difference.
   */
  public SimpleParameterSensitivityHullWhiteIssuerDiscountInterpolatedFDCalculator(final InstrumentDerivativeVisitor<HullWhiteIssuerProviderInterface, Double> valueCalculator, final double shift) {
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
  public SimpleParameterSensitivity calculateSensitivity(final InstrumentDerivative instrument, final HullWhiteIssuerProviderDiscount issuercurves) {
    SimpleParameterSensitivity result = new SimpleParameterSensitivity();
    final Double valueInit = instrument.accept(_valueCalculator, issuercurves);
    final Double valueInitMinus = -valueInit;
    // Discounting
    final Set<Currency> ccyDiscounting = issuercurves.getMulticurveProvider().getCurrencies();
    for (final Currency ccy : ccyDiscounting) {
      final YieldAndDiscountCurve curve = issuercurves.getMulticurveProvider().getCurve(ccy);
      ArgumentChecker.isTrue(curve instanceof YieldCurve, "Curve should be a YieldCurve");
      final YieldCurve curveYield = (YieldCurve) curve;
      ArgumentChecker.isTrue(curveYield.getCurve() instanceof InterpolatedDoublesCurve, "Yield curve should be based on InterpolatedDoublesCurve");
      final InterpolatedDoublesCurve curveInt = (InterpolatedDoublesCurve) curveYield.getCurve();
      final int nbNodePoint = curveInt.getXDataAsPrimitive().length;
      final double[] sensitivity = new double[nbNodePoint];
      for (int loopnode = 0; loopnode < nbNodePoint; loopnode++) {
        final double[] yieldBumped = curveInt.getYDataAsPrimitive().clone();
        yieldBumped[loopnode] += _shift;
        final YieldAndDiscountCurve dscBumped = new YieldCurve(curveInt.getName(), new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumped, curveInt.getInterpolator(), true));
        final HullWhiteIssuerProviderDiscount marketDscBumped = new HullWhiteIssuerProviderDiscount(new IssuerProviderDiscount(issuercurves.getMulticurveProvider().withDiscountFactor(ccy, dscBumped),
            issuercurves.getIssuerProvider().getIssuerCurves()), issuercurves.getHullWhiteParameters()/*, issuercurves.getHullWhiteIssuerCurrency()*/);
        final Double valueBumped = instrument.accept(_valueCalculator, marketDscBumped);
        final Double valueDiff = valueBumped + valueInitMinus;
        sensitivity[loopnode] = valueDiff / _shift;
      }
      final String name = issuercurves.getMulticurveProvider().getName(ccy);
      result = result.plus(name, new DoubleMatrix1D(sensitivity));
    }
    // Forward ON
    final Set<IndexON> indexON = issuercurves.getMulticurveProvider().getIndexesON();
    for (final IndexON index : indexON) {
      final YieldAndDiscountCurve curve = issuercurves.getMulticurveProvider().getCurve(index);
      ArgumentChecker.isTrue(curve instanceof YieldCurve, "Curve should be a YieldCurve");
      final YieldCurve curveYield = (YieldCurve) curve;
      ArgumentChecker.isTrue(curveYield.getCurve() instanceof InterpolatedDoublesCurve, "Yield curve should be based on InterpolatedDoublesCurve");
      final InterpolatedDoublesCurve curveInt = (InterpolatedDoublesCurve) curveYield.getCurve();
      final int nbNodePoint = curveInt.getXDataAsPrimitive().length;
      final double[] sensitivity = new double[nbNodePoint];
      for (int loopnode = 0; loopnode < nbNodePoint; loopnode++) {
        final double[] yieldBumped = curveInt.getYDataAsPrimitive().clone();
        yieldBumped[loopnode] += _shift;
        final YieldAndDiscountCurve fwdBumped = new YieldCurve(curveInt.getName(), new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumped, curveInt.getInterpolator(), true));
        final HullWhiteIssuerProviderDiscount marketFwdBumped = new HullWhiteIssuerProviderDiscount(new IssuerProviderDiscount(issuercurves.getMulticurveProvider().withForward(index, fwdBumped),
            issuercurves.getIssuerProvider().getIssuerCurves()), issuercurves.getHullWhiteParameters()/*, issuercurves.getHullWhiteIssuerCurrency()*/);
        final Double valueBumped = instrument.accept(_valueCalculator, marketFwdBumped);
        final Double valueDiff = valueBumped + valueInitMinus;
        sensitivity[loopnode] = valueDiff / _shift;
      }
      final String name = issuercurves.getMulticurveProvider().getName(index);
      result = result.plus(name, new DoubleMatrix1D(sensitivity));
    }
    // Forward Ibor - symmetrical
    final Set<IborIndex> indexForward = issuercurves.getMulticurveProvider().getIndexesIbor();
    for (final IborIndex index : indexForward) {
      final YieldAndDiscountCurve curve = issuercurves.getMulticurveProvider().getCurve(index);
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
        final HullWhiteIssuerProviderDiscount marketFwdBumpedPlus = new HullWhiteIssuerProviderDiscount(new IssuerProviderDiscount(issuercurves.getMulticurveProvider().withForward(index,
            fwdBumpedPlus),
            issuercurves.getIssuerProvider().getIssuerCurves()), issuercurves.getHullWhiteParameters()/*, issuercurves.getHullWhiteIssuerCurrency()*/);
        final Double valueBumpedPlus = instrument.accept(_valueCalculator, marketFwdBumpedPlus);
        final double[] yieldBumpedMinus = curveInt.getYDataAsPrimitive().clone();
        yieldBumpedMinus[loopnode] -= _shift;
        final YieldAndDiscountCurve fwdBumpedMinus = new YieldCurve(curveInt.getName(),
            new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumpedMinus, curveInt.getInterpolator(), true));
        final HullWhiteIssuerProviderDiscount marketFwdBumpedMinus = new HullWhiteIssuerProviderDiscount(new IssuerProviderDiscount(issuercurves.getMulticurveProvider().withForward(index,
            fwdBumpedMinus),
            issuercurves.getIssuerProvider().getIssuerCurves()), issuercurves.getHullWhiteParameters()/*, issuercurves.getHullWhiteIssuerCurrency()*/);
        final Double valueBumpedMinus = instrument.accept(_valueCalculator, marketFwdBumpedMinus);
        final Double valueDiff = valueBumpedPlus - valueBumpedMinus;
        sensitivity[loopnode] = valueDiff / (2 * _shift);
      }
      final String name = issuercurves.getMulticurveProvider().getName(index);
      result = result.plus(name, new DoubleMatrix1D(sensitivity));
    }
    // Discounting issuer
    final Set<Pair<Object, LegalEntityFilter<LegalEntity>>> issuerCcies = issuercurves.getIssuerProvider().getIssuers();
    for (final Pair<Object, LegalEntityFilter<LegalEntity>> ic : issuerCcies) {
      final YieldAndDiscountCurve curve = issuercurves.getIssuerProvider().getIssuerCurve(ic);
      ArgumentChecker.isTrue(curve instanceof YieldCurve, "Curve should be a YieldCurve");
      final YieldCurve curveYield = (YieldCurve) curve;
      ArgumentChecker.isTrue(curveYield.getCurve() instanceof InterpolatedDoublesCurve, "Yield curve should be based on InterpolatedDoublesCurve");
      final InterpolatedDoublesCurve curveInt = (InterpolatedDoublesCurve) curveYield.getCurve();
      final int nbNodePoint = curveInt.getXDataAsPrimitive().length;
      final double[] sensitivity = new double[nbNodePoint];
      for (int loopnode = 0; loopnode < nbNodePoint; loopnode++) {
        final double[] yieldBumped = curveInt.getYDataAsPrimitive().clone();
        yieldBumped[loopnode] += _shift;
        final YieldAndDiscountCurve icBumped = new YieldCurve(curveInt.getName(), new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumped, curveInt.getInterpolator(), true));
        final HullWhiteIssuerProviderDiscount providerIcBumped = new HullWhiteIssuerProviderDiscount(issuercurves.getIssuerProvider().withIssuerCurve(ic, icBumped),
            issuercurves.getHullWhiteParameters()/*, issuercurves.getHullWhiteIssuerCurrency()*/);
        final Double valueBumped = instrument.accept(_valueCalculator, providerIcBumped);
        final Double valueDiff = valueBumped + valueInitMinus;
        sensitivity[loopnode] = valueDiff / _shift;
      }
      final String name = issuercurves.getIssuerProvider().getName(ic);
      result = result.plus(name, new DoubleMatrix1D(sensitivity));
    }
    return result;
  }
}
