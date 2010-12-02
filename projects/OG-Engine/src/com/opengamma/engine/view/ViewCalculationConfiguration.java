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

import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;
import com.opengamma.util.tuple.Pair;

/**
 * The configuration for one set of calculations on a particular view.
 */
@PublicAPI
public class ViewCalculationConfiguration implements Serializable {

  private final ViewDefinition _viewDefinition;
  private final String _name;

  /**
   * Contains the required portfolio outputs for each security type. These are the outputs produced at the position
   * and aggregate position level, with respect to the reference portfolio. Accepting portfolio outputs as a set of
   * strings is really just for user convenience; ValueRequirements are eventually still needed for each of these for
   * every position and aggregate position in the reference portfolio.
   */
  private final Map<String, Set<Pair<String, ValueProperties>>> _portfolioRequirementsBySecurityType = new TreeMap<String, Set<Pair<String, ValueProperties>>>();
  
  /**
   * Contains the required trade outputs for each security type. These are the outputs produced at the trade level, 
   * with respect to the reference portfolio. 
   */
  private final Map<String, Set<Pair<String, ValueProperties>>> _tradeRequirementsBySecurityType = new TreeMap<String, Set<Pair<String, ValueProperties>>>();

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
  public Map<String, Set<Pair<String, ValueProperties>>> getPortfolioRequirementsBySecurityType() {
    return Collections.unmodifiableMap(_portfolioRequirementsBySecurityType);
  }
  
  /**
   * Gets the required trade outputs by security type. These are the outputs produced at the trade level, 
   * with respect to the reference portfolio.
   * 
   * @return  a map of security type to the names of the required outputs for that type, not null
   */
  public Map<String, Set<Pair<String, ValueProperties>>> getTradeRequirementsBySecurityType() {
    return Collections.unmodifiableMap(_tradeRequirementsBySecurityType);
  }

  /**
   * Gets a set containing every portfolio output that is required, regardless of the security type(s) on which the
   * output is required. These are outputs produced at the position and aggregate position level, with respect to the
   * reference portfolio. 
   * 
   * @return  a set of every required portfolio output, not null
   */
  public Set<Pair<String, ValueProperties>> getAllPortfolioRequirements() {
    Set<Pair<String, ValueProperties>> requirements = new TreeSet<Pair<String, ValueProperties>>();
    for (Set<Pair<String, ValueProperties>> secTypeDefinitions : _portfolioRequirementsBySecurityType.values()) {
      requirements.addAll(secTypeDefinitions);
    }
    return requirements;
  }
  
  /**
   * Gets a set containing every trade output that is required, regardless of the security type(s) on which the
   * output is required. These are outputs produced at the trade level, with respect to the
   * reference portfolio. 
   * 
   * @return  a set of every required trade output, not null
   */
  public Set<Pair<String, ValueProperties>> getAllTradeRequirements() {
    Set<Pair<String, ValueProperties>> requirements = new TreeSet<Pair<String, ValueProperties>>();
    for (Set<Pair<String, ValueProperties>> secTypeDefinitions : _tradeRequirementsBySecurityType.values()) {
      requirements.addAll(secTypeDefinitions);
    }
    return requirements;
  }

  /**
   * Adds a set of required portfolio outputs for the given security type. These are outputs produced at the position
   * and aggregate position level, with respect to the reference portfolio.
   * 
   * @param securityType  the type of security for which the outputs should be produced, not null
   * @param requiredOutputs  a set of output names and value constraints, not null
   */
  public void addPortfolioRequirements(String securityType, Set<Pair<String, ValueProperties>> requiredOutputs) {
    ArgumentChecker.notNull(securityType, "securityType");
    ArgumentChecker.notNull(requiredOutputs, "requiredOutputs");
    Set<Pair<String, ValueProperties>> secTypeRequirements = _portfolioRequirementsBySecurityType.get(securityType);
    if (secTypeRequirements == null) {
      secTypeRequirements = new TreeSet<Pair<String, ValueProperties>>();
      _portfolioRequirementsBySecurityType.put(securityType, secTypeRequirements);
    }
    secTypeRequirements.addAll(requiredOutputs);
  }
  
  /**
   * Adds a set of required trade outputs for the given security type. These are outputs produced at the trade level, 
   * with respect to the reference portfolio.
   * 
   * @param securityType  the type of security for which the outputs should be produced, not null
   * @param requiredOutputs  a set of output names and value constraints, not null
   */
  public void addTradeRequirements(String securityType, Set<Pair<String, ValueProperties>> requiredOutputs) {
    ArgumentChecker.notNull(securityType, "securityType");
    ArgumentChecker.notNull(requiredOutputs, "requiredOutputs");
    Set<Pair<String, ValueProperties>> secTypeRequirements = _tradeRequirementsBySecurityType.get(securityType);
    if (secTypeRequirements == null) {
      secTypeRequirements = new TreeSet<Pair<String, ValueProperties>>();
      _tradeRequirementsBySecurityType.put(securityType, secTypeRequirements);
    }
    secTypeRequirements.addAll(requiredOutputs);
  }

