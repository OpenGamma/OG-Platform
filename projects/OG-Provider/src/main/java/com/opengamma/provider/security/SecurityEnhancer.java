/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.provider.security;

import java.util.List;
import java.util.Map;

import com.opengamma.core.security.Security;
import com.opengamma.util.PublicSPI;

/**
 * An enhancer that can add additional information to a security.
 * <p>
 * This provides a mechanism for a data source to add additional information to
 * a security that has already been loaded.
 * For example, it may be desirable to loaded the security from one data source and
 * then add extra external IDs from one or more different data sources.
 * <p>
 * This interface has a minimal and simple API designed to be easy to implement on top
 * of new data providers.
 * <p>
 * This interface is read-only.
 * Implementations must be thread-safe.
 */
@PublicSPI
public interface SecurityEnhancer {

  /**
   * Enhances the information about a the specified security from the underlying data source.
   * <p>
   * The security is specified by external identifier bundle.
   * 
   * @param security  the security to enhance, not null
   * @return the enhanced security, not null
   * @throws RuntimeException if a problem occurs
   */
  Security enhanceSecurity(Security security);

  /**
   * Enhances the information about a the specified securities from the underlying data source.
   * <p>
   * Each security in the supplied list is enhanced.
   * The result is of the same size and order as the input.
   * 
   * @param securities  the securities to enhance, not null
   * @return the enhanced securities, not null
   * @throws RuntimeException if a problem occurs
   */
  List<Security> enhanceSecurities(List<Security> securities);

  /**
   * Enhances the information about a the specified securities from the underlying data source.
   * <p>
   * Each security in the supplied map is enhanced.
   * The map key has no effect on enhancement.
   * The result is keyed by the same key as the input.
   * 
   * @param <R>  the type of the map key
   * @param securities  the securities to enhance, not null
   * @return the enhanced securities, not null
   * @throws RuntimeException if a problem occurs
   */
  <R> Map<R, Security> enhanceSecurities(Map<R, Security> securities);

  /**
   * Enhances one or more security information objects from the underlying data source.
   * <p>
   * This is the underlying operation.
   * All other methods delegate to this one.
   * 
   * @param request  the request, not null
   * @return the enhanced security result, not null
   * @throws RuntimeException if a problem occurs
   */
  SecurityEnhancerResult enhanceSecurities(SecurityEnhancerRequest request);

}
