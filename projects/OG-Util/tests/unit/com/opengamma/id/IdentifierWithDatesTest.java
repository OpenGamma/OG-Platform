/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

import javax.time.calendar.LocalDate;
import javax.time.calendar.MonthOfYear;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.testng.annotations.Test;

/**
 * Test IdentifierWithDates. 
 */
@Test
public class IdentifierWithDatesTest {

  private static final IdentificationScheme SCHEME = IdentificationScheme.of("Scheme");
  private static final Identifier IDENTIFIER = Identifier.of(SCHEME, "value");
  private static final LocalDate VALID_FROM = LocalDate.of(2010, MonthOfYear.JANUARY, 1);
  private static final LocalDate VALID_TO = LocalDate.of(2010, MonthOfYear.DECEMBER, 1);
  
  public void test_factory_Identifier_LocalDate_LocalDate() {
    IdentifierWithDates test = IdentifierWithDates.of(IDENTIFIER, VALID_FROM, VALID_TO);
    assertEquals(IDENTIFIER, test.asIdentifier());
    assertEquals(VALID_FROM, test.getValidFrom());
    assertEquals(VALID_TO, test.getValidTo());
    assertEquals("Scheme::value:S:2010-01-01:E:2010-12-01", test.toString());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_factory_Identifier_LocalDate_LocalDate_nullIdentifier() {
    IdentifierWithDates.of((Identifier) null, VALID_FROM, VALID_TO);
  }

  public void test_factory_Identifier_LocalDate_LocalDate_nullValidFrom() {
    IdentifierWithDates test = IdentifierWithDates.of(IDENTIFIER, (LocalDate) null, VALID_TO);
    assertEquals(IDENTIFIER, test.asIdentifier());
    assertNull(test.getValidFrom());
    assertEquals(VALID_TO, test.getValidTo());
    assertEquals("Scheme::value:E:2010-12-01", test.toString());
  }

  public void test_factory_Identifier_LocalDate_LocalDate_nullValidTo() {
    IdentifierWithDates test = IdentifierWithDates.of(IDENTIFIER, VALID_FROM, (LocalDate) null);
    assertEquals(IDENTIFIER, test.asIdentifier());
    assertNull(test.getValidTo());
    assertEquals(VALID_FROM, test.getValidFrom());
    assertEquals("Scheme::value:S:2010-01-01", test.toString());
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_factory_validFrom_after_validTo() {
    IdentifierWithDates.of(IDENTIFIER, VALID_TO, VALID_FROM);
  }

  //-------------------------------------------------------------------------
  public void test_parse() {
    IdentifierWithDates test = IdentifierWithDates.parse("Scheme::value:S:2010-01-01:E:2010-12-01");
    assertEquals(IDENTIFIER, test.asIdentifier());
    assertEquals(VALID_FROM, test.getValidFrom());
    assertEquals(VALID_TO, test.getValidTo());
    
    test = IdentifierWithDates.parse("Scheme::value:S:2010-01-01");
    assertEquals(IDENTIFIER, test.asIdentifier());
    assertEquals(VALID_FROM, test.getValidFrom());
    assertNull(test.getValidTo());
    
    test = IdentifierWithDates.parse("Scheme::value:E:2010-12-01");
    assertEquals(IDENTIFIER, test.asIdentifier());
    assertEquals(VALID_TO, test.getValidTo());
    assertNull(test.getValidFrom());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_parse_invalidFormat() {
    Identifier.parse("Scheme:value");
  }

  //-------------------------------------------------------------------------
  public void test_getIdentityKey() {
    IdentifierWithDates test = IdentifierWithDates.of(IDENTIFIER, VALID_FROM, VALID_TO);
    assertEquals(IDENTIFIER, test.getIdentityKey());
  }

  //-------------------------------------------------------------------------
  public void test_equals() {
    IdentifierWithDates d1a = IdentifierWithDates.of(IDENTIFIER, VALID_FROM, VALID_TO);
    IdentifierWithDates d1b = IdentifierWithDates.of(IDENTIFIER, VALID_FROM, VALID_TO);
    IdentifierWithDates d2 = IdentifierWithDates.of(IDENTIFIER, LocalDate.of(2000, 1, 1), LocalDate.of(2000, 12, 1));
    
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
    IdentifierWithDates d1a = IdentifierWithDates.of(IDENTIFIER, VALID_FROM, VALID_TO);
    IdentifierWithDates d1b = IdentifierWithDates.of(IDENTIFIER, VALID_FROM, VALID_TO);
    
    assertEquals(d1a.hashCode(), d1b.hashCode());
  }

  //-------------------------------------------------------------------------
  public void test_fudgeEncoding_with_valid_dates() {
    Identifier identifier = Identifier.of("id1", "value1");
    IdentifierWithDates test = IdentifierWithDates.of(identifier, VALID_FROM, VALID_TO);
    
    FudgeMsg msg = test.toFudgeMsg(new FudgeContext());
    assertNotNull(msg);
    assertEquals(4, msg.getNumFields());
    
    IdentifierWithDates decoded = IdentifierWithDates.fromFudgeMsg(msg);
    assertEquals(test, decoded);
  }
  
  public void test_fudgeEncoding_with_validFrom() {
    Identifier identifier = Identifier.of("id1", "value1");
    IdentifierWithDates test = IdentifierWithDates.of(identifier, VALID_FROM, null);
    
    FudgeMsg msg = test.toFudgeMsg(new FudgeContext());
    assertNotNull(msg);
    assertEquals(3, msg.getNumFields());
    
    IdentifierWithDates decoded = IdentifierWithDates.fromFudgeMsg(msg);
    assertEquals(test, decoded);
  }
  
  public void test_fudgeEncoding_with_validTo() {
    Identifier identifier = Identifier.of("id1", "value1");
    IdentifierWithDates test = IdentifierWithDates.of(identifier, null, VALID_TO);
    
    FudgeMsg msg = test.toFudgeMsg(new FudgeContext());
    assertNotNull(msg);
    assertEquals(3, msg.getNumFields());
    
    IdentifierWithDates decoded = IdentifierWithDates.fromFudgeMsg(msg);
    assertEquals(test, decoded);
  }
  
}
