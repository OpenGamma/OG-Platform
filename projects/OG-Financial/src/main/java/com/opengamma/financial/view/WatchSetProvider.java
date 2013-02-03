/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view;

import java.util.Set;

import com.opengamma.id.ObjectId;

/**
 * Interface to allow an alternate set of unique identifiers to be watched for changes in addition to those
 * explicitly requested by functions. For example, there may be alternative schemes and delegation rules in
 * place.
 */
public interface WatchSetProvider {

  /**
   * Returns additional unique identifiers to watch for changes.
   *
   * @param watchSet explicitly requested watch set, not null
   * @return the additional identifiers to watch, null if there are no additional ones
   */
  Set<ObjectId> getAdditionalWatchSet(Set<ObjectId> watchSet);

}
