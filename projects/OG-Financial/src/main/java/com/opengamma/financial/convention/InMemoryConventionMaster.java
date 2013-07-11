/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import java.util.ArrayList;
import java.util.Collection;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.percurrency.JPConventions;
import com.opengamma.financial.convention.percurrency.USConventions;
import com.opengamma.financial.convention.percurrency.ZAConventions;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.UniqueId;

/**
 *
 */
public class InMemoryConventionMaster implements ConventionMaster {
  /** In-memory scheme */
  public static final ExternalScheme IN_MEMORY_UNIQUE_SCHEME = ExternalScheme.of("In-memory");
  /** Maps from external ids to unique ids */
  private final ExternalIdBundleMapper<Convention> _mapper = new ExternalIdBundleMapper<>(IN_MEMORY_UNIQUE_SCHEME.getName());

  /**
   *
   */
  public InMemoryConventionMaster() {
    init();
  }

  /**
   * Initializes the convention master.
   */
  protected void init() {
    addFXConventions();
    JPConventions.addFixedIncomeInstrumentConventions(this);
    USConventions.addFixedIncomeInstrumentConventions(this);
    ZAConventions.addFixedIncomeInstrumentConventions(this);
  }

  private void addFXConventions() {
    final ExternalId us = ExternalSchemes.financialRegionId("US");
    final BusinessDayConvention following = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
    final FXSpotConvention usdCadSpot = new FXSpotConvention("USD/CAD FX Spot", ExternalIdBundle.of(ExternalId.of("CONVENTION", "USD/CAD FX Spot")), 1, us);
    final FXForwardAndSwapConvention usdCadForward = new FXForwardAndSwapConvention("USD/CAD FX Forward", ExternalIdBundle.of(ExternalId.of("CONVENTION", "USD/CAD FX Forward")),
        ExternalId.of("CONVENTION", "USD/CAD FX Spot"), following, false, us);
    final FXSpotConvention fxSpot = new FXSpotConvention("FX Spot", ExternalIdBundle.of(ExternalId.of("CONVENTION", "FX Spot")), 2, us);
    // TODO: Holiday should not be US only.
    final FXForwardAndSwapConvention fxForward = new FXForwardAndSwapConvention("FX Forward", ExternalIdBundle.of(ExternalId.of("CONVENTION", "FX Forward")),
        ExternalId.of("CONVENTION", "FX Spot"), following, false, us);
    add(usdCadSpot);
    add(usdCadForward);
    add(fxSpot);
    add(fxForward);
  }

  @Override
  public ConventionSearchResult searchConvention(final ConventionSearchRequest searchRequest) {
    final Collection<Convention> collection = _mapper.get(searchRequest.getIdentifiers());
    return new ConventionSearchResult(wrapConventionsWithDocuments(collection));
  }

  @Override
  public ConventionSearchResult searchHistoricalConvention(final ConventionSearchHistoricRequest searchRequest) {
    final Collection<Convention> collection = _mapper.get(searchRequest.getIdentifiers());
    return new ConventionSearchResult(wrapConventionsWithDocuments(collection));
  }

  @Override
  public ConventionDocument getConvention(final UniqueId uniqueId) {
    return new ConventionDocument(_mapper.get(uniqueId));
  }

  @Override
  public UniqueId add(final Convention convention) {
    final UniqueId uid = _mapper.add(convention.getExternalIdBundle(), convention);
    convention.setUniqueId(uid);
    return uid;
  }

  private Collection<ConventionDocument> wrapConventionsWithDocuments(final Collection<Convention> collection) {
    final Collection<ConventionDocument> results = new ArrayList<>(collection.size());
    for (final Convention convention : collection) {
      results.add(new ConventionDocument(convention));
    }
    return results;
  }
}
