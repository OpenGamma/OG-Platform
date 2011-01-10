/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.fudgemsg.MutableFudgeFieldContainer;

import com.google.common.collect.Sets;
import com.opengamma.livedata.server.FieldHistoryStore;

/**
 * Rejects any update that doesn't contain a set of fields.
 *
 * @author kirk
 */
public class RequiredFieldFilter implements NormalizationRule {
  private final Set<String> _requiredFieldNames = new HashSet<String>();
  
  public RequiredFieldFilter(String... requiredFieldNames) {
    this(Sets.newHashSet(requiredFieldNames));
  }
  
  public RequiredFieldFilter(Collection<String> requiredFieldNames) {
    _requiredFieldNames.addAll(requiredFieldNames);
  }

  /**
   * @return the requiredFieldNames
   */
  public Set<String> getRequiredFieldNames() {
    return _requiredFieldNames;
  }

  /**
   * Rejects any update that doesn't contain a set of fields.
   * 
   * @param msg message to normalize
   * @param fieldHistory field history
   * @return {@code null} if {@code msg} doesn't contain
   * all required fields, {@code msg} unmodified otherwise
   */
  @Override
  public MutableFudgeFieldContainer apply(MutableFudgeFieldContainer msg,
      FieldHistoryStore fieldHistory) {
    Set<String> namesFromMsg = msg.getAllFieldNames();
    if (namesFromMsg.containsAll(getRequiredFieldNames())) {
      return msg;
    }
    return null;
  }

}
