/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import org.fudgemsg.MutableFudgeFieldContainer;

import com.opengamma.livedata.server.FieldHistoryStore;

/**
 *  
 *
 * @author pietari
 */
public interface NormalizationRule {
  
  /**
   * Applies the normalization rule. 
   * 
   * @param msg Message to normalize. Will already be partially normalized if
   * this rule is not the first one in the chain. Never null.
   * @param fieldHistory What is contained in this store
   * is completely up to the normalization rule(s) applied
   * by {@code MarketDataDistributor}. For example, one of the
   * rules may be {@link FieldHistoryUpdater}, which would populate
   * the store with the current state of the message normalization pipeline. 
   * There is a separate history store for each {@link MarketDataDistributor},
   * so you can safely assume histories for different distributors
   * will not interact. The store is never null. 
   * @return The normalized message. The method may modify and 
   * return the input parameter <code>msg</code> if desired. 
   */
  public MutableFudgeFieldContainer apply(
      MutableFudgeFieldContainer msg,
      FieldHistoryStore fieldHistory);

}
