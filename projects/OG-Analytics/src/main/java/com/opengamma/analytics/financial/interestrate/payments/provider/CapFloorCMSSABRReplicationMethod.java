/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 *  Class used to compute the price of a CMS cap/floor by swaption replication on a SABR formula.
 *  Reference: Hagan, P. S. (2003). Convexity conundrums: Pricing CMS swaps, caps, and floors. Wilmott Magazine, March, pages 38--44.
 *  OpenGamma implementation note: Replication pricing for linear and TEC format CMS, Version 1.2, March 2011.
 */
public class CapFloorCMSSABRReplicationMethod extends CapFloorCMSSABRReplicationAbstractMethod {

  /**
   * The method default instance.
   */
  private static final CapFloorCMSSABRReplicationMethod INSTANCE = new CapFloorCMSSABRReplicationMethod();

  /**
   * Returns a default instance of the CMS cap/floor replication method. The default integration interval is 1.00 (100%).
   * @return The calculation method
   */
  public static CapFloorCMSSABRReplicationMethod getDefaultInstance() {
    return INSTANCE;
  }

  /**
   * The par rate calculator.
   */
  private static final ParRateDiscountingCalculator PRDC = ParRateDiscountingCalculator.getInstance();
  /**
   * The par rate sensitivity calculator.
   */
  private static final ParRateCurveSensitivityDiscountingCalculator PRCSDC = ParRateCurveSensitivityDiscountingCalculator.getInstance();

  /**
   * Default constructor of the CMS cap/floor replication method. The default integration interval is 1.00 (100%).
   */
  private CapFloorCMSSABRReplicationMethod() {
    super(1.0);
  }

  /**
   * Constructor of the CMS cap/floor replication method with the integration range.
   * @param integrationInterval Integration range.
   */
  public CapFloorCMSSABRReplicationMethod(final double integrationInterval) {
    super(integrationInterval);
  }

  /**
   * Compute the present value of a CMS cap/floor by replication in SABR framework.
   * For floor the replication is between 0.0 and the strike. 0.0 is used as the rates are always >=0.0 in SABR.
   * @param cmsCapFloor The CMS cap/floor.
   * @param sabrData The SABR data bundle.
   * @return The present value.
   */
  @Override
  public MultipleCurrencyAmount presentValue(final CapFloorCMS cmsCapFloor, final SABRSwaptionProviderInterface sabrData) {
    ArgumentChecker.notNull(cmsCapFloor, "CMA cap/floor");
    ArgumentChecker.notNull(sabrData, "SABR swaption provider");
    final Currency ccy = cmsCapFloor.getCurrency();
    final SABRInterestRateParameters sabrParameter = sabrData.getSABRParameter();
    final SwapFixedCoupon<? extends Payment> underlyingSwap = cmsCapFloor.getUnderlyingSwap();
    final double forward = underlyingSwap.accept(PRDC, sabrData.getMulticurveProvider());
    final double discountFactor = sabrData.getMulticurveProvider().getDiscountFactor(ccy, cmsCapFloor.getPaymentTime());
    final CMSIntegrant integrant = new CMSIntegrant(cmsCapFloor, sabrParameter, forward);
    final double strike = cmsCapFloor.getStrike();
    final double strikePart = discountFactor * integrant.g(forward) / integrant.h(forward) * integrant.k(strike) * integrant.bs(strike);
    final double absoluteTolerance = 0.1 / (discountFactor * Math.abs(cmsCapFloor.getNotional()) * cmsCapFloor.getPaymentYearFraction());
    // Implementation note: Each sub-integral has less than 0.1 currency unit error.
    final double relativeTolerance = 1.0E-6;
    final RungeKuttaIntegrator1D integrator = new RungeKuttaIntegrator1D(absoluteTolerance, relativeTolerance, getNbIteration());
    // TODO: replace the integrator by an integrator that does not used the limits (open end). Recorded as [PLAT-1679].
    // TODO: replace the integrator by an integrator that accept infinite interval (for the upper limit of cap).
    double integralPart;
    try {
      if (cmsCapFloor.isCap()) {
        integralPart = discountFactor * integrator.integrate(integrant, strike, strike + getIntegrationInterval());
      } else {
        integralPart = discountFactor * integrator.integrate(integrant, 0.0, strike);
      }
    } catch (final Exception e) {
      throw new MathException(e);
    }
    final double priceCMS = (strikePart + integralPart) * cmsCapFloor.getNotional() * cmsCapFloor.getPaymentYearFraction();
    return MultipleCurrencyAmount.of(cmsCapFloor.getCurrency(), priceCMS);
  }

