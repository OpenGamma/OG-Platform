/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.util;

import static com.opengamma.bbg.BloombergConstants.FIELD_FUT_FIRST_TRADE_DT;
import static com.opengamma.bbg.BloombergConstants.FIELD_FUT_LAST_TRADE_DT;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_BBG_UNIQUE;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_CUSIP;
import static com.opengamma.bbg.BloombergConstants.FIELD_OPT_CHAIN;
import static com.opengamma.bbg.BloombergConstants.FIELD_PARSEKYABLE_DES;
import static com.opengamma.bbg.util.BloombergDataUtils.toBloombergDate;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.StringReader;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.Month;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesConstants;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.id.ExternalIdWithDates;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BloombergDataUtilsTest {

  private static final String[] IDENTIFIERS = new String[] {
      "#Comment",
      "ISIN~ISIN 1234",
      "BLOOMBERG_BUID~BUID 1234",
      "BLOOMBERG_TICKER~TICKER 1234",
      "CUSIP~CUSIP 1234",
      "SEDOL1~SEDOL1 1234",
      "BLOOMBERG_TCM~BLOOMBERG_TCM 1234"};

  //-------------------------------------------------------------------------
  @Test
  public void optionChain() throws Exception {
    MockReferenceDataProvider rdp = new MockReferenceDataProvider();
    rdp.addExpectedField(FIELD_OPT_CHAIN);
    rdp.addResult("FIRST US Equity", FIELD_OPT_CHAIN, "Security Description=SECOND US Equity");
    rdp.addResult("FIRST US Equity", FIELD_OPT_CHAIN, "Security Description=THIRD US Equity");
    
    Set<ExternalId> optionChain = BloombergDataUtils.getOptionChain(rdp, "FIRST US Equity");
    assertNotNull(optionChain);
    assertTrue(optionChain.contains(ExternalSchemes.bloombergTickerSecurityId("SECOND US Equity")));
    assertTrue(optionChain.contains(ExternalSchemes.bloombergTickerSecurityId("THIRD US Equity")));
  }

  @Test
  public void addTwoDigitYearCode() throws Exception {
    Set<ExternalIdWithDates> identifiers = Sets.newHashSet(
        ExternalIdWithDates.of(ExternalSchemes.bloombergTickerSecurityId("EDU0 Comdty"),
            LocalDate.of(2010, Month.SEPTEMBER, 14), LocalDate.of(2020, Month.SEPTEMBER, 14)),
        ExternalIdWithDates.of(ExternalSchemes.bloombergBuidSecurityId("IX11084074-0"), null, null));

    ExternalIdBundleWithDates withTwoDigits = BloombergDataUtils.addTwoDigitYearCode(new ExternalIdBundleWithDates(identifiers));
    assertTrue(withTwoDigits.size() == 3);
    for (ExternalIdWithDates identifierWithDates : identifiers) {
      assertTrue(withTwoDigits.contains(identifierWithDates));
    }
    assertTrue(withTwoDigits.contains(ExternalIdWithDates.of(
        ExternalSchemes.bloombergTickerSecurityId("EDU20 Comdty"),
        LocalDate.of(2010, Month.SEPTEMBER, 14), LocalDate.of(2020, Month.SEPTEMBER, 14))));
    
    identifiers = Sets.newHashSet(
        ExternalIdWithDates.of(ExternalSchemes.bloombergTickerSecurityId("EDU09 Comdty"),
            LocalDate.of(1999, Month.SEPTEMBER, 14), LocalDate.of(2009, Month.SEPTEMBER, 14)),
        ExternalIdWithDates.of(ExternalSchemes.bloombergBuidSecurityId("IX9471080-0"), null, null));
    withTwoDigits = BloombergDataUtils.addTwoDigitYearCode(new ExternalIdBundleWithDates(identifiers));
    assertTrue(withTwoDigits.size() == 3);
    for (ExternalIdWithDates identifierWithDates : identifiers) {
      assertTrue(withTwoDigits.contains(identifierWithDates));
    }
    assertTrue(withTwoDigits.contains(ExternalIdWithDates.of(
        ExternalSchemes.bloombergTickerSecurityId("EDU9 Comdty"),
        LocalDate.of(1999, Month.SEPTEMBER, 14), LocalDate.of(2009, Month.SEPTEMBER, 14))));
  }

  @Test
  public void parseIdentifiers() throws Exception {
    Set<ExternalIdWithDates> identifiers = Sets.newHashSet(
        ExternalIdWithDates.of(ExternalSchemes.bloombergTickerSecurityId("EDU0 Comdty"),
            LocalDate.of(2010, Month.SEPTEMBER, 14), LocalDate.of(2020, Month.SEPTEMBER, 14)),
        ExternalIdWithDates.of(ExternalSchemes.bloombergBuidSecurityId("IX11084074-0"), null, null),
        ExternalIdWithDates.of(ExternalSchemes.cusipSecurityId("EDU0"), null, null));

    ExternalIdBundleWithDates expected = new ExternalIdBundleWithDates(identifiers);
    
    MutableFudgeMsg message = new FudgeContext().newMessage();
    message.add(FIELD_ID_BBG_UNIQUE, "IX11084074-0");
    message.add(FIELD_ID_CUSIP, "EDU0");
    message.add(FIELD_PARSEKYABLE_DES, "EDU0 Comdty");
    message.add(FIELD_FUT_FIRST_TRADE_DT, "2010-09-14");
    message.add(FIELD_FUT_LAST_TRADE_DT, "2020-09-14");
    
    ExternalIdBundleWithDates actual = BloombergDataUtils.parseIdentifiers(message, FIELD_FUT_FIRST_TRADE_DT, FIELD_FUT_LAST_TRADE_DT);
    assertEquals(expected, actual);
  }

  @Test
  public void identifierLoader() throws Exception {
    Set<ExternalId> identifiers = BloombergDataUtils.identifierLoader(new StringReader(multiStringLine(IDENTIFIERS)));
    assertNotNull(identifiers);
    assertEquals(6, identifiers.size());
    assertTrue(identifiers.contains(ExternalId.of("ISIN", "ISIN 1234")));
    assertTrue(identifiers.contains(ExternalId.of("BLOOMBERG_BUID", "BUID 1234")));
    assertTrue(identifiers.contains(ExternalId.of("BLOOMBERG_TICKER", "TICKER 1234")));
    assertTrue(identifiers.contains(ExternalId.of("CUSIP", "CUSIP 1234")));
    assertTrue(identifiers.contains(ExternalId.of("SEDOL1", "SEDOL1 1234")));
    assertTrue(identifiers.contains(ExternalId.of("BLOOMBERG_TCM", "BLOOMBERG_TCM 1234")));
  }

  @Test
  public void getBUID() throws Exception {
    MockReferenceDataProvider rdp = new MockReferenceDataProvider();
    rdp.addExpectedField(FIELD_ID_BBG_UNIQUE);
    rdp.addResult("FIRST US Equity", FIELD_ID_BBG_UNIQUE, "EQ1234");
    rdp.addResult("SECOND US Equity", FIELD_ID_BBG_UNIQUE, "EQ4321");
    
    Map<String, String> buids = BloombergDataUtils.getBUID(rdp, Sets.newHashSet("FIRST US Equity", "SECOND US Equity"));
    assertNotNull(buids);
    assertEquals(2, buids.size());
    assertEquals("EQ1234", buids.get("FIRST US Equity"));
    assertEquals("EQ4321", buids.get("SECOND US Equity"));
  }

  private static String multiStringLine(String... lines) {
    StringBuilder buf = new StringBuilder();
    for (String string : lines) {
      buf.append(string);
      buf.append(System.getProperty("line.separator"));
    }
    return buf.toString();
  }
  
  @Test
  public void testFutureBundleToGenericFutureTicker() {
    ZonedDateTime now = LocalDateTime.of(2012, 8, 25, 14, 32, 00, 00).atZone(ZoneId.of("Europe/London"));
    ExternalId testInput1 = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "EDZ2 Comdty");
    ExternalId expectedOutput1 = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "ED2 Comdty");
    ExternalId actualOutput1 = BloombergDataUtils.futureBundleToGenericFutureTicker(testInput1.toBundle(), now, LocalTime.of(15, 00).atOffset(ZoneOffset.ofHours(1)), ZoneId.of("Europe/London"));
    assertEquals(expectedOutput1, actualOutput1);
    ExternalId testInput2 = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "EDZ1 Comdty");
    ExternalId expectedOutput2 = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "ED37 Comdty");
    ExternalId actualOutput2 = BloombergDataUtils.futureBundleToGenericFutureTicker(testInput2.toBundle(), now, LocalTime.of(15, 00).atOffset(ZoneOffset.ofHours(1)), ZoneId.of("Europe/London"));
    assertEquals(expectedOutput2, actualOutput2);
  }

  public void test_resolveObservationTime() {
    assertEquals(HistoricalTimeSeriesConstants.DEFAULT_OBSERVATION_TIME, BloombergDataUtils.resolveObservationTime(null));
    assertEquals(HistoricalTimeSeriesConstants.DEFAULT_OBSERVATION_TIME, BloombergDataUtils.resolveObservationTime("UNKNOWN"));
    assertNull(BloombergDataUtils.resolveObservationTime("FOO"));
    assertEquals(HistoricalTimeSeriesConstants.LONDON_CLOSE, BloombergDataUtils.resolveObservationTime("CMPL"));
    assertEquals(HistoricalTimeSeriesConstants.NEWYORK_CLOSE, BloombergDataUtils.resolveObservationTime("CMPN"));
    assertEquals(HistoricalTimeSeriesConstants.TOKYO_CLOSE, BloombergDataUtils.resolveObservationTime("CMPT"));
  }

  @Test
  public void testBloombergDateTime() {
    LocalDate localDate = LocalDate.of(99999999, 12, 13);
    assertEquals(toBloombergDate(localDate), "99991213");
  }

}
