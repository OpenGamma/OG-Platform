/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.user;

import org.threeten.bp.Instant;

import com.opengamma.master.config.ConfigMaster;
import com.opengamma.util.ArgumentChecker;

/**
 * A client opened by a user.
 */
public class FinancialClient {

  /**
   * The manager.
   */
  private final FinancialClientManager _manager;
  /**
   * The client name.
   */
  private final String _clientName;
  /**
   * The portfolio master.
   */
  private final FinancialUserPortfolioMaster _portfolioMaster;
  /**
   * The position master.
   */
  private final FinancialUserPositionMaster _positionMaster;
  /**
   * The security master.
   */
  private final FinancialUserSecurityMaster _securityMaster;
  /**
   * The yield curve master.
   */
  private final FinancialUserInterpolatedYieldCurveDefinitionMaster _interpolatedYieldCurveDefinitionMaster;
  /**
   * The snashot master.
   */
  private final FinancialUserSnapshotMaster _snapshotMaster;
  /**
   * Contains the timestamp of the last time something was requested.
   */
  private volatile Instant _lastAccessed;
  /**
   * The config source
   */
  private ConfigMaster _configMaster;

  /**
   * Creates an instance.
   * 
   * @param manager  the manager, not null
   * @param clientName  the client name, not null
   */
  public FinancialClient(FinancialClientManager manager, String clientName) {
    ArgumentChecker.notNull(manager, "manager");
    ArgumentChecker.notNull(clientName, "clientName");
    _lastAccessed = Instant.now();
    _manager = manager;
    _clientName = clientName;
    FinancialUserServices services = manager.getServices();
    _portfolioMaster = new FinancialUserPortfolioMaster(this, services.getPortfolioMaster());
    _positionMaster = new FinancialUserPositionMaster(this, services.getPositionMaster());
    _securityMaster = new FinancialUserSecurityMaster(this, services.getSecurityMaster());
    _configMaster = new FinancialUserConfigMaster(this, services.getConfigMaster());
    _interpolatedYieldCurveDefinitionMaster = new FinancialUserInterpolatedYieldCurveDefinitionMaster(this, services.getInterpolatedYieldCurveDefinitionMaster());
    _snapshotMaster = new FinancialUserSnapshotMaster(this, services.getSnapshotMaster());
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the instant the client was last accessed.
   * 
   * @return the access, not null
   */
  public Instant getLastAccessed() {
    return _lastAccessed;
  }

  /**
   * Updates the last accessed time.
   */
  public void updateLastAccessed() {
    _lastAccessed = Instant.now();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the user name.
   * 
   * @return the user name, not null
   */
  public String getUserName() {
    return _manager.getUserName();
  }

  /**
   * Gets the client name.
   * 
   * @return the client name, not null
   */
  public String getClientName() {
    return _clientName;
  }

  /**
   * Gets the tracker.
   * 
   * @return the tracker, not null
   */
  public FinancialUserDataTracker getUserDataTracker() {
    return _manager.getUserDataTracker();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the portfolio master.
   * 
   * @return the value of the property
   */
  public FinancialUserPortfolioMaster getPortfolioMaster() {
    updateLastAccessed();
    return _portfolioMaster;
  }

  /**
   * Gets the position master.
   * 
   * @return the value of the property
   */
  public FinancialUserPositionMaster getPositionMaster() {
    updateLastAccessed();
    return _positionMaster;
  }

  /**
   * Gets the security master.
   * 
   * @return the value of the property
   */
  public FinancialUserSecurityMaster getSecurityMaster() {
    updateLastAccessed();
    return _securityMaster;
  }

  /**
   * Gets the view definition master.
   * 
   * @return the value of the property
   */
  public ConfigMaster getConfigMaster() {
    updateLastAccessed();
    return _configMaster;
  }

  /**
   * Gets the yield curve master.
   * 
   * @return the value of the property
   */
  public FinancialUserInterpolatedYieldCurveDefinitionMaster getInterpolatedYieldCurveDefinitionMaster() {
    updateLastAccessed();
    return _interpolatedYieldCurveDefinitionMaster;
  }

  /**
   * Gets the snashot master.
   * 
   * @return the value of the property
   */
  public FinancialUserSnapshotMaster getSnapshotMaster() {
    updateLastAccessed();
    return _snapshotMaster;
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getUserName() + "/" + getClientName() + "]";
  }

}
