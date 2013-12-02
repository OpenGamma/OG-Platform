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
public class LegalEntityMetaTest {

  /**
   * Tests failure for a credit rating-specific request when the credit rating is null
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testNullCreditRatings() {
    final LegalEntityCreditRatings meta = LegalEntityCreditRatings.builder().create();
    meta.getMetaData(new LegalEntity(null, SHORT_NAME, null, null, null));
  }

  /**
   * Tests failure for a request based on the ratings description where the description is null.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testNullRatingDescription() {
    final Set<CreditRating> creditRatings = new HashSet<>();
    creditRatings.add(CreditRating.of("A", "S&P", true));
    final LegalEntityCreditRatings meta = LegalEntityCreditRatings.builder().useRatingDescriptionForAgency("S&P").create();
    meta.getMetaData(new LegalEntity(null, SHORT_NAME, creditRatings, null, null));
  }

  /**
   * Tests failure for an agency rating that is not present in the legal entity.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testUseUnavailableRating() {
    final Set<CreditRating> creditRatings = new HashSet<>();
    creditRatings.add(CreditRating.of("A", "S&P", true));
    creditRatings.add(CreditRating.of("A", "Moody's", true));
    final LegalEntityCreditRatings meta = LegalEntityCreditRatings.builder().useRatingForAgency("Fitch").create();
    meta.getMetaData(new LegalEntity(null, SHORT_NAME, creditRatings, null, null));
  }

  /**
   * Tests failure for an agency rating description that is not present in the legal entity.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testUseUnavailableRatingDescription() {
    final Set<CreditRating> creditRatings = new HashSet<>();
    creditRatings.add(CreditRating.of("A", "S&P", "Prime", true));
    creditRatings.add(CreditRating.of("A", "Moody's", "Prime", true));
    final LegalEntityCreditRatings meta = LegalEntityCreditRatings.builder().useRatingDescriptionForAgency("Fitch").create();
    meta.getMetaData(new LegalEntity(null, SHORT_NAME, creditRatings, null, null));
  }

  /**
   * Tests ratings requests.
   */
  @Test
  public void testCreditRatings() {
    LegalEntityMeta<LegalEntity> meta = LegalEntityCreditRatings.builder().create();
    assertEquals(CREDIT_RATINGS, meta.getMetaData(LEGAL_ENTITY));
    assertEquals(CREDIT_RATINGS, meta.getMetaData(LEGAL_ENTITY_RED_CODE));
    final Set<CreditRating> creditRatings = new HashSet<>(CREDIT_RATINGS);
    creditRatings.add(CreditRating.of("C", "Poor", "Test", false));
    meta = LegalEntityCreditRatings.builder().useRatings().create();
    Set<Pair<String, String>> expected = new HashSet<>();
    expected.add(Pairs.of("Moody's", "B"));
    expected.add(Pairs.of("S&P", "A"));
    expected.add(Pairs.of("Test", "C"));
    assertEquals(expected, meta.getMetaData(new LegalEntity(null, SHORT_NAME, creditRatings, null, null)));
    assertEquals(expected, meta.getMetaData(new LegalEntityWithREDCode(null, SHORT_NAME, creditRatings, null, null, "")));
    expected = new HashSet<>();
    expected.add(Pairs.of("Moody's", "B"));
    expected.add(Pairs.of("S&P", "A"));
    assertEquals(expected, meta.getMetaData(LEGAL_ENTITY));
    assertEquals(expected, meta.getMetaData(LEGAL_ENTITY_RED_CODE));
    meta = LegalEntityCreditRatings.builder().useRatingForAgency("Moody's").create();
    expected = new HashSet<>();
    expected.add(Pairs.of("Moody's", "B"));
    assertEquals(expected, meta.getMetaData(LEGAL_ENTITY));
    assertEquals(expected, meta.getMetaData(LEGAL_ENTITY_RED_CODE));
    meta = LegalEntityCreditRatings.builder().useRatingForAgency("S&P").create();
    expected = new HashSet<>();
    expected.add(Pairs.of("S&P", "A"));
    assertEquals(expected, meta.getMetaData(LEGAL_ENTITY));
    assertEquals(expected, meta.getMetaData(LEGAL_ENTITY_RED_CODE));
    meta = LegalEntityCreditRatings.builder().useRatingDescriptions().create();
    expected = new HashSet<>();
    expected.add(Pairs.of("Moody's", "Investment Grade"));
    expected.add(Pairs.of("S&P", "Prime"));
    expected.add(Pairs.of("Test", "Poor"));
    assertEquals(expected, meta.getMetaData(new LegalEntity(null, SHORT_NAME, creditRatings, null, null)));
    assertEquals(expected, meta.getMetaData(new LegalEntityWithREDCode(null, SHORT_NAME, creditRatings, null, null, "")));
    expected = new HashSet<>();
    expected.add(Pairs.of("Moody's", "Investment Grade"));
    expected.add(Pairs.of("S&P", "Prime"));
    assertEquals(expected, meta.getMetaData(LEGAL_ENTITY));
    assertEquals(expected, meta.getMetaData(LEGAL_ENTITY_RED_CODE));
    meta = LegalEntityCreditRatings.builder().useRatingDescriptionForAgency("Moody's").create();
    expected = new HashSet<>();
    expected.add(Pairs.of("Moody's", "Investment Grade"));
    assertEquals(expected, meta.getMetaData(LEGAL_ENTITY));
    assertEquals(expected, meta.getMetaData(LEGAL_ENTITY_RED_CODE));
    meta = LegalEntityCreditRatings.builder().useRatingDescriptionForAgency("S&P").create();
    expected = new HashSet<>();
    expected.add(Pairs.of("S&P", "Prime"));
    assertEquals(expected, meta.getMetaData(LEGAL_ENTITY));
    assertEquals(expected, meta.getMetaData(LEGAL_ENTITY_RED_CODE));
  }

