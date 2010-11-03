/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import java.net.URISyntaxException;
import java.util.Arrays;

import javax.time.calendar.LocalDate;

import junit.framework.Assert;

import org.junit.Test;

import com.opengamma.financial.world.exchange.ExchangeUtils;
import com.opengamma.financial.world.exchange.coppclark.CoppClarkExchangeFileReader;
import com.opengamma.financial.world.exchange.master.ExchangeDocument;
import com.opengamma.financial.world.exchange.master.ManageableExchange;
import com.opengamma.financial.world.exchange.master.MasterExchangeSource;
import com.opengamma.financial.world.exchange.master.memory.InMemoryExchangeMaster;
import com.opengamma.financial.world.holiday.HolidayType;
import com.opengamma.financial.world.holiday.coppclark.CoppClarkHolidayFileReader;
import com.opengamma.financial.world.holiday.master.HolidaySource;
import com.opengamma.financial.world.holiday.master.memory.InMemoryHolidayMaster;
import com.opengamma.financial.world.region.Region;
import com.opengamma.financial.world.region.RegionUtils;
import com.opengamma.financial.world.region.master.MasterRegionSource;
import com.opengamma.financial.world.region.master.RegionMaster;
import com.opengamma.financial.world.region.master.RegionSource;
import com.opengamma.financial.world.region.master.loader.RegionFileReader;
import com.opengamma.financial.world.region.master.memory.InMemoryRegionMaster;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;

/**
 * Unit tests for {@see InMemoryHolidayRepository} and {@see InMemoryExchangeRepository}
 */
public class InMemoryHolidayAndExchangeRespositoriesTest {
//  private static final String REGION_HIERARCHY = InMemoryRegionMaster.POLITICAL_HIERARCHY_NAME;
  
  @Test
  public void testExchangeRepository() throws URISyntaxException {
    RegionMaster regionMaster = new InMemoryRegionMaster();
    RegionFileReader.populate(regionMaster);
    RegionSource regionSource = new MasterRegionSource(regionMaster);
    InMemoryExchangeMaster exchangeRepo = new InMemoryExchangeMaster();
    MasterExchangeSource exchangeSource = new MasterExchangeSource(exchangeRepo);
    
    Region uk = regionSource.getHighestLevelRegion(RegionUtils.countryRegionId("GB"));
    
    Identifier euronextLiffeMIC = Identifier.of(ExchangeUtils.ISO_MIC, "XLIF");
    Identifier euronextLiffeCCID = Identifier.of(ExchangeUtils.COPP_CLARK_CENTER_ID, "979");
    Identifier euronextLiffeCCName = Identifier.of(ExchangeUtils.COPP_CLARK_NAME, "Euronext LIFFE (UK contracts)");
    Identifier euronextLiffeExtra = Identifier.of("TEST_SCHEME", "EURONEXT LIFFE");
    IdentifierBundle euronextLiffeIDs = new IdentifierBundle(Arrays.asList(new Identifier[] { euronextLiffeMIC, euronextLiffeCCID, euronextLiffeCCName }));
    IdentifierBundle euronextLiffeIDsWithExtra = new IdentifierBundle(Arrays.asList(new Identifier[] { euronextLiffeExtra, euronextLiffeMIC, euronextLiffeCCName }));
    
//    Identifier gemmaMIC = Identifier.of(ExchangeUtils.ISO_MIC, "GEMX");
//    Identifier gemmaCCID = Identifier.of(ExchangeUtils.COPP_CLARK_CENTER_ID, "1063");
//    Identifier gemmaCCName = Identifier.of(ExchangeUtils.COPP_CLARK_CENTER_ID, "GEMMA Gilt Edged Market Makers Association");
//    IdentifierBundle gemmaIDs = new IdentifierBundle(Arrays.asList(new Identifier[] { gemmaMIC, gemmaCCID, gemmaCCName }));
    
//    Identifier americanStockExchangeMIC = Identifier.of(ExchangeUtils.ISO_MIC, "XASE");
//    Identifier americanStockExchangeCCID = Identifier.of(ExchangeUtils.COPP_CLARK_CENTER_ID, "784");
//    Identifier americanStockExchangeCCName = Identifier.of(ExchangeUtils.COPP_CLARK_NAME, "American Stock Exchange");
//    IdentifierBundle americanStockExchangeIDs = new IdentifierBundle(
//        Arrays.asList(new Identifier[] { americanStockExchangeMIC, americanStockExchangeCCID, americanStockExchangeCCName }));
    
//    Identifier batsMIC = Identifier.of(ExchangeUtils.ISO_MIC, "BATO");
//    Identifier batsCCID = Identifier.of(ExchangeUtils.COPP_CLARK_CENTER_ID, "1331");
//    Identifier batsCCName = Identifier.of(ExchangeUtils.COPP_CLARK_NAME, "BATS Exchange Options Market");
//    IdentifierBundle batsIDs = new IdentifierBundle(Arrays.asList(new Identifier[] { batsMIC, batsCCID, batsCCName }));
    
//    IdentifierBundle notRightIDs = new IdentifierBundle(Arrays.asList(new Identifier[] { batsMIC, gemmaMIC, euronextLiffeCCName }));
    // store the euronext LIFFE bundle with it's name and region
    // REVIEW: jim 13-Aug-2010 -- change this to use a bundle rather than the first thing from a bundle
    ExchangeDocument addDoc1 = new ExchangeDocument(new ManageableExchange(euronextLiffeIDs, euronextLiffeCCName.getValue(), uk.getIdentifiers().iterator().next()));
    exchangeRepo.add(addDoc1);
    
    // try pulling it out with the first id in the bundle and check the fields of the returned object
    ManageableExchange euronextLiffe = exchangeSource.getSingleExchange(euronextLiffeMIC);
    Assert.assertEquals(euronextLiffeCCName.getValue(), euronextLiffe.getName());
    Assert.assertEquals(uk, regionSource.getHighestLevelRegion(euronextLiffe.getRegionId()));
    Assert.assertEquals("MemExg", euronextLiffe.getUniqueIdentifier().getScheme());
    Assert.assertEquals("1", euronextLiffe.getUniqueIdentifier().getValue());
    
    // try the other two ids in the bundle
    Assert.assertEquals(euronextLiffe, exchangeSource.getSingleExchange(euronextLiffeCCID));
    Assert.assertEquals(euronextLiffe, exchangeSource.getSingleExchange(euronextLiffeCCName));
    
    // try the whole bundle
    Assert.assertEquals(euronextLiffe, exchangeSource.getSingleExchange(euronextLiffeIDs));
    
    // try bundle with an extra 'unknown id' in it and one of the original missing.
    ManageableExchange euronextLiffe5 = exchangeSource.getSingleExchange(euronextLiffeIDsWithExtra);
    Assert.assertEquals(euronextLiffe5, euronextLiffe);
    
    Assert.assertNull(exchangeSource.getSingleExchange(euronextLiffeExtra));
    
    // put it again with the extra/missing bundle
    // REVIEW: jim 13-Aug-2010 -- change this to use a bundle rather than the first thing from a bundle
    ExchangeDocument addDoc2 = new ExchangeDocument(new ManageableExchange(euronextLiffeIDsWithExtra, euronextLiffeCCName.getValue(), uk.getIdentifiers().iterator().next()));
    exchangeRepo.add(addDoc2);
    
    // this needs fixing.  It should add the identifier to the bundle, but it can't, so we should probably not allow the above operation.
    Assert.assertEquals(3, exchangeSource.getSingleExchange(euronextLiffeExtra).getIdentifiers().size());
  }

