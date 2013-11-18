/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.function.resolver.ComputationTargetFilter;
import com.opengamma.engine.function.resolver.IdentityResolutionRuleTransform;
import com.opengamma.engine.function.resolver.ResolutionRuleTransform;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * The configuration for one set of calculations on a particular view.
 */
@PublicAPI
public class ViewCalculationConfiguration implements Serializable {
  
  private static final Logger s_logger = LoggerFactory.getLogger(ViewCalculationConfiguration.class);

  /**
   * Dummy "security type" constant to request a value at the aggregate level only.
   */
  public static final String SECURITY_TYPE_AGGREGATE_ONLY = "AGGREGATE_ONLY";
  // Andrew 2011-09-02 -- Is this a good idea? Should there be a set of requirements for aggregate nodes only?

  /** Serialization version. */
  private static final long serialVersionUID = 1L;
  private final ViewDefinition _viewDefinition;

  private final String _name;

  /**
   * Contains the required portfolio outputs for each security type. These are the outputs produced at the position
   * and aggregate position level, with respect to the reference portfolio. Accepting portfolio outputs as a set of
   * strings is really just for user convenience; ValueRequirements are eventually still needed for each of these for
   * every position and aggregate position in the reference portfolio.
   */
  private final Map<String, Set<Pair<String, ValueProperties>>> _portfolioRequirementsBySecurityType = new TreeMap<>();

  /**
   * Contains any specific outputs required, where each entry really corresponds to a single output at computation
   * time.
   */
  private final Set<ValueRequirement> _specificRequirements = new HashSet<>();

  /**
   * Start with an empty delta definition which will perform simple equality comparisons. This should be customized as
   * required for the view configuration.
   */
  private DeltaDefinition _deltaDefinition = new DeltaDefinition();

  /**
   * The scenarioId to be used for this configuration
   * */
  private UniqueId _scenarioId;

  /**
   * The scenarioParametersId to be used for this configuration
   * */
  private UniqueId _scenarioParametersId;

  /**
   * A set of default properties for functions to configure themselves from. Note that these are intended to represent generic
   * concepts that would typically be expressed through constraints, for example a default currency or default curve, that might
   * apply to a number of functions and will affect graph construction. Information specific to a particular function to
   * override its default execution behavior only (i.e. it will not affect the choice to use that function in the graph, or any
   * other aspect of graph building) should be set using the {@link FunctionParameters} for that function (for example a Monte
   * Carlo iteration count) - see {@link #_resolutionRuleTransform} - and not constraints or default properties.
   */
  private ValueProperties _defaultProperties = ValueProperties.none();

  /**
   * A transformation to apply to the default resolution rules created by the view processor. Altering the resolution rules can
   * affect dependency graph construction by allowing functions to be suppressed and/or priorities changed. The default parameters
   * for functions can also be adjusted, either globally or using a {@link ComputationTargetFilter} to affect only a specific
   * subset of the graph.
   */
  private ResolutionRuleTransform _resolutionRuleTransform = IdentityResolutionRuleTransform.INSTANCE;

  /**
   * Defines the labels and order of the columns used for displaying the data in the UI. This doesn't have to contain
   * columns for every value name / properties combination in the configuration. Any columns that aren't defined
   * will use the default label and will appear at the end in the order their requirements are defined.
   */
  private List<Column> _columns = Collections.emptyList();

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
  
