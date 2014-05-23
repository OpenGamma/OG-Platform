/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.target;

import java.io.ObjectStreamException;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.text.StrBuilder;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.MemoryUtils;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;

/**
 * An immutable reference to a particular computation target that will be resolved later on in the view compilation process to a {@link ComputationTargetSpecification} that satisfies the requirement.
 */
@PublicAPI
public final class ComputationTargetRequirement extends ComputationTargetReference {

  private static final long serialVersionUID = 1L;

  /**
   * The identifiers of the target, never null
   */
  private final ExternalIdBundle _identifiers;

  /**
   * Creates a reference to a required computation target.
   * 
   * @param targetType the type of the target, not null
   * @param id the target identifier, may be null for {@link ComputationTargetType#NULL} target type only
   */
  public ComputationTargetRequirement(final ComputationTargetType targetType, final ExternalId id) {
    super(targetType);
    if (targetType != ComputationTargetType.NULL) {
      ArgumentChecker.notNull(id, "id");
      _identifiers = id.toBundle();
    } else {
      ArgumentChecker.isTrue(id == null, "id");
      _identifiers = ExternalIdBundle.EMPTY;
    }
  }

  /**
   * Creates a reference to a required computation target.
   * 
   * @param targetType the type of the target, not null
   * @param bundle the identifier bundle, may be null or empty for {@link ComputationTargetType#NULL} target type only
   */
  public ComputationTargetRequirement(final ComputationTargetType targetType, final ExternalIdBundle bundle) {
    super(targetType);
    if (targetType != ComputationTargetType.NULL) {
      ArgumentChecker.notNull(bundle, "bundle");
      ArgumentChecker.isFalse(bundle.isEmpty(), "bundle");
      _identifiers = bundle;
    } else {
      ArgumentChecker.isTrue((bundle == null) || bundle.isEmpty(), "bundle");
      _identifiers = ExternalIdBundle.EMPTY;
    }
  }

  /* package */ComputationTargetRequirement(final ComputationTargetType targetType, final ExternalIdBundle bundle, final ComputationTargetReference parent) {
    super(targetType, parent);
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.isFalse(bundle.isEmpty(), "bundle");
    _identifiers = bundle;
  }

  private ComputationTargetRequirement(final ComputationTargetReference parent, final ComputationTargetType type, final ExternalIdBundle identifiers) {
    super(parent, type);
    _identifiers = identifiers;
  }

  /**
   * Creates a requirement that describes an arbitrary target. The identifier is not resolved to a target but will be presented as an arbitrary parameter to a function.
   * 
   * @param identifier the identifier to hold, not null
   * @return the target requirement, not null
   */
  public static ComputationTargetRequirement of(final ExternalId identifier) {
    return new ComputationTargetRequirement(ComputationTargetType.PRIMITIVE, identifier);
  }

  /**
   * Creates a requirement that describes an arbitrary target. The identifiers are not resolved to a target but the preferred one from the bundle will be presented as an arbitrary parameter to a
   * function.
   * 
   * @param bundle the identifiers to hold, not null
   * @return the target specification, not null
   */
  public static ComputationTargetRequirement of(final ExternalIdBundle bundle) {
    return new ComputationTargetRequirement(ComputationTargetType.PRIMITIVE, bundle);
  }

  /**
   * Gets the external identifier bundle, if one exists.
   * 
   * @return the external identifier bundle, may be null
   */
  public ExternalIdBundle getIdentifiers() {
    return _identifiers;
  }

  @Override
  public ComputationTargetRequirement getRequirement() {
    return this;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof ComputationTargetRequirement) {
      ComputationTargetRequirement other = (ComputationTargetRequirement) obj;
      return super.equals(obj) && ObjectUtils.equals(_identifiers, other._identifiers);
    }
    return false;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    if (_identifiers != null) {
      result = prime * result + _identifiers.hashCode();
    }
    return result;
  }

  @Override
  protected String getIdStringImpl() {
    return getIdentifiers().toString();
  }

  @Override
  public String toString() {
    return new StrBuilder()
        .append("CTReq[")
        .append(getType())
        .append(", ")
        .append(getIdString())
        .append(']')
        .toString();
  }

  private Object readResolve() throws ObjectStreamException {
    return MemoryUtils.instance(this);
  }

  @Override
  protected ComputationTargetRequirement create(final ComputationTargetReference parent, final ComputationTargetType type) {
    return new ComputationTargetRequirement(parent, type, getIdentifiers());
  }

  @Override
  public <T> T accept(final ComputationTargetReferenceVisitor<T> visitor) {
    return visitor.visitComputationTargetRequirement(this);
  }

}
