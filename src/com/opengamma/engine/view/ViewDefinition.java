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
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMessageFactory;
import org.fudgemsg.MutableFudgeFieldContainer;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.msg.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

/**
 * The encapsulated logic that controls how precisely a view is to be constructed
 * and computed.
 */
public class ViewDefinition implements Serializable {
  
  /**
   * Fudge message key for the name.
   */
  public static final String NAME_KEY = "name";
  /**
   * Fudge message key for the portfolioId.
   */
  public static final String PORTFOLIO_ID_KEY = "portfolioId";
  /**
   * Fudge message key for the liveDataUser.
   */
  public static final String USER_KEY = "user";
  /**
   * Fudge message key for the name.
   */
  public static final String COMPUTE_PORTFOLIO_NODE_CALCULATIONS_KEY = "computePortfolioNodeCalculations";
  /**
   * Fudge message key for the portfolioId.
   */
  public static final String COMPUTE_POSITION_NODE_CALCULATIONS_KEY = "computePositionNodeCalculations";
  /**
   * Fudge message key for the liveDataUser.
   */
  public static final String COMPUTE_SECURITY_NODE_CALCULATIONS_KEY = "computeSecurityNodeCalculations";
  /**
   * Fudge message key for the liveDataUser.
   */
  public static final String COMPUTE_PRIMITIVE_NODE_CALCULATIONS_KEY = "computePrimitiveNodeCalculations";
  /**
   * Fudge message key for the portfolioId.
   */
  public static final String DELTA_RECALCULATION_PERIOD_KEY = "deltaRecalculationPeriod";
  /**
   * Fudge message key for the liveDataUser.
   */
  public static final String FULL_RECALCULATION_PERIOD_KEY = "fullRecalculationPeriod";
  /**
   * Fudge message key for the liveDataUser.
   */
  public static final String CALCULATION_CONFIGURATIONS_BY_NAME_KEY = "calculationConfigurationsByName";
  
  
  
  private final String _name;
  private final UniqueIdentifier _portfolioId;
  private final UserPrincipal _liveDataUser;
  
  // NOTE: jim 14-June-2010 -- put these back in as we're going to use them now.
  private boolean _computePortfolioNodeCalculations = true;
  private boolean _computePositionNodeCalculations = true;
  private boolean _computeSecurityNodeCalculations /*= false*/;
  private boolean _computePrimitiveNodeCalculations /*= false*/;
  
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

  /**
   * @return whether or not to compute all portfolio nodes, rather than just those required
   */
  public boolean isComputePortfolioNodeCalculations() {
    return _computePortfolioNodeCalculations;
  }

  /**
   * @param computePortfolioNodeCalculations whether or not to compute all portfolio nodes, rather than just those required
   */
  public void setComputePortfolioNodeCalculations(boolean computePortfolioNodeCalculations) {
    _computePortfolioNodeCalculations = computePortfolioNodeCalculations;
  }

  /**
   * @return whether or not to compute all position nodes, rather than just those required
   */
  public boolean isComputePositionNodeCalculations() {
    return _computePositionNodeCalculations;
  }

  /**
   * @param computePositionNodeCalculations whether or not to compute all position nodes, rather than just those required
   */
  public void setComputePositionNodeCalculations(boolean computePositionNodeCalculations) {
    _computePositionNodeCalculations = computePositionNodeCalculations;
  }

  /**
   * @return whether or not to compute all security nodes, rather than just those required
   */
  public boolean isComputeSecurityNodeCalculations() {
    return _computeSecurityNodeCalculations;
  }

  /**
   * @param computeSecurityNodeCalculations whether or not to compute all security nodes, rather than just those required
   */
  public void setComputeSecurityNodeCalculations(boolean computeSecurityNodeCalculations) {
    _computeSecurityNodeCalculations = computeSecurityNodeCalculations;
  }

  /**
   * @return whether or not to compute all primitive nodes, rather than just those required
   */
  public boolean isComputePrimitiveNodeCalculations() {
    return _computePrimitiveNodeCalculations;
  }

  /**
   * @param computePrimitiveNodeCalculations whether or not to compute all primitive nodes, rather than just those required
   */
  public void setComputePrimitiveNodeCalculations(boolean computePrimitiveNodeCalculations) {
    _computePrimitiveNodeCalculations = computePrimitiveNodeCalculations;
  }
  
  public boolean shouldWriteResults(ComputationTarget computationTarget) {
    ComputationTargetType computationTargetType = computationTarget.getType();
    
    switch (computationTargetType) {
      case PRIMITIVE:
        return isComputePrimitiveNodeCalculations();
      case SECURITY:
        return isComputeSecurityNodeCalculations();
      case POSITION:
        return isComputePositionNodeCalculations();
      case PORTFOLIO_NODE:
        return isComputePortfolioNodeCalculations();
      default:
        throw new RuntimeException("Unexpected type " + computationTargetType);
    }
  }
  
