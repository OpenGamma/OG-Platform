/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.annuity.definition.FixedAnnuity;
import com.opengamma.financial.interestrate.annuity.definition.VariableAnnuity;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class PresentValueCalculator {

  public double getFixedAnnuity(FixedAnnuity annuity, YieldCurveBundle curves) {
    Validate.notNull(annuity);
    Validate.notNull(curves);
    YieldAndDiscountCurve curve = curves.getCurve(annuity.getFundingCurveName());
    double[] t = annuity.getPaymentTimes();
    double[] c = annuity.getPaymentAmounts();
    int n = annuity.getNumberOfPayments();
    double res = 0;
    for (int i = 0; i < n; i++) {
      res += c[i] * curve.getDiscountFactor(t[i]);
    }
    return res;
  }

  public Map<String, List<Pair<Double, Double>>> getFixedAnnuitySensitivity(FixedAnnuity annuity, YieldCurveBundle curves) {
    String curveName = annuity.getFundingCurveName();
    YieldAndDiscountCurve curve = curves.getCurve(curveName);
    double[] t = annuity.getPaymentTimes();
    double[] c = annuity.getPaymentAmounts();
    int n = annuity.getNumberOfPayments();
    List<Pair<Double, Double>> temp = new ArrayList<Pair<Double, Double>>();
    for (int i = 0; i < n; i++) {
      DoublesPair s = new DoublesPair(t[i], -t[i] * c[i] * curve.getDiscountFactor(t[i]));
      temp.add(s);
    }
    Map<String, List<Pair<Double, Double>>> result = new HashMap<String, List<Pair<Double, Double>>>();
    result.put(curveName, temp);
    return result;
  }

  public double getLiborAnnuity(VariableAnnuity annuity, YieldCurveBundle curves) {
    Validate.notNull(annuity);
    Validate.notNull(curves);
    YieldAndDiscountCurve fundCurve = curves.getCurve(annuity.getFundingCurveName());

    double[] libors = getLiborRates(annuity, curves);
    double[] t = annuity.getPaymentTimes();
    int n = annuity.getNumberOfPayments();
    double res = 0;
    for (int i = 0; i < n; i++) {
      res += libors[i] * fundCurve.getDiscountFactor(t[i]);
    }
    return res;
  }

  public Map<String, List<Pair<Double, Double>>> getLiborAnnuitySensitivity(VariableAnnuity annuity, YieldCurveBundle curves) {

    Validate.notNull(annuity);
    Validate.notNull(curves);
    String fundingCurveName = annuity.getFundingCurveName();
    String liborCurveName = annuity.getLiborCurveName();
    YieldAndDiscountCurve fundCurve = curves.getCurve(fundingCurveName);
    YieldAndDiscountCurve liborCurve = curves.getCurve(liborCurveName);
    double notional = annuity.getNotional();
    double[] libors = getLiborRates(annuity, curves);
    double[] t = annuity.getPaymentTimes();
    double[] deltaStart = annuity.getDeltaStart();
    double[] deltaEnd = annuity.getDeltaEnd();
    int n = annuity.getNumberOfPayments();
    Map<String, List<Pair<Double, Double>>> result = new HashMap<String, List<Pair<Double, Double>>>();

    List<Pair<Double, Double>> temp = new ArrayList<Pair<Double, Double>>();
    DoublesPair s;
    for (int i = 0; i < n; i++) {
      s = new DoublesPair(t[i], -t[i] * fundCurve.getDiscountFactor(t[i]) * libors[i] * notional);
      temp.add(s);
    }

    if (liborCurveName != fundingCurveName) {
      result.put(fundingCurveName, temp);
      temp = new ArrayList<Pair<Double, Double>>();
    }

    double ta, tb, df, dfa, dfb, ratio;
    for (int i = 0; i < n; i++) {
      ta = (i == 0 ? 0.0 : t[i - 1]) + deltaStart[i];
      tb = t[i] + deltaEnd[i];
      df = fundCurve.getDiscountFactor(t[i]);
      dfa = liborCurve.getDiscountFactor(ta);
      dfb = liborCurve.getDiscountFactor(tb);
      ratio = notional * df * dfa / dfb;
      s = new DoublesPair(ta, -ta * ratio);
      temp.add(s);
      s = new DoublesPair(tb, tb * ratio);
      temp.add(s);

    }
    result.put(liborCurveName, temp);

    return result;
  }

  /*
   * gets the libor rates multiplied by year fraction
   */
  private double[] getLiborRates(final VariableAnnuity annuity, YieldCurveBundle curves) {

    YieldAndDiscountCurve curve = curves.getCurve(annuity.getLiborCurveName());
    final int n = annuity.getNumberOfPayments();
    final double[] paymentTimes = annuity.getPaymentTimes();
    final double[] deltaStart = annuity.getDeltaStart();
    final double[] deltaEnd = annuity.getDeltaEnd();
    final double[] libors = new double[n];
    double ta, tb;
    for (int i = 0; i < n; i++) {
      ta = (i == 0 ? 0.0 : paymentTimes[i - 1]) + deltaStart[i];
      tb = paymentTimes[i] + deltaEnd[i];
      libors[i] = (curve.getDiscountFactor(ta) / curve.getDiscountFactor(tb) - 1.0);
    }
    return libors;
  }

}
