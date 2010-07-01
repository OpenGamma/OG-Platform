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
import java.util.Map.Entry;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMessageFactory;
import org.fudgemsg.MutableFudgeFieldContainer;

import com.opengamma.util.ArgumentChecker;

/**
 * The configuration for one set of calculations on a particular view.
 */
public class ViewCalculationConfiguration implements Serializable {
  /**
   * Fudge message key for the name
   */
  public static final String NAME_KEY = "name";
  /**
   * Fudge message key for the security type.
   */
  public static final String SECURITY_TYPE_KEY = "securityType";
  /**
   * Fudge message key for the definitions.
   */
  public static final String DEFINITIONS_KEY = "definitions";
  /**
   * Fudge message key for the requirementsBySecurityType.
   */
  public static final String REQUIREMENTS_BY_SECURITY_TYPE_KEY = "requirementsBySecurityType";
  
  
  private final ViewDefinition _definition;
  private final String _name;
  private final Map<String, Set<String>> _requirementsBySecurityType =
    new TreeMap<String, Set<String>>();
  
  /**
   * Start with an empty delta definition which will perform simple equality comparisons. This should be customized as
   * required for the view configuration.
   */
  private final DeltaDefinition _deltaDefinition = new DeltaDefinition();
  
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
  
  /**
   * @return the delta definition
   */
  public DeltaDefinition getDeltaDefinition() {
    return _deltaDefinition;
  }

  public Map<String, Set<String>> getValueRequirementsBySecurityTypes() {
    return Collections.unmodifiableMap(_requirementsBySecurityType);
  }
  
  public void addValueRequirements(String securityType, Set<String> definitions) {
    assert securityType != null;
    Set<String> secTypeRequirements = _requirementsBySecurityType.get(securityType);
    if (secTypeRequirements == null) {
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
    for (Set<String> secTypeDefinitions : _requirementsBySecurityType.values()) {
      requirements.addAll(secTypeDefinitions);
    }
    return requirements;
  }

  /**
   * Serializes this ViewDefinition to a Fudge message.
   * @param factory  the Fudge context, not null
   * @return the Fudge message, not null
   */
  public FudgeFieldContainer toFudgeMsg(FudgeMessageFactory factory) {
    MutableFudgeFieldContainer result = factory.newMessage();
    result.add(NAME_KEY, getName());
    for (Entry<String, Set<String>> entry : _requirementsBySecurityType.entrySet()) {
      MutableFudgeFieldContainer requirementMsg = factory.newMessage();
      String securityType = entry.getKey();
      Set<String> definitions = entry.getValue();
      requirementMsg.add(SECURITY_TYPE_KEY, securityType);
      requirementMsg.add(SECURITY_TYPE_KEY, securityType);
      for (String definition : definitions) {
        requirementMsg.add(DEFINITIONS_KEY, definition);
      }
      result.add(REQUIREMENTS_BY_SECURITY_TYPE_KEY, requirementMsg);
    }
    return result;
  }

}
