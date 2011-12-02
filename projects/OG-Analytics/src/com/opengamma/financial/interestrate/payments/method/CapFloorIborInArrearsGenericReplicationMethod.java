/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments.method;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.method.PricingMethod;
import com.opengamma.financial.interestrate.payments.CapFloorIbor;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.util.money.CurrencyAmount;

/**
 *  Class used to compute the price and sensitivity of a Ibor cap/floor in arrears. 
 *  The cap/floor are supposed to be exactly in arrears. The payment date is ignored and the start fixing period date is used instead.
 */
//TODO: Add a reference to Libor-with-delay pricing method when available.
public class CapFloorIborInArrearsGenericReplicationMethod implements PricingMethod {

  /**
   * Base method for the pricing of standard cap/floors.
   */
  private final PricingMethod _baseMethod;
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
  public CapFloorIborInArrearsGenericReplicationMethod(final PricingMethod baseMethod) {
    super();
    this._baseMethod = baseMethod;
  }

  /**
   * Computes the present value of an Ibor cap/floor in arrears by replication.
   * @param cap The cap/floor.
   * @param sabrData The SABR data.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final CapFloorIbor cap, final SABRInterestRateDataBundle sabrData) {
    Validate.notNull(cap);
    Validate.notNull(sabrData);
    final CapFloorIbor capStandard = new CapFloorIbor(cap.getCurrency(), cap.getFixingPeriodEndTime(), cap.getFundingCurveName(), cap.getPaymentYearFraction(), cap.getNotional(), cap.getFixingTime(),
        cap.getIndex(), cap.getFixingPeriodStartTime(), cap.getFixingPeriodEndTime(), cap.getFixingYearFraction(), cap.getForwardCurveName(), cap.getStrike(), cap.isCap());
    final double beta = sabrData.getCurve(cap.getForwardCurveName()).getDiscountFactor(cap.getFixingPeriodStartTime())
        / sabrData.getCurve(cap.getForwardCurveName()).getDiscountFactor(cap.getFixingPeriodEndTime()) * sabrData.getCurve(cap.getFundingCurveName()).getDiscountFactor(cap.getFixingPeriodEndTime())
        / sabrData.getCurve(cap.getFundingCurveName()).getDiscountFactor(cap.getFixingPeriodStartTime());
    final double strikePart = (1.0 + cap.getFixingYearFraction() * cap.getStrike()) * _baseMethod.presentValue(capStandard, sabrData).getAmount();
    final double absoluteTolerance = 1.0;
    final double relativeTolerance = 1E-10;
    final RungeKuttaIntegrator1D integrator = new RungeKuttaIntegrator1D(absoluteTolerance, relativeTolerance, _nbIteration);
    final InArrearsIntegrant integrant = new InArrearsIntegrant(_baseMethod, capStandard, sabrData);
    double integralPart;
    try {
      if (cap.isCap()) {
        integralPart = integrator.integrate(integrant, cap.getStrike(), cap.getStrike() + _integrationInterval);
      } else {
        integralPart = integrator.integrate(integrant, 0.0, cap.getStrike());
      }
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
    integralPart *= 2.0 * cap.getFixingYearFraction();
    final double pv = (strikePart + integralPart) / beta;
    return CurrencyAmount.of(cap.getCurrency(), pv);
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof CapFloorIbor, "Cap/Floor on Ibor");
    Validate.isTrue(curves instanceof SABRInterestRateDataBundle, "SABR interest rate data bundle required");
    return presentValue((CapFloorIbor) instrument, (SABRInterestRateDataBundle) curves);
  }

  /**
   * Inner class to implement the integration used in price replication.
   */
  private class InArrearsIntegrant extends Function1D<Double, Double> {

    /**
     * The base method for the pricing of standard cap/floors.
     */
    private final PricingMethod _basePricingMethod;
    /**
     * The standard cap/floor used for replication.
     */
    private final CapFloorIbor _capStandard;
    /**
     * The SABR data bundle used in the standard cap/floor pricing.
     */
    private final SABRInterestRateDataBundle _sabrData;

    /**
     * Constructor with the required data.
     * @param baseMethod The base method for the pricing of standard cap/floors.
     * @param capStandard The standard cap/floor used for replication.
     * @param sabrData The SABR data bundle used in the standard cap/floor pricing.
     */
    public InArrearsIntegrant(final PricingMethod baseMethod, final CapFloorIbor capStandard, final SABRInterestRateDataBundle sabrData) {
      this._basePricingMethod = baseMethod;
      this._capStandard = capStandard;
      this._sabrData = sabrData;
    }

    @Override
    public Double evaluate(final Double x) {
      final CapFloorIbor capStrike = _capStandard.withStrike(x);
      return _basePricingMethod.presentValue(capStrike, _sabrData).getAmount();
    }
  }

}
