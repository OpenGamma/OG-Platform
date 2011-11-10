/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.fudgemsg.MutableFudgeMsg;

import com.google.common.collect.Sets;
import com.opengamma.livedata.server.FieldHistoryStore;

/**
 * Rejects any update that doesn't contain a set of fields.
 */
public class RequiredFieldFilter implements NormalizationRule {

  /**
   * The field names that must be present.
   */
  private final Set<String> _requiredFieldNames = new HashSet<String>();

  /**
   * Creates a filter with a set of required field names.
   * 
   * @param requiredFieldNames  the field names, not null
   */
  public RequiredFieldFilter(String... requiredFieldNames) {
    this(Sets.newHashSet(requiredFieldNames));
  }

  /**
   * Creates a filter with a set of required field names.
   * 
   * @param requiredFieldNames  the field names, not null
   */
  public RequiredFieldFilter(Collection<String> requiredFieldNames) {
    _requiredFieldNames.addAll(requiredFieldNames);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the required field names.
   * 
   * @return the required field names, not null
   */
  public Set<String> getRequiredFieldNames() {
    return _requiredFieldNames;
  }

  //-------------------------------------------------------------------------
  /**
   * Rejects any update that doesn't contain a set of fields.
   * 
   * @param msg  the message to normalize, not null
   * @param securityUniqueId  the data provider's unique ID of the security, not null
   * @param fieldHistory  the distributor-specific field history which the rule may choose to update, not null 
   * @return the unaltered message, null if it does not contain all the required fields
   */
  @Override
  public MutableFudgeMsg apply(MutableFudgeMsg msg, String securityUniqueId, FieldHistoryStore fieldHistory) {
    Set<String> namesFromMsg = msg.getAllFieldNames();
    if (namesFromMsg.containsAll(getRequiredFieldNames())) {
      return msg;
    }
    return null;
  }

}
