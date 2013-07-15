/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.util;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;

import com.opengamma.financial.security.option.OptionType;
import com.opengamma.id.ExternalId;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BloombergEQVanillaOptionChainTest {

  // ------------ FIELDS ------------
  private static LocalDate s_referenceDatePreExpiration;
  private static LocalDate s_referenceDatePostExpiration;
  private static LocalDate s_referenceDateForTestingMinus;
  private static List<ExternalId> s_identifiers;
  private static BloombergEQVanillaOptionChain s_chain;

  // ------------ METHODS ------------
  // -------- TESTS --------
  // ---- PRECONDITIONS - TEST DATA ----
  @Test
  public void preTest() {
    // Make sure the test data itself is correct
    assertEquals(1024, s_identifiers.size());
  }

  // ---- TEST ON EMPTY INPUT ----
  @Test
  public void testEmpty () {
    BloombergEQVanillaOptionChain empty = new BloombergEQVanillaOptionChain(new ArrayList<ExternalId>());
    BloombergEQVanillaOptionChain chain = empty.narrowByOptionType(OptionType.CALL);
    assertCount(0, chain);
    
    chain = empty.narrowByStrike(100d, 1);
    assertCount(0, chain);
    
    chain = empty.narrowByExpiry(LocalDate.now(), 0);
    assertCount(0, chain);
  }

  // ---- OPTION TYPE ----
  @Test(dependsOnMethods="preTest")
  public void testOptionType_1 () {
    BloombergEQVanillaOptionChain callChain = s_chain.narrowByOptionType(OptionType.CALL);
    BloombergEQVanillaOptionChain putChain  = s_chain.narrowByOptionType(OptionType.PUT);
    assertCount (512, callChain);
    assertOptionType (OptionType.CALL, callChain);
    assertCount (512, putChain);
    assertOptionType (OptionType.PUT, putChain);
  }

  // ---- STRIKE ----
  @Test(dependsOnMethods="preTest")
  public void testStrike_0_On () {
    double referencePrice = 140;
    int offset = 0;
    BloombergEQVanillaOptionChain chain = s_chain.narrowByStrike(referencePrice, offset);
    assertCount(6, referencePrice, offset, chain);
    assertStrike(140, referencePrice, offset, chain);
  }

  @Test(dependsOnMethods="preTest")
  public void testStrike_Plus_1 () {
    double referencePrice = 141;
    int offset = 1;
    BloombergEQVanillaOptionChain chain = s_chain.narrowByStrike(referencePrice, offset);
    assertCount(6, referencePrice, offset, chain);
    assertStrike(145, referencePrice, offset, chain);
  }

  @Test(dependsOnMethods="preTest")
  public void testStrike_Plus_2 () {
    double referencePrice = 141;
    int offset = 2;
    BloombergEQVanillaOptionChain chain = s_chain.narrowByStrike(referencePrice, offset);
    assertCount(6, referencePrice, offset, chain);
    assertStrike(150, referencePrice, offset, chain);
  }

  @Test(dependsOnMethods="preTest")
  public void testStrike_Plus_5 () {
    double referencePrice = 141;
    int offset = 5;
    BloombergEQVanillaOptionChain chain = s_chain.narrowByStrike(referencePrice, offset);
    assertCount(10, referencePrice, offset, chain);
    assertStrike(165, referencePrice, offset, chain);
  }

  @Test(dependsOnMethods="preTest")
  public void testStrike_Plus_100 () {
    double referencePrice = 141;
    int offset = 100;
    BloombergEQVanillaOptionChain chain = s_chain.narrowByStrike(referencePrice, offset);
    assertCount(10, referencePrice, offset, chain);
    assertStrike(540, referencePrice, offset, chain);
  }

  @Test(dependsOnMethods="preTest")
  public void testStrike_Plus_1_Interval () {
    int offset = 1;
    
    double referencePrice = 530;
    for (int i = 1 ; i <= 4 ; i++) {
      referencePrice = referencePrice + 1.0;
      BloombergEQVanillaOptionChain chain = s_chain.narrowByStrike(referencePrice, offset);
      assertCount(4, referencePrice, offset, chain);
      assertStrike(535, referencePrice, offset, chain);
    }
  }

  @Test(dependsOnMethods="preTest")
  public void testStrike_Minus_1 () {
    double referencePrice = 141;
    int offset = -1;
    BloombergEQVanillaOptionChain chain = s_chain.narrowByStrike(referencePrice, offset);
    assertCount(6, referencePrice, offset, chain);
    assertStrike(140, referencePrice, offset, chain);
  }

  @Test(dependsOnMethods="preTest")
  public void testStrike_Minus_2 () {
    double referencePrice = 141;
    int offset = -2;
    BloombergEQVanillaOptionChain chain = s_chain.narrowByStrike(referencePrice, offset);
    assertCount(6, referencePrice, offset, chain);
    assertStrike(135, referencePrice, offset, chain);
  }

  @Test(dependsOnMethods="preTest")
  public void testStrike_Minus_5 () {
    double referencePrice = 141;
    int offset = -5;
    BloombergEQVanillaOptionChain chain = s_chain.narrowByStrike(referencePrice, offset);
    assertCount(4, referencePrice, offset, chain);
    assertStrike(120, referencePrice, offset, chain);
  }

  @Test(dependsOnMethods="preTest")
  public void testStrike_Minus_100 () {
    double referencePrice = 141;
    int offset = -100;
    BloombergEQVanillaOptionChain chain = s_chain.narrowByStrike(referencePrice, offset);
    assertCount(2, referencePrice, offset, chain);
    assertStrike(100, referencePrice, offset, chain);
  }

  @Test(dependsOnMethods="preTest")
  public void testStrike_Minus_1_Interval () {
    int offset = -1;
    
    double referencePrice = 531;
    for (int i = 1 ; i <= 4 ; i++) {
      referencePrice = referencePrice + 1.0;
      BloombergEQVanillaOptionChain chain = s_chain.narrowByStrike(referencePrice, offset);
      assertCount(12, referencePrice, offset, chain);
      assertStrike(530, referencePrice, offset, chain);
    }
  }

  @Test(dependsOnMethods="preTest")
  public void testStrike_Comprehensive () {
    testStrike_Comprehensive_Impl(190,0,190);
    testStrike_Comprehensive_Impl(190,1,195);
    testStrike_Comprehensive_Impl(190,-1,185);
    
    //testStrike_Comprehensive_Impl(191,0,190);    
    testStrike_Comprehensive_Impl(191,1,195);
    testStrike_Comprehensive_Impl(191,-1,190);
    
    testStrike_Comprehensive_Impl(189,0,190);
    testStrike_Comprehensive_Impl(189,1,190);
    testStrike_Comprehensive_Impl(189,-1,185);
  }

  private void testStrike_Comprehensive_Impl(double referencePrice, int offset, double expectedStrike) {
    BloombergEQVanillaOptionChain chain = s_chain.narrowByStrike(referencePrice, offset);
    assertStrike (expectedStrike, referencePrice, offset, chain);
  }

  // ---- EXPIRY ----
  @Test(dependsOnMethods="preTest")
  public void testExpiry_Plus_0_Pre () {
    LocalDate refDate = s_referenceDatePreExpiration;
    int months = 0;
    BloombergEQVanillaOptionChain chain = s_chain.narrowByExpiry(refDate, months);
    assertCount(156, refDate, months, chain);
    assertExpiry(Month.APRIL, 16, 2011, refDate, months, chain);
  }
  
  @Test(dependsOnMethods="preTest")
  public void testExpiry_Plus_0_Post () {
    LocalDate refDate = s_referenceDatePostExpiration;
    int months = 0;
    BloombergEQVanillaOptionChain chain = s_chain.narrowByExpiry(refDate, months);
    assertCount(138, refDate, months, chain);
    assertExpiry(Month.MAY, 21, 2011, refDate, months, chain);
  }

  @Test(dependsOnMethods="preTest")
  public void testExpiry_Plus_1_Pre () {
    LocalDate refDate = s_referenceDatePreExpiration;
    int months = 1;
    BloombergEQVanillaOptionChain chain = s_chain.narrowByExpiry(refDate, months);
    assertCount(138, refDate, months, chain);
    assertExpiry(Month.MAY, 21, 2011, refDate, months, chain);
  }
  
  @Test(dependsOnMethods="preTest")
  public void testExpiry_Plus_1_Post () {
    LocalDate refDate = s_referenceDatePostExpiration;
    int months = 1;
    BloombergEQVanillaOptionChain chain = s_chain.narrowByExpiry(refDate, months);
    assertCount(138, refDate, months, chain);
    assertExpiry(Month.MAY, 21, 2011, refDate, months, chain);
  }

  @Test(dependsOnMethods="preTest")
  public void testExpiry_Plus_2_Pre () {
    LocalDate refDate = s_referenceDatePreExpiration;
    int months = 2;
    BloombergEQVanillaOptionChain chain = s_chain.narrowByExpiry(refDate, months);
    assertCount(120, refDate, months, chain);
    assertExpiry(Month.JUNE, 18, 2011, refDate, months, chain);
  }
  
  @Test(dependsOnMethods="preTest")
  public void testExpiry_Plus_2_Post () {
    LocalDate refDate = s_referenceDatePostExpiration;
    int months = 2;
    BloombergEQVanillaOptionChain chain = s_chain.narrowByExpiry(refDate, months);
    assertCount(120, refDate, months, chain);
    assertExpiry(Month.JUNE, 18, 2011, refDate, months, chain);
  }
  
  @Test(dependsOnMethods="preTest")
  public void testExpiry_Plus_3_Pre () {
    LocalDate refDate = s_referenceDatePreExpiration;
    int months = 3;
    BloombergEQVanillaOptionChain chain = s_chain.narrowByExpiry(refDate, months);
    assertCount(156, refDate, months, chain);
    assertExpiry(Month.JULY, 16, 2011, refDate, months, chain);
  }
  
  @Test(dependsOnMethods="preTest")
  public void testExpiry_Plus_3_Post () {
    LocalDate refDate = s_referenceDatePostExpiration;
    int months = 3;
    BloombergEQVanillaOptionChain chain = s_chain.narrowByExpiry(refDate, months);
    assertCount(156, refDate, months, chain);
    assertExpiry(Month.JULY, 16, 2011, refDate, months, chain);
  }
  
  @Test(dependsOnMethods="preTest")
  public void testExpiry_Plus_4_Pre () {
    LocalDate refDate = s_referenceDatePreExpiration;
    int months = 4;
    BloombergEQVanillaOptionChain chain = s_chain.narrowByExpiry(refDate, months);
    assertCount(156, refDate, months, chain);
    assertExpiry(Month.JULY, 16, 2011, refDate, months, chain);
  }
  
  @Test(dependsOnMethods="preTest")
  public void testExpiry_Plus_4_Post () {
    LocalDate refDate = s_referenceDatePostExpiration;
    int months = 4;
    BloombergEQVanillaOptionChain chain = s_chain.narrowByExpiry(refDate, months);
    assertCount(156, refDate, months, chain);
    assertExpiry(Month.JULY, 16, 2011, refDate, months, chain);
  }
  
  @Test(dependsOnMethods="preTest")
  public void testExpiry_Plus_5_Pre () {
    LocalDate refDate = s_referenceDatePreExpiration;
    int months = 5;
    BloombergEQVanillaOptionChain chain = s_chain.narrowByExpiry(refDate, months);
    assertCount(152, refDate, months, chain);
    assertExpiry(Month.OCTOBER, 22, 2011, refDate, months, chain);
  }
  
  @Test(dependsOnMethods="preTest")
  public void testExpiry_Plus_5_Post () {
    LocalDate refDate = s_referenceDatePostExpiration;
    int months = 5;
    BloombergEQVanillaOptionChain chain = s_chain.narrowByExpiry(refDate, months);
    assertCount(152, refDate, months, chain);
    assertExpiry(Month.OCTOBER, 22, 2011, refDate, months, chain);
  }
  
  @Test(dependsOnMethods="preTest")
  public void testExpiry_Plus_6_Pre () {
    LocalDate refDate = s_referenceDatePreExpiration;
    int months = 6;
    BloombergEQVanillaOptionChain chain = s_chain.narrowByExpiry(refDate, months);
    assertCount(152, refDate, months, chain);
    assertExpiry(Month.OCTOBER, 22, 2011, refDate, months, chain);
  }
  
  @Test(dependsOnMethods="preTest")
  public void testExpiry_Plus_6_Post () {
    LocalDate refDate = s_referenceDatePostExpiration;
    int months = 6;
    BloombergEQVanillaOptionChain chain = s_chain.narrowByExpiry(refDate, months);
    assertCount(152, refDate, months, chain);
    assertExpiry(Month.OCTOBER, 22, 2011, refDate, months, chain);
  }
  
  @Test(dependsOnMethods="preTest")
  public void testExpiry_Plus_100 () {
    LocalDate refDate = s_referenceDatePreExpiration;
    int months = 100;
    BloombergEQVanillaOptionChain chain = s_chain.narrowByExpiry(refDate, months);
    assertCount(144, refDate, months, chain);
    assertExpiry(Month.JANUARY, 19, 2013, refDate, months, chain);
  }
  
  @Test(dependsOnMethods="preTest")
  public void testExpiry_Forward_0 () {
    LocalDate refDate = LocalDate.of(2012, Month.NOVEMBER, 11);
    int months = 0;
    BloombergEQVanillaOptionChain chain = s_chain.narrowByExpiry(refDate, months);
    assertCount(144, refDate, months, chain);
    assertExpiry(Month.JANUARY, 19, 2013, refDate, months, chain);
  }

  @Test(dependsOnMethods="preTest")
  public void testExpiry_Minus_0 () {
    LocalDate refDate = s_referenceDateForTestingMinus;
    int months = 0;
    BloombergEQVanillaOptionChain chain = s_chain.narrowByExpiry(refDate, months);
    assertCount(152, refDate, months, chain);
    assertExpiry(Month.OCTOBER, 22, 2011, refDate, months, chain);
  }
  
  @Test(dependsOnMethods="preTest")
  public void testExpiry_Minus_1 () {
    LocalDate refDate = s_referenceDateForTestingMinus;
    int months = -1;
    BloombergEQVanillaOptionChain chain = s_chain.narrowByExpiry(refDate, months);
    assertCount(152, refDate, months, chain);
    assertExpiry(Month.OCTOBER, 22, 2011, refDate, months, chain);
  }
  
  @Test(dependsOnMethods="preTest")
  public void testExpiry_Minus_2 () {
    LocalDate refDate = s_referenceDateForTestingMinus;
    int months = -2;
    BloombergEQVanillaOptionChain chain = s_chain.narrowByExpiry(refDate, months);
    assertCount(152, refDate, months, chain);
    assertExpiry(Month.OCTOBER, 22, 2011, refDate, months, chain);
  }
  
  @Test(dependsOnMethods="preTest")
  public void testExpiry_Minus_3 () {
    LocalDate refDate = s_referenceDateForTestingMinus;
    int months = -3;
    BloombergEQVanillaOptionChain chain = s_chain.narrowByExpiry(refDate, months);
    assertCount(156, refDate, months, chain);
    assertExpiry(Month.JULY, 16, 2011, refDate, months, chain);
  }
  
  @Test(dependsOnMethods="preTest")
  public void testExpiry_Minus_4 () {
    LocalDate refDate = s_referenceDateForTestingMinus;
    int months = -4;
    BloombergEQVanillaOptionChain chain = s_chain.narrowByExpiry(refDate, months);
    assertCount(156, refDate, months, chain);
    assertExpiry(Month.JULY, 16, 2011, refDate, months, chain);
  }
  
  @Test(dependsOnMethods="preTest")
  public void testExpiry_Minus_5 () {
    LocalDate refDate = s_referenceDateForTestingMinus;
    int months = -5;
    BloombergEQVanillaOptionChain chain = s_chain.narrowByExpiry(refDate, months);
    assertCount(120, refDate, months, chain);
    assertExpiry(Month.JUNE, 18, 2011, refDate, months, chain);
  }
  
  @Test(dependsOnMethods="preTest")
  public void testExpiry_Minus_6 () {
    LocalDate refDate = s_referenceDateForTestingMinus;
    int months = -6;
    BloombergEQVanillaOptionChain chain = s_chain.narrowByExpiry(refDate, months);
    assertCount(138, refDate, months, chain);
    assertExpiry(Month.MAY, 21, 2011, refDate, months, chain);
  }
  
  @Test(dependsOnMethods="preTest")
  public void testExpiry_Minus_100 () {
    LocalDate refDate = s_referenceDateForTestingMinus;
    int months = -100;
    BloombergEQVanillaOptionChain chain = s_chain.narrowByExpiry(refDate, months);
    assertCount(156, refDate, months, chain);
    assertExpiry(Month.APRIL, 16, 2011, refDate, months, chain);
  }

  
  // ---- COMBINATIONS ----
  @Test(dependsOnMethods="preTest")
  public void testCombo_1 () {
    LocalDate refDate = s_referenceDatePreExpiration;
    double refPrice = 162.3;
    int strikeOffset = 1;
    int months = 2;
    BloombergEQVanillaOptionChain chain = 
      s_chain.narrowByExpiry(refDate, months).narrowByStrike(refPrice, strikeOffset).narrowByOptionType(OptionType.CALL);
    assertCount(1, chain);
    assertExpiry(Month.JUNE, 18, 2011, refDate, months, chain);
    assertStrike(170, refPrice, strikeOffset, chain);
  }
  
  @Test(dependsOnMethods="preTest")
  public void testCombo_2 () {
    LocalDate refDate = s_referenceDatePreExpiration;
    double refPrice = 162.3;
    int strikeOffset = 1;
    int months = 2;
    BloombergEQVanillaOptionChain chain = 
      s_chain.narrowByExpiry(refDate, months).narrowByStrike(refPrice, strikeOffset).narrowByOptionType(OptionType.PUT);
    assertCount(1, chain);
    assertExpiry(Month.JUNE, 18, 2011, refDate, months, chain);
    assertStrike(170, refPrice, strikeOffset, chain);
  }
  
  
  // -------- TEST SUBROUTINES --------
  private void assertCount (int expectedCount, BloombergEQVanillaOptionChain chain) {
    assertEquals (expectedCount, chain.getIdentifiers().size());
  }
  
  private void assertCount (int expected, double referencePrice, int offset, BloombergEQVanillaOptionChain chain) {
    StringBuilder sb = new StringBuilder();
    sb.append ("[ FAIL : referencePrice = ").append(referencePrice)
      .append (" | offset = ").append(offset);
    
    StringBuilder sb2 = new StringBuilder(sb).append(" | Chain is empty ]");    
    assertTrue(sb2.toString(), chain.getIdentifiers().size() > 0);
    
    StringBuilder sb3 = new StringBuilder(sb).append(" | expected count = ").append(expected);
    int actual = chain.getIdentifiers().size();
    sb3.append(" | actual count = ").append(actual).append(" ]");
    assertEquals (sb3.toString(), expected, actual);
  }
  
  private void assertCount (int expected, LocalDate referenceDate, int monthsFromReferenceDate, BloombergEQVanillaOptionChain chain) {
    StringBuilder sb = new StringBuilder();
    sb.append ("[ FAIL : referenceDate = ").append(referenceDate)
      .append (" | months = ").append(monthsFromReferenceDate);
    
    StringBuilder sb2 = new StringBuilder(sb).append(" | Chain is empty ]");    
    assertTrue(sb2.toString(), chain.getIdentifiers().size() > 0);
    
    StringBuilder sb3 = new StringBuilder(sb).append(" | expected count = ").append(expected);
    int actual = chain.getIdentifiers().size();
    sb3.append(" | actual count = ").append(actual).append(" ]");
    assertEquals (sb3.toString(), expected, actual);
  }
  
  private void assertOptionType (OptionType expected, BloombergEQVanillaOptionChain chain) {
    for (ExternalId identifier : chain.getIdentifiers()) {
      BloombergTickerParserEQVanillaOption parser = new BloombergTickerParserEQVanillaOption(identifier);
      assertEquals (expected, parser.getOptionType());
    }
  }
  
  private void assertStrike (double expected, double referencePrice, int offset, BloombergEQVanillaOptionChain chain) {
    StringBuilder sb = new StringBuilder();
    sb.append ("[ FAIL : referencePrice = ").append(referencePrice)
      .append (" | offset = ").append(offset);
    
    StringBuilder sb2 = new StringBuilder(sb).append(" | Chain is empty ]");    
    assertTrue(sb2.toString(), chain.getIdentifiers().size() > 0);
    
    StringBuilder sb3 = new StringBuilder(sb).append(" | expected strike = ").append(expected);
    for (ExternalId identifier : chain.getIdentifiers()) {      
      BloombergTickerParserEQVanillaOption parser = new BloombergTickerParserEQVanillaOption(identifier);
      double actual = parser.getStrike();
      sb3.append(" | actual strike = ").append(actual).append(" ]");
      assertEquals (sb3.toString(), expected, actual);
    }
  }

  private void assertExpiry (Month expected, int expectedDay, int expectedYear, 
                             LocalDate referenceDate, int monthsFromReferenceDate, 
                             BloombergEQVanillaOptionChain chain) {
    LocalDate expectedValue = LocalDate.of(expectedYear, expected, expectedDay);
    
    StringBuilder sb = new StringBuilder();
    sb.append ("[ FAIL : referenceDate = ").append(referenceDate)
      .append (" | months = ").append(monthsFromReferenceDate);
    
    StringBuilder sb2 = new StringBuilder(sb).append(" | Chain is empty ]");    
    assertTrue(sb2.toString(), chain.getIdentifiers().size() > 0);
    
    StringBuilder sb3 = new StringBuilder(sb).append(" | expected expiry = ").append(expectedValue);
    for (ExternalId identifier : chain.getIdentifiers()) {      
      BloombergTickerParserEQVanillaOption parser = new BloombergTickerParserEQVanillaOption(identifier);
      LocalDate actual = parser.getExpiry();
      sb3.append(" | actual expiry = ").append(actual).append(" ]");
      assertEquals (sb3.toString(), expectedValue, actual);
    }
  }
  
  
  // -------- SETUP --------
  @BeforeClass 
  public static void initTestData () {
    s_referenceDatePreExpiration = LocalDate.of(2011, Month.APRIL, 11);
    s_referenceDatePostExpiration = LocalDate.of(2011, Month.APRIL, 18);
    s_referenceDateForTestingMinus = LocalDate.of(2011, Month.NOVEMBER, 11);
    
    s_identifiers = new ArrayList<ExternalId>(1024);
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C120 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C125 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C130 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C135 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C140 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C145 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C150 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C155 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C160 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C165 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C170 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C175 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C180 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C185 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C190 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C195 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C200 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C205 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C210 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C215 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C220 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C225 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C230 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C235 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C240 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C245 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C250 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C255 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C260 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C265 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C270 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C275 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C280 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C285 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C290 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C295 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C300 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C305 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C310 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C315 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C320 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C325 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C330 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C335 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C340 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C345 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C350 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C355 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C360 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C365 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C370 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C375 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C380 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C385 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C390 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C395 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C400 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C405 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C410 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C415 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C420 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C425 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C430 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C435 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C440 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C445 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C450 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C455 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C460 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C465 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C470 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C480 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C490 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C500 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C510 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C520 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C530 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 C540 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P120 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P125 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P130 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P135 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P140 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P145 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P150 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P155 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P160 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P165 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P170 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P175 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P180 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P185 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P190 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P195 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P200 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P205 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P210 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P215 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P220 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P225 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P230 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P235 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P240 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P245 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P250 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P255 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P260 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P265 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P270 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P275 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P280 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P285 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P290 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P295 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P300 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P305 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P310 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P315 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P320 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P325 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P330 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P335 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P340 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P345 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P350 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P355 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P360 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P365 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P370 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P375 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P380 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P385 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P390 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P395 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P400 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P405 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P410 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P415 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P420 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P425 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P430 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P435 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P440 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P445 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P450 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P455 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P460 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P465 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P470 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P480 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P490 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P500 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P510 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P520 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P530 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 04/16/11 P540 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C170 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C175 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C180 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C185 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C190 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C195 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C200 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C205 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C210 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C215 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C220 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C225 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C230 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C235 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C240 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C245 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C250 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C255 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C260 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C265 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C270 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C275 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C280 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C285 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C290 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C295 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C300 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C305 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C310 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C315 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C320 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C325 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C330 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C335 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C340 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C345 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C350 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C355 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C360 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C365 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C370 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C375 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C380 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C385 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C390 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C395 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C400 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C405 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C410 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C415 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C420 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C425 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C430 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C435 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C440 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C445 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C450 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C455 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C460 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C465 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C470 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C475 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C480 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C485 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C490 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C500 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C510 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C520 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 C530 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P170 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P175 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P180 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P185 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P190 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P195 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P200 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P205 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P210 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P215 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P220 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P225 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P230 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P235 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P240 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P245 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P250 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P255 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P260 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P265 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P270 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P275 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P280 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P285 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P290 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P295 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P300 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P305 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P310 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P315 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P320 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P325 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P330 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P335 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P340 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P345 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P350 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P355 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P360 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P365 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P370 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P375 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P380 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P385 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P390 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P395 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P400 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P405 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P410 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P415 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P420 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P425 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P430 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P435 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P440 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P445 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P450 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P455 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P460 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P465 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P470 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P475 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P480 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P485 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P490 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P500 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P510 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P520 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 05/21/11 P530 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C170 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C175 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C180 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C185 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C190 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C195 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C200 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C210 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C220 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C230 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C240 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C245 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C250 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C255 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C260 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C265 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C270 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C275 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C280 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C285 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C290 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C295 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C300 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C305 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C310 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C315 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C320 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C325 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C330 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C335 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C340 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C345 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C350 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C355 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C360 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C365 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C370 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C375 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C380 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C385 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C390 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C395 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C400 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C405 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C410 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C415 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C420 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C425 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C430 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C435 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C440 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C445 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C450 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C455 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C460 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C470 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C480 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C490 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C500 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 C510 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P170 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P175 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P180 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P185 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P190 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P195 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P200 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P210 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P220 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P230 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P240 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P245 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P250 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P255 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P260 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P265 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P270 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P275 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P280 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P285 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P290 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P295 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P300 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P305 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P310 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P315 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P320 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P325 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P330 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P335 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P340 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P345 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P350 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P355 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P360 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P365 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P370 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P375 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P380 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P385 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P390 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P395 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P400 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P405 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P410 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P415 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P420 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P425 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P430 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P435 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P440 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P445 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P450 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P455 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P460 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P470 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P480 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P490 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P500 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 06/18/11 P510 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C155 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C160 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C165 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C170 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C175 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C180 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C185 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C190 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C195 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C200 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C205 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C210 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C215 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C220 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C225 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C230 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C235 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C240 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C245 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C250 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C255 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C260 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C265 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C270 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C275 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C280 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C285 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C290 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C295 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C300 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C305 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C310 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C315 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C320 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C325 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C330 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C335 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C340 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C345 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C350 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C355 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C360 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C365 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C370 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C375 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C380 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C385 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C390 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C395 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C400 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C405 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C410 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C415 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C420 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C425 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C430 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C435 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C440 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C445 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C450 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C455 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C460 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C465 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C470 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C475 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C480 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C485 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C490 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C495 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C500 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C505 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C510 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C515 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C520 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C525 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C530 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C535 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 C540 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P155 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P160 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P165 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P170 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P175 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P180 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P185 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P190 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P195 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P200 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P205 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P210 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P215 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P220 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P225 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P230 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P235 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P240 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P245 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P250 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P255 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P260 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P265 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P270 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P275 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P280 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P285 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P290 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P295 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P300 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P305 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P310 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P315 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P320 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P325 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P330 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P335 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P340 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P345 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P350 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P355 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P360 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P365 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P370 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P375 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P380 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P385 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P390 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P395 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P400 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P405 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P410 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P415 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P420 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P425 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P430 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P435 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P440 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P445 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P450 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P455 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P460 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P465 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P470 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P475 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P480 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P485 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P490 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P495 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P500 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P505 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P510 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P515 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P520 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P525 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P530 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P535 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 07/16/11 P540 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C165 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C170 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C175 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C180 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C185 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C190 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C195 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C200 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C205 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C210 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C215 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C220 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C225 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C230 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C235 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C240 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C245 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C250 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C255 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C260 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C265 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C270 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C275 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C280 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C285 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C290 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C295 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C300 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C305 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C310 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C315 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C320 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C325 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C330 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C335 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C340 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C345 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C350 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C355 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C360 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C365 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C370 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C375 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C380 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C385 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C390 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C395 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C400 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C405 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C410 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C415 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C420 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C425 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C430 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C435 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C440 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C445 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C450 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C455 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C460 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C465 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C470 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C475 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C480 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C485 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C490 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C495 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C500 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C505 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C510 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C515 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C520 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C525 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C530 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C535 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 C540 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P165 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P170 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P175 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P180 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P185 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P190 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P195 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P200 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P205 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P210 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P215 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P220 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P225 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P230 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P235 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P240 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P245 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P250 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P255 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P260 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P265 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P270 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P275 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P280 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P285 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P290 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P295 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P300 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P305 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P310 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P315 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P320 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P325 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P330 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P335 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P340 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P345 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P350 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P355 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P360 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P365 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P370 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P375 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P380 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P385 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P390 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P395 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P400 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P405 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P410 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P415 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P420 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P425 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P430 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P435 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P440 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P445 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P450 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P455 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P460 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P465 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P470 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P475 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P480 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P485 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P490 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P495 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P500 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P505 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P510 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P515 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P520 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P525 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P530 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P535 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 10/22/11 P540 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C100 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C105 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C110 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C115 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C120 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C125 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C130 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C135 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C140 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C145 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C150 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C155 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C160 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C165 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C170 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C175 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C180 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C185 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C190 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C195 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C200 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C205 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C210 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C215 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C220 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C225 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C230 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C235 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C240 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C245 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C250 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C255 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C260 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C265 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C270 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C275 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C280 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C285 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C290 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C295 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C300 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C305 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C310 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C315 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C320 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C325 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C330 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C335 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C340 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C345 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C350 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C355 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C360 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C365 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C370 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C375 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C380 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C385 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C390 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C395 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C400 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C405 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C410 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C415 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C420 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C425 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C430 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C435 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C440 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C450 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C460 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C470 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C480 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C490 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C500 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C510 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C520 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C530 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 C540 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P100 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P105 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P110 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P115 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P120 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P125 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P130 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P135 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P140 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P145 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P150 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P155 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P160 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P165 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P170 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P175 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P180 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P185 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P190 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P195 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P200 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P205 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P210 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P215 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P220 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P225 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P230 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P235 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P240 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P245 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P250 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P255 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P260 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P265 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P270 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P275 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P280 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P285 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P290 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P295 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P300 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P305 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P310 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P315 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P320 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P325 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P330 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P335 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P340 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P345 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P350 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P355 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P360 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P365 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P370 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P375 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P380 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P385 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P390 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P395 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P400 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P405 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P410 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P415 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P420 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P425 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P430 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P435 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P440 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P450 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P460 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P470 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P480 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P490 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P500 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P510 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P520 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P530 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/21/12 P540 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C135 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C140 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C145 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C150 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C155 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C160 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C165 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C170 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C175 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C180 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C185 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C190 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C195 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C200 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C205 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C210 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C215 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C220 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C225 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C230 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C235 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C240 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C245 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C250 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C255 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C260 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C265 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C270 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C275 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C280 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C285 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C290 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C295 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C300 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C305 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C310 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C315 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C320 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C325 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C330 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C335 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C340 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C345 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C350 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C355 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C360 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C365 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C370 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C375 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C380 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C385 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C390 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C395 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C400 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C405 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C410 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C415 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C420 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C425 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C430 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C435 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C440 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C450 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C460 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C470 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C480 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C490 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C500 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C510 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C520 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C530 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 C540 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P135 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P140 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P145 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P150 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P155 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P160 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P165 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P170 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P175 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P180 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P185 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P190 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P195 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P200 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P205 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P210 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P215 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P220 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P225 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P230 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P235 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P240 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P245 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P250 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P255 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P260 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P265 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P270 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P275 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P280 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P285 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P290 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P295 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P300 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P305 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P310 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P315 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P320 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P325 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P330 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P335 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P340 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P345 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P350 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P355 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P360 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P365 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P370 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P375 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P380 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P385 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P390 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P395 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P400 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P405 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P410 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P415 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P420 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P425 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P430 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P435 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P440 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P450 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P460 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P470 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P480 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P490 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P500 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P510 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P520 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P530 Equity"));
    s_identifiers.add(ExternalId.parse("BLOOMBERG_TICKER~AAPL US 01/19/13 P540 Equity"));   
    
    s_chain = new BloombergEQVanillaOptionChain(s_identifiers);
  }
}