  /**
   * Tests requests for RED codes.
   */
  @Test
  public void testREDCode() {
    assertEquals(RED_CODE, new LegalEntityREDCode().getMetaData(LEGAL_ENTITY_RED_CODE));
  }

  /**
   * Tests failure for a region-specific request when the region is null in the legal entity.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testNullRegionInEntity() {
    final LegalEntityRegion meta = LegalEntityRegion.builder().create();
    meta.getMetaData(new LegalEntity(null, SHORT_NAME, null, null, null));
  }

  /**
   * Tests failure when a country is requested that is not in the legal entity.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testRegionNoMatchingCountry() {
    final LegalEntityRegion meta = LegalEntityRegion.builder().useCountry("GB").create();
    meta.getMetaData(LEGAL_ENTITY);
  }

  /**
   * Tests failure when a currency is requested that is not in the legal entity.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testRegionNoMatchingCurrency() {
    final LegalEntityRegion meta = LegalEntityRegion.builder().useCurrency("GBP").create();
    meta.getMetaData(LEGAL_ENTITY);
  }

  /**
   * Tests region requests.
   */
  @Test
  public void testRegion() {
    LegalEntityRegion meta = LegalEntityRegion.builder().create();
    assertEquals(REGION, meta.getMetaData(LEGAL_ENTITY));
    assertEquals(REGION, meta.getMetaData(LEGAL_ENTITY_RED_CODE));
    meta = LegalEntityRegion.builder().useName().create();
    assertEquals(Collections.singleton(REGION.getName()), meta.getMetaData(LEGAL_ENTITY));
    assertEquals(Collections.singleton(REGION.getName()), meta.getMetaData(LEGAL_ENTITY_RED_CODE));
    meta = LegalEntityRegion.builder().useCountries().create();
    assertEquals(Sets.newHashSet(Country.US, Country.CA), meta.getMetaData(LEGAL_ENTITY));
    assertEquals(Sets.newHashSet(Country.US, Country.CA), meta.getMetaData(LEGAL_ENTITY_RED_CODE));
    meta = LegalEntityRegion.builder().useCountry("US").create();
    assertEquals(Sets.newHashSet(Country.US), meta.getMetaData(LEGAL_ENTITY));
    assertEquals(Sets.newHashSet(Country.US), meta.getMetaData(LEGAL_ENTITY_RED_CODE));
    meta = LegalEntityRegion.builder().useCurrencies().create();
    assertEquals(Sets.newHashSet(Currency.CAD, Currency.USD), meta.getMetaData(LEGAL_ENTITY));
    assertEquals(Sets.newHashSet(Currency.CAD, Currency.USD), meta.getMetaData(LEGAL_ENTITY_RED_CODE));
    meta = LegalEntityRegion.builder().useCurrency("USD").create();
    assertEquals(Sets.newHashSet(Currency.USD), meta.getMetaData(LEGAL_ENTITY));
    assertEquals(Sets.newHashSet(Currency.USD), meta.getMetaData(LEGAL_ENTITY_RED_CODE));
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
    final LegalEntitySector meta = LegalEntitySector.builder().useClassificationForType(ICBCode.NAME).create();
    meta.getMetaData(new LegalEntity(TICKER, SHORT_NAME, CREDIT_RATINGS, sector, REGION));
  }

