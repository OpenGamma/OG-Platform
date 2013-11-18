/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.ParRateCalculator;
import com.opengamma.analytics.financial.interestrate.ParRateCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMSSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponCMS;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.NormalFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.NormalPriceFunction;
import com.opengamma.analytics.financial.model.volatility.NormalImpliedVolatilityFormula;
import com.opengamma.analytics.math.function.DoubleFunction1D;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.function.RealPolynomialFunction1D;
import com.opengamma.analytics.math.rootfinding.BrentSingleRootFinder;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 *  Class used to compute the price of a CMS spread cap/floor with the bi-normal approach with correlation by strike.
 *  OpenGamma implementation note: Bi-normal with correlation by strike approach to CMS spread pricing, version 1.1, June 2011.
 *  @deprecated {@link SABRInterestRateDataBundle} is deprecated
 */
@Deprecated
public class CapFloorCMSSpreadSABRBinormalMethod implements PricingMethod {

  /**
   * The par rate calculator.
   */
  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();
  private static final ParRateCurveSensitivityCalculator PRCSC = ParRateCurveSensitivityCalculator.getInstance();
  /**
   * The formula used to compute the implied volatility.
   */
  private static final NormalImpliedVolatilityFormula NORMAL_IMPLIED_VOLATILITY = new NormalImpliedVolatilityFormula();
  /**
   * The formula used to compute the implied volatility.
   */
  private static final NormalPriceFunction NORMAL_PRICE = new NormalPriceFunction();

  /**
   * The method to compute the price of CMS cap/floors.
   */
  private final CapFloorCMSSABRReplicationAbstractMethod _methodCmsCap;
  /**
   * The method to compute the price o CMS coupons.
   */
  private final CouponCMSSABRReplicationGenericMethod _methodCmsCoupon;
  /**
   * The correlation as function of the strike.
   */
  private final DoubleFunction1D _correlation;

  /**
   * Constructor of the CMS spread cap/floor method with the CMS cap and coupon methods.
   * @param correlation The rates correlation.
   * @param methodCmsCap The pricing method for the CMS cap/floor.
   * @param methodCmsCoupon The pricing method for the CMS coupons.
   */
  public CapFloorCMSSpreadSABRBinormalMethod(final DoubleFunction1D correlation, final CapFloorCMSSABRReplicationAbstractMethod methodCmsCap,
      final CouponCMSSABRReplicationGenericMethod methodCmsCoupon) {
    Validate.notNull(correlation, "Correlation");
    _correlation = correlation;
    _methodCmsCap = methodCmsCap;
    _methodCmsCoupon = methodCmsCoupon;
  }

  /**
   * Gets the correlation (rho) as function of the strike.
   * @return The correlation
   */
  public DoubleFunction1D getCorrelation() {
    return _correlation;
  }

