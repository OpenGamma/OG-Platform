/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.opengamma.util.ArgumentChecker;

/**
 * The encapsulated logic that controls how precisely a view is to be constructed
 * and computed.
 *
 * @author kirk
 */
public class ViewDefinition implements Serializable {
  private final String _name;
  private final String _rootPortfolioName;
  private final Map<String, ViewCalculationConfiguration> _calculationConfigurationsByName =
    new TreeMap<String, ViewCalculationConfiguration>();
  
  public ViewDefinition(String name, String rootPortfolioName) {
    assert name != null;
    assert rootPortfolioName != null;
    
    _name = name;
    _rootPortfolioName = rootPortfolioName;
  }
  
  public Set<String> getAllValueRequirements() {
    Set<String> requirements = new TreeSet<String>();
    for(ViewCalculationConfiguration calcConfig : _calculationConfigurationsByName.values()) {
      requirements.addAll(calcConfig.getAllValueRequirements());
    }
    return requirements;
  }

  public String getName() {
    return _name;
  }

  public String getRootPortfolioName() {
    return _rootPortfolioName;
  }
  
  public Collection<ViewCalculationConfiguration> getAllCalculationConfigurations() {
    return new ArrayList<ViewCalculationConfiguration>(_calculationConfigurationsByName.values());
  }
  
  public Set<String> getAllCalculationConfigurationNames() {
    return Collections.unmodifiableSet(_calculationConfigurationsByName.keySet());
  }
  
  public Map<String, ViewCalculationConfiguration> getAllCalculationConfigurationsByName() {
    return Collections.unmodifiableMap(_calculationConfigurationsByName);
  }
  
  public ViewCalculationConfiguration getCalculationConfiguration(String configurationName) {
    return _calculationConfigurationsByName.get(configurationName);
  }
  
  public void addViewCalculationConfiguration(ViewCalculationConfiguration calcConfig) {
    ArgumentChecker.checkNotNull(calcConfig, "calculation configuration");
    ArgumentChecker.checkNotNull(calcConfig.getName(), "Configuration name");
    _calculationConfigurationsByName.put(calcConfig.getName(), calcConfig);
  }
  
  public void addValueDefinition(String calculationConfigurationName, String securityType, String requirementName) {
    ViewCalculationConfiguration calcConfig = _calculationConfigurationsByName.get(calculationConfigurationName);
    if(calcConfig == null) {
      calcConfig = new ViewCalculationConfiguration(this, calculationConfigurationName);
      _calculationConfigurationsByName.put(calculationConfigurationName, calcConfig);
    }
    calcConfig.addValueRequirement(securityType, requirementName);
  }

}
