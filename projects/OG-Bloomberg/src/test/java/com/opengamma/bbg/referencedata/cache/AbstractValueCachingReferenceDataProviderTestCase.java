/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.referencedata.cache;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.fudgemsg.FudgeMsg;

import com.opengamma.bbg.referencedata.MockReferenceDataProvider;
import com.opengamma.bbg.referencedata.ReferenceData;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetRequest;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetResult;
import com.opengamma.bbg.referencedata.impl.AbstractReferenceDataProvider;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Test.
 */
public abstract class AbstractValueCachingReferenceDataProviderTestCase {
  // Abstract class didn't work well with TestNG groups and Maven
  // All annotations and local variables moved to subclasses to make it work

  private static final String CISCO_TICKER = "CSCO US Equity";
  private static final String FIELD_ID_ISIN = "ID_ISIN";
  private static final String FIELD_ID_CUSIP = "ID_CUSIP";
  private static final String FIELD_TICKER = "TICKER";
  private static final String FIELD_ID_BB_UNIQUE = "ID_BB_UNIQUE";
  private static final String AAPL_TICKER = "AAPL US Equity";

  //-------------------------------------------------------------------------
  protected abstract MockReferenceDataProvider getUnderlyingProvider();

  protected abstract UnitTestingReferenceDataProvider getUnitProvider();

  protected abstract ReferenceDataProvider getProvider();

  //-------------------------------------------------------------------------
  protected void numberOfReturnedFields() {
    getUnderlyingProvider().addExpectedField(FIELD_ID_BB_UNIQUE);
    getUnderlyingProvider().addExpectedField(FIELD_TICKER);
    getUnderlyingProvider().addResult(AAPL_TICKER, FIELD_ID_BB_UNIQUE, "BUID");
    getUnderlyingProvider().addResult(AAPL_TICKER, FIELD_TICKER, "TICKER");
    
    Set<String> fields = new TreeSet<String>();
    fields.add(FIELD_ID_BB_UNIQUE);
    fields.add(FIELD_TICKER);
    String securityDes = AAPL_TICKER;
    Set<String> securities = Collections.singleton(securityDes);
    getUnitProvider().addAcceptableRequest(securities, fields);
    ReferenceDataProviderGetResult result = getProvider().getReferenceData(
        ReferenceDataProviderGetRequest.createGet(securities, fields, true));
    assertNotNull(result);
    ReferenceData perSecurity = result.getReferenceData(securityDes);
    assertNotNull(perSecurity);
    FudgeMsg fieldData = perSecurity.getFieldValues();
    assertNotNull(fieldData);
    assertEquals(2, fieldData.getNumFields());
    
    fields.clear();
    fields.add(FIELD_TICKER);
    getUnitProvider().clearAcceptableRequests();
    result = getProvider().getReferenceData(
        ReferenceDataProviderGetRequest.createGet(securities, fields, true));
    assertNotNull(result);
    perSecurity = result.getReferenceData(securityDes);
    assertNotNull(perSecurity);
    fieldData = perSecurity.getFieldValues();
    assertNotNull(fieldData);
    assertEquals(1, fieldData.getNumFields());
  }

  protected void singleSecurityEscalatingFields() {
    getUnderlyingProvider().addResult(AAPL_TICKER, FIELD_ID_BB_UNIQUE, "BUID");
    getUnderlyingProvider().addResult(AAPL_TICKER, FIELD_TICKER, "TICKER");
    getUnderlyingProvider().addResult(AAPL_TICKER, FIELD_ID_CUSIP, "CUSIP");
    getUnderlyingProvider().addResult(AAPL_TICKER, FIELD_ID_ISIN, "ISIN");
    
    Set<String> fields = new TreeSet<String>();
    fields.add(FIELD_ID_BB_UNIQUE);
    fields.add(FIELD_TICKER);
    String securityDes = AAPL_TICKER;
    Set<String> securities = Collections.singleton(securityDes);
    getUnitProvider().addAcceptableRequest(securities, fields);
    ReferenceDataProviderGetResult result = getProvider().getReferenceData(
        ReferenceDataProviderGetRequest.createGet(securities, fields, true));
    assertNotNull(result);
    assertNotNull(result.getReferenceData(securityDes));

    getUnitProvider().clearAcceptableRequests();
    result = getProvider().getReferenceData(
        ReferenceDataProviderGetRequest.createGet(securities, fields, true));
    assertNotNull(result);
    assertNotNull(result.getReferenceData(securityDes));
    
    fields.add(FIELD_ID_CUSIP);
    fields.add(FIELD_ID_ISIN);
    getUnitProvider().clearAcceptableRequests();
    Set<String> expectedFields = new TreeSet<String>();
    expectedFields.add(FIELD_ID_CUSIP);
    expectedFields.add(FIELD_ID_ISIN);
    getUnitProvider().addAcceptableRequest(securities, expectedFields);
    result = getProvider().getReferenceData(
        ReferenceDataProviderGetRequest.createGet(securities, fields, true));
    assertNotNull(result);
    assertNotNull(result.getReferenceData(securityDes));
  }

