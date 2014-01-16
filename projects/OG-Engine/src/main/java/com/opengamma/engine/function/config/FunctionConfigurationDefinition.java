/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.config;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.opengamma.core.config.Config;
import com.opengamma.core.config.ConfigGroups;
import com.opengamma.util.ArgumentChecker;

/**
 * Container for static function configuration definitions.
 * <p>
 * Note that
 */
@Config(description = "Function configuration definition", group = ConfigGroups.MISC)
public final class FunctionConfigurationDefinition {

  private static final Logger s_logger = LoggerFactory.getLogger(FunctionConfigurationDefinition.class);

  /**
   * Function bundle name.
   */
  private final String _name;
  /**
   * List of linked function configuration definition documents
   */
  private final List<String> _functionConfigurationDefinitions;
  /**
   * List of static functions.
   */
  private final List<StaticFunctionConfiguration> _staticFunctions;
  /**
   * List of parameterized functions.
   */
  private final List<ParameterizedFunctionConfiguration> _parameterizedFunctions;

  /**
   * Creates an instance
   * 
   * @param name the name of the function configuration definition, not null.
   * @param functionConfigurationDefinitions the names of linked function configuration definition documents, not null.
   * @param staticFunctions the list of static function configurations, not null.
   * @param parameterizedFunctions the list of parameterized function configurations, not null.
   */
  public FunctionConfigurationDefinition(final String name, final List<String> functionConfigurationDefinitions, final List<StaticFunctionConfiguration> staticFunctions,
      final List<ParameterizedFunctionConfiguration> parameterizedFunctions) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(functionConfigurationDefinitions, "functionConfigurationDefinitions");
    ArgumentChecker.notNull(staticFunctions, "staticFunctions");
    ArgumentChecker.notNull(parameterizedFunctions, "parameterizedFunctions");

    _name = name;
    _functionConfigurationDefinitions = ImmutableList.copyOf(functionConfigurationDefinitions);
    _staticFunctions = ImmutableList.copyOf(staticFunctions);
    _parameterizedFunctions = ImmutableList.copyOf(parameterizedFunctions);
  }

  /**
   * Gets the name.
   * 
   * @return the name
   */
  public String getName() {
    return _name;
  }

  /**
   * Gets the functionConfigurationDefinitions.
   * 
   * @return the functionConfigurationDefinitions
   */
  public List<String> getFunctionConfigurationDefinitions() {
    return _functionConfigurationDefinitions;
  }

  /**
   * Gets the staticFunctions.
   * 
   * @return the staticFunctions
   */
  public List<StaticFunctionConfiguration> getStaticFunctions() {
    return _staticFunctions;
  }

  /**
   * Gets the parameterizedFunctions.
   * 
   * @return the parameterizedFunctions
   */
  public List<ParameterizedFunctionConfiguration> getParameterizedFunctions() {
    return _parameterizedFunctions;
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  /**
   * Creates a FunctionConfigurationDefinition from a given FunctionConfigurationSource
   * 
   * @param name the definition name, not-null.
   * @param configurationSource the function configuration source, not-null.
   * @return the created function configuration definition, not-null.
   */
  public static FunctionConfigurationDefinition of(final String name, final FunctionConfigurationSource configurationSource) {
    return of(name, Collections.<String>emptyList(), configurationSource);
  }

  /**
   * Creates a static FunctionConfigurationDefinition from a given FunctionConfigurationSource
   * 
   * @param name the definition name, not-null.
   * @param linkedConfigs the list of linked configs, not-null.
   * @param configurationSource the function configuration source, not-null.
   * @return the created function configuration definition, not-null.
   */
  public static FunctionConfigurationDefinition of(final String name, final List<String> linkedConfigs, final FunctionConfigurationSource configurationSource) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(configurationSource, "configurationSource");
    ArgumentChecker.notNull(linkedConfigs, "linkedConfigs");

    final List<FunctionConfiguration> functions = configurationSource.getFunctionConfiguration(Instant.now()).getFunctions();

    List<StaticFunctionConfiguration> staticFunctions = Lists.newArrayList();
    List<ParameterizedFunctionConfiguration> parameterizedFunctions = Lists.newArrayList();

    for (FunctionConfiguration functionConfiguration : functions) {
      if (functionConfiguration instanceof ParameterizedFunctionConfiguration) {
        parameterizedFunctions.add((ParameterizedFunctionConfiguration) functionConfiguration);
      } else if (functionConfiguration instanceof StaticFunctionConfiguration) {
        staticFunctions.add((StaticFunctionConfiguration) functionConfiguration);
      } else {
        s_logger.warn("Unsupported FunctionConfiguration type {} ", functionConfiguration.getClass());
      }
    }

    return new FunctionConfigurationDefinition(name, linkedConfigs, staticFunctions, parameterizedFunctions);
  }

}