  /**
   * Compute the present value of a CMS spread cap/floor in the binormal approach.
   * @param cmsSpread The CMS spread cap/floor.
   * @param sabrData The SABR data bundle.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final CapFloorCMSSpread cmsSpread, final SABRInterestRateDataBundle sabrData) {
    final double forward1 = cmsSpread.getUnderlyingSwap1().accept(PRC, sabrData);
    final double forward2 = cmsSpread.getUnderlyingSwap2().accept(PRC, sabrData);
    CouponCMS cmsCoupon1 = CouponCMS.from(cmsSpread, cmsSpread.getUnderlyingSwap1(), cmsSpread.getSettlementTime());
    cmsCoupon1 = cmsCoupon1.withNotional(Math.abs(cmsCoupon1.getNotional()));
    CouponCMS cmsCoupon2 = CouponCMS.from(cmsSpread, cmsSpread.getUnderlyingSwap2(), cmsSpread.getSettlementTime());
    cmsCoupon2 = cmsCoupon2.withNotional(Math.abs(cmsCoupon2.getNotional()));
    final CapFloorCMS cmsCap1 = CapFloorCMS.from(cmsCoupon1, forward1, true);
    final CapFloorCMS cmsCap2 = CapFloorCMS.from(cmsCoupon2, forward2, true);
    final double cmsCoupon1Price = _methodCmsCoupon.presentValue(cmsCoupon1, sabrData).getAmount();
    final double cmsCoupon2Price = _methodCmsCoupon.presentValue(cmsCoupon2, sabrData).getAmount();
    final double cmsCap1Price = _methodCmsCap.presentValue(cmsCap1, sabrData).getAmount();
    final double cmsCap2Price = _methodCmsCap.presentValue(cmsCap2, sabrData).getAmount();
    final double discountFactorPayment = sabrData.getCurve(cmsSpread.getFundingCurveName()).getDiscountFactor(cmsSpread.getPaymentTime());
    final NormalFunctionData dataCap1 = new NormalFunctionData(cmsCoupon1Price / (discountFactorPayment * cmsCap1.getNotional() * cmsCap1.getPaymentYearFraction()), discountFactorPayment
        * cmsCap1.getNotional() * cmsCap1.getPaymentYearFraction(), 0.0);
    final EuropeanVanillaOption optionCap1 = new EuropeanVanillaOption(forward1, cmsSpread.getFixingTime(), true);
    double cmsCap1ImpliedVolatility = 0;
    try {
      cmsCap1ImpliedVolatility = NORMAL_IMPLIED_VOLATILITY.getImpliedVolatility(dataCap1, optionCap1, cmsCap1Price);
    } catch (final Exception e) {
      //TODO
    }
    final NormalFunctionData dataCap2 = new NormalFunctionData(cmsCoupon2Price / (discountFactorPayment * cmsCap2.getNotional() * cmsCap2.getPaymentYearFraction()), discountFactorPayment
        * cmsCap2.getNotional() * cmsCap2.getPaymentYearFraction(), cmsCap1ImpliedVolatility);
    final EuropeanVanillaOption optionCap2 = new EuropeanVanillaOption(forward2, cmsSpread.getFixingTime(), true);
    double cmsCap2ImpliedVolatility = 0;
    try {
      cmsCap2ImpliedVolatility = NORMAL_IMPLIED_VOLATILITY.getImpliedVolatility(dataCap2, optionCap2, cmsCap2Price);
    } catch (final Exception e) {
      //TODO
    }
    final double cmsSpreadImpliedVolatility = Math.sqrt(cmsCap1ImpliedVolatility * cmsCap1ImpliedVolatility - 2 * _correlation.evaluate(cmsSpread.getStrike()) * cmsCap1ImpliedVolatility
        * cmsCap2ImpliedVolatility + cmsCap2ImpliedVolatility * cmsCap2ImpliedVolatility);
    final NormalFunctionData dataSpread = new NormalFunctionData(
        (cmsCoupon1Price - cmsCoupon2Price) / (discountFactorPayment * Math.abs(cmsSpread.getNotional()) * cmsSpread.getPaymentYearFraction()), discountFactorPayment * cmsSpread.getNotional()
            * cmsSpread.getPaymentYearFraction(), cmsSpreadImpliedVolatility);
    final EuropeanVanillaOption optionSpread = new EuropeanVanillaOption(cmsSpread.getStrike(), cmsSpread.getFixingTime(), cmsSpread.isCap());
    final Function1D<NormalFunctionData, Double> normalFunction = NORMAL_PRICE.getPriceFunction(optionSpread);
    final double cmsSpreadPrice = normalFunction.evaluate(dataSpread);
    return CurrencyAmount.of(cmsSpread.getCurrency(), cmsSpreadPrice);
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof CapFloorCMSSpread, "CMS spread cap/floor");
    Validate.isTrue(curves instanceof SABRInterestRateDataBundle, "Bundle should contain SABR data");
    return presentValue((CapFloorCMSSpread) instrument, (SABRInterestRateDataBundle) curves);
  }

  /**
   * Compute the implied correlation for a specific CMS spread cap/floor from the given price. The model correlation structure is not used.
   * @param cmsSpread The CMS spread cap/floor.
   * @param sabrData The SABR data bundle.
   * @param price The CMS spread price.
   * @return The implied correlation.
   */
  public double impliedCorrelation(final CapFloorCMSSpread cmsSpread, final SABRInterestRateDataBundle sabrData, final double price) {
    final SolveCorrelation function = new SolveCorrelation(cmsSpread, sabrData, price);
    final BrentSingleRootFinder finder = new BrentSingleRootFinder();
    final double correlation = finder.getRoot(function, -0.999, 0.999);
    return correlation;
  }

