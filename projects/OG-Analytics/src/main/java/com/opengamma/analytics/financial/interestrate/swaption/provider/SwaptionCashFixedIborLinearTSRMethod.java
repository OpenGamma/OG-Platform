/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.provider;

import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.swap.provider.SwapFixedCouponDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProviderInterface;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Method to compute the present value of cash-settled European swaptions with with the Linear Terminal Swap Rate method. 
 * The physical swaptions are priced with SABR.
 */
public class SwaptionCashFixedIborLinearTSRMethod {

  /**
   * The par rate calculator.
   */
  private static final ParRateDiscountingCalculator PRDC = ParRateDiscountingCalculator.getInstance();
  /**
   * Minimal number of integration steps in the replication.
   */
  private final int _nbIteration = 6;
  /**
   * Range of the integral. Used only for caps. Represent the approximation of infinity in the strike dimension.
   * The range is [strike, strike+integrationInterval].
   */
  private final double _integrationInterval = 1.0;

  /**
   * The swap method.
   */
  private static final SwapFixedCouponDiscountingMethod METHOD_SWAP = SwapFixedCouponDiscountingMethod.getInstance();

  /**
   * Computes the present value of a cash-settled European swaption in the linear TSR method.
   * @param swaption The swaption.
   * @param sabrData The SABR data (used for physical swaptions).
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final SwaptionCashFixedIbor swaption, final SABRSwaptionProviderInterface sabrData) {
    ArgumentChecker.notNull(swaption, "Swaption");
    ArgumentChecker.notNull(sabrData, "SABR swaption provider");
    MulticurveProviderInterface multicurves = sabrData.getMulticurveProvider();
    Currency ccy = swaption.getCurrency();
    final AnnuityCouponFixed annuityFixed = swaption.getUnderlyingSwap().getFixedLeg();
    final double nominal = Math.abs(annuityFixed.getNthPayment(0).getNotional());
    final double discountFactorSettle = multicurves.getDiscountFactor(ccy, swaption.getSettlementTime());
    final double annuityPhysical = METHOD_SWAP.presentValueBasisPoint(swaption.getUnderlyingSwap(), multicurves) / nominal;
    final double strike = swaption.getStrike();
    final double forward = swaption.getUnderlyingSwap().accept(PRDC, multicurves);
    // Linear approximation
    final double[] alpha = new double[2];
    for (int loopcpn = 0; loopcpn < annuityFixed.getNumberOfPayments(); loopcpn++) {
      alpha[1] += annuityFixed.getNthPayment(loopcpn).getPaymentYearFraction();
    }
    alpha[1] = 1 / alpha[1];
    alpha[0] = (discountFactorSettle / annuityPhysical - alpha[1]) / forward;

    final LinearTSRIntegrant integrant = new LinearTSRIntegrant(swaption, sabrData.getSABRParameter(), forward, alpha);

    @SuppressWarnings("synthetic-access")
    final double strikePart = integrant.k(strike) * integrant.bs(strike);
    final double absoluteTolerance = 1.0E-2;
    final double relativeTolerance = 1.0E-5;
    final RungeKuttaIntegrator1D integrator = new RungeKuttaIntegrator1D(absoluteTolerance, relativeTolerance, _nbIteration);
    double integralPart;
    try {
      if (swaption.isCall()) {
        integralPart = integrator.integrate(integrant, strike, strike + _integrationInterval);
      } else {
        integralPart = integrator.integrate(integrant, 0.0, strike);
      }
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
    final double pv = nominal * annuityPhysical * (strikePart + integralPart) * (swaption.isLong() ? 1.0 : -1.0);
    return MultipleCurrencyAmount.of(swaption.getCurrency(), pv);
  }

  /**
   * Inner class to implement the integration used in price replication.
   */
  private static final class LinearTSRIntegrant extends Function1D<Double, Double> {

    private static final double EPS = 1E-10;

    private final double[] _linear;
    private final int _nbFixedPeriod;
    private final int _nbFixedPaymentYear;
    private final double _strike;
    private final double _forward;
    private final double _timeToExpiry;
    private final double _maturity;
    private final boolean _isCall;
    private final SABRFormulaData _sabrData;
    private final VolatilityFunctionProvider<SABRFormulaData> _sabrFunction;

    private final BlackPriceFunction _blackFunction = new BlackPriceFunction();

    /**
     * Constructor with the required data.
     * @param baseMethod The base method for the pricing of standard cap/floors.
     * @param capStandard The standard cap/floor used for replication.
     * @param sabrData The SABR data bundle used in the standard cap/floor pricing.
     */
    public LinearTSRIntegrant(final SwaptionCashFixedIbor swaption, final SABRInterestRateParameters sabrParameter, final double forward, final double[] linear) {
      _forward = forward;
      _nbFixedPeriod = swaption.getUnderlyingSwap().getFixedLeg().getPayments().length;
      _nbFixedPaymentYear = (int) Math.round(1.0 / swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(0).getPaymentYearFraction());
      _timeToExpiry = swaption.getTimeToExpiry();
      final AnnuityCouponFixed annuityFixed = swaption.getUnderlyingSwap().getFixedLeg();
      _maturity = annuityFixed.getNthPayment(annuityFixed.getNumberOfPayments() - 1).getPaymentTime() - swaption.getSettlementTime();
      final DoublesPair expiryMaturity = DoublesPair.of(_timeToExpiry, _maturity);
      final double alpha = sabrParameter.getAlpha(expiryMaturity);
      final double beta = sabrParameter.getBeta(expiryMaturity);
      final double rho = sabrParameter.getRho(expiryMaturity);
      final double nu = sabrParameter.getNu(expiryMaturity);
      _sabrData = new SABRFormulaData(alpha, beta, rho, nu);
      _sabrFunction = sabrParameter.getSabrFunction();
      _isCall = swaption.isCall();
      _strike = swaption.getStrike();
      _linear = linear;
    }

    @Override
    public Double evaluate(final Double x) {
      final double[] kD = kpkpp(x);
      // Implementation note: kD[0] contains the first derivative of k; kD[1] the second derivative of k. 
      return (kD[1] * (x - _strike) + 2.0 * kD[0]) * bs(x);
    }

    /**
     * The factor used in the strike part and in the integration of the replication.
     * @param x The swap rate. 
     * @return The factor.
     */
    private double k(final double x) {
      double g;
      final double linear = _linear[0] * x + _linear[1];
      if (x >= EPS) {
        final double periodFactor = 1 + x / _nbFixedPaymentYear;
        final double nPeriodDiscount = Math.pow(periodFactor, -_nbFixedPeriod);
        g = 1.0 / x * (1.0 - nPeriodDiscount);
      } else {
        g = ((double) _nbFixedPeriod) / _nbFixedPaymentYear;
      }
      return linear * g;
    }

    /**
     * The first and second derivative of the function k.
     * @param x The swap rate.
     * @return The derivative (first element is the first derivative, second element is second derivative).
     */
    private double[] kpkpp(final double x) {
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
      final double kp = _linear[0] * g + (_linear[0] * x + _linear[1]) * gp;
      final double kpp = 2 * _linear[0] * gp + (_linear[0] * x + _linear[1]) * gpp;
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

  }

}
