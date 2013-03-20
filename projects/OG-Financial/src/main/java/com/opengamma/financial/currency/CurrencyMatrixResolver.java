/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import java.util.Set;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.resolver.AbstractIdentifierResolver;
import com.opengamma.engine.target.resolver.Resolver;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * An engine resolver for {@link CurrencyMatrix} objects
 */
public class CurrencyMatrixResolver extends AbstractIdentifierResolver implements Resolver<CurrencyMatrix> {

  /**
   * The type used to indicate the currency matrix.
   */
  public static final ComputationTargetType TYPE = ComputationTargetType.of(CurrencyMatrix.class);

  /**
   * The identifier scheme for external identifiers that reference a matrix by name.
   */
  public static final ExternalScheme IDENTIFIER_SCHEME = ExternalScheme.of(CurrencyMatrix.class.getSimpleName());

  private final CurrencyMatrixSource _underlying;

  public CurrencyMatrixResolver(final CurrencyMatrixSource underlying) {
    ArgumentChecker.notNull(underlying, "underlying");
    _underlying = underlying;
  }

  protected CurrencyMatrixSource getUnderlying() {
    return _underlying;
  }

  // Resolver

  @Override
  public CurrencyMatrix resolveObject(UniqueId uniqueId, VersionCorrection versionCorrection) {
    try {
      return getUnderlying().getCurrencyMatrix(uniqueId);
    } catch (DataNotFoundException e) {
      return null;
    }
  }

  // IdentifierResolver

  @Override
  public UniqueId resolveExternalId(ExternalIdBundle identifiers, VersionCorrection versionCorrection) {
    final Set<String> names = identifiers.getValues(IDENTIFIER_SCHEME);
    for (String name : names) {
      final CurrencyMatrix matrix = getUnderlying().getCurrencyMatrix(name, versionCorrection);
      if (matrix != null) {
        return matrix.getUniqueId();
      }
    }
    return null;
  }

  @Override
  public UniqueId resolveObjectId(ObjectId identifier, VersionCorrection versionCorrection) {
    try {
      return getUnderlying().getCurrencyMatrix(identifier, versionCorrection).getUniqueId();
    } catch (DataNotFoundException e) {
      return null;
    }
  }

  // ChangeProvider

  @Override
  public ChangeManager changeManager() {
    return getUnderlying().changeManager();
  }

}
