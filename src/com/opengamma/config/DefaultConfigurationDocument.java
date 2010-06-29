/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.config;

import javax.time.Instant;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.util.ArgumentChecker;

/**
 * Default Implementation of ConfigurationDocument 
 *@param <T> The configuaration type
 */
public class DefaultConfigurationDocument<T> implements ConfigurationDocument<T> {
  
  private final String _id;
  private final String _oid;
  private final int _version;
  private final String _name;
  private final Instant _creationInstant;
  private T _value;
  
  /**
   * @param id the mongodb generated object id, not-null
   * @param oid the configuration document id, all versions will have same oid, not-null
   * @param version the version number, > 0
   * @param name the name of configuration document, not-null
   * @param creationInstant the creation time, not-null
   * @param value the actual configuation type, not-null
   */
  public DefaultConfigurationDocument(String id, String oid, int version, String name, Instant creationInstant, T value) {
    ArgumentChecker.notNull(id, "id");
    ArgumentChecker.notNull(oid, "oid");
    ArgumentChecker.isTrue(version > 0, "negative version not allowed");
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(creationInstant, "creationInstant");
    ArgumentChecker.notNull(value, "value");
    _id = id;
    _oid = oid;
    _version = version;
    _name = name;
    _creationInstant = creationInstant;
    _value = value;
  }

  @Override
  public Instant getCreationInstant() {
    return _creationInstant;
  }

  @Override
  public String getId() {
    return _id;
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public String getOid() {
    return _oid;
  }

  @Override
  public T getValue() {
    return _value;
  }

  @Override
  public int getVersion() {
    return _version;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ObjectUtils.hashCode(getOid());
    result = prime * result + ObjectUtils.hashCode(getVersion());
    result = prime * result + ObjectUtils.hashCode(getValue());
    return result;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof DefaultConfigurationDocument) {
      DefaultConfigurationDocument other = (DefaultConfigurationDocument) obj;
      return ObjectUtils.equals(getOid(), other.getOid()) && ObjectUtils.equals(getVersion(), other.getVersion()) && ObjectUtils.equals(getValue(), other.getValue());
    }
    return false;
  }
  
  

}
