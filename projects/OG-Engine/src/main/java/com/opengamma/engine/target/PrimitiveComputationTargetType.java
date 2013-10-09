/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.target;

import java.util.Collection;
import java.util.Map;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.target.resolver.DeepResolver;
import com.opengamma.engine.target.resolver.IdentifierResolver;
import com.opengamma.engine.target.resolver.ObjectResolver;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;

/**
 * Specialized form of {@link ObjectComputationTargetType} for primitive objects that can be converted directly to/from unique identifiers without an external resolver service. Instances also serve as
 * an {@link ObjectResolver} and possibly {@link IdentifierResolver} so that they can be added to a {@link ComputationTargetResolver} and/or {@link ComputationTargetSpecificationResolver} to handle
 * the type.
 * 
 * @param <T> the target object type
 */
public class PrimitiveComputationTargetType<T extends UniqueIdentifiable> extends ObjectComputationTargetType<T> implements ObjectResolver<T> {

  private static final long serialVersionUID = 1L;

  private final ObjectResolver<T> _resolver;

  private static final class IdentifierResolving<T extends UniqueIdentifiable> extends PrimitiveComputationTargetType<T> implements IdentifierResolver {

    private static final long serialVersionUID = 1L;

    private final IdentifierResolver _resolver;

    private IdentifierResolving(final ComputationTargetType type, final Class<T> clazz, final ObjectResolver<T> objectResolver, final IdentifierResolver identifierResolver) {
      super(type, clazz, objectResolver);
      _resolver = identifierResolver;
    }

    // IdentifierResolver

    @Override
    public UniqueId resolveExternalId(final ExternalIdBundle identifiers, final VersionCorrection versionCorrection) {
      return _resolver.resolveExternalId(identifiers, versionCorrection);
    }

    @Override
    public Map<ExternalIdBundle, UniqueId> resolveExternalIds(final Collection<ExternalIdBundle> identifiers, final VersionCorrection versionCorrection) {
      return _resolver.resolveExternalIds(identifiers, versionCorrection);
    }

    @Override
    public UniqueId resolveObjectId(final ObjectId identifier, final VersionCorrection versionCorrection) {
      return _resolver.resolveObjectId(identifier, versionCorrection);
    }

    @Override
    public Map<ObjectId, UniqueId> resolveObjectIds(final Collection<ObjectId> identifiers, final VersionCorrection versionCorrection) {
      return _resolver.resolveObjectIds(identifiers, versionCorrection);
    }

  }

  protected PrimitiveComputationTargetType(final ComputationTargetType type, final Class<T> clazz, final ObjectResolver<T> resolver) {
    super(type, clazz);
    _resolver = resolver;
  }

  public static <T extends UniqueIdentifiable> PrimitiveComputationTargetType<T> of(final ComputationTargetType type, final Class<T> clazz, final ObjectResolver<T> resolver) {
    assert type.isTargetType(clazz);
    assert resolver != null;
    if (resolver instanceof IdentifierResolver) {
      return new IdentifierResolving<T>(type, clazz, resolver, (IdentifierResolver) resolver);
    } else {
      return new PrimitiveComputationTargetType<T>(type, clazz, resolver);
    }
  }

  public T resolve(final UniqueId identifier) {
    return _resolver.resolveObject(identifier, VersionCorrection.LATEST);
  }

  // ObjectResolver

  @Override
  public T resolveObject(final UniqueId identifier, final VersionCorrection versionCorrection) {
    return _resolver.resolveObject(identifier, versionCorrection);
  }

  @Override
  public DeepResolver deepResolver() {
    return _resolver.deepResolver();
  }

  @Override
  public ChangeManager changeManager() {
    return _resolver.changeManager();
  }

}
