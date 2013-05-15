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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.bbg.referencedata.MockReferenceDataProvider;
import com.opengamma.bbg.referencedata.ReferenceData;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetRequest;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetResult;
import com.opengamma.bbg.referencedata.impl.AbstractReferenceDataProvider;
import com.opengamma.util.tuple.Pair;

/**
 * Test.
 */
public abstract class AbstractValueCachingReferenceDataProviderTestCase {

  private static final String CISCO_TICKER = "CSCO US Equity";
  private static final String FIELD_ID_ISIN = "ID_ISIN";
  private static final String FIELD_ID_CUSIP = "ID_CUSIP";
  private static final String FIELD_TICKER = "TICKER";
  private static final String FIELD_ID_BB_UNIQUE = "ID_BB_UNIQUE";
  private static final String AAPL_TICKER = "AAPL US Equity";

  private MockReferenceDataProvider _underlyingProvider;
  private UnitTestingReferenceDataProvider _unitProvider;
  private ReferenceDataProvider _cachingProvider;

  @BeforeMethod
  public void setUp() {
    _underlyingProvider = new MockReferenceDataProvider();
    _unitProvider = new UnitTestingReferenceDataProvider(_underlyingProvider);
    _cachingProvider = createCachingProvider();
  }

  protected final ReferenceDataProvider getUnderlyingProvider() {
    return _unitProvider;
  }

  protected final ReferenceDataProvider getCachingProvider() {
    return _cachingProvider;
  }

  protected abstract ReferenceDataProvider createCachingProvider();

  //-------------------------------------------------------------------------
  @Test
  public void numberOfReturnedFields() {
    _underlyingProvider.addExpectedField(FIELD_ID_BB_UNIQUE);
    _underlyingProvider.addExpectedField(FIELD_TICKER);
    _underlyingProvider.addResult(AAPL_TICKER, FIELD_ID_BB_UNIQUE, "BUID");
    _underlyingProvider.addResult(AAPL_TICKER, FIELD_TICKER, "TICKER");
    
    Set<String> fields = new TreeSet<String>();
    fields.add(FIELD_ID_BB_UNIQUE);
    fields.add(FIELD_TICKER);
    String securityDes = AAPL_TICKER;
    Set<String> securities = Collections.singleton(securityDes);
    _unitProvider.addAcceptableRequest(securities, fields);
    ReferenceDataProviderGetResult result = getCachingProvider().getReferenceData(
        ReferenceDataProviderGetRequest.createGet(securities, fields, true));
    assertNotNull(result);
    ReferenceData perSecurity = result.getReferenceData(securityDes);
    assertNotNull(perSecurity);
    FudgeMsg fieldData = perSecurity.getFieldValues();
    assertNotNull(fieldData);
    assertEquals(2, fieldData.getNumFields());
    
    fields.clear();
    fields.add(FIELD_TICKER);
    _unitProvider.clearAcceptableRequests();
    result = getCachingProvider().getReferenceData(
        ReferenceDataProviderGetRequest.createGet(securities, fields, true));
    assertNotNull(result);
    perSecurity = result.getReferenceData(securityDes);
    assertNotNull(perSecurity);
    fieldData = perSecurity.getFieldValues();
    assertNotNull(fieldData);
    assertEquals(1, fieldData.getNumFields());
  }

  @Test
  public void singleSecurityEscalatingFields() {
    _underlyingProvider.addResult(AAPL_TICKER, FIELD_ID_BB_UNIQUE, "BUID");
    _underlyingProvider.addResult(AAPL_TICKER, FIELD_TICKER, "TICKER");
    _underlyingProvider.addResult(AAPL_TICKER, FIELD_ID_CUSIP, "CUSIP");
    _underlyingProvider.addResult(AAPL_TICKER, FIELD_ID_ISIN, "ISIN");
    
    Set<String> fields = new TreeSet<String>();
    fields.add(FIELD_ID_BB_UNIQUE);
    fields.add(FIELD_TICKER);
    String securityDes = AAPL_TICKER;
    Set<String> securities = Collections.singleton(securityDes);
    _unitProvider.addAcceptableRequest(securities, fields);
    ReferenceDataProviderGetResult result = getCachingProvider().getReferenceData(
        ReferenceDataProviderGetRequest.createGet(securities, fields, true));
    assertNotNull(result);
    assertNotNull(result.getReferenceData(securityDes));

    _unitProvider.clearAcceptableRequests();
    result = getCachingProvider().getReferenceData(
        ReferenceDataProviderGetRequest.createGet(securities, fields, true));
    assertNotNull(result);
    assertNotNull(result.getReferenceData(securityDes));
    
    fields.add(FIELD_ID_CUSIP);
    fields.add(FIELD_ID_ISIN);
    _unitProvider.clearAcceptableRequests();
    Set<String> expectedFields = new TreeSet<String>();
    expectedFields.add(FIELD_ID_CUSIP);
    expectedFields.add(FIELD_ID_ISIN);
    _unitProvider.addAcceptableRequest(securities, expectedFields);
    result = getCachingProvider().getReferenceData(
        ReferenceDataProviderGetRequest.createGet(securities, fields, true));
    assertNotNull(result);
    assertNotNull(result.getReferenceData(securityDes));
  }

