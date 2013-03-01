/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security;

import java.util.Map;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.PublicSPI;

/**
 * A tool for loading securities into a master.
 * <p>
 * The security loader provides the functionality to load new securities into the system.
 * The loaded securities will be placed into a master.
 * <p>
 * Implementations will check the master before loading to ensure that the same
 * security is not loaded twice.
 */
@PublicSPI
public interface SecurityLoader {

  /**
   * Ensures information about a single security is available.
   * <p>
   * The security is specified by external identifier bundle.
   * 
   * @param externalIdBundle  the external identifier bundle, not null
   * @return the security information, null if not found
   * @throws RuntimeException if a problem occurs
   */
  UniqueId loadSecurity(ExternalIdBundle externalIdBundle);

  /**
   * Gets information about a collection of securities from the underlying data source.
   * <p>
   * The securities are specified by external identifier bundles.
   * The result is keyed by the input bundles.
   * A missing entry in the result occurs if the security information could not be found
   * 
   * @param externalIdBundles  the external identifier bundles, not null
   * @return the security information, not null
   * @throws RuntimeException if a problem occurs
   */
  Map<ExternalIdBundle, UniqueId> loadSecurities(Iterable<ExternalIdBundle> externalIdBundles);

  /**
   * Gets one or more security information objects from the underlying data source.
   * <p>
   * This is the underlying operation.
   * All other methods delegate to this one.
   * 
   * @param request  the request, not null
   * @return the security information result, not null
   * @throws RuntimeException if a problem occurs
   */
  SecurityLoaderResult loadSecurities(SecurityLoaderRequest request);

}
