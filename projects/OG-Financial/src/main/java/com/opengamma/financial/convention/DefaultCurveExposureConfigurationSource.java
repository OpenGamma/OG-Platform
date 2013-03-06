/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class DefaultCurveExposureConfigurationSource implements CurveExposureConfigurationSource {
  private final CurveExposureConfigurationMaster _configurationMaster;

  /**
   *
   */
  public DefaultCurveExposureConfigurationSource(final CurveExposureConfigurationMaster configurationMaster) {
    ArgumentChecker.notNull(configurationMaster, "configuration master");
    _configurationMaster = configurationMaster;
  }

  @Override
  public CurveExposureConfiguration getCurveExposureConfiguration(final ExternalId identifier, final String configurationType) {
    final CurveExposureConfigurationSearchResult result = _configurationMaster.searchCurveExposureConfiguration(new CurveExposureConfigurationSearchRequest(identifier));
    for (final CurveExposureConfigurationDocument configurationDocument : result.getResults()) {
      if (configurationDocument.getValue().getConfigurationName().equals(configurationType)) {
        return configurationDocument.getValue();
      }
    }
    return null;
  }

  @Override
  public CurveExposureConfiguration getCurveExposureConfiguration(final ExternalIdBundle identifiers, final String configurationType) {
    final CurveExposureConfigurationSearchResult result = _configurationMaster.searchCurveExposureConfiguration(new CurveExposureConfigurationSearchRequest(identifiers));
    for (final CurveExposureConfigurationDocument configurationDocument : result.getResults()) {
      if (configurationDocument.getValue().getConfigurationName().equals(configurationType)) {
        return configurationDocument.getValue();
      }
    }
    return null;
  }

  @Override
  public CurveExposureConfiguration getCurveExposureConfiguration(final UniqueId identifier, final String configurationType) {
    final CurveExposureConfigurationDocument result = _configurationMaster.getCurveExposureConfiguration(identifier);
    if (result.getValue().getConfigurationName().equals(configurationType)) {
      return result.getValue();
    }
    return null;
  }

  @Override
  public <T extends CurveExposureConfiguration> T getCurveExposureConfiguration(final ExternalId identifier, final Class<T> type) {
    final CurveExposureConfigurationSearchResult result = _configurationMaster.searchCurveExposureConfiguration(new CurveExposureConfigurationSearchRequest(identifier));
    for (final CurveExposureConfigurationDocument configurationDocument : result.getResults()) {
      if (configurationDocument.getValue().getClass().equals(type)) {
        return (T) configurationDocument.getValue();
      }
    }
    return null;
  }

  @Override
  public <T extends CurveExposureConfiguration> T getCurveExposureConfiguration(final ExternalIdBundle identifiers, final Class<T> type) {
    final CurveExposureConfigurationSearchResult result = _configurationMaster.searchCurveExposureConfiguration(new CurveExposureConfigurationSearchRequest(identifiers));
    for (final CurveExposureConfigurationDocument configurationDocument : result.getResults()) {
      if (configurationDocument.getValue().equals(type)) {
        return (T) configurationDocument.getValue();
      }
    }
    return null;
  }

  @Override
  public <T extends CurveExposureConfiguration> T getCurveExposureConfiguration(final UniqueId identifier, final Class<T> type) {
    final CurveExposureConfigurationDocument result = _configurationMaster.getCurveExposureConfiguration(identifier);
    if (result.getValue().equals(type)) {
      return (T) result.getValue();
    }
    return null;
  }

}
