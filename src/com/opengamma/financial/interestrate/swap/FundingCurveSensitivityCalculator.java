/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class FundingCurveSensitivityCalculator {
  private final AnnuityCalculator _annuityCalculator = new AnnuityCalculator();
  private final LiborCalculator _liborCalculator = new LiborCalculator();
  private final FloatingLegCalculator _floatingLegCalculator = new FloatingLegCalculator();

  public List<Pair<Double, Double>> getFundingCurveSensitivities(YieldAndDiscountCurve forwardCurve, YieldAndDiscountCurve fundingCurve, Swap swap) {
    final double[] libors = _liborCalculator.getLiborRate(forwardCurve, swap);
    final double annuity = _annuityCalculator.getAnnuity(fundingCurve, swap);
    final double floating = _floatingLegCalculator.getFloatLeg(forwardCurve, fundingCurve, swap);
    final double floatOverAnnSq = -floating / annuity / annuity;
    final List<Pair<Double, Double>> results = new ArrayList<Pair<Double, Double>>();
    Pair<Double, Double> temp;
    int nFix = swap.getNumberOfFixedPayments();
    int nFloat = swap.getNumberOfFloatingPayments();
    double[] floatPaymentTimes = swap.getFloatingPaymentTimes();
    double[] fixedPaymentTimes = swap.getFixedPaymentTimes();
    double[] floatYearFractions = swap.getFloatingYearFractions();
    double[] fixedYearFractions = swap.getFixedYearFractions();

    for (int i = 0; i < nFix; i++) {
      temp =
          new DoublesPair(fixedPaymentTimes[i], -fixedPaymentTimes[i] * fundingCurve.getDiscountFactor(fixedPaymentTimes[i]) * fixedYearFractions[i]
              * floatOverAnnSq);
      results.add(temp);
    }
    for (int i = 0; i < nFloat; i++) {
      temp =
          new DoublesPair(floatPaymentTimes[i], -floatPaymentTimes[i] * fundingCurve.getDiscountFactor(floatPaymentTimes[i]) * libors[i]
              * floatYearFractions[i] / annuity);
      results.add(temp);
    }
    return results;

  }
  //TODO doesn't need to be a list

}
