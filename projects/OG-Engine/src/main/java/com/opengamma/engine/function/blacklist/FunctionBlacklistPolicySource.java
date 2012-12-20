/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import com.opengamma.id.UniqueId;

/**
 * Provides named function blacklist policies.
 */
public interface FunctionBlacklistPolicySource {

  /**
   * Locates and returns the policy uniquely identified.
   * 
   * @param uniqueId the unique identifier of the policy, not null
   * @return the policy if one exists, null if none was found
   */
  FunctionBlacklistPolicy getPolicy(UniqueId uniqueId);

  /**
   * Locates and returns a suitable policy with the given symbolic name.
   * 
   * @param name the symbolic name of the policy, not null
   * @return the policy if one exists, null if none was found
   */
  FunctionBlacklistPolicy getPolicy(String name);

}
