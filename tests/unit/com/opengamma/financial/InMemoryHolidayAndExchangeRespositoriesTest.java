/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Set;

import javax.time.calendar.LocalDate;

import junit.framework.Assert;

import org.junit.Test;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;

/**
 * Unit tests for {@see InMemoryHolidayRepository} and {@see InMemoryExchangeRepository}
 */
public class InMemoryHolidayAndExchangeRespositoriesTest {
  private static final String REGION_HIERARCHY = InMemoryRegionRepository.POLITICAL_HIERARCHY_NAME;
  
  @Test
  public void testExchangeRepository() throws URISyntaxException {
    RegionMaster regionRepo = new InMemoryRegionRepository();
    RegionFileReader.populateMaster(regionRepo, new File(RegionFileReader.REGIONS_FILE_PATH));
    RegionSource regionSource = new DefaultRegionSource(regionRepo);
    InMemoryExchangeRepository exchangeRepo = new InMemoryExchangeRepository();
    ExchangeSource exchangeSource = new DefaultExchangeSource(exchangeRepo);
    
    Region uk = regionSource.getHighestLevelRegion(Identifier.of(InMemoryRegionRepository.ISO_COUNTRY_2, "GB"));

    Identifier euronextLiffeMIC = Identifier.of(ExchangeRepository.ISO_MIC, "XLIF");
    Identifier euronextLiffeCCID = Identifier.of(ExchangeRepository.COPP_CLARK_CENTER_ID, "979");
    Identifier euronextLiffeCCName = Identifier.of(ExchangeRepository.COPP_CLARK_NAME, "Euronext LIFFE (UK contracts)");
    Identifier euronextLiffeExtra = Identifier.of("TEST_SCHEME", "EURONEXT LIFFE");
    IdentifierBundle euronextLiffeIDs = new IdentifierBundle(Arrays.asList(new Identifier[] { euronextLiffeMIC, euronextLiffeCCID, euronextLiffeCCName }));
    IdentifierBundle euronextLiffeIDsWithExtra = new IdentifierBundle(Arrays.asList(new Identifier[] { euronextLiffeExtra, euronextLiffeMIC, euronextLiffeCCName }));
    
    Identifier gemmaMIC = Identifier.of(ExchangeRepository.ISO_MIC, "GEMX");
    Identifier gemmaCCID = Identifier.of(ExchangeRepository.COPP_CLARK_CENTER_ID, "1063");
    Identifier gemmaCCName = Identifier.of(ExchangeRepository.COPP_CLARK_CENTER_ID, "GEMMA Gilt Edged Market Makers Association");
    IdentifierBundle gemmaIDs = new IdentifierBundle(Arrays.asList(new Identifier[] { gemmaMIC, gemmaCCID, gemmaCCName }));
    
    Identifier americanStockExchangeMIC = Identifier.of(ExchangeRepository.ISO_MIC, "XASE");
    Identifier americanStockExchangeCCID = Identifier.of(ExchangeRepository.COPP_CLARK_CENTER_ID, "784");
    Identifier americanStockExchangeCCName = Identifier.of(ExchangeRepository.COPP_CLARK_NAME, "American Stock Exchange");
    IdentifierBundle americanStockExchangeIDs = new IdentifierBundle(
        Arrays.asList(new Identifier[] { americanStockExchangeMIC, americanStockExchangeCCID, americanStockExchangeCCName }));
    
    Identifier batsMIC = Identifier.of(ExchangeRepository.ISO_MIC, "BATO");
    Identifier batsCCID = Identifier.of(ExchangeRepository.COPP_CLARK_CENTER_ID, "1331");
    Identifier batsCCName = Identifier.of(ExchangeRepository.COPP_CLARK_NAME, "BATS Exchange Options Market");
    IdentifierBundle batsIDs = new IdentifierBundle(Arrays.asList(new Identifier[] { batsMIC, batsCCID, batsCCName }));
    
    IdentifierBundle notRightIDs = new IdentifierBundle(Arrays.asList(new Identifier[] { batsMIC, gemmaMIC, euronextLiffeCCName }));
    // store the euronext LIFFE bundle with it's name and region
    // REVIEW: jim 13-Aug-2010 -- change this to use a bundle rather than the first thing from a bundle
    exchangeRepo.addExchange(euronextLiffeIDs, euronextLiffeCCName.getValue(), uk.getIdentifiers().iterator().next());
    
    // try pulling it out with the first id in the bundle and check the fields of the returned object
    Exchange euronextLiffe = exchangeSource.getSingleExchange(euronextLiffeMIC);
    Assert.assertEquals(euronextLiffeCCName.getValue(), euronextLiffe.getName());
    Assert.assertEquals(uk, regionSource.getHighestLevelRegion(euronextLiffe.getRegion()));
    Assert.assertEquals(InMemoryExchangeRepository.EXCHANGE_SCHEME, euronextLiffe.getUniqueIdentifier().getScheme());
    Assert.assertEquals("1", euronextLiffe.getUniqueIdentifier().getValue());
    
    // try the other two ids in the bundle
    Assert.assertEquals(euronextLiffe, exchangeSource.getSingleExchange(euronextLiffeCCID));
    Assert.assertEquals(euronextLiffe, exchangeSource.getSingleExchange(euronextLiffeCCName));
    
    // try the whole bundle
    Assert.assertEquals(euronextLiffe, exchangeSource.getSingleExchange(euronextLiffeIDs));
    
    // try bundle with an extra 'unknown id' in it and one of the original missing.
    Exchange euronextLiffe5 = exchangeSource.getSingleExchange(euronextLiffeIDsWithExtra);
    Assert.assertEquals(euronextLiffe5, euronextLiffe);
    
    Assert.assertNull(exchangeSource.getSingleExchange(euronextLiffeExtra));
    
    // put it again with the extra/missing bundle
    // REVIEW: jim 13-Aug-2010 -- change this to use a bundle rather than the first thing from a bundle
    exchangeRepo.addExchange(euronextLiffeIDsWithExtra, euronextLiffeCCName.getValue(), uk.getIdentifiers().iterator().next());
   
    // this needs fixing.  It should add the identifier to the bundle, but it can't, so we should probably not allow the above operation.
    Assert.assertEquals(3, exchangeSource.getSingleExchange(euronextLiffeExtra).getIdentifiers().size());
  }
  
