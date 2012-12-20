/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.helper;

import java.util.Set;

import com.opengamma.engine.value.ValueProperties;

/**
 * Identifies and describes a possible or available output from an input set (e.g. a portfolio or view definition).
 */
public interface AvailableOutput {

  /**
   * Returns the name of the output value, e.g. {@code Present Value}.
   * 
   * @return the value name, not null
   */
  String getValueName();

  /**
   * Tests if the output is available on a position.
   * 
   * @return true if available
   */
  boolean isAvailableOnPosition();

  /**
   * Returns the security types this output is available on.
   * 
   * @return the security types
   */
  Set<String> getSecurityTypes();

  /**
   * Tests if the output is available on a position in the given security type.
   * 
   * @param securityType  the security type to test for
   * @return true if available
   */
  boolean isAvailableOn(final String securityType);

  /**
   * Tests if the output is available on a portfolio node.
   * 
   * @return true if available
   */
  boolean isAvailableOnPortfolioNode();

  /**
   * Returns the maximal property set on all the output is available on.
   * 
   * @return the property set, not null
   */
  ValueProperties getProperties();

  /**
   * Returns the maximal property set on positions in the given security type.
   * 
   * @param securityType security type to consider
   * @return the property set, not null
   */
  ValueProperties getPositionProperties(String securityType);

  /**
   * Returns the maximal property set on portfolio nodes.
   * 
   * @return the property set, not null
   */
  ValueProperties getPortfolioNodeProperties();

}
