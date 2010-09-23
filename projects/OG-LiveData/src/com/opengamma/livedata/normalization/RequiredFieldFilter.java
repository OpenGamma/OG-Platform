/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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
 * Rejects any update that doesn't contain one of a set of fields.
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