  /**
   * Computes the present value sensitivity to the yield curves of a CMS cap/floor by replication in SABR framework.
   * @param cmsCapFloor The CMS cap/floor.
   * @param sabrData The SABR data bundle. The SABR function need to be the Hagan function.
   * @return The present value sensitivity to curves.
   */
  @Override
  @SuppressWarnings("synthetic-access")
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final CapFloorCMS cmsCapFloor, final SABRSwaptionProviderInterface sabrData) {
    ArgumentChecker.notNull(cmsCapFloor, "CMA cap/floor");
    ArgumentChecker.notNull(sabrData, "SABR swaption provider");
    final Currency ccy = cmsCapFloor.getCurrency();
    final SABRInterestRateParameters sabrParameter = sabrData.getSABRParameter();
    final SwapFixedCoupon<? extends Payment> underlyingSwap = cmsCapFloor.getUnderlyingSwap();
    final double forward = underlyingSwap.accept(PRDC, sabrData.getMulticurveProvider());
    final double discountFactor = sabrData.getMulticurveProvider().getDiscountFactor(ccy, cmsCapFloor.getPaymentTime());
    final double strike = cmsCapFloor.getStrike();
    // Common
    final CMSIntegrant integrantPrice = new CMSIntegrant(cmsCapFloor, sabrParameter, forward);
    final CMSDeltaIntegrant integrantDelta = new CMSDeltaIntegrant(cmsCapFloor, sabrParameter, forward);
    final double factor = discountFactor / integrantDelta.h(forward) * integrantDelta.g(forward);
    final double absoluteTolerance = 1.0E+1 / (factor * Math.abs(cmsCapFloor.getNotional()) * cmsCapFloor.getPaymentYearFraction());
    final double relativeTolerance = 1.0E-2;
    final RungeKuttaIntegrator1D integrator = new RungeKuttaIntegrator1D(absoluteTolerance, relativeTolerance, getNbIteration());
    // Price
    final double[] bs = integrantDelta.bsbsp(strike);
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
    final Map<String, List<DoublesPair>> resultMapDsc = new HashMap<>();
    resultMapDsc.put(sabrData.getMulticurveProvider().getName(ccy), list);
    final MulticurveSensitivity dscDp = MulticurveSensitivity.ofYieldDiscounting(resultMapDsc);
    final MulticurveSensitivity forwardDp = cmsCapFloor.getUnderlyingSwap().accept(PRCSDC, sabrData.getMulticurveProvider());
    return MultipleCurrencyMulticurveSensitivity.of(ccy, dscDp.plus(forwardDp.multipliedBy(deltaS0)));
  }

  /**
   * Computes the present value sensitivity to the SABR parameters of a CMS cap/floor by replication in SABR framework.
   * @param cmsCapFloor The CMS cap/floor.
   * @param sabrData The SABR data bundle. The SABR function need to be the Hagan function.
   * @return The present value sensitivity to SABR parameters.
   */
  @Override
  public PresentValueSABRSensitivityDataBundle presentValueSABRSensitivity(final CapFloorCMS cmsCapFloor, final SABRSwaptionProviderInterface sabrData) {
    ArgumentChecker.notNull(cmsCapFloor, "CMA cap/floor");
    ArgumentChecker.notNull(sabrData, "SABR swaption provider");
    final Currency ccy = cmsCapFloor.getCurrency();
    final SABRInterestRateParameters sabrParameter = sabrData.getSABRParameter();
    final SwapFixedCoupon<? extends Payment> underlyingSwap = cmsCapFloor.getUnderlyingSwap();
    final double forward = underlyingSwap.accept(PRDC, sabrData.getMulticurveProvider());
    final double discountFactor = sabrData.getMulticurveProvider().getDiscountFactor(ccy, cmsCapFloor.getPaymentTime());
    final double strike = cmsCapFloor.getStrike();
    final double maturity = underlyingSwap.getFixedLeg().getNthPayment(underlyingSwap.getFixedLeg().getNumberOfPayments() - 1).getPaymentTime() - cmsCapFloor.getSettlementTime();
    final CMSVegaIntegrant integrantVega = new CMSVegaIntegrant(cmsCapFloor, sabrParameter, forward);
    final double factor = discountFactor / integrantVega.h(forward) * integrantVega.g(forward);
    final double absoluteTolerance = 1.0 / (factor * Math.abs(cmsCapFloor.getNotional()) * cmsCapFloor.getPaymentYearFraction());
    final double relativeTolerance = 1E-3;
    final RungeKuttaIntegrator1D integrator = new RungeKuttaIntegrator1D(absoluteTolerance, relativeTolerance, getNbIteration());
    final double factor2 = factor * integrantVega.k(strike) * integrantVega.bs(strike);
    final double[] strikePartPrice = new double[4];
    final double[] volatilityAdjoint = sabrData.getSABRParameter().getVolatilityAdjoint(cmsCapFloor.getFixingTime(), maturity, strike, forward);
    strikePartPrice[0] = factor2 * volatilityAdjoint[3];
    strikePartPrice[1] = factor2 * volatilityAdjoint[4];
    strikePartPrice[2] = factor2 * volatilityAdjoint[5];
    strikePartPrice[3] = factor2 * volatilityAdjoint[6];
    final double[] integralPart = new double[4];
    final double[] totalSensi = new double[4];
    for (int loopparameter = 0; loopparameter < 4; loopparameter++) {
      integrantVega.setParameterIndex(loopparameter);
      try {
        if (cmsCapFloor.isCap()) {
          integralPart[loopparameter] = discountFactor * integrator.integrate(integrantVega, strike, strike + getIntegrationInterval());
        } else {
          integralPart[loopparameter] = discountFactor * integrator.integrate(integrantVega, 0.0, strike);
        }
      } catch (final Exception e) {
        throw new RuntimeException(e);
      }
      totalSensi[loopparameter] = (strikePartPrice[loopparameter] + integralPart[loopparameter]) * cmsCapFloor.getNotional() * cmsCapFloor.getPaymentYearFraction();
    }
    final PresentValueSABRSensitivityDataBundle sensi = new PresentValueSABRSensitivityDataBundle();
    final DoublesPair expiryMaturity = DoublesPair.of(cmsCapFloor.getFixingTime(), maturity);
    sensi.addAlpha(expiryMaturity, totalSensi[0]);
    sensi.addBeta(expiryMaturity, totalSensi[1]);
    sensi.addRho(expiryMaturity, totalSensi[2]);
    sensi.addNu(expiryMaturity, totalSensi[3]);
    return sensi;
  }

  /**
   * Computes the present value sensitivity to the strike of a CMS cap/floor by replication in SABR framework.
   * @param cmsCapFloor The CMS cap/floor.
   * @param sabrData The SABR data bundle. The SABR function need to be the Hagan function.
   * @return The present value sensitivity to strike.
   */
  @Override
  public double presentValueStrikeSensitivity(final CapFloorCMS cmsCapFloor, final SABRSwaptionProviderInterface sabrData) {
    ArgumentChecker.notNull(cmsCapFloor, "CMA cap/floor");
    ArgumentChecker.notNull(sabrData, "SABR swaption provider");
    final Currency ccy = cmsCapFloor.getCurrency();
    final SABRInterestRateParameters sabrParameter = sabrData.getSABRParameter();
    final SwapFixedCoupon<? extends Payment> underlyingSwap = cmsCapFloor.getUnderlyingSwap();
    final double forward = underlyingSwap.accept(PRDC, sabrData.getMulticurveProvider());
    final double discountFactor = sabrData.getMulticurveProvider().getDiscountFactor(ccy, cmsCapFloor.getPaymentTime());
    final double strike = cmsCapFloor.getStrike();
    final double maturity = underlyingSwap.getFixedLeg().getNthPayment(underlyingSwap.getFixedLeg().getNumberOfPayments() - 1).getPaymentTime() - cmsCapFloor.getSettlementTime();
    final DoublesPair expiryMaturity = DoublesPair.of(cmsCapFloor.getFixingTime(), maturity);

    final CMSStrikeIntegrant integrant = new CMSStrikeIntegrant(cmsCapFloor, sabrParameter, forward);
    final double factor = discountFactor * integrant.g(forward) / integrant.h(forward);

    final double absoluteTolerance = 1.0E-9;
    final double relativeTolerance = 1.0E-5;
    final RungeKuttaIntegrator1D integrator = new RungeKuttaIntegrator1D(absoluteTolerance, relativeTolerance, getNbIteration());

    final double alpha = sabrParameter.getAlpha(expiryMaturity);
    final double beta = sabrParameter.getBeta(expiryMaturity);
    final double rho = sabrParameter.getRho(expiryMaturity);
    final double nu = sabrParameter.getNu(expiryMaturity);
    final SABRFormulaData sabrPoint = new SABRFormulaData(alpha, beta, rho, nu);
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, cmsCapFloor.getFixingTime(), cmsCapFloor.isCap());
    final Function1D<SABRFormulaData, double[]> sabrFunctionAdjoint = sabrParameter.getSabrFunction().getVolatilityAdjointFunction(option, forward);
    final double[] volA = sabrFunctionAdjoint.evaluate(sabrPoint);
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volA[0]);
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final double[] bsA = blackFunction.getPriceAdjoint(option, dataBlack);

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
    final double secondPart = integrant.k(strike) * (bsA[3] + bsA[2] * volA[2]);

    return cmsCapFloor.getNotional() * cmsCapFloor.getPaymentYearFraction() * factor * (firstPart + secondPart + thirdPart);
  }

  /**
   * Inner class to implement the integration used in price replication.
   */
  private class CMSIntegrant extends Function1D<Double, Double> {
    private static final double EPS = 1E-10;
    private final int _nbFixedPeriod;
    private final int _nbFixedPaymentYear;
    private final double _tau;
    private final double _delta;
    private final double _eta;
    private final double _timeToExpiry;
    private final double _maturity;
    private final double _strike;
    private final double _forward;
    private final double _factor;
    private final SABRFormulaData _sabrData;
    /**
     * The function containing the SABR volatility formula.
     */
    private final VolatilityFunctionProvider<SABRFormulaData> _sabrFunction;
    private final BlackPriceFunction _blackFunction = new BlackPriceFunction();
    private final boolean _isCall;

    /**
     * Constructor of the integrant.
     * @param cmsCap The CMS cap/floor.
     * @param sabrParameter The SABR parameters.
     * @param forward The swap forward rate.
     */
    public CMSIntegrant(final CapFloorCMS cmsCap, final SABRInterestRateParameters sabrParameter, final double forward) {
      _nbFixedPeriod = cmsCap.getUnderlyingSwap().getFixedLeg().getPayments().length;
      _nbFixedPaymentYear = (int) Math.round(1.0 / cmsCap.getUnderlyingSwap().getFixedLeg().getNthPayment(0).getPaymentYearFraction());
      _tau = 1.0 / _nbFixedPaymentYear;
      _delta = cmsCap.getPaymentTime() - cmsCap.getSettlementTime();
      _eta = -_delta;
      _timeToExpiry = cmsCap.getFixingTime();
      // TODO: A better notion of maturity may be required (using period?)
      final AnnuityCouponFixed annuityFixed = cmsCap.getUnderlyingSwap().getFixedLeg();
      _maturity = annuityFixed.getNthPayment(annuityFixed.getNumberOfPayments() - 1).getPaymentTime() - cmsCap.getSettlementTime();
      _forward = forward;
      final DoublesPair expiryMaturity = DoublesPair.of(_timeToExpiry, _maturity);
      final double alpha = sabrParameter.getAlpha(expiryMaturity);
      final double beta = sabrParameter.getBeta(expiryMaturity);
      final double rho = sabrParameter.getRho(expiryMaturity);
      final double nu = sabrParameter.getNu(expiryMaturity);
      _sabrData = new SABRFormulaData(alpha, beta, rho, nu);
      _sabrFunction = sabrParameter.getSabrFunction();
      _isCall = cmsCap.isCap();
      _strike = cmsCap.getStrike();
      _factor = g(_forward) / h(_forward);
    }

    @Override
    public Double evaluate(final Double x) {
      final double[] kD = kpkpp(x);
      // Implementation note: kD[0] contains the first derivative of k; kD[1] the second derivative of k.
      return (kD[1] * (x - _strike) + 2.0 * kD[0]) * bs(x) * _factor;
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
    double[] kpkpp(final double x) {
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
      final double g2 = g * g;
      final double h = Math.pow(1.0 + _tau * x, _eta);
      final double hp = _eta * _tau * h / periodFactor;
      final double hpp = (_eta - 1.0) * _tau * hp / periodFactor;
      final double kp = hp / g - h * gp / g2;
      final double kpp = hpp / g - 2 * hp * gp / g2 - h * (gpp / g2 - 2 * (gp * gp) / (g2 * g));
      return new double[] {kp, kpp};
    }

    /**
     * The Black-Scholes formula with numeraire 1 as function of the strike.
     * @param strike The strike.
     * @return The Black-Scholes formula.
     */
    double bs(final double strike) {
      final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, _timeToExpiry, _isCall);
      final Function1D<SABRFormulaData, Double> funcSabr = _sabrFunction.getVolatilityFunction(option, _forward);
      final double volatility = funcSabr.evaluate(_sabrData);
      final BlackFunctionData dataBlack = new BlackFunctionData(_forward, 1.0, volatility);
      final Function1D<BlackFunctionData, Double> func = _blackFunction.getPriceFunction(option);
      return func.evaluate(dataBlack);
    }

    /**
     * Gets the eps field.
     * @return the eps
     */
    public double getEps() {
      return EPS;
    }

  }

  private class CMSDeltaIntegrant extends CMSIntegrant {

    private final double[] _nnp;

    /**
     * @param cmsCap
     * @param sabrParameter
     * @param forward
     */
    public CMSDeltaIntegrant(final CapFloorCMS cmsCap, final SABRInterestRateParameters sabrParameter, final double forward) {
      super(cmsCap, sabrParameter, forward);
      _nnp = nnp(forward);
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public Double evaluate(final Double x) {
      final double[] kD = super.kpkpp(x);
      // Implementation note: kD[0] contains the first derivative of k; kD[1] the second derivative of k.
      final double[] bs = bsbsp(x);
      return (kD[1] * (x - super._strike) + 2.0 * kD[0]) * (_nnp[1] * bs[0] + _nnp[0] * bs[1]);
    }

    private double[] nnp(final double x) {
      final double[] result = new double[2];
      final double[] ggp = ggp(x);
      final double[] hhp = hhp(x);
      result[0] = ggp[0] / hhp[0];
      result[1] = ggp[1] / hhp[0] - ggp[0] * hhp[1] / (hhp[0] * hhp[0]);
      return result;
    }

    @SuppressWarnings("synthetic-access")
    private double[] ggp(final double x) {
      final double[] result = new double[2];
      if (x >= getEps()) {
        final double periodFactor = 1 + x / super._nbFixedPaymentYear;
        final double nPeriodDiscount = Math.pow(periodFactor, -super._nbFixedPeriod);
        result[0] = 1.0 / x * (1.0 - nPeriodDiscount);
        result[1] = -result[0] / x + super._tau * super._nbFixedPeriod / x * nPeriodDiscount / periodFactor;
      } else {
        result[0] = super._nbFixedPeriod * super._tau;
        result[1] = -super._nbFixedPeriod * (super._nbFixedPeriod + 1.0) * super._tau * super._tau / 2.0;
      }
      return result;
    }

    @SuppressWarnings("synthetic-access")
    private double[] hhp(final double x) {
      final double[] result = new double[2];
      result[0] = Math.pow(1.0 + super._tau * x, super._eta);
      result[1] = super._eta * super._tau * result[0] / (1 + x * super._tau);
      return result;
    }

    /**
     * The Black-Scholes formula and its derivative with respect to the forward.
     * @param strike The strike.
     * @return The Black-Scholes formula and its derivative.
     */
    @SuppressWarnings("synthetic-access")
    double[] bsbsp(final double strike) {
      final double[] result = new double[2];
      final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, super._timeToExpiry, super._isCall);
      final SABRHaganVolatilityFunction sabrHaganFunction = (SABRHaganVolatilityFunction) super._sabrFunction;
      final double[] volatility = sabrHaganFunction.getVolatilityAdjoint(option, super._forward, super._sabrData);
      final BlackFunctionData dataBlack = new BlackFunctionData(super._forward, 1.0, volatility[0]);
      final double[] bsAdjoint = super._blackFunction.getPriceAdjoint(option, dataBlack);
      result[0] = bsAdjoint[0];
      result[1] = bsAdjoint[1] + bsAdjoint[2] * volatility[1];
      return result;
    }

  }

  private class CMSVegaIntegrant extends CMSIntegrant {

    private int _parameterIndex;

    /**
     * @param cmsCap
     * @param sabrParameter
     * @param forward
     */
    public CMSVegaIntegrant(final CapFloorCMS cmsCap, final SABRInterestRateParameters sabrParameter, final double forward) {
      super(cmsCap, sabrParameter, forward);
    }

    /**
     * Sets the _parameterIndex field.
     * @param _parameterIndex  the _parameterIndex
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
      final SABRHaganVolatilityFunction sabrHaganFunction = (SABRHaganVolatilityFunction) super._sabrFunction;
      final double[] volatilityAdjoint = sabrHaganFunction.getVolatilityAdjoint(option, super._forward, super._sabrData);
      return super._factor * (kD[1] * (x - super._strike) + 2.0 * kD[0]) * bs(x) * volatilityAdjoint[3 + _parameterIndex];
    }

    /**
     * The derivative with respect to the volatility of the Black-Scholes formula with numeraire 1.
     * @param strike The strike.
     * @return The Black-Scholes formula derivative with respect to volatility.
     */
    @SuppressWarnings("synthetic-access")
    @Override
    double bs(final double strike) {
      final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, super._timeToExpiry, super._isCall);
      final Function1D<SABRFormulaData, Double> funcSabr = super._sabrFunction.getVolatilityFunction(option, super._forward);
      final double volatility = funcSabr.evaluate(super._sabrData);
      final BlackFunctionData dataBlack = new BlackFunctionData(super._forward, 1.0, volatility);
      final double[] bsAdjoint = super._blackFunction.getPriceAdjoint(option, dataBlack);
      return bsAdjoint[2];
    }

  }

  private class CMSStrikeIntegrant extends CMSIntegrant {

    /**
     * @param cmsCap
     * @param sabrParameter
     * @param forward
     */
    public CMSStrikeIntegrant(final CapFloorCMS cmsCap, final SABRInterestRateParameters sabrParameter, final double forward) {
      super(cmsCap, sabrParameter, forward);
    }

    @Override
    public Double evaluate(final Double x) {
      final double[] kD = super.kpkpp(x);
      // Implementation note: kD[0] contains the first derivative of k; kD[1] the second derivative of k.
      return -kD[1] * bs(x);
    }

  } // End CMSStrikeIntegrant

}
