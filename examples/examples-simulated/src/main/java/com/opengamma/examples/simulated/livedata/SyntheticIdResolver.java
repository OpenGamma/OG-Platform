/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.examples.simulated.livedata;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.livedata.resolver.IdResolver;

/**
 * Resolves identifiers to the object identifiers used in the OG-Example simulated feed. 
 */
public class SyntheticIdResolver implements IdResolver {

  @Override
  public ExternalId resolve(final ExternalIdBundle ids) {
    final ExternalId id = ids.getExternalId(ExternalSchemes.OG_SYNTHETIC_TICKER);
    if (id != null) {
      return id;
    }
    return null;
  }

  @Override
  public Map<ExternalIdBundle, ExternalId> resolve(final Collection<ExternalIdBundle> ids) {
    final Map<ExternalIdBundle, ExternalId> result = Maps.newHashMapWithExpectedSize(ids.size());
    for (ExternalIdBundle id : ids) {
      result.put(id, resolve(id));
    }
    return result;
  }

}
