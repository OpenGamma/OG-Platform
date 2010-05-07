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
   * @param fieldHistory Contains completely unnormalized ticks
   * from the underlying market data API. 
   * @return The normalized message. The method may modify and 
   * return the input parameter <code>msg</code> if desired. 
   */
  public MutableFudgeFieldContainer apply(
      MutableFudgeFieldContainer msg,
      FieldHistoryStore fieldHistory);

}
