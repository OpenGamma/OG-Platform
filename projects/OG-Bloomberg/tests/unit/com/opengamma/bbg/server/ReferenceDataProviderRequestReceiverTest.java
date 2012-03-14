/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.server;


import static org.testng.AssertJUnit.assertNotNull;

import java.lang.reflect.Method;
import java.util.Collections;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.bbg.CachingReferenceDataProvider;
import com.opengamma.bbg.ReferenceDataProvider;
import com.opengamma.bbg.ReferenceDataResult;
import com.opengamma.bbg.RemoteReferenceDataProvider;
import com.opengamma.bbg.test.BloombergLiveDataServerUtils;
import com.opengamma.transport.ByteArrayFudgeRequestSender;
import com.opengamma.transport.FudgeRequestDispatcher;
import com.opengamma.transport.InMemoryByteArrayRequestConduit;

/**
 * 
 */
public class ReferenceDataProviderRequestReceiverTest {

  private CachingReferenceDataProvider _refDataProvider = null;

  @BeforeMethod
  public void setUp(Method m) throws Exception {
    _refDataProvider = BloombergLiveDataServerUtils.getCachingReferenceDataProvider(m);
  }

  @AfterMethod
  public void tearDown() throws Exception {
    BloombergLiveDataServerUtils.stopCachingReferenceDataProvider(_refDataProvider);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test() throws Exception {
    //receives FudgeMsg and sends out FudgeMsg
    ReferenceDataProviderRequestReceiver requestReceiver = new ReferenceDataProviderRequestReceiver(getReferenceDataProvider());
    
    //receives request in byte array, converts to FudgeMsg and pass request to underlying FudgeRequestReceiver
    //return reply as byte after converting from FudgeMsg
    FudgeRequestDispatcher fudgeRequestDispatcher = new FudgeRequestDispatcher(requestReceiver);
    
    //A ByteArrayRequestSender
    InMemoryByteArrayRequestConduit inMemoryByteArrayRequestConduit = new InMemoryByteArrayRequestConduit(fudgeRequestDispatcher);
    
    //Fudge request sender
    ByteArrayFudgeRequestSender fudgeRequestSender = new ByteArrayFudgeRequestSender(inMemoryByteArrayRequestConduit);
    
    RemoteReferenceDataProvider remoteReferenceDataProvider = new RemoteReferenceDataProvider(fudgeRequestSender);
    
    ReferenceDataResult dataResult = remoteReferenceDataProvider.getFields(Collections.singleton("AAPL US Equity"), Collections.singleton("SECURITY_TYP"));
    assertNotNull(dataResult);
  }

  /**
   * @return
   */
  private ReferenceDataProvider getReferenceDataProvider() {
    return _refDataProvider;   
  }

}
