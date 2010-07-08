/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.config;

import java.util.List;
import java.util.Set;

import javax.time.Instant;

/**
 * General Configuration Document Repository
 * 
 * @param <T> Type of Document 
 *
 * 
 */
public interface ConfigDocumentRepository<T> {
  
  /**
   * Load the current version of the document with the specified name
   * 
   * @param name The name of config document not-null
   * @return the config Document,  null if not found
   */
  ConfigDocument<T> getByName(String name);
  
  /**
   * Load version of the document which <em>currently</em> has the name provided
   * as of the point in time provided.
   * 
   * @param currentName the current name of document, not-null
   * @param effectiveInstant effective time after name change, not-null
   * @return the Config Document,  null if not found
   */
  ConfigDocument<T> getByName(String currentName, Instant effectiveInstant);
  
  /**
   * Load the document with specified oid and version
   * @param oid the object identifier, not-null
   * @param version the document version
   * @return the config document, null if not found
   */
  ConfigDocument<T> getByOid(String oid, int version);

  /**
   * Obtain all versions of the document with the specified OID in between
   * the two dates.
   * 
   * @param oid the config doc object identifier, not null
   * @param startDate the startDate, not null
   * @param endDate the endDate, null for current date
   * @return the list of config documents, not-null empty if not found
   */
  List<ConfigDocument<T>> getSequence(String oid, Instant startDate, Instant endDate);

  /**
   * 
   * @param name the config document name, not-null
   * @param value The config document, not-null
   * @return created config document, null if can not be created
   */
  ConfigDocument<T> insertNewItem(String name, T value);
  
  /**
   * 
   * @param oid the config doc object identifier, not null
   * @param value the config document, not-null 
   * @return created config document, null if can not be created
   */
  ConfigDocument<T> insertNewVersion(String oid, T value);
  
  /**
   * Insert new version with a different name
   * @param oid the config doc object identifier, not null
   * @param name The name of config document not-null
   * @param value the config document, not-null 
   * @return created config document, null if can not be created
   */
  ConfigDocument<T> insertNewVersion(String oid, String name, T value);
  
  /**
   * @return all names for config documents
   */
  Set<String> getNames();
  
}