  /**
   * Computes the present value curves sensitivity of a CMS spread cap/floor in the bi-normal approach.
   * For the CMS cap/floor volatility calibration, ATM forward strikes are used.
   * @param cmsSpread The CMS spread cap/floor.
   * @param sabrData The SABR data bundle.
   * @return The present value curve sensitivity.
   */
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final CapFloorCMSSpread cmsSpread, final SABRInterestRateDataBundle sabrData) {
    // Forward sweep
    final double strike1 = cmsSpread.getUnderlyingSwap1().accept(PRC, sabrData);
    final double strike2 = cmsSpread.getUnderlyingSwap2().accept(PRC, sabrData);
    CouponCMS cmsCoupon1 = CouponCMS.from(cmsSpread, cmsSpread.getUnderlyingSwap1(), cmsSpread.getSettlementTime());
    cmsCoupon1 = cmsCoupon1.withNotional(Math.abs(cmsCoupon1.getNotional()));
    CouponCMS cmsCoupon2 = CouponCMS.from(cmsSpread, cmsSpread.getUnderlyingSwap2(), cmsSpread.getSettlementTime());
    cmsCoupon2 = cmsCoupon2.withNotional(Math.abs(cmsCoupon2.getNotional()));
    final CapFloorCMS cmsCap1 = CapFloorCMS.from(cmsCoupon1, strike1, true); // ATM forward cap CMS
    final CapFloorCMS cmsCap2 = CapFloorCMS.from(cmsCoupon2, strike2, true); // ATM forward cap CMS
    final double cmsCoupon1Pv = _methodCmsCoupon.presentValue(cmsCoupon1, sabrData).getAmount();
    final double cmsCoupon2Pv = _methodCmsCoupon.presentValue(cmsCoupon2, sabrData).getAmount();
    final double cmsCap1Pv = _methodCmsCap.presentValue(cmsCap1, sabrData).getAmount();
    final double cmsCap2Pv = _methodCmsCap.presentValue(cmsCap2, sabrData).getAmount();
    final double discountFactorPayment = sabrData.getCurve(cmsSpread.getFundingCurveName()).getDiscountFactor(cmsSpread.getPaymentTime());
    final double factor = discountFactorPayment * cmsCap1.getNotional() * cmsCap1.getPaymentYearFraction();
    final double expectation1 = cmsCoupon1Pv / factor;
    final double expectation2 = cmsCoupon2Pv / factor;
    NormalFunctionData dataCap1 = new NormalFunctionData(expectation1, factor, 0.0);
    final EuropeanVanillaOption optionCap1 = new EuropeanVanillaOption(strike1, cmsSpread.getFixingTime(), true);
    double cmsCap1ImpliedVolatility = 0;
    try {
      cmsCap1ImpliedVolatility = NORMAL_IMPLIED_VOLATILITY.getImpliedVolatility(dataCap1, optionCap1, cmsCap1Pv);
    } catch (final Exception e) {
      //TODO
    }
    NormalFunctionData dataCap2 = new NormalFunctionData(expectation2, factor, cmsCap1ImpliedVolatility);
    final EuropeanVanillaOption optionCap2 = new EuropeanVanillaOption(strike2, cmsSpread.getFixingTime(), true);
    double cmsCap2ImpliedVolatility = 0;
    try {
      cmsCap2ImpliedVolatility = NORMAL_IMPLIED_VOLATILITY.getImpliedVolatility(dataCap2, optionCap2, cmsCap2Pv);
    } catch (final Exception e) {
      //TODO
    }
    final double rho = _correlation.evaluate(cmsSpread.getStrike());
    final double cmsSpreadImpliedVolatility = Math.sqrt(cmsCap1ImpliedVolatility * cmsCap1ImpliedVolatility - 2 * rho * cmsCap1ImpliedVolatility * cmsCap2ImpliedVolatility + cmsCap2ImpliedVolatility
        * cmsCap2ImpliedVolatility);
    final NormalFunctionData dataSpread = new NormalFunctionData(expectation1 - expectation2, discountFactorPayment * cmsSpread.getNotional() * cmsSpread.getPaymentYearFraction(),
        cmsSpreadImpliedVolatility);
    final EuropeanVanillaOption optionSpread = new EuropeanVanillaOption(cmsSpread.getStrike(), cmsSpread.getFixingTime(), cmsSpread.isCap());
    final double[] cmsSpreadPvDerivative = new double[3];
    final double cmsSpreadPv = NORMAL_PRICE.getPriceAdjoint(optionSpread, dataSpread, cmsSpreadPvDerivative);
    // Backward sweep
    final double cmsSpreadPvBar = 1.0;
    final double cmsSpreadImpliedVolatilityBar = cmsSpreadPvDerivative[1] * cmsSpreadPvBar;
    final double cmsCap2ImpliedVolatilityBar = (cmsCap2ImpliedVolatility - rho * cmsCap1ImpliedVolatility) / cmsSpreadImpliedVolatility * cmsSpreadImpliedVolatilityBar; // OK
    final double cmsCap1ImpliedVolatilityBar = (cmsCap1ImpliedVolatility - rho * cmsCap2ImpliedVolatility) / cmsSpreadImpliedVolatility * cmsSpreadImpliedVolatilityBar; // OK
    dataCap2 = new NormalFunctionData(expectation2, factor, cmsCap2ImpliedVolatility);
    final double[] cmsCap2PriceNormalDerivative = new double[3];
    NORMAL_PRICE.getPriceAdjoint(optionCap2, dataCap2, cmsCap2PriceNormalDerivative);
    final double expectation2Bar = -cmsSpreadPvDerivative[0] * cmsSpreadPvBar + -cmsCap2PriceNormalDerivative[0] / cmsCap2PriceNormalDerivative[1] * cmsCap2ImpliedVolatilityBar; // OK
    dataCap1 = new NormalFunctionData(expectation1, factor, cmsCap1ImpliedVolatility);
    final double[] cmsCap1PriceNormalDerivative = new double[3];
    NORMAL_PRICE.getPriceAdjoint(optionCap1, dataCap1, cmsCap1PriceNormalDerivative);
    final double expectation1Bar = cmsSpreadPvDerivative[0] * cmsSpreadPvBar + -cmsCap1PriceNormalDerivative[0] / cmsCap1PriceNormalDerivative[1] * cmsCap1ImpliedVolatilityBar; // OK
    final double factorBar = -cmsCoupon1Pv / (factor * factor) * expectation1Bar + -cmsCoupon2Pv / (factor * factor) * expectation2Bar - cmsCap2Pv / factor / cmsCap2PriceNormalDerivative[1]
        * cmsCap2ImpliedVolatilityBar - cmsCap1Pv / factor / cmsCap1PriceNormalDerivative[1] * cmsCap1ImpliedVolatilityBar; // OK
    final double discountFactorPaymentBar = cmsCap1.getNotional() * cmsCap1.getPaymentYearFraction() * factorBar + cmsSpreadPv / discountFactorPayment * cmsSpreadPvBar;
    final double cmsCap2PvBar = 1.0 / cmsCap2PriceNormalDerivative[1] * cmsCap2ImpliedVolatilityBar; // OK
    final double cmsCap1PvBar = 1.0 / cmsCap1PriceNormalDerivative[1] * cmsCap1ImpliedVolatilityBar; // OK
    final double cmsCoupon2PvBar = expectation2Bar / factor; // OK
    final double cmsCoupon1PvBar = expectation1Bar / factor; // OK
    //Calibration strike dependency -- START
    double strike1Bar = -cmsCap1PriceNormalDerivative[2] / cmsCap1PriceNormalDerivative[1] * cmsCap1ImpliedVolatilityBar;
    double strike2Bar = -cmsCap2PriceNormalDerivative[2] / cmsCap2PriceNormalDerivative[1] * cmsCap2ImpliedVolatilityBar;
    strike1Bar += _methodCmsCap.presentValueStrikeSensitivity(cmsCap1, sabrData) * cmsCap1PvBar;
    strike2Bar += _methodCmsCap.presentValueStrikeSensitivity(cmsCap2, sabrData) * cmsCap2PvBar;
    final InterestRateCurveSensitivity forward1CurveSensitivity = new InterestRateCurveSensitivity(cmsSpread.getUnderlyingSwap1().accept(PRCSC, sabrData));
    final InterestRateCurveSensitivity forward2CurveSensitivity = new InterestRateCurveSensitivity(cmsSpread.getUnderlyingSwap2().accept(PRCSC, sabrData));
    //Calibration strike dependency -- END
    final InterestRateCurveSensitivity cmsCoupon1CurveSensitivity = _methodCmsCoupon.presentValueCurveSensitivity(cmsCoupon1, sabrData);
    final InterestRateCurveSensitivity cmsCoupon2CurveSensitivity = _methodCmsCoupon.presentValueCurveSensitivity(cmsCoupon2, sabrData);
    final InterestRateCurveSensitivity cmsCap1CurveSensitivity = _methodCmsCap.presentValueCurveSensitivity(cmsCap1, sabrData);
    final InterestRateCurveSensitivity cmsCap2CurveSensitivity = _methodCmsCap.presentValueCurveSensitivity(cmsCap2, sabrData);
    final List<DoublesPair> list = new ArrayList<>();
    list.add(DoublesPair.of(cmsSpread.getPaymentTime(), -cmsSpread.getPaymentTime() * discountFactorPayment));
    final Map<String, List<DoublesPair>> resultMap = new HashMap<>();
    resultMap.put(cmsSpread.getFundingCurveName(), list);
    final InterestRateCurveSensitivity dfCurveSensitivity = new InterestRateCurveSensitivity(resultMap);
    InterestRateCurveSensitivity result;
    result = dfCurveSensitivity.multipliedBy(discountFactorPaymentBar);
    result = result.plus(cmsCoupon1CurveSensitivity.multipliedBy(cmsCoupon1PvBar));
    result = result.plus(cmsCoupon2CurveSensitivity.multipliedBy(cmsCoupon2PvBar));
    result = result.plus(cmsCap1CurveSensitivity.multipliedBy(cmsCap1PvBar));
    result = result.plus(cmsCap2CurveSensitivity.multipliedBy(cmsCap2PvBar));
    //Calibration strike dependency -- START
    result = result.plus(forward1CurveSensitivity.multipliedBy(strike1Bar));
    result = result.plus(forward2CurveSensitivity.multipliedBy(strike2Bar));
    //Calibration strike dependency -- END
    return result;
  }

  /**
   * Computes the present value sensitivity to the SABR parameters of a CMS spread cap/floor in the SABR framework.
   * @param cmsSpread The CMS spread cap/floor.
   * @param sabrData The SABR data bundle.
   * @return The present value SABR sensitivity.
   */
  public PresentValueSABRSensitivityDataBundle presentValueSABRSensitivity(final CapFloorCMSSpread cmsSpread, final SABRInterestRateDataBundle sabrData) {
    // Forward sweep
    final double forward1 = cmsSpread.getUnderlyingSwap1().accept(PRC, sabrData);
    final double forward2 = cmsSpread.getUnderlyingSwap2().accept(PRC, sabrData);
    CouponCMS cmsCoupon1 = CouponCMS.from(cmsSpread, cmsSpread.getUnderlyingSwap1(), cmsSpread.getSettlementTime());
    cmsCoupon1 = cmsCoupon1.withNotional(Math.abs(cmsCoupon1.getNotional()));
    CouponCMS cmsCoupon2 = CouponCMS.from(cmsSpread, cmsSpread.getUnderlyingSwap2(), cmsSpread.getSettlementTime());
    cmsCoupon2 = cmsCoupon2.withNotional(Math.abs(cmsCoupon2.getNotional()));
    final CapFloorCMS cmsCap1 = CapFloorCMS.from(cmsCoupon1, forward1, true);
    final CapFloorCMS cmsCap2 = CapFloorCMS.from(cmsCoupon2, forward2, true);
    final double cmsCoupon1Price = _methodCmsCoupon.presentValue(cmsCoupon1, sabrData).getAmount();
    final double cmsCoupon2Price = _methodCmsCoupon.presentValue(cmsCoupon2, sabrData).getAmount();
    final double cmsCap1Price = _methodCmsCap.presentValue(cmsCap1, sabrData).getAmount();
    final double cmsCap2Price = _methodCmsCap.presentValue(cmsCap2, sabrData).getAmount();
    final double discountFactorPayment = sabrData.getCurve(cmsSpread.getFundingCurveName()).getDiscountFactor(cmsSpread.getPaymentTime());
    final double factor = discountFactorPayment * cmsCap1.getNotional() * cmsCap1.getPaymentYearFraction();
    final double expectation1 = cmsCoupon1Price / factor;
    final double expectation2 = cmsCoupon2Price / factor;
    NormalFunctionData dataCap1 = new NormalFunctionData(expectation1, factor, 0.0);
    final EuropeanVanillaOption optionCap1 = new EuropeanVanillaOption(forward1, cmsSpread.getFixingTime(), true);
    double cmsCap1ImpliedVolatility = 0;
    try {
      cmsCap1ImpliedVolatility = NORMAL_IMPLIED_VOLATILITY.getImpliedVolatility(dataCap1, optionCap1, cmsCap1Price);
    } catch (final Exception e) {
      //TODO
    }
    NormalFunctionData dataCap2 = new NormalFunctionData(expectation2, factor, cmsCap1ImpliedVolatility);
    final EuropeanVanillaOption optionCap2 = new EuropeanVanillaOption(forward2, cmsSpread.getFixingTime(), true);
    double cmsCap2ImpliedVolatility = 0;
    try {
      cmsCap2ImpliedVolatility = NORMAL_IMPLIED_VOLATILITY.getImpliedVolatility(dataCap2, optionCap2, cmsCap2Price);
    } catch (final Exception e) {
      //TODO
    }
    final double rho = _correlation.evaluate(cmsSpread.getStrike());
    final double cmsSpreadImpliedVolatility = Math.sqrt(cmsCap1ImpliedVolatility * cmsCap1ImpliedVolatility - 2 * rho * cmsCap1ImpliedVolatility * cmsCap2ImpliedVolatility + cmsCap2ImpliedVolatility
        * cmsCap2ImpliedVolatility);
    final NormalFunctionData dataSpread = new NormalFunctionData(expectation1 - expectation2, discountFactorPayment * cmsSpread.getNotional() * cmsSpread.getPaymentYearFraction(),
        cmsSpreadImpliedVolatility);
    final EuropeanVanillaOption optionSpread = new EuropeanVanillaOption(cmsSpread.getStrike(), cmsSpread.getFixingTime(), cmsSpread.isCap());
    final double[] cmsSpreadPriceDerivative = new double[3];
    NORMAL_PRICE.getPriceAdjoint(optionSpread, dataSpread, cmsSpreadPriceDerivative);
    // Backward sweep
    final double cmsSpreadPriceBar = 1.0;
    final double cmsSpreadImpliedVolatilityBar = cmsSpreadPriceDerivative[1] * cmsSpreadPriceBar;
    final double cmsCap2ImpliedVolatilityBar = (cmsCap2ImpliedVolatility - rho * cmsCap1ImpliedVolatility) / cmsSpreadImpliedVolatility * cmsSpreadImpliedVolatilityBar;
    final double cmsCap1ImpliedVolatilityBar = (cmsCap1ImpliedVolatility - rho * cmsCap2ImpliedVolatility) / cmsSpreadImpliedVolatility * cmsSpreadImpliedVolatilityBar;
    dataCap2 = new NormalFunctionData(expectation2, factor, cmsCap2ImpliedVolatility);
    final double[] cmsCap2PriceNormalDerivative = new double[3];
    NORMAL_PRICE.getPriceAdjoint(optionCap2, dataCap2, cmsCap2PriceNormalDerivative);
    final double expectation2Bar = -cmsSpreadPriceDerivative[0] * cmsSpreadPriceBar + -cmsCap2PriceNormalDerivative[0] / cmsCap2PriceNormalDerivative[1] * cmsCap2ImpliedVolatilityBar;
    dataCap1 = new NormalFunctionData(expectation1, factor, cmsCap1ImpliedVolatility);
    final double[] cmsCap1PriceNormalDerivative = new double[3];
    NORMAL_PRICE.getPriceAdjoint(optionCap1, dataCap1, cmsCap1PriceNormalDerivative);
    final double expectation1Bar = cmsSpreadPriceDerivative[0] * cmsSpreadPriceBar + -cmsCap1PriceNormalDerivative[0] / cmsCap1PriceNormalDerivative[1] * cmsCap1ImpliedVolatilityBar;
    final double cmsCap2PriceBar = 1.0 / cmsCap2PriceNormalDerivative[1] * cmsCap2ImpliedVolatilityBar;
    final double cmsCap1PriceBar = 1.0 / cmsCap1PriceNormalDerivative[1] * cmsCap1ImpliedVolatilityBar;
    final double cmsCoupon2PriceBar = expectation2Bar / factor;
    final double cmsCoupon1PriceBar = expectation1Bar / factor;
    PresentValueSABRSensitivityDataBundle cmsCoupon1SABRSensitivity = _methodCmsCoupon.presentValueSABRSensitivity(cmsCoupon1, sabrData);
    PresentValueSABRSensitivityDataBundle cmsCoupon2SABRSensitivity = _methodCmsCoupon.presentValueSABRSensitivity(cmsCoupon2, sabrData);
    PresentValueSABRSensitivityDataBundle cmsCap1SABRSensitivity = _methodCmsCap.presentValueSABRSensitivity(cmsCap1, sabrData);
    PresentValueSABRSensitivityDataBundle cmsCap2SABRSensitivity = _methodCmsCap.presentValueSABRSensitivity(cmsCap2, sabrData);
    cmsCoupon1SABRSensitivity = cmsCoupon1SABRSensitivity.multiplyBy(cmsCoupon1PriceBar);
    cmsCoupon2SABRSensitivity = cmsCoupon2SABRSensitivity.multiplyBy(cmsCoupon2PriceBar);
    cmsCap1SABRSensitivity = cmsCap1SABRSensitivity.multiplyBy(cmsCap1PriceBar);
    cmsCap2SABRSensitivity = cmsCap2SABRSensitivity.multiplyBy(cmsCap2PriceBar);
    PresentValueSABRSensitivityDataBundle result = cmsCoupon1SABRSensitivity;
    result = result.plus(cmsCoupon2SABRSensitivity);
    result = result.plus(cmsCap1SABRSensitivity);
    result = result.plus(cmsCap2SABRSensitivity);
    return result;
  }

  /**
   * Inner class to solve for the implied correlation.
   */
  class SolveCorrelation extends DoubleFunction1D {
    /**
     * The CMS spread cap/floor.
     */
    private final CapFloorCMSSpread _cmsSpread;
    /**
     * The SABR data bundle.
     */
    private final SABRInterestRateDataBundle _sabrData;
    /**
     * The CMS spread price.
     */
    private final double _price;

    /**
     * Constructor of the difference function.
     * @param cmsSpread The CMS spread cap/floor.
     * @param sabrData The SABR data bundle.
     * @param price The CMS spread price.
     */
    public SolveCorrelation(final CapFloorCMSSpread cmsSpread, final SABRInterestRateDataBundle sabrData, final double price) {
      this._cmsSpread = cmsSpread;
      this._sabrData = sabrData;
      this._price = price;
    }

    @Override
    public Double evaluate(final Double x) {
      @SuppressWarnings("synthetic-access")
      final CapFloorCMSSpreadSABRBinormalMethod method = new CapFloorCMSSpreadSABRBinormalMethod(new RealPolynomialFunction1D(new double[] {x }), _methodCmsCap, _methodCmsCoupon);
      return method.presentValue(_cmsSpread, _sabrData).getAmount() - _price;
    }
  }

}
