/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.holiday.impl;

import java.net.URISyntaxException;

import javax.time.calendar.LocalDate;

import junit.framework.Assert;

import org.junit.Test;

import com.opengamma.core.common.Currency;
import com.opengamma.core.exchange.ExchangeUtils;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.core.region.RegionSource;
import com.opengamma.id.Identifier;
import com.opengamma.master.exchange.ManageableExchange;
import com.opengamma.master.exchange.impl.CoppClarkExchangeFileReader;
import com.opengamma.master.exchange.impl.MasterExchangeSource;
import com.opengamma.master.holiday.impl.CoppClarkHolidayFileReader;
import com.opengamma.master.holiday.impl.InMemoryHolidayMaster;
import com.opengamma.master.region.impl.InMemoryRegionMaster;
import com.opengamma.master.region.impl.MasterRegionSource;
import com.opengamma.master.region.impl.RegionFileReader;

/**
 * Test CoppClarkHolidayFileReader.
 */
public class CoppClarkHolidayFileReaderTest {

  private static Identifier EURONEXT_LIFFE = Identifier.of(ExchangeUtils.ISO_MIC, "XLIF");

  @Test
  public void testHolidayRespository() throws URISyntaxException {
    HolidaySource holidaySource = CoppClarkHolidayFileReader.createPopulated(new InMemoryHolidayMaster()); 
    
    Assert.assertTrue(holidaySource.isHoliday(LocalDate.of(2012, 06, 05), HolidayType.SETTLEMENT, EURONEXT_LIFFE));
    Assert.assertFalse(holidaySource.isHoliday(LocalDate.of(2012, 06, 06), HolidayType.SETTLEMENT, EURONEXT_LIFFE));
    Assert.assertTrue(holidaySource.isHoliday(LocalDate.of(2012, 06, 05), HolidayType.TRADING, EURONEXT_LIFFE));
    Assert.assertFalse(holidaySource.isHoliday(LocalDate.of(2012, 06, 06), HolidayType.TRADING, EURONEXT_LIFFE));
    
    MasterExchangeSource exchangeSource = CoppClarkExchangeFileReader.createPopulated().getExchangeSource();
    ManageableExchange euronextLiffe = exchangeSource.getSingleExchange(EURONEXT_LIFFE);
    Assert.assertNotNull(euronextLiffe);
    Assert.assertTrue(holidaySource.isHoliday(LocalDate.of(2012, 06, 05), HolidayType.BANK, euronextLiffe.getRegionId()));
    Assert.assertFalse(holidaySource.isHoliday(LocalDate.of(2012, 06, 06), HolidayType.BANK, euronextLiffe.getRegionId()));
    
    InMemoryRegionMaster regionMaster = new InMemoryRegionMaster();
    RegionFileReader.populate(regionMaster);
    RegionSource regionSource = new MasterRegionSource(regionMaster);
    Currency currency = regionSource.getHighestLevelRegion(euronextLiffe.getRegionId()).getCurrency();
    Assert.assertTrue(holidaySource.isHoliday(LocalDate.of(2012, 06, 05), currency));
    Assert.assertFalse(holidaySource.isHoliday(LocalDate.of(2012, 06, 06), currency));
  }

}
