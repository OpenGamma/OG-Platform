/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata.scenarios;

import java.util.Objects;

import com.opengamma.util.ArgumentChecker;

/**
 * Contains the name of the individual curve that was matched when a market data filter matches a multicurve bundle.
 * <p>
 * Market data filters operate at the level of the market data ID, which for curves means they operate on curve
 * bundles. However the scenario framework handles individual curves as that is what users want to deal with.
 * This class bridges the gap between those two models so a filter that matches a bundle can communicate to
 * the scenario framework which curve was matched.
 */
public class MulticurveMatchDetails implements MatchDetails {

  private final String _curveName;

  /**
   * @param curveName the name of the curve that matched the filter
   */
  public MulticurveMatchDetails(String curveName) {
    _curveName = ArgumentChecker.notEmpty(curveName, "curveName");
  }

  /**
   * @return the name of the curve that matched the filter
   */
  public String getCurveName() {
    return _curveName;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_curveName);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    MulticurveMatchDetails other = (MulticurveMatchDetails) obj;
    return Objects.equals(this._curveName, other._curveName);
  }

  @Override
  public String toString() {
    return "MulticurveMatchDetails [_curveName='" + _curveName + "']";
  }
}
