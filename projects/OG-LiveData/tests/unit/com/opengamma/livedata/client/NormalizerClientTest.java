/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.normalization.DefaultNormalizer;
import com.opengamma.livedata.normalization.NormalizationRuleSet;
import com.opengamma.livedata.normalization.Normalizer;
import com.opengamma.livedata.normalization.StandardRuleResolver;
import com.opengamma.livedata.normalization.UnitChange;
import com.opengamma.livedata.resolver.IdentityIdResolver;
import com.opengamma.livedata.server.NormalizerServer;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.FudgeRequestSender;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Tests the {@link NormalizerClient} and {@link NormalizerServer} classes.
 */
@Test
public class NormalizerClientTest {

  private NormalizerClient createClient() {
    final UnitChange rule = new UnitChange("Foo", 0.01);
    final NormalizationRuleSet rules = new NormalizationRuleSet("Test", rule);
    final Normalizer underlying = new DefaultNormalizer(new IdentityIdResolver(), new StandardRuleResolver(Arrays.asList(rules)));
    final NormalizerServer server = new NormalizerServer(underlying);
    final NormalizerClient client = new NormalizerClient(new FudgeRequestSender() {

      @Override
      public FudgeContext getFudgeContext() {
        return OpenGammaFudgeContext.getInstance();
      }

      @Override
      public void sendRequest(FudgeMsg request, FudgeMessageReceiver responseReceiver) {
        final FudgeMsg response = server.requestReceived(new FudgeDeserializer(getFudgeContext()), new FudgeMsgEnvelope(request));
        responseReceiver.messageReceived(getFudgeContext(), new FudgeMsgEnvelope(response));
      }

    });
    return client;
  }

  public void testSingleRequest() {
    final NormalizerClient client = createClient();
    final MutableFudgeMsg msgIn = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msgIn.add("Foo", null, 42d);
    msgIn.add("Bar", null, 42d);
    final FudgeMsg msgOut = client.normalizeValues(new LiveDataSpecification("Test", ExternalId.of("Scheme", "Value")), msgIn);
    assertNotNull(msgOut);
    assertEquals(msgOut.getDouble("Foo"), 42d * 0.01);
    assertEquals(msgOut.getDouble("Bar"), 42d);
  }

  public void testBulkRequest() {
    final NormalizerClient client = createClient();
    Map<LiveDataSpecification, FudgeMsg> map = new HashMap<LiveDataSpecification, FudgeMsg>();
    final LiveDataSpecification lds1 = new LiveDataSpecification("Test", ExternalId.of("Message", "1"));
    final MutableFudgeMsg msg1 = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msg1.add("Foo", null, 42d);
    map.put(lds1, msg1);
    final LiveDataSpecification lds2 = new LiveDataSpecification("Test", ExternalId.of("Message", "2"));
    final MutableFudgeMsg msg2 = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msg2.add("Bar", null, 42d);
    map.put(lds2, msg2);
    map = client.normalizeValues(map);
    assertNotNull(map);
    assertEquals(map.get(lds1).getDouble("Foo"), 42d * 0.01);
    assertEquals(map.get(lds2).getDouble("Bar"), 42d);
  }

}
