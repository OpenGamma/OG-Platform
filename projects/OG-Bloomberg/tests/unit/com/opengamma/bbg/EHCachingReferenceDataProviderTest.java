/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.fudgemsg.FudgeMsg;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.bbg.util.MockReferenceDataProvider;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.tuple.Pair;

/**
 * Test.
 */
@Test(groups = "unit")
public class EHCachingReferenceDataProviderTest {

  private static final String CISCO_TICKER = "CSCO US Equity";
  private static final String FIELD_ID_ISIN = "ID_ISIN";
  private static final String FIELD_ID_CUSIP = "ID_CUSIP";
  private static final String FIELD_TICKER = "TICKER";
  private static final String FIELD_ID_BB_UNIQUE = "ID_BB_UNIQUE";
  private static final String AAPL_TICKER = "AAPL US Equity";

  private MockReferenceDataProvider _underlyingProvider = null;
  private UnitTestingReferenceDataProvider _unitProvider = null;
  private EHCachingReferenceDataProvider _cachingProvider = null;

  @BeforeMethod
  public void setUp(Method m) {
    EHCacheUtils.clearAll();
    
    _underlyingProvider = new MockReferenceDataProvider();
    _unitProvider = new UnitTestingReferenceDataProvider(_underlyingProvider);
    _cachingProvider = new EHCachingReferenceDataProvider(
        _unitProvider, 
        EHCacheUtils.createCacheManager(), 
        OpenGammaFudgeContext.getInstance());
  }

  //-------------------------------------------------------------------------
  @Test
  public void numberOfReturnedFields() {
    _underlyingProvider.addExpectedField(FIELD_ID_BB_UNIQUE);
    _underlyingProvider.addExpectedField(FIELD_TICKER);
    _underlyingProvider.addResult(AAPL_TICKER, FIELD_ID_BB_UNIQUE, "BUID");
    _underlyingProvider.addResult(AAPL_TICKER, FIELD_TICKER, "TICKER");
    
    EHCachingReferenceDataProvider provider = _cachingProvider;
    
    Set<String> fields = new TreeSet<String>();
    fields.add(FIELD_ID_BB_UNIQUE);
    fields.add(FIELD_TICKER);
    String securityDes = AAPL_TICKER;
    Set<String> securities = Collections.singleton(securityDes);
    _unitProvider.addAcceptableRequest(securities, fields);
    ReferenceDataResult result = provider.getFields(securities, fields);
    assertNotNull(result);
    PerSecurityReferenceDataResult perSecurity = result.getResult(securityDes);
    assertNotNull(perSecurity);
    FudgeMsg fieldData = perSecurity.getFieldData();
    assertNotNull(fieldData);
    assertEquals(2, fieldData.getNumFields());
    
    fields.clear();
    fields.add(FIELD_TICKER);
    _unitProvider.clearAcceptableRequests();
    result = provider.getFields(securities, fields);
    assertNotNull(result);
    perSecurity = result.getResult(securityDes);
    assertNotNull(perSecurity);
    fieldData = perSecurity.getFieldData();
    assertNotNull(fieldData);
    assertEquals(1, fieldData.getNumFields());
  }

