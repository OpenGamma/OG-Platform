/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.config;

import javax.time.Instant;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.id.UniqueIdentifier;

/**
 * Default Implementation of ConfigurationDocument 
 *@param <T> The configuaration type
 */
public class DefaultConfigDocument<T> implements ConfigDocument<T> {
  
  private String _id;
  private String _oid;
  private int _version;
  private String _name;
  private Instant _creationInstant;
  private Instant _lastReadInstant;
  private T _value;
  private UniqueIdentifier _uniqueIdentifier;
  
  public DefaultConfigDocument() {
  }
  
  /**
   * @param id the mongodb generated object id, not-null
   * @param oid the configuration document id, all versions will have same oid, not-null
   * @param version the version number, > 0
   * @param name the name of configuration document, not-null
   * @param creationInstant the creation time, not-null
   * @param lastReadInstant the last accessed time, not -null
   * @param value the actual configuation type, not-null
   */
  public DefaultConfigDocument(String id, String oid, int version, String name, Instant creationInstant, Instant lastReadInstant, T value) {
    _id = id;
    _oid = oid;
    _version = version;
    _name = name;
    _creationInstant = creationInstant;
    _lastReadInstant = lastReadInstant;
    _value = value;
  }
  
  /**
   * @param id the id to set
   */
  public void setId(String id) {
    _id = id;
  }

  /**
   * @param oid the oid to set
   */
  public void setOid(String oid) {
    _oid = oid;
  }

  /**
   * @param version the version to set
   */
  public void setVersion(int version) {
    _version = version;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    _name = name;
  }

  /**
   * @param creationInstant the creationInstant to set
   */
  public void setCreationInstant(Instant creationInstant) {
    _creationInstant = creationInstant;
  }

  /**
   * @param lastReadInstant the lastReadInstant to set
   */
  public void setLastReadInstant(Instant lastReadInstant) {
    _lastReadInstant = lastReadInstant;
  }

  /**
   * @param value the value to set
   */
  public void setValue(T value) {
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
  public Instant getLastReadInstant() {
    return _lastReadInstant;
  }
  
  @Override
  public UniqueIdentifier getUniqueIdentifier() {
    return _uniqueIdentifier;
  }

  /**
   * @param uid the uniqueIdentifier
   */
  public void setUniqueIdentifier(UniqueIdentifier uid) {
    _uniqueIdentifier = uid;
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
    if (obj instanceof DefaultConfigDocument) {
      DefaultConfigDocument other = (DefaultConfigDocument) obj;
      return ObjectUtils.equals(getOid(), other.getOid()) && ObjectUtils.equals(getVersion(), other.getVersion()) && ObjectUtils.equals(getValue(), other.getValue());
    }
    return false;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
  
}
