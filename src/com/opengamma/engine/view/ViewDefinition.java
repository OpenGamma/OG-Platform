/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * A simple in-memory implementation of {@link ViewDefinition}. 
 *
 * @author kirk
 */
public class ViewDefinition implements Serializable {
  private final String _name;
  private final String _rootPortfolioName;
  private final Map<String, Set<String>> _definitionsBySecurityType =
    new TreeMap<String, Set<String>>();
  
  public ViewDefinition(String name, String rootPortfolioName) {
    assert name != null;
    assert rootPortfolioName != null;
    
    _name = name;
    _rootPortfolioName = rootPortfolioName;
  }

  public Set<String> getAllValueDefinitions() {
    Set<String> definitions = new TreeSet<String>();
    for(Set<String> secTypeDefinitions : _definitionsBySecurityType.values()) {
      definitions.addAll(secTypeDefinitions);
    }
    return definitions;
  }

  public String getName() {
    return _name;
  }

  public String getRootPortfolioName() {
    return _rootPortfolioName;
  }

  public Map<String, Set<String>> getValueDefinitionsBySecurityTypes() {
    return Collections.unmodifiableMap(_definitionsBySecurityType);
  }
  
  public void addValueDefinitions(String securityType, Set<String> definitions) {
    assert securityType != null;
    Set<String> secTypeDefinitions = _definitionsBySecurityType.get(securityType);
    if(secTypeDefinitions == null) {
      secTypeDefinitions = new TreeSet<String>();
      _definitionsBySecurityType.put(securityType, secTypeDefinitions);
    }
    secTypeDefinitions.addAll(definitions);
  }
  
  public void addValueDefinition(String securityType, String definition) {
    addValueDefinitions(securityType, Collections.singleton(definition));
  }

}
