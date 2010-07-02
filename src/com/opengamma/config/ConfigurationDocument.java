/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.config;

import javax.time.Instant;

/**
 * A configuration document type
 *
 * @param <T> the type of Config Document
 */
public interface ConfigurationDocument<T> {

  /**
   * Fudge message key for the oid.
   */
  String OID_FUDGE_FIELD_NAME = "oid";
  /**
   * Fudge message key for the version.
   */
  String VERSION_FUDGE_FIELD_NAME = "version";
  /**
   * Fudge message key for the name.
   */
  String NAME_FUDGE_FIELD_NAME = "name";
  /**
   * Fudge message key for the creationInstant.
   */
  String CREATION_INSTANT_FUDGE_FIELD_NAME = "creationInstant";
  /**
   * Fudge message key for the lastRead.
   */
  String LAST_READ_INSTANT_FUDGE_FIELD_NAME = "lastReadInstant";
  /**
   * Fudge message key for the value.
   */
  
  String VALUE_FUDGE_FIELD_NAME = "value";

  String getId();
  
  String getOid();
  
  int getVersion();
  
  String getName();
   
  Instant getCreationInstant();
  
  Instant getLastReadInstant();
  
  T getValue();
  
}
