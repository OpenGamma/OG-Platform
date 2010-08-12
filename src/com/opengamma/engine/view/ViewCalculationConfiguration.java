/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.util.ArgumentChecker;

/**
 * The configuration for one set of calculations on a particular view.
 */
public class ViewCalculationConfiguration implements Serializable {
  
  private final ViewDefinition _viewDefinition;
  private final String _name;
  
  private boolean _disableAggregatePositionOutputs;
  private boolean _disablePositionOutputs;
  
  /**
   * Contains the required portfolio outputs for each security type. These are the outputs produced at the position
   * and aggregate position level, with respect to the reference portfolio. Accepting portfolio outputs as a set of
   * strings is really just for user convenience; ValueRequirements are eventually still needed for each of these for
   * every position and aggregate position in the reference portfolio.
   */
  private final Map<String, Set<String>> _portfolioRequirementsBySecurityType = new TreeMap<String, Set<String>>();
  
  /**
   * Contains any specific outputs required, where each entry really corresponds to a single output at computation
   * time.
   */
  private final Set<ValueRequirement> _specificRequirements = new HashSet<ValueRequirement>();
  
  /**
   * Start with an empty delta definition which will perform simple equality comparisons. This should be customized as
   * required for the view configuration.
   */
  private DeltaDefinition _deltaDefinition = new DeltaDefinition();
  
  /**
   * Constructs an instance.
   * 
   * @param definition  the parent view definition, not null
   * @param name  the calculation configuration name, not null
   */
  public ViewCalculationConfiguration(ViewDefinition definition, String name) {
    ArgumentChecker.notNull(definition, "Parent view definition");
    ArgumentChecker.notNull(name, "Calculation configuration name");
    _viewDefinition = definition;
    _name = name;
  }

  /**
   * @return the parent view definition, not null
   */
  public ViewDefinition getViewDefinition() {
    return _viewDefinition;
  }

  /**
   * @return the name, not null
   */
  public String getName() {
    return _name;
  }
  
  /**
   * @return the delta definition, not null
   */
  public DeltaDefinition getDeltaDefinition() {
    return _deltaDefinition;
  }
  
  public void setDeltaDefinition(DeltaDefinition deltaDefinition) {
    ArgumentChecker.notNull(deltaDefinition, "deltaDefinition");
    _deltaDefinition = deltaDefinition;
  }

  /**
   * Gets the required portfolio outputs by security type. These are the outputs produced at the position and
   * aggregate position level, with respect to the reference portfolio.
   * 
   * @return  a map of security type to the names of the required outputs for that type, not null
   */
  public Map<String, Set<String>> getPortfolioRequirementsBySecurityType() {
    return Collections.unmodifiableMap(_portfolioRequirementsBySecurityType);
  }
  
  /**
   * Gets a set containing every portfolio output that is required, regardless of the security type(s) on which the
   * output is required. These are outputs produced at the position and aggregate position level, with respect to the
   * reference portfolio. 
   * 
   * @return  a set of every required portfolio output, not null
   */
  public Set<String> getAllPortfolioRequirements() {
    Set<String> requirements = new TreeSet<String>();
    for (Set<String> secTypeDefinitions : _portfolioRequirementsBySecurityType.values()) {
      requirements.addAll(secTypeDefinitions);
    }
    return requirements;
  }
  
  /**
   * Adds a set of required portfolio outputs for the given security type. These are outputs produced at the position
   * and aggregate position level, with respect to the reference portfolio.
   * 
   * @param securityType  the type of security for which the outputs should be produced, not null
   * @param requiredOutputs  a set of output names, not null
   * 
   * @see {@link #setPositionOutputsDisabled(boolean)}
   * @see {@link #setAggregatePositionOutputsDisabled(boolean)}
   */
  public void addPortfolioRequirements(String securityType, Set<String> requiredOutputs) {
    ArgumentChecker.notNull(securityType, "securityType");
    ArgumentChecker.notNull(requiredOutputs, "requiredOutputs");
    Set<String> secTypeRequirements = _portfolioRequirementsBySecurityType.get(securityType);
    if (secTypeRequirements == null) {
      secTypeRequirements = new TreeSet<String>();
      _portfolioRequirementsBySecurityType.put(securityType, secTypeRequirements);
    }
    secTypeRequirements.addAll(requiredOutputs);
  }
  
  /**
   * Adds a required portfolio output for the given security type. This is an output produced at the position and
   * aggregate position level, with respect to the reference portfolio.
   * 
   * @param securityType  the type of security for which the output should be produced, not null
   * @param requiredOutput  an output name, not null
   * 
   * @see {@link #setPositionOutputsDisabled(boolean)}
   * @see {@link #setAggregatePositionOutputsDisabled(boolean)}
   */
  public void addPortfolioRequirement(String securityType, String requiredOutput) {
    ArgumentChecker.notNull(requiredOutput, "requiredOutput");
    addPortfolioRequirements(securityType, Collections.singleton(requiredOutput));
  }
  
