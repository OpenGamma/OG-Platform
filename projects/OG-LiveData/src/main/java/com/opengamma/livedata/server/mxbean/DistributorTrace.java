/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server.mxbean;

/**
 * Data of a distributor trace.
 */
public class DistributorTrace {

  private final String _jmsTopic;
  private final String _expiry;
  private final boolean _hasExpired;
  private final boolean _isPersistent;
  private final long _messagesSent;

  public DistributorTrace(String jmsTopic, String expiry, boolean hasExpired, boolean persistent, long messagesSent) {
    _jmsTopic = jmsTopic;
    _expiry = expiry;
    _hasExpired = hasExpired;
    _isPersistent = persistent;
    _messagesSent = messagesSent;
  }

  public String getJmsTopic() {
    return _jmsTopic;
  }

  public String getExpiry() {
    return _expiry;
  }

  public boolean isHasExpired() {
    return _hasExpired;
  }

  public boolean isPersistent() {
    return _isPersistent;
  }

  public long getMessagesSent() {
    return _messagesSent;
  }

}
