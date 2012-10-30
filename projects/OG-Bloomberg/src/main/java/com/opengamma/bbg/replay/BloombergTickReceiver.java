/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.replay;


/**
 * 
 *
 * @author yomi
 */
public interface BloombergTickReceiver {
  public void tickReceived(BloombergTick msg);
}
