/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import java.io.CharArrayWriter;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.wire.FudgeMsgWriter;
import org.fudgemsg.wire.json.FudgeJSONStreamWriter;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Converter from FudgeMessage to JSON
 */
public class FudgeMessageJSONBuilder {

  private final FudgeContext _fudgeContext;

  /**
   * Creates the builder
   * 
   * @param fudgeContext the fudge context
   */
  public FudgeMessageJSONBuilder(FudgeContext fudgeContext) {
    ArgumentChecker.notNull(fudgeContext, "FudgeContext");
    _fudgeContext = fudgeContext;
  }

  /**
   * Creates an instance.
   */
  public FudgeMessageJSONBuilder() {
    this(OpenGammaFudgeContext.getInstance());
  }

  /**
   * Converts the given fudgeMsg to a JSON string representation
   * 
   * @param fudgeMsg the fudge message
   * @return the json representation
   */
  public String build(FudgeMsg fudgeMsg) {
    ArgumentChecker.notNull(fudgeMsg, "fudge message");
    
    final CharArrayWriter caw = new CharArrayWriter();
    try (FudgeMsgWriter fmw = new FudgeMsgWriter(new FudgeJSONStreamWriter(_fudgeContext, caw))) {
      fmw.writeMessage(fudgeMsg);
    }
    return caw.toString();
  }

}
