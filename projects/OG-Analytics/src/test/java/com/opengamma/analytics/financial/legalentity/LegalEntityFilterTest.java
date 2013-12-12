/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.legalentity;

import static com.opengamma.analytics.financial.legalentity.LegalEntityTest.CREDIT_RATINGS;
import static com.opengamma.analytics.financial.legalentity.LegalEntityTest.LEGAL_ENTITY;
import static com.opengamma.analytics.financial.legalentity.LegalEntityTest.LEGAL_ENTITY_RED_CODE;
import static com.opengamma.analytics.financial.legalentity.LegalEntityTest.RED_CODE;
import static com.opengamma.analytics.financial.legalentity.LegalEntityTest.REGION;
import static com.opengamma.analytics.financial.legalentity.LegalEntityTest.SECTOR;
import static com.opengamma.analytics.financial.legalentity.LegalEntityTest.SHORT_NAME;
import static com.opengamma.analytics.financial.legalentity.LegalEntityTest.TICKER;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.joda.beans.impl.flexi.FlexiBean;
import org.joda.beans.ser.JodaBeanSer;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Tests for the classes that extract data from an {@link LegalEntity}.
 */
@Test(groups = TestGroup.UNIT)
public class LegalEntityFilterTest {

  /**
   * Tests failure for a credit rating-specific request when the credit rating is null
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testNullCreditRatings() {
    final LegalEntityCreditRatings filter = new LegalEntityCreditRatings();
    filter.getFilteredData(new LegalEntity(null, SHORT_NAME, null, null, null));
  }

  /**
   * Tests failure for a request based on the ratings description where the description is null.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testRatingDescription() {
    final Set<CreditRating> creditRatings = new HashSet<>();
    creditRatings.add(CreditRating.of("A", "S&P", true));
    final LegalEntityCreditRatings filter = new LegalEntityCreditRatings();
    filter.setPerAgencyRatingDescriptions(Collections.singleton("S&P"));
    filter.getFilteredData(new LegalEntity(null, SHORT_NAME, creditRatings, null, null));
  }

  /**
   * Tests failure for an agency rating that is not present in the legal entity.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testUseUnavailableRating() {
    final Set<CreditRating> creditRatings = new HashSet<>();
    creditRatings.add(CreditRating.of("A", "S&P", true));
    creditRatings.add(CreditRating.of("A", "Moody's", true));
    final LegalEntityCreditRatings filter = new LegalEntityCreditRatings();
    filter.setPerAgencyRatings(Collections.singleton("Fitch"));
    filter.getFilteredData(new LegalEntity(null, SHORT_NAME, creditRatings, null, null));
  }

  /**
   * Tests failure for an agency rating description that is not present in the legal entity.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testUseUnavailableRatingDescription() {
    final Set<CreditRating> creditRatings = new HashSet<>();
    creditRatings.add(CreditRating.of("A", "S&P", "Prime", true));
    creditRatings.add(CreditRating.of("A", "Moody's", "Prime", true));
    final LegalEntityCreditRatings filter = new LegalEntityCreditRatings();
    filter.setPerAgencyRatingDescriptions(Collections.singleton("Fitch"));
    filter.getFilteredData(new LegalEntity(null, SHORT_NAME, creditRatings, null, null));
  }

  /**
   * Tests ratings requests.
   */
  @Test
  public void testCreditRatings() {
    LegalEntityCreditRatings filter = new LegalEntityCreditRatings();
    assertEquals(CREDIT_RATINGS, filter.getFilteredData(LEGAL_ENTITY));
    assertEquals(CREDIT_RATINGS, filter.getFilteredData(LEGAL_ENTITY_RED_CODE));
    final Set<CreditRating> creditRatings = new HashSet<>(CREDIT_RATINGS);
    creditRatings.add(CreditRating.of("C", "Poor", "Test", false));
    filter = new LegalEntityCreditRatings();
    filter.setUseRating(true);
    Set<Pair<String, String>> expected = new HashSet<>();
    expected.add(Pairs.of("Moody's", "B"));
    expected.add(Pairs.of("S&P", "A"));
    expected.add(Pairs.of("Test", "C"));
    assertEquals(expected, filter.getFilteredData(new LegalEntity(null, SHORT_NAME, creditRatings, null, null)));
    assertEquals(expected, filter.getFilteredData(new LegalEntityWithREDCode(null, SHORT_NAME, creditRatings, null, null, "")));
    expected = new HashSet<>();
    expected.add(Pairs.of("Moody's", "B"));
    expected.add(Pairs.of("S&P", "A"));
    assertEquals(expected, filter.getFilteredData(LEGAL_ENTITY));
    assertEquals(expected, filter.getFilteredData(LEGAL_ENTITY_RED_CODE));
    filter = new LegalEntityCreditRatings();
    filter.setPerAgencyRatings(Collections.singleton("Moody's"));
    expected = new HashSet<>();
    expected.add(Pairs.of("Moody's", "B"));
    assertEquals(expected, filter.getFilteredData(LEGAL_ENTITY));
    assertEquals(expected, filter.getFilteredData(LEGAL_ENTITY_RED_CODE));
    filter = new LegalEntityCreditRatings();
    filter.setPerAgencyRatings(Collections.singleton("S&P"));
    expected = new HashSet<>();
    expected.add(Pairs.of("S&P", "A"));
    assertEquals(expected, filter.getFilteredData(LEGAL_ENTITY));
    assertEquals(expected, filter.getFilteredData(LEGAL_ENTITY_RED_CODE));
    filter = new LegalEntityCreditRatings();
    filter.setUseRatingDescription(true);
    expected = new HashSet<>();
    expected.add(Pairs.of("Moody's", "Investment Grade"));
    expected.add(Pairs.of("S&P", "Prime"));
    expected.add(Pairs.of("Test", "Poor"));
    assertEquals(expected, filter.getFilteredData(new LegalEntity(null, SHORT_NAME, creditRatings, null, null)));
    assertEquals(expected, filter.getFilteredData(new LegalEntityWithREDCode(null, SHORT_NAME, creditRatings, null, null, "")));
    expected = new HashSet<>();
    expected.add(Pairs.of("Moody's", "Investment Grade"));
    expected.add(Pairs.of("S&P", "Prime"));
    assertEquals(expected, filter.getFilteredData(LEGAL_ENTITY));
    assertEquals(expected, filter.getFilteredData(LEGAL_ENTITY_RED_CODE));
    filter = new LegalEntityCreditRatings();
    filter.setPerAgencyRatingDescriptions(Collections.singleton("Moody's"));
    expected = new HashSet<>();
    expected.add(Pairs.of("Moody's", "Investment Grade"));
    assertEquals(expected, filter.getFilteredData(LEGAL_ENTITY));
    assertEquals(expected, filter.getFilteredData(LEGAL_ENTITY_RED_CODE));
    filter = new LegalEntityCreditRatings();
    filter.setPerAgencyRatingDescriptions(Collections.singleton("S&P"));
    expected = new HashSet<>();
    expected.add(Pairs.of("S&P", "Prime"));
    assertEquals(expected, filter.getFilteredData(LEGAL_ENTITY));
    assertEquals(expected, filter.getFilteredData(LEGAL_ENTITY_RED_CODE));
  }

