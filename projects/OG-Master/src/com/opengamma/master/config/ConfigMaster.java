/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.ChangeProvider;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.PublicSPI;

/**
 * A general-purpose configuration master.
 * <p>
 * The configuration master provides a uniform view over storage of configuration elements.
 * This interface provides methods that allow the master to be searched and updated.
 * <p>
 * Many different kinds of configuration element may be stored in a single master.
 */
@PublicSPI
public interface ConfigMaster extends ChangeProvider {

  /**
   * Gets a document by unique identifier.
   * <p>
   * The identifier version string will be used to return the correct historic version providing
   * that the master supports history.
   * 
   * @param uniqueId  the unique identifier, not null
   * @return the document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no document with that unique identifier
   */
  ConfigDocument<?> get(UniqueId uniqueId);

  /**
   * Gets a document by object identifier and version-correction locator.
   * <p>
   * The version-correction will be used to return the correct historic version providing
   * that the master supports history.
   * 
   * @param objectId  the object identifier, not null
   * @param versionCorrection  the version-correction locator to search at, not null
   * @return the document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no document with that unique identifier
   */
  ConfigDocument<?> get(ObjectIdentifiable objectId, VersionCorrection versionCorrection);

  /**
   * Gets a document by unique identifier.
   * <p>
   * The identifier version string will be used to return the correct historic version providing
   * that the master supports history.
   * 
   * @param <T>  the configuration element type
   * @param uniqueId  the unique identifier, not null
   * @param clazz the class of the configuration element
   * @return the document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no document with that unique identifier
   */
  <T> ConfigDocument<T> get(UniqueId uniqueId, Class<T> clazz);

  /**
   * Gets a document by object identifier and version-correction locator.
   * <p>
   * The version-correction will be used to return the correct historic version providing
   * that the master supports history.
   * 
   * @param <T>  the configuration element type
   * @param objectId  the object identifier, not null
   * @param versionCorrection  the version-correction locator to search at, not null
   * @param clazz the class of the configuration element
   * @return the document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no document with that unique identifier
   */
  <T> ConfigDocument<T> get(ObjectIdentifiable objectId, VersionCorrection versionCorrection, Class<T> clazz);

  /**
   * Adds a document to the data store.
   * <p>
   * The version instant, correction instant and identifier will be set in the response.
   *
   * @param <T>  the configuration element type
   * @param document  the document, not null
   * @return the added document, may be an update of the input document, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  <T> ConfigDocument<T> add(ConfigDocument<T> document);

  /**
   * Updates a document in the data store.
   * <p>
   * The specified document must contain the unique identifier.
   * If the identifier has a version it must be the latest version.
   * The version instant, correction instant and identifier will be set in the response.
   * <p>
   * A full master will store detailed historic information on documents.
   * Thus, an update does not prevent retrieval or correction of an earlier version.
   * 
   * @param <T>  the configuration element type
   * @param document  the document, not null
   * @return the current state of the document, may be an update of the input document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no document with that unique identifier
   */
  <T> ConfigDocument<T> update(ConfigDocument<T> document);

  /**
   * Corrects a document in the data store.
   * <p>
   * A full master will store detailed historic information on documents
   * and will support correction of each version.
   * To update a document with a new version, use {@link #update}.
   * To correct a previously stored version, use this method.
   * Older versions and corrections can be accessed using a versioned identifier.
   * <p>
   * The specified document must contain the unique identifier.
   * The unique identifier must specify the last correction of a specific version of the document.
   * 
   * @param <T>  the configuration element type
   * @param document  the document, not null
   * @return the corrected state of the version, may be an update of the input document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no document with that unique identifier
   */
  <T> ConfigDocument<T> correct(ConfigDocument<T> document);

  /**
   * Queries the meta-data about the master.
   * <p>
   * This can return information that is useful for drop-down lists.
   * 
   * @param request  the search request, not null
   * @return the requested meta-data, not null
   */
  ConfigMetaDataResult metaData(ConfigMetaDataRequest request);

  /**
   * Searches for configuration documents matching the specified search criteria.
   * 
   * @param <T>  the configuration element type
   * @param request  the search request, not null
   * @return the search result, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  <T> ConfigSearchResult<T> search(ConfigSearchRequest<T> request);

  /**
   * Queries the history of a single piece of configuration.
   * <p>
   * The request must contain an object identifier to identify the configuration.
   * 
   * @param <T>  the configuration element type
   * @param request  the history request, not null
   * @return the configuration history, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  <T> ConfigHistoryResult<T> history(ConfigHistoryRequest<T> request);

  /**
   * Removes a document from the data store.
   * <p>
   * A full master will store detailed historic information on documents.
   * Thus, a removal does not prevent retrieval or correction of an earlier version.
   * <p>
   * If the identifier has a version it must be the latest version.
   * 
   * @param uniqueId  the unique identifier to remove, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no document with that unique identifier
   */
  void remove(final UniqueId uniqueId);

}
