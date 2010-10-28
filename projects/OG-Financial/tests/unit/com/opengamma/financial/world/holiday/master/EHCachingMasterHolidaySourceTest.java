/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.world.holiday.master;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import javax.time.calendar.LocalDate;

import org.junit.Before;
import org.junit.Test;

import com.opengamma.financial.Currency;
import com.opengamma.financial.world.holiday.HolidayType;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * Test EHCachingHolidaySource
 */
public class EHCachingMasterHolidaySourceTest {

  private HolidayMaster _underlyingHolidayMaster = null;
  private EHCachingMasterHolidaySource _cachingHolidaySource = null;
  
  private static final LocalDate DATE_MONDAY = LocalDate.of(2010, 10, 25);
  private static final LocalDate DATE_TUESDAY = LocalDate.of(2010, 10, 26);
  private static final LocalDate DATE_SUNDAY = LocalDate.of(2010, 10, 24);
  private static final Currency GBP = Currency.getInstance("GBP");
  private static final Identifier ID = Identifier.of("C", "D");
  private static final IdentifierBundle BUNDLE = IdentifierBundle.of(ID);

  @Before
  public void setUp() throws Exception {
    EHCacheUtils.clearAll();
    _underlyingHolidayMaster = mock(HolidayMaster.class);
    _cachingHolidaySource = new EHCachingMasterHolidaySource(_underlyingHolidayMaster, EHCacheUtils.createCacheManager());
  }

  //-------------------------------------------------------------------------
  
  @Test
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

  @Test
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
  
  @Test
  public void isHoliday_dateTypeAndIdentifier() {
    HolidaySearchRequest request = new HolidaySearchRequest(HolidayType.BANK, IdentifierBundle.of(ID));
    
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
