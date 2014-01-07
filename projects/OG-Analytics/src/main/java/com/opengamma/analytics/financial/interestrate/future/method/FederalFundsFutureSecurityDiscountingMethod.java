/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureSecurity;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Methods for the pricing of Federal Funds futures by discounting (using average of forward rates; not convexity adjustment).
 * @deprecated Use {@link com.opengamma.analytics.financial.interestrate.future.provider.FederalFundsFutureSecurityDiscountingMethod}
 */
@Deprecated
public final class FederalFundsFutureSecurityDiscountingMethod extends FederalFundsFutureSecurityMethod {

  /**
   * Creates the method unique instance.
   */
  private static final FederalFundsFutureSecurityDiscountingMethod INSTANCE = new FederalFundsFutureSecurityDiscountingMethod();

  /**
   * Return the method unique instance.
   * @return The instance.
   */
  public static FederalFundsFutureSecurityDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private FederalFundsFutureSecurityDiscountingMethod() {
  }

  /**
   * Computes the present value of the future security as the value of one future with a price of 0.
   * @param future The future security.
   * @param curves The curves.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final FederalFundsFutureSecurity future, final YieldCurveBundle curves) {
    final double price = price(future, curves);
    final double pv = price * future.getPaymentAccrualFactor() * future.getNotional();
    return CurrencyAmount.of(future.getCurrency(), pv);
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof FederalFundsFutureSecurity, "Federal Funds future security");
    return presentValue((FederalFundsFutureSecurity) instrument, curves);
  }

  @Override
  /**
   * Computes the Federal Funds future price using average of forward rates (not convexity adjustment).
   * @param future The future security.
   * @param curves The curves.
   * @return The price.
   */
  public double price(final FederalFundsFutureSecurity future, final YieldCurveBundle curves) {
    Validate.notNull(future, "Future");
    Validate.notNull(curves, "Curves");
    final int nbFixing = future.getFixingPeriodAccrualFactor().length;
    final YieldAndDiscountCurve ois = curves.getCurve(future.getOISCurveName());
    final double[] df = new double[nbFixing + 1];
    for (int loopfix = 0; loopfix < nbFixing + 1; loopfix++) {
      df[loopfix] = ois.getDiscountFactor(future.getFixingPeriodTime()[loopfix]);
    }
    double interest = future.getAccruedInterest();
    for (int loopfix = 0; loopfix < nbFixing; loopfix++) {
      interest += df[loopfix] / df[loopfix + 1] - 1.0;
    }
    return 1.0 - interest / future.getFixingTotalAccrualFactor();
  }

  /**
   * Computes the interest rate sensitivity of future price.
   * @param future The future security.
   * @param curves The curves.
   * @return The curve sensitivity.
   */
  @Override
  public InterestRateCurveSensitivity priceCurveSensitivity(final FederalFundsFutureSecurity future, final YieldCurveBundle curves) {
    Validate.notNull(future, "Future");
    Validate.notNull(curves, "Curves");
    final int nbFixing = future.getFixingPeriodAccrualFactor().length;
    final YieldAndDiscountCurve ois = curves.getCurve(future.getOISCurveName());
    final double[] df = new double[nbFixing + 1];
    for (int loopfix = 0; loopfix < nbFixing + 1; loopfix++) {
      df[loopfix] = ois.getDiscountFactor(future.getFixingPeriodTime()[loopfix]);
    }
    // Backward sweep
    final double priceBar = 1.0;
    final double interestBar = -1.0 / future.getFixingTotalAccrualFactor() * priceBar;
    final double[] dfBar = new double[nbFixing + 1];
    for (int loopfix = 0; loopfix < nbFixing; loopfix++) {
      dfBar[loopfix] += 1.0 / df[loopfix + 1] * interestBar;
      dfBar[loopfix + 1] += -df[loopfix] / (df[loopfix + 1] * df[loopfix + 1]) * interestBar;
    }
    final Map<String, List<DoublesPair>> resultMap = new HashMap<>();
    final List<DoublesPair> listOIS = new ArrayList<>();
    for (int loopfix = 0; loopfix < nbFixing + 1; loopfix++) {
      listOIS.add(DoublesPair.of(future.getFixingPeriodTime()[loopfix], -future.getFixingPeriodTime()[loopfix] * df[loopfix] * dfBar[loopfix]));
    }
    resultMap.put(future.getOISCurveName(), listOIS);
    final InterestRateCurveSensitivity result = new InterestRateCurveSensitivity(resultMap);
    return result;
  }

}
