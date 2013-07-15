/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.digest;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionCompilationContext;

/**
 * Source of target digest information.
 * <p>
 * Target digests can be used during graph building to recognize typical patterns that can simplify the construction process. For example, if the sub-graph for pricing any swap is dependent only that
 * swap's currency then a digest of <code>SWAP_<em>currency</em></code> might work well. As soon as the sub-graph for one target has been built, any similar targets can reuse that selection quickly.
 * In some circumstances, this may not be necessary for good system performance (and the extra step might incur a penalty). In other circumstances it might be, particularly if selecting a suitable
 * value specification to satisfy a requirement involves substantial back-tracking in function selection.
 * <p>
 * The implementation needs to be tightly coupled to the behavior of the function repository; for example it might be sufficient to use the asset class of the associated security. If functions are
 * basing their requirements on more specific properties on the targets (for example the currency) then that should also be included in the digest for the best effect. The extreme case is to use the
 * target itself as its own digest but this does not then give any performance improvement.
 * <p>
 * The function compilation context is available when calculating the digest as some functions might make use of information embedded within the view calculation configuration to alter their
 * behaviors.
 */
public interface TargetDigests {

  /**
   * Returns a digest of the target, or null if none is meaningful or useful.
   * <p>
   * Any digests returned must implement appropriate {@link Object#equals} and {@link Object#hashCode} methods.
   * 
   * @param context the function compilation context, not null
   * @param target the target specification, not null
   * @return the digest, or null if none is meaningful or useful
   */
  Object getDigest(FunctionCompilationContext context, ComputationTargetSpecification target);

}
