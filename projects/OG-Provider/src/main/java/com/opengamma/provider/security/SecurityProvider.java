/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.provider.security;

import java.util.Collection;
import java.util.Map;

import com.opengamma.core.security.Security;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.PublicSPI;

/**
 * A provider of security information.
 * <p>
 * This provides access to a data source for security information.
 * For example, major data sources provide details about securities such as equities,
 * indices and bonds.
 * <p>
 * This interface has a minimal and simple API designed to be easy to implement on top
 * of new data providers.
 * <p>
 * This interface is read-only.
 * Implementations must be thread-safe.
 */
@PublicSPI
public interface SecurityProvider {

  /**
   * Gets information about a security from the underlying data source.
   * <p>
   * The security is specified by external identifier bundle.
   * 
   * @param externalIdBundle  the external identifier bundle, not null
   * @return the security information, null if not found
   * @throws RuntimeException if a problem occurs
   */
  Security getSecurity(ExternalIdBundle externalIdBundle);

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
  Map<ExternalIdBundle, Security> getSecurities(Collection<ExternalIdBundle> externalIdBundles);

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
  SecurityProviderResult getSecurities(SecurityProviderRequest request);

}
