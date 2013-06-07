/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.parameters;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.math.curve.Curve;
import com.opengamma.analytics.math.surface.Surface;

/**
 * _atmVolatility
 */
public class InflationConvexityAdjustmentParameters {

  /**
   * The ATM volatility curve of the price index. The dimensions are the expiration. Not null.
   */
  private final Curve<Double, Double> _atmVolatility;

  /**
   * The correlation surface. The dimensions are the expiration. Not null.
   */
  private final Surface<Double, Double, Double> _priceIndexCorrelation;

  /**
   * The volatility surface. The dimensions are the expiration. Not null.
   */
  private final Curve<Double, Double> _priceIndexRateCorrelation;
  /**
   * The Ibor index for which the volatility is valid. Not null.
   */
  private final IndexPrice _index;

  /**
   * Constructor from the parameter surfaces. 
   * @param atmVolatility The atm Black volatility curve.
   * @param priceIndexCorrelation The Black volatility curve.
   * @param priceIndexRateCorrelation The Black volatility curve. 
   * @param index The Ibor index for which the volatility is valid.
   */
  public InflationConvexityAdjustmentParameters(final Curve<Double, Double> atmVolatility, final Surface<Double, Double, Double> priceIndexCorrelation,
      final Curve<Double, Double> priceIndexRateCorrelation, final IndexPrice index) {
    Validate.notNull(atmVolatility, "volatility curve");
    Validate.notNull(priceIndexCorrelation, "volatility curve");
    Validate.notNull(priceIndexRateCorrelation, "volatility curve");
    Validate.notNull(index, "index price");
    _atmVolatility = atmVolatility;
    _priceIndexCorrelation = priceIndexCorrelation;
    _priceIndexRateCorrelation = priceIndexRateCorrelation;
    _index = index;
  }

  /**
   * Return the volatility for a time to expiration and strike.
   * @return The atm volatility.
   */
  public Curve<Double, Double> getAtmVolatility() {
    return _atmVolatility;
  }

  /**
   * Return the volatility for a time to expiration and strike.
   * @return The volatility.
   */
  public Surface<Double, Double, Double> getPriceIndexCorrelation() {
    return _priceIndexCorrelation;
  }

  /**
   * Return the volatility for a time to expiration and strike.
   * @return The volatility.
   */
  public Curve<Double, Double> getPriceIndexRateCorrelation() {
    return _priceIndexRateCorrelation;
  }

  /**
   * Gets the Ibor index for which the volatility is valid.
   * @return The index.
   */
  public IndexPrice getIndex() {
    return _index;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_atmVolatility == null) ? 0 : _atmVolatility.hashCode());
    result = prime * result + ((_index == null) ? 0 : _index.hashCode());
    result = prime * result + ((_priceIndexCorrelation == null) ? 0 : _priceIndexCorrelation.hashCode());
    result = prime * result + ((_priceIndexRateCorrelation == null) ? 0 : _priceIndexRateCorrelation.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    InflationConvexityAdjustmentParameters other = (InflationConvexityAdjustmentParameters) obj;
    if (_atmVolatility == null) {
      if (other._atmVolatility != null) {
        return false;
      }
    } else if (!_atmVolatility.equals(other._atmVolatility)) {
      return false;
    }
    if (_index == null) {
      if (other._index != null) {
        return false;
      }
    } else if (!_index.equals(other._index)) {
      return false;
    }
    if (_priceIndexCorrelation == null) {
      if (other._priceIndexCorrelation != null) {
        return false;
      }
    } else if (!_priceIndexCorrelation.equals(other._priceIndexCorrelation)) {
      return false;
    }
    if (_priceIndexRateCorrelation == null) {
      if (other._priceIndexRateCorrelation != null) {
        return false;
      }
    } else if (!_priceIndexRateCorrelation.equals(other._priceIndexRateCorrelation)) {
      return false;
    }
    return true;
  }

}
