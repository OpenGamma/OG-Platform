/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;


import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
/**
 *
 */
public class CurveExposureConfigurationSearchRequest {
  private final ExternalIdBundle _identifiers;

  /**
   *
   */
  public CurveExposureConfigurationSearchRequest(final ExternalId identifier) {
    _identifiers = ExternalIdBundle.of(identifier);
  }

  public CurveExposureConfigurationSearchRequest(final ExternalIdBundle identifiers) {
    _identifiers = identifiers;
  }

  public ExternalIdBundle getIdentifiers() {
    return _identifiers;
  }

}
