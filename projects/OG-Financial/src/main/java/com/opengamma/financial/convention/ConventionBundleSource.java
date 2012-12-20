/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;

/**
 * Source of convention data.
 * <p>
 * An implementation may be backed by a {@link ConventionBundleMaster} or alternative source of reference/convention data.
 */
public interface ConventionBundleSource {

  /**
   * Fetches a set of convention data for the given identifier.
   *
   * @param identifier an identifier of the instrument
   * @return the convention data, or null if not found
   */
  ConventionBundle getConventionBundle(ExternalId identifier);

  /**
   * Fetches a set of convention data for the given instrument identifiers.
   *
   * @param identifiers the identifiers of the instrument
   * @return the convention data, or null if not found
   */
  ConventionBundle getConventionBundle(ExternalIdBundle identifiers);

  /**
   * Fetches a set of convention data by that data's unique identifier.
   *
   * @param identifier the convention data identifier, not null
   * @return the convention data, or null if not found
   */
  ConventionBundle getConventionBundle(UniqueId identifier);

}
