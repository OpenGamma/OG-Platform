/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.helper;

import java.util.Set;

/**
 * Identifies and describes the possible or available outputs from an input set (e.g. a portfolio or view definition).
 */
public interface AvailableOutputs {

  /**
   * Returns the set of security types defined within the set. Each of these may be used as a valid parameter to {@link #getPositionOutputs}.
   * 
   * @return the set of security types
   */
  Set<String> getSecurityTypes();

  /**
   * Returns a set of outputs that can be asked of positions on the given security type.
   * 
   * @param securityType security type
   * @return set of outputs, not null
   */
  Set<AvailableOutput> getPositionOutputs(String securityType);

  /**
   * Returns a set of outputs that can be asked of portfolio nodes.
   * 
   * @return set of outputs, not null
   */
  Set<AvailableOutput> getPortfolioNodeOutputs();

  /**
   * Returns a set of outputs that can be asked of position nodes.
   * 
   * @return set of outputs, not null
   */
  Set<AvailableOutput> getPositionOutputs();

  /**
   * Returns a set of outputs that can be asked of portfolio or position nodes.
   * 
   * @return set of outputs, not null
   */
  Set<AvailableOutput> getOutputs();

}
