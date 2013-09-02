package com.opengamma.financial.view.rest;

import static org.mockito.Mockito.mock;

import org.fudgemsg.FudgeContext;
import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.util.test.TestGroup;


@Test(groups = TestGroup.UNIT)
public class ViewClientJmsResultPublisherTest {
  
  private ViewClientJmsResultPublisher _viewClientResultPublisher;

 
  
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testStartExceptionWhenNoJms()  throws Exception {
    _viewClientResultPublisher = new ViewClientJmsResultPublisher(mock(ViewClient.class), mock(FudgeContext.class), null);
    _viewClientResultPublisher.startPublishingResults("test");
    
  }
  
  @Test
  public void testStopNoJms() throws Exception {
    _viewClientResultPublisher = new ViewClientJmsResultPublisher(mock(ViewClient.class), mock(FudgeContext.class), null);
    _viewClientResultPublisher.stopPublishingResults();
  }
  
  
  

}
