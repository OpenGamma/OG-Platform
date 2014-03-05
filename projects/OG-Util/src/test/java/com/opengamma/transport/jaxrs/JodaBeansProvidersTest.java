/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jaxrs;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import org.joda.beans.Bean;
import org.joda.beans.impl.flexi.FlexiBean;
import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.test.TestGroup;

/**
 * Test the JodaBeansProducerConsumer pairs.
 */
@Test(groups = TestGroup.UNIT)
public class JodaBeansProvidersTest {

  @SuppressWarnings({"rawtypes", "unchecked" })
  private void testBeans(final MessageBodyWriter producer, final MessageBodyReader consumer) {
    FlexiBean sub = new FlexiBean();
    sub.put("child", "name");
    FlexiBean bean = new FlexiBean();
    bean.put("foo", "bar");
    bean.put("number", 42);
    bean.put("sub", sub);
    
    // from bean
    assertTrue(producer.isWriteable(bean.getClass(), bean.getClass(), null, null));
    assertEquals(producer.getSize(bean, bean.getClass(), bean.getClass(), null, null), -1);
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      producer.writeTo(bean, bean.getClass(), bean.getClass(), null, null, null, baos);
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException(ex.getMessage(), ex);
    }
    final byte[] data = baos.toByteArray();
    
    // to bean
    assertTrue(consumer.isReadable(bean.getClass(), bean.getClass(), null, null));
    final ByteArrayInputStream bais = new ByteArrayInputStream(data);
    final Bean input;
    try {
      input = (Bean) consumer.readFrom(Bean.class, Bean.class, null, null, null, bais);
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException(ex.getMessage(), ex);
    }
    assertNotNull(input);
    assertEquals(bean, input);
  }

  public void testBinary() {
    JodaBeanBinaryProducerConsumer pc = new JodaBeanBinaryProducerConsumer();
    testBeans(pc, pc);
  }

  public void testXML() {
    JodaBeanXmlProducerConsumer pc = new JodaBeanXmlProducerConsumer();
    testBeans(pc, pc);
  }

}
