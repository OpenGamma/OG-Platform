/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.google.common.collect.Maps;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.client.NormalizerClient;
import com.opengamma.livedata.msg.NormalizationRequest;
import com.opengamma.livedata.msg.NormalizationResponse;
import com.opengamma.livedata.normalization.Normalizer;
import com.opengamma.transport.FudgeRequestReceiver;
import com.opengamma.util.ArgumentChecker;

/**
 * Handles requests made by a {@link NormalizerClient}.
 */
public class NormalizerServer implements FudgeRequestReceiver {

  private final Normalizer _underlying;

  public NormalizerServer(final Normalizer underlying) {
    ArgumentChecker.notNull(underlying, "underlying");
    _underlying = underlying;
  }

  protected Normalizer getUnderlying() {
    return _underlying;
  }

  @Override
  public FudgeMsg requestReceived(final FudgeDeserializer deserializer, final FudgeMsgEnvelope requestEnvelope) {
    final NormalizationRequest request = NormalizationRequest.fromFudgeMsg(deserializer, requestEnvelope.getMessage());
    List<LiveDataSpecification> specifications = request.getLiveDataSpecification();
    List<FudgeMsg> messages = request.getValues();
    if (specifications.size() != messages.size()) {
      throw new IllegalArgumentException();
    }
    if (specifications.size() == 1) {
      final FudgeMsg msg = getUnderlying().normalizeValues(specifications.get(0), messages.get(0));
      if (msg == null) {
        return null;
      }
      messages = Collections.singletonList(msg);
    } else {
      Map<LiveDataSpecification, FudgeMsg> map = Maps.newHashMapWithExpectedSize(specifications.size());
      final Iterator<LiveDataSpecification> itrSpecifications = specifications.iterator();
      final Iterator<FudgeMsg> itrMessages = messages.iterator();
      while (itrSpecifications.hasNext() && itrMessages.hasNext()) {
        map.put(itrSpecifications.next(), itrMessages.next());
      }
      map = getUnderlying().normalizeValues(map);
      if (map == null) {
        return null;
      }
      specifications = new ArrayList<LiveDataSpecification>(map.size());
      messages = new ArrayList<FudgeMsg>(map.size());
      for (Map.Entry<LiveDataSpecification, FudgeMsg> mapEntry : map.entrySet()) {
        specifications.add(mapEntry.getKey());
        messages.add(mapEntry.getValue());
      }
    }
    final NormalizationResponse response = new NormalizationResponse(request.getCorrelationId(), specifications, messages);
    return response.toFudgeMsg(new FudgeSerializer(deserializer.getFudgeContext()));
  }

}
