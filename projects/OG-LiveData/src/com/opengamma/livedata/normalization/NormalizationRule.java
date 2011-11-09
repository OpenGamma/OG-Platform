/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import org.fudgemsg.MutableFudgeMsg;

import com.opengamma.livedata.server.FieldHistoryStore;

/**
 * A normalization rule.
 * <p>
 * Depending on where in the chain of normalization rules this rule is inserted, the incoming message could be
 * completely unnormalized, partially normalized, or fully normalized.
 */
public interface NormalizationRule {
  
  /**
   * Applies the normalization rule. This method may modify and return the input argument {@code msg}. A null result
   * is valid and means that the message should not be sent to the client at all.
   * 
   * @param msg  the message to normalize, not null
   * @param securityUniqueId  the data provider's unique ID of the security, not null
   * @param fieldHistory  the distributor-specific field history which the rule may choose to update, not null 
   * @return the normalized message, or null to prevent the message from being sent to the client
   */
  MutableFudgeMsg apply(MutableFudgeMsg msg, String securityUniqueId, FieldHistoryStore fieldHistory);

}