  /**
   * Serializes this ViewDefinition to a Fudge message.
   * @param factory  the Fudge context, not null
   * @return the Fudge message, not null
   */
  public FudgeFieldContainer toFudgeMsg(FudgeMessageFactory factory) {
    ArgumentChecker.notNull(factory, "Fudge Context");
    MutableFudgeFieldContainer msg = factory.newMessage();
    msg.add(NAME_KEY, getName());
    msg.add(PORTFOLIO_ID_KEY, getPortfolioId().toFudgeMsg(factory));
    msg.add(USER_KEY, getLiveDataUser().toFudgeMsg(factory));
    msg.add(COMPUTE_PORTFOLIO_NODE_CALCULATIONS_KEY, _computePortfolioNodeCalculations);
    msg.add(COMPUTE_POSITION_NODE_CALCULATIONS_KEY, _computePositionNodeCalculations);
    msg.add(COMPUTE_SECURITY_NODE_CALCULATIONS_KEY, _computeSecurityNodeCalculations);
    msg.add(COMPUTE_PRIMITIVE_NODE_CALCULATIONS_KEY, _computePrimitiveNodeCalculations);
    if (_deltaRecalculationPeriod != null) {
      msg.add(DELTA_RECALCULATION_PERIOD_KEY, _deltaRecalculationPeriod);
    }
    if (_deltaRecalculationPeriod != null) {
      msg.add(FULL_RECALCULATION_PERIOD_KEY, _fullRecalculationPeriod);
    }
    
    for (ViewCalculationConfiguration calConfig : _calculationConfigurationsByName.values()) {
      msg.add(CALCULATION_CONFIGURATIONS_BY_NAME_KEY, calConfig.toFudgeMsg(factory));
    }
    return msg;
  }

  /**
   * Deserializes this ViewDefinition from a Fudge message.
   * @param msg  the Fudge message, not null
   * @return the ViewDefinition, not null
   */
  public static ViewDefinition fromFudgeMsg(FudgeFieldContainer msg) {
    String name = msg.getString(NAME_KEY);
    
    FudgeFieldContainer portfolioIdMsg = msg.getMessage(PORTFOLIO_ID_KEY);
    String scheme = portfolioIdMsg.getString(UniqueIdentifier.SCHEME_FUDGE_FIELD_NAME);
    String value = portfolioIdMsg.getString(UniqueIdentifier.VALUE_FUDGE_FIELD_NAME);
    String version = portfolioIdMsg.getString(UniqueIdentifier.VERSION_FUDGE_FIELD_NAME);
    UniqueIdentifier portfolioId = UniqueIdentifier.of(scheme, value, version);
    
    FudgeFieldContainer userMessage = msg.getMessage(USER_KEY);
    String userName = userMessage.getString(UserPrincipal.USER_NAME_KEY);
    String ipAddress = userMessage.getString(UserPrincipal.IP_ADDRESS_KEY);
    
    UserPrincipal liveDataUser = new UserPrincipal(userName, ipAddress);
    ViewDefinition result = new ViewDefinition(name, portfolioId, liveDataUser);
     
    result._computePortfolioNodeCalculations = msg.getBoolean(COMPUTE_PORTFOLIO_NODE_CALCULATIONS_KEY);
    result._computePositionNodeCalculations = msg.getBoolean(COMPUTE_POSITION_NODE_CALCULATIONS_KEY);
    result._computePrimitiveNodeCalculations = msg.getBoolean(COMPUTE_PRIMITIVE_NODE_CALCULATIONS_KEY);
    result._computeSecurityNodeCalculations = msg.getBoolean(COMPUTE_SECURITY_NODE_CALCULATIONS_KEY);
    if (msg.hasField(DELTA_RECALCULATION_PERIOD_KEY)) {
      result._deltaRecalculationPeriod = msg.getLong(DELTA_RECALCULATION_PERIOD_KEY);
    }
    if (msg.hasField(FULL_RECALCULATION_PERIOD_KEY)) {
      result._fullRecalculationPeriod = msg.getLong(FULL_RECALCULATION_PERIOD_KEY);
    }
    for (FudgeField field : msg.getAllByName(CALCULATION_CONFIGURATIONS_BY_NAME_KEY)) {
      FudgeFieldContainer calConfigMsg = (FudgeFieldContainer) field.getValue();
      String calConfigName = calConfigMsg.getString(ViewCalculationConfiguration.NAME_KEY);
      for (FudgeField reqBySecType : calConfigMsg.getAllByName(ViewCalculationConfiguration.REQUIREMENTS_BY_SECURITY_TYPE_KEY)) {
        FudgeFieldContainer reqBySecTypeMsg = (FudgeFieldContainer) reqBySecType.getValue();
        String securityType = reqBySecTypeMsg.getString(ViewCalculationConfiguration.SECURITY_TYPE_KEY);
        for (FudgeField fudgeField : reqBySecTypeMsg.getAllByName(ViewCalculationConfiguration.DEFINITIONS_KEY)) {
          String definition = (String) fudgeField.getValue();
          result.addValueDefinition(calConfigName, securityType, definition);
        }
      }
    }
    
    return result;
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
    if (obj instanceof ViewDefinition) {
      ViewDefinition other = (ViewDefinition) obj;
      return ObjectUtils.equals(getName(), other.getName()) 
        && ObjectUtils.equals(getPortfolioId(), other.getPortfolioId())
        && ObjectUtils.equals(getLiveDataUser(), other.getLiveDataUser())
        && ObjectUtils.equals(_computePortfolioNodeCalculations, other._computePortfolioNodeCalculations)
        && ObjectUtils.equals(_computePositionNodeCalculations, other._computePositionNodeCalculations)
        && ObjectUtils.equals(_computePrimitiveNodeCalculations, other._computePrimitiveNodeCalculations)
        && ObjectUtils.equals(_computeSecurityNodeCalculations, other._computeSecurityNodeCalculations)
        && ObjectUtils.equals(_deltaRecalculationPeriod, other._deltaRecalculationPeriod)
        && ObjectUtils.equals(_fullRecalculationPeriod, other._fullRecalculationPeriod)
        && ObjectUtils.equals(getAllCalculationConfigurationNames(), other.getAllCalculationConfigurationNames())
        && ObjectUtils.equals(getAllValueRequirements(), other.getAllValueRequirements());
    }
    return false;
  }
  
  


}
