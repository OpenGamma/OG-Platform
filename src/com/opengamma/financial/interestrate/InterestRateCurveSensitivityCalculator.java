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

import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.libor.Libor;
import com.opengamma.financial.interestrate.swap.definition.BasisSwap;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class InterestRateCurveSensitivityCalculator implements InterestRateDerivativeVisitor<Map<String, List<Pair<Double, Double>>>> {

  PresentValueCalculator _pvCalculator = new PresentValueCalculator();

  Map<String, List<Pair<Double, Double>>> getSensitivity(InterestRateDerivative instrument, YieldCurveBundle curves) {
    return instrument.accept(this, curves);
  }

  /*
   * The actual value of the curve is irrelevant - the sensitivity is always 1.0
   */
  @Override
  public Map<String, List<Pair<Double, Double>>> visitCash(Cash cash, YieldCurveBundle curves) {
    String curveName = cash.getYieldCurveName();
    final List<Pair<Double, Double>> temp = new ArrayList<Pair<Double, Double>>();
    temp.add(new DoublesPair(cash.getPaymentTime(), 1.0));
    Map<String, List<Pair<Double, Double>>> result = new HashMap<String, List<Pair<Double, Double>>>();
    result.put(curveName, temp);
    return result;
  }

  @Override
  public Map<String, List<Pair<Double, Double>>> visitForwardRateAgreement(ForwardRateAgreement fra, YieldCurveBundle curves) {
    String curveName = fra.getLiborCurveName();
    YieldAndDiscountCurve curve = curves.getCurve(curveName);
    final double ta = fra.getStartTime();
    final double tb = fra.getEndTime();
    final double delta = tb - ta;
    final double ratio = curve.getDiscountFactor(ta) / curve.getDiscountFactor(tb) / delta;
    final DoublesPair s1 = new DoublesPair(ta, -ta * ratio);
    final DoublesPair s2 = new DoublesPair(tb, tb * ratio);
    final List<Pair<Double, Double>> temp = new ArrayList<Pair<Double, Double>>();
    temp.add(s1);
    temp.add(s2);
    Map<String, List<Pair<Double, Double>>> result = new HashMap<String, List<Pair<Double, Double>>>();
    result.put(curveName, temp);
    return result;
  }

  @Override
  public Map<String, List<Pair<Double, Double>>> visitInterestRateFuture(InterestRateFuture future, YieldCurveBundle curves) {
    String curveName = future.getCurveName();
    YieldAndDiscountCurve curve = curves.getCurve(curveName);
    final double ta = future.getStartTime();
    final double tb = future.getEndTime();
    final double delta = tb - ta;
    final double ratio = curve.getDiscountFactor(ta) / curve.getDiscountFactor(tb) / delta;
    final DoublesPair s1 = new DoublesPair(ta, -ta * ratio);
    final DoublesPair s2 = new DoublesPair(tb, tb * ratio);
    final List<Pair<Double, Double>> temp = new ArrayList<Pair<Double, Double>>();
    temp.add(s1);
    temp.add(s2);
    Map<String, List<Pair<Double, Double>>> result = new HashMap<String, List<Pair<Double, Double>>>();
    result.put(curveName, temp);
    return result;
  }

  @Override
  public Map<String, List<Pair<Double, Double>>> visitLibor(Libor libor, YieldCurveBundle curves) {
    String curveName = libor.getLiborCurveName();
    YieldAndDiscountCurve curve = curves.getCurve(curveName);

    final DoublesPair pair = new DoublesPair(libor.getPaymentTime(), 1 / curve.getDiscountFactor(libor.getPaymentTime()));
    final List<Pair<Double, Double>> temp = new ArrayList<Pair<Double, Double>>();
    temp.add(pair);
    Map<String, List<Pair<Double, Double>>> result = new HashMap<String, List<Pair<Double, Double>>>();
    result.put(curveName, temp);
    return result;
  }

  @Override
  public Map<String, List<Pair<Double, Double>>> visitSwap(FixedFloatSwap swap, YieldCurveBundle curves) {

    double a = _pvCalculator.getFixedAnnuity(swap.getFixedLeg(), curves);
    double b = _pvCalculator.getLiborAnnuity(swap.getFloatingLeg(), curves);
    double bOveraSq = b / a / a;
    Map<String, List<Pair<Double, Double>>> senseA = _pvCalculator.getFixedAnnuitySensitivity(swap.getFixedLeg(), curves);
    Map<String, List<Pair<Double, Double>>> senseB = _pvCalculator.getLiborAnnuitySensitivity(swap.getFloatingLeg(), curves);

    Map<String, List<Pair<Double, Double>>> result = new HashMap<String, List<Pair<Double, Double>>>();
    for (String name : curves.getAllNames()) {
      List<Pair<Double, Double>> temp = new ArrayList<Pair<Double, Double>>();
      if (senseA.containsKey(name)) {
        for (Pair<Double, Double> pair : senseA.get(name)) {
          double t = pair.getFirst();
          DoublesPair newPair = new DoublesPair(t, -bOveraSq * pair.getSecond());
          temp.add(newPair);
        }
      }
      if (senseB.containsKey(name)) {
        for (Pair<Double, Double> pair : senseB.get(name)) {
          double t = pair.getFirst();
          DoublesPair newPair = new DoublesPair(t, pair.getSecond() / a);
          temp.add(newPair);
        }
      }
      result.put(name, temp);
    }
    return result;
  }

  @Override
  public Map<String, List<Pair<Double, Double>>> visitBasisSwap(BasisSwap swap, YieldCurveBundle curves) {

    double a = _pvCalculator.getLiborAnnuity(swap.getRecieveLeg(), curves);
    double b = _pvCalculator.getLiborAnnuity(swap.getPayLeg(), curves);
    double c = _pvCalculator.getFixedAnnuity(swap.getSpreadLeg(), curves);
    Map<String, List<Pair<Double, Double>>> senseA = _pvCalculator.getLiborAnnuitySensitivity(swap.getRecieveLeg(), curves);
    Map<String, List<Pair<Double, Double>>> senseB = _pvCalculator.getLiborAnnuitySensitivity(swap.getPayLeg(), curves);
    Map<String, List<Pair<Double, Double>>> senseC = _pvCalculator.getFixedAnnuitySensitivity(swap.getSpreadLeg(), curves);
    Map<String, List<Pair<Double, Double>>> result = new HashMap<String, List<Pair<Double, Double>>>();

    for (String name : curves.getAllNames()) {
      List<Pair<Double, Double>> temp = new ArrayList<Pair<Double, Double>>();
      if (senseA.containsKey(name)) {
        for (Pair<Double, Double> pair : senseA.get(name)) {
          temp.add(new DoublesPair(pair.getFirst(), pair.getSecond() / c));
        }
      }
      if (senseB.containsKey(name)) {
        for (Pair<Double, Double> pair : senseB.get(name)) {
          temp.add(new DoublesPair(pair.getFirst(), -pair.getSecond() / c));
        }
      }
      if (senseC.containsKey(name)) {
        for (Pair<Double, Double> pair : senseC.get(name)) {
          temp.add(new DoublesPair(pair.getFirst(), (b - a) / c * pair.getSecond()));
        }
      }
      result.put(name, temp);
    }
    return result;
  }

}
