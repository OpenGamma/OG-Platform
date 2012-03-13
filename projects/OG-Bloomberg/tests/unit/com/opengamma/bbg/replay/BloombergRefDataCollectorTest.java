/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.replay;


import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;

import javax.time.Instant;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.opengamma.bbg.CachingReferenceDataProvider;
import com.opengamma.bbg.PerSecurityReferenceDataResult;
import com.opengamma.bbg.ReferenceDataResult;
import com.opengamma.bbg.livedata.LoggedReferenceDataProvider;
import com.opengamma.bbg.test.BloombergLiveDataServerUtils;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Test BloombergRefDataCollector
 */
public class BloombergRefDataCollectorTest {
  
  private static final String WATCH_LIST_FILE = "watchListTest.txt";
  private static final String FIELD_LIST_FILE = "fieldListTest.txt";
  public static final FudgeContext s_fudgeContext = OpenGammaFudgeContext.getInstance();
  
  private BloombergRefDataCollector _refDataCollector;
  private CachingReferenceDataProvider _refDataProvider;
  private File _outputFile;

  @BeforeMethod
  public void setUp(Method m) throws Exception {    
    CachingReferenceDataProvider cachingReferenceDataProvider = BloombergLiveDataServerUtils.getCachingReferenceDataProvider(m);
    _refDataProvider = cachingReferenceDataProvider;
    
    File watchListFile = new File(BloombergRefDataCollectorTest.class.getResource(WATCH_LIST_FILE).getPath());
    File fieldListFile = new File(BloombergRefDataCollectorTest.class.getResource(FIELD_LIST_FILE).getPath());
    
    String outfileName = getClass().getSimpleName() + "-" + Thread.currentThread().getName() + "-" + Instant.now().toEpochNanos();
    
    _outputFile = File.createTempFile(outfileName, null);
    _outputFile.deleteOnExit();
    
    _refDataCollector = new BloombergRefDataCollector(s_fudgeContext, watchListFile, _refDataProvider, fieldListFile, _outputFile);
    _refDataCollector.start();
  }

  @AfterMethod
  public void tearDown() throws Exception {
    //clean up
    _refDataCollector.stop();
    BloombergLiveDataServerUtils.stopCachingReferenceDataProvider(_refDataProvider);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test() {
    LoggedReferenceDataProvider loggedRefDataProvider = new LoggedReferenceDataProvider(s_fudgeContext, _outputFile);
    
    Set<String> securities = Sets.newHashSet("QQQQ US Equity", "/buid/EQ0082335400001000");
    Set<String> fields = Collections.singleton("SECURITY_TYP");
    ReferenceDataResult result = loggedRefDataProvider.getFields(securities, fields);
    
    Set<String> testSecurities = result.getSecurities();
    assertEquals(2, testSecurities.size());
    
    assertTrue(testSecurities.containsAll(securities));
    
    for (String security : testSecurities) {
      PerSecurityReferenceDataResult perSecurity = result.getResult(security);
      FudgeMsg fieldData = perSecurity.getFieldData();
      String securityType = fieldData.getString("SECURITY_TYP");
      assertEquals("ETP", securityType);
    }
    
  }

}
