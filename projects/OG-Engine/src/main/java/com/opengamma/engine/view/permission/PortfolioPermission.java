/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.permission;

/**
* Enum representing a user's access to a portfolio or node within a portfolio.
*/
public enum PortfolioPermission {
  /**
   * User is allowed access to portfolio node and all child nodes.
   */
  ALLOW,

  /**
   * User is allowed access to the portfolio node and some of the child nodes.
   */
  PARTIAL,

  /**
   * User is not allowed access to portfolio node or any child nodes.
   */
  DENY
}
