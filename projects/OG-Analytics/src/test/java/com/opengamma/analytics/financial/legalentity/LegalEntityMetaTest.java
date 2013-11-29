/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.legalentity;

import static com.opengamma.analytics.financial.legalentity.LegalEntityTest.CREDIT_RATINGS;
import static com.opengamma.analytics.financial.legalentity.LegalEntityTest.LEGAL_ENTITY;
import static com.opengamma.analytics.financial.legalentity.LegalEntityTest.OBLIGOR_RED_CODE;
import static com.opengamma.analytics.financial.legalentity.LegalEntityTest.RED_CODE;
import static com.opengamma.analytics.financial.legalentity.LegalEntityTest.REGION;
import static com.opengamma.analytics.financial.legalentity.LegalEntityTest.SECTOR;
import static com.opengamma.analytics.financial.legalentity.LegalEntityTest.SHORT_NAME;
import static com.opengamma.analytics.financial.legalentity.LegalEntityTest.TICKER;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Collections;
import java.util.Set;

import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.legalentity.GICSCode;
import com.opengamma.analytics.financial.legalentity.ICBCode;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityCombinedCreditRatings;
import com.opengamma.analytics.financial.legalentity.LegalEntityREDCode;
import com.opengamma.analytics.financial.legalentity.LegalEntityRegion;
import com.opengamma.analytics.financial.legalentity.LegalEntitySector;
import com.opengamma.analytics.financial.legalentity.LegalEntityShortName;
import com.opengamma.analytics.financial.legalentity.LegalEntityTicker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for the classes that extract data from an {@link LegalEntity}.
 */
@Test(groups = TestGroup.UNIT)
public class LegalEntityMetaTest {

  @Test
  public void testCombinedCreditRatings() {
    assertEquals(CREDIT_RATINGS, new LegalEntityCombinedCreditRatings().getMetaData(LEGAL_ENTITY));
    assertEquals(CREDIT_RATINGS, new LegalEntityCombinedCreditRatings().getMetaData(OBLIGOR_RED_CODE));
  }

  @Test
  public void testREDCode() {
    assertEquals(RED_CODE, new LegalEntityREDCode().getMetaData(OBLIGOR_RED_CODE));
  }

  @Test
  public void testRegion() {
    LegalEntityRegion meta = LegalEntityRegion.builder().create();
    assertEquals(REGION, meta.getMetaData(LEGAL_ENTITY));
    assertEquals(REGION, meta.getMetaData(OBLIGOR_RED_CODE));
    meta = LegalEntityRegion.builder().withName(REGION.getName()).create();
    assertEquals(Collections.singleton(REGION.getName()), meta.getMetaData(LEGAL_ENTITY));
    assertEquals(Collections.singleton(REGION.getName()), meta.getMetaData(OBLIGOR_RED_CODE));
    meta = LegalEntityRegion.builder().withCurrency(Currency.CAD).create();
    assertEquals(Sets.newHashSet(Currency.CAD, Currency.USD), meta.getMetaData(LEGAL_ENTITY));
    assertEquals(Sets.newHashSet(Currency.CAD, Currency.USD), meta.getMetaData(OBLIGOR_RED_CODE));
    meta = LegalEntityRegion.builder().withCurrency(Currency.CAD).create();
    assertEquals(Sets.newHashSet(Currency.CAD, Currency.USD), meta.getMetaData(LEGAL_ENTITY));
    assertEquals(Sets.newHashSet(Currency.CAD, Currency.USD), meta.getMetaData(OBLIGOR_RED_CODE));
  }

  @Test
  public void testSector() {
    LegalEntitySector meta = LegalEntitySector.builder().create();
    assertEquals(SECTOR, meta.getMetaData(LEGAL_ENTITY));
    assertEquals(SECTOR, meta.getMetaData(OBLIGOR_RED_CODE));
    meta = LegalEntitySector.builder().withName("INDUSTRY").create();
    assertEquals(Collections.singleton(SECTOR.getName()), meta.getMetaData(LEGAL_ENTITY));
    meta = LegalEntitySector.builder().withClassificationName("GICS").create();
    assertEquals(Collections.singleton(GICSCode.of(10203040)), meta.getMetaData(LEGAL_ENTITY));
    meta = LegalEntitySector.builder().withClassificationName("ICB").create();
    assertEquals(Collections.singleton(ICBCode.of("1020")), meta.getMetaData(LEGAL_ENTITY));
    meta = LegalEntitySector.builder().withClassificationName("OTHER").create();
    assertTrue(meta.getMetaData(LEGAL_ENTITY) instanceof Set);
    assertTrue(((Set<?>) meta.getMetaData(LEGAL_ENTITY)).isEmpty());
    assertTrue(meta.getMetaData(OBLIGOR_RED_CODE) instanceof Set);
    assertTrue(((Set<?>) meta.getMetaData(OBLIGOR_RED_CODE)).isEmpty());
  }

  @Test
  public void testShortName() {
    assertEquals(SHORT_NAME, new LegalEntityShortName().getMetaData(LEGAL_ENTITY));
    assertEquals(SHORT_NAME, new LegalEntityShortName().getMetaData(OBLIGOR_RED_CODE));
  }

  @Test
  public void testTicker() {
    assertEquals(TICKER, new LegalEntityTicker().getMetaData(LEGAL_ENTITY));
    assertEquals(TICKER, new LegalEntityTicker().getMetaData(OBLIGOR_RED_CODE));
  }
}
