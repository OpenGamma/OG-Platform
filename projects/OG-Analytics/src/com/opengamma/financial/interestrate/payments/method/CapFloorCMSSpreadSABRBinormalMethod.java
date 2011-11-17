/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.ParRateCalculator;
import com.opengamma.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.method.PricingMethod;
import com.opengamma.financial.interestrate.payments.CapFloorCMS;
import com.opengamma.financial.interestrate.payments.CapFloorCMSSpread;
import com.opengamma.financial.interestrate.payments.CouponCMS;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.option.pricing.analytic.formula.NormalPriceFunction;
import com.opengamma.financial.model.volatility.NormalImpliedVolatilityFormula;
import com.opengamma.math.function.DoubleFunction1D;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.function.RealPolynomialFunction1D;
import com.opengamma.math.rootfinding.BrentSingleRootFinder;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 *  Class used to compute the price of a CMS spread cap/floor with the binormal approach with correlation by strike.
 *  OpenGamma implementation note: Binormal with correlation by strike approach to CMS spread pricing, version 1.1, June 2011.
 */
public class CapFloorCMSSpreadSABRBinormalMethod implements PricingMethod {

  /**
   * The method to compute the price of CMS cap/floors.
   */
  private static final CapFloorCMSSABRReplicationMethod METHOD_CMS_CAP = CapFloorCMSSABRReplicationMethod.getDefaultInstance();
  /**
   * The method to compute the price o CMS coupons.
   */
  private static final CouponCMSSABRReplicationMethod METHOD_CMS_COUPON = CouponCMSSABRReplicationMethod.getDefaultInstance();
  /**
   * The par rate calculator.
   */
  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();
  /**
   * The formula used to compute the implied volatility.
   */
  private static final NormalImpliedVolatilityFormula NORMAL_IMPLIED_VOLATILITY = new NormalImpliedVolatilityFormula();
  /**
   * The formula used to compute the implied volatility.
   */
  private static final NormalPriceFunction NORMAL_PRICE = new NormalPriceFunction();
  /**
   * The correlation as function of the strike.
   */
  private final DoubleFunction1D _correlation;

  /** 
   * Default constructor of the CMS spread cap/floor method.
   * @param correlation The rates correlation.
   */
  public CapFloorCMSSpreadSABRBinormalMethod(final DoubleFunction1D correlation) {
    Validate.notNull(correlation, "Correlation");
    _correlation = correlation;
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
    double forward1 = PRC.visit(cmsSpread.getUnderlyingSwap1(), sabrData);
    double forward2 = PRC.visit(cmsSpread.getUnderlyingSwap2(), sabrData);
    CouponCMS cmsCoupon1 = CouponCMS.from(cmsSpread, cmsSpread.getUnderlyingSwap1(), cmsSpread.getSettlementTime());
    CouponCMS cmsCoupon2 = CouponCMS.from(cmsSpread, cmsSpread.getUnderlyingSwap2(), cmsSpread.getSettlementTime());
    CapFloorCMS cmsCap1 = CapFloorCMS.from(cmsCoupon1, forward1, true);
    CapFloorCMS cmsCap2 = CapFloorCMS.from(cmsCoupon2, forward2, true);
    double cmsCoupon1Price = METHOD_CMS_COUPON.presentValue(cmsCoupon1, sabrData);
    double cmsCoupon2Price = METHOD_CMS_COUPON.presentValue(cmsCoupon2, sabrData);
    double cmsCap1Price = METHOD_CMS_CAP.presentValue(cmsCap1, sabrData).getAmount();
    double cmsCap2Price = METHOD_CMS_CAP.presentValue(cmsCap2, sabrData).getAmount();
    double discountFactorPayment = sabrData.getCurve(cmsSpread.getFundingCurveName()).getDiscountFactor(cmsSpread.getPaymentTime());
    BlackFunctionData dataCap1 = new BlackFunctionData(cmsCoupon1Price / (discountFactorPayment * cmsCap1.getNotional() * cmsCap1.getPaymentYearFraction()), discountFactorPayment
        * cmsCap1.getNotional() * cmsCap1.getPaymentYearFraction(), 0.0);
    final EuropeanVanillaOption optionCap1 = new EuropeanVanillaOption(forward1, cmsSpread.getFixingTime(), true);
    final double cmsCap1ImpliedVolatility = NORMAL_IMPLIED_VOLATILITY.getImpliedVolatility(dataCap1, optionCap1, cmsCap1Price);
    final BlackFunctionData dataCap2 = new BlackFunctionData(cmsCoupon2Price / (discountFactorPayment * cmsCap2.getNotional() * cmsCap2.getPaymentYearFraction()), discountFactorPayment
        * cmsCap2.getNotional() * cmsCap2.getPaymentYearFraction(), cmsCap1ImpliedVolatility);
    final EuropeanVanillaOption optionCap2 = new EuropeanVanillaOption(forward2, cmsSpread.getFixingTime(), true);
    final double cmsCap2ImpliedVolatility = NORMAL_IMPLIED_VOLATILITY.getImpliedVolatility(dataCap2, optionCap2, cmsCap2Price);
    final double cmsSpreadImpliedVolatility = Math.sqrt(cmsCap1ImpliedVolatility * cmsCap1ImpliedVolatility - 2 * _correlation.evaluate(cmsSpread.getStrike()) * cmsCap1ImpliedVolatility
        * cmsCap2ImpliedVolatility + cmsCap2ImpliedVolatility * cmsCap2ImpliedVolatility);
    final BlackFunctionData dataSpread = new BlackFunctionData((cmsCoupon1Price - cmsCoupon2Price) / (discountFactorPayment * cmsSpread.getNotional() * cmsSpread.getPaymentYearFraction()),
        discountFactorPayment * cmsSpread.getNotional() * cmsSpread.getPaymentYearFraction(), cmsSpreadImpliedVolatility);
    EuropeanVanillaOption optionSpread = new EuropeanVanillaOption(cmsSpread.getStrike(), cmsSpread.getFixingTime(), cmsSpread.isCap());
    Function1D<BlackFunctionData, Double> normalFunction = NORMAL_PRICE.getPriceFunction(optionSpread);
    double cmsSpreadPrice = normalFunction.evaluate(dataSpread);
    return CurrencyAmount.of(cmsSpread.getCurrency(), cmsSpreadPrice);
  }

