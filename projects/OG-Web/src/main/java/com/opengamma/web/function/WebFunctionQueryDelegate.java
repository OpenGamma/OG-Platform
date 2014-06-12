/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.function;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.threeten.bp.Instant;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationBundle;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.engine.function.config.ParameterizedFunctionConfiguration;
import com.opengamma.engine.function.config.StaticFunctionConfiguration;

/**
 * Converts function configurations into a format suitable for viewing on the web. Can also handle filtering of query results via a predicate method.
 */
class WebFunctionQueryDelegate {

  private static final Pattern s_simpleName = Pattern.compile("([^\\.]+)$");

  private final FunctionConfigurationSource _functionConfigurationSource;

  public WebFunctionQueryDelegate(FunctionConfigurationSource functionConfigurationSource) {
    _functionConfigurationSource = functionConfigurationSource;
  }

  /**
   * @return all function type details
   */
  public SortedMap<String, WebFunctionTypeDetails> queryAll() {
    return query(Predicates.<WebFunctionTypeDetails>alwaysTrue());
  }

  /**
   * Returns the types which match the predicate.
   * 
   * @param predicate
   * @return
   */
  public SortedMap<String, WebFunctionTypeDetails> query(Predicate<WebFunctionTypeDetails> predicate) {
    FunctionConfigurationBundle functionConfiguration = _functionConfigurationSource.getFunctionConfiguration(Instant.now());

    SortedMap<String, WebFunctionTypeDetails> allFunctions = Maps.newTreeMap();

    for (FunctionConfiguration input : functionConfiguration.getFunctions()) {
      StaticFunctionConfiguration config = ((StaticFunctionConfiguration) input);
      String fullName = config.getDefinitionClassName();

      Matcher matcher = s_simpleName.matcher(fullName);
      String simpleName = matcher.find() ? matcher.group(1) : "Unknown";

      WebFunctionTypeDetails typeDetails = new WebFunctionTypeDetails();
      typeDetails.setFullyQualifiedName(fullName);
      typeDetails.setSimpleName(simpleName);

      boolean isParameterized = config instanceof ParameterizedFunctionConfiguration;
      typeDetails.setParameterized(isParameterized);

      List<String> parameters;
      if (isParameterized) {
        parameters = ((ParameterizedFunctionConfiguration) config).getParameter();
      } else {
        parameters = Lists.newArrayList();
      }

      List<List<String>> parametersList = Lists.newLinkedList();
      parametersList.add(parameters);
      typeDetails.setParameters(parametersList);

      if (!predicate.apply(typeDetails)) {
        continue;
      }

      if (allFunctions.containsKey(simpleName)) {
        allFunctions.get(simpleName).getParameters().add(parameters);
      } else {
        allFunctions.put(simpleName, typeDetails);
      }
    }

    for (WebFunctionTypeDetails typeDetails : allFunctions.values()) {
      if (typeDetails.isParameterized()) {
        sortParameters(typeDetails);
      }
    }

    return allFunctions;

  }

  private void sortParameters(WebFunctionTypeDetails typeDetails) {

    List<List<String>> parameters = typeDetails.getParameters();

    final Ordering<Object> stringOrdering = Ordering.usingToString();

    Collections.sort(parameters, new Comparator<List<String>>() {

      @Override
      public int compare(List<String> o1, List<String> o2) {

        for (int i = 0; i < Ints.min(o1.size(), o2.size()); i++) {
          int compare = stringOrdering.compare(o1, o2);
          if (compare != 0) {
            return compare;
          }
        }

        return 0;
      }
    });

  }

}
