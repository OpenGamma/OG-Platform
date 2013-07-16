/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import java.io.IOException;
import java.io.StringWriter;

import org.eclipse.jetty.util.ajax.JSON;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.wire.FudgeStreamWriter;
import org.fudgemsg.wire.json.FudgeJSONStreamWriter;

/**
 * A fall-back for complex types for which no custom converter has been registered.
 * The function result must be serializable somehow as a Fudge message,
 * and from this we can generate a JSON string.
 */
public class FudgeBasedJsonGenerator implements JSON.Generator {

  private final FudgeContext _fudgeContext;
  private final Object _value;

  public FudgeBasedJsonGenerator(FudgeContext fudgeContext, Object value) {
    _fudgeContext = fudgeContext;
    _value = value;
  }

  @Override
  public void addJSON(Appendable buffer) {
    StringWriter stringWriter = new StringWriter();
    FudgeStreamWriter fudgeWriter = new FudgeJSONStreamWriter(_fudgeContext, stringWriter);
    FudgeMsgEnvelope msg = _fudgeContext.toFudgeMsg(_value);
    fudgeWriter.writeFields(msg.getMessage());
    fudgeWriter.flush();
    fudgeWriter.close();
    try {
      buffer.append(stringWriter.toString());
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

}
