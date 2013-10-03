/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg.config;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.bbg.loader.SecurityType;
import com.opengamma.core.config.Config;
import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;

/**
 * The mapping of bloomberg security types description to OG security types.
 */
@Config(description = "Bloomberg security type definition")
public class BloombergSecurityTypeDefinition implements Serializable, UniqueIdentifiable, MutableUniqueIdentifiable {
  
  private static final long serialVersionUID = 1L;
  
  /**
   * Name for the default configuration document
   */
  public static final String DEFAULT_CONFIG_NAME = "DEFAULT_BBG_SEC_TYPE_DEFINITION";
     
  
  private final Map<SecurityType, Set<String>> _bbgSecurityTypeMap = Maps.newConcurrentMap();
  
  private UniqueId _uniqueId;

  public SecurityType getSecurityType(String bbgSecurityType) {
    ArgumentChecker.notNull(bbgSecurityType, "bloomberg security type");
    for (Entry<SecurityType, Set<String>> entry : _bbgSecurityTypeMap.entrySet()) {
      if (entry.getValue().contains(bbgSecurityType)) {
        return entry.getKey();
      }
    }
    return null;
  }
  
  public Collection<String> getValidTypes(SecurityType securityType) {
    ArgumentChecker.notNull(securityType, "security type");
    return ImmutableSet.copyOf(_bbgSecurityTypeMap.get(securityType));
  }
  
  public void addSecurityType(String bbgSecurityType, SecurityType type) {
    ArgumentChecker.notNull(bbgSecurityType, "bloomberg security type");
    ArgumentChecker.notNull(type, "security type");
    Set<String> securityTypes = _bbgSecurityTypeMap.get(type);
    if (securityTypes == null) {
      securityTypes = Sets.newHashSet();
      _bbgSecurityTypeMap.put(type, securityTypes);
    }
    securityTypes.add(bbgSecurityType);
  }
  
  public Collection<SecurityType> getAllSecurityTypes() {
    return ImmutableSet.copyOf(_bbgSecurityTypeMap.keySet());
  }

  @Override
  public int hashCode() {
    HashCodeBuilder builder = new HashCodeBuilder();
    builder.append(_bbgSecurityTypeMap);
    return builder.toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }
  
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }


  @Override
  public UniqueId getUniqueId() {
    return _uniqueId;
  }

  @Override
  public void setUniqueId(UniqueId uniqueId) {
    _uniqueId = uniqueId;
  }
 
}
