/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments.method;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.ParRateCalculator;
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

/**
 *  Class used to compute the price of a CMS spread cap/floor with the binormal approach with correlation by strike.
 *  OpenGamma implementation note: Binormal with correlation by strike approach to CMS spread pricing, version 1.0, May 2011.
 */
public class CapFloorCMSSpreadSABRBinormalMethod {

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
   * Compute the present value of a CMS spread cap/floor in the binormal approach. 
   * @param cmsSpread The CMS spread cap/floor.
   * @param sabrData The SABR data bundle.
   * @return The present value.
   */
  public double presentValue(final CapFloorCMSSpread cmsSpread, final SABRInterestRateDataBundle sabrData) {
    final double forward1 = PRC.visit(cmsSpread.getUnderlyingSwap1(), sabrData);
    final double forward2 = PRC.visit(cmsSpread.getUnderlyingSwap2(), sabrData);
    final CouponCMS cmsCoupon1 = CouponCMS.from(cmsSpread, cmsSpread.getUnderlyingSwap1(), cmsSpread.getSettlementTime());
    final CouponCMS cmsCoupon2 = CouponCMS.from(cmsSpread, cmsSpread.getUnderlyingSwap2(), cmsSpread.getSettlementTime());
    final CapFloorCMS cmsCap1 = CapFloorCMS.from(cmsCoupon1, forward1, true);
    final CapFloorCMS cmsCap2 = CapFloorCMS.from(cmsCoupon2, forward2, true);
    final double cmsCoupon1Price = METHOD_CMS_COUPON.presentValue(cmsCoupon1, sabrData);
    final double cmsCoupon2Price = METHOD_CMS_COUPON.presentValue(cmsCoupon2, sabrData);
    final double cmsCap1Price = METHOD_CMS_CAP.presentValue(cmsCap1, sabrData).getAmount();
    final double cmsCap2Price = METHOD_CMS_CAP.presentValue(cmsCap2, sabrData).getAmount();
    final double discountFactorPayment = sabrData.getCurve(cmsSpread.getFundingCurveName()).getDiscountFactor(cmsSpread.getPaymentTime());
    final BlackFunctionData dataCap1 = new BlackFunctionData(cmsCoupon1Price / (discountFactorPayment * cmsCap1.getNotional() * cmsCap1.getPaymentYearFraction()), discountFactorPayment
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
    final EuropeanVanillaOption optionSpread = new EuropeanVanillaOption(cmsSpread.getStrike(), cmsSpread.getFixingTime(), cmsSpread.isCap());
    final Function1D<BlackFunctionData, Double> normalFunction = NORMAL_PRICE.getPriceFunction(optionSpread);
    final double cmsSpreadPrice = normalFunction.evaluate(dataSpread);
    return cmsSpreadPrice;
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
      final CapFloorCMSSpreadSABRBinormalMethod method = new CapFloorCMSSpreadSABRBinormalMethod(new RealPolynomialFunction1D(new double[] {x}));
      return method.presentValue(_cmsSpread, _sabrData) - _price;
    }
  }

  /**
   * Gets the correlation (rho) as function of the strike.
   * @return The correlation
   */
  public DoubleFunction1D getCorrelation() {
    return _correlation;
  }

}
