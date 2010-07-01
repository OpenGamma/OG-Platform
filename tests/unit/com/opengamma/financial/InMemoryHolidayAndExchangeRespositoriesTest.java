/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Set;

import javax.time.calendar.LocalDate;

import junit.framework.Assert;

import org.junit.Test;

import com.google.common.io.Resources;
import com.opengamma.financial.Currency;
import com.opengamma.financial.Exchange;
import com.opengamma.financial.ExchangeRepository;
import com.opengamma.financial.HolidayType;
import com.opengamma.financial.InMemoryExchangeRespository;
import com.opengamma.financial.InMemoryHolidayRepository;
import com.opengamma.financial.InMemoryRegionRepository;
import com.opengamma.financial.Region;
import com.opengamma.financial.RegionRepository;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;

/**
 * Unit tests for {@see InMemoryHolidayRepository} and {@see InMemoryExchangeRepository}
 */
public class InMemoryHolidayAndExchangeRespositoriesTest {
  private static final String REGION_HIERARCHY = InMemoryRegionRepository.POLITICAL_HIERARCHY_NAME;
  @Test
  public void testExchangeRepository() throws URISyntaxException {
    URL countryCSV = Resources.getResource("com/opengamma/financial/countrylist_test.csv");
    RegionRepository regionRepo = new InMemoryRegionRepository(new File(countryCSV.toURI()));
    InMemoryExchangeRespository exchangeRepo = new InMemoryExchangeRespository(regionRepo);
    Set<Region> gbMatches = regionRepo.getHierarchyNodes(null, REGION_HIERARCHY, InMemoryRegionRepository.ISO_COUNTRY_2, "GB");
    Assert.assertEquals(1, gbMatches.size());
    Region uk = gbMatches.iterator().next();
    Set<Region> usMatches = regionRepo.getHierarchyNodes(null, REGION_HIERARCHY, InMemoryRegionRepository.ISO_COUNTRY_2, "FR");
    Assert.assertEquals(1, usMatches.size());
    Region us = usMatches.iterator().next();
    Identifier euronextLiffeMIC = new Identifier(ExchangeRepository.ISO_MIC, "XLIF");
    Identifier euronextLiffeCCID = new Identifier(ExchangeRepository.COPP_CLARK_CENTER_ID, "979");
    Identifier euronextLiffeCCName = new Identifier(ExchangeRepository.COPP_CLARK_NAME, "Euronext LIFFE (UK contracts)");
    Identifier euronextLiffeExtra = new Identifier("TEST_SCHEME", "EURONEXT LIFFE");
    IdentifierBundle euronextLiffeIDs = new IdentifierBundle(Arrays.asList(new Identifier[] { euronextLiffeMIC, euronextLiffeCCID, euronextLiffeCCName }));
    IdentifierBundle euronextLiffeIDsWithExtra = new IdentifierBundle(Arrays.asList(new Identifier[] { euronextLiffeExtra, euronextLiffeMIC, euronextLiffeCCName }));
    
    Identifier gemmaMIC = new Identifier(ExchangeRepository.ISO_MIC, "GEMX");
    Identifier gemmaCCID = new Identifier(ExchangeRepository.COPP_CLARK_CENTER_ID, "1063");
    Identifier gemmaCCName = new Identifier(ExchangeRepository.COPP_CLARK_CENTER_ID, "GEMMA Gilt Edged Market Makers Association");
    IdentifierBundle gemmaIDs = new IdentifierBundle(Arrays.asList(new Identifier[] { gemmaMIC, gemmaCCID, gemmaCCName }));
    
    Identifier americanStockExchangeMIC = new Identifier(ExchangeRepository.ISO_MIC, "XASE");
    Identifier americanStockExchangeCCID = new Identifier(ExchangeRepository.COPP_CLARK_CENTER_ID, "784");
    Identifier americanStockExchangeCCName = new Identifier(ExchangeRepository.COPP_CLARK_NAME, "American Stock Exchange");
    IdentifierBundle americanStockExchangeIDs = new IdentifierBundle(
        Arrays.asList(new Identifier[] { americanStockExchangeMIC, americanStockExchangeCCID, americanStockExchangeCCName }));
    
    Identifier batsMIC = new Identifier(ExchangeRepository.ISO_MIC, "BATO");
    Identifier batsCCID = new Identifier(ExchangeRepository.COPP_CLARK_CENTER_ID, "1331");
    Identifier batsCCName = new Identifier(ExchangeRepository.COPP_CLARK_NAME, "BATS Exchange Options Market");
    IdentifierBundle batsIDs = new IdentifierBundle(Arrays.asList(new Identifier[] { batsMIC, batsCCID, batsCCName }));
    
    IdentifierBundle notRightIDs = new IdentifierBundle(Arrays.asList(new Identifier[] { batsMIC, gemmaMIC, euronextLiffeCCName }));
    // store the euronext LIFFE bundle with it's name and region
    exchangeRepo.putExchange(null, euronextLiffeIDs, euronextLiffeCCName.getValue(), uk.getUniqueIdentifier());
    
    // try pulling it out with the first id in the bundle and check the fields of the returned object
    Exchange euronextLiffe = exchangeRepo.resolveExchange(null, euronextLiffeMIC);
    Assert.assertEquals(euronextLiffeCCName.getValue(), euronextLiffe.getName());
    Assert.assertEquals(uk, euronextLiffe.getRegion());
    Assert.assertEquals(InMemoryExchangeRespository.EXCHANGE_SCHEME, euronextLiffe.getUniqueIdentifier().getScheme());
    Assert.assertEquals("1", euronextLiffe.getUniqueIdentifier().getValue());
    
    // try the other two ids in the bundle
    Assert.assertEquals(euronextLiffe, exchangeRepo.resolveExchange(null, euronextLiffeCCID));
    Assert.assertEquals(euronextLiffe, exchangeRepo.resolveExchange(null, euronextLiffeCCName));
    
    // try the whole bundle
    Assert.assertEquals(euronextLiffe, exchangeRepo.resolveExchange(null, euronextLiffeIDs));
    
    // try bundle with an extra 'unknown id' in it and one of the original missing.
    Exchange euronextLiffe5 = exchangeRepo.resolveExchange(null, euronextLiffeIDsWithExtra);
    Assert.assertEquals(euronextLiffe5, euronextLiffe);
    
    Assert.assertNull(exchangeRepo.resolveExchange(null, euronextLiffeExtra));
    
    // put it again with the extra/missing bundle
    exchangeRepo.putExchange(null, euronextLiffeIDsWithExtra, euronextLiffeCCName.getValue(), uk.getUniqueIdentifier());
   
    Assert.assertEquals(4, exchangeRepo.resolveExchange(null, euronextLiffeExtra).getIdentifiers().size());
  }
  
