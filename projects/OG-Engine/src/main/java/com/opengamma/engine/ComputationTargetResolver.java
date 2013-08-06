/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import com.opengamma.core.change.ChangeProvider;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.target.ComputationTargetSpecificationResolver;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.resolver.ObjectResolver;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;

/**
 * A resolver from a target specification to a real target.
 * <p>
 * Within the engine targets are often referred to by the specification for performance reasons. The resolver converts the specifications, identified by a unique identifier, back to a real target.
 */
public interface ComputationTargetResolver extends ChangeProvider {

  // TODO: move to com.opengamma.engine.target.resolver

  /**
   * Partial application of {@link ComputationTargetResolver} for a given version/correction. This provides methods which do not include the version/correction parameter but are equivalent to calling
   * the original methods with a fixed value.
   */
  interface AtVersionCorrection {

    /**
     * Resolves the specification to a real target, using the bound version/correction for any deep resolution of the object. An instance of this will be bound to the function compilation and
     * execution contexts that is correctly configured for the view. Any functions can then use this for all resolutions and not have to be aware of version/correction issues.
     * 
     * @param specification the specification to resolve, not null
     * @return the resolved target, null if not found
     */
    ComputationTarget resolve(ComputationTargetSpecification specification);

    /**
     * Returns an {@link ObjectResolver} instance that will correspond to the behavior used by the {@link #resolve} method.
     * 
     * @param specification the specification to resolve, not null
     * @return the object resolver, or null if there is none ({@code resolve} would not resolve the object)
     */
    ObjectResolver<?> getResolver(ComputationTargetSpecification specification);

    /**
     * Returns a specification resolver that can be used to produce strict target specifications from looser requirements. This is the resolver bound to the parent {@link ComputationTargetResolver}
     * that is applied to the same version/correction as this instance.
     * 
     * @return the specification resolver, not null
     */
    ComputationTargetSpecificationResolver.AtVersionCorrection getSpecificationResolver();

    /**
     * Simplifies the type to the simplest form that this resolver will recognize. For example {@code CTSpec[FooSecurity, Sec~1]} might be simplified to {@code CTSpec[SECURITY, Sec~1]} if the same
     * resolution will take place regardless of whether the type is a security or a sub-class of it. If no simplification is possible, the original type may be returned.
     * <p>
     * Note that it is always correct to return the type object unchanged.
     * 
     * @param type the type to simplify, not null
     * @return the simplified type, not null
     */
    ComputationTargetType simplifyType(ComputationTargetType type);

    /**
     * Returns the version/correction instants this instance is locked to.
     * 
     * @return the version/correction instants, not null
     */
    VersionCorrection getVersionCorrection();

  }

  /**
   * Resolves the specification to a real target.
   * <p>
   * The specification contains a {@link UniqueId} that refers to a real target, such as a portfolio or security. The resolver converts this reference back to the original fully formed object. The
   * type component of the specification may be used as a hint on how to resolve the unique identifier. The type field of the resolved target may be a sub-type of the hinted type that more accurately
   * describes the target object.
   * 
   * @param specification the specification to resolve, not null
   * @param versionCorrection the version/correction time to perform any deep resolutions at, not null
   * @return the resolved target, null if not found
   */
  ComputationTarget resolve(ComputationTargetSpecification specification, VersionCorrection versionCorrection);

  /**
   * Returns an {@link ObjectResolver} instance that will correspond to the behavior used by the {@link #resolve} method.
   * 
   * @param specification the specification to resolve, not null
   * @return the object resolver, or null if there is none ({@code resolve} would not resolve the object)
   */
  ObjectResolver<?> getResolver(ComputationTargetSpecification specification);

  /**
   * Simplifies the type to the simplest form that this resolver will recognize. For example {@code CTSpec[FooSecurity, Sec~1]} might be simplified to {@code CTSpec[SECURITY, Sec~1]} if the same
   * resolution will take place regardless of whether the type is a security or a sub-class of it. If no simplification is possible, the original type may be returned.
   * <p>
   * Note that it is always correct to return the type object unchanged.
   * 
   * @param type the type to simplify, not null
   * @return the simplified type, not null
   */
  ComputationTargetType simplifyType(ComputationTargetType type);

  /**
   * Returns the {@link SecuritySource} associated with the resolver, if any. If there is no security source then the resolver will not be able to resolve {@link ComputationTargetType#SECURITY}
   * targets. When portfolios are retrieved, the objects are fully resolved using this security source.
   * 
   * @return the security source, or null if none
   */
  SecuritySource getSecuritySource();

  /**
   * Returns a specification resolver that can be used to produce strict target specifications from looser requirements. This should return a resolver configured with the same sources that are used
   * for strict target resolution.
   * 
   * @return the specification resolver, not null
   */
  ComputationTargetSpecificationResolver getSpecificationResolver();

  /**
   * Partially applies the {@link #resolve} operation for the given version/correction. Any calls made on the returned instance are equivalent to calling methods on this with the supplied
   * version/correction parameter.
   * 
   * @param versionCorrection the version/correction to use, not null
   * @return the partial application, not null
   */
  AtVersionCorrection atVersionCorrection(VersionCorrection versionCorrection);

}