  @Test
  public void fieldNotAvailable() {
    _underlyingProvider.addResult(AAPL_TICKER, FIELD_ID_BB_UNIQUE, "BUID");
    _underlyingProvider.addResult(AAPL_TICKER, "INVALID_FIELD1", null);
    _underlyingProvider.addResult(AAPL_TICKER, "INVALID_FIELD2", null);
    _underlyingProvider.addResult(AAPL_TICKER, "INVALID_FIELD3", null);
    _underlyingProvider.addResult(CISCO_TICKER, FIELD_ID_BB_UNIQUE, "BUID");
    _underlyingProvider.addResult(CISCO_TICKER, "INVALID_FIELD1", null);
    _underlyingProvider.addResult(CISCO_TICKER, "INVALID_FIELD2", null);
    _underlyingProvider.addResult(CISCO_TICKER, "INVALID_FIELD3", null);
    
    Set<String> fields = new TreeSet<String>();
    fields.add(FIELD_ID_BB_UNIQUE);
    fields.add("INVALID_FIELD1");
    fields.add("INVALID_FIELD2");
    fields.add("INVALID_FIELD3");
    
    Set<String> securities = new TreeSet<String>();
    securities.add(AAPL_TICKER);
    securities.add(CISCO_TICKER);
    
    _unitProvider.addAcceptableRequest(securities, fields);
    ReferenceDataProviderGetResult result = getCachingProvider().getReferenceData(
        ReferenceDataProviderGetRequest.createGet(securities, fields, true));
    assertNotNull(result);
    ReferenceData aaplResult = result.getReferenceData(AAPL_TICKER);
    assertNotNull(aaplResult);
    ReferenceData ciscoResult = result.getReferenceData(CISCO_TICKER);
    assertNotNull(ciscoResult);
    
    _unitProvider.clearAcceptableRequests();
    result = getCachingProvider().getReferenceData(
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

  @Test
  public void securityNotAvailable() {
    _underlyingProvider.addResult("INVALID", FIELD_ID_BB_UNIQUE, null);
    
    String invalidSec = "INVALID";
    
    Set<String> fields = new TreeSet<String>();
    fields.add(FIELD_ID_BB_UNIQUE);
    
    Set<String> securities = Collections.singleton(invalidSec);
    
    _unitProvider.addAcceptableRequest(securities, fields);
    ReferenceDataProviderGetResult result = getCachingProvider().getReferenceData(
        ReferenceDataProviderGetRequest.createGet(securities, fields, true));
    assertNotNull(result);
    assertNotNull(result.getReferenceData(invalidSec));
    
    _unitProvider.clearAcceptableRequests();
    
    result = getCachingProvider().getReferenceData(
        ReferenceDataProviderGetRequest.createGet(securities, fields, true));
    assertNotNull(result);
    assertNotNull(result.getReferenceData(invalidSec));
  }

  @Test
  public void multipleSecuritiesSameEscalatingFields() {
    _underlyingProvider.addResult(AAPL_TICKER, FIELD_ID_BB_UNIQUE, "A");
    _underlyingProvider.addResult(AAPL_TICKER, FIELD_TICKER, "B");
    _underlyingProvider.addResult(AAPL_TICKER, FIELD_ID_CUSIP, "C");
    _underlyingProvider.addResult(AAPL_TICKER, FIELD_ID_ISIN, "D");
    _underlyingProvider.addResult(CISCO_TICKER, FIELD_ID_BB_UNIQUE, "A");
    _underlyingProvider.addResult(CISCO_TICKER, FIELD_TICKER, "B");
    _underlyingProvider.addResult(CISCO_TICKER, FIELD_ID_CUSIP, "C");
    _underlyingProvider.addResult(CISCO_TICKER, FIELD_ID_ISIN, "D");
    
    Set<String> fields = new TreeSet<String>();
    fields.add(FIELD_ID_BB_UNIQUE);
    fields.add(FIELD_TICKER);
    Set<String> aaplOnly = Collections.singleton(AAPL_TICKER);
    Set<String> cscoOnly = Collections.singleton(CISCO_TICKER);
    Set<String> bothSecurities = new TreeSet<String>();
    bothSecurities.addAll(aaplOnly);
    bothSecurities.addAll(cscoOnly);
    
    _unitProvider.addAcceptableRequest(aaplOnly, fields);
    ReferenceDataProviderGetResult result = getCachingProvider().getReferenceData(
        ReferenceDataProviderGetRequest.createGet(aaplOnly, fields, true));
    assertNotNull(result);
    
    _unitProvider.clearAcceptableRequests();
    _unitProvider.addAcceptableRequest(cscoOnly, fields);
    result = getCachingProvider().getReferenceData(
        ReferenceDataProviderGetRequest.createGet(cscoOnly, fields, true));
    assertNotNull(result);

    _unitProvider.clearAcceptableRequests();
    result = getCachingProvider().getReferenceData(
        ReferenceDataProviderGetRequest.createGet(bothSecurities, fields, true));
    assertNotNull(result);
    
    fields.add(FIELD_ID_CUSIP);
    fields.add(FIELD_ID_ISIN);
    _unitProvider.clearAcceptableRequests();
    Set<String> expectedFields = new TreeSet<String>();
    expectedFields.add(FIELD_ID_CUSIP);
    expectedFields.add(FIELD_ID_ISIN);
    _unitProvider.addAcceptableRequest(bothSecurities, expectedFields);
    result = getCachingProvider().getReferenceData(
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
      Pair<Set<String>, Set<String>> request = Pair.of(securities, fields);
      _acceptableRequests.add(request);
    }

    @Override
    protected ReferenceDataProviderGetResult doBulkGet(ReferenceDataProviderGetRequest request) {
      Pair<Set<String>, Set<String>> pair = Pair.of(request.getIdentifiers(), request.getFields());
      assertTrue(_acceptableRequests.contains(pair));
      return _underlying.getReferenceData(request);
    }
  }

}