  protected void fieldNotAvailable() {
    getUnderlyingProvider().addResult(AAPL_TICKER, FIELD_ID_BB_UNIQUE, "BUID");
    getUnderlyingProvider().addResult(AAPL_TICKER, "INVALID_FIELD1", null);
    getUnderlyingProvider().addResult(AAPL_TICKER, "INVALID_FIELD2", null);
    getUnderlyingProvider().addResult(AAPL_TICKER, "INVALID_FIELD3", null);
    getUnderlyingProvider().addResult(CISCO_TICKER, FIELD_ID_BB_UNIQUE, "BUID");
    getUnderlyingProvider().addResult(CISCO_TICKER, "INVALID_FIELD1", null);
    getUnderlyingProvider().addResult(CISCO_TICKER, "INVALID_FIELD2", null);
    getUnderlyingProvider().addResult(CISCO_TICKER, "INVALID_FIELD3", null);
    
    Set<String> fields = new TreeSet<String>();
    fields.add(FIELD_ID_BB_UNIQUE);
    fields.add("INVALID_FIELD1");
    fields.add("INVALID_FIELD2");
    fields.add("INVALID_FIELD3");
    
    Set<String> securities = new TreeSet<String>();
    securities.add(AAPL_TICKER);
    securities.add(CISCO_TICKER);
    
    getUnitProvider().addAcceptableRequest(securities, fields);
    ReferenceDataProviderGetResult result = getProvider().getReferenceData(
        ReferenceDataProviderGetRequest.createGet(securities, fields, true));
    assertNotNull(result);
    ReferenceData aaplResult = result.getReferenceData(AAPL_TICKER);
    assertNotNull(aaplResult);
    ReferenceData ciscoResult = result.getReferenceData(CISCO_TICKER);
    assertNotNull(ciscoResult);
    
    getUnitProvider().clearAcceptableRequests();
    result = getProvider().getReferenceData(
        ReferenceDataProviderGetRequest.createGet(securities, fields, true));
    assertNotNull(result);
    ReferenceData aaplCachedResult = result.getReferenceData(AAPL_TICKER);
    ReferenceData ciscoCachedResult = result.getReferenceData(CISCO_TICKER);
    
    assertNotNull(aaplCachedResult);
    assertEquals(aaplResult.getIdentifier(), aaplCachedResult.getIdentifier());
    assertEquals(aaplResult.getFieldValues(), aaplCachedResult.getFieldValues());
    
    assertNotNull(ciscoCachedResult);
    assertEquals(ciscoResult.getIdentifier(), ciscoCachedResult.getIdentifier());
    assertEquals(ciscoResult.getFieldValues(), ciscoCachedResult.getFieldValues());
  }

  protected void securityNotAvailable() {
    getUnderlyingProvider().addResult("INVALID", FIELD_ID_BB_UNIQUE, null);
    
    String invalidSec = "INVALID";
    
    Set<String> fields = new TreeSet<String>();
    fields.add(FIELD_ID_BB_UNIQUE);
    
    Set<String> securities = Collections.singleton(invalidSec);
    
    getUnitProvider().addAcceptableRequest(securities, fields);
    ReferenceDataProviderGetResult result = getProvider().getReferenceData(
        ReferenceDataProviderGetRequest.createGet(securities, fields, true));
    assertNotNull(result);
    assertNotNull(result.getReferenceData(invalidSec));
    
    getUnitProvider().clearAcceptableRequests();
    
    result = getProvider().getReferenceData(
        ReferenceDataProviderGetRequest.createGet(securities, fields, true));
    assertNotNull(result);
    assertNotNull(result.getReferenceData(invalidSec));
  }

