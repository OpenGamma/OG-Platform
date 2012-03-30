/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.method;

import java.util.Map;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.money.Currency;

/**
 * YieldCurvebundle with a map indicating in which currency is.
 * TODO: This class is used only because there is no indication of the currency in the curve. This class should disappear once the YieldCurveBundle is reviewed/improved.
 */
public class YieldCurveWithCcyBundle extends YieldCurveBundle {

  /**
   * A map linking each curve in the bundle to its currency.
   */
  private final Map<String, Currency> _curveCurrency;

  /**
   * Constructor.
   * @param curveCurrency A map linking each curve in the bundle to its currency.
   * @param bundle The yield curve bundle. A new bundle with a new map and the same elements is created.
   */
  public YieldCurveWithCcyBundle(final Map<String, Currency> curveCurrency, final YieldCurveBundle bundle) {
    super(bundle);
    // TODO: check that the map is complete (all curves).
    _curveCurrency = curveCurrency;
  }

  /**
   * Constructor.
   * @param bundle The yield curve bundle. A new bundle with a new map and the same elements is created; the same map currency/curves is used.
   */
  public YieldCurveWithCcyBundle(final YieldCurveWithCcyBundle bundle) {
    super(bundle);
    _curveCurrency = bundle._curveCurrency;
  }

  /**
   * Gets map linking each curve in the bundle to its currency.
   * @return The map.
   */
  public Map<String, Currency> getCcyMap() {
    return _curveCurrency;
  }

  /**
   * Return the currency associated to a given curve.
   * @param curveName The curve name.
   * @return The currency.
   */
  public Currency getCurveCurrency(final String curveName) {
    Currency ccy = _curveCurrency.get(curveName);
    if (ccy == null) {
      throw new IllegalArgumentException("Named yield curve not in map: " + curveName);
    }
    return ccy;
  }

}
