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
 * Instance representing cube quote types.
 */
@FromStringFactory(factory = CubeQuoteTypeFactory.class)
public class CubeQuoteType implements NamedInstance, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * A cube with (swaption expiry, swap maturity, relative strike <b>in bp</b>) axes.
   */
  public static final CubeQuoteType EXPIRY_MATURITY_RELATIVE_STRIKE = new CubeQuoteType("ExpiryMaturityRelativeStrike");
  /**
   * A cube with (swaption expiry, swap maturity, moneyness <b>as a decimal</b>) axes.
   */
  public static final CubeQuoteType EXPIRY_MATURITY_MONEYNESS = new CubeQuoteType("ExpiryMaturityMoneyness");

  /** The quote type name */
  private final String _name;

  /**
   * @param name The name, not null
   */
  protected CubeQuoteType(final String name) {
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