  @Test
  public void testHolidayRespository() throws URISyntaxException {
    InMemoryRegionRepository regionRepo = new InMemoryRegionRepository();
    RegionFileReader.populateMaster(regionRepo, new File(RegionFileReader.REGIONS_FILE_PATH));
    RegionSource regionSource = new DefaultRegionSource(regionRepo);
    InMemoryExchangeRepository exchangeRepo = new InMemoryExchangeRepository();
    ExchangeSource exchangeSource = new DefaultExchangeSource(exchangeRepo);
    ExchangeFileReader exchangeReader = new ExchangeFileReader(exchangeRepo);
    exchangeReader.readFile(new File(CoppClarkFileReader.EXCHANGE_HOLIDAYS_REPOST_FILE_PATH));
    HolidayRepository holidayRepo = new InMemoryHolidayRepository(regionSource, exchangeSource);
    
    HolidaySource holidaySource = new DefaultHolidaySource(holidayRepo);
    CoppClarkFileReader reader = new CoppClarkFileReader(holidayRepo, 
                            new File(CoppClarkFileReader.CURRENCY_HOLIDAYS_FILE_PATH), 
                            new File(CoppClarkFileReader.FINANCIAL_CENTRES_HOLIDAYS_FILE_PATH),
                            new File(CoppClarkFileReader.EXCHANGE_TRADING_HOLIDAYS_FILE_PATH),
                            new File(CoppClarkFileReader.EXCHANGE_SETTLEMENT_HOLIDAYS_FILE_PATH));
    Identifier euronextLiffeId = Identifier.of(ExchangeRepository.ISO_MIC, "XLIF");
    Exchange euronextLiffe = exchangeSource.getSingleExchange(euronextLiffeId);
    Assert.assertNotNull(euronextLiffe);
    Assert.assertTrue(holidaySource.isHoliday(euronextLiffeId, LocalDate.of(2012, 06, 05), HolidayType.SETTLEMENT));
    Assert.assertFalse(holidaySource.isHoliday(euronextLiffeId, LocalDate.of(2012, 06, 06), HolidayType.SETTLEMENT));
    Assert.assertTrue(holidaySource.isHoliday(euronextLiffe.getRegion(), LocalDate.of(2012, 06, 05), HolidayType.BANK));
    Assert.assertFalse(holidaySource.isHoliday(euronextLiffe.getRegion(), LocalDate.of(2012, 06, 06), HolidayType.BANK));
    Assert.assertTrue(holidaySource.isHoliday(euronextLiffeId, LocalDate.of(2012, 06, 05), HolidayType.TRADING));
    Assert.assertFalse(holidaySource.isHoliday(euronextLiffeId, LocalDate.of(2012, 06, 06), HolidayType.TRADING));
    String curncy = regionSource.getHighestLevelRegion(euronextLiffe.getRegion()).getData().getString(InMemoryRegionRepository.ISO_CURRENCY_3);
    Currency currency = Currency.getInstance(curncy);
    Assert.assertTrue(holidaySource.isHoliday(currency, LocalDate.of(2012, 06, 05)));
    Assert.assertFalse(holidaySource.isHoliday(currency, LocalDate.of(2012, 06, 06)));
  }
}
