/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.view;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.impl.PortfolioMapper;
import com.opengamma.core.position.impl.SecurityTypeMapperFunction;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.view.ResultModelDefinition;
import com.opengamma.engine.view.ResultOutputMode;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.UniqueId;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;
import com.opengamma.language.position.PortfolioUtils;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Triple;

/**
 * Creates a view definition
 */
public class ViewDefinitionFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final ViewDefinitionFunction INSTANCE = new ViewDefinitionFunction();

  private static final Pattern REQUIREMENT_EXPRESSION = Pattern.compile("^(?:(.+)/)?(.+?)(\\[(.+)\\])?$");
  private static final String DEFAULT_CONFIG_NAME = "Default";
  private static final long DEFAULT_CALCULATION_TIME = 1000L;

  private long _defaultMinDelta = DEFAULT_CALCULATION_TIME;
  private long _defaultMaxDelta = DEFAULT_CALCULATION_TIME;
  private long _defaultMinFull = DEFAULT_CALCULATION_TIME;
  private long _defaultMaxFull = DEFAULT_CALCULATION_TIME;

  private final MetaFunction _meta;

  private static final int NAME = 0;
  private static final int PORTFOLIO = 1;
  private static final int REQUIREMENTS = 2;

  private static List<MetaParameter> parameters() {
    final MetaParameter name = new MetaParameter("name", JavaTypeInfo.builder(String.class).get());
    final MetaParameter portfolio = new MetaParameter("portfolio", JavaTypeInfo.builder(UniqueId.class).get());
    final MetaParameter requirements = new MetaParameter("requirements", JavaTypeInfo.builder(String.class).get().arrayOfWithAllowNull(true));
    return Arrays.asList(name, portfolio, requirements);
  }

  private ViewDefinitionFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.VIEW, "ViewDefinition", getParameters(), this));
  }

  protected ViewDefinitionFunction() {
    this(new DefinitionAnnotater(ViewDefinitionFunction.class));
  }

  public void setDefaultMinDelta(final long defaultMinDelta) {
    _defaultMinDelta = defaultMinDelta;
  }

  public long getDefaultMinDelta() {
    return _defaultMinDelta;
  }

  public void setDefaultMaxDelta(final long defaultMaxDelta) {
    _defaultMaxDelta = defaultMaxDelta;
  }

  public long getDefaultMaxDelta() {
    return _defaultMaxDelta;
  }

  public void setDefaultMinFull(final long defaultMinFull) {
    _defaultMinFull = defaultMinFull;
  }

  public long getDefaultMinFull() {
    return _defaultMinFull;
  }

  public void setDefaultMaxFull(final long defaultMaxFull) {
    _defaultMaxFull = defaultMaxFull;
  }

  public long getDefaultMaxFull() {
    return _defaultMaxFull;
  }

  /**
   * Parses the string descriptions of the portfolio requirements into sets of value requirements (string/constraint pairs)
   * for each calculation configuration referenced.
   * 
   * @param portfolioRequirements the user supplied requirements
   * @return a map of configuration to value requirement sets
   */
  protected static Map<String, Set<Pair<String, ValueProperties>>> parseRequirements(final String[] portfolioRequirements) {
    final Map<String, Set<Pair<String, ValueProperties>>> results = new HashMap<String, Set<Pair<String, ValueProperties>>>();
    for (String requirement : portfolioRequirements) {
      if (StringUtils.isBlank(requirement)) {
        continue;
      }
      Triple<String, String, ValueProperties> parsedRequirement = parseRequirement(requirement);
      Set<Pair<String, ValueProperties>> configRequirements = results.get(parsedRequirement.getFirst());
      if (configRequirements == null) {
        configRequirements = new HashSet<Pair<String, ValueProperties>>();
        results.put(parsedRequirement.getFirst(), configRequirements);
      }
      configRequirements.add(Pair.of(parsedRequirement.getSecond(), parsedRequirement.getThird()));
    }
    return results;
  }

  /**
   * Parses ValueRequirement strings of the form: CalcConfig/ValueName[Constraint1=[Value1,Value2],Constraint2=Value3]
   * 
   * @param requirement the requirement string, not null
   * @return the (configuration, value name, value properties) triple, not null
   */
  protected static Triple<String, String, ValueProperties> parseRequirement(String requirement) {
    ArgumentChecker.notNull(requirement, "requirement");
    Matcher matcher = REQUIREMENT_EXPRESSION.matcher(requirement);
    if (!matcher.matches()) {
      throw new OpenGammaRuntimeException("Invalid requirement syntax: " + requirement);
    }
    String calcConfigName = matcher.group(1);
    if (StringUtils.isBlank(calcConfigName)) {
      calcConfigName = DEFAULT_CONFIG_NAME;
    }
    String requirementName = matcher.group(2);
    ValueProperties constraints;
    String constraintsString = matcher.group(4);
    constraints = ValueProperties.parse(constraintsString);
    return Triple.of(calcConfigName, requirementName, constraints);
  }

  public ViewDefinition invoke(final SessionContext context, final String viewName, final UniqueId portfolio, final String[] requirements) {
    final Map<String, Set<Pair<String, ValueProperties>>> portfolioRequirementsByConfiguration = parseRequirements(requirements);
    ResultModelDefinition resultModelDefinition = new ResultModelDefinition(ResultOutputMode.TERMINAL_OUTPUTS);
    final ViewDefinition definition = new ViewDefinition(viewName, portfolio, context.getUserContext().getLiveDataUser(), resultModelDefinition);
    definition.setMinDeltaCalculationPeriod(getDefaultMinDelta());
    definition.setMaxDeltaCalculationPeriod(getDefaultMaxDelta());
    definition.setMinFullCalculationPeriod(getDefaultMinFull());
    definition.setMaxFullCalculationPeriod(getDefaultMaxFull());
    final Set<String> portfolioSecurityTypes = PortfolioMapper.mapToSet(PortfolioUtils.getResolvedPortfolio(context.getGlobalContext(), portfolio).getRootNode(), new SecurityTypeMapperFunction());
    for (Map.Entry<String, Set<Pair<String, ValueProperties>>> configurationPortfolioRequirements : portfolioRequirementsByConfiguration.entrySet()) {
      ViewCalculationConfiguration config = new ViewCalculationConfiguration(definition, configurationPortfolioRequirements.getKey());
      // We don't know why particular values have been requested and what they might be applicable to, so best thing is
      // to add all requirements for all security types in the portfolio.
      if (configurationPortfolioRequirements != null) {
        for (String portfolioSecurityType : portfolioSecurityTypes) {
          config.addPortfolioRequirements(portfolioSecurityType, configurationPortfolioRequirements.getValue());
        }
      }
      definition.addViewCalculationConfiguration(config);
    }
    return definition;
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke(sessionContext, (String) parameters[NAME], (UniqueId) parameters[PORTFOLIO], (String[]) parameters[REQUIREMENTS]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
