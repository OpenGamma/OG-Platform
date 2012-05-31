/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.holiday.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertSame;
import net.sf.ehcache.CacheManager;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * Test {@link EHCachingHolidaySource}.
 */
@Test
public class EHCachingHolidaySourceTest {

  private HolidaySource _underlyingSource;
  private EHCachingHolidaySource _cachingSource;

  private static final UniqueId UID = UniqueId.of("A", "B");
  private static final ObjectId OID = ObjectId.of("A", "B");
  private static final VersionCorrection VC = VersionCorrection.LATEST;

  @BeforeMethod
  public void setUp() throws Exception {
    EHCacheUtils.clearAll();
    _underlyingSource = mock(HolidaySource.class);
    CacheManager cm = EHCacheUtils.createCacheManager();
    _cachingSource = new EHCachingHolidaySource(_underlyingSource, cm);
  }

  public void getHoliday_uniqueId() {
    final Holiday h = new SimpleHoliday();
    when(_underlyingSource.getHoliday(UID)).thenReturn(h);
    assertSame(_cachingSource.getHoliday(UID), h);
    assertSame(_cachingSource.getHoliday(UID), h);
    verify(_underlyingSource, times(1)).getHoliday(UID);
  }

  public void getHoliday_objectId() {
    final Holiday h = new SimpleHoliday();
    when(_underlyingSource.getHoliday(OID, VC)).thenReturn(h);
    assertSame(_cachingSource.getHoliday(OID, VC), h);
    assertSame(_cachingSource.getHoliday(OID, VC), h);
    verify(_underlyingSource, times(1)).getHoliday(OID, VC);
  }

}
