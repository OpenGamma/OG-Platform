/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.loader;

import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.SecurityLoader;
import com.opengamma.master.security.SecurityMaster;

/**
 * Represents the context required for populating the platform with data.
 * <p>
 * This bean is primarily intended for use from Spring.
 * It holds that various objects in an easy to access manner.
 */
public class LoaderContext {

  /**
   * The portfolio master.
   */
  private PortfolioMaster _portfolioMaster;
  /**
   * The position master.
   */
  private PositionMaster _positionMaster;
  /**
   * The security master.
   */
  private SecurityMaster _securityMaster;
  /**
   * The security loader.
   */
  private SecurityLoader _securityLoader;

  //-------------------------------------------------------------------------
  /**
   * Gets the portfolio master.
   * @return the master, not null
   */
  public PortfolioMaster getPortfolioMaster() {
    return _portfolioMaster;
  }

  /**
   * Sets the portfolio master.
   * @param portfolioMaster  the master, not null
   */
  public void setPortfolioMaster(final PortfolioMaster portfolioMaster) {
    _portfolioMaster = portfolioMaster;
  }

  /**
   * Gets the position master.
   * @return the master, not null
   */
  public PositionMaster getPositionMaster() {
    return _positionMaster;
  }

  /**
   * Sets the position master.
   * @param positionMaster  the master, not null
   */
  public void setPositionMaster(final PositionMaster positionMaster) {
    _positionMaster = positionMaster;
  }

  /**
   * Gets the security master.
   * @return the master, not null
   */
  public SecurityMaster getSecurityMaster() {
    return _securityMaster;
  }

  /**
   * Sets the security master.
   * @param securityMaster  the master, not null
   */
  public void setSecurityMaster(final SecurityMaster securityMaster) {
    _securityMaster = securityMaster;
  }

  /**
   * Gets the security loader.
   * @return the loader, not null
   */
  public SecurityLoader getSecurityLoader() {
    return _securityLoader;
  }

  /**
   * Sets the security loader.
   * @param securityLoader  the loader, not null
   */
  public void setSecurityLoader(final SecurityLoader securityLoader) {
    _securityLoader = securityLoader;
  }

}
