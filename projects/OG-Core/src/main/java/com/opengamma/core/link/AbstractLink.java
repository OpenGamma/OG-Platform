/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.link;

/**
 * Represents a link to an object using an identifier for the object. The link can be
 * resolved on demand. Use of links allows provision of objects by remote servers while
 * maintaining the ability to capture updates to the linked resources on each subsequent
 * resolution.
 *
 * @param <I> the type of the identifier for the linked object
 * @param <T> the type of the object being linked to
 */
public abstract class AbstractLink<I, T> implements Link<I, T> {

  /**
   * The identifier for the linked object.
   */
  private final I _identifier;

  /**
   * The class of the object being linked to.
   */
  private final Class<T> _type;

  /**
   * The resolver used to resolve the link on demand.
   */
  private final LinkResolver<T> _resolver;

  /**
   * Constructs the abstract link.
   *
   * @param identifier the identifier for the linked object
   * @param type The class of the object being linked to.
   * @param resolver The resolver used to resolve the link on demand.
   */
  public AbstractLink(I identifier, Class<T> type, LinkResolver<T> resolver) {
    _type = type;
    _identifier = identifier;
    _resolver = resolver;
  }

  @Override
  public I getIdentifier() {
    return _identifier;
  }

  @Override
  public Class<T> getType() {
    return _type;
  }

  @Override
  public String toString() {
    return "Link->" + getType() + ":" + getIdentifier();
  }

  @Override
  public T resolve() {
    return _resolver.resolve();
  }
}
