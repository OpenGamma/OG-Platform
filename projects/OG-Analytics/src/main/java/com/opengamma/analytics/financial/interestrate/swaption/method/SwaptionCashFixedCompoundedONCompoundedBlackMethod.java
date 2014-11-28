/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.method;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.ParRateCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;
import com.opengamma.analytics.financial.interestrate.sensitivity.PresentValueSwaptionSurfaceSensitivity;
import com.opengamma.analytics.financial.interestrate.swap.method.SwapFixedCompoundingONCompoundingDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedCompoundedONCompounded;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;

/**
 *  Class used to compute the price and sensitivity of a cash-settled swaption with the Black model.
 *  @deprecated Use {@link com.opengamma.analytics.financial.interestrate.swaption.provider.SwaptionCashFixedIborBlackMethod}
 */
// TODO: Complete the code when the definition of cash settlement is clear for those swaptions.
@Deprecated
public final class SwaptionCashFixedCompoundedONCompoundedBlackMethod implements PricingMethod {

  /**
   * The method unique instance.
   */
  private static final SwaptionCashFixedCompoundedONCompoundedBlackMethod INSTANCE = new SwaptionCashFixedCompoundedONCompoundedBlackMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static SwaptionCashFixedCompoundedONCompoundedBlackMethod getInstance() {
    return INSTANCE;
  }

  /**
   * The present value curve sensitivity calculator.
   */
  private static final PresentValueCurveSensitivityCalculator PV_SENSITIVITY_CALCULATOR = PresentValueCurveSensitivityCalculator.getInstance();
  /**
   * The present value calculator.
   */
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();

  /**
   * Private constructor.
   */
  private SwaptionCashFixedCompoundedONCompoundedBlackMethod() {
  }

  /**
   * The swap method.
   */
  private static final SwapFixedCompoundingONCompoundingDiscountingMethod METHOD_SWAP = SwapFixedCompoundingONCompoundingDiscountingMethod.getInstance();
  /**
   * The par rate calculator.
   */
  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();

