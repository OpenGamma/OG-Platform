/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.id.UniqueIdentifier;

/**
 * 
 */
public class ComputationTarget {
  
  private int _id = -1;
  private int _computationTargetType = -1;
  private String _idScheme;
  private String _idValue;
  private String _idVersion;
  private String _name;
  
  public int getId() {
    return _id;
  }

  public void setId(int id) {
    _id = id;
  }

  public int getComputationTargetType() {
    return _computationTargetType;
  }

  public void setComputationTargetType(int computationTargetType) {
    _computationTargetType = computationTargetType;
  }
  
  public static int getType(ComputationTargetType computationTargetType) {
    return computationTargetType.ordinal();   
  }
  
  public void setComputationTargetType(ComputationTargetType computationTargetType) {
    _computationTargetType = getType(computationTargetType);
  }

  public String getIdScheme() {
    return _idScheme;
  }

  public void setIdScheme(String idScheme) {
    _idScheme = idScheme;
  }
  
  public String getIdValue() {
    return _idValue;
  }
  
  public void setIdValue(String idValue) {
    _idValue = idValue;
  }
  
  public String getIdVersion() {
    return _idVersion;
  }

  public void setIdVersion(String idVersion) {
    _idVersion = idVersion;
  }
  
  public String getName() {
    return _name;
  }

  public void setName(String name) {
    _name = name;
  }

  public ComputationTargetSpecification toComputationTargetSpec() {
    for (ComputationTargetType type : ComputationTargetType.values()) {
      if (type.ordinal() == _computationTargetType) {
        return new ComputationTargetSpecification(
            type,
            UniqueIdentifier.of(_idScheme, _idValue, _idVersion)
        );
      }
    }
    
    throw new IllegalStateException("Cannot find type with ordinal " + _computationTargetType); 
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
  
}