  /**
   * Tests failure for a classification type that does not exist in legal entity.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testSectorNoMatchingClassification() {
    final Sector sector = Sector.of("INDUSTRIALS");
    final LegalEntitySector meta = LegalEntitySector.builder().useClassificationForType(GICSCode.NAME).create();
    meta.getMetaData(new LegalEntity(TICKER, SHORT_NAME, CREDIT_RATINGS, sector, REGION));
  }

  /**
   * Tests sector requests.
   */
  @Test
  public void testSector() {
    LegalEntitySector meta = LegalEntitySector.builder().create();
    assertEquals(SECTOR, meta.getMetaData(LEGAL_ENTITY));
    assertEquals(SECTOR, meta.getMetaData(LEGAL_ENTITY_RED_CODE));
    meta = LegalEntitySector.builder().useName().create();
    assertEquals(Collections.singleton(SECTOR.getName()), meta.getMetaData(LEGAL_ENTITY));
    meta = LegalEntitySector.builder().useClassificationForType(GICSCode.NAME).create();
    assertEquals(Collections.singleton(GICSCode.of(10203040)), meta.getMetaData(LEGAL_ENTITY));
    meta = LegalEntitySector.builder().useClassificationForType(ICBCode.NAME).create();
    assertEquals(Collections.singleton(ICBCode.of("1020")), meta.getMetaData(LEGAL_ENTITY));
    meta = LegalEntitySector.builder().useClassificationName().create();
    assertTrue(meta.getMetaData(LEGAL_ENTITY) instanceof Set);
    assertTrue(((Set<?>) meta.getMetaData(LEGAL_ENTITY)).isEmpty());
    assertTrue(meta.getMetaData(LEGAL_ENTITY_RED_CODE) instanceof Set);
    assertTrue(((Set<?>) meta.getMetaData(LEGAL_ENTITY_RED_CODE)).isEmpty());
  }

  /**
   * Tests short name requests.
   */
  @Test
  public void testShortName() {
    assertEquals(SHORT_NAME, new LegalEntityShortName().getMetaData(LEGAL_ENTITY));
    assertEquals(SHORT_NAME, new LegalEntityShortName().getMetaData(LEGAL_ENTITY_RED_CODE));
  }

  /**
   * Tests ticker requests.
   */
  @Test
  public void testTicker() {
    assertEquals(TICKER, new LegalEntityTicker().getMetaData(LEGAL_ENTITY));
    assertEquals(TICKER, new LegalEntityTicker().getMetaData(LEGAL_ENTITY_RED_CODE));
  }

  /**
   * Tests combining rating and sector.
   */
  @Test
  public void testRatingAndSector() {
    final LegalEntityCombinedMeta meta = LegalEntityCombinedMeta.builder()
        .useMeta(LegalEntityCreditRatings.builder().useRatingForAgency("S&P").create())
        .useMeta(LegalEntitySector.builder().useName().create()).create();
    final Set<Object> expected = new HashSet<>();
    expected.add(Collections.singleton(SECTOR.getName()));
    final Set<Object> ratings = new HashSet<>();
    ratings.add(Pairs.of("S&P", "A"));
    expected.add(ratings);
    assertEquals(expected, meta.getMetaData(LEGAL_ENTITY));
    assertEquals(expected, meta.getMetaData(LEGAL_ENTITY_RED_CODE));
  }
}
