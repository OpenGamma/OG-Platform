/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import java.util.Collection;
import java.util.Set;

import javax.time.InstantProvider;

import com.opengamma.DataNotFoundException;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.SecuritySource;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * A security master that can be managed.
 * <p>
 * The security master provides a uniform view over a set of security definitions.
 * This interface provides methods that allow the master to be searched and updated.
 */
public interface ManageableSecurityMaster extends SecuritySource {

  /**
   * Checks if this security master manages the unique identifier.
   * @param uid  the unique identifier, not null
   * @return true if the master manages the identifier
   */
  boolean isManagerFor(UniqueIdentifier uid);

  /**
   * Checks if this security master allows modification of the underlying data source.
   * @return true if the master supports modification
   */
  boolean isModificationSupported();

  //-------------------------------------------------------------------------
  /**
   * Finds a specific security by identifier.
   * @param uid  the unique identifier, null returns null
   * @return the security, null if not found
   * @throws IllegalArgumentException if the identifier is not from this security master
   */
  @Override
  Security getSecurity(UniqueIdentifier uid);

  /**
   * Finds a security by unique identifier at an instant.
   * Any version in the unique identifier is ignored.
   * @param uid  the unique identifier, null returns null
   * @param asAt  obtains the version at the specified instant, not null
   * @param asViewedAt  obtains the version that was viewed at the specified instant, null retrieves the latest
   * @return the security, null if not found
   * @throws IllegalArgumentException if the identifier is not from this security master
   */
  Security getSecurity(UniqueIdentifier uid, InstantProvider asAt, InstantProvider asViewedAt);

  /**
   * Finds all securities that match the specified bundle of keys.
   * If there are none specified, this method must return an
   * empty collection, and not {@code null}.
   * @param secKey  the bundle keys to match, not null
   * @return all securities matching the specified key, empty if no matches, not null
   */
  @Override
  Collection<Security> getSecurities(IdentifierBundle secKey);

  /**
   * Finds the single best-fit security that matches the specified bundle of keys.
   * <p>
   * It is entirely the responsibility of the implementation to determine which
   * security matches best for any given bundle of keys.
   * @param secKey  the bundle keys to match, not null
   * @return the single security matching the bundle of keys, null if not found
   */
  @Override
  Security getSecurity(IdentifierBundle secKey);

  /**
   * Obtain all the available security types in this security master.
   * <p>
   * The implementation should return the available types, however if this is
   * not possible it may return all potential types.
   * @return the set of available security types, not null
   */
  @Override
  Set<String> getAllSecurityTypes();

//  //-------------------------------------------------------------------------
//  /**
//   * Searches for securities matching the request.
//   * 
//   * @param request  the request to add, not null
//   * @return the matched securities, not null
//   * @throws IllegalArgumentException if the request is invalid
//   */
//  SearchSecuritiesResult searchSecurities(final SearchSecuritiesRequest request);
//
//  /**
//   * Gets a managed security.
//   * 
//   * @param securityUid  the unique identifier, not null
//   * @return the security, null if not found
//   * @throws IllegalArgumentException if the request is invalid
//   * @throws DataNotFoundException if the security is not found
//   */
//  ManagedSecurity getManagedSecurity(final UniqueIdentifier securityUid);

  /**
   * Adds a security to the data store.
   * 
   * @param request  the request, not null
   * @return the new unique identifier of the security, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  UniqueIdentifier addSecurity(final AddSecurityRequest request);

  /**
   * Updates a security.
   * 
   * @param request  the request, not null
   * @return the new unique identifier of the security, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if the security is not found
   */
  UniqueIdentifier updateSecurity(final UpdateSecurityRequest request);

  /**
   * Removes a security.
   * <p>
   * If the unique identifier contains a version it must be the latest version.
   * <p>
   * Where possible, implementations should retain the data in such a way that the
   * security can be reinstated.
   * 
   * @param uid  the security unique identifier to remove, not null
   * @return the new unique identifier of the security, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if the security is not found
   */
  UniqueIdentifier removeSecurity(final UniqueIdentifier uid);
  // TODO: remove/reinstate as parameters to update(), as per status of Active/Deleted

//  /**
//   * Reinstates a previously removed security.
//   * <p>
//   * Any version in the unique identifier will be ignored.
//   * 
//   * @param securityUid  the security unique identifier to reinstate, not null
//   * @return the new unique identifier of the security, null if unable to reinstate
//   * @throws IllegalArgumentException if the request is invalid
//   * @throws DataNotFoundException if the security is not found
//   */
//  UniqueIdentifier reinstateSecurity(final UniqueIdentifier securityUid);

}
