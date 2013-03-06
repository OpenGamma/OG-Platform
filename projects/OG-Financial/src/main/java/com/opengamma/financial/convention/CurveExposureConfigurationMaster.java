/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import com.opengamma.id.UniqueId;

/**
 *
 */
public interface CurveExposureConfigurationMaster {

  CurveExposureConfigurationSearchResult searchCurveExposureConfiguration(CurveExposureConfigurationSearchRequest searchRequest);

  CurveExposureConfigurationSearchResult searchHistoricalCurveExposureConfiguration(CurveExposureConfigurationSearchHistoricRequest searchRequest);

  CurveExposureConfigurationDocument getCurveExposureConfiguration(UniqueId uniqueId);

  UniqueId add(CurveExposureConfiguration curveExposureConfiguration);
}
