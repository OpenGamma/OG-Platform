/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.financial.timeseries.exchange.ExchangeDataProvider;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Tool for loading bloomberg security definitions in bulk.
 */
public class BloombergBulkSecurityLoader {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergBulkSecurityLoader.class);

  private final Set<SecurityType> _ignoredSecurityTypes;
  private final Map<SecurityType, SecurityLoader> _securityLoaderMap;

  private final ReferenceDataProvider _referenceDataProvider;
  private final ExchangeDataProvider _exchangeDataProvider;
  private final SecurityTypeResolver _bbgSecurityTypeResolver;

  /**
   * @param referenceDataProvider the reference data provider, not-null
   * @param exchangeDataProvider the exchange data provider, not-null
   */
  public BloombergBulkSecurityLoader(final ReferenceDataProvider referenceDataProvider, final ExchangeDataProvider exchangeDataProvider) {
    ArgumentChecker.notNull(referenceDataProvider, "ReferenceDataProvider");
    ArgumentChecker.notNull(exchangeDataProvider, "Exchange Data Provider");
    _exchangeDataProvider = exchangeDataProvider;
    _referenceDataProvider = referenceDataProvider;
    _bbgSecurityTypeResolver = new BloombergSecurityTypeResolver(referenceDataProvider);
    _securityLoaderMap = createSecurityLoaders();
    _ignoredSecurityTypes = getIgnoredSecurityTypes();
  }

  private Map<SecurityType, SecurityLoader> createSecurityLoaders() {
    final ImmutableMap.Builder<SecurityType, SecurityLoader> mapBuilder = ImmutableMap.builder();

    addLoader(mapBuilder, new AgricultureFutureLoader(_referenceDataProvider));
    addLoader(mapBuilder, new BillLoader(_referenceDataProvider));
    addLoader(mapBuilder, new BondFutureLoader(_referenceDataProvider));
    addLoader(mapBuilder, new BondLoader(_referenceDataProvider));
    addLoader(mapBuilder, new EnergyFutureLoader(_referenceDataProvider));
    addLoader(mapBuilder, new EquityDividendFutureLoader(_referenceDataProvider));
    addLoader(mapBuilder, new EquityFutureLoader(_referenceDataProvider));
    addLoader(mapBuilder, new EquityIndexOptionLoader(_referenceDataProvider));
    addLoader(mapBuilder, new EquityLoader(_referenceDataProvider, _exchangeDataProvider));
    addLoader(mapBuilder, new EquityOptionLoader(_referenceDataProvider));
    addLoader(mapBuilder, new FXFutureLoader(_referenceDataProvider));
    addLoader(mapBuilder, new IndexFutureLoader(_referenceDataProvider));
    addLoader(mapBuilder, new IndexLoader(_referenceDataProvider));
    addLoader(mapBuilder, new InterestRateFutureLoader(_referenceDataProvider));
    addLoader(mapBuilder, new IRFutureOptionLoader(_referenceDataProvider));
    addLoader(mapBuilder, new BondFutureOptionLoader(_referenceDataProvider));
    addLoader(mapBuilder, new CommodityFutureOptionLoader(_referenceDataProvider));
    addLoader(mapBuilder, new FxFutureOptionLoader(_referenceDataProvider));
    addLoader(mapBuilder, new MetalFutureLoader(_referenceDataProvider));
    addLoader(mapBuilder, new EquityIndexFutureOptionLoader(_referenceDataProvider));
    addLoader(mapBuilder, new EquityIndexDividendFutureOptionLoader(_referenceDataProvider));
    return mapBuilder.build();
  }

  private void addLoader(final ImmutableMap.Builder<SecurityType, SecurityLoader> builder, final SecurityLoader securityLoader) {
    builder.put(securityLoader.getSecurityType(), securityLoader);
  }

  private Set<SecurityType> getIgnoredSecurityTypes() {
    return ImmutableSet.of(
        SecurityType.EQUITY_INDEX,
        SecurityType.RATE);
  }

  public Map<ExternalIdBundle, ManageableSecurity> loadSecurity(final Collection<ExternalIdBundle> identifiers) {
    ArgumentChecker.notNull(identifiers, "identifiers");
    final Map<ExternalIdBundle, ManageableSecurity> result = Maps.newHashMap();

    if (identifiers.isEmpty()) {
      return result;
    }

    // [BBG-87] - Convert to BUIDs
    final BiMap<String, ExternalIdBundle> bundle2Bbgkey = BloombergDataUtils.convertToBloombergBuidKeys(identifiers, _referenceDataProvider);

    final Map<SecurityType, Set<String>> securitiesByType = groupBySecurityType(_bbgSecurityTypeResolver.getSecurityType(identifiers), bundle2Bbgkey.inverse());
    for (final Entry<SecurityType, Set<String>> entry : securitiesByType.entrySet()) {
      final SecurityType secType = entry.getKey();
      final Set<String> bbgkeys = entry.getValue();
      if (_ignoredSecurityTypes.contains(secType)) {
        s_logger.info("Skipping securities {} of type {} which does not require loading", bbgkeys, secType);
        continue;
      }
      final SecurityLoader loader = _securityLoaderMap.get(secType);
      if (loader == null) {
        s_logger.warn("Unable to load security type {} mapped from {} as no loader is registered", secType, bbgkeys);
        continue;
      }
      final Map<String, ManageableSecurity> securities = loader.loadSecurities(bbgkeys);
      for (final Entry<String, ManageableSecurity> secEntry : securities.entrySet()) {
        final ExternalIdBundle identifierBundle = bundle2Bbgkey.get(secEntry.getKey());
        result.put(identifierBundle, secEntry.getValue());
      }
    }
    return result;
  }

  private Map<SecurityType, Set<String>> groupBySecurityType(final Map<ExternalIdBundle, SecurityType> securityTypeResult, final BiMap<ExternalIdBundle, String> bundle2bbgKey) {
    final Map<SecurityType, Set<String>> result = Maps.newHashMap();
    for (final Entry<ExternalIdBundle, SecurityType> entry : securityTypeResult.entrySet()) {
      final SecurityType securityType = entry.getValue();
      Set<String> bbgKeys = result.get(securityType);
      if (bbgKeys == null) {
        bbgKeys = Sets.newHashSet();
        result.put(securityType, bbgKeys);
      }
      bbgKeys.add(bundle2bbgKey.get(entry.getKey()));
    }
    return result;
  }

}
