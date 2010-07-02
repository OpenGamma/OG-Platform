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
public class ForwardCurveSensitivityCalculator {
  private final AnnuityCalculator _annuityCalculator = new AnnuityCalculator();

  //TODO doesn't need to be a list - can see how many points are needed
  public List<Pair<Double, Double>> getForwardCurveSensitivities(final YieldAndDiscountCurve forwardCurve,
      final YieldAndDiscountCurve fundingCurve, final Swap swap) {
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

}
