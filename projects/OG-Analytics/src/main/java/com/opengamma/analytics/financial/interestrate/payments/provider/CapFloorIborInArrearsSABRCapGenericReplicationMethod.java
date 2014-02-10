/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRCapProviderInterface;
import com.opengamma.analytics.financial.provider.method.CapFloorIborSABRCapMethodInterface;
import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 *  Class used to compute the price and sensitivity of a Ibor cap/floor in arrears.
 *  The cap/floor are supposed to be exactly in arrears. The payment date is ignored and the start fixing period date is used instead.
 */
//TODO: Add a reference to Libor-with-delay pricing method documentation  when available.
public class CapFloorIborInArrearsSABRCapGenericReplicationMethod {

  /**
   * Base method for the pricing of standard cap/floors.
   */
  private final CapFloorIborSABRCapMethodInterface _baseMethod;
  /**
   * Range of the integral. Used only for caps. Represent the approximation of infinity in the strike dimension.
   * The range is [strike, strike+integrationInterval].
   */
  private final double _integrationInterval = 2.0;
  /**
   * Minimal number of integration steps in the replication.
   */
  private final int _nbIteration = 6;

  /**
   * Constructor of the in-arrears pricing method.
   * @param baseMethod The base method for the pricing of standard cap/floors.
   */
  public CapFloorIborInArrearsSABRCapGenericReplicationMethod(final CapFloorIborSABRCapMethodInterface baseMethod) {
    this._baseMethod = baseMethod;
  }

  /**
   * Computes the present value of an Ibor cap/floor in arrears by replication.
   * @param cap The cap/floor.
   * @param sabr The SABR cap and multi-curves provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final CapFloorIbor cap, final SABRCapProviderInterface sabr) {
    ArgumentChecker.notNull(cap, "The cap/floor shoud not be null");
    ArgumentChecker.notNull(sabr, "SABR cap provider");
    final Currency ccy = cap.getCurrency();
    final MulticurveProviderInterface multicurves = sabr.getMulticurveProvider();
    final CapFloorIbor capStandard = new CapFloorIbor(cap.getCurrency(), cap.getFixingPeriodEndTime(), cap.getPaymentYearFraction(), cap.getNotional(), cap.getFixingTime(),
        cap.getIndex(), cap.getFixingPeriodStartTime(), cap.getFixingPeriodEndTime(), cap.getFixingAccrualFactor(), cap.getStrike(), cap.isCap());
    final double forward = multicurves.getSimplyCompoundForwardRate(cap.getIndex(), cap.getFixingPeriodStartTime(), cap.getFixingPeriodEndTime(), cap.getFixingAccrualFactor());
    final double beta = (1.0 + cap.getFixingAccrualFactor() * forward) * multicurves.getDiscountFactor(ccy, cap.getFixingPeriodEndTime())
        / multicurves.getDiscountFactor(ccy, cap.getFixingPeriodStartTime());
    final double strikePart = (1.0 + cap.getFixingAccrualFactor() * cap.getStrike()) * _baseMethod.presentValue(capStandard, sabr).getAmount(ccy);
    final double absoluteTolerance = 1.0;
    final double relativeTolerance = 1E-10;
    final RungeKuttaIntegrator1D integrator = new RungeKuttaIntegrator1D(absoluteTolerance, relativeTolerance, _nbIteration);
    final InArrearsIntegrant integrant = new InArrearsIntegrant(_baseMethod, capStandard, sabr);
    double integralPart;
    try {
      if (cap.isCap()) {
        integralPart = integrator.integrate(integrant, cap.getStrike(), cap.getStrike() + _integrationInterval);
      } else {
        integralPart = integrator.integrate(integrant, 0.0, cap.getStrike());
      }
    } catch (final Exception e) {
      throw new MathException(e);
    }
    integralPart *= 2.0 * cap.getFixingAccrualFactor();
    final double pv = (strikePart + integralPart) / beta;
    return MultipleCurrencyAmount.of(cap.getCurrency(), pv);
  }

  /**
   * Inner class to implement the integration used in price replication.
   */
  private static final class InArrearsIntegrant extends Function1D<Double, Double> {

    /**
     * The base method for the pricing of standard cap/floors.
     */
    private final CapFloorIborSABRCapMethodInterface _basePricingMethod;
    /**
     * The standard cap/floor used for replication.
     */
    private final CapFloorIbor _capStandard;
    /**
     * The SABR data bundle used in the standard cap/floor pricing.
     */
    private final SABRCapProviderInterface _sabrData;

    /**
     * Constructor with the required data.
     * @param baseMethod The base method for the pricing of standard cap/floors.
     * @param capStandard The standard cap/floor used for replication.
     * @param sabrData The SABR data bundle used in the standard cap/floor pricing.
     */
    public InArrearsIntegrant(final CapFloorIborSABRCapMethodInterface baseMethod, final CapFloorIbor capStandard, final SABRCapProviderInterface sabr) {
      _basePricingMethod = baseMethod;
      _capStandard = capStandard;
      _sabrData = sabr;
    }

    @Override
    public Double evaluate(final Double x) {
      final CapFloorIbor capStrike = _capStandard.withStrike(x);
      return _basePricingMethod.presentValue(capStrike, _sabrData).getAmount(_capStandard.getCurrency());
    }
  }

}