  @Override
  public CurrencyAmount presentValue(InterestRateDerivative instrument, YieldCurveBundle curves) {
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
   * Computes the present value curves sensitivity of a CMS spread cap/floor in the binormal approach. 
   * For the CMS cap/floor volatility calibration, ATM forward strikes are used. The change of strike due to curve change is not taken into account in the curve sensitivity;
   * it is the sensitivity with constant calibration strikes.
   * @param cmsSpread The CMS spread cap/floor.
   * @param sabrData The SABR data bundle.
   * @return The present value curve sensitivity.
   */
  public InterestRateCurveSensitivity presentValueSensitivity(final CapFloorCMSSpread cmsSpread, final SABRInterestRateDataBundle sabrData) {
    // Forward sweep
    double forward1 = PRC.visit(cmsSpread.getUnderlyingSwap1(), sabrData);
    double forward2 = PRC.visit(cmsSpread.getUnderlyingSwap2(), sabrData);
    CouponCMS cmsCoupon1 = CouponCMS.from(cmsSpread, cmsSpread.getUnderlyingSwap1(), cmsSpread.getSettlementTime());
    CouponCMS cmsCoupon2 = CouponCMS.from(cmsSpread, cmsSpread.getUnderlyingSwap2(), cmsSpread.getSettlementTime());
    CapFloorCMS cmsCap1 = CapFloorCMS.from(cmsCoupon1, forward1, true);
    CapFloorCMS cmsCap2 = CapFloorCMS.from(cmsCoupon2, forward2, true);
    double cmsCoupon1Price = METHOD_CMS_COUPON.presentValue(cmsCoupon1, sabrData);
    double cmsCoupon2Price = METHOD_CMS_COUPON.presentValue(cmsCoupon2, sabrData);
    double cmsCap1Price = METHOD_CMS_CAP.presentValue(cmsCap1, sabrData).getAmount();
    double cmsCap2Price = METHOD_CMS_CAP.presentValue(cmsCap2, sabrData).getAmount();
    double discountFactorPayment = sabrData.getCurve(cmsSpread.getFundingCurveName()).getDiscountFactor(cmsSpread.getPaymentTime());
    double factor = discountFactorPayment * cmsCap1.getNotional() * cmsCap1.getPaymentYearFraction();
    double expectation1 = cmsCoupon1Price / factor;
    double expectation2 = cmsCoupon2Price / factor;
    BlackFunctionData dataCap1 = new BlackFunctionData(expectation1, factor, 0.0);
    EuropeanVanillaOption optionCap1 = new EuropeanVanillaOption(forward1, cmsSpread.getFixingTime(), true);
    double cmsCap1ImpliedVolatility = NORMAL_IMPLIED_VOLATILITY.getImpliedVolatility(dataCap1, optionCap1, cmsCap1Price);
    BlackFunctionData dataCap2 = new BlackFunctionData(expectation2, factor, cmsCap1ImpliedVolatility);
    EuropeanVanillaOption optionCap2 = new EuropeanVanillaOption(forward2, cmsSpread.getFixingTime(), true);
    double cmsCap2ImpliedVolatility = NORMAL_IMPLIED_VOLATILITY.getImpliedVolatility(dataCap2, optionCap2, cmsCap2Price);
    double rho = _correlation.evaluate(cmsSpread.getStrike());
    double cmsSpreadImpliedVolatility = Math.sqrt(cmsCap1ImpliedVolatility * cmsCap1ImpliedVolatility - 2 * rho * cmsCap1ImpliedVolatility * cmsCap2ImpliedVolatility + cmsCap2ImpliedVolatility
        * cmsCap2ImpliedVolatility);
    BlackFunctionData dataSpread = new BlackFunctionData(expectation1 - expectation2, discountFactorPayment * cmsSpread.getNotional() * cmsSpread.getPaymentYearFraction(), cmsSpreadImpliedVolatility);
    EuropeanVanillaOption optionSpread = new EuropeanVanillaOption(cmsSpread.getStrike(), cmsSpread.getFixingTime(), cmsSpread.isCap());
    double[] cmsSpreadPriceDerivative = new double[3];
    NORMAL_PRICE.getPriceAdjoint(optionSpread, dataSpread, cmsSpreadPriceDerivative);
    // Backward sweep
    double cmsSpreadPriceBar = 1.0;
    double cmsSpreadImpliedVolatilityBar = cmsSpreadPriceDerivative[1] * cmsSpreadPriceBar;
    double cmsCap2ImpliedVolatilityBar = (cmsCap2ImpliedVolatility - rho * cmsCap1ImpliedVolatility) / cmsSpreadImpliedVolatility * cmsSpreadImpliedVolatilityBar;
    double cmsCap1ImpliedVolatilityBar = (cmsCap1ImpliedVolatility - rho * cmsCap2ImpliedVolatility) / cmsSpreadImpliedVolatility * cmsSpreadImpliedVolatilityBar;
    dataCap2 = new BlackFunctionData(expectation2, factor, cmsCap2ImpliedVolatility);
    double[] cmsCap2PriceNormalDerivative = new double[3];
    NORMAL_PRICE.getPriceAdjoint(optionCap2, dataCap2, cmsCap2PriceNormalDerivative);
    double expectation2Bar = -cmsSpreadPriceDerivative[0] * cmsSpreadPriceBar + -cmsCap2PriceNormalDerivative[0] / cmsCap2PriceNormalDerivative[1] * cmsCap2ImpliedVolatilityBar;
    dataCap1 = new BlackFunctionData(expectation1, factor, cmsCap1ImpliedVolatility);
    double[] cmsCap1PriceNormalDerivative = new double[3];
    NORMAL_PRICE.getPriceAdjoint(optionCap1, dataCap1, cmsCap1PriceNormalDerivative);
    double expectation1Bar = cmsSpreadPriceDerivative[0] * cmsSpreadPriceBar + -cmsCap1PriceNormalDerivative[0] / cmsCap1PriceNormalDerivative[1] * cmsCap1ImpliedVolatilityBar;
    double factorBar = -cmsCoupon1Price / (factor * factor) * expectation1Bar + -cmsCoupon2Price / (factor * factor) * expectation2Bar - cmsCap2Price / factor / cmsCap2PriceNormalDerivative[1]
        * cmsCap2ImpliedVolatilityBar - cmsCap1Price / factor / cmsCap1PriceNormalDerivative[1] * cmsCap1ImpliedVolatilityBar;
    double discountFactorPaymentBar = cmsCap1.getNotional() * cmsCap1.getPaymentYearFraction() * factorBar;
    double cmsCap2PriceBar = 1.0 / cmsCap2PriceNormalDerivative[1] * cmsCap2ImpliedVolatilityBar;
    double cmsCap1PriceBar = 1.0 / cmsCap1PriceNormalDerivative[1] * cmsCap1ImpliedVolatilityBar;
    double cmsCoupon2PriceBar = expectation2Bar / factor;
    double cmsCoupon1PriceBar = expectation1Bar / factor;
    InterestRateCurveSensitivity cmsCoupon1CurveSensitivity = METHOD_CMS_COUPON.presentValueCurveSensitivity(cmsCoupon1, sabrData);
    InterestRateCurveSensitivity cmsCoupon2CurveSensitivity = METHOD_CMS_COUPON.presentValueCurveSensitivity(cmsCoupon2, sabrData);
    InterestRateCurveSensitivity cmsCap1CurveSensitivity = METHOD_CMS_CAP.presentValueSensitivity(cmsCap1, sabrData);
    InterestRateCurveSensitivity cmsCap2CurveSensitivity = METHOD_CMS_CAP.presentValueSensitivity(cmsCap2, sabrData);
    final List<DoublesPair> list = new ArrayList<DoublesPair>();
    list.add(new DoublesPair(cmsSpread.getPaymentTime(), -cmsSpread.getPaymentTime() * discountFactorPayment));
    final Map<String, List<DoublesPair>> resultMap = new HashMap<String, List<DoublesPair>>();
    resultMap.put(cmsSpread.getFundingCurveName(), list);
    InterestRateCurveSensitivity dfCurveSensitivity = new InterestRateCurveSensitivity(resultMap);
    InterestRateCurveSensitivity result;
    result = dfCurveSensitivity.multiply(discountFactorPaymentBar);
    result = result.add(cmsCoupon1CurveSensitivity.multiply(cmsCoupon1PriceBar));
    result = result.add(cmsCoupon2CurveSensitivity.multiply(cmsCoupon2PriceBar));
    result = result.add(cmsCap1CurveSensitivity.multiply(cmsCap1PriceBar));
    result = result.add(cmsCap2CurveSensitivity.multiply(cmsCap2PriceBar));
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
    double forward1 = PRC.visit(cmsSpread.getUnderlyingSwap1(), sabrData);
    double forward2 = PRC.visit(cmsSpread.getUnderlyingSwap2(), sabrData);
    CouponCMS cmsCoupon1 = CouponCMS.from(cmsSpread, cmsSpread.getUnderlyingSwap1(), cmsSpread.getSettlementTime());
    CouponCMS cmsCoupon2 = CouponCMS.from(cmsSpread, cmsSpread.getUnderlyingSwap2(), cmsSpread.getSettlementTime());
    CapFloorCMS cmsCap1 = CapFloorCMS.from(cmsCoupon1, forward1, true);
    CapFloorCMS cmsCap2 = CapFloorCMS.from(cmsCoupon2, forward2, true);
    double cmsCoupon1Price = METHOD_CMS_COUPON.presentValue(cmsCoupon1, sabrData);
    double cmsCoupon2Price = METHOD_CMS_COUPON.presentValue(cmsCoupon2, sabrData);
    double cmsCap1Price = METHOD_CMS_CAP.presentValue(cmsCap1, sabrData).getAmount();
    double cmsCap2Price = METHOD_CMS_CAP.presentValue(cmsCap2, sabrData).getAmount();
    double discountFactorPayment = sabrData.getCurve(cmsSpread.getFundingCurveName()).getDiscountFactor(cmsSpread.getPaymentTime());
    double factor = discountFactorPayment * cmsCap1.getNotional() * cmsCap1.getPaymentYearFraction();
    double expectation1 = cmsCoupon1Price / factor;
    double expectation2 = cmsCoupon2Price / factor;
    BlackFunctionData dataCap1 = new BlackFunctionData(expectation1, factor, 0.0);
    EuropeanVanillaOption optionCap1 = new EuropeanVanillaOption(forward1, cmsSpread.getFixingTime(), true);
    double cmsCap1ImpliedVolatility = NORMAL_IMPLIED_VOLATILITY.getImpliedVolatility(dataCap1, optionCap1, cmsCap1Price);
    BlackFunctionData dataCap2 = new BlackFunctionData(expectation2, factor, cmsCap1ImpliedVolatility);
    EuropeanVanillaOption optionCap2 = new EuropeanVanillaOption(forward2, cmsSpread.getFixingTime(), true);
    double cmsCap2ImpliedVolatility = NORMAL_IMPLIED_VOLATILITY.getImpliedVolatility(dataCap2, optionCap2, cmsCap2Price);
    double rho = _correlation.evaluate(cmsSpread.getStrike());
    double cmsSpreadImpliedVolatility = Math.sqrt(cmsCap1ImpliedVolatility * cmsCap1ImpliedVolatility - 2 * rho * cmsCap1ImpliedVolatility * cmsCap2ImpliedVolatility + cmsCap2ImpliedVolatility
        * cmsCap2ImpliedVolatility);
    BlackFunctionData dataSpread = new BlackFunctionData(expectation1 - expectation2, discountFactorPayment * cmsSpread.getNotional() * cmsSpread.getPaymentYearFraction(), cmsSpreadImpliedVolatility);
    EuropeanVanillaOption optionSpread = new EuropeanVanillaOption(cmsSpread.getStrike(), cmsSpread.getFixingTime(), cmsSpread.isCap());
    double[] cmsSpreadPriceDerivative = new double[3];
    NORMAL_PRICE.getPriceAdjoint(optionSpread, dataSpread, cmsSpreadPriceDerivative);
    // Backward sweep
    double cmsSpreadPriceBar = 1.0;
    double cmsSpreadImpliedVolatilityBar = cmsSpreadPriceDerivative[1] * cmsSpreadPriceBar;
    double cmsCap2ImpliedVolatilityBar = (cmsCap2ImpliedVolatility - rho * cmsCap1ImpliedVolatility) / cmsSpreadImpliedVolatility * cmsSpreadImpliedVolatilityBar;
    double cmsCap1ImpliedVolatilityBar = (cmsCap1ImpliedVolatility - rho * cmsCap2ImpliedVolatility) / cmsSpreadImpliedVolatility * cmsSpreadImpliedVolatilityBar;
    dataCap2 = new BlackFunctionData(expectation2, factor, cmsCap2ImpliedVolatility);
    double[] cmsCap2PriceNormalDerivative = new double[3];
    NORMAL_PRICE.getPriceAdjoint(optionCap2, dataCap2, cmsCap2PriceNormalDerivative);
    double expectation2Bar = -cmsSpreadPriceDerivative[0] * cmsSpreadPriceBar + -cmsCap2PriceNormalDerivative[0] / cmsCap2PriceNormalDerivative[1] * cmsCap2ImpliedVolatilityBar;
    dataCap1 = new BlackFunctionData(expectation1, factor, cmsCap1ImpliedVolatility);
    double[] cmsCap1PriceNormalDerivative = new double[3];
    NORMAL_PRICE.getPriceAdjoint(optionCap1, dataCap1, cmsCap1PriceNormalDerivative);
    double expectation1Bar = cmsSpreadPriceDerivative[0] * cmsSpreadPriceBar + -cmsCap1PriceNormalDerivative[0] / cmsCap1PriceNormalDerivative[1] * cmsCap1ImpliedVolatilityBar;
    double cmsCap2PriceBar = 1.0 / cmsCap2PriceNormalDerivative[1] * cmsCap2ImpliedVolatilityBar;
    double cmsCap1PriceBar = 1.0 / cmsCap1PriceNormalDerivative[1] * cmsCap1ImpliedVolatilityBar;
    double cmsCoupon2PriceBar = expectation2Bar / factor;
    double cmsCoupon1PriceBar = expectation1Bar / factor;
    PresentValueSABRSensitivityDataBundle cmsCoupon1SABRSensitivity = METHOD_CMS_COUPON.presentValueSABRSensitivity(cmsCoupon1, sabrData);
    PresentValueSABRSensitivityDataBundle cmsCoupon2SABRSensitivity = METHOD_CMS_COUPON.presentValueSABRSensitivity(cmsCoupon2, sabrData);
    PresentValueSABRSensitivityDataBundle cmsCap1SABRSensitivity = METHOD_CMS_CAP.presentValueSABRSensitivity(cmsCap1, sabrData);
    PresentValueSABRSensitivityDataBundle cmsCap2SABRSensitivity = METHOD_CMS_CAP.presentValueSABRSensitivity(cmsCap2, sabrData);
    cmsCoupon1SABRSensitivity.multiply(cmsCoupon1PriceBar);
    cmsCoupon2SABRSensitivity.multiply(cmsCoupon2PriceBar);
    cmsCap1SABRSensitivity.multiply(cmsCap1PriceBar);
    cmsCap2SABRSensitivity.multiply(cmsCap2PriceBar);
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
    public Double evaluate(Double x) {
      CapFloorCMSSpreadSABRBinormalMethod method = new CapFloorCMSSpreadSABRBinormalMethod(new RealPolynomialFunction1D(new double[] {x}));
      return method.presentValue(_cmsSpread, _sabrData).getAmount() - _price;
    }
  }

}
