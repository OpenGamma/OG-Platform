/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.annuity.definition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.PresentValueSensitivityCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.rootfinding.BracketRoot;
import com.opengamma.math.rootfinding.RealSingleRootFinder;
import com.opengamma.math.rootfinding.VanWijngaardenDekkerBrentSingleRootFinder;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public final class ZSpreadCalculator {

  private static PresentValueCalculator s_pvc = PresentValueCalculator.getInstance();
  private static BracketRoot s_bracketRoot = new BracketRoot();
  private static final RealSingleRootFinder s_root = new VanWijngaardenDekkerBrentSingleRootFinder();
  private static final ZSpreadCalculator s_instance = new ZSpreadCalculator();

  private ZSpreadCalculator() {
  }

  public static ZSpreadCalculator getInstance() {
    return s_instance;
  }

  public double calculateZSpread(final GenericAnnuity<? extends Payment> annuity, final YieldCurveBundle curves, final double price) {
    Validate.notNull(annuity, "annuity");
    Validate.notNull(curves, "curves");

    final Function1D<Double, Double> f = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double y) {
        return calculatePriceForZSpread(annuity, curves, y) - price;
      }
    };

    final double[] range = s_bracketRoot.getBracketedPoints(f, 0.0, 0.2);
    return s_root.getRoot(f, range[0], range[1]);
  }

  public double calculatePriceForZSpread(final GenericAnnuity<? extends Payment> annuity, final YieldCurveBundle curves, final double zSpread) {
    Validate.notNull(annuity, "annuity");
    Validate.notNull(curves, "curves");

    double sum = 0;

    final int n = annuity.getNumberOfPayments();
    Payment payment;
    for (int i = 0; i < n; i++) {
      payment = annuity.getNthPayment(i);
      double temp = s_pvc.getValue(payment, curves);
      sum += temp * Math.exp(-zSpread * payment.getPaymentTime());
    }
    return sum;
  }

  public double calculatePriceSensitivityToZSpread(final GenericAnnuity<? extends Payment> annuity, final YieldCurveBundle curves, final double zSpread) {
    Validate.notNull(annuity, "annuity");
    Validate.notNull(curves, "curves");

    double sum = 0;

    final int n = annuity.getNumberOfPayments();
    Payment payment;
    for (int i = 0; i < n; i++) {
      payment = annuity.getNthPayment(i);
      double temp = s_pvc.getValue(payment, curves);
      double time = payment.getPaymentTime();
      sum -= time * temp * Math.exp(-zSpread * time);
    }
    return sum;
  }

  public Map<String, List<DoublesPair>> calculatePriceSensitivityToCurve(final GenericAnnuity<? extends Payment> annuity, final YieldCurveBundle curves, final double zSpread) {
    Validate.notNull(annuity, "annuity");
    Validate.notNull(curves, "curves");

    Map<String, List<DoublesPair>> temp = PresentValueSensitivityCalculator.getInstance().getValue(annuity, curves);
    if (zSpread == 0.0) {
      return temp;
    }
    Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    for (String name : temp.keySet()) {
      List<DoublesPair> unadjusted = temp.get(name);
      ArrayList<DoublesPair> adjusted = new ArrayList<DoublesPair>(unadjusted.size());
      for (DoublesPair pair : unadjusted) {
        DoublesPair newPair = new DoublesPair(pair.first, pair.second * Math.exp(-zSpread * pair.first));
        adjusted.add(newPair);
      }
      result.put(name, adjusted);
    }
    return result;
  }

  public Map<String, List<DoublesPair>> calculateZSpreadSensitivityToCurve(final GenericAnnuity<? extends Payment> annuity, final YieldCurveBundle curves, final double zSpread) {
    Validate.notNull(annuity, "annuity");
    Validate.notNull(curves, "curves");

    double dPricedZ = calculatePriceSensitivityToZSpread(annuity, curves, zSpread);
    Validate.isTrue(dPricedZ != 0.0, "Price Sensitivity To ZSpread is zero");

    Map<String, List<DoublesPair>> temp = PresentValueSensitivityCalculator.getInstance().getValue(annuity, curves);

    Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    for (String name : temp.keySet()) {
      List<DoublesPair> unadjusted = temp.get(name);
      ArrayList<DoublesPair> adjusted = new ArrayList<DoublesPair>(unadjusted.size());
      for (DoublesPair pair : unadjusted) {
        DoublesPair newPair = new DoublesPair(pair.first, -pair.second * Math.exp(-zSpread * pair.first) / dPricedZ);
        adjusted.add(newPair);
      }
      result.put(name, adjusted);
    }
    return result;
  }
}
