/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import java.util.ArrayList;
import java.util.Collection;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class InMemoryCurveExposureConfigurationMaster implements CurveExposureConfigurationMaster {
  public static final ExternalScheme IN_MEMORY_UNIQUE_SCHEME = ExternalScheme.of("In-memory Curve Exposure Configuration");
  private final ExternalIdBundleMapper<CurveExposureConfiguration> _mapper = new ExternalIdBundleMapper<>(IN_MEMORY_UNIQUE_SCHEME.getName());

  /**
   *
   */
  public InMemoryCurveExposureConfigurationMaster() {
    init();
  }

  protected void init() {
    final ExternalIdBundle usd = ExternalIdBundle.of(Currency.OBJECT_SCHEME, "USD");
    final CurveExposureConfiguration usdConfig = new DiscountingMethodCurveExposureConfiguration("USD Discounting Config", usd,
        "Discounting", "DefaultTwoCurveUSDConfig", "Forward3M", "DefaultTwoCurveUSDConfig", "Discounting", "DefaultTwoCurveUSDConfig");
    add(usdConfig);
  }

  @Override
  public CurveExposureConfigurationSearchResult searchCurveExposureConfiguration(final CurveExposureConfigurationSearchRequest searchRequest) {
    final Collection<CurveExposureConfiguration> collection = _mapper.get(searchRequest.getIdentifiers());
    return new CurveExposureConfigurationSearchResult(wrapConfigurationsWithDocuments(collection));
  }

  @Override
  public CurveExposureConfigurationSearchResult searchHistoricalCurveExposureConfiguration(final CurveExposureConfigurationSearchHistoricRequest searchRequest) {
    final Collection<CurveExposureConfiguration> collection = _mapper.get(searchRequest.getIdentifiers());
    return new CurveExposureConfigurationSearchResult(wrapConfigurationsWithDocuments(collection));
  }

  @Override
  public CurveExposureConfigurationDocument getCurveExposureConfiguration(final UniqueId uniqueId) {
    return new CurveExposureConfigurationDocument(_mapper.get(uniqueId));
  }

  @Override
  public UniqueId add(final CurveExposureConfiguration curveExposureConfiguration) {
    final UniqueId uid = _mapper.add(curveExposureConfiguration.getExternalIdBundle(), curveExposureConfiguration);
    curveExposureConfiguration.setUniqueId(uid);
    return uid;
  }

  private Collection<CurveExposureConfigurationDocument> wrapConfigurationsWithDocuments(final Collection<CurveExposureConfiguration> collection) {
    final Collection<CurveExposureConfigurationDocument> results = new ArrayList<>(collection.size());
    for (final CurveExposureConfiguration configuration : collection) {
      results.add(new CurveExposureConfigurationDocument(configuration));
    }
    return results;
  }
}
