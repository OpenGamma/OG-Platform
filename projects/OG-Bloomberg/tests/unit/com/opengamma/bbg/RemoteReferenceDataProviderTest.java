/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import org.testng.annotations.Test;

import com.opengamma.bbg.server.ReferenceDataProviderRequestReceiver;
import com.opengamma.bbg.test.BloombergLiveDataServerUtils;
import com.opengamma.transport.ByteArrayFudgeRequestSender;
import com.opengamma.transport.FudgeRequestDispatcher;
import com.opengamma.transport.InMemoryByteArrayRequestConduit;

/**
 * 
 */
@Test
public class RemoteReferenceDataProviderTest  extends BloombergReferenceDataProviderTestCase {
  
  private CachingReferenceDataProvider _refDataProvider;

  @Override
  protected ReferenceDataProvider createReferenceDataProvider(Class<?> c) {
    _refDataProvider = BloombergLiveDataServerUtils.getCachingReferenceDataProvider(c);
    
    //receives FudgeMsg and sends out FudgeMsg
    ReferenceDataProviderRequestReceiver requestReceiver = new ReferenceDataProviderRequestReceiver(_refDataProvider);
    //receives request in byte array, converts to FudgeMsg and pass request to underlying FudgeRequestReceiver
    //return reply as byte after converting from FudgeMsg
    FudgeRequestDispatcher fudgeRequestDispatcher = new FudgeRequestDispatcher(requestReceiver);
    //A ByteArrayRequestSender
    InMemoryByteArrayRequestConduit inMemoryByteArrayRequestConduit = new InMemoryByteArrayRequestConduit(fudgeRequestDispatcher);
    //Fudge request sender
    ByteArrayFudgeRequestSender fudgeRequestSender = new ByteArrayFudgeRequestSender(inMemoryByteArrayRequestConduit);
    ReferenceDataProvider remoteReferenceDataProvider = new RemoteReferenceDataProvider(fudgeRequestSender);
    
    return remoteReferenceDataProvider;
  }

  @Override
  protected void stopProvider() {
    BloombergLiveDataServerUtils.stopCachingReferenceDataProvider(_refDataProvider);
  }

}
