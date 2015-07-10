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
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.SABRExtrapolationRightFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 *  Class used to compute the price of a CMS cap/floor by swaption replication on a SABR formula with extrapolation.
 *  Reference: Hagan, P. S. (2003). Convexity conundrums: Pricing CMS swaps, caps, and floors. Wilmott Magazine, March, pages 38--44.
 *  OpenGamma implementation note: Replication pricing for linear and TEC format CMS, Version 1.2, March 2011.
 *  OpenGamma implementation note for the extrapolation: Smile extrapolation, version 1.2, May 2011.
 *  @deprecated Use classes descended from {@link CapFloorCMSSABRReplicationAbstractMethod}
 */
@Deprecated
public class CapFloorCMSSABRExtrapolationRightReplicationMethod extends CapFloorCMSSABRReplicationAbstractMethod {

  /**
   * The cut-off strike. The smile is extrapolated above that level.
   */
  private final double _cutOffStrike;
  /**
   * The tail thickness parameter.
   */
  private final double _mu;

  /**
   * The par rate calculator.
   */
  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();
  /**
   * The par rate sensitivity calculator.
   */
  private static final ParRateCurveSensitivityCalculator PRSC = ParRateCurveSensitivityCalculator.getInstance();

  /**
   * Default constructor of the CMS cap/floor replication method. The default integration interval is 1.00 (100%).
   * @param cutOffStrike The cut-off strike.
   * @param mu The tail thickness parameter.
   */
  public CapFloorCMSSABRExtrapolationRightReplicationMethod(final double cutOffStrike, final double mu) {
    super(1.0);
    _mu = mu;
    _cutOffStrike = cutOffStrike;
  }

  /**
   * Default constructor of the CMS cap/floor replication method. The default integration interval is 1.00 (100%).
   * @param cutOffStrike The cut-off strike.
   * @param mu The tail thickness parameter.
   * @param integrationInterval Integration range.
   */
  public CapFloorCMSSABRExtrapolationRightReplicationMethod(final double cutOffStrike, final double mu, final double integrationInterval) {
    super(integrationInterval);
    _mu = mu;
    _cutOffStrike = cutOffStrike;
  }

  /**
   * Compute the present value of a CMS cap/floor by replication in SABR framework with extrapolation on the right.
   * @param cmsCapFloor The CMS cap/floor.
   * @param sabrData The SABR data bundle.
   * @return The present value.
   */
  @Override
  public CurrencyAmount presentValue(final CapFloorCMS cmsCapFloor, final SABRInterestRateDataBundle sabrData) {
    Validate.notNull(cmsCapFloor);
    Validate.notNull(sabrData);
    final SABRInterestRateParameters sabrParameter = sabrData.getSABRParameter();
    final SwapFixedCoupon<? extends Payment> underlyingSwap = cmsCapFloor.getUnderlyingSwap();
    final double forward = underlyingSwap.accept(PRC, sabrData);
    final double discountFactorTp = sabrData.getCurve(underlyingSwap.getFixedLeg().getNthPayment(0).getFundingCurveName())
        .getDiscountFactor(cmsCapFloor.getPaymentTime());
    final double maturity = underlyingSwap.getFixedLeg().getNthPayment(underlyingSwap.getFixedLeg().getNumberOfPayments() - 1).getPaymentTime()
        - cmsCapFloor.getSettlementTime();
    final DoublesPair expiryMaturity = DoublesPair.of(cmsCapFloor.getFixingTime(), maturity);
    final double alpha = sabrParameter.getAlpha(expiryMaturity);
    final double beta = sabrParameter.getBeta(expiryMaturity);
    final double rho = sabrParameter.getRho(expiryMaturity);
    final double nu = sabrParameter.getNu(expiryMaturity);
    final SABRFormulaData sabrPoint = new SABRFormulaData(alpha, beta, rho, nu);
    final CMSIntegrant integrant = new CMSIntegrant(cmsCapFloor, sabrPoint, forward, _cutOffStrike, _mu);
    final double strike = cmsCapFloor.getStrike();
    final double factor = discountFactorTp / integrant.h(forward) * integrant.g(forward);
    final double strikePart = factor * integrant.k(strike) * integrant.bs(strike);
    final double absoluteTolerance = 1.0 / (factor * Math.abs(cmsCapFloor.getNotional()) * cmsCapFloor.getPaymentYearFraction());
    final double relativeTolerance = 1E-10;
    final RungeKuttaIntegrator1D integrator = new RungeKuttaIntegrator1D(absoluteTolerance, relativeTolerance, getNbIteration());
    double integralPart;
    try {
      if (cmsCapFloor.isCap()) {
        integralPart = discountFactorTp * integrator.integrate(integrant, strike, strike + getIntegrationInterval());
      } else {
        integralPart = discountFactorTp * integrator.integrate(integrant, 0.0, strike);
      }
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
    final double priceCMS = (strikePart + integralPart) * cmsCapFloor.getNotional() * cmsCapFloor.getPaymentYearFraction();
    return CurrencyAmount.of(cmsCapFloor.getCurrency(), priceCMS);
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof CapFloorCMS, "CMS cap/floor");
    Validate.isTrue(curves instanceof SABRInterestRateDataBundle, "Bundle should contain SABR data");
    return presentValue((CapFloorCMS) instrument, (SABRInterestRateDataBundle) curves);
  }

