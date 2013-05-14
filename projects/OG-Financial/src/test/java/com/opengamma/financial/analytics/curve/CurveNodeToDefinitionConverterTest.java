/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.core.holiday.impl.SimpleHoliday;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.region.impl.SimpleRegion;
import com.opengamma.financial.convention.CMSLegConvention;
import com.opengamma.financial.convention.CompoundingIborLegConvention;
import com.opengamma.financial.convention.Convention;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.EquityConvention;
import com.opengamma.financial.convention.FXForwardAndSwapConvention;
import com.opengamma.financial.convention.FXSpotConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.InterestRateFutureConvention;
import com.opengamma.financial.convention.OISLegConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class CurveNodeToDefinitionConverterTest {

  private static class MyConventionSource implements ConventionSource {

    @Override
    public Convention getConvention(final ExternalId identifier) {
      return null;
    }

    @Override
    public CMSLegConvention getCMSLegConvention(final ExternalId identifier) {
      return null;
    }

    @Override
    public CompoundingIborLegConvention getCompoundingIborLegConvention(final ExternalId identifier) {
      return null;
    }

    @Override
    public DepositConvention getDepositConvention(final ExternalId identifier) {
      return null;
    }

    @Override
    public EquityConvention getEquityConvention(final ExternalId identifier) {
      return null;
    }

    @Override
    public InterestRateFutureConvention getInterestRateFutureConvention(final ExternalId identifier) {
      return null;
    }

    @Override
    public FXForwardAndSwapConvention getFXForwardAndSwapConvention(final ExternalId identifier) {
      return null;
    }

    @Override
    public FXSpotConvention getFXSpotConvention(final ExternalId identifier) {
      return null;
    }

    @Override
    public IborIndexConvention getIborIndexConvention(final ExternalId identifier) {
      return null;
    }

    @Override
    public OISLegConvention getOISLegConvention(final ExternalId identifier) {
      return null;
    }

    @Override
    public OvernightIndexConvention getOvernightIndexConvention(final ExternalId identifier) {
      return null;
    }

  }

  private static class MyHolidaySource implements HolidaySource {
    private final Calendar _calendar;
    private final Currency _currency;
    private final ExternalIdBundle _regionIds;
    private final ExternalId _regionId;
    private final UniqueId _uniqueId;
    private final SimpleHoliday _holiday;

    public MyHolidaySource(final Calendar calendar, final Currency currency, final String country) {
      _calendar = calendar;
      _currency = currency;
      _regionId = ExternalId.of(ExternalSchemes.ISO_COUNTRY_ALPHA2, country);
      _regionIds = ExternalIdBundle.of(_regionId);
      _holiday = new SimpleHoliday();
      _uniqueId = UniqueId.of(_regionId);
      _holiday.setUniqueId(_uniqueId);
    }

    @Override
    public Holiday get(final UniqueId uniqueId) {
      if (uniqueId.equals(_uniqueId)) {
        return _holiday;
      }
      return null;
    }

    @Override
    public Holiday get(final ObjectId objectId, final VersionCorrection versionCorrection) {
      if (objectId.equals(_regionId)) {
        return _holiday;
      }
      return null;
    }

    @Override
    public Map<UniqueId, Holiday> get(final Collection<UniqueId> uniqueIds) {
      for (final UniqueId uniqueId : uniqueIds) {
        if (uniqueId.equals(_uniqueId)) {
          return Collections.<UniqueId, Holiday>singletonMap(uniqueId, _holiday);
        }
      }
      return Collections.emptyMap();
    }

    @Override
    public Map<ObjectId, Holiday> get(final Collection<ObjectId> objectIds, final VersionCorrection versionCorrection) {
      for (final ObjectId objectId : objectIds) {
        if (objectId.equals(_regionId)) {
          return Collections.<ObjectId, Holiday>singletonMap(objectId, _holiday);
        }
      }
      return Collections.emptyMap();
    }

    @Override
    public boolean isHoliday(final LocalDate dateToCheck, final Currency currency) {
      if (!currency.equals(_currency)) {
        throw new OpenGammaRuntimeException("Do not have calendar for " + currency);
      }
      return !_calendar.isWorkingDay(dateToCheck);
    }

    @Override
    public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType, final ExternalIdBundle regionOrExchangeIds) {
      if (!regionOrExchangeIds.equals(_regionIds)) {
        throw new OpenGammaRuntimeException("Do not have calendar for " + regionOrExchangeIds);
      }
      return !_calendar.isWorkingDay(dateToCheck);
    }

    @Override
    public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType, final ExternalId regionOrExchangeId) {
      if (!regionOrExchangeId.equals(_regionId)) {
        throw new OpenGammaRuntimeException("Do not have calendar for " + regionOrExchangeId);
      }
      return !_calendar.isWorkingDay(dateToCheck);    }

  }

  private static class MyRegionSource implements RegionSource {
    private final ExternalIdBundle _regionBundle;
    private final Region _region;

    public MyRegionSource(final String countryId) {
      final SimpleRegion region = new SimpleRegion();
      final ExternalId id = ExternalId.of(ExternalSchemes.ISO_COUNTRY_ALPHA2, countryId);
      region.addExternalId(id);
      region.setUniqueId(UniqueId.of(id));
      _regionBundle = ExternalIdBundle.of(id);
      _region = region;
    }

    @Override
    public Collection<Region> get(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
      return Collections.singleton(_region);
    }

    @Override
    public Map<ExternalIdBundle, Collection<Region>> getAll(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
      for (final ExternalIdBundle bundle : bundles) {
        if(bundle.equals(_regionBundle)) {
          return Collections.<ExternalIdBundle, Collection<Region>>singletonMap(_regionBundle, Collections.singleton(_region));
        }
      }
      return Collections.emptyMap();
    }

    @Override
    public Collection<Region> get(final ExternalIdBundle bundle) {
      if (bundle.equals(_regionBundle)) {
        return Collections.singleton(_region);
      }
      return Collections.emptySet();
    }

    @Override
    public Region getSingle(final ExternalIdBundle bundle) {
      if (bundle.equals(_regionBundle)) {
        return _region;
      }
      return null;
    }

    @Override
    public Region getSingle(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
      if (bundle.equals(_regionBundle)) {
        return _region;
      }
      return null;
    }

    @Override
    public Map<ExternalIdBundle, Region> getSingle(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
      for (final ExternalIdBundle bundle : bundles) {
        if(bundle.equals(_regionBundle)) {
          return Collections.<ExternalIdBundle, Region>singletonMap(_regionBundle, _region);
        }
      }
      return Collections.emptyMap();
    }

    @Override
    public Region get(final UniqueId uniqueId) {
      if (uniqueId.equals(_region.getUniqueId())) {
        return _region;
      }
      return null;
    }

    @Override
    public Region get(final ObjectId objectId, final VersionCorrection versionCorrection) {
      if (objectId.atLatestVersion().equals(_region.getUniqueId())) {
        return _region;
      }
      return null;
    }

    @Override
    public Map<UniqueId, Region> get(final Collection<UniqueId> uniqueIds) {
      for (final UniqueId uniqueId : uniqueIds) {
        if (uniqueId.equals(_region.getUniqueId())) {
          return Collections.singletonMap(uniqueId, _region);
        }
      }
      return null;
    }

    @Override
    public Map<ObjectId, Region> get(final Collection<ObjectId> objectIds, final VersionCorrection versionCorrection) {
      for (final ObjectId objectId : objectIds) {
        if (objectId.atLatestVersion().equals(_region.getUniqueId())) {
          return Collections.singletonMap(objectId, _region);
        }
      }
      return null;
    }

    @Override
    public ChangeManager changeManager() {
      return DummyChangeManager.INSTANCE;
    }

    @Override
    public Region getHighestLevelRegion(final ExternalId externalId) {
      return _region;
    }

    @Override
    public Region getHighestLevelRegion(final ExternalIdBundle bundle) {
      return _region;
    }

  }
}
