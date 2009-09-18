/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.analytics.AnalyticValueDefinition;

/**
 * A simple in-memory implementation of {@link ViewDefinition}. 
 *
 * @author kirk
 */
public class ViewDefinitionImpl implements ViewDefinition, Serializable {
  private final String _name;
  private final String _rootPortfolioName;
  private final Map<String, Collection<AnalyticValueDefinition>> _definitionsBySecurityType =
    new HashMap<String, Collection<AnalyticValueDefinition>>();
  
  public ViewDefinitionImpl(String name, String rootPortfolioName) {
    assert name != null;
    assert rootPortfolioName != null;
    
    _name = name;
    _rootPortfolioName = rootPortfolioName;
  }

  @Override
  public Collection<AnalyticValueDefinition> getAllValueDefinitions() {
    Set<AnalyticValueDefinition> definitions = new HashSet<AnalyticValueDefinition>();
    for(Collection<AnalyticValueDefinition> secTypeDefinitions : _definitionsBySecurityType.values()) {
      definitions.addAll(secTypeDefinitions);
    }
    return definitions;
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public String getRootPortfolioName() {
    return _rootPortfolioName;
  }

  @Override
  public Map<String, Collection<AnalyticValueDefinition>> getValueDefinitionsBySecurityTypes() {
    return Collections.unmodifiableMap(_definitionsBySecurityType);
  }
  
  public void addValueDefinitions(String securityType, Collection<? extends AnalyticValueDefinition> definitions) {
    assert securityType != null;
    Collection<AnalyticValueDefinition> secTypeDefinitions = _definitionsBySecurityType.get(securityType);
    if(secTypeDefinitions == null) {
      secTypeDefinitions = new HashSet<AnalyticValueDefinition>();
      _definitionsBySecurityType.put(securityType, secTypeDefinitions);
    }
    secTypeDefinitions.addAll(definitions);
  }
  
  public void addValueDefinition(String securityType, AnalyticValueDefinition definition) {
    addValueDefinitions(securityType, Collections.singleton(definition));
  }

}
