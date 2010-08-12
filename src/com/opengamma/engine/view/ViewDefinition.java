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

import org.apache.commons.lang.ObjectUtils;

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
  
  /** 
   * A delta recomputation of the view should be performed at this interval.
   * Milliseconds.
   * 0 = can be performed as often as there is CPU resources for.
   * Null = delta recomputation only needs to be performed if underlying
   * market data changes.  
   */
  private Long _deltaRecalculationPeriod;
  
  /** 
   * A full recomputation of the view should be performed at this interval 
   * (i.e., no delta vs. previous result should be used).
   * Milliseconds.
   * 0 = each computation should be a full recomputation.
   * Null = no full recomputation needs to be performed - previous result can always be used
   */ 
  private Long _fullRecalculationPeriod;
  
  private final Map<String, ViewCalculationConfiguration> _calculationConfigurationsByName =
    new TreeMap<String, ViewCalculationConfiguration>();
  
  /**
   * Constructs an instance.
   * 
   * @param name  the name of the view definition
   * @param portfolioId  the unique identifier of the reference portfolio for this view definition
   * @param userName  the name of the user who owns the view definition
   */
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
  
  /**
   * Gets a set containing every portfolio output that is required, across all calculation configurations, regardless
   * of the security type(s) on which the output is required. These are outputs produced at the position and aggregate
   * position level, with respect to the reference portfolio. 
   * 
   * @return  a set of every required portfolio output across all calculation configurations, not null
   */
  public Set<String> getAllPortfolioRequirements() {
    Set<String> requirements = new TreeSet<String>();
    for (ViewCalculationConfiguration calcConfig : _calculationConfigurationsByName.values()) {
      requirements.addAll(calcConfig.getAllPortfolioRequirements());
    }
    return requirements;
  }

  public String getName() {
    return _name;
  }
  
  /**
   * Gets the unique identifier of the reference portfolio for this view. This is the portfolio on which position-level
   * calculations will be performed.
   * 
   * @return  the unique identifier of the reference portfolio, possibly null.
   */
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
  
  public void addPortfolioRequirement(String calculationConfigurationName, String securityType, String requirementName) {
    ViewCalculationConfiguration calcConfig = _calculationConfigurationsByName.get(calculationConfigurationName);
    if (calcConfig == null) {
      calcConfig = new ViewCalculationConfiguration(this, calculationConfigurationName);
      _calculationConfigurationsByName.put(calculationConfigurationName, calcConfig);
    }
    calcConfig.addPortfolioRequirement(securityType, requirementName);
  }

  /**
   * @return A delta recomputation of the view should be performed at this interval.
   * Milliseconds.
   * 0 = can be performed as often as there is CPU resources for.
   * Null = delta recomputation only needs to be performed if underlying
   * market data changes.
   */
  public Long getDeltaRecalculationPeriod() {
    return _deltaRecalculationPeriod;
  }

  /**
   * @param minimumRecalculationPeriod the minimumRecalculationPeriod to set, milliseconds
   */
  public void setDeltaRecalculationPeriod(Long minimumRecalculationPeriod) {
    _deltaRecalculationPeriod = minimumRecalculationPeriod;
  }

  /**
   * @return A full recomputation of the view should be performed at this interval 
   * (i.e., no delta vs. previous result should be used).
   * Milliseconds.
   * 0 = each computation should be a full recomputation.
   * Null = no full recomputation needs to be performed - previous result can always be used
   */
  public Long getFullRecalculationPeriod() {
    return _fullRecalculationPeriod;
  }

  /**
   * @param fullRecalculationPeriod the fullRecalculationPeriod to set, milliseconds
   */
  public void setFullRecalculationPeriod(Long fullRecalculationPeriod) {
    _fullRecalculationPeriod = fullRecalculationPeriod;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ObjectUtils.hashCode(getName());
    result = prime * result + ObjectUtils.hashCode(getPortfolioId());
    result = prime * result + ObjectUtils.hashCode(getLiveDataUser());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    
    if (!(obj instanceof ViewDefinition)) {
      return false;
    }
    
    ViewDefinition other = (ViewDefinition) obj;
    boolean basicPropertiesEqual = ObjectUtils.equals(getName(), other.getName()) 
      && ObjectUtils.equals(getPortfolioId(), other.getPortfolioId())
      && ObjectUtils.equals(getLiveDataUser(), other.getLiveDataUser())
      && ObjectUtils.equals(_deltaRecalculationPeriod, other._deltaRecalculationPeriod)
      && ObjectUtils.equals(_fullRecalculationPeriod, other._fullRecalculationPeriod)
      && ObjectUtils.equals(getAllCalculationConfigurationNames(), other.getAllCalculationConfigurationNames());
    if (!basicPropertiesEqual) {
      return false;
    }
    
    for (ViewCalculationConfiguration localCalcConfig : _calculationConfigurationsByName.values()) {
      ViewCalculationConfiguration otherCalcConfig = other.getCalculationConfiguration(localCalcConfig.getName());
      if (!localCalcConfig.equals(otherCalcConfig)) {
        return false;
      }
    }
    
    return true;
  }

}
