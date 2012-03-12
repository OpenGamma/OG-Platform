/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.fudgemsg.FudgeMsg;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.bbg.test.BloombergLiveDataServerUtils;
import com.opengamma.bbg.test.MongoCachedReferenceData;
import com.opengamma.util.tuple.Pair;

/**
 * NOTE: This test will make real bloomberg queries, so run it sparingly.
 *
 * @author kirk
 */
public class MongoDBCachingReferenceDataProviderTest {
  
  private static final String CISCO_TICKER = "CSCO US Equity";
  private static final String FIELD_ID_ISIN = "ID_ISIN";
  private static final String FIELD_ID_CUSIP = "ID_CUSIP";
  private static final String FIELD_TICKER = "TICKER";
  private static final String FIELD_ID_BB_UNIQUE = "ID_BB_UNIQUE";
  private static final String AAPL_TICKER = "AAPL US Equity";
  
  private UnitTestingReferenceDataProvider _unitProvider = null;
  private MongoDBCachingReferenceDataProvider _mongoProvider = null;
  private BloombergReferenceDataProvider _underlying;
  
  @BeforeMethod
  public void setupMongoDBCaching() {
    _underlying = BloombergLiveDataServerUtils.getUnderlyingProvider();
    _unitProvider = new UnitTestingReferenceDataProvider(_underlying);
    boolean clearData = true; // This is why we make real queries
    _mongoProvider = MongoCachedReferenceData.makeMongoProvider(_unitProvider, MongoDBCachingReferenceDataProviderTest.class, clearData);
  }
  
  @AfterMethod
  public void terminateMongoDBCaching() {
    _underlying.stop();
  }
  
  @Test
  public void numberOfReturnedFields() {
    MongoDBCachingReferenceDataProvider provider = _mongoProvider;
    
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
    MongoDBCachingReferenceDataProvider provider = _mongoProvider;
    
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
    
    MongoDBCachingReferenceDataProvider provider = _mongoProvider;
    
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
 MongoDBCachingReferenceDataProvider provider = _mongoProvider;
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
    MongoDBCachingReferenceDataProvider provider = _mongoProvider;
    
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