  protected void multipleSecuritiesSameEscalatingFields() {
    getUnderlyingProvider().addResult(AAPL_TICKER, FIELD_ID_BB_UNIQUE, "A");
    getUnderlyingProvider().addResult(AAPL_TICKER, FIELD_TICKER, "B");
    getUnderlyingProvider().addResult(AAPL_TICKER, FIELD_ID_CUSIP, "C");
    getUnderlyingProvider().addResult(AAPL_TICKER, FIELD_ID_ISIN, "D");
    getUnderlyingProvider().addResult(CISCO_TICKER, FIELD_ID_BB_UNIQUE, "A");
    getUnderlyingProvider().addResult(CISCO_TICKER, FIELD_TICKER, "B");
    getUnderlyingProvider().addResult(CISCO_TICKER, FIELD_ID_CUSIP, "C");
    getUnderlyingProvider().addResult(CISCO_TICKER, FIELD_ID_ISIN, "D");
    
    Set<String> fields = new TreeSet<String>();
    fields.add(FIELD_ID_BB_UNIQUE);
    fields.add(FIELD_TICKER);
    Set<String> aaplOnly = Collections.singleton(AAPL_TICKER);
    Set<String> cscoOnly = Collections.singleton(CISCO_TICKER);
    Set<String> bothSecurities = new TreeSet<String>();
    bothSecurities.addAll(aaplOnly);
    bothSecurities.addAll(cscoOnly);
    
    getUnitProvider().addAcceptableRequest(aaplOnly, fields);
    ReferenceDataProviderGetResult result = getProvider().getReferenceData(
        ReferenceDataProviderGetRequest.createGet(aaplOnly, fields, true));
    assertNotNull(result);
    
    getUnitProvider().clearAcceptableRequests();
    getUnitProvider().addAcceptableRequest(cscoOnly, fields);
    result = getProvider().getReferenceData(
        ReferenceDataProviderGetRequest.createGet(cscoOnly, fields, true));
    assertNotNull(result);

    getUnitProvider().clearAcceptableRequests();
    result = getProvider().getReferenceData(
        ReferenceDataProviderGetRequest.createGet(bothSecurities, fields, true));
    assertNotNull(result);
    
    fields.add(FIELD_ID_CUSIP);
    fields.add(FIELD_ID_ISIN);
    getUnitProvider().clearAcceptableRequests();
    Set<String> expectedFields = new TreeSet<String>();
    expectedFields.add(FIELD_ID_CUSIP);
    expectedFields.add(FIELD_ID_ISIN);
    getUnitProvider().addAcceptableRequest(bothSecurities, expectedFields);
    result = getProvider().getReferenceData(
        ReferenceDataProviderGetRequest.createGet(bothSecurities, fields, true));
    assertNotNull(result);
  }

  //-------------------------------------------------------------------------
  public static class UnitTestingReferenceDataProvider extends AbstractReferenceDataProvider {
    private final ReferenceDataProvider _underlying;
    private List<Pair<Set<String>, Set<String>>> _acceptableRequests =
      new ArrayList<Pair<Set<String>, Set<String>>>();
    
    public UnitTestingReferenceDataProvider(ReferenceDataProvider underlying) {
      _underlying = underlying;
    }
    
    public void clearAcceptableRequests() {
      _acceptableRequests.clear();
    }
    
    public void addAcceptableRequest(Set<String> securities, Set<String> fields) {
      Pair<Set<String>, Set<String>> request = Pairs.of(securities, fields);
      _acceptableRequests.add(request);
    }

    @Override
    protected ReferenceDataProviderGetResult doBulkGet(ReferenceDataProviderGetRequest request) {
      Pair<Set<String>, Set<String>> pair = Pairs.of(request.getIdentifiers(), request.getFields());
      assertTrue(_acceptableRequests.contains(pair));
      return _underlying.getReferenceData(request);
    }
  }

}
