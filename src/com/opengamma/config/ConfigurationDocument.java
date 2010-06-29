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

  String getId();
  
  String getOid();
  
  int getVersion();
  
  String getName();
   
  Instant getCreationInstant();
  
  T getValue();
  
}
