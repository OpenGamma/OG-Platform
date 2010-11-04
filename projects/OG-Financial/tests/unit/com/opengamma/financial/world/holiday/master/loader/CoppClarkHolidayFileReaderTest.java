/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.world.holiday.master.loader;

import java.net.URISyntaxException;

import javax.time.calendar.LocalDate;

import junit.framework.Assert;

import org.junit.Test;

import com.opengamma.financial.Currency;
import com.opengamma.financial.world.exchange.ExchangeUtils;
import com.opengamma.financial.world.exchange.master.ManageableExchange;
import com.opengamma.financial.world.exchange.master.MasterExchangeSource;
import com.opengamma.financial.world.exchange.master.loader.CoppClarkExchangeFileReader;
import com.opengamma.financial.world.holiday.HolidayType;
import com.opengamma.financial.world.holiday.master.HolidaySource;
import com.opengamma.financial.world.holiday.master.loader.CoppClarkHolidayFileReader;
import com.opengamma.financial.world.holiday.master.memory.InMemoryHolidayMaster;
import com.opengamma.financial.world.region.master.MasterRegionSource;
import com.opengamma.financial.world.region.master.RegionSource;
import com.opengamma.financial.world.region.master.loader.RegionFileReader;
import com.opengamma.financial.world.region.master.memory.InMemoryRegionMaster;
import com.opengamma.id.Identifier;

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
