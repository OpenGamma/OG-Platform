/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.engine.view.permission.PortfolioPermission;

/**
 * Responsible for checking whether a portfolio node is accessible or not.
 */
public interface NodeChecker {

  /**
   * Check if this node is accessible or not. Method is not responsible for
   * checking child/parent nodes which is done elsewhere.
   *
   * @param node the node to check, not null
   * @return an indication of the permission (ALLOW or DENY), not null
   */
  PortfolioPermission check(PortfolioNode node);
}