  //-------------------------------------------------------------------------
  /**
   * Copies this view calculation configuration to a new parent view definition, adding the copy to the new owner.
   * 
   * @param newOwner  the new parent view definition, not null
   * @return
   */
  public void copyTo(ViewDefinition newOwner) {
    ViewCalculationConfiguration copy = new ViewCalculationConfiguration(newOwner, getName());
    newOwner.addViewCalculationConfiguration(copy);
    copy.setDefaultProperties(getDefaultProperties());
    copy.addSpecificRequirements(getSpecificRequirements());
    copy.setScenarioId(getScenarioId());
    copy.setScenarioParametersId(getScenarioParametersId());
    for (Map.Entry<String, Set<Pair<String, ValueProperties>>> requirementEntry : getPortfolioRequirementsBySecurityType().entrySet()) {
      copy.addPortfolioRequirements(requirementEntry.getKey(), requirementEntry.getValue()); 
    }
    
    // REVIEW jonathan 2011-11-13 -- should really do deep copies of these to avoid references to same objects
    copy.getDeltaDefinition().setNumberComparer(getDeltaDefinition().getNumberComparer());
    copy.setResolutionRuleTransform(getResolutionRuleTransform());
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
   * Returns the default value properties for the view. Functions that expect a property constraint on values
   * they are asked to produce should refer to the defaults if the constraint is absent, or use the default
   * to construct the input requirements.
   * 
   * @return the default property set
   */
  public ValueProperties getDefaultProperties() {
    return _defaultProperties;
  }

  /**
   * Returns the scenarioId to be used for this configuration - if set, this will refer to a scenario defined in
   * the config master.
   *
   * @return the scenarioId for this configuration, may be null
   */
  public UniqueId getScenarioId() {
    return _scenarioId;
  }

  /**
   * Sets the scenarioId to be used for this configuration - if set, this will refer to a scenario defined in
   * the config master.
   *
   * @param scenarioId the scenarioId for this configuration, may be null
   */
  public void setScenarioId(UniqueId scenarioId) {
    _scenarioId = scenarioId;
  }

  /**
  /**
   * Returns the scenarioParametersId to be used for this configuration - if set, this will refer to a scenario
   * parameters defined in the config master.
   *
   * @return the scenarioParametersId for this configuration, may be null
   */
  public UniqueId getScenarioParametersId() {
    return _scenarioParametersId;
  }

  /**
   * Sets the scenarioParametersId to be used for this configuration - if set, this will refer to a scenario parameters
   * defined in the config master.
   *
   * @param scenarioParametersId the scenarioParametersId for this configuration, may be null
   */
  public void setScenarioParametersId(UniqueId scenarioParametersId) {
    _scenarioParametersId = scenarioParametersId;
  }

  /**
   * Sets the default value properties for the view. Functions that expect a property constraint on values
   * they are asked to produce should refer to the defaults if the constraint is absent, or use the default
   * to construct the input requirements.
   * 
   * @param defaultProperties the default properties
   */
  public void setDefaultProperties(final ValueProperties defaultProperties) {
    ArgumentChecker.notNull(defaultProperties, "defaultProperties");
    _defaultProperties = defaultProperties;
  }

  /**
   * Sets the transformation to use on resolution rules when compiling a view for execution under this
   * configuration.
   * 
   * @return the resolution rule transformation
   */
  public ResolutionRuleTransform getResolutionRuleTransform() {
    return _resolutionRuleTransform;
  }

  public void setResolutionRuleTransform(final ResolutionRuleTransform resolutionRuleTransform) {
    ArgumentChecker.notNull(resolutionRuleTransform, "resolutionRuleTransform");
    _resolutionRuleTransform = resolutionRuleTransform;
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
   * Gets a set containing every portfolio output that is required, regardless of the security type(s) on which the
   * output is required. These are outputs produced at the position and aggregate position level, with respect to the
   * reference portfolio. 
   * 
   * @return  a set of every required portfolio output, not null
   */
  public Set<Pair<String, ValueProperties>> getAllPortfolioRequirements() {
    Set<Pair<String, ValueProperties>> requirements = Sets.newLinkedHashSet();
    for (Set<Pair<String, ValueProperties>> secTypeDefinitions : _portfolioRequirementsBySecurityType.values()) {
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
      secTypeRequirements = Sets.newLinkedHashSet();
      _portfolioRequirementsBySecurityType.put(securityType, secTypeRequirements);
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
    addPortfolioRequirements(securityType, Collections.singleton(Pairs.of(requiredOutput, constraints)));
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

  /**
   * @return The column definitions, not null
   */
  public List<Column> getColumns() {
    return _columns;
  }

  /**
   * @param columns The column definitions, not null
   */
  public void setColumns(List<Column> columns) {
    ArgumentChecker.notNull(columns, "columns");
    _columns = columns;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ObjectUtils.hashCode(getName());
    result = prime * result + ObjectUtils.hashCode(getAllPortfolioRequirements());
    result = prime * result + ObjectUtils.hashCode(getSpecificRequirements());
    result = prime * result + ObjectUtils.hashCode(getDefaultProperties());
    result = prime * result + ObjectUtils.hashCode(getScenarioId());
    result = prime * result + ObjectUtils.hashCode(getScenarioParametersId());
    result = prime * result + ObjectUtils.hashCode(getResolutionRuleTransform());
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
        && ObjectUtils.equals(_portfolioRequirementsBySecurityType.keySet(), other._portfolioRequirementsBySecurityType.keySet())) {
      return false;
    }
    Map<String, Set<Pair<String, ValueProperties>>> otherPortfolioRequirementsBySecurityType = other.getPortfolioRequirementsBySecurityType();
    for (Map.Entry<String, Set<Pair<String, ValueProperties>>> securityTypeRequirements : getPortfolioRequirementsBySecurityType().entrySet()) {
      Set<Pair<String, ValueProperties>> otherRequirements = otherPortfolioRequirementsBySecurityType.get(securityTypeRequirements.getKey());
      if (!ObjectUtils.equals(securityTypeRequirements.getValue(), otherRequirements)) {
        return false;
      }
    }
    if (!ObjectUtils.equals(getDefaultProperties(), other.getDefaultProperties())) {
      return false;
    }
    if (!ObjectUtils.equals(getResolutionRuleTransform(), other.getResolutionRuleTransform())) {
      return false;
    }
    if (!ObjectUtils.equals(getScenarioId(), other.getScenarioId())) {
      return false;
    }
    if (!ObjectUtils.equals(getScenarioParametersId(), other.getScenarioParametersId())) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    if (s_logger.isDebugEnabled()) {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE, false);
    } else {
      return "ViewCalculationConfiguration[" + getName() + "]";
    }
  }

  public static final class Column {

    private final String _header;
    private final String _valueName;
    private final ValueProperties _properties;

    public Column(String header, String valueName, ValueProperties properties) {
      ArgumentChecker.notNull(header, "label");
      ArgumentChecker.notNull(valueName, "valueName");
      ArgumentChecker.notNull(properties, "properties");
      _header = header;
      _valueName = valueName;
      _properties = properties;
    }

    public String getHeader() {
      return _header;
    }

    public String getValueName() {
      return _valueName;
    }

    public ValueProperties getProperties() {
      return _properties;
    }
  }
}
