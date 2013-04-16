/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;
import static org.threeten.bp.Month.DECEMBER;
import static org.threeten.bp.Month.JANUARY;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.util.test.TestGroup;

/**
 * Test {@link ExternalIdWithDates}. 
 */
@Test(groups = TestGroup.UNIT)
public class ExternalIdWithDatesTest {

  private static final ExternalScheme SCHEME = ExternalScheme.of("Scheme");
  private static final ExternalId IDENTIFIER = ExternalId.of(SCHEME, "value");
  private static final LocalDate VALID_FROM = LocalDate.of(2010, JANUARY, 1);
  private static final LocalDate VALID_TO = LocalDate.of(2010, DECEMBER, 1);

  public void test_factory_ExternalId_LocalDate_LocalDate() {
    ExternalIdWithDates test = ExternalIdWithDates.of(IDENTIFIER, VALID_FROM, VALID_TO);
    assertEquals(IDENTIFIER, test.toExternalId());
    assertEquals(VALID_FROM, test.getValidFrom());
    assertEquals(VALID_TO, test.getValidTo());
    assertEquals("Scheme~value~S~2010-01-01~E~2010-12-01", test.toString());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_factory_ExternalId_LocalDate_LocalDate_nullExternalId() {
    ExternalIdWithDates.of((ExternalId) null, VALID_FROM, VALID_TO);
  }

  public void test_factory_ExternalId_LocalDate_LocalDate_nullValidFrom() {
    ExternalIdWithDates test = ExternalIdWithDates.of(IDENTIFIER, (LocalDate) null, VALID_TO);
    assertEquals(IDENTIFIER, test.toExternalId());
    assertNull(test.getValidFrom());
    assertEquals(VALID_TO, test.getValidTo());
    assertEquals("Scheme~value~E~2010-12-01", test.toString());
  }

  public void test_factory_ExternalId_LocalDate_LocalDate_nullValidTo() {
    ExternalIdWithDates test = ExternalIdWithDates.of(IDENTIFIER, VALID_FROM, (LocalDate) null);
    assertEquals(IDENTIFIER, test.toExternalId());
    assertNull(test.getValidTo());
    assertEquals(VALID_FROM, test.getValidFrom());
    assertEquals("Scheme~value~S~2010-01-01", test.toString());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_factory_validFrom_after_validTo() {
    ExternalIdWithDates.of(IDENTIFIER, VALID_TO, VALID_FROM);
  }

  //-------------------------------------------------------------------------
  public void test_parse() {
    ExternalIdWithDates test = ExternalIdWithDates.parse("Scheme~value~S~2010-01-01~E~2010-12-01");
    assertEquals(IDENTIFIER, test.toExternalId());
    assertEquals(VALID_FROM, test.getValidFrom());
    assertEquals(VALID_TO, test.getValidTo());

    test = ExternalIdWithDates.parse("Scheme~value~S~2010-01-01");
    assertEquals(IDENTIFIER, test.toExternalId());
    assertEquals(VALID_FROM, test.getValidFrom());
    assertNull(test.getValidTo());

    test = ExternalIdWithDates.parse("Scheme~value~E~2010-12-01");
    assertEquals(IDENTIFIER, test.toExternalId());
    assertEquals(VALID_TO, test.getValidTo());
    assertNull(test.getValidFrom());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_parse_invalidFormat() {
    ExternalId.parse("Scheme:value");
  }

  //-------------------------------------------------------------------------
  public void test_getIdentityKey() {
    ExternalIdWithDates test = ExternalIdWithDates.of(IDENTIFIER, VALID_FROM, VALID_TO);
    assertEquals(IDENTIFIER, test.getExternalId());
  }

  //-------------------------------------------------------------------------
  public void test_isValid() {
    ExternalIdWithDates test = ExternalIdWithDates.of(IDENTIFIER, VALID_FROM, VALID_TO);
    assertEquals(true, test.isValidOn(null));
    assertEquals(false, test.isValidOn(LocalDate.of(1999, 1, 1)));
    assertEquals(true, test.isValidOn(VALID_FROM));
    assertEquals(true, test.isValidOn(VALID_TO));
    assertEquals(false, test.isValidOn(LocalDate.of(2099, 1, 1)));
  }

  //-------------------------------------------------------------------------
  public void test_equals() {
    ExternalIdWithDates d1a = ExternalIdWithDates.of(IDENTIFIER, VALID_FROM, VALID_TO);
    ExternalIdWithDates d1b = ExternalIdWithDates.of(IDENTIFIER, VALID_FROM, VALID_TO);
    ExternalIdWithDates d2 = ExternalIdWithDates.of(IDENTIFIER, LocalDate.of(2000, 1, 1), LocalDate.of(2000, 12, 1));

    assertEquals(true, d1a.equals(d1a));
    assertEquals(true, d1a.equals(d1b));
    assertEquals(false, d1a.equals(d2));

    assertEquals(true, d1b.equals(d1a));
    assertEquals(true, d1b.equals(d1b));
    assertEquals(false, d1b.equals(d2));

    assertEquals(false, d2.equals(d1a));
    assertEquals(false, d2.equals(d1b));
    assertEquals(true, d2.equals(d2));

    assertEquals(false, d1b.equals("d1"));
    assertEquals(false, d1b.equals(null));
  }

  public void test_hashCode() {
    ExternalIdWithDates d1a = ExternalIdWithDates.of(IDENTIFIER, VALID_FROM, VALID_TO);
    ExternalIdWithDates d1b = ExternalIdWithDates.of(IDENTIFIER, VALID_FROM, VALID_TO);

    assertEquals(d1a.hashCode(), d1b.hashCode());
  }

}
