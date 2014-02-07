/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jaxrs;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.wire.FudgeDataOutputStreamWriter;
import org.fudgemsg.wire.FudgeMsgWriter;
import org.joda.beans.Bean;

/**
 * A JAX-RS provider to convert RESTful responses to Fudge binary encoded messages.
 * <p>
 * This converts directly to Fudge from the RESTful resource without the need to manually create the message in application code.
 */
@Provider
@Produces(FudgeRest.MEDIA)
public class FudgeObjectBinaryProducer extends FudgeBase implements MessageBodyWriter<Object> {

  /**
   * Creates the producer.
   */
  public FudgeObjectBinaryProducer() {
    super();
  }

  /**
   * Creates the producer.
   * 
   * @param context the Fudge context to use, not null
   */
  public FudgeObjectBinaryProducer(final FudgeContext context) {
    super(context);
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return FudgeRest.MEDIA_TYPE.equals(mediaType) || type == FudgeResponse.class || Bean.class.isAssignableFrom(type) || FudgeMsgEnvelope.class.isAssignableFrom(type) ||
        FudgeMsg.class.isAssignableFrom(type);
  }

  @Override
  public long getSize(Object obj, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(Object obj, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {

    FudgeMsgEnvelope msg;
    if (obj instanceof FudgeResponse) {
      FudgeResponse wrapper = (FudgeResponse) obj;
      msg = getFudgeContext().toFudgeMsg(wrapper.getValue());
    } else if (obj instanceof FudgeMsgEnvelope) {
      msg = (FudgeMsgEnvelope) obj;
    } else if (obj instanceof FudgeMsg) {
      msg = new FudgeMsgEnvelope((FudgeMsg) obj);
    } else {
      msg = getFudgeContext().toFudgeMsg(obj);
    }

    @SuppressWarnings("resource")
    final FudgeMsgWriter writer = new FudgeMsgWriter(new FudgeDataOutputStreamWriter(getFudgeContext(), entityStream));
    writer.writeMessageEnvelope(msg, getFudgeTaxonomyId());
    writer.flush();
  }

}
