/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.target;

import java.util.Map;
import java.util.Set;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;

/**
 * Lookup a {@link ComputationTargetSpecification} from a {@link ComputationTargetReference}.
 */
public interface ComputationTargetSpecificationResolver {

  /**
   * Partial application of {@link ComputationTargetSpecificationResolver} for a given version/correction. This provides methods which do not include the version/correction parameter but are
   * equivalent to calling the original methods with a fixed value.
   */
  interface AtVersionCorrection {

    /**
     * Resolves the given target reference to a strict target specification. The reference may be a {@link ComputationTargetRequirement} instance containing one or more external identifiers, or a
     * {@link ComputationTargetSpecification} instance that contains an unversioned {@link UniqueId} (an object identifier). The resolved specification will include a versioned unique identifier.
     * <p>
     * If the reference is already a resolved specification then the same object is returned. If the reference contains an external identifier bundle the appropriate source will be used to look up the
     * object and a specification constructed from the unique identifier of the returned object, if any.
     * 
     * @param reference the reference to resolve, not null
     * @return the resolved target specification, or null if the reference could not be resolved
     */
    ComputationTargetSpecification getTargetSpecification(ComputationTargetReference reference);

    /**
     * Resolves the given target references to strict target specifications. This is a bulk version of {@link #getTargetSpecification} which should be more efficient than calling it repeatedly as it
     * may be able to batch up calls to the underlying components which may have more efficient bulk operations.
     * 
     * @param references the references to resolve, not null and not containing nulls
     * @return the resolved target references, containing an entry for each of the resolved references, not null
     */
    Map<ComputationTargetReference, ComputationTargetSpecification> getTargetSpecifications(Set<ComputationTargetReference> references);

  }

  /**
   * Resolves the given target reference to a strict target specification. The reference may be a {@link ComputationTargetRequirement} instance containing one or more external identifiers, or a
   * {@link ComputationTargetSpecification} instance that contains an unversioned {@link UniqueId} (an object identifier). The resolved specification will include a versioned unique identifier.
   * <p>
   * If the reference is already a resolved specification then the same object is returned. If the reference contains an external identifier bundle the appropriate source will be used to look up the
   * object and a specification constructed from the unique identifier of the returned object, if any.
   * 
   * @param reference the reference to resolve, not null
   * @param versionCorrection the version correction to use when looking up information in external sources to perform the resolution, not null
   * @return the resolved target specification, or null if the reference could not be resolved
   */
  ComputationTargetSpecification getTargetSpecification(ComputationTargetReference reference, VersionCorrection versionCorrection);

  /**
   * Resolves the given target references to strict target specifications. This is a bulk version of {@link #getTargetSpecification} which should be more efficient than calling it repeatedly as it may
   * be able to batch up calls to any underlying components which may have more efficient bulk operations.
   * 
   * @param references the references to resolve, not null and not containing nulls
   * @param versionCorrection the version/correction to use when looking up information in external sources to perform the resolution, not null
   * @return the resolved target references, containing an entry for each of the resolved references, not null
   */
  Map<ComputationTargetReference, ComputationTargetSpecification> getTargetSpecifications(Set<ComputationTargetReference> references, VersionCorrection versionCorrection);

  /**
   * Partially applies the {@link #getTargetSpecification} operation for the given version/correction. Any calls made on the returned instance are equivalent to calling methods on this with the
   * supplied version/correction parameter.
   * 
   * @param versionCorrection the version/correction to use, not null
   * @return the partial application, not null
   */
  AtVersionCorrection atVersionCorrection(VersionCorrection versionCorrection);

}
