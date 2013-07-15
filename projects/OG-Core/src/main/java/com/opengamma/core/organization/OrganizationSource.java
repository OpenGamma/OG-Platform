/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.organization;

import com.opengamma.core.Source;
import com.opengamma.core.change.ChangeProvider;
import com.opengamma.util.PublicSPI;

/**
 * A source of organization information as accessed by the main application.
 * <p>
 * This interface provides a simple view of organizations as needed by the engine.
 * This may be backed by a full-featured organization master, or by a much simpler data structure.
 * <p>
 * This interface is read-only.
 * Implementations must be thread-safe.
 */
@PublicSPI
public interface OrganizationSource extends Source<Organization>, ChangeProvider {

  /**
   * Retrieve an organization via its RED code. Returns null if no matching
   * organization is found.
   *
   * @param redCode the code to query by
   * @return the organization with matching RED code, null if not found
   */
  Organization getOrganizationByRedCode(String redCode);

  /**
   * Retrieve an organization via its ticker. Returns null if no matching
   * organization is found.
   *
   * @param ticker the ticker to query by
   * @return the organization with matching ticker, null if not found
   */
  Organization getOrganizationByTicker(String ticker);
}
