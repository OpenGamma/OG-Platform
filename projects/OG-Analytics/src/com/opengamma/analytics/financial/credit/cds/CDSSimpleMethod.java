/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.cds;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountAddZeroSpreadCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Do the actual CDS calculations
 * 
 * @author Niels Stchedroff
 */
public class CDSSimpleMethod implements PricingMethod {

  private static final Logger s_logger = LoggerFactory.getLogger(CDSSimpleMethod.class);
  
  @Override
  public CurrencyAmount presentValue(InstrumentDerivative instrument, YieldCurveBundle curves) {
    
    CDSDerivative cds = (CDSDerivative) instrument;
    YieldAndDiscountCurve cdsCcyCurve = curves.getCurve(cds.getDiscountCurveName());
    YieldAndDiscountCurve bondCcyCurve = curves.getCurve(cds.getUnderlyingDiscountCurveName());
    YieldAndDiscountCurve spreadCurve = curves.getCurve(cds.getSpreadCurveName());
    
    return CurrencyAmount.of(cds.getPremium().getCurrency(), calculate(cds, cdsCcyCurve, bondCcyCurve, spreadCurve));
  }
  
  /**
   * Build the credit curve from the bond curve and the spread curve
   * 
   * @param bondCcyCurve
   * @param spreadCurve
   * @returnThe combined curve
   */
  private static YieldAndDiscountAddZeroSpreadCurve buildCreditCurve(YieldAndDiscountCurve bondCcyCurve, YieldAndDiscountCurve spreadCurve) {
    return new YieldAndDiscountAddZeroSpreadCurve(bondCcyCurve.getName() + "_" + spreadCurve.getName(), false, bondCcyCurve, spreadCurve);
  }
  
  public static double calculate(CDSDerivative cds, YieldAndDiscountCurve cdsCcyCurve, YieldAndDiscountCurve bondCcyCurve, YieldAndDiscountCurve spreadCurve) {
    YieldAndDiscountCurve creditCurve = buildCreditCurve(bondCcyCurve, spreadCurve);
    return calculateDefaultLeg(cds, cdsCcyCurve, bondCcyCurve, creditCurve) - calculatePremiumLeg(cds, cdsCcyCurve, bondCcyCurve, creditCurve);
  }
  
  public static double calculatePremiumLeg(CDSDerivative cds, YieldAndDiscountCurve cdsCcyCurve, YieldAndDiscountCurve bondCcyCurve, YieldAndDiscountCurve creditCurve) {
    
    final CouponFixed[] premiumPayments = cds.getPremium().getPayments();
    final double oneMinusRecoveryRate = 1.0 - cds.getRecoveryRate();

    double total = 0.0;
    
    for (int i = 0; i < premiumPayments.length; ++i) {
      
      final double t = premiumPayments[i].getPaymentTime();
      
      // Test data is using periodic interest rates and needs converting to make numbers agree with Excel
      // final double cdsCcyDiscountFactor = (new PeriodicInterestRate(cdsCcyCurve.getInterestRate(t), 1)).toContinuous().getDiscountFactor(t);
      // final double bondCcyDiscountFactor = (new PeriodicInterestRate(bondCcyCurve.getInterestRate(t), 1)).toContinuous().getDiscountFactor(t);
      // final double riskyDiscountFactor = (new PeriodicInterestRate(creditCurve.getInterestRate(t), 1)).toContinuous().getDiscountFactor(t);
      
      final double cdsCcyDiscountFactor = cdsCcyCurve.getDiscountFactor(t);
      final double bondCcyDiscountFactor = bondCcyCurve.getDiscountFactor(t);
      final double riskyDiscountFactor = creditCurve.getDiscountFactor(t);
      
      final double probabilityOfDefault = (1.0 - (riskyDiscountFactor / bondCcyDiscountFactor)) / oneMinusRecoveryRate;
      final double discountedExpectedCashflow = premiumPayments[i].getAmount() * (1.0 - probabilityOfDefault) * cdsCcyDiscountFactor;
      total += discountedExpectedCashflow;
      
      if (s_logger.isDebugEnabled()) {
        s_logger.debug("t = " + t
          + ", cdsCcyDiscountFactor = " + cdsCcyDiscountFactor + ", bondCcyDiscountFactor = " + bondCcyDiscountFactor + ", riskyDiscountFactor = " + riskyDiscountFactor
          + ", probabilityOfDefault = " + probabilityOfDefault + ", discountedExpectedCashflow = " + discountedExpectedCashflow);
      }
    }
    
    if (s_logger.isDebugEnabled()) {
      s_logger.debug("total = " + total);
    }
    
    return total;
  }
  
  public static double calculateDefaultLeg(CDSDerivative cds, YieldAndDiscountCurve cdsCcyCurve, YieldAndDiscountCurve bondCcyCurve, YieldAndDiscountCurve creditCurve) {
    
    final PaymentFixed[] possibleDefaultPayments = cds.getPayout().getPayments();
    final double oneMinusRecoveryRate = 1.0 - cds.getRecoveryRate();
    double probabilityOfPriorDefault = 0.0;
    double total = 0.0;
    
    for (int i = 0; i < possibleDefaultPayments.length; ++i) {
      
      final double t = possibleDefaultPayments[i].getPaymentTime();
      
      // Test data is using periodic interest rates and needs converting to make numbers agree with Excel
      // final double cdsCcyDiscountFactor = (new PeriodicInterestRate(cdsCcyCurve.getInterestRate(t), 1)).toContinuous().getDiscountFactor(t);
      // final double bondCcyDiscountFactor = (new PeriodicInterestRate(bondCcyCurve.getInterestRate(t), 1)).toContinuous().getDiscountFactor(t);
      // final double riskyDiscountFactor = (new PeriodicInterestRate(creditCurve.getInterestRate(t), 1)).toContinuous().getDiscountFactor(t);
      
      final double cdsCcyDiscountFactor = cdsCcyCurve.getDiscountFactor(t);
      final double bondCcyDiscountFactor = bondCcyCurve.getDiscountFactor(t);
      final double riskyDiscountFactor = creditCurve.getDiscountFactor(t);
      
      final double probabilityOfDefault = (1.0 - (riskyDiscountFactor / bondCcyDiscountFactor)) / oneMinusRecoveryRate;
      final double discountedExpectedCashflow = possibleDefaultPayments[i].getAmount() * (probabilityOfDefault - probabilityOfPriorDefault) * cdsCcyDiscountFactor;
      total += discountedExpectedCashflow;
      
      probabilityOfPriorDefault = probabilityOfDefault;
      
      if (s_logger.isDebugEnabled()) {
        s_logger.debug("t = " + t
          + ", cdsCcyDiscountFactor = " + cdsCcyDiscountFactor + ", bondCcyDiscountFactor = " + bondCcyDiscountFactor + ", riskyDiscountFactor = " + riskyDiscountFactor
          + ", probabilityOfDefault = " + probabilityOfDefault + ", discountedExpectedCashflow = " + discountedExpectedCashflow);
      }
    }
    
    if (s_logger.isDebugEnabled()) {
      s_logger.debug("total = " + total);
    }
    
    return total;
  }
  
}
