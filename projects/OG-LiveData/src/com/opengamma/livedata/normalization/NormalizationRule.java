/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import org.fudgemsg.MutableFudgeFieldContainer;

import com.opengamma.livedata.server.FieldHistoryStore;

/**
 * A normalization rule.  
 */
public interface NormalizationRule {
  
  /**
   * Applies the normalization rule. 
   * 
   * @param msg message to normalize. Will already be partially normalized if
   * this rule is not the first one in the chain. Not null.
   * @param fieldHistory what is contained in this history
   * is completely up to the normalization rule(s) applied
   * by {@code MarketDataDistributor}. For example, one of the
   * rules may be {@link FieldHistoryUpdater}, which would populate
   * the store with the current state of the message normalization pipeline. 
   * There is a separate history store for each {@link MarketDataDistributor},
   * so you can safely assume histories for different distributors
   * will not interact. Not null. 
   * @return the normalized message. The method may modify and 
   * return the input parameter <code>msg</code> if desired. 
   * Null is a valid return value and means that the message should
   * not be sent to the client at all.
   */
  MutableFudgeFieldContainer apply(
      MutableFudgeFieldContainer msg,
      FieldHistoryStore fieldHistory);

}