  /**
   * Adds a set of required portfolio outputs for the given security type with no value constraints. This is
   * equivilant to calling {@link #addPortfolioRequirements (String, Set)} with
   * {@code ValueProperties.none ()} against each output name.
   * 
   * @param securityType the type of security for which the outputs should be produced, not null
   * @param requiredOutputs a set of output names, not null
   */
  public void addPortfolioRequirementNames(final String securityType, final Set<String> requiredOutputs) {
    ArgumentChecker.notNull(securityType, "securityType");
    ArgumentChecker.notNull(requiredOutputs, "requiredOutput");
    for (String requiredOutput : requiredOutputs) {
      addPortfolioRequirementName(securityType, requiredOutput);
    }
  }
  
  /**
   * Adds a set of required trade outputs for the given security type with no value constraints. This is
   * equivilant to calling {@link #addTradeRequirements (String, Set)} with
   * {@code ValueProperties.none ()} against each output name.
   * 
   * @param securityType the type of security for which the outputs should be produced, not null
   * @param requiredOutputs a set of output names, not null
   */
  public void addTradeRequirementNames(final String securityType, final Set<String> requiredOutputs) {
    ArgumentChecker.notNull(securityType, "securityType");
    ArgumentChecker.notNull(requiredOutputs, "requiredOutput");
    for (String requiredOutput : requiredOutputs) {
      addTradeRequirementName(securityType, requiredOutput);
    }
  }

  /**
   * Adds a required portfolio output for the given security type. This is an output produced at the position and
   * aggregate position level, with respect to the reference portfolio.
   * 
   * @param securityType  the type of security for which the output should be produced, not null
   * @param requiredOutput  an output name, not null
   * @param constraints constraints on the requirement, not null
   */
  public void addPortfolioRequirement(String securityType, String requiredOutput, ValueProperties constraints) {
    ArgumentChecker.notNull(securityType, "securityType");
    ArgumentChecker.notNull(requiredOutput, "requiredOutput");
    ArgumentChecker.notNull(constraints, "constraints");
    addPortfolioRequirements(securityType, Collections.singleton((Pair<String, ValueProperties>) Pair.of(requiredOutput, constraints)));
  }
  
  /**
   * Adds a required trade output for the given security type. This is an output produced at the trade level, 
   * with respect to the reference portfolio.
   * 
   * @param securityType  the type of security for which the output should be produced, not null
   * @param requiredOutput  an output name, not null
   * @param constraints constraints on the requirement, not null
   */
  public void addTradeRequirement(String securityType, String requiredOutput, ValueProperties constraints) {
    ArgumentChecker.notNull(securityType, "securityType");
    ArgumentChecker.notNull(requiredOutput, "requiredOutput");
    ArgumentChecker.notNull(constraints, "constraints");
    addTradeRequirements(securityType, Collections.singleton((Pair<String, ValueProperties>) Pair.of(requiredOutput, constraints)));
  }

  /**
   * Adds a required portfolio output for the given security type with no value constraints. This is equivilant
   * to calling {@link #addPortfolioRequirement (String, String, ValueProperties)} with {@code ValueProperties.none ()}.
   * 
   * @param securityType the type of security for which the output should be produced, not null
   * @param requiredOutput an output name, not null
   */
  public void addPortfolioRequirementName(final String securityType, final String requiredOutput) {
    addPortfolioRequirement(securityType, requiredOutput, ValueProperties.none());
  }
  
  /**
   * Adds a required trade output for the given security type with no value constraints. This is equivilant
   * to calling {@link #addTradeRequirement (String, String, ValueProperties)} with {@code ValueProperties.none ()}.
   * 
   * @param securityType the type of security for which the output should be produced, not null
   * @param requiredOutput an output name, not null
   */
  public void addTradeRequirementName(final String securityType, final String requiredOutput) {
    addTradeRequirement(securityType, requiredOutput, ValueProperties.none());
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
    result = prime * result + ObjectUtils.hashCode(getAllTradeRequirements());
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
    if (!(ObjectUtils.equals(getName(), other.getName()) && ObjectUtils.equals(getDeltaDefinition(), other.getDeltaDefinition()) && ObjectUtils.equals(getSpecificRequirements(), other
        .getSpecificRequirements()))
        && ObjectUtils.equals(_portfolioRequirementsBySecurityType.keySet(), other._portfolioRequirementsBySecurityType.keySet())
        && ObjectUtils.equals(_tradeRequirementsBySecurityType.keySet(), other._tradeRequirementsBySecurityType.keySet())) {
      return false;
    }
    Map<String, Set<Pair<String, ValueProperties>>> otherPortfolioRequirementsBySecurityType = other.getPortfolioRequirementsBySecurityType();
    for (Map.Entry<String, Set<Pair<String, ValueProperties>>> securityTypeRequirements : getPortfolioRequirementsBySecurityType().entrySet()) {
      Set<Pair<String, ValueProperties>> otherRequirements = otherPortfolioRequirementsBySecurityType.get(securityTypeRequirements.getKey());
      if (!ObjectUtils.equals(securityTypeRequirements.getValue(), otherRequirements)) {
        return false;
      }
    }
    Map<String, Set<Pair<String, ValueProperties>>> otherTradeRequirementsBySecurityType = other.getTradeRequirementsBySecurityType();
    for (Map.Entry<String, Set<Pair<String, ValueProperties>>> securityTypeRequirements : getTradeRequirementsBySecurityType().entrySet()) {
      Set<Pair<String, ValueProperties>> otherRequirements = otherTradeRequirementsBySecurityType.get(securityTypeRequirements.getKey());
      if (!ObjectUtils.equals(securityTypeRequirements.getValue(), otherRequirements)) {
        return false;
      }
    }
    return true;
  }

}
