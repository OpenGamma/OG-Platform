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
public class EHCachingHolidaySourceTest {

  private HolidaySource _underlyingHolidaySource = null;
  private EHCachingHolidaySource _cachingHolidaySource = null;
  
  private static final LocalDate DATE_MONDAY = LocalDate.of(2010, 10, 25);
  private static final LocalDate DATE_SUNDAY = LocalDate.of(2010, 10, 24);
  private static final Currency GBP = Currency.getInstance("GBP");
  private static final Identifier ID = Identifier.of("C", "D");
  private static final IdentifierBundle BUNDLE = IdentifierBundle.of(ID);

  @Before
  public void setUp() throws Exception {
    _underlyingHolidaySource = mock(HolidaySource.class);
    _cachingHolidaySource = new EHCachingHolidaySource(_underlyingHolidaySource, EHCacheUtils.createCacheManager());
  }

  //-------------------------------------------------------------------------
  
  @Test
  public void isHoliday_dateAndCurrency() {
    when(_underlyingHolidaySource.isHoliday(DATE_MONDAY, GBP)).thenReturn(true);
    when(_underlyingHolidaySource.isHoliday(DATE_SUNDAY, GBP)).thenReturn(false);
    
    assertTrue(_cachingHolidaySource.isHoliday(DATE_MONDAY, GBP));
    assertTrue(_cachingHolidaySource.isHoliday(DATE_MONDAY, GBP));
    assertFalse(_cachingHolidaySource.isHoliday(DATE_SUNDAY, GBP));
    
    verify(_underlyingHolidaySource, times(1)).isHoliday(DATE_MONDAY, GBP);
    verify(_underlyingHolidaySource, times(1)).isHoliday(DATE_SUNDAY, GBP);
  }

  @Test
  public void isHoliday_dateTypeAndBundle() {
    when(_underlyingHolidaySource.isHoliday(DATE_MONDAY, HolidayType.BANK, BUNDLE)).thenReturn(true);
    
    assertTrue(_cachingHolidaySource.isHoliday(DATE_MONDAY, HolidayType.BANK, BUNDLE));
    assertTrue(_cachingHolidaySource.isHoliday(DATE_MONDAY, HolidayType.BANK, BUNDLE));
    
    verify(_underlyingHolidaySource, times(1)).isHoliday(DATE_MONDAY, HolidayType.BANK, BUNDLE);
  }
  
  @Test
  public void isHoliday_dateTypeAndIdentifier() {
    when(_underlyingHolidaySource.isHoliday(DATE_MONDAY, HolidayType.BANK, ID)).thenReturn(true);
    
    assertTrue(_cachingHolidaySource.isHoliday(DATE_MONDAY, HolidayType.BANK, ID));
    assertTrue(_cachingHolidaySource.isHoliday(DATE_MONDAY, HolidayType.BANK, ID));
    
    verify(_underlyingHolidaySource, times(1)).isHoliday(DATE_MONDAY, HolidayType.BANK, ID);
  }
  
}
