/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config;

import java.util.List;

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

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // methods copied from com.opengamma.master.AbstractMaster

  /**
   * Replaces a single version of the document in the data store.
   * <p>
   * This applies a correction that replaces a single version in the data store
   * with the specified list.
   * <p>
   * The versioning will be taken from the "version from" instant in each specified document.
   * The "version to" instant and "correction" instants will be ignored on input.
   * If the "version from" instant is null, the latest instant is used.
   * No two versioned documents may have the same "version from" instant.
   * The versions must all be within the version range of the original version being replaced.
   * <p>
   * If the list is empty, the previous version will have its version range extended.
   * The unique identifier must specify a version of the document that is active when
   * queried with the {@link VersionCorrection latest correction instant}.
   * The unique identifier in each specified document will be ignored on input.
   *
   * @param uniqueId  the unique identifier to replace, not null
   * @param replacementDocuments  the replacement documents, not null
   * @return the list of versioned documents matching the input list, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no document with the object identifier
   */
  <T> List<UniqueId> replaceVersion(UniqueId uniqueId, List<ConfigDocument<T>> replacementDocuments);

  /**
   * Replaces all the versions of the document in the data store.
   * <p>
   * This applies a correction that replaces all the versions in the data store
   * with the specified list.
   * <p>
   * The versioning will be taken from the "version from" instant in each specified document.
   * The "version to" instant and "correction" instants will be ignored on input.
   * If the "version from" instant is null, the latest instant is used.
   * No two versioned documents may have the same "version from" instant.
   * <p>
   * If the list is empty, this is equivalent to {@link #remove}.
   * If the object identifier is a {@link UniqueId}, then it must specify a version of the
   * document that is active when queried with the {@link VersionCorrection latest correction instant}.
   * The unique identifier in each specified document will be ignored on input.
   *
   * @param objectId  the object identifier of the document to replace, not null
   * @param replacementDocuments  the replacement documents, not null
   * @return the list of unique identifiers matching the input list, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no document with the object identifier
   */
  <T> List<UniqueId> replaceAllVersions(ObjectIdentifiable objectId, List<ConfigDocument<T>> replacementDocuments);

  <T> List<UniqueId> replaceVersions(ObjectIdentifiable objectId, List<ConfigDocument<T>> replacementDocuments);

  /**
   * Replaces a single version of the document in the data store.
   * <p>
   * This applies a correction that replaces a single version in the data store
   * with the specified list.
   * This is equivalent to calling {@link #replace(UniqueId, java.util.List)} with a single
   * element list of the specified document.
   * <p>
   * The document must contain the unique identifier to be replaced.
   * The document "version" and "correction" instants will be ignored on input.
   * The unique identifier must specify a version of the document that is active when
   * queried with the {@link VersionCorrection latest correction instant}.
   *
   * @param replacementDocument  the replacement document, not null
   * @return the versioned document matching the input one, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no document with the object identifier
   */
  <T> UniqueId replaceVersion(ConfigDocument<T> replacementDocument);
  // NOTE: this is the same as the current correct() method, which should be deprecated, then deleted

  /**
   * Removes a single version of the document from the data store.
   * <p>
   * This applies a correction that replaces a single version in the data store
   * with the specified list.
   * This is equivalent to calling {@link #replace(UniqueId, java.util.List)} with a single
   * element list of the specified document.
   * <p>
   * The unique identifier must specify a version of the document that is active when
   * queried with the {@link VersionCorrection latest correction instant}.
   *
   * @param uniqueId  the unique identifier to remove, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no document with that unique identifier
   */
  <T> void removeVersion(UniqueId uniqueId);

  /**
   * Adds a new version of the document to the data store.
   * <p>
   * This applies a correction that adds the specified document to the data store.
   * <p>
   * The versioning will be taken from the "version from" instant in the specified document.
   * The "version to" instant and "correction" instants will be ignored on input.
   * No two active versioned documents in the data store may have the same "version from" instant.
   * If the "version from" instant is null, the latest instant is used which would
   * be equivalent to calling {@link #update(D)}.
   * <p>
   * If the object identifier is a {@link UniqueId}, then it must specify a version of the
   * document that is active when queried with the {@link VersionCorrection latest correction instant}.
   *
   * @param objectId  the object identifier of the document to add a version to, not null
   * @param documentToAdd  the document, not null
   * @return the current state of the document, may be an update of the input document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no document with that unique identifier
   */
  <T> UniqueId addVersion(ObjectIdentifiable objectId, ConfigDocument<T> documentToAdd);
  // if "version from" is non-null this is equivalent to a search to find the active document
  // for the "version from" instant and using replaceVersion() with a list of two documents
  // if "version from" is null this is equivalent to update(), but update() can hopefully be deprecated


}