  @Test
  public void singleSecurityEscalatingFields() {
    _underlyingProvider.addResult(AAPL_TICKER, FIELD_ID_BB_UNIQUE, "BUID");
    _underlyingProvider.addResult(AAPL_TICKER, FIELD_TICKER, "TICKER");
    _underlyingProvider.addResult(AAPL_TICKER, FIELD_ID_CUSIP, "CUSIP");
    _underlyingProvider.addResult(AAPL_TICKER, FIELD_ID_ISIN, "ISIN");
    
    EHCachingReferenceDataProvider provider = _cachingProvider;
    
    Set<String> fields = new TreeSet<String>();
    fields.add(FIELD_ID_BB_UNIQUE);
    fields.add(FIELD_TICKER);
    String securityDes = AAPL_TICKER;
    Set<String> securities = Collections.singleton(securityDes);
    _unitProvider.addAcceptableRequest(securities, fields);
    ReferenceDataResult result = provider.getFields(securities, fields);
    assertNotNull(result);
    assertNotNull(result.getResult(securityDes));

    _unitProvider.clearAcceptableRequests();
    result = provider.getFields(securities, fields);
    assertNotNull(result);
    assertNotNull(result.getResult(securityDes));
    
    fields.add(FIELD_ID_CUSIP);
    fields.add(FIELD_ID_ISIN);
    _unitProvider.clearAcceptableRequests();
    Set<String> expectedFields = new TreeSet<String>();
    expectedFields.add(FIELD_ID_CUSIP);
    expectedFields.add(FIELD_ID_ISIN);
    _unitProvider.addAcceptableRequest(securities, expectedFields);
    result = provider.getFields(securities, fields);
    assertNotNull(result);
    assertNotNull(result.getResult(securityDes));
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
    
    EHCachingReferenceDataProvider provider = _cachingProvider;
    
    Set<String> fields = new TreeSet<String>();
    fields.add(FIELD_ID_BB_UNIQUE);
    fields.add("INVALID_FIELD1");
    fields.add("INVALID_FIELD2");
    fields.add("INVALID_FIELD3");
    
    Set<String> securities = new TreeSet<String>();
    securities.add(AAPL_TICKER);
    securities.add(CISCO_TICKER);
    
    _unitProvider.addAcceptableRequest(securities, fields);
    ReferenceDataResult result = provider.getFields(securities, fields);
    assertNotNull(result);
    PerSecurityReferenceDataResult aaplResult = result.getResult(AAPL_TICKER);
    assertNotNull(aaplResult);
    PerSecurityReferenceDataResult ciscoResult = result.getResult(CISCO_TICKER);
    assertNotNull(ciscoResult);
    
    _unitProvider.clearAcceptableRequests();
    result = provider.getFields(securities, fields);
    assertNotNull(result);
    PerSecurityReferenceDataResult aaplCachedResult = result.getResult(AAPL_TICKER);
    PerSecurityReferenceDataResult ciscoCachedResult = result.getResult(CISCO_TICKER);
    
    assertNotNull(aaplCachedResult);
    assertEquals(aaplResult.getSecurity(), aaplCachedResult.getSecurity());
    assertEquals(aaplResult.getFieldData(), aaplCachedResult.getFieldData());
    
    assertNotNull(ciscoCachedResult);
    assertEquals(ciscoResult.getSecurity(), ciscoCachedResult.getSecurity());
    assertEquals(ciscoResult.getFieldData(), ciscoCachedResult.getFieldData());
  }

  @Test
  public void securityNotAvailable() {
    _underlyingProvider.addResult("INVALID", FIELD_ID_BB_UNIQUE, null);
    
    EHCachingReferenceDataProvider provider = _cachingProvider;
    String invalidSec = "INVALID";
    
    Set<String> fields = new TreeSet<String>();
    fields.add(FIELD_ID_BB_UNIQUE);
    
    Set<String> securities = Collections.singleton(invalidSec);
    
    _unitProvider.addAcceptableRequest(securities, fields);
    ReferenceDataResult result = provider.getFields(securities, fields);
    assertNotNull(result);
    assertNotNull(result.getResult(invalidSec));
    
    _unitProvider.clearAcceptableRequests();
    
    result = provider.getFields(securities, fields);
    assertNotNull(result);
    assertNotNull(result.getResult(invalidSec));
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
    
    EHCachingReferenceDataProvider provider = _cachingProvider;
    
    Set<String> fields = new TreeSet<String>();
    fields.add(FIELD_ID_BB_UNIQUE);
    fields.add(FIELD_TICKER);
    Set<String> aaplOnly = Collections.singleton(AAPL_TICKER);
    Set<String> cscoOnly = Collections.singleton(CISCO_TICKER);
    Set<String> bothSecurities = new TreeSet<String>();
    bothSecurities.addAll(aaplOnly);
    bothSecurities.addAll(cscoOnly);
    
    _unitProvider.addAcceptableRequest(aaplOnly, fields);
    ReferenceDataResult result = provider.getFields(aaplOnly, fields);
    assertNotNull(result);
    
    _unitProvider.clearAcceptableRequests();
    _unitProvider.addAcceptableRequest(cscoOnly, fields);
    result = provider.getFields(cscoOnly, fields);
    assertNotNull(result);

    _unitProvider.clearAcceptableRequests();
    result = provider.getFields(bothSecurities, fields);
    assertNotNull(result);
    
    fields.add(FIELD_ID_CUSIP);
    fields.add(FIELD_ID_ISIN);
    _unitProvider.clearAcceptableRequests();
    Set<String> expectedFields = new TreeSet<String>();
    expectedFields.add(FIELD_ID_CUSIP);
    expectedFields.add(FIELD_ID_ISIN);
    _unitProvider.addAcceptableRequest(bothSecurities, expectedFields);
    result = provider.getFields(bothSecurities, fields);
    assertNotNull(result);
  }

  //-------------------------------------------------------------------------
  public static class UnitTestingReferenceDataProvider implements ReferenceDataProvider {
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
    public ReferenceDataResult getFields(Set<String> securities,
        Set<String> fields) {
      Pair<Set<String>, Set<String>> request = Pair.of(securities, fields);
      assertTrue(_acceptableRequests.contains(request));
      return _underlying.getFields(securities, fields);
    }
    
  }

}
