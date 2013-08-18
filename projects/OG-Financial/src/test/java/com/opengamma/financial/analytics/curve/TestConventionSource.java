/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import java.util.Map;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.convention.Convention;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;

/**
 *
 */
public class TestConventionSource implements ConventionSource {
  private final Map<ExternalId, Convention> _conventions;

  public TestConventionSource(final Map<ExternalId, Convention> conventions) {
    _conventions = conventions;
  }

  @Override
  public Convention getConvention(final ExternalId identifier) {
    return _conventions.get(identifier);
  }

  @Override
  public Convention getConvention(final ExternalIdBundle identifiers) {
    return null;
  }

  @Override
  public Convention getConvention(final UniqueId identifier) {
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends Convention> T getConvention(final Class<T> clazz, final ExternalId identifier) {
    final Convention convention = _conventions.get(identifier);
    if (convention == null) {
      return null;
    }
    if (clazz.isAssignableFrom(convention.getClass())) {
      return (T) convention;
    }
    throw new OpenGammaRuntimeException("Convention for " + identifier + " was not of expected type " + clazz);
  }

  @Override
  public <T extends Convention> T getConvention(final Class<T> clazz, final ExternalIdBundle identifiers) {
    return null;
  }

  @Override
  public <T extends Convention> T getConvention(final Class<T> clazz, final UniqueId identifier) {
    return null;
  }

}