  /**
   * Tests requests for RED codes.
   */
  @Test
  public void testREDCode() {
    assertEquals(RED_CODE, new LegalEntityREDCode().getFilteredData(LEGAL_ENTITY_RED_CODE));
  }

  /**
   * Tests failure for a region-specific request when the region is null in the legal entity.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testNullRegionInEntity() {
    final LegalEntityRegion filter = new LegalEntityRegion();
    filter.getFilteredData(new LegalEntity(null, SHORT_NAME, null, null, null));
  }

  /**
   * Tests failure when a country is requested that is not in the legal entity.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testRegionNoMatchingCountry() {
    final LegalEntityRegion filter = new LegalEntityRegion();
    filter.setCountries(Collections.singleton(Country.GB));
    filter.getFilteredData(LEGAL_ENTITY);
  }

  /**
   * Tests failure when a currency is requested that is not in the legal entity.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testRegionNoMatchingCurrency() {
    final LegalEntityRegion filter = new LegalEntityRegion();
    filter.setCurrencies(Collections.singleton(Currency.GBP));
    filter.getFilteredData(LEGAL_ENTITY);
  }

  /**
   * Tests region requests.
   */
  @Test
  public void testRegion() {
    LegalEntityRegion filter = new LegalEntityRegion();
    assertEquals(REGION, filter.getFilteredData(LEGAL_ENTITY));
    assertEquals(REGION, filter.getFilteredData(LEGAL_ENTITY_RED_CODE));
    filter = new LegalEntityRegion();
    filter.setUseName(true);
    assertEquals(Collections.singleton(REGION.getName()), filter.getFilteredData(LEGAL_ENTITY));
    assertEquals(Collections.singleton(REGION.getName()), filter.getFilteredData(LEGAL_ENTITY_RED_CODE));
    filter = new LegalEntityRegion();
    filter.setUseCountry(true);
    assertEquals(Sets.newHashSet(Country.US, Country.CA), filter.getFilteredData(LEGAL_ENTITY));
    assertEquals(Sets.newHashSet(Country.US, Country.CA), filter.getFilteredData(LEGAL_ENTITY_RED_CODE));
    filter = new LegalEntityRegion();
    filter.setCountries(Collections.singleton(Country.US));
    assertEquals(Sets.newHashSet(Country.US), filter.getFilteredData(LEGAL_ENTITY));
    assertEquals(Sets.newHashSet(Country.US), filter.getFilteredData(LEGAL_ENTITY_RED_CODE));
    filter = new LegalEntityRegion();
    filter.setUseCurrency(true);
    assertEquals(Sets.newHashSet(Currency.CAD, Currency.USD), filter.getFilteredData(LEGAL_ENTITY));
    assertEquals(Sets.newHashSet(Currency.CAD, Currency.USD), filter.getFilteredData(LEGAL_ENTITY_RED_CODE));
    filter = new LegalEntityRegion();
    filter.setCurrencies(Collections.singleton(Currency.USD));
    System.err.println(JodaBeanSer.PRETTY.xmlWriter().write(filter));
    assertEquals(Sets.newHashSet(Currency.USD), filter.getFilteredData(LEGAL_ENTITY));
    assertEquals(Sets.newHashSet(Currency.USD), filter.getFilteredData(LEGAL_ENTITY_RED_CODE));
    //TODO test builder chaining and currency / country pairs
  }

