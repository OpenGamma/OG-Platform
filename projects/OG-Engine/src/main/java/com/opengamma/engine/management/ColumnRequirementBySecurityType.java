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
import com.opengamma.util.ArgumentChecker;

/**
 * Holds a Value requirement name and properties associated with a particular security type.  
 * Essentially a ValueRequirement + security type without a specific target.  Intention is it's neater
 * than lots of Map.Entry<String, Pair<String, ValueProperties>>
 */
public class ColumnRequirementBySecurityType {
  private static final Logger s_logger = LoggerFactory.getLogger(ColumnRequirementBySecurityType.class);
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
   * Name of field for the number of calcalations that yielded errors
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
  /**
   * Name of the field for the value requirement
   */
  public static final String SECURITY_TYPE = "SecurityType";  
  
  private String _securityType;
  private ColumnRequirement _columnRequirement;

  public String getSecurityType() {
    return _securityType;
  }

  public ColumnRequirement getColumnRequirement() {
    return _columnRequirement;
  }

  public ColumnRequirementBySecurityType(String securityType, ColumnRequirement columnRequirement) {
    ArgumentChecker.notNull(securityType, "securityType");
    ArgumentChecker.notNull(columnRequirement, "columnRequirement");
    _securityType = securityType;
    _columnRequirement = columnRequirement;
  }
  
  public static final ColumnRequirementBySecurityType of(String securityType, ColumnRequirement columnRequirement) {
    return new ColumnRequirementBySecurityType(securityType, columnRequirement);
  }
  
  public CompositeData toCompositeData(CompositeType type, int success, int fail, int error, int total) {
    Map<String, Object> elements = new LinkedHashMap<>();
    elements.put(SECURITY_TYPE, getSecurityType());
    elements.put(REQUIREMENT_NAME, getColumnRequirement().getRequirementName());
    elements.put(VALUE_PROPERTIES, getColumnRequirement().getProperties().toSimpleString());
    elements.put(SUCCEEDED, success);
    elements.put(FAILED, fail);
    elements.put(ERRORS, error);
    elements.put(TOTAL, total);
    elements.put(PERCENTAGE, total > 0 ? ((double) success / (double) total) * 100d : 0d); // 2dp-ish
    try {
      return new CompositeDataSupport(type, elements);
    } catch (OpenDataException ex) {
      s_logger.error("Error creating composite data support object", ex);
      throw new OpenGammaRuntimeException("Error creating composite data support object", ex);
    }
  }
  
  public static CompositeType getCompositeType() {
    try {
      return new CompositeType("ColumnRequirementsBySecurityType", 
          "Number of successes or failures for a value requirement by security type", 
          new String[] {SECURITY_TYPE, 
                        REQUIREMENT_NAME, 
                        VALUE_PROPERTIES, 
                        SUCCEEDED, 
                        FAILED,
                        ERRORS,
                        TOTAL, 
                        PERCENTAGE },
          new String[] {"The security type", 
                        "The name of the requirement", 
                        "The ValueProperties parameters to the requirement", 
                        "number of calculations that succeeded", 
                        "number of calculations failed to be resolved", 
                        "number of calculations that returned errors", 
                        "total expected number of calculations", 
                        "percentage of calculations that succeeded" },
          new OpenType[] {SimpleType.STRING, SimpleType.STRING, SimpleType.STRING, SimpleType.INTEGER, SimpleType.INTEGER, SimpleType.INTEGER, SimpleType.INTEGER, SimpleType.DOUBLE });
    } catch (OpenDataException ex) {
      s_logger.error("OpenDataException building CompositeType for ColumnRequirement", ex);
      throw new OpenGammaRuntimeException("OpenDataException building CompositeType for ColumnRequirement", ex);
    }
  }
  
  public static TabularType getTablularType() {
    try {
      return new TabularType("ListColumnRequirementsBySecurityType", 
          "List of number of successes or failures for a value requirement by security type", 
          getCompositeType(), new String[] {ColumnRequirementBySecurityType.SECURITY_TYPE, 
                                            ColumnRequirementBySecurityType.REQUIREMENT_NAME, 
                                            ColumnRequirementBySecurityType.VALUE_PROPERTIES });
    } catch (OpenDataException ex) {
      s_logger.error("OpenDataException building TabularType for ColumnRequirement", ex);
      throw new OpenGammaRuntimeException("OpenDataException building TabularType for ColumnRequirement", ex);
    }
  }


  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_columnRequirement == null) ? 0 : _columnRequirement.hashCode());
    result = prime * result + ((_securityType == null) ? 0 : _securityType.hashCode());
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
    if (!(obj instanceof ColumnRequirementBySecurityType)) {
      return false;
    }
    ColumnRequirementBySecurityType other = (ColumnRequirementBySecurityType) obj;
    if (_columnRequirement == null) {
      if (other._columnRequirement != null) {
        return false;
      }
    } else if (!_columnRequirement.equals(other._columnRequirement)) {
      return false;
    }
    if (_securityType == null) {
      if (other._securityType != null) {
        return false;
      }
    } else if (!_securityType.equals(other._securityType)) {
      return false;
    }
    return true;
  }
}