  /**
   * Gets whether aggregate position outputs are disabled. This is independent of individual position outputs.
   * 
   * @return  whether aggregate position outputs are disabled
   */
  public boolean isAggregatePositionOutputsDisabled() {
    return _disableAggregatePositionOutputs;
  }

  /**
   * Sets whether aggregate position outputs are disabled. For example, the referenced portfolio could have a deep
   * structure with many nodes at which aggregate portfolio outputs would be calculated. If these are not required then
   * disabling them could speed up the computation cycle significantly.
   * 
   * @param disableAggregatePositionOutputs  whether aggregate position outputs are to be disabled.
   */
  public void setAggregatePositionOutputsDisabled(boolean disableAggregatePositionOutputs) {
    _disableAggregatePositionOutputs = disableAggregatePositionOutputs;
  }

  /**
   * Gets whether individual position outputs are disabled. This is independent of aggregate position outputs. 
   * 
   * @return  whether individual position outputs are disabled
   */
  public boolean isPositionOutputsDisabled() {
    return _disablePositionOutputs;
  }

  /**
   * Sets whether individual position outputs are disabled. If only aggregate position calculations are required, with
   * respect to the hierarchy of the reference portfolio, then disabling outputs for individual positions through this
   * method could speed up the computation cycle significantly. This is beneficial for calculations, such as VaR, which
   * can be performed at the aggregate level without requiring the complete result of the same calculation on its
   * children. Aggregate calculations where this is not the case will be unaffected, although disabling the individual
   * position outputs will still hide them user even though they were calculated.
   * 
   * @param disablePositionOutputs  whether individual position outputs are to be disabled
   */
  public void setPositionOutputsDisabled(boolean disablePositionOutputs) {
    _disablePositionOutputs = disablePositionOutputs;
  }
  
  /**
   * Gets whether outputs for a particular target have been disabled in the view calculation configuration. This should
   * be used to determine whether or not an output for the target should appear in the results.
   * 
   * @param computationTarget  the target, not null
   * @return  <code>true</code> if outputs for this target have been disabled, <code>false</code> otherwise.
   */
  public boolean outputsDisabled(ComputationTarget computationTarget) {
    ArgumentChecker.notNull(computationTarget, "computationTarget");
    ComputationTargetType computationTargetType = computationTarget.getType();
    switch (computationTargetType) {
      case PRIMITIVE:
      case SECURITY:
        return false;
      case POSITION:
        return isPositionOutputsDisabled();
      case PORTFOLIO_NODE:
        return isAggregatePositionOutputsDisabled();
      default:
        throw new RuntimeException("Unexpected target type " + computationTargetType);
    }
  }
  
  /**
   * Gets a set containing every specific requirement. 
   * 
   * @return  a set containing every specific requirement, not null
   */
  public Set<ValueRequirement> getSpecificRequirements() {
    return Collections.unmodifiableSet(_specificRequirements);
  }
  
  /**
   * Adds a set of required outputs. These outputs are specific in the sense that they have already been resolved into
   * {@link ValueRequirement}s which reference the target for which the value is required. Such outputs would usually
   * be related to the portfolio outputs in some way, for example to obtain some underlying market data that was input
   * to the portfolio calculations. However, no relationship to the portfolio is required, particularly because
   * the view might not reference a portfolio, and these outputs could be used to request arbitrary values.
   * 
   * @param requirements  the requirements, not null
   */
  public void addSpecificRequirements(Set<ValueRequirement> requirements) {
    ArgumentChecker.notNull(requirements, "requirements");
    _specificRequirements.addAll(requirements);
  }
  
  /**
   * Adds a required output. This output is specific in the sense that it has already been resolved into a
   * {@link ValueRequirement} which references the target for which the value is required. Such an output would usually
   * be related to the portfolio outputs in some way, for example to obtain some underlying market data that was an
   * input to the portfolio calculations. However, no relationship to the portfolio is required, particularly because
   * the view might not reference a portfolio, and this output could be used to request an arbitrary value.
   *  
   * @param requirement  the output, not null
   */
  public void addSpecificRequirement(ValueRequirement requirement) {
    ArgumentChecker.notNull(requirement, "requirement");
    addSpecificRequirements(Collections.singleton(requirement));
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ObjectUtils.hashCode(getName());
    result = prime * result + ObjectUtils.hashCode(getAllPortfolioRequirements());
    result = prime * result + ObjectUtils.hashCode(getSpecificRequirements());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ViewCalculationConfiguration)) {
      return false;
    }
    
    ViewCalculationConfiguration other = (ViewCalculationConfiguration) obj;
    
    boolean i2 = ObjectUtils.equals(getDeltaDefinition(), other.getDeltaDefinition());
    System.out.println(i2);

    return ObjectUtils.equals(getName(), other.getName())
      && ObjectUtils.equals(getDeltaDefinition(), other.getDeltaDefinition())
      && ObjectUtils.equals(isPositionOutputsDisabled(), other.isPositionOutputsDisabled())
      && ObjectUtils.equals(isAggregatePositionOutputsDisabled(), other.isAggregatePositionOutputsDisabled())
      && ObjectUtils.equals(getSpecificRequirements(), other.getSpecificRequirements());
  }
  
}
