/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.google.common.collect.Maps;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.msg.NormalizationRequest;
import com.opengamma.livedata.msg.NormalizationResponse;
import com.opengamma.livedata.normalization.Normalizer;
import com.opengamma.transport.FudgeRequestSender;
import com.opengamma.transport.FudgeSynchronousClient;

/**
 * A remote client to the normalization service, running on top of a {@link FudgeRequestSender}
 * implementation.
 */
public class NormalizerClient extends FudgeSynchronousClient implements Normalizer {

  public NormalizerClient(final FudgeRequestSender sender) {
    super(sender);
  }

  protected NormalizationResponse send(final NormalizationRequest request) {
    final FudgeMsg requestMessage = request.toFudgeMsg(new FudgeSerializer(getMessageSender().getFudgeContext()));
    final FudgeMsg responseMessage;
    try {
      responseMessage = sendRequestAndWaitForResponse(requestMessage, request.getCorrelationId());
    } catch (RuntimeException e) {
      return null;
    }
    if (responseMessage == null) {
      return null;
    }
    return NormalizationResponse.fromFudgeMsg(new FudgeDeserializer(getMessageSender().getFudgeContext()), responseMessage);
  }

  // Normalizer

  @Override
  public FudgeMsg normalizeValues(final LiveDataSpecification specification, final FudgeMsg data) {
    final NormalizationResponse response = send(new NormalizationRequest(getNextCorrelationId(), Collections.singleton(specification), Collections.singleton(data)));
    if (response == null) {
      return null;
    }
    return response.getValues().get(0);
  }

  @Override
  public Map<LiveDataSpecification, FudgeMsg> normalizeValues(final Map<LiveDataSpecification, FudgeMsg> data) {
    final List<LiveDataSpecification> dataSpecifications = new ArrayList<LiveDataSpecification>(data.size());
    final List<FudgeMsg> dataMessages = new ArrayList<FudgeMsg>(data.size());
    for (Map.Entry<LiveDataSpecification, FudgeMsg> dataEntry : data.entrySet()) {
      dataSpecifications.add(dataEntry.getKey());
      dataMessages.add(dataEntry.getValue());
    }
    final NormalizationResponse response = send(new NormalizationRequest(getNextCorrelationId(), dataSpecifications, dataMessages));
    if (response == null) {
      return null;
    }
    final Iterator<LiveDataSpecification> itrSpecifications = response.getLiveDataSpecification().iterator();
    final Iterator<FudgeMsg> itrMessages = response.getValues().iterator();
    final Map<LiveDataSpecification, FudgeMsg> result = Maps.newHashMapWithExpectedSize(data.size());
    while (itrSpecifications.hasNext() && itrMessages.hasNext()) {
      result.put(itrSpecifications.next(), itrMessages.next());
    }
    return result;
  }

  // FudgeSynchronousClient

  @Override
  protected Long getCorrelationIdFromReply(FudgeMsg reply) {
    return reply.getLong(NormalizationResponse.CORRELATION_ID_KEY);
  }

}
