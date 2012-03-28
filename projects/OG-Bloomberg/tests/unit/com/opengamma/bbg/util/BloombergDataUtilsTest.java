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
import static com.opengamma.bbg.BloombergConstants.FIELD_PARSEKYABLE_DES;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.LocalDate;
import javax.time.calendar.MonthOfYear;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.opengamma.bbg.CachingReferenceDataProvider;
import com.opengamma.bbg.test.BloombergLiveDataServerUtils;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.id.ExternalIdWithDates;

/**
 * 
 */
public class BloombergDataUtilsTest {

  @SuppressWarnings("unused")
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergDataUtilsTest.class);
  
  private CachingReferenceDataProvider _refDataProvider;
  
  private static final String[] IDENTIFIERS = new String[] {
      "#Comment",
      "ISIN~ISIN 1234",
      "BLOOMBERG_BUID~BUID 1234",
      "BLOOMBERG_TICKER~TICKER 1234",
      "CUSIP~CUSIP 1234",
      "SEDOL1~SEDOL1 1234",
      "BLOOMBERG_TCM~BLOOMBERG_TCM 1234"};
  
  @BeforeMethod
  public void setupBloombergSecuritySource(Method m) {
    _refDataProvider = BloombergLiveDataServerUtils.getCachingReferenceDataProvider(m);
  }
  
  @AfterMethod
  public void terminateSecurityMaster() {
    BloombergLiveDataServerUtils.stopCachingReferenceDataProvider(_refDataProvider);
  }

  @Test
  // @Ignore("Because this contacts Bloomberg, we don't want to run all the time")
  public void aaplOptionChain() throws Exception {
    
    Set<ExternalId> optionChain = BloombergDataUtils.getOptionChain(_refDataProvider, "AAPL US Equity");
    assertNotNull(optionChain);
    assertFalse(optionChain.isEmpty());
  }

  @Test
  public void addTwoDigitYearCode() throws Exception {
    Set<ExternalIdWithDates> identifiers = Sets.newHashSet(
        ExternalIdWithDates.of(SecurityUtils.bloombergTickerSecurityId("EDU0 Comdty"),
            LocalDate.of(2010, MonthOfYear.SEPTEMBER, 14), LocalDate.of(2020, MonthOfYear.SEPTEMBER, 14)),
        ExternalIdWithDates.of(SecurityUtils.bloombergBuidSecurityId("IX11084074-0"), null, null));

    ExternalIdBundleWithDates withTwoDigits = BloombergDataUtils.addTwoDigitYearCode(new ExternalIdBundleWithDates(identifiers));
    assertTrue(withTwoDigits.size() == 3);
    for (ExternalIdWithDates identifierWithDates : identifiers) {
      assertTrue(withTwoDigits.contains(identifierWithDates));
    }
    assertTrue(withTwoDigits.contains(ExternalIdWithDates.of(
        SecurityUtils.bloombergTickerSecurityId("EDU20 Comdty"),
        LocalDate.of(2010, MonthOfYear.SEPTEMBER, 14), LocalDate.of(2020, MonthOfYear.SEPTEMBER, 14))));
    
    identifiers = Sets.newHashSet(
        ExternalIdWithDates.of(SecurityUtils.bloombergTickerSecurityId("EDU09 Comdty"),
            LocalDate.of(1999, MonthOfYear.SEPTEMBER, 14), LocalDate.of(2009, MonthOfYear.SEPTEMBER, 14)),
        ExternalIdWithDates.of(SecurityUtils.bloombergBuidSecurityId("IX9471080-0"), null, null));
    withTwoDigits = BloombergDataUtils.addTwoDigitYearCode(new ExternalIdBundleWithDates(identifiers));
    assertTrue(withTwoDigits.size() == 3);
    for (ExternalIdWithDates identifierWithDates : identifiers) {
      assertTrue(withTwoDigits.contains(identifierWithDates));
    }
    assertTrue(withTwoDigits.contains(ExternalIdWithDates.of(
        SecurityUtils.bloombergTickerSecurityId("EDU9 Comdty"),
        LocalDate.of(1999, MonthOfYear.SEPTEMBER, 14), LocalDate.of(2009, MonthOfYear.SEPTEMBER, 14))));
  }

  @Test
  public void parseIdentifiers() throws Exception {
    Set<ExternalIdWithDates> identifiers = Sets.newHashSet(
        ExternalIdWithDates.of(SecurityUtils.bloombergTickerSecurityId("EDU0 Comdty"),
            LocalDate.of(2010, MonthOfYear.SEPTEMBER, 14), LocalDate.of(2020, MonthOfYear.SEPTEMBER, 14)),
        ExternalIdWithDates.of(SecurityUtils.bloombergBuidSecurityId("IX11084074-0"), null, null),
        ExternalIdWithDates.of(SecurityUtils.cusipSecurityId("EDU0"), null, null));

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
    Map<String, String> buids = BloombergDataUtils.getBUID(_refDataProvider, Sets.newHashSet("AAPL US Equity", "IBM US Equity"));
    assertNotNull(buids);
    assertEquals(2, buids.size());
    assertEquals("EQ0010169500001000", buids.get("AAPL US Equity"));
    assertEquals("EQ0010080100001000", buids.get("IBM US Equity"));
  }
  
  private static String multiStringLine(String... lines) {
    StringBuilder buf = new StringBuilder();
    for (String string : lines) {
      buf.append(string);
      buf.append(System.getProperty("line.separator"));
    }
    return buf.toString();
  }
  
  

}
