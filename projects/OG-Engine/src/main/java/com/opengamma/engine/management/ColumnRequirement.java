/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.management;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.util.ArgumentChecker;

/**
 * Holds a Value requirement name and properties.
 * Essentially a ValueRequirement without a specific target.  Intention is it's neater
 * than lots of Pair<String, ValueProperties>>
 */
public final class ColumnRequirement {
  /**
   * Name of field for percentage of calculations that succeeded
   */
  public static final String PERCENTAGE = "Percentage";
  /**
   * Name of field for the number of total calculations
   */
  public static final String TOTAL = "Total";
  /**
   * Name of field for the number of failed calcalations
   */
  public static final String FAILED = "Failed";
  /**
   * Name of field for the number of failed calcalations
   */
  public static final String ERRORS = "Errors";
  /**
   * Name of field for the number of calculations that succeeded
   */
  public static final String SUCCEEDED = "Succeeded";
  /**
   * Name of field for the value properties
   */
  public static final String VALUE_PROPERTIES = "ValueProperties";
  /**
   * Name of the field for the value requirement
   */
  public static final String REQUIREMENT_NAME = "RequirementName";

  private static final Logger s_logger = LoggerFactory.getLogger(ColumnRequirement.class);
  
  private String _requirementName;
  private ValueProperties _properties;
  private ColumnRequirement(String requirementName, ValueProperties properties) {
    ArgumentChecker.notNull(requirementName, "requirementName");
    ArgumentChecker.notNull(properties, "properties");
    _requirementName = requirementName;
    _properties = properties;
  }
  

  
  public static ColumnRequirement of(String requirementName, ValueProperties properties) {
    return new ColumnRequirement(requirementName, properties);
  }
  
  public String getRequirementName() {
    return _requirementName;
  }
  public ValueProperties getProperties() {
    return _properties;
  }
  
  // Stupid JMX support that shouldn't really be necessary.
  
  public CompositeData toCompositeData(CompositeType type, int success, int fail, int error, int total) {
    Map<String, Object> elements = new LinkedHashMap<>();
    elements.put(REQUIREMENT_NAME, getRequirementName());
    elements.put(VALUE_PROPERTIES, getProperties().toSimpleString());
    elements.put(SUCCEEDED, success);
    elements.put(FAILED, fail);
    elements.put(ERRORS, error);
    elements.put(TOTAL, total);
    elements.put(PERCENTAGE, (total > 0) ? ((double) success / (double) total) * 100d : 0d); // 2dp-ish
    try {
      return new CompositeDataSupport(type, elements);
    } catch (OpenDataException ex) {
      s_logger.error("Error creating composite data support object", ex);
      return null;
    }
  }
  
  public static CompositeType getCompositeType() {
    try {
      return new CompositeType("ColumnRequirements", 
          "Number of successes or failures for a value requirement by security type", 
          new String[] {REQUIREMENT_NAME, 
                        VALUE_PROPERTIES,
                        SUCCEEDED, 
                        FAILED, 
                        ERRORS,
                        TOTAL, 
                        PERCENTAGE },
          new String[] {"The name of the requirement", 
                        "The ValueProperties parameters to the requirement", 
                        "number of calculations that succeeded", 
                        "number of calculations failed", 
                        "number of calculations that returned errors", 
                        "total expected number of calculations", 
                        "percentage of calculations that succeeded" },
          new OpenType[] {SimpleType.STRING, SimpleType.STRING, SimpleType.INTEGER, SimpleType.INTEGER, SimpleType.INTEGER, SimpleType.INTEGER, SimpleType.DOUBLE });
    } catch (OpenDataException ex) {
      s_logger.error("OpenDataException building CompositeType for ColumnRequirementBySecurityType", ex);
      throw new OpenGammaRuntimeException("OpenDataException building CompositeType for ColumnRequirementBySecurityType", ex);
    }
  }
  
  public static TabularType getTabularType() {
    try {
      return new TabularType("ListColumnRequirements", 
          "List of number of successes or failures for a value requirement by security type", 
          getCompositeType(), new String[] {ColumnRequirement.REQUIREMENT_NAME, 
                                            ColumnRequirement.VALUE_PROPERTIES });
    } catch (OpenDataException ex) {
      s_logger.error("OpenDataException building TabularType for ColumnRequirementBySecurityType", ex);
      throw new OpenGammaRuntimeException("OpenDataException building TabularType for ColumnRequirementBySecurityType", ex);
    }
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_properties == null) ? 0 : _properties.hashCode());
    result = prime * result + ((_requirementName == null) ? 0 : _requirementName.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof ColumnRequirement)) {
      return false;
    }
    ColumnRequirement other = (ColumnRequirement) obj;
    if (_properties == null) {
      if (other._properties != null) {
        return false;
      }
    } else if (!_properties.equals(other._properties)) {
      return false;
    }
    if (_requirementName == null) {
      if (other._requirementName != null) {
        return false;
      }
    } else if (!_requirementName.equals(other._requirementName)) {
      return false;
    }
    return true;
  }

}