  @Test
  public void testHolidayRespository() throws URISyntaxException {
    InMemoryRegionMaster regionMaster = new InMemoryRegionMaster();
    RegionFileReader.populate(regionMaster);
    RegionSource regionSource = new MasterRegionSource(regionMaster);
    MasterExchangeSource exchangeSource = CoppClarkExchangeFileReader.createPopulated().getExchangeSource();
    HolidaySource holidaySource = CoppClarkHolidayFileReader.createPopulated(new InMemoryHolidayMaster()); 
    Identifier euronextLiffeId = Identifier.of(ExchangeUtils.ISO_MIC, "XLIF");
    ManageableExchange euronextLiffe = exchangeSource.getSingleExchange(euronextLiffeId);
    Assert.assertNotNull(euronextLiffe);
    Assert.assertTrue(holidaySource.isHoliday(LocalDate.of(2012, 06, 05), HolidayType.SETTLEMENT, euronextLiffeId));
    Assert.assertFalse(holidaySource.isHoliday(LocalDate.of(2012, 06, 06), HolidayType.SETTLEMENT, euronextLiffeId));
    Assert.assertTrue(holidaySource.isHoliday(LocalDate.of(2012, 06, 05), HolidayType.BANK, euronextLiffe.getRegionId()));
    Assert.assertFalse(holidaySource.isHoliday(LocalDate.of(2012, 06, 06), HolidayType.BANK, euronextLiffe.getRegionId()));
    Assert.assertTrue(holidaySource.isHoliday(LocalDate.of(2012, 06, 05), HolidayType.TRADING, euronextLiffeId));
    Assert.assertFalse(holidaySource.isHoliday(LocalDate.of(2012, 06, 06), HolidayType.TRADING, euronextLiffeId));
    Currency currency = regionSource.getHighestLevelRegion(euronextLiffe.getRegionId()).getCurrency();
    Assert.assertTrue(holidaySource.isHoliday(LocalDate.of(2012, 06, 05), currency));
    Assert.assertFalse(holidaySource.isHoliday(LocalDate.of(2012, 06, 06), currency));
  }

}