  /**
   * Tests failure for empty sector classifications.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testEmptySectorClassifications() {
    final FlexiBean classifications = new FlexiBean();
    classifications.put(GICSCode.NAME, GICSCode.of("1020"));
    final Sector sector = Sector.of("INDUSTRIALS", classifications);
    final LegalEntitySector filter = new LegalEntitySector();
    filter.setClassifications(Collections.singleton(ICBCode.NAME));
    filter.getFilteredData(new LegalEntity(TICKER, SHORT_NAME, CREDIT_RATINGS, sector, REGION));
  }

  /**
   * Tests failure for a classification type that does not exist in legal entity.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testSectorNoMatchingClassification() {
    final Sector sector = Sector.of("INDUSTRIALS");
    final LegalEntitySector filter = new LegalEntitySector();
    filter.setClassifications(Collections.singleton(GICSCode.NAME));
    filter.getFilteredData(new LegalEntity(TICKER, SHORT_NAME, CREDIT_RATINGS, sector, REGION));
  }

  /**
   * Tests sector requests.
   */
  @Test
  public void testSector() {
    LegalEntitySector filter = new LegalEntitySector();
    assertEquals(SECTOR, filter.getFilteredData(LEGAL_ENTITY));
    assertEquals(SECTOR, filter.getFilteredData(LEGAL_ENTITY_RED_CODE));
    filter = new LegalEntitySector();
    filter.setUseSectorName(true);
    assertEquals(Collections.singleton(SECTOR.getName()), filter.getFilteredData(LEGAL_ENTITY));
    filter = new LegalEntitySector();
    filter.setClassifications(Collections.singleton(GICSCode.NAME));
    assertEquals(Collections.singleton(GICSCode.of(10203040)), filter.getFilteredData(LEGAL_ENTITY));
    filter = new LegalEntitySector();
    filter.setClassifications(Collections.singleton(ICBCode.NAME));
    assertEquals(Collections.singleton(ICBCode.of("1020")), filter.getFilteredData(LEGAL_ENTITY));
    filter = new LegalEntitySector();
    filter.setUseClassificationName(true);
    assertTrue(filter.getFilteredData(LEGAL_ENTITY) instanceof Set);
    assertTrue(((Set<?>) filter.getFilteredData(LEGAL_ENTITY)).isEmpty());
    assertTrue(filter.getFilteredData(LEGAL_ENTITY_RED_CODE) instanceof Set);
    assertTrue(((Set<?>) filter.getFilteredData(LEGAL_ENTITY_RED_CODE)).isEmpty());
  }

  /**
   * Tests short name requests.
   */
  @Test
  public void testShortName() {
    assertEquals(SHORT_NAME, new LegalEntityShortName().getFilteredData(LEGAL_ENTITY));
    assertEquals(SHORT_NAME, new LegalEntityShortName().getFilteredData(LEGAL_ENTITY_RED_CODE));
  }

  /**
   * Tests ticker requests.
   */
  @Test
  public void testTicker() {
    assertEquals(TICKER, new LegalEntityTicker().getFilteredData(LEGAL_ENTITY));
    assertEquals(TICKER, new LegalEntityTicker().getFilteredData(LEGAL_ENTITY_RED_CODE));
  }

  /**
   * Tests combining rating and sector.
   */
  @Test
  public void testRatingAndSector() {
    final LegalEntityCombiningFilter filter = new LegalEntityCombiningFilter();
    final Set<LegalEntityFilter<LegalEntity>> underlyingFilters = new HashSet<>();
    final LegalEntityCreditRatings ratingsFilter = new LegalEntityCreditRatings();
    ratingsFilter.setPerAgencyRatings(Collections.singleton("S&P"));
    underlyingFilters.add(ratingsFilter);
    final LegalEntitySector sectorFilter = new LegalEntitySector();
    sectorFilter.setUseSectorName(true);
    underlyingFilters.add(sectorFilter);
    filter.setFiltersToUse(underlyingFilters);
    final Set<Object> expected = new HashSet<>();
    expected.add(SECTOR.getName());
    expected.add(Pairs.of("S&P", "A"));
    assertEquals(expected, filter.getFilteredData(LEGAL_ENTITY));
    assertEquals(expected, filter.getFilteredData(LEGAL_ENTITY_RED_CODE));
  }
}
