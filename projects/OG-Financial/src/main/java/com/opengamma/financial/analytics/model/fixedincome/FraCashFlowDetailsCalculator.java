/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.financial.security.irs.PayReceiveType;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.time.Tenor;

/**
 * Generates a pay or receive cashflow for a FRA. These are 
 * reported as {@link SwapLegCashFlows} to be consistent with 
 * the IR cashflow result type for swaps.
 * <p>
 * This implementation reports a "fixed" and "float" cashflow
 * for the FRA. Whilst these do not exist in reality (only a
 * single, netted cashflow is paid), it is convenient from
 * a reporting point of view for the flows to be consistent
 * with those for swaps. (Similarly, swap cashflow payments
 * are in reality netted, but split out as separate sets of
 * cashflows).
 * 
 * The "Pay" leg is taken to be the flow with a negative PV.
 */
public class FraCashFlowDetailsCalculator extends InstrumentDerivativeVisitorAdapter<ForwardRateAgreementDefinition, SwapLegCashFlows> {
  
  private final MulticurveProviderDiscount _multicurve;
  private final PayReceiveType _type;
  
  /**
   * @param multicurve the curve to use for discounting and forward rate computation
   * @param type the type of cashflow to produce - pay or receive
   */
  public FraCashFlowDetailsCalculator(MulticurveProviderDiscount multicurve, PayReceiveType type) {
    _multicurve = multicurve;
    _type = type;
  }

  @Override
  public SwapLegCashFlows visitForwardRateAgreement(ForwardRateAgreement fra, ForwardRateAgreementDefinition fraDefinition) {
    
    double settlementDf = _multicurve.getDiscountFactor(fra.getCurrency(), fra.getPaymentTime());
    double forwardRate = _multicurve.getSimplyCompoundForwardRate(fra.getIndex(), fra.getFixingPeriodStartTime(), fra.getFixingPeriodEndTime(), fra.getFixingYearFraction());;
    double paymentYearFraction = fraDefinition.getPaymentYearFraction();
    
    double fixedRate = fraDefinition.getRate();
    double fixedProjectedAmount = -fraDefinition.getNotional() * paymentYearFraction * fixedRate / (1 + paymentYearFraction * forwardRate);
    double fixedPresentValue = settlementDf * fixedProjectedAmount;
    
    if (PayReceiveType.PAY.equals(_type) == fixedPresentValue <= 0) {
      FixedCashFlowDetails fixedCashFlow = fixedCashFlow(fraDefinition, settlementDf, fixedProjectedAmount, fixedPresentValue);
      return FixedLegCashFlows.builder().cashFlowDetails(fixedCashFlow).build();
    } else {
      double projectedAmount = fraDefinition.getNotional() * paymentYearFraction * forwardRate / (1 + paymentYearFraction * forwardRate);
      double presentValue = settlementDf * projectedAmount;
      FloatingCashFlowDetails floatCashFlow = floatCashFlow(fraDefinition, fra, settlementDf, forwardRate, projectedAmount, presentValue);
      return FloatingLegCashFlows.builder().cashFlowDetails(floatCashFlow).build();
    }
    
  }

  private FloatingCashFlowDetails floatCashFlow(ForwardRateAgreementDefinition fraDefinition, 
                                                ForwardRateAgreement fra, 
                                                double settlementDf, 
                                                double forwardRate, 
                                                double projectedAmount, 
                                                double presentValue) {
    FloatingCashFlowDetails.Builder builder = FloatingCashFlowDetails.builder();
    builder.accrualEndDate(fraDefinition.getAccrualEndDate().toLocalDate());
    builder.accrualFactor(fraDefinition.getFixingPeriodAccrualFactor());
    builder.accrualStartDate(fraDefinition.getAccrualStartDate().toLocalDate());
    builder.df(settlementDf);
    builder.fixingEndDate(fraDefinition.getFixingPeriodEndDate().toLocalDate());
    builder.fixingStartDate(fraDefinition.getFixingPeriodStartDate().toLocalDate());
    builder.fixingYearFrac(fra.getFixingYearFraction());
    builder.forwardRate(forwardRate);
    builder.indexTenors(Tenor.of(fraDefinition.getIndex().getTenor()));
    builder.notional(CurrencyAmount.of(fraDefinition.getCurrency(), fraDefinition.getNotional()));
    builder.paymentDate(fraDefinition.getPaymentDate().toLocalDate());
    builder.presentValue(CurrencyAmount.of(fraDefinition.getCurrency(), presentValue));
    builder.projectedAmount(CurrencyAmount.of(fraDefinition.getCurrency(), projectedAmount));
    return builder.build();
  }

  private FixedCashFlowDetails fixedCashFlow(ForwardRateAgreementDefinition fraDefinition, 
                                             double df, 
                                             double projectedAmount, 
                                             double presentValue) {
    FixedCashFlowDetails.Builder builder = FixedCashFlowDetails.builder();
    builder.accrualEndDate(fraDefinition.getAccrualEndDate().toLocalDate());
    builder.accrualFactor(fraDefinition.getFixingPeriodAccrualFactor());
    builder.accrualStartDate(fraDefinition.getAccrualStartDate().toLocalDate());
    builder.df(df);
    //note: notional is positive w.r.t. payer of floating rate);
    builder.notional(CurrencyAmount.of(fraDefinition.getCurrency(), -fraDefinition.getNotional())); 
    builder.paymentDate(fraDefinition.getPaymentDate().toLocalDate());
    builder.presentValue(CurrencyAmount.of(fraDefinition.getCurrency(), presentValue));
    builder.projectedAmount(CurrencyAmount.of(fraDefinition.getCurrency(), projectedAmount));
    builder.rate(fraDefinition.getRate());
    FixedCashFlowDetails fixedCashFlow = builder.build();
    return fixedCashFlow;
  }
  
}