  /**
   * Computes the present value of a cash-settled European swaption in the Black model.
   * @param swaption The swaption.
   * @param curveBlack The curves with Black volatility data.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final SwaptionCashFixedCompoundedONCompounded swaption, final YieldCurveWithBlackSwaptionBundle curveBlack) {
    ArgumentChecker.isTrue(false, "Method not implemented");
    //    ArgumentChecker.notNull(swaption, "Swaption");
    //    ArgumentChecker.notNull(curveBlack, "Curves with Black volatility");
    //    final Annuity<? extends Payment> annuityFixed = swaption.getUnderlyingSwap().getFirstLeg();
    //    final GeneratorInstrument<GeneratorAttributeIR> generatorSwap = curveBlack.getBlackParameters().getGeneratorSwap();
    //    final GeneratorSwapFixedCompoundedONCompounded fixedCompoundedON = (GeneratorSwapFixedCompoundedONCompounded) generatorSwap;
    //    final Calendar calendar = fixedCompoundedON.getOvernightCalendar();
    //    final double tenor = swaption.getMaturityTime();
    //    final double forward = swaption.getUnderlyingSwap().accept(PRC, curveBlack);
    //    //    final double forward = METHOD_SWAP.presentValueBasisPoint(swaption.getUnderlyingSwap(), fixedCompoundedON.getFixedLegDayCount(), calendar, curveBlack);
    //    final double pvbp = METHOD_SWAP.getAnnuityCash(swaption.getUnderlyingSwap(), forward);
    //    // Implementation comment: cash-settled swaptions make sense only for constant strike, the computation of coupon equivalent is not required.
    //    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    //    final double volatility = curveBlack.getBlackParameters().getVolatility(swaption.getTimeToExpiry(), tenor);
    //    final double discountFactorSettle = curveBlack.getCurve(annuityFixed.getNthPayment(0).getFundingCurveName()).getDiscountFactor(swaption.getSettlementTime());
    //    final BlackFunctionData dataBlack = new BlackFunctionData(forward, discountFactorSettle * pvbp, volatility);
    //    final Function1D<BlackFunctionData, Double> func = blackFunction.getPriceFunction(swaption);
    //    final double price = func.evaluate(dataBlack) * (swaption.isLong() ? 1.0 : -1.0);
    //    return CurrencyAmount.of(swaption.getCurrency(), price);
    return null;
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    ArgumentChecker.isTrue(instrument instanceof SwaptionCashFixedIbor, "Physical delivery swaption");
    ArgumentChecker.isTrue(curves instanceof YieldCurveWithBlackSwaptionBundle, "Bundle should contain Black Swaption data");
    return presentValue(instrument, curves);
  }

  /**
   * Computes the present value rate sensitivity to rates of a cash-settled European swaption in the Black model.
   * @param swaption The swaption.
   * @param curveBlack The curves with Black volatility data.
   * @return The present value curve sensitivity.
   */
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final SwaptionCashFixedCompoundedONCompounded swaption, final YieldCurveWithBlackSwaptionBundle curveBlack) {
    ArgumentChecker.isTrue(false, "Method not implemented");
    //    ArgumentChecker.notNull(swaption, "Swaption");
    //    ArgumentChecker.notNull(curveBlack, "Curves with Black volatility");
    //    final Annuity<? extends Payment> annuityFixed = swaption.getUnderlyingSwap().getFirstLeg();
    //    final double tenor = swaption.getMaturityTime();
    //    final GeneratorInstrument<GeneratorAttributeIR> generatorSwap = curveBlack.getBlackParameters().getGeneratorSwap();
    //    final GeneratorSwapFixedCompoundedONCompounded fixedCompoundedON = (GeneratorSwapFixedCompoundedONCompounded) generatorSwap;
    //    final Calendar calendar = fixedCompoundedON.getOvernightCalendar();
    //    final DayCount dayCount = fixedCompoundedON.getFixedLegDayCount();
    //    final double forward = METHOD_SWAP.presentValueBasisPoint(swaption.getUnderlyingSwap(), dayCount, calendar, curveBlack);
    //    final Swap<CouponFixedAccruedCompounding, CouponONCompounded> swap = swaption.getUnderlyingSwap();
    //    // Derivative of the forward with respect to the rates.
    //    final double pvSecond = swap.getSecondLeg().accept(PVC, curveBlack) * Math.signum(swap.getSecondLeg().getNthPayment(0).getNotional());
    //    final InterestRateCurveSensitivity pvbpDr = METHOD_SWAP.presentValueBasisPointCurveSensitivity(swap, dayCount, curveBlack);
    //    final InterestRateCurveSensitivity pvSecondDr = new InterestRateCurveSensitivity(swap.getSecondLeg().accept(PV_SENSITIVITY_CALCULATOR, curveBlack)).multipliedBy(Math
    //        .signum(swap.getSecondLeg().getNthPayment(0).getNotional()));
    //    final double pvbp = METHOD_SWAP.getAnnuityCash(swap, forward);
    //    final InterestRateCurveSensitivity forwardDr = pvSecondDr.multipliedBy(1.0 / pvbp).plus(pvbpDr.multipliedBy(-pvSecond / (pvbp * pvbp)));
    //    // Derivative of the cash annuity with respect to the forward.
    //    final double pvbpDf = METHOD_SWAP.getAnnuityCashDerivative(swaption.getUnderlyingSwap(), forward);
    //    // Implementation note: strictly speaking, the strike equivalent is curve dependent; that dependency is ignored.
    //    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    //    final double volatility = curveBlack.getBlackParameters().getVolatility(swaption.getTimeToExpiry(), tenor);
    //    final double discountFactorSettle = curveBlack.getCurve(annuityFixed.getNthPayment(0).getFundingCurveName()).getDiscountFactor(swaption.getSettlementTime());
    //    final BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatility);
    //    final double[] bsAdjoint = blackFunction.getPriceAdjoint(swaption, dataBlack);
    //    final double sensiDF = -swaption.getSettlementTime() * discountFactorSettle * pvbp * bsAdjoint[0];
    //    final List<DoublesPair> list = new ArrayList<>();
    //    list.add(DoublesPair.of(swaption.getSettlementTime(), sensiDF));
    //    final Map<String, List<DoublesPair>> resultMap = new HashMap<>();
    //    resultMap.put(annuityFixed.getNthPayment(0).getFundingCurveName(), list);
    //    InterestRateCurveSensitivity result = new InterestRateCurveSensitivity(resultMap);
    //    result = result.plus(forwardDr.multipliedBy(discountFactorSettle * (pvbpDf * bsAdjoint[0] + pvbp * bsAdjoint[1])));
    //    if (!swaption.isLong()) {
    //      result = result.multipliedBy(-1);
    //    }
    //    return result;
    return null;
  }

  /**
   * Computes the present value sensitivity to the Black volatility (also called vega) of a cash-settled European swaption in the Black swaption model.
   * @param swaption The swaption.
   * @param curveBlack The curves with Black volatility data.
   * @return The present value Black sensitivity.
   */
  public PresentValueSwaptionSurfaceSensitivity presentValueBlackSensitivity(final SwaptionCashFixedCompoundedONCompounded swaption, final YieldCurveWithBlackSwaptionBundle curveBlack) {
    ArgumentChecker.isTrue(false, "Method not implemented");
    //    ArgumentChecker.notNull(swaption, "Swaption");
    //    ArgumentChecker.notNull(curveBlack, "Curves with Black volatility");
    //    final Annuity<? extends Payment> annuityFixed = swaption.getUnderlyingSwap().getFirstLeg();
    //    final GeneratorInstrument<GeneratorAttributeIR> generatorSwap = curveBlack.getBlackParameters().getGeneratorSwap();
    //    final GeneratorSwapFixedCompoundedONCompounded fixedCompoundedON = (GeneratorSwapFixedCompoundedONCompounded) generatorSwap;
    //    final Calendar calendar = fixedCompoundedON.getOvernightCalendar();
    //    final double forward = METHOD_SWAP.presentValueBasisPoint(swaption.getUnderlyingSwap(), fixedCompoundedON.getFixedLegDayCount(), calendar, curveBlack);
    //    final double pvbp = METHOD_SWAP.getAnnuityCash(swaption.getUnderlyingSwap(), forward);
    //    final double discountFactorSettle = curveBlack.getCurve(annuityFixed.getNthPayment(0).getFundingCurveName()).getDiscountFactor(swaption.getSettlementTime());
    //    final DoublesPair point = DoublesPair.of(swaption.getTimeToExpiry(), swaption.getMaturityTime());
    //    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    //    final double volatility = curveBlack.getBlackParameters().getVolatility(point);
    //    final BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatility);
    //    final double[] bsAdjoint = blackFunction.getPriceAdjoint(swaption, dataBlack);
    //    final Map<DoublesPair, Double> sensitivity = new HashMap<>();
    //    sensitivity.put(point, bsAdjoint[2] * pvbp * discountFactorSettle * (swaption.isLong() ? 1.0 : -1.0));
    //    return new PresentValueBlackSwaptionSensitivity(sensitivity, curveBlack.getBlackParameters().getGeneratorSwap());
    return null;
  }

  /**
   * Computes the implied Black volatility of the vanilla swaption.
   * @param swaption The swaption.
   * @param curves The yield curve bundle.
   * @return The implied volatility.
   */
  public double impliedVolatility(final SwaptionCashFixedCompoundedONCompounded swaption, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "Curves");
    ArgumentChecker.isTrue(curves instanceof YieldCurveWithBlackSwaptionBundle, "Yield curve bundle should contain Black swaption data");
    final YieldCurveWithBlackSwaptionBundle curvesBlack = (YieldCurveWithBlackSwaptionBundle) curves;
    ArgumentChecker.notNull(swaption, "Forex option");
    final double tenor = swaption.getMaturityTime();
    final double volatility = curvesBlack.getBlackParameters().getVolatility(swaption.getTimeToExpiry(), tenor);
    return volatility;
  }
}
