/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.security;

import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import org.apache.commons.lang.StringUtils;
import org.fudgemsg.AnnotationReflector;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableBiMap.Builder;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDescription;

/**
 * Provides all supported security/index types to their descriptions
 */
public final class SecurityTypesDescriptionProvider {

  private static final Logger s_logger = LoggerFactory.getLogger(SecurityTypesDescriptionProvider.class);

  /**
   * Singleton instance.
   */
  private static final SecurityTypesDescriptionProvider s_instance = new SecurityTypesDescriptionProvider();

  /**
   * Map of security type to description.
   */
  private final BiMap<String, String> _type2Description;

  //-------------------------------------------------------------------------
  /**
   * Gets the singleton instance.
   * 
   * @return the provider, not null
   */
  public static SecurityTypesDescriptionProvider getInstance() {
    return s_instance;
  }

  //-------------------------------------------------------------------------
  /**
   * Restricted constructor
   */
  private SecurityTypesDescriptionProvider() {
    Builder<String, String> builder = ImmutableBiMap.builder();
    AnnotationReflector reflector = AnnotationReflector.getDefaultReflector();
    Set<Class<?>> securityClasses = reflector.getReflector().getTypesAnnotatedWith(SecurityDescription.class);
    for (Class<?> securityClass : securityClasses) {
      SecurityDescription securityDescriptionAnnotation = securityClass.getAnnotation(SecurityDescription.class);
      if (securityDescriptionAnnotation != null) {
        // extract type
        String type = StringUtils.trimToNull(securityDescriptionAnnotation.type());
        if (type == null) {
          if (ManageableSecurity.class.isAssignableFrom(securityClass)) {
            MetaBean metaBean = JodaBeanUtils.metaBean(securityClass);
            ManageableSecurity bareSecurity = (ManageableSecurity) metaBean.builder().build();
            type = bareSecurity.getSecurityType();
          } else {
            s_logger.warn("{} anotated with {}, but not subtype of {}", securityClass, SecurityDescription.class, ManageableSecurity.class);
          }
        }
        // extract description
        String description = StringUtils.trimToNull(securityDescriptionAnnotation.description());
        if (description == null) {
          description = securityClass.getSimpleName();
        }
        builder.put(type, description);
      }
    }
    _type2Description = builder.build();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the set of security/index types.
   * 
   * @return the types, not null
   */
  public SortedSet<String> getSecurityTypes() {
    return ImmutableSortedSet.copyOf(_type2Description.keySet());
  }

  /**
   * Gets the map of security type to description.
   * 
   * @return the map, not null
   */
  public SortedMap<String, String> getType2Description() {
    ImmutableSortedMap.Builder<String, String> result = ImmutableSortedMap.naturalOrder();
    for (Entry<String, String> entry : _type2Description.entrySet()) {
      result.put(entry.getKey(), entry.getValue());
    }
    return result.build();
  }


  /**
   * Gets the map of security description to type.
   * 
   * @return the map, not null
   */
  public SortedMap<String, String> getDescription2Type() {
    ImmutableSortedMap.Builder<String, String> result = ImmutableSortedMap.naturalOrder();
    for (Entry<String, String> entry : _type2Description.inverse().entrySet()) {
      result.put(entry.getKey(), entry.getValue());
    }
    return result.build();
  }


}
