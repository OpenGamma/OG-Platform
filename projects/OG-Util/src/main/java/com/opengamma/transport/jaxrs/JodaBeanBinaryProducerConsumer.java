/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jaxrs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.joda.beans.Bean;
import org.joda.beans.MetaProperty;
import org.joda.beans.impl.flexi.FlexiBean;
import org.joda.beans.ser.JodaBeanMimeType;
import org.joda.beans.ser.JodaBeanSer;

import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * A JAX-RS provider to convert RESTful Joda-Bean instances to and from binary encoded messages.
 */
@Provider
@Produces(JodaBeanMimeType.BINARY)
@Consumes(JodaBeanMimeType.BINARY)
public class JodaBeanBinaryProducerConsumer implements MessageBodyReader<Object>, MessageBodyWriter<Object> {

  /**
   * Creates an instance.
   */
  public JodaBeanBinaryProducerConsumer() {
    super();
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return Bean.class.isAssignableFrom(type) || type == FudgeResponse.class ||
        FudgeMsgEnvelope.class.isAssignableFrom(type) || FudgeMsg.class.isAssignableFrom(type);
  }

  @Override
  public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
    boolean isBean = Bean.class.isAssignableFrom(type);
    Class<? extends Bean> cls = (isBean ? type.asSubclass(Bean.class) : FlexiBean.class);
    Bean bean = JodaBeanSer.COMPACT.binReader().read(entityStream, cls);
    if (isBean) {
      return bean;
    }
    FlexiBean fbean = (FlexiBean) bean;
    if (((Object) type) == FudgeResponse.class) {
      return FudgeResponse.of(fbean.get("value"));
    }
    if (((Object) type) == FudgeMsg.class) {
      return createMessage(bean);
    }
    if (((Object) type) == FudgeMsgEnvelope.class) {
      return new FudgeMsgEnvelope(createMessage(bean));
    }
    return bean;
  }

  private MutableFudgeMsg createMessage(Bean bean) {
    MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    for (MetaProperty<?> mp : bean.metaBean().metaPropertyIterable()) {
      Object obj = mp.get(bean);
      if (obj instanceof Bean) {
        msg.add(mp.name(), createMessage((Bean) obj));
      } else  {
        msg.add(mp.name(), obj);
      }
    }
    return msg;
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return Bean.class.isAssignableFrom(type) || type == FudgeResponse.class ||
        FudgeMsgEnvelope.class.isAssignableFrom(type) || FudgeMsg.class.isAssignableFrom(type);
  }

  @Override
  public long getSize(Object bean, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(Object obj, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
    Bean bean = null;
    if (Bean.class.isAssignableFrom(type)) {
      bean = (Bean) obj;
    } else {
      if (((Object) type) == FudgeResponse.class) {
        FudgeResponse rsp = (FudgeResponse) obj;
        FlexiBean fb = new FlexiBean();
        fb.set("value", rsp.getValue());
        bean = fb;
      } else if (((Object) type) == FudgeMsg.class) {
        FudgeMsg msg = (FudgeMsg) obj;
        bean = createBean(msg);
      } else if (((Object) type) == FudgeMsgEnvelope.class) {
        FudgeMsgEnvelope env = (FudgeMsgEnvelope) obj;
        bean = createBean(env.getMessage());
      }
    }
    JodaBeanSer.COMPACT.binWriter().write(bean, entityStream);
  }

  private Bean createBean(FudgeMsg msg) {
    FlexiBean fb = new FlexiBean();
    for (FudgeField field : msg.getAllFields()) {
      Object obj = field.getValue();
      if (obj instanceof FudgeMsg) {
        fb.set(field.getName(), createBean((FudgeMsg) obj));
      } else  {
        fb.set(field.getName(), obj);
      }
    }
    return fb;
  }

}
