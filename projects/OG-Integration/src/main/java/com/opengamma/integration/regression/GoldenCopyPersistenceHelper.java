/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import net.sf.saxon.event.StreamWriterToReceiver;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.Serializer.Property;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.wire.FudgeMsgReader;
import org.fudgemsg.wire.FudgeMsgWriter;
import org.fudgemsg.wire.xml.FudgeXMLStreamReader;
import org.fudgemsg.wire.xml.FudgeXMLStreamWriter;

import com.google.common.base.Throwables;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * 
 */
public class GoldenCopyPersistenceHelper {

  /**
   * Relative path to golden copy dir
   */
  private static final String GOLDEN_COPY_DIR = "regression/golden_copy/";
  private static final FudgeContext FUDGE_CONTEXT = OpenGammaFudgeContext.getInstance();

  
  public GoldenCopy load(String viewName, String snapshotName) {
    String name = buildFilename(viewName, snapshotName);
    
    try (FileReader reader = new FileReader(new File(GOLDEN_COPY_DIR + name));
        FudgeMsgReader fudgeMessageReader = new FudgeMsgReader(new FudgeXMLStreamReader(FUDGE_CONTEXT, reader));) {
      FudgeMsg nextMessage = fudgeMessageReader.nextMessage();
      FudgeDeserializer deser = new FudgeDeserializer(FUDGE_CONTEXT);
      return deser.fudgeMsgToObject(GoldenCopy.class, nextMessage);
    } catch (Exception ex) {
      throw Throwables.propagate(ex);
    }
    
  }
  
  
  public void save(GoldenCopy goldenCopy) {
    FudgeSerializer serializer = new FudgeSerializer(FUDGE_CONTEXT);
    String viewName = goldenCopy.getViewName();
    String snapshotName = goldenCopy.getSnapshotName();
    String name = buildFilename(viewName, snapshotName);
    Processor p = new Processor(false);
    
    File goldenCopyDir = new File(GOLDEN_COPY_DIR);
    if (!goldenCopyDir.exists()) {
      boolean createdDirOk = goldenCopyDir.mkdirs();
      if (!createdDirOk) {
        throw new IllegalStateException("Unable to create dir: " + GOLDEN_COPY_DIR);
      }
    }
    
    try (FileWriter writer = new FileWriter(new File(GOLDEN_COPY_DIR + name));
        FudgeMsgWriter fudgeMsgWriter = createMsgWriter(p, writer);
        ) {
      MutableFudgeMsg msg = serializer.objectToFudgeMsg(goldenCopy);
      fudgeMsgWriter.writeMessage(msg);
      writer.append("\n");
      fudgeMsgWriter.flush();
    } catch (Exception ex) {
      throw Throwables.propagate(ex);
    }

  }

  private FudgeMsgWriter createMsgWriter(Processor p, FileWriter writer) {
    Serializer xmlSerializer = p.newSerializer(writer);
    xmlSerializer.setOutputProperty(Property.INDENT, "yes");
    StreamWriterToReceiver xmlStreamWriter;
    try {
      xmlStreamWriter = xmlSerializer.getXMLStreamWriter();
    } catch (SaxonApiException ex) {
      throw Throwables.propagate(ex);
    }
    FudgeXMLStreamWriter streamWriter = new FudgeXMLStreamWriter(FUDGE_CONTEXT, xmlStreamWriter);

    FudgeMsgWriter fudgeMsgWriter = new FudgeMsgWriter(streamWriter);
    return fudgeMsgWriter;
  }

  private String buildFilename(String viewName, String snapshotName) {
    String name = viewName + "." + snapshotName + ".xml";
    return name;
  }
  
  
}
