/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.multicurvecommodity.curve;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.curve.FunctionalDoublesCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.util.serialization.InvokedSerializedForm;
import com.opengamma.util.ArgumentChecker;

/**
 * A curve containing the (estimated) commodity forward  value at different maturities.
 */
public class CommodityForwardCurve {

  /**
   * The commodity forward curve index curve.
   */
  private final DoublesCurve _fwdCurve;

  /**
   * A small amount of time
   */
  private static final double SMALL_TIME = 1.0E-6;

  /**
   * Constructor from a curve object.
   * @param fwdCurve The curve.
   */
  public CommodityForwardCurve(final DoublesCurve fwdCurve) {
    Validate.notNull(fwdCurve, "curve");
    _fwdCurve = fwdCurve;
  }

  /**
   * Constructor from the spot value, a discount curve and a convenience yield curve.
   * @param spot The spot.
   * @param discountCurve The discount curve.
   * @param convenienceYieldCurve The convenience yield curve.
   */
  public CommodityForwardCurve(final double spot, final YieldAndDiscountCurve discountCurve, final YieldAndDiscountCurve convenienceYieldCurve) {
    Validate.notNull(discountCurve, "curve");
    Validate.notNull(convenienceYieldCurve, "curve");
    _fwdCurve = getForwardCurve(spot, discountCurve, convenienceYieldCurve);
  }

  /**
   * Gets the underlying curve object.
   * @return The  forward curve.
   */
  public DoublesCurve getFwdCurve() {
    return _fwdCurve;
  }

  /**
   * Returns the curve name.
   * @return The name.
   */
  public String getName() {
    return _fwdCurve.getName();
  }

  /**
   * Returns the estimated commodity forward value for a given time.
   * @param time The time
   * @return The commodity forward value.
   */
  public double getForwardValue(final Double time) {
    return _fwdCurve.getYValue(time);
  }

  /**
   * Returns the estimated commodity forward value for a given time.
   * @return The commodity forward value.
   */
  public double getSpotValue() {
    return _fwdCurve.getYValue(0.0);
  }

  /**
   * Gets the number of parameters in a curve.
   * @return The number of parameters
   */
  public int getNumberOfParameters() {
    return _fwdCurve.size();
  }

  /**
   * The list of underlying curves (up to one level).
   * @return The list.
   */
  public List<String> getUnderlyingCurvesNames() {
    return new ArrayList<>();
  }

  /**
   * Gets the sensitivities of the commodity forward to the curve parameters for a time.
   * @param time The time
   * @return The sensitivities. If the time is less than 1e<sup>-6</sup>, the rate is
   * ill-defined and zero is returned.
   */
  public double[] getCommodityForwardParameterSensitivity(final double time) {
    final Double[] curveSensitivity = _fwdCurve.getYValueParameterSensitivity(time);
    final double[] commodityForwardSensitivity = new double[curveSensitivity.length];
    // Implementation note: if time = 0, the rate is ill-defined: return 0 sensitivity
    if (Math.abs(time) < SMALL_TIME) {
      return commodityForwardSensitivity;
    }
    for (int loopp = 0; loopp < curveSensitivity.length; loopp++) {
      commodityForwardSensitivity[loopp] = curveSensitivity[loopp];
    }
    return commodityForwardSensitivity;
  }

  protected static DoublesCurve getForwardCurve(final double spot, final YieldAndDiscountCurve discountCurve, final YieldAndDiscountCurve convenienceYieldCurve) {
    ArgumentChecker.notNull(discountCurve, "risk-free curve");
    ArgumentChecker.notNull(convenienceYieldCurve, "cost-of-carry curve");
    final Function1D<Double, Double> f = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double t) {
        return spot * convenienceYieldCurve.getDiscountFactor(t) / discountCurve.getDiscountFactor(t);
      }

    };
    return new FunctionalDoublesCurve(f) {
      public Object writeReplace() {
        return new InvokedSerializedForm(CommodityForwardCurve.class, "getForwardCurve", spot, discountCurve, convenienceYieldCurve);
      }
    };
  }

  protected static YieldAndDiscountCurve getConvenienceYieldCurve(final double spot, final YieldAndDiscountCurve discountCurve, final DoublesCurve fwdCurve) {
    ArgumentChecker.notNull(discountCurve, "discount curve");
    ArgumentChecker.notNull(fwdCurve, "convenience yield curve");
    final Function1D<Double, Double> f = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double t) {
        return -Math.log(fwdCurve.getYValue(t) / spot) / t - discountCurve.getInterestRate(t);
      }

    };
    return YieldCurve.from(new FunctionalDoublesCurve(f));
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_fwdCurve == null) ? 0 : _fwdCurve.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final CommodityForwardCurve other = (CommodityForwardCurve) obj;
    if (_fwdCurve == null) {
      if (other._fwdCurve != null) {
        return false;
      }
    } else if (!_fwdCurve.equals(other._fwdCurve)) {
      return false;
    }
    return true;
  }

}
