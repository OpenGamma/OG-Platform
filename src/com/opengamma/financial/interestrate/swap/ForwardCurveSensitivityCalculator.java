/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.libor.Libor;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class ForwardCurveSensitivityCalculator {
  private final AnnuityCalculator _annuityCalculator = new AnnuityCalculator();

  public List<Pair<Double, Double>> getForwardCurveSensitivities(final YieldAndDiscountCurve forwardCurve, final YieldAndDiscountCurve fundingCurve, final InterestRateDerivative derivative) {
    if (derivative instanceof Swap) {
      return getSwapSensitivities(forwardCurve, fundingCurve, (Swap) derivative);
    } else if (derivative instanceof Cash) {
      return getCashSensitivities();
    } else if (derivative instanceof ForwardRateAgreement) {
      return getFRASensitivities(forwardCurve, (ForwardRateAgreement) derivative);
    } else if (derivative instanceof InterestRateFuture) {
      return getIRFutureSensitivities(forwardCurve, (InterestRateFuture) derivative);
    } else if (derivative instanceof Libor) {
      return getLiborSensitivities(forwardCurve, (Libor) derivative);
    }
    throw new IllegalArgumentException("Unhandled InterestRateDerivative type");
  }

  //TODO doesn't need to be a list - can see how many points are needed
  private List<Pair<Double, Double>> getSwapSensitivities(final YieldAndDiscountCurve forwardCurve, final YieldAndDiscountCurve fundingCurve, final Swap swap) {
    Validate.notNull(forwardCurve);
    Validate.notNull(fundingCurve);
    Validate.notNull(swap);
    final double annuity = _annuityCalculator.getAnnuity(fundingCurve, swap);
    final List<Pair<Double, Double>> results = new ArrayList<Pair<Double, Double>>();
    double ta, tb;
    double temp1;
    double modDF, dfta, dftb;
    Pair<Double, Double> temp;
    final int nFloat = swap.getNumberOfFloatingPayments();
    final double[] floatPaymentTimes = swap.getFloatingPaymentTimes();
    final double[] deltaStart = swap.getDeltaStart();
    final double[] deltaEnd = swap.getDeltaEnd();
    final double[] floatYearFractions = swap.getFloatingYearFractions();
    final double[] liborYearFractions = swap.getReferenceYearFractions();
    for (int i = 0; i < nFloat; i++) {
      ta = (i == 0 ? 0.0 : floatPaymentTimes[i - 1]) + deltaStart[i];
      tb = floatPaymentTimes[i] + deltaEnd[i];
      modDF = floatYearFractions[i] / liborYearFractions[i] * fundingCurve.getDiscountFactor(floatPaymentTimes[i]);
      dfta = forwardCurve.getDiscountFactor(ta);
      dftb = forwardCurve.getDiscountFactor(tb);
      temp1 = modDF * dfta / dftb / annuity;
      temp = new DoublesPair(ta, -ta * temp1);
      results.add(temp);
      temp = new DoublesPair(tb, tb * temp1);
      results.add(temp);
    }
    return results;
  }

  private List<Pair<Double, Double>> getCashSensitivities() {
    return new ArrayList<Pair<Double, Double>>();
  }

  private List<Pair<Double, Double>> getFRASensitivities(final YieldAndDiscountCurve forwardCurve, final ForwardRateAgreement fra) {
    Validate.notNull(forwardCurve);
    Validate.notNull(fra);
    final double ta = fra.getStartTime();
    final double tb = fra.getEndTime();
    final double delta = tb - ta;
    final double ratio = forwardCurve.getDiscountFactor(ta) / forwardCurve.getDiscountFactor(tb) / delta;
    final DoublesPair s1 = new DoublesPair(ta, -ta * ratio);
    final DoublesPair s2 = new DoublesPair(tb, tb * ratio);
    final List<Pair<Double, Double>> result = new ArrayList<Pair<Double, Double>>();
    result.add(s1);
    result.add(s2);
    return result;
  }

  private List<Pair<Double, Double>> getIRFutureSensitivities(final YieldAndDiscountCurve forwardCurve, final InterestRateFuture interestRateFuture) {
    Validate.notNull(forwardCurve);
    Validate.notNull(forwardCurve);
    final double ta = interestRateFuture.getStartTime();
    final double tb = interestRateFuture.getEndTime();
    final double delta = tb - ta;
    final double ratio = forwardCurve.getDiscountFactor(ta) / forwardCurve.getDiscountFactor(tb) / delta;
    final DoublesPair s1 = new DoublesPair(ta, -ta * ratio);
    final DoublesPair s2 = new DoublesPair(tb, tb * ratio);
    final List<Pair<Double, Double>> result = new ArrayList<Pair<Double, Double>>();
    result.add(s1);
    result.add(s2);
    return result;
  }

  private List<Pair<Double, Double>> getLiborSensitivities(final YieldAndDiscountCurve forwardCurve, final Libor libor) {
    Validate.notNull(libor);
    Validate.notNull(forwardCurve);
    final DoublesPair pair = new DoublesPair(libor.getPaymentTime(), 1 / forwardCurve.getDiscountFactor(libor.getPaymentTime()));
    final List<Pair<Double, Double>> result = new ArrayList<Pair<Double, Double>>();
    result.add(pair);
    return result;
  }
}
