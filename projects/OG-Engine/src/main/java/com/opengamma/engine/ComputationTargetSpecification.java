/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import java.io.ObjectStreamException;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.text.StrBuilder;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetReferenceVisitor;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;
import com.opengamma.util.credit.CreditCurveIdentifier;
import com.opengamma.util.money.Currency;

/**
 * An immutable specification of a particular computation target that will be resolved later on in a computation process to the actual target.
 */
@PublicAPI
public final class ComputationTargetSpecification extends ComputationTargetReference {

  // [PLAT-444]: move to com.opengamma.engine.target

  /**
   * An specification that describes a target value of null. This will always be resolved to a {@link ComputationTarget} that contains a type of {@link ComputationTargetType#NULL} and a null value. It
   * can be used when the function is self describing and can gain no behavioral information from the target.
   */
  public static final ComputationTargetSpecification NULL = MemoryUtils.instance(new ComputationTargetSpecification(ComputationTargetType.NULL, null));

  private static final long serialVersionUID = 1L;

  /**
   * The identifier of the target.
   */
  private final UniqueId _uniqueId;

  /**
   * Creates a lightweight specification of a computation target.
   * 
   * @param targetType the type of the target, not null
   * @param uid the target identifier, may be null for the {@link ComputationTargetType#NULL} target type only
   */
  public ComputationTargetSpecification(final ComputationTargetType targetType, final UniqueId uid) {
    super(targetType);
    if (targetType == ComputationTargetType.NULL) {
      ArgumentChecker.isTrue(uid == null, "uid");
    } else {
      ArgumentChecker.notNull(uid, "uid");
    }
    _uniqueId = uid;
  }

  public/* [PLAT-444]: should be package visible */ComputationTargetSpecification(final ComputationTargetType type, final UniqueId uid, final ComputationTargetReference parent) {
    super(type, parent);
    _uniqueId = uid;
  }

  public/* [PLAT-444]: should be package visible */ComputationTargetSpecification(final ComputationTargetReference parent, final ComputationTargetType type, final UniqueId uid) {
    super(parent, type);
    _uniqueId = uid;
  }

  /**
   * Creates a specification that will reference a portfolio node.
   * 
   * @param portfolioNode the node that will be the target, not null
   * @return the target specification, not null
   */
  public static ComputationTargetSpecification of(final PortfolioNode portfolioNode) {
    return ComputationTargetType.PORTFOLIO_NODE.specification(portfolioNode);
  }

  /**
   * Creates a specification that will reference a position.
   * 
   * @param position the position that will be the target, not null
   * @return the target specification, not null
   */
  public static ComputationTargetSpecification of(final Position position) {
    return ComputationTargetType.POSITION.specification(position);
  }

  /**
   * Creates a specification that will reference a security.
   * 
   * @param security the security that will be the target, not null
   * @return the target specification, not null
   */
  public static ComputationTargetSpecification of(final Security security) {
    return ComputationTargetType.SECURITY.specification(security);
  }

  /**
   * Creates a specification that will reference a trade.
   * 
   * @param trade the trade that will be the target, not null
   * @return the target specification, not null
   */
  public static ComputationTargetSpecification of(final Trade trade) {
    return ComputationTargetType.TRADE.specification(trade);
  }

  /**
   * Creates a specification that describes a currency. A currency may be used as an arbitrary parameter to a function, for example one that provides curve definitions or other meta data that is keyed
   * by currency.
   * 
   * @param currency the currency described, not null
   * @return the target specification, not null
   */
  public static ComputationTargetSpecification of(final Currency currency) {
    return ComputationTargetType.CURRENCY.specification(currency);
  }

  /**
   * Creates a specification that describes a credit curve identifier. A credit curve identifier may be used as an arbitrary parameter to a function, for example one that provides spread curve
   * definitions or other meta data that is keyed by id.
   * 
   * @param creditCurveIdentifier the credit curve identifier described, not null
   * @return the target specification, not null
   */
  public static ComputationTargetSpecification of(final CreditCurveIdentifier creditCurveIdentifier) {
    return ComputationTargetType.CREDIT_CURVE_IDENTIFIER.specification(creditCurveIdentifier);
  }

  /**
   * Creates a specification that describes an arbitrary target. The unique identifier is not resolved to a target but will be presented as an arbitrary parameter to a function.
   * 
   * @param uniqueId the identifier to hold, not null
   * @return the target specification, not null
   */
  public static ComputationTargetSpecification of(final UniqueId uniqueId) {
    return new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, uniqueId);
  }

  /**
   * Gets the unique identifier, if one exists.
   * 
   * @return the unique identifier, may be null if the target type is {@link ComputationTargetType#NULL}
   */
  public UniqueId getUniqueId() {
    return _uniqueId;
  }

  /**
   * Tests if this specification is compatible with another. To be compatible, the target types must be compatible and the unique identifier chain match at each level.
   * 
   * @param other the target specification to test against, not null
   * @return true if the object described by the supplied specification is suitable for this specification
   */
  public boolean isCompatible(final ComputationTargetSpecification other) {
    if (!getType().isCompatible(other.getType())) {
      return false;
    }
    // TODO: should be checking the parent as well
    return ObjectUtils.equals(getUniqueId(), other.getUniqueId());
  }

  @Override
  public ComputationTargetSpecification getSpecification() {
    return this;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof ComputationTargetSpecification) {
      final ComputationTargetSpecification other = (ComputationTargetSpecification) obj;
      return super.equals(obj) && ObjectUtils.equals(_uniqueId, other._uniqueId);
    }
    return false;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    if (_uniqueId != null) {
      result = prime * result + _uniqueId.hashCode();
    }
    return result;
  }

  @Override
  protected String getIdStringImpl() {
    if (getUniqueId() != null) {
      return getUniqueId().toString();
    } else {
      return "NULL";
    }
  }

  @Override
  public String toString() {
    return new StrBuilder()
        .append("CTSpec[")
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
  protected ComputationTargetSpecification create(final ComputationTargetReference parent, final ComputationTargetType type) {
    return new ComputationTargetSpecification(parent, type, getUniqueId());
  }

  @Override
  public <T> T accept(final ComputationTargetReferenceVisitor<T> visitor) {
    return visitor.visitComputationTargetSpecification(this);
  }

}
