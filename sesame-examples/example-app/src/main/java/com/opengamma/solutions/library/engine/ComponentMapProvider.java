/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.solutions.library.engine;

import javax.inject.Inject;

import org.threeten.bp.Instant;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import com.google.inject.Provider;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.ThreadLocalServiceContext;
import com.opengamma.service.VersionCorrectionProvider;
import com.opengamma.sesame.engine.ComponentMap;
import com.opengamma.sesame.engine.FixedInstantVersionCorrectionProvider;
import com.opengamma.util.ArgumentChecker;

/**
 * Provider responsible for creating the {@link ComponentMap}.
 */
public class ComponentMapProvider implements Provider<ComponentMap> {

  private final HolidaySource _holidaySource;
  private final ConfigSource _configSource;
  private final HistoricalTimeSeriesSource _htsSource;
  private final ConventionBundleSource _conventionBundleSource;
  private final HistoricalTimeSeriesResolver _htsResolver;
  private final SecuritySource _securitySource;
  private final ConventionSource _conventionSource;
  private final RegionSource _regionSource;
  private final MarketDataSnapshotSource _snapshotSource;
  private final LegalEntitySource _legalEntitySource;
  private final ExchangeSource _exchangeSource;
  private final PositionSource _positionSource;

  /**
   * @param holidaySource the holiday source
   * @param configSource the config source
   * @param htsSource the hts source
   * @param conventionBundleSource the convention bundle source
   * @param htsResolver the hts resolver to use
   * @param securitySource the security source
   * @param conventionSource the convention source
   * @param regionSource the region source
   * @param positionSource the position source
   * @param snapshotSource the snapshot source
   * @param legalEntitySource the legal entity source
   * @param exchangeSource the exchange source
   */
  @Inject
  public ComponentMapProvider(HolidaySource holidaySource,
                              ConfigSource configSource,
                              HistoricalTimeSeriesSource htsSource,
                              ConventionBundleSource conventionBundleSource,
                              HistoricalTimeSeriesResolver htsResolver,
                              SecuritySource securitySource,
                              ConventionSource conventionSource,
                              RegionSource regionSource,
                              PositionSource positionSource,
                              MarketDataSnapshotSource snapshotSource,
                              LegalEntitySource legalEntitySource,
                              ExchangeSource exchangeSource) {

    _holidaySource = ArgumentChecker.notNull(holidaySource, "holidaySource");
    _configSource = ArgumentChecker.notNull(configSource, "configSource");
    _htsSource = ArgumentChecker.notNull(htsSource, "htsSource");
    _conventionBundleSource = ArgumentChecker.notNull(conventionBundleSource, "conventionBundleSource");
    _htsResolver = ArgumentChecker.notNull(htsResolver, "htsResolver");
    _securitySource = ArgumentChecker.notNull(securitySource, "securitySource");
    _conventionSource = ArgumentChecker.notNull(conventionSource, "conventionSource");
    _regionSource = ArgumentChecker.notNull(regionSource, "regionSource");
    _positionSource = ArgumentChecker.notNull(positionSource, "positionSource");
    _exchangeSource = ArgumentChecker.notNull(exchangeSource, "exchangeSource");
    _legalEntitySource = ArgumentChecker.notNull(legalEntitySource, "legalEntitySource");
    _snapshotSource = ArgumentChecker.notNull(snapshotSource, "snapshotSource");

  }


  @Override
  public ComponentMap get() {
    
    ClassToInstanceMap<Object> components = MutableClassToInstanceMap.create();
    
    components.putInstance(HolidaySource.class, _holidaySource);
    components.putInstance(ConfigSource.class, _configSource);
    components.putInstance(HistoricalTimeSeriesSource.class, _htsSource);
    components.putInstance(ConventionBundleSource.class, _conventionBundleSource);
    components.putInstance(HistoricalTimeSeriesResolver.class, _htsResolver);
    components.putInstance(SecuritySource.class, _securitySource);
    components.putInstance(ConventionSource.class, _conventionSource);
    components.putInstance(RegionSource.class, _regionSource);
    components.putInstance(PositionSource.class, _positionSource);
    components.putInstance(MarketDataSnapshotSource.class, _snapshotSource);
    components.putInstance(LegalEntitySource.class, _legalEntitySource);
    components.putInstance(ExchangeSource.class, _exchangeSource);

    ComponentMap componentMap = ComponentMap.of(components);
    ServiceContext serviceContext = ServiceContext.of(componentMap.getComponents())
        .with(VersionCorrectionProvider.class,
              new FixedInstantVersionCorrectionProvider(Instant.now()));
    ThreadLocalServiceContext.init(serviceContext);

    return componentMap;
  }
  
}
