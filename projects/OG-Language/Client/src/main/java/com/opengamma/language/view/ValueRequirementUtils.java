/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Triple;

/**
 * Utilities for handling {@link ValueRequirement}s from functions.
 */
public final class ValueRequirementUtils {

  private static final Pattern REQUIREMENT_EXPRESSION = Pattern.compile("^(?:(.+)/)?(.+?)([\\[\\{](.+)[\\]\\}])?$");
  private static final String DEFAULT_CONFIG_NAME = "Default";
  
  /**
   * Hidden constructor
   */
  private ValueRequirementUtils() {
  }
  
  public static String generateRequirementName(String calcConfigName, String valueName, ValueProperties constraints) {
    String requirementName = valueName;
    if (!DEFAULT_CONFIG_NAME.equals(calcConfigName)) {
      requirementName = calcConfigName + "/" + requirementName;
    }
    if (constraints != null && !constraints.isEmpty()) {
      requirementName += "[" + constraints.toSimpleString() + "]";
    }
    return requirementName;
  }
  
  /**
   * Parses the string descriptions of the portfolio requirements into sets of value requirements (string/constraint pairs)
   * for each calculation configuration referenced.
   * 
   * @param portfolioRequirements the user supplied requirements
   * @return a map of configuration to value requirement sets
   */
  public static Map<String, Set<Pair<String, ValueProperties>>> parseRequirements(final String[] portfolioRequirements) {
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
  public static Triple<String, String, ValueProperties> parseRequirement(String requirement) {
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
  
}
