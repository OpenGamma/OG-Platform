/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg.replay;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Utility methods used by bloomberg replay components
 */
final class BloombergTickReplayUtils {
  
  private static FudgeContext s_fudgeContext = OpenGammaFudgeContext.getInstance();
  
  /**
   * Fudge message terminate field
   */
  public static final String OG_TERMINATE_FIELD = "OG-TERMINATE";

  /**
   * Restricted constructor
   */
  private BloombergTickReplayUtils() {
  }
  
  /**
   * checks if a fudge message has terminate field and true
   * 
   * @param msg the fudge message, not-null
   * @return true if terminate message or false otherwise
   */
  public static boolean isTerminateMsg(FudgeMsg msg) {
    ArgumentChecker.notNull(msg, "fudgeMsg");
    return msg.hasField(OG_TERMINATE_FIELD) && msg.getBoolean(OG_TERMINATE_FIELD);
  }
  
  /**
   * Creates a terminate fudge message
   * 
   * @return the fudge message
   */
  public static FudgeMsg getTerminateMessage() {
    MutableFudgeMsg msg = s_fudgeContext.newMessage();
    msg.add(OG_TERMINATE_FIELD, true);
    return msg;
  }

  
}
