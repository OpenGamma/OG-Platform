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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  private static final Logger s_logger = LoggerFactory.getLogger(NormalizerClient.class);

  public NormalizerClient(final FudgeRequestSender sender) {
    super(sender);
  }

  protected NormalizationResponse send(final NormalizationRequest request) {
    final FudgeMsg requestMessage = request.toFudgeMsg(new FudgeSerializer(getMessageSender().getFudgeContext()));
    final FudgeMsg responseMessage;
    try {
      responseMessage = sendRequestAndWaitForResponse(requestMessage, request.getCorrelationId());
    } catch (RuntimeException e) {
      s_logger.warn("Couldn't send normalization request", e);
      s_logger.debug("Exception", e);
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
  public Map<LiveDataSpecification, FudgeMsg> normalizeValues(final Map<LiveDataSpecification, ? extends FudgeMsg> data) {
    final List<LiveDataSpecification> dataSpecifications = new ArrayList<LiveDataSpecification>(data.size());
    final List<FudgeMsg> dataMessages = new ArrayList<FudgeMsg>(data.size());
    for (Map.Entry<LiveDataSpecification, ? extends FudgeMsg> dataEntry : data.entrySet()) {
      dataSpecifications.add(dataEntry.getKey());
      dataMessages.add(dataEntry.getValue());
    }
    final NormalizationRequest request = new NormalizationRequest(getNextCorrelationId(), dataSpecifications, dataMessages);
    s_logger.debug("Sending {} to server", request);
    final NormalizationResponse response = send(request);
    s_logger.debug("Received {} from server", response);
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
