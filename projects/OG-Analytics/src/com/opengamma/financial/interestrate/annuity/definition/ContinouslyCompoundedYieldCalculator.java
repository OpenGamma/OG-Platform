/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.annuity.definition;


/**
 * 
 */
//public final class ContinouslyCompoundedYieldCalculator {
//
//  private static ContinouslyCompoundedYieldCalculator s_instance = new ContinouslyCompoundedYieldCalculator();
//  private static BracketRoot s_bracketRoot = new BracketRoot();
//  private static final RealSingleRootFinder s_root = new VanWijngaardenDekkerBrentSingleRootFinder();
//
//  public static ContinouslyCompoundedYieldCalculator getInstance() {
//    return s_instance;
//  }
//
//  private ContinouslyCompoundedYieldCalculator() {
//  }
//
//  /**
//   * For a set of future cash flows with an assumed present value (dirty price), calculates the continuously compounded constant interest rate that gives the 
//   * same present value
//   * @param annuity Set of known cash flows 
//   * @param pv The present value of the future cash flows. Also know as dirty or full price
//   * @return continuously compounded yield (as a fraction) 
//   */
//  public double calculateYield(final FixedAnnuity annuity, final double pv) {
//    Validate.notNull(annuity, "annuity");
//
//    final Function1D<Double, Double> f = new Function1D<Double, Double>() {
//
//      @Override
//      public Double evaluate(final Double y) {
//        return calculatePriceForYield(annuity, y) - pv;
//      }
//
//    };
//
//    double[] range = s_bracketRoot.getBracketedPoints(f, 0.0, 0.2);
//    return s_root.getRoot(f, range[0], range[1]);
//  }
//
//  /**
//   * Calculate the present value of a set of cash flows given a yield 
//   * @param annuity  Set of known cash flows 
//   * @param yield Continuously compounded constant interest rate 
//   * @return Present value (dirty price)
//   */
//  public double calculatePriceForYield(final FixedAnnuity annuity, final double yield) {
//    double sum = 0;
//    double[] payments = annuity.getPaymentAmounts();
//    double[] times = annuity.getPaymentTimes();
//    int n = payments.length;
//    for (int i = 0; i < n; i++) {
//      sum += payments[i] * Math.exp(-yield * times[i]);
//    }
//    return sum;
//  }
//
//  /**
//   * The (negative) of the first derivative of the PV of a set of cash flows with respect to its continuously compounded yield. This is Macaulay duration times the PV
//   * @param annuity Set of known cash flows  
//   * @param pv The present value of the future cash flows. Also know as dirty or full price
//   * @return Dollar Duration
//   */
//  public double calculateDollarDuration(final FixedAnnuity annuity, final double pv) {
//    double yield = calculateYield(annuity, pv);
//    return calculateDollarDurationFromYield(annuity, yield);
//  }
//
//  /**
//   *  The (negative) of the first derivative of the PV of a set of cash flows with respect to its continuously compounded yield. This is Macaulay duration times the PV
//   * @param annuity Set of known cash flows 
//   * @param yield Continuously compounded constant interest rate 
//   * @return Dollar Duration
//   */
//  public double calculateDollarDurationFromYield(final FixedAnnuity annuity, final double yield) {
//    double sum = 0;
//    double[] payments = annuity.getPaymentAmounts();
//    double[] times = annuity.getPaymentTimes();
//    int n = payments.length;
//    for (int i = 0; i < n; i++) {
//      sum += payments[i] * times[i] * Math.exp(-yield * times[i]);
//    }
//    return sum;
//  }
//
//  /**
//   * Calculates the Macaulay duration, i.e. The (negative) of the first derivative of the PV of a set of cash flows with respect to its continuously compounded yield 
//   * divided by the PV. 
//   * @param annuity Set of known cash flows 
//   * @param pv The present value of the future cash flows. Also know as dirty or full price
//   * @return The Macaulay duration
//   */
//  public double calculateDuration(final FixedAnnuity annuity, final double pv) {
//    if (pv == 0.0) {
//      throw new IllegalArgumentException("pv cannot be zero for duration calculation");
//    }
//    double dollarDuration = calculateDollarDuration(annuity, pv);
//    return dollarDuration / pv;
//  }
//
//  /**
//   * Calculates the Macaulay duration, i.e. The (negative) of the first derivative of the PV of a set of cash flows with respect to its continuously compounded yield 
//   * divided by the PV. 
//   * @param annuity Set of known cash flows
//   * @param yield Continuously compounded constant interest rate 
//   * @return The Macaulay duration
//   */
//  public double calculateDurationFromYield(final FixedAnnuity annuity, final Double yield) {
//    double price = calculatePriceForYield(annuity, yield);
//    double dollarDur = calculateDollarDurationFromYield(annuity, yield);
//    return dollarDur / price;
//  }
//
//  /**
//   * The convexity of a set of cash flows, i.e. the second derivative of its PV with respect to its continuously compounded yield
//   * @param annuity Set of known cash flows
//   * @param pv The present value of the future cash flows. Also know as dirty or full price
//   * @return The convexity
//   */
//  public double calculateConvexity(final FixedAnnuity annuity, final double pv) {
//    double yield = calculateYield(annuity, pv);
//    return calculateConvexityFromYield(annuity, yield);
//  }
//
//  /**
//   * The convexity of a set of cash flows, i.e. the second derivative of its PV with respect to its continuously compounded yield
//   * @param annuity Set of known cash flows
//   * @param yield Continuously compounded constant interest rate 
//   * @return The convexity
//   */
//  public double calculateConvexityFromYield(final FixedAnnuity annuity, final double yield) {
//    double sum = 0;
//    double[] payments = annuity.getPaymentAmounts();
//    double[] times = annuity.getPaymentTimes();
//    int n = payments.length;
//    for (int i = 0; i < n; i++) {
//      sum += payments[i] * times[i] * times[i] * Math.exp(-yield * times[i]);
//    }
//    return sum;
//  }
//
//  /**
//   * For a set of cash flows calculates the nth derivative of its PV with respect to its continuously compounded yield
//   * @param annuity Set of known cash flows
//   * @param pv The present value of the future cash flows. Also know as dirty or full price
//   * @param n The order of the derivative 
//   * @return nth order yield sensitivity
//   */
//  public double calculateNthOrderSensitivity(final FixedAnnuity annuity, final double pv, int n) {
//    double yield = calculateYield(annuity, pv);
//    return calculateNthOrderSensitivityFromYield(annuity, yield, n);
//  }
//
//  /**
//   * For a set of cash flows calculates the nth derivative of its PV with respect to its continuously compounded yield
//   * @param annuity Set of known cash flows
//   * @param yield Continuously compounded constant interest rate 
//   * @param n The order of the derivative 
//   * @return nth order yield sensitivity
//   */
//  public double calculateNthOrderSensitivityFromYield(final FixedAnnuity annuity, final double yield, int n) {
//    double sum = 0;
//    double[] payments = annuity.getPaymentAmounts();
//    double[] times = annuity.getPaymentTimes();
//    int m = payments.length;
//    for (int i = 0; i < m; i++) {
//      sum += payments[i] * Math.pow(times[i], n) * Math.exp(-yield * times[i]);
//    }
//    return sum;
//  }

//}
