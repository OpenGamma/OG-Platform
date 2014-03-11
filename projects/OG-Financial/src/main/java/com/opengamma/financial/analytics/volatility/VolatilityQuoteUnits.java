/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.joda.convert.FromStringFactory;

import com.opengamma.financial.convention.NamedInstance;
import com.opengamma.util.ArgumentChecker;

/**
 * Instance representing volatility quote units.
 */
@FromStringFactory(factory = VolatilityQuoteUnitsFactory.class)
public class VolatilityQuoteUnits implements NamedInstance, Serializable {

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * Lognormal quotes.
   */
  public static final VolatilityQuoteUnits LOGNORMAL = new VolatilityQuoteUnits("Lognormal");

  /**
   * Normal quotes.
   */
  public static final VolatilityQuoteUnits NORMAL = new VolatilityQuoteUnits("Normal");

  /**
   * Rates quotes i.e. 0.05 is expressed as 5.
   */
  public static final VolatilityQuoteUnits RATES = new VolatilityQuoteUnits("Rates");

  /**
   * Decimal quotes i.e. 0.05 is expressed as 00.05.
   */
  public static final VolatilityQuoteUnits DECIMALS = new VolatilityQuoteUnits("Decimals");

  /**
   * Basis points quotes i.e. 0.05 is expressed as 500.
   */
  public static final VolatilityQuoteUnits BASIS_POINTS = new VolatilityQuoteUnits("Basis Points");

  /** The volatility quote units name */
  private final String _name;

  /**
   * @param name The name, not null
   */
  protected VolatilityQuoteUnits(final String name) {
    ArgumentChecker.notNull(name, "name");
    _name = name;
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