  @Test
  public void testHolidayRespository() throws URISyntaxException {
    URL countryCSV = Resources.getResource("com/opengamma/financial/countrylist_test.csv");
    InMemoryRegionRepository regionRepo = new InMemoryRegionRepository(new File(countryCSV.toURI()));
    InMemoryExchangeRespository exchangeRepo = new InMemoryExchangeRespository(regionRepo);
    URI currenciesCSV = Resources.getResource("com/opengamma/financial/Currencies_20100610.csv").toURI();
    URI financialCentresCSV = Resources.getResource("com/opengamma/financial/FinancialCentres_20100610.csv").toURI();
    URI exchangeTradingCSV = Resources.getResource("com/opengamma/financial/ExchangeTrading_20100610.csv").toURI();
    URI exchangeSettlementCSV = Resources.getResource("com/opengamma/financial/ExchangeSettlement_20100610.csv").toURI();
    InMemoryHolidayRepository holidayRepo = new InMemoryHolidayRepository(regionRepo, exchangeRepo,
                                                                          new File(currenciesCSV), new File(financialCentresCSV), 
                                                                          new File(exchangeTradingCSV), new File(exchangeSettlementCSV));
    Exchange euronextLiffe = exchangeRepo.resolveExchange(null, Identifier.of(ExchangeRepository.ISO_MIC, "XLIF"));
    Assert.assertTrue(holidayRepo.isHoliday(null, euronextLiffe, LocalDate.of(2012, 06, 05), HolidayType.SETTLEMENT));
    Assert.assertFalse(holidayRepo.isHoliday(null, euronextLiffe, LocalDate.of(2012, 06, 06), HolidayType.SETTLEMENT));
    Assert.assertTrue(holidayRepo.isHoliday(null, euronextLiffe.getRegion(), LocalDate.of(2012, 06, 05), HolidayType.BANK));
    Assert.assertFalse(holidayRepo.isHoliday(null, euronextLiffe.getRegion(), LocalDate.of(2012, 06, 06), HolidayType.BANK));
    Assert.assertTrue(holidayRepo.isHoliday(null, euronextLiffe, LocalDate.of(2012, 06, 05), HolidayType.TRADING));
    Assert.assertFalse(holidayRepo.isHoliday(null, euronextLiffe, LocalDate.of(2012, 06, 06), HolidayType.TRADING));
    Assert.assertTrue(holidayRepo.isHoliday(null, euronextLiffe.getRegion(), LocalDate.of(2012, 06, 05), HolidayType.CURRENCY));
    Assert.assertFalse(holidayRepo.isHoliday(null, euronextLiffe.getRegion(), LocalDate.of(2012, 06, 06), HolidayType.CURRENCY));
    Currency currency = Currency.getInstance(euronextLiffe.getRegion().getData().getString(InMemoryRegionRepository.ISO_CURRENCY_3));
    Assert.assertTrue(holidayRepo.isHoliday(null, currency, LocalDate.of(2012, 06, 05), HolidayType.CURRENCY));
    Assert.assertFalse(holidayRepo.isHoliday(null, currency, LocalDate.of(2012, 06, 06), HolidayType.CURRENCY));
  }
}
