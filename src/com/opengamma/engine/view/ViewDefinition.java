/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.msg.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

/**
 * The encapsulated logic that controls how precisely a view is to be constructed
 * and computed.
 */
public class ViewDefinition implements Serializable {
  private final String _name;
  private final UniqueIdentifier _portfolioId;
  private final UserPrincipal _liveDataUser;
  private Long _minimumRecalculationPeriod;
  private boolean _computePortfolioNodeCalculations = true;
  private boolean _computePositionNodeCalculations = true;
  private boolean _computeSecurityNodeCalculations /*= false*/;
  private boolean _computePrimitiveNodeCalculations /*= false*/;
  private final Map<String, ViewCalculationConfiguration> _calculationConfigurationsByName =
    new TreeMap<String, ViewCalculationConfiguration>();
  
  public ViewDefinition(String name, UniqueIdentifier portfolioId, String userName) {
    ArgumentChecker.notNull(name, "View name");
    ArgumentChecker.notNull(portfolioId, "Portfolio id");
    ArgumentChecker.notNull(userName, "User name");
    
    _name = name;
    _portfolioId = portfolioId;
    _liveDataUser = UserPrincipal.getLocalUser(userName);
  }
  
  public ViewDefinition(String name, UniqueIdentifier portfolioId, UserPrincipal liveDataUser) {
    ArgumentChecker.notNull(name, "View name");
    ArgumentChecker.notNull(portfolioId, "Portfolio id");
    ArgumentChecker.notNull(liveDataUser, "User name");
    
    _name = name;
    _portfolioId = portfolioId;
    _liveDataUser = liveDataUser;
  }
  
  public Set<String> getAllValueRequirements() {
    Set<String> requirements = new TreeSet<String>();
    for (ViewCalculationConfiguration calcConfig : _calculationConfigurationsByName.values()) {
      requirements.addAll(calcConfig.getAllValueRequirements());
    }
    return requirements;
  }

  public String getName() {
    return _name;
  }

  public UniqueIdentifier getPortfolioId() {
    return _portfolioId;
  }
  
  /**
   * @return The LiveData user should be used to create 
   * LiveData subscriptions. It is thus a kind of 'super-user'
   * and ensures that the View can be materialized even without
   * any end user trying to use it.
   * <p>
   * Authenticating the end users of the View (of which there can be many) 
   * is a separate matter entirely and has nothing to do with this user.  
   */
  public UserPrincipal getLiveDataUser() {
    return _liveDataUser;
  }
  
  public Collection<ViewCalculationConfiguration> getAllCalculationConfigurations() {
    return new ArrayList<ViewCalculationConfiguration>(_calculationConfigurationsByName.values());
  }
  
  public Set<String> getAllCalculationConfigurationNames() {
    return Collections.unmodifiableSet(_calculationConfigurationsByName.keySet());
  }
  
  public Map<String, ViewCalculationConfiguration> getAllCalculationConfigurationsByName() {
    return Collections.unmodifiableMap(_calculationConfigurationsByName);
  }
  
  public ViewCalculationConfiguration getCalculationConfiguration(String configurationName) {
    return _calculationConfigurationsByName.get(configurationName);
  }
  
  public void addViewCalculationConfiguration(ViewCalculationConfiguration calcConfig) {
    ArgumentChecker.notNull(calcConfig, "calculation configuration");
    ArgumentChecker.notNull(calcConfig.getName(), "Configuration name");
    _calculationConfigurationsByName.put(calcConfig.getName(), calcConfig);
  }
  
  public void addValueDefinition(String calculationConfigurationName, String securityType, String requirementName) {
    ViewCalculationConfiguration calcConfig = _calculationConfigurationsByName.get(calculationConfigurationName);
    if (calcConfig == null) {
      calcConfig = new ViewCalculationConfiguration(this, calculationConfigurationName);
      _calculationConfigurationsByName.put(calculationConfigurationName, calcConfig);
    }
    calcConfig.addValueRequirement(securityType, requirementName);
  }

  /**
   * @return the minimumRecalculationPeriod
   */
  public Long getMinimumRecalculationPeriod() {
    return _minimumRecalculationPeriod;
  }

  /**
   * @param minimumRecalculationPeriod the minimumRecalculationPeriod to set
   */
  public void setMinimumRecalculationPeriod(Long minimumRecalculationPeriod) {
    _minimumRecalculationPeriod = minimumRecalculationPeriod;
  }

  /**
   * @return the computePortfolioNodeCalculations
   */
  public boolean isComputePortfolioNodeCalculations() {
    return _computePortfolioNodeCalculations;
  }

  /**
   * @param computePortfolioNodeCalculations the computePortfolioNodeCalculations to set
   */
  public void setComputePortfolioNodeCalculations(boolean computePortfolioNodeCalculations) {
    _computePortfolioNodeCalculations = computePortfolioNodeCalculations;
  }

  /**
   * @return the computePositionNodeCalculations
   */
  public boolean isComputePositionNodeCalculations() {
    return _computePositionNodeCalculations;
  }

  /**
   * @param computePositionNodeCalculations the computePositionNodeCalculations to set
   */
  public void setComputePositionNodeCalculations(boolean computePositionNodeCalculations) {
    _computePositionNodeCalculations = computePositionNodeCalculations;
  }

  /**
   * @return the computeSecurityNodeCalculations
   */
  public boolean isComputeSecurityNodeCalculations() {
    return _computeSecurityNodeCalculations;
  }

  /**
   * @param computeSecurityNodeCalculations the computeSecurityNodeCalculations to set
   */
  public void setComputeSecurityNodeCalculations(boolean computeSecurityNodeCalculations) {
    _computeSecurityNodeCalculations = computeSecurityNodeCalculations;
  }

  /**
   * @return the computePrimitiveNodeCalculations
   */
  public boolean isComputePrimitiveNodeCalculations() {
    return _computePrimitiveNodeCalculations;
  }

  /**
   * @param computePrimitiveNodeCalculations the computePrimitiveNodeCalculations to set
   */
  public void setComputePrimitiveNodeCalculations(boolean computePrimitiveNodeCalculations) {
    _computePrimitiveNodeCalculations = computePrimitiveNodeCalculations;
  }

}
