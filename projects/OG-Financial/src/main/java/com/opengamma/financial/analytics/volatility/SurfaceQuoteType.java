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
 * Instance representing surface quote types.
 */
@FromStringFactory(factory = CubeQuoteTypeFactory.class)
public class SurfaceQuoteType implements NamedInstance, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * A surface with (swaption expiry, swap maturity) axes.
   */
  public static final SurfaceQuoteType EXPIRY_MATURITY = new SurfaceQuoteType("ExpiryMaturity");

  /** The quote type name */
  private final String _name;

  /**
   * @param name The name, not null
   */
  protected SurfaceQuoteType(final String name) {
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
