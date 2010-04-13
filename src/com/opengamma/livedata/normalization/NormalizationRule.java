/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import java.io.Serializable;

import org.fudgemsg.MutableFudgeFieldContainer;

import com.opengamma.livedata.server.FieldHistoryStore;

/**
 *  
 *
 * @author pietari
 */
public interface NormalizationRule extends Serializable {
  
  /**
   * Applies the normalization rule. 
   * 
   * @param msg Message to normalize. Never null.
   * @return The normalized message. The method may modify and 
   * return the input parameter <code>msg</code> if desired. 
   */
  public MutableFudgeFieldContainer apply(
      MutableFudgeFieldContainer msg,
      FieldHistoryStore fieldHistory);

}
