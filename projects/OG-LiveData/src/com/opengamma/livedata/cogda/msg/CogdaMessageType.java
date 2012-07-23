/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.cogda.msg;

/**
 * An enumeration of all message types to support ease of event loop processing.
 */
public enum CogdaMessageType {
  /** {@see ConnectionRequestMessage} */
  CONNECTION_REQUEST,
  /** {@see ConnectionResponseMessage} */
  CONNECTION_RESPONSE,
  /** {@see CogdaLiveDataSnapshotRequestMessage} */
  SNAPSHOT_REQUEST,
  /** {@see CogdaLiveDataSnapshotResponseMessage} */
  SNAPSHOT_RESPONSE,
  /** {@see CogdaLiveDataSubscriptionRequestMessage} */
  SUBSCRIPTION_REQUEST,
  /** {@see CogdaLiveDataSubscriptionResponseMessage} */
  SUBSCRIPTION_RESPONSE,
  /** {@see CogdaLiveDataUpdateMessage} */
  LIVE_DATA_UPDATE;

}
