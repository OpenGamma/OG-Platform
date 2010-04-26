/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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

import com.opengamma.util.ArgumentChecker;

/**
 * The configuration for one set of calculations on a particular view.
 *
 * @author kirk
 */
public class ViewCalculationConfiguration implements Serializable {
  private final ViewDefinition _definition;
  private final String _name;
  private final Map<String, Set<String>> _requirementsBySecurityType =
    new TreeMap<String, Set<String>>();
  
  public ViewCalculationConfiguration(ViewDefinition definition, String name) {
    ArgumentChecker.notNull(definition, "Parent view definition");
    ArgumentChecker.notNull(name, "Calculation configuration name");
    _definition = definition;
    _name = name;
  }

  /**
   * @return the definition
   */
  public ViewDefinition getDefinition() {
    return _definition;
  }

  /**
   * @return the name
   */
  public String getName() {
    return _name;
  }

  public Map<String, Set<String>> getValueRequirementsBySecurityTypes() {
    return Collections.unmodifiableMap(_requirementsBySecurityType);
  }
  
  public void addValueRequirements(String securityType, Set<String> definitions) {
    assert securityType != null;
    Set<String> secTypeRequirements = _requirementsBySecurityType.get(securityType);
    if(secTypeRequirements == null) {
      secTypeRequirements = new TreeSet<String>();
      _requirementsBySecurityType.put(securityType, secTypeRequirements);
    }
    secTypeRequirements.addAll(definitions);
  }
  
  public void addValueRequirement(String securityType, String definition) {
    addValueRequirements(securityType, Collections.singleton(definition));
  }

  public Set<String> getAllValueRequirements() {
    Set<String> requirements = new TreeSet<String>();
    for(Set<String> secTypeDefinitions : _requirementsBySecurityType.values()) {
      requirements.addAll(secTypeDefinitions);
    }
    return requirements;
  }

}
