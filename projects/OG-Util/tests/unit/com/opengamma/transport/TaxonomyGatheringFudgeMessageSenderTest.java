/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test TaxonomyGatheringFudgeMessageSender.
 */
@Test
public class TaxonomyGatheringFudgeMessageSenderTest {
  private File _tmpFile;
  
  @BeforeMethod
  public void setupTmpFile() throws IOException {
    _tmpFile = File.createTempFile("TaxonomyGatheringFudgeMessageSenderTest", ".properties");
    _tmpFile.deleteOnExit();
  }

  public void noTaxonomyFileAvailableYet() throws IOException {
    _tmpFile.delete();
    
    FudgeContext context = new FudgeContext();
    CollectingFudgeMessageReceiver collectingReceiver = new CollectingFudgeMessageReceiver();
    ByteArrayFudgeMessageReceiver fudgeReceiver = new ByteArrayFudgeMessageReceiver(collectingReceiver);
    DirectInvocationByteArrayMessageSender byteArraySender = new DirectInvocationByteArrayMessageSender(fudgeReceiver);
    ByteArrayFudgeMessageSender fudgeSender = new ByteArrayFudgeMessageSender(byteArraySender, context);
    TaxonomyGatheringFudgeMessageSender gatheringSender = new TaxonomyGatheringFudgeMessageSender(fudgeSender, _tmpFile.getAbsolutePath());

    assertTrue(gatheringSender.getCurrentTaxonomy().isEmpty());
  }

  @Test(timeOut = 10000)
  public void taxonomyGathering() throws IOException, InterruptedException {
    _tmpFile.delete();
    FudgeContext context = new FudgeContext();
    CollectingFudgeMessageReceiver collectingReceiver = new CollectingFudgeMessageReceiver();
    ByteArrayFudgeMessageReceiver fudgeReceiver = new ByteArrayFudgeMessageReceiver(collectingReceiver);
    DirectInvocationByteArrayMessageSender byteArraySender = new DirectInvocationByteArrayMessageSender(fudgeReceiver);
    ByteArrayFudgeMessageSender fudgeSender = new ByteArrayFudgeMessageSender(byteArraySender, context);
    TaxonomyGatheringFudgeMessageSender gatheringSender = new TaxonomyGatheringFudgeMessageSender(fudgeSender, _tmpFile.getAbsolutePath(), context, 1000L);
    
    MutableFudgeFieldContainer msg1 = context.newMessage();
    msg1.add("name1", 1);
    msg1.add("name2", 1);
    msg1.add("name3", 1);
    msg1.add("name1", 1);
    MutableFudgeFieldContainer msg2 = context.newMessage();
    msg1.add("name4", msg2);
    msg2.add(14, 1);
    msg2.add("name5", "foo");
    
    gatheringSender.send(msg1);

    assertTrue(gatheringSender.getCurrentTaxonomy().containsKey("name1"));
    assertTrue(gatheringSender.getCurrentTaxonomy().containsKey("name2"));
    assertTrue(gatheringSender.getCurrentTaxonomy().containsKey("name3"));
    assertTrue(gatheringSender.getCurrentTaxonomy().containsKey("name4"));
    assertTrue(gatheringSender.getCurrentTaxonomy().containsKey("name5"));
    assertEquals(5, gatheringSender.getCurrentTaxonomy().size());
    
    Properties props = new Properties();
    gatheringSender.waitForNextWrite();
    InputStream is = new FileInputStream(_tmpFile);
    props.load(new BufferedInputStream(is));
    is.close();
    
    for(Map.Entry<Object, Object> propEntry : props.entrySet()) {
      Integer ordinal = gatheringSender.getCurrentTaxonomy().get(propEntry.getValue());
      assertEquals(ordinal.intValue(), Integer.parseInt((String) propEntry.getKey()));
    }
    assertEquals(5, props.size());
  }

  @Test(timeOut = 20000)
  public void validFileLoadingOnStartup() throws IOException, InterruptedException {
    _tmpFile.delete();
    FudgeContext context = new FudgeContext();
    CollectingFudgeMessageReceiver collectingReceiver = new CollectingFudgeMessageReceiver();
    ByteArrayFudgeMessageReceiver fudgeReceiver = new ByteArrayFudgeMessageReceiver(collectingReceiver);
    DirectInvocationByteArrayMessageSender byteArraySender = new DirectInvocationByteArrayMessageSender(fudgeReceiver);
    ByteArrayFudgeMessageSender fudgeSender = new ByteArrayFudgeMessageSender(byteArraySender, context);
    TaxonomyGatheringFudgeMessageSender gatheringSender = new TaxonomyGatheringFudgeMessageSender(fudgeSender, _tmpFile.getAbsolutePath(), context, 1000L);
    
    MutableFudgeFieldContainer msg1 = context.newMessage();
    msg1.add("name1", 1);
    msg1.add("name2", 1);
    msg1.add("name3", 1);
    msg1.add("name1", 1);
    MutableFudgeFieldContainer msg2 = context.newMessage();
    msg1.add("name4", msg2);
    msg2.add(14, 1);
    msg2.add("name5", "foo");
    
    gatheringSender.send(msg1);
    gatheringSender.waitForNextWrite();
    gatheringSender.getTimer().cancel();

    // Now reload the file.
    gatheringSender = new TaxonomyGatheringFudgeMessageSender(fudgeSender, _tmpFile.getAbsolutePath(), context, 5000L);
    assertTrue(gatheringSender.getCurrentTaxonomy().containsKey("name1"));
    assertTrue(gatheringSender.getCurrentTaxonomy().containsKey("name2"));
    assertTrue(gatheringSender.getCurrentTaxonomy().containsKey("name3"));
    assertTrue(gatheringSender.getCurrentTaxonomy().containsKey("name4"));
    assertTrue(gatheringSender.getCurrentTaxonomy().containsKey("name5"));
    assertEquals(5, gatheringSender.getCurrentTaxonomy().size());
  }
}