  /**
   * Computes the present value sensitivity to the yield curves of a CMS cap/floor by replication in the SABR framework with extrapolation on the right.
   * @param cmsCapFloor The CMS cap/floor.
   * @param sabrData The SABR data bundle. The SABR function need to be the Hagan function.
   * @return The present value sensitivity to curves.
   */
  @Override
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final CapFloorCMS cmsCapFloor, final SABRInterestRateDataBundle sabrData) {
    Validate.notNull(cmsCapFloor);
    Validate.notNull(sabrData);
    final SABRInterestRateParameters sabrParameter = sabrData.getSABRParameter();
    final SwapFixedCoupon<? extends Payment> underlyingSwap = cmsCapFloor.getUnderlyingSwap();
    final double forward = underlyingSwap.accept(PRC, sabrData);
    final double discountFactor = sabrData.getCurve(underlyingSwap.getFixedLeg().getNthPayment(0).getFundingCurveName()).getDiscountFactor(cmsCapFloor.getPaymentTime());
    final double strike = cmsCapFloor.getStrike();
    final double maturity = underlyingSwap.getFixedLeg().getNthPayment(underlyingSwap.getFixedLeg().getNumberOfPayments() - 1).getPaymentTime()
        - cmsCapFloor.getSettlementTime();
    final DoublesPair expiryMaturity = DoublesPair.of(cmsCapFloor.getFixingTime(), maturity);
    final double alpha = sabrParameter.getAlpha(expiryMaturity);
    final double beta = sabrParameter.getBeta(expiryMaturity);
    final double rho = sabrParameter.getRho(expiryMaturity);
    final double nu = sabrParameter.getNu(expiryMaturity);
    final SABRFormulaData sabrPoint = new SABRFormulaData(alpha, beta, rho, nu);
    // Common
    final CMSIntegrant integrantPrice = new CMSIntegrant(cmsCapFloor, sabrPoint, forward, _cutOffStrike, _mu);
    final CMSDeltaIntegrant integrantDelta = new CMSDeltaIntegrant(cmsCapFloor, sabrPoint, forward, _cutOffStrike, _mu);
    final double factor = discountFactor / integrantDelta.h(forward) * integrantDelta.g(forward);
    final double absoluteTolerance = 1.0 / (factor * Math.abs(cmsCapFloor.getNotional()) * cmsCapFloor.getPaymentYearFraction());
    final double relativeTolerance = 1E-10;
    final RungeKuttaIntegrator1D integrator = new RungeKuttaIntegrator1D(absoluteTolerance, relativeTolerance, getNbIteration());
    // Price
    final double[] bs = integrantDelta.bsbsp(strike);
    @SuppressWarnings("synthetic-access")
    final double[] n = integrantDelta.nnp(forward);
    final double strikePartPrice = discountFactor * integrantDelta.k(strike) * n[0] * bs[0];
    double integralPartPrice;
    try {
      if (cmsCapFloor.isCap()) {
        integralPartPrice = discountFactor * integrator.integrate(integrantPrice, strike, strike + getIntegrationInterval());
      } else {
        integralPartPrice = discountFactor * integrator.integrate(integrantPrice, 0.0, strike);
      }
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
    final double price = (strikePartPrice + integralPartPrice) * cmsCapFloor.getNotional() * cmsCapFloor.getPaymentYearFraction();
    // Delta
    final double strikePart = discountFactor * integrantDelta.k(strike) * (n[1] * bs[0] + n[0] * bs[1]);
    double integralPart;
    try {
      if (cmsCapFloor.isCap()) {
        integralPart = discountFactor * integrator.integrate(integrantDelta, strike, strike + getIntegrationInterval());
      } else {
        integralPart = discountFactor * integrator.integrate(integrantDelta, 0.0, strike);
      }
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
    final double deltaS0 = (strikePart + integralPart) * cmsCapFloor.getNotional() * cmsCapFloor.getPaymentYearFraction();
    final double deltaPD = price / discountFactor;
    final double sensiDF = -cmsCapFloor.getPaymentTime() * discountFactor * deltaPD;
    final List<DoublesPair> list = new ArrayList<>();
    list.add(DoublesPair.of(cmsCapFloor.getPaymentTime(), sensiDF));
    final Map<String, List<DoublesPair>> resultMap = new HashMap<>();
    resultMap.put(cmsCapFloor.getUnderlyingSwap().getFixedLeg().getNthPayment(0).getFundingCurveName(), list);
    InterestRateCurveSensitivity result = new InterestRateCurveSensitivity(resultMap);
    final InterestRateCurveSensitivity forwardDr = new InterestRateCurveSensitivity(cmsCapFloor.getUnderlyingSwap().accept(PRSC, sabrData));
    result = result.plus(forwardDr.multipliedBy(deltaS0));
    return result;
  }

  /**
   * Computes the present value sensitivity to the SABR parameters of a CMS cap/floor by replication in SABR framework with extrapolation on the right.
   * @param cmsCapFloor The CMS cap/floor.
   * @param sabrData The SABR data bundle. The SABR function need to be the Hagan function.
   * @return The present value sensitivity to SABR parameters.
   */
  @Override
  public PresentValueSABRSensitivityDataBundle presentValueSABRSensitivity(final CapFloorCMS cmsCapFloor, final SABRInterestRateDataBundle sabrData) {
    final SABRInterestRateParameters sabrParameter = sabrData.getSABRParameter();
    final SwapFixedCoupon<? extends Payment> underlyingSwap = cmsCapFloor.getUnderlyingSwap();
    final double forward = underlyingSwap.accept(PRC, sabrData);
    final double discountFactorTp = sabrData.getCurve(underlyingSwap.getFixedLeg().getNthPayment(0).getFundingCurveName())
        .getDiscountFactor(cmsCapFloor.getPaymentTime());
    final double strike = cmsCapFloor.getStrike();
    final double maturity = underlyingSwap.getFixedLeg().getNthPayment(underlyingSwap.getFixedLeg().getNumberOfPayments() - 1).getPaymentTime()
        - cmsCapFloor.getSettlementTime();
    final DoublesPair expiryMaturity = DoublesPair.of(cmsCapFloor.getFixingTime(), maturity);
    final double alpha = sabrParameter.getAlpha(expiryMaturity);
    final double beta = sabrParameter.getBeta(expiryMaturity);
    final double rho = sabrParameter.getRho(expiryMaturity);
    final double nu = sabrParameter.getNu(expiryMaturity);
    final SABRFormulaData sabrPoint = new SABRFormulaData(alpha, beta, rho, nu);
    final CMSVegaIntegrant integrantVega = new CMSVegaIntegrant(cmsCapFloor, sabrPoint, forward, _cutOffStrike, _mu);
    final double factor = discountFactorTp / integrantVega.h(forward) * integrantVega.g(forward);
    final SABRExtrapolationRightFunction sabrExtrapolation = new SABRExtrapolationRightFunction(forward, sabrPoint, _cutOffStrike, cmsCapFloor.getFixingTime(), _mu);
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, cmsCapFloor.getFixingTime(), cmsCapFloor.isCap());
    final double factor2 = factor * integrantVega.k(strike);
    final double[] strikePartPrice = new double[4];
    sabrExtrapolation.priceAdjointSABR(option, strikePartPrice);
    for (int loopvega = 0; loopvega < 4; loopvega++) {
      strikePartPrice[loopvega] *= factor2;
    }
    final double absoluteTolerance = 1.0 / (factor * Math.abs(cmsCapFloor.getNotional()) * cmsCapFloor.getPaymentYearFraction());
    final double relativeTolerance = 1E-3;
    final RungeKuttaIntegrator1D integrator = new RungeKuttaIntegrator1D(absoluteTolerance, relativeTolerance, getNbIteration());
    final double[] integralPart = new double[4];
    final double[] totalSensi = new double[4];
    for (int loopparameter = 0; loopparameter < 4; loopparameter++) {
      integrantVega.setParameterIndex(loopparameter);
      try {
        if (cmsCapFloor.isCap()) {
          integralPart[loopparameter] = discountFactorTp * integrator.integrate(integrantVega, strike, strike + getIntegrationInterval());
        } else {
          integralPart[loopparameter] = discountFactorTp * integrator.integrate(integrantVega, 0.0, strike);
        }
      } catch (final Exception e) {
        throw new RuntimeException(e);
      }
      totalSensi[loopparameter] = (strikePartPrice[loopparameter] + integralPart[loopparameter]) * cmsCapFloor.getNotional() * cmsCapFloor.getPaymentYearFraction();
    }
    final PresentValueSABRSensitivityDataBundle sensi = new PresentValueSABRSensitivityDataBundle();
    sensi.addAlpha(expiryMaturity, totalSensi[0]);
    sensi.addBeta(expiryMaturity, totalSensi[1]);
    sensi.addRho(expiryMaturity, totalSensi[2]);
    sensi.addNu(expiryMaturity, totalSensi[3]);
    return sensi;
  }

  /**
   * Computes the present value sensitivity to the strike of a CMS cap/floor by replication in SABR framework with extrapolation on the right.
   * @param cmsCapFloor The CMS cap/floor.
   * @param sabrData The SABR data bundle. The SABR function need to be the Hagan function.
   * @return The present value sensitivity to strike.
   */
  @Override
  public double presentValueStrikeSensitivity(final CapFloorCMS cmsCapFloor, final SABRInterestRateDataBundle sabrData) {
    final SABRInterestRateParameters sabrParameter = sabrData.getSABRParameter();
    final SwapFixedCoupon<? extends Payment> underlyingSwap = cmsCapFloor.getUnderlyingSwap();
    final double forward = underlyingSwap.accept(PRC, sabrData);
    final double discountFactor = sabrData.getCurve(underlyingSwap.getFixedLeg().getNthPayment(0).getFundingCurveName()).getDiscountFactor(cmsCapFloor.getPaymentTime());
    final double strike = cmsCapFloor.getStrike();
    final double maturity = underlyingSwap.getFixedLeg().getNthPayment(underlyingSwap.getFixedLeg().getNumberOfPayments() - 1).getPaymentTime()
        - cmsCapFloor.getSettlementTime();
    final DoublesPair expiryMaturity = DoublesPair.of(cmsCapFloor.getFixingTime(), maturity);
    final double alpha = sabrParameter.getAlpha(expiryMaturity);
    final double beta = sabrParameter.getBeta(expiryMaturity);
    final double rho = sabrParameter.getRho(expiryMaturity);
    final double nu = sabrParameter.getNu(expiryMaturity);
    final SABRFormulaData sabrPoint = new SABRFormulaData(alpha, beta, rho, nu);
    final CMSStrikeIntegrant integrant = new CMSStrikeIntegrant(cmsCapFloor, sabrPoint, forward, _cutOffStrike, _mu);
    final double factor = discountFactor * integrant.g(forward) / integrant.h(forward);
    final double absoluteTolerance = 1.0E-9;
    final double relativeTolerance = 1.0E-5;
    final RungeKuttaIntegrator1D integrator = new RungeKuttaIntegrator1D(absoluteTolerance, relativeTolerance, getNbIteration());
    final SABRExtrapolationRightFunction sabrExtrapolation = new SABRExtrapolationRightFunction(forward, sabrPoint, _cutOffStrike, cmsCapFloor.getFixingTime(), _mu);
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, cmsCapFloor.getFixingTime(), cmsCapFloor.isCap());
    final double[] kpkpp = integrant.kpkpp(strike);
    double firstPart;
    double thirdPart;
    if (cmsCapFloor.isCap()) {
      firstPart = -kpkpp[0] * integrant.bs(strike);
      thirdPart = integrator.integrate(integrant, strike, strike + getIntegrationInterval());
    } else {
      firstPart = 3 * kpkpp[0] * integrant.bs(strike);
      thirdPart = integrator.integrate(integrant, 0.0, strike);
    }
    final double secondPart = integrant.k(strike) * sabrExtrapolation.priceDerivativeStrike(option);

    return cmsCapFloor.getNotional() * cmsCapFloor.getPaymentYearFraction() * factor * (firstPart + secondPart + thirdPart);
  }

  /**
   * Inner class to implement the integration used in price replication.
   */
  private class CMSIntegrant extends Function1D<Double, Double> {
    protected static final double EPS = 1E-10;
    private final int _nbFixedPeriod;
    private final int _nbFixedPaymentYear;
    private final double _tau;
    private final double _delta;
    private final double _eta;
    private final double _timeToExpiry;
    private final double _strike;
    private final double _forward;
    private final double _factor;
    private final SABRExtrapolationRightFunction _sabrExtrapolation;
    private final boolean _isCall;

    /**
     * Gets the _nbFixedPeriod field.
     * @return the _nbFixedPeriod
     */
    public int getNbFixedPeriod() {
      return _nbFixedPeriod;
    }

    /**
     * Gets the _nbFixedPaymentYear field.
     * @return the _nbFixedPaymentYear
     */
    public int getNbFixedPaymentYear() {
      return _nbFixedPaymentYear;
    }

    /**
     * Gets the _tau field.
     * @return the _tau
     */
    public double getTau() {
      return _tau;
    }

    /**
     * Gets the _eta field.
     * @return the _eta
     */
    public double getEta() {
      return _eta;
    }

    /**
     * Gets the _timeToExpiry field.
     * @return the _timeToExpiry
     */
    public double getTimeToExpiry() {
      return _timeToExpiry;
    }

    /**
     * Gets the _sabrExtrapolation field.
     * @return the _sabrExtrapolation
     */
    public SABRExtrapolationRightFunction getSabrExtrapolation() {
      return _sabrExtrapolation;
    }

    /**
     * Gets the _isCall field.
     * @return the _isCall
     */
    public boolean isCall() {
      return _isCall;
    }

    /**
     * Gets the _strike field.
     * @return the _strike
     */
    public double getStrike() {
      return _strike;
    }

    /**
     * Constructor.
     * @param cmsCap The CMS cap/floor.
     * @param sabrParameter The SABR parameters.
     * @param forward The forward.
     * @param cutOffStrike The cut-off strike.
     * @param mu The tail thickness parameter.
     */
    public CMSIntegrant(final CapFloorCMS cmsCap, final SABRFormulaData sabrPoint, final double forward, final double cutOffStrike, final double mu) {
      _nbFixedPeriod = cmsCap.getUnderlyingSwap().getFixedLeg().getPayments().length;
      _nbFixedPaymentYear = (int) Math.round(1.0 / cmsCap.getUnderlyingSwap().getFixedLeg().getNthPayment(0).getPaymentYearFraction());
      _tau = 1.0 / _nbFixedPaymentYear;
      _delta = cmsCap.getPaymentTime() - cmsCap.getSettlementTime();
      _eta = -_delta;
      _timeToExpiry = cmsCap.getFixingTime();
      _forward = forward;
      _sabrExtrapolation = new SABRExtrapolationRightFunction(forward, sabrPoint, cutOffStrike, _timeToExpiry, mu);
      _isCall = cmsCap.isCap();
      _strike = cmsCap.getStrike();
      _factor = g(_forward) / h(_forward);
    }

    @Override
    public Double evaluate(final Double x) {
      final double[] kD = kpkpp(x);
      // Implementation note: kD[0] contains the first derivative of k; kD[1] the second derivative of k.
      return _factor * (kD[1] * (x - _strike) + 2.0 * kD[0]) * bs(x);
    }

    /**
     * The approximation of the discount factor as function of the swap rate.
     * @param x The swap rate.
     * @return The discount factor.
     */
    double h(final double x) {
      return Math.pow(1.0 + _tau * x, _eta);
    }

    /**
     * The cash annuity.
     * @param x The swap rate.
     * @return The annuity.
     */
    double g(final double x) {
      if (x >= EPS) {
        final double periodFactor = 1 + x / _nbFixedPaymentYear;
        final double nPeriodDiscount = Math.pow(periodFactor, -_nbFixedPeriod);
        return 1.0 / x * (1.0 - nPeriodDiscount);
      }
      return ((double) _nbFixedPeriod) / _nbFixedPaymentYear;
    }

    /**
     * The factor used in the strike part and in the integration of the replication.
     * @param x The swap rate.
     * @return The factor.
     */
    double k(final double x) {
      double g;
      double h;
      if (x >= EPS) {
        final double periodFactor = 1 + x / _nbFixedPaymentYear;
        final double nPeriodDiscount = Math.pow(periodFactor, -_nbFixedPeriod);
        g = 1.0 / x * (1.0 - nPeriodDiscount);
        h = Math.pow(1.0 + _tau * x, _eta);
      } else {
        g = ((double) _nbFixedPeriod) / _nbFixedPaymentYear;
        h = 1.0;
      }
      return h / g;
    }

    /**
     * The first and second derivative of the function k.
     * @param x The swap rate.
     * @return The derivative (first element is the first derivative, second element is second derivative.
     */
    protected double[] kpkpp(final double x) {
      final double periodFactor = 1 + x / _nbFixedPaymentYear;
      final double nPeriodDiscount = Math.pow(periodFactor, -_nbFixedPeriod);
      /**
       * The value of the annuity and its first and second derivative.
       */
      double g, gp, gpp;
      if (x >= EPS) {
        g = 1.0 / x * (1.0 - nPeriodDiscount);
        gp = -g / x + _nbFixedPeriod / x / _nbFixedPaymentYear * nPeriodDiscount / periodFactor;
        gpp = 2.0 / (x * x) * g - 2.0 * _nbFixedPeriod / (x * x) / _nbFixedPaymentYear * nPeriodDiscount / periodFactor - (_nbFixedPeriod + 1.0) * _nbFixedPeriod / x
            / (_nbFixedPaymentYear * _nbFixedPaymentYear) * nPeriodDiscount / (periodFactor * periodFactor);
      } else {
        // Implementation comment: When x is (almost) 0, useful for CMS swaps which are priced as CMS cap of strike 0.
        g = ((double) _nbFixedPeriod) / _nbFixedPaymentYear;
        gp = -_nbFixedPeriod / 2.0 * (_nbFixedPeriod + 1.0) / (_nbFixedPaymentYear * _nbFixedPaymentYear);
        gpp = _nbFixedPeriod / 2.0 * (_nbFixedPeriod + 1.0) * (1.0 + (_nbFixedPeriod + 2.0) / 3.0) / (_nbFixedPaymentYear * _nbFixedPaymentYear * _nbFixedPaymentYear);
      }
      final double h = Math.pow(1.0 + _tau * x, _eta);
      final double hp = _eta * _tau * h / periodFactor;
      final double hpp = (_eta - 1.0) * _tau * hp / periodFactor;
      final double kp = hp / g - h * gp / (g * g);
      final double kpp = hpp / g - 2 * hp * gp / (g * g) - h * (gpp / (g * g) - 2 * (gp * gp) / (g * g * g));
      return new double[] {kp, kpp };
    }

    /**
     * The Black-Scholes formula with numeraire 1 as function of the strike.
     * @param strike The strike.
     * @return The Black-Scholes formula.
     */
    double bs(final double strike) {
      final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, _timeToExpiry, _isCall);
      return _sabrExtrapolation.price(option);
    }
  }

  private class CMSDeltaIntegrant extends CMSIntegrant {

    private final double[] _nnp;

    public CMSDeltaIntegrant(final CapFloorCMS cmsCap, final SABRFormulaData sabrPoint, final double forward, final double cutOffStrike, final double mu) {
      super(cmsCap, sabrPoint, forward, cutOffStrike, mu);
      _nnp = nnp(forward);
    }

    @Override
    public Double evaluate(final Double x) {
      final double[] kD = kpkpp(x);
      // Implementation note: kD[0] contains the first derivative of k; kD[1] the second derivative of k.
      final double[] bs = bsbsp(x);
      return (kD[1] * (x - getStrike()) + 2.0 * kD[0]) * (_nnp[1] * bs[0] + _nnp[0] * bs[1]);
    }

    /**
     * The Black-Scholes formula and its derivative with respect to the forward.
     * @param strike The strike.
     * @return The Black-Scholes formula and its derivative.
     */
    double[] bsbsp(final double strike) {
      final double[] result = new double[2];
      final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, getTimeToExpiry(), isCall());
      result[0] = getSabrExtrapolation().price(option);
      result[1] = getSabrExtrapolation().priceDerivativeForward(option);
      return result;
    }

    private double[] nnp(final double x) {
      final double[] result = new double[2];
      final double[] ggp = ggp(x);
      final double[] hhp = hhp(x);
      result[0] = ggp[0] / hhp[0];
      result[1] = ggp[1] / hhp[0] - ggp[0] * hhp[1] / (hhp[0] * hhp[0]);
      return result;
    }

    private double[] ggp(final double x) {
      final double[] result = new double[2];
      if (x >= EPS) {
        final double periodFactor = 1 + x / getNbFixedPaymentYear();
        final double nPeriodDiscount = Math.pow(periodFactor, -getNbFixedPeriod());
        result[0] = 1.0 / x * (1.0 - nPeriodDiscount);
        result[1] = -result[0] / x + getTau() * getNbFixedPeriod() / x * nPeriodDiscount / periodFactor;
      } else {
        result[0] = getNbFixedPeriod() * getTau();
        result[1] = -getNbFixedPeriod() * (getNbFixedPeriod() + 1.0) * getTau() * getTau() / 2.0;
      }
      return result;
    }

    private double[] hhp(final double x) {
      final double[] result = new double[2];
      result[0] = Math.pow(1.0 + getTau() * x, getEta());
      result[1] = getEta() * getTau() * result[0] / (1 + x * getTau());
      return result;
    }

  }

  private class CMSVegaIntegrant extends CMSIntegrant {

    /**
     * The index of the sensitivity computed.
     */
    private int _parameterIndex;

    /**
     * Constructor.
     * @param cmsCap The CMS cap/floor.
     * @param sabrParameter The SABR parameters.
     * @param forward The forward.
     * @param cutOffStrike The cut-off strike.
     * @param mu The tail thickness parameter.
     */
    public CMSVegaIntegrant(final CapFloorCMS cmsCap, final SABRFormulaData sabrPoint, final double forward, final double cutOffStrike, final double mu) {
      super(cmsCap, sabrPoint, forward, cutOffStrike, mu);
    }

    /**
     * Sets the index of the sensitivity computed.
     * @param parameterIndex  The index.
     */
    public void setParameterIndex(final int parameterIndex) {
      this._parameterIndex = parameterIndex;
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public Double evaluate(final Double x) {
      final double[] kD = super.kpkpp(x);
      // Implementation note: kD[0] contains the first derivative of k; kD[1] the second derivative of k.
      final EuropeanVanillaOption option = new EuropeanVanillaOption(x, super._timeToExpiry, super._isCall);
      final double[] priceDerivativeSABR = new double[4];
      getSabrExtrapolation().priceAdjointSABR(option, priceDerivativeSABR);
      return super._factor * (kD[1] * (x - super._strike) + 2.0 * kD[0]) * priceDerivativeSABR[_parameterIndex];
    }

  }

  private class CMSStrikeIntegrant extends CMSIntegrant {

    /**
     * @param cmsCap
     * @param sabrParameter
     * @param forward
     */
    public CMSStrikeIntegrant(final CapFloorCMS cmsCap, final SABRFormulaData sabrPoint, final double forward, final double cutOffStrike, final double mu) {
      super(cmsCap, sabrPoint, forward, cutOffStrike, mu);
    }

    @Override
    public Double evaluate(final Double x) {
      final double[] kD = super.kpkpp(x);
      // Implementation note: kD[0] contains the first derivative of k; kD[1] the second derivative of k.
      return -kD[1] * bs(x);
    }

  } // End CMSStrikeIntegrant

}
