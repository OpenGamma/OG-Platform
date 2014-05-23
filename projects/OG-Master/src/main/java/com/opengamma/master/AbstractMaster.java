/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.AuthorizationException;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.PublicSPI;

/**
 * An outline definition of a general-purpose master storing documents.
 * <p>
 * The master provides a uniform view over a set of documents.
 * This interface provides methods that allow the documents to be
 * added, updated, corrected, removed and retrieved.
 * Sub interfaces typically add methods for searching.
 *
 * @param <D>  the type of the document
 */
@PublicSPI
public interface AbstractMaster<D extends AbstractDocument> {

  /**
   * Gets a document by unique identifier.
   * <p>
   * The identifier version string will be used to return the correct historic version providing
   * that the master supports history.
   * <p>
   * Access to a document may be controlled by permissions.
   * If the user does not have permission to view the document then an exception is thrown.
   *
   * @param uniqueId  the unique identifier, not null
   * @return the document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no document with that unique identifier
   * @throws AuthorizationException if the document requires permissions that the user does not have
   */
  D get(UniqueId uniqueId);

  /**
   * Gets a document by object identifier and version-correction locator.
   * <p>
   * The version-correction will be used to return the correct historic version providing
   * that the master supports history.
   * <p>
   * Access to a document may be controlled by permissions.
   * If the user does not have permission to view the document then an exception is thrown.
   *
   * @param objectId  the object identifier, not null
   * @param versionCorrection  the version-correction locator to search at, not null
   * @return the document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no document with that unique identifier
   * @throws AuthorizationException if the document requires permissions that the user does not have
   */
  D get(ObjectIdentifiable objectId, VersionCorrection versionCorrection);

  /**
   * Gets objects by unique identifier.
   * <p>
   * A unique identifier exactly specifies a single object at a single version-correction.
   * This bulk method is potentially a more efficient form of {@link #get} for multiple lookups.
   * <p>
   * Access to a document may be controlled by permissions.
   * If the user does not have permission to view the document then the document is omitted from the result.
   *
   * @param uniqueIds the unique identifiers to query, not null
   * @return the map of results, if there is no data for an identifier it will be missing from the map, not null
   */
  Map<UniqueId, D> get(Collection<UniqueId> uniqueIds);

  /**
   * Adds a document to the data store.
   * <p>
   * This always adds to the data store, even if the document was previously added.
   * The version instant, correction instant and identifier will be set in the response.
   *
   * @param document  the document, not null
   * @return the added document, may be an update of the input document, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  D add(D document);

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
   * @param document  the document, not null
   * @return the current state of the document, may be an update of the input document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no document with that unique identifier
   */
  D update(D document);
  // TODO: deprecate

  /**
   * Removes a document from the data store.
   * <p>
   * A full master will store detailed historic information on documents.
   * Thus, a removal does not prevent retrieval or correction of an earlier version.
   * <p>
   * If the identifier has a version it must be the latest version.
   *
   * @param oid  the object identifier to remove, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no document with that unique identifier
   */
  void remove(final ObjectIdentifiable oid);

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
   * @param document  the document, not null
   * @return the corrected state of the version, may be an update of the input document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no document with that unique identifier
   */
  D correct(D document);
  // TODO: deprecate

  //-------------------------------------------------------------------------
  /**
   * Replaces a single version of the document in the data store.
   * <p>
   * This applies a correction that replaces a single version in the data store
   * with the specified list.
   * <p>
   * The versioning will be taken from the "version from" instant in each specified document.
   * The "version to" instant and "correction" instants will be ignored on input.
   * If the "version from" instant is null, version from of the replaced document is used
   * No two versioned documents may have the same "version from" instant.
   * The versions must all be within the version range of the original version being replaced.
   * The list can not be empty. 
   * <p>
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
  List<UniqueId> replaceVersion(UniqueId uniqueId, List<D> replacementDocuments);

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
  List<UniqueId> replaceAllVersions(ObjectIdentifiable objectId, List<D> replacementDocuments);

  List<UniqueId> replaceVersions(ObjectIdentifiable objectId, List<D> replacementDocuments);


  //-------------------------------------------------------------------------
  // convenience methods

  /**
   * Replaces a single version of the document in the data store.
   * <p>
   * This applies a correction that replaces a single version in the data store
   * with the specified document.
   * This is equivalent to calling {@link #replaceVersion(UniqueId, List)} with a single
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
  UniqueId replaceVersion(D replacementDocument);
  // NOTE: this is the same as the current correct() method, which should be deprecated, then deleted

  /**
   * Removes a single version of the document from the data store.
   * <p>
   * This applies a correction that replaces a single version in the data store
   * with the specified list.
   * This is equivalent to calling {@link #replace(UniqueId, List)} with a single
   * element list of the specified document.
   * <p>
   * The unique identifier must specify a version of the document that is active when
   * queried with the {@link VersionCorrection latest correction instant}.
   *
   * @param uniqueId  the unique identifier to remove, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no document with that unique identifier
   */
  void removeVersion(UniqueId uniqueId);

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
  UniqueId addVersion(ObjectIdentifiable objectId, D documentToAdd);

}
