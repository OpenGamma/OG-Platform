/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.fra;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.method.PricingMethod;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Method to compute the present value and its sensitivities for a FRA with discounting. The present value is computed as the (forward rate - FRA rate) 
 * multiplied by the notional and the payment accrual factor and discounted to settlement. The discounting to settlement is done using the forward rate over
 * the fixing period. The value is further discounted from settlement to today using the discounting curve.
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{equation*}
 * P^D(0,t_1)\\frac{\\delta_P(F-K)}{1+\\delta_F F} \\quad \\mbox{and}\\quad F = \\frac{1}{\\delta_F}\\left( \\frac{P^j(0,t_1)}{P^j(0,t_2)}-1\\right)
 * \\end{equation*}
 * }
 * This approach is valid subject to a independence hypothesis between the discounting curve and some spread.
 * <P> Reference: Henrard, M. (2010). The irony in the derivatives discounting part II: the crisis. Wilmott Journal, 2(6):301-316.
 */
public class ForwardRateAgreementDiscountingMethod implements PricingMethod {

  /**
   * Compute the present value of a FRA by discounting.
   * @param fra The FRA.
   * @param curves The yield curves. Should contain the discounting and forward curves associated. 
   * @return The present value.
   */
  public CurrencyAmount presentValue(final ZZZForwardRateAgreement fra, final YieldCurveBundle curves) {
    Validate.notNull(fra, "FRA");
    Validate.notNull(curves, "Curves");
    final YieldAndDiscountCurve discountingCurve = curves.getCurve(fra.getFundingCurveName());
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(fra.getForwardCurveName());
    double discountFactorSettlement = discountingCurve.getDiscountFactor(fra.getPaymentTime());
    double forward = (forwardCurve.getDiscountFactor(fra.getFixingPeriodStartTime()) / forwardCurve.getDiscountFactor(fra.getFixingPeriodEndTime()) - 1) / fra.getFixingYearFraction();
    double presentValue = discountFactorSettlement * fra.getPaymentYearFraction() * fra.getNotional() * (forward - fra.getRate()) / (1 + fra.getFixingYearFraction() * forward);
    return CurrencyAmount.of(fra.getCurrency(), presentValue);
  }

  @Override
  public CurrencyAmount presentValue(InterestRateDerivative instrument, YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof ZZZForwardRateAgreement, "Forward rate agreement");
    return presentValue((ZZZForwardRateAgreement) instrument, curves);
  }

  /**
   * Compute the present value sensitivity to rates of a FRA by discounting.
   * @param fra The FRA.
   * @param curves The yield curves. Should contain the discounting and forward curves associated. 
   * @return The present value sensitivity.
   */
  public PresentValueSensitivity presentValueCurveSensitivity(final ZZZForwardRateAgreement fra, final YieldCurveBundle curves) {
    Validate.notNull(fra, "FRA");
    Validate.notNull(curves, "Curves");
    final YieldAndDiscountCurve discountingCurve = curves.getCurve(fra.getFundingCurveName());
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(fra.getForwardCurveName());
    double df = discountingCurve.getDiscountFactor(fra.getPaymentTime());
    double dfForwardStart = forwardCurve.getDiscountFactor(fra.getFixingPeriodStartTime());
    double dfForwardEnd = forwardCurve.getDiscountFactor(fra.getFixingPeriodEndTime());
    double forward = (dfForwardStart / dfForwardEnd - 1.0) / fra.getFixingYearFraction();
    // Backward sweep
    double pvBar = 1.0;
    double forwardBar = df * fra.getPaymentYearFraction() * fra.getNotional() * (1 - (forward - fra.getRate()) / (1 + fra.getFixingYearFraction() * forward) * fra.getFixingYearFraction())
        / (1 + fra.getFixingYearFraction() * forward);
    double dfForwardEndBar = -dfForwardStart / (dfForwardEnd * dfForwardEnd) / fra.getFixingYearFraction() * forwardBar;
    double dfForwardStartBar = 1.0 / (fra.getFixingYearFraction() * dfForwardEnd) * forwardBar;
    double dfBar = fra.getPaymentYearFraction() * fra.getNotional() * (forward - fra.getRate()) / (1 + fra.getFixingYearFraction() * forward) * pvBar;
    Map<String, List<DoublesPair>> resultMap = new HashMap<String, List<DoublesPair>>();
    List<DoublesPair> listDiscounting = new ArrayList<DoublesPair>();
    listDiscounting.add(new DoublesPair(fra.getPaymentTime(), -fra.getPaymentTime() * df * dfBar));
    resultMap.put(fra.getFundingCurveName(), listDiscounting);
    List<DoublesPair> listForward = new ArrayList<DoublesPair>();
    listForward.add(new DoublesPair(fra.getFixingPeriodStartTime(), -fra.getFixingPeriodStartTime() * dfForwardStart * dfForwardStartBar));
    listForward.add(new DoublesPair(fra.getFixingPeriodEndTime(), -fra.getFixingPeriodEndTime() * dfForwardEnd * dfForwardEndBar));
    resultMap.put(fra.getForwardCurveName(), listForward);
    PresentValueSensitivity result = new PresentValueSensitivity(resultMap);
    return result;
  }

  /**
   * Compute the par rate or forward rate of the FRA.
   * @param fra The FRA.
   * @param curves The yield curves. Should contain the discounting and forward curves associated. 
   * @return The par rate.
   */
  public double parRate(final ZZZForwardRateAgreement fra, final YieldCurveBundle curves) {
    Validate.notNull(fra, "FRA");
    Validate.notNull(curves, "Curves");
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(fra.getForwardCurveName());
    double forward = (forwardCurve.getDiscountFactor(fra.getFixingPeriodStartTime()) / forwardCurve.getDiscountFactor(fra.getFixingPeriodEndTime()) - 1) / fra.getFixingYearFraction();
    return forward;
  }

  /**
   * Compute the par rate sensitivity to the rates for a FRA.
   * @param fra The FRA.
   * @param curves The yield curves. Should contain the discounting and forward curves associated. 
   * @return The par rate sensitivity.
   */
  public PresentValueSensitivity parRateCurveSensitivity(final ZZZForwardRateAgreement fra, final YieldCurveBundle curves) {
    Validate.notNull(fra, "FRA");
    Validate.notNull(curves, "Curves");
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(fra.getForwardCurveName());
    double dfForwardStart = forwardCurve.getDiscountFactor(fra.getFixingPeriodStartTime());
    double dfForwardEnd = forwardCurve.getDiscountFactor(fra.getFixingPeriodEndTime());
    // Backward sweep
    double forwardBar = 1.0;
    double dfForwardEndBar = -dfForwardStart / (dfForwardEnd * dfForwardEnd) / fra.getFixingYearFraction() * forwardBar;
    double dfForwardStartBar = 1.0 / (fra.getFixingYearFraction() * dfForwardEnd) * forwardBar;
    Map<String, List<DoublesPair>> resultMap = new HashMap<String, List<DoublesPair>>();
    List<DoublesPair> listForward = new ArrayList<DoublesPair>();
    listForward.add(new DoublesPair(fra.getFixingPeriodStartTime(), -fra.getFixingPeriodStartTime() * dfForwardStart * dfForwardStartBar));
    listForward.add(new DoublesPair(fra.getFixingPeriodEndTime(), -fra.getFixingPeriodEndTime() * dfForwardEnd * dfForwardEndBar));
    resultMap.put(fra.getForwardCurveName(), listForward);
    PresentValueSensitivity result = new PresentValueSensitivity(resultMap);
    return result;
  }

}
