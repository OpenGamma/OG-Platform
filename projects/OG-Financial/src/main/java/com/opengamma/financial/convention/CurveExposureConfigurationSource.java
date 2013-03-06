/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;

/**
 *
 */
public interface CurveExposureConfigurationSource {

  CurveExposureConfiguration getCurveExposureConfiguration(ExternalId identifier, String configurationType);

  CurveExposureConfiguration getCurveExposureConfiguration(ExternalIdBundle identifiers, String configurationType);

  CurveExposureConfiguration getCurveExposureConfiguration(UniqueId identifier, String configurationType);

  <T extends CurveExposureConfiguration> T getCurveExposureConfiguration(ExternalId identifier, Class<T> type);

  <T extends CurveExposureConfiguration> T getCurveExposureConfiguration(ExternalIdBundle identifiers, Class<T> type);

  <T extends CurveExposureConfiguration> T getCurveExposureConfiguration(UniqueId identifier, Class<T> type);
}
