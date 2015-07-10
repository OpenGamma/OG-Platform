/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.holiday.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Collections;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.google.common.collect.ImmutableMap;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.HolidaySearchRequest;
import com.opengamma.master.holiday.HolidaySearchResult;
import com.opengamma.master.holiday.ManageableHoliday;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.ThreadLocalServiceContext;
import com.opengamma.service.VersionCorrectionProvider;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

import net.sf.ehcache.CacheManager;

/**
 * Test.
 */
@Test(groups = {TestGroup.UNIT, "ehcache"})
public class EHCachingMasterHolidaySourceTest {

  private static final LocalDate DATE_MONDAY = LocalDate.of(2010, 10, 25);
  private static final LocalDate DATE_TUESDAY = LocalDate.of(2010, 10, 26);
  private static final LocalDate DATE_SUNDAY = LocalDate.of(2010, 10, 24);
  private static final Currency GBP = Currency.GBP;
  private static final ExternalId ID = ExternalId.of("C", "D");
  private static final ExternalIdBundle BUNDLE = ExternalIdBundle.of(ID);

  private HolidayMaster _underlyingHolidayMaster = null;
  private EHCachingMasterHolidaySource _cachingHolidaySource = null;
  private CacheManager _cacheManager;

  @BeforeClass
  public void setUpClass() {
    _cacheManager = EHCacheUtils.createTestCacheManager(EHCachingMasterHolidaySourceTest.class);
    ThreadLocalServiceContext.init(ServiceContext.of(VersionCorrectionProvider.class, new VersionCorrectionProvider() {
      @Override
      public VersionCorrection getPortfolioVersionCorrection() {
        return VersionCorrection.LATEST;
      }

      @Override
      public VersionCorrection getConfigVersionCorrection() {
        return VersionCorrection.LATEST;
      }
    }));  }

  @AfterClass
  public void tearDownClass() {
    EHCacheUtils.shutdownQuiet(_cacheManager);
    ThreadLocalServiceContext.init(ServiceContext.of(ImmutableMap.<Class<?>, Object>of()));
  }

  @BeforeMethod
  public void setUp() {
    _underlyingHolidayMaster = mock(HolidayMaster.class);
    _cachingHolidaySource = new EHCachingMasterHolidaySource(_underlyingHolidayMaster, _cacheManager);
  }

  @AfterMethod
  public void tearDown() {
    EHCacheUtils.clear(_cacheManager, EHCachingMasterHolidaySource.HOLIDAY_CACHE);
  }

  //-------------------------------------------------------------------------
  public void isHoliday_dateAndCurrency() {
    HolidaySearchRequest request = new HolidaySearchRequest(GBP);
    
    ManageableHoliday holiday = new ManageableHoliday(GBP, Collections.singletonList(DATE_MONDAY));
    HolidaySearchResult result = new HolidaySearchResult();
    result.getDocuments().add(new HolidayDocument(holiday));
    
    when(_underlyingHolidayMaster.search(request)).thenReturn(result);
    
    assertTrue(_cachingHolidaySource.isHoliday(DATE_MONDAY, GBP));
    assertTrue(_cachingHolidaySource.isHoliday(DATE_MONDAY, GBP));
    assertFalse(_cachingHolidaySource.isHoliday(DATE_TUESDAY, GBP));
    assertTrue(_cachingHolidaySource.isHoliday(DATE_SUNDAY, GBP)); // weekend
    
    verify(_underlyingHolidayMaster, times(1)).search(request);
  }

  public void isHoliday_dateTypeAndBundle() {
    HolidaySearchRequest request = new HolidaySearchRequest(HolidayType.BANK, BUNDLE);
    
    ManageableHoliday holiday = new ManageableHoliday(GBP, Collections.singletonList(DATE_MONDAY));
    HolidaySearchResult result = new HolidaySearchResult();
    result.getDocuments().add(new HolidayDocument(holiday));
    
    when(_underlyingHolidayMaster.search(request)).thenReturn(result);
    
    assertTrue(_cachingHolidaySource.isHoliday(DATE_MONDAY, HolidayType.BANK, BUNDLE));
    assertTrue(_cachingHolidaySource.isHoliday(DATE_MONDAY, HolidayType.BANK, BUNDLE));
    assertFalse(_cachingHolidaySource.isHoliday(DATE_TUESDAY, HolidayType.BANK, BUNDLE));
    assertTrue(_cachingHolidaySource.isHoliday(DATE_SUNDAY, HolidayType.BANK, BUNDLE)); // weekend
    
    verify(_underlyingHolidayMaster, times(1)).search(request);
  }

  public void isHoliday_dateTypeAndExternalId() {
    HolidaySearchRequest request = new HolidaySearchRequest(HolidayType.BANK, ExternalIdBundle.of(ID));
    
    ManageableHoliday holiday = new ManageableHoliday(GBP, Collections.singletonList(DATE_MONDAY));
    HolidaySearchResult result = new HolidaySearchResult();
    result.getDocuments().add(new HolidayDocument(holiday));
    
    when(_underlyingHolidayMaster.search(request)).thenReturn(result);
    
    assertTrue(_cachingHolidaySource.isHoliday(DATE_MONDAY, HolidayType.BANK, ID));
    assertTrue(_cachingHolidaySource.isHoliday(DATE_MONDAY, HolidayType.BANK, ID));
    assertFalse(_cachingHolidaySource.isHoliday(DATE_TUESDAY, HolidayType.BANK, ID));
    assertTrue(_cachingHolidaySource.isHoliday(DATE_SUNDAY, HolidayType.BANK, ID)); // weekend
    
    verify(_underlyingHolidayMaster, times(1)).search(request);
  }

}
