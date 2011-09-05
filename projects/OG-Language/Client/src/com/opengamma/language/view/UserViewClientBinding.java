/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.view;


/**
 * Language binding specific data extension to {@link UserViewClient} instances.
 * 
 * @param <T> user specified data type
 */
public abstract class UserViewClientBinding<T extends UserViewClientData> {

  private static final UserViewClientData NULL = new UserViewClientData() {
  };

  /**
   * Returns the binding association object. By default this is the binding instance; so a singleton pattern should be used
   * for constructing the binding instances. If there is object "churn", then the {@link #getClass} object could be used, or
   * simply the language ID string. It just must be unique to the binding.
   * 
   * @return the binding object, never null
   */
  protected Object getBindingObject() {
    return this;
  }

  /**
   * Creates a new user data object.
   * 
   * @param viewClient the view client object
   * @return the new user data object, not null
   */
  protected abstract T create(UserViewClient viewClient);

  /**
   * Fetch the data associated with the view client through this binding, creating a new data object if there is none.
   * 
   * @param viewClient view client object to fetch data for
   * @return the user data, not null
   */
  @SuppressWarnings("unchecked")
  public final T get(final UserViewClient viewClient) {
    T data = (T) viewClient.getData(getBindingObject());
    if (data == null) {
      synchronized (this) {
        data = (T) viewClient.getData(getBindingObject());
        if (data != null) {
          return data;
        }
        try {
          data = create(viewClient);
        } catch (RuntimeException e) {
          viewClient.setData(getBindingObject(), NULL);
          throw e;
        }
        if (data == null) {
          viewClient.setData(getBindingObject(), NULL);
          throw new NullPointerException();
        }
        viewClient.setData(getBindingObject(), data);
      }
    } else if (data == NULL) {
      return null;
    }
    return data;
  }

}
