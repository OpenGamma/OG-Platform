/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * <p/>
 * Please see distribution for license.
 */
package com.opengamma.auth;

import com.opengamma.lambdava.functions.Function1;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * <p/>
 * Please see distribution for license.
 */
public class Either<A, B> {
  private final A _a;
  private final B _b;
  private final boolean _isA;

  private Either(A a, B b, boolean isA) {
    this._a = a;
    this._b = b;
    this._isA = isA;
  }

  public static <A, B> Either<A, B> left(A a) {
    return new Either<>(a, (B) null, true);
  }

  public static <A, B> Either<A, B> right(B b) {
    return new Either<>((A) null, b, false);
  }


  public static <A, B, R> R match(Either<A, B> e, Function1<A, R> ifA, Function1<B, R> ifB) {
    if (e._isA) {
      return ifA.execute(e._a);
    } else {
      return ifB.execute(e._b);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Either either = (Either) o;

    if (_a != null ? !_a.equals(either._a) : either._a != null) {
      return false;
    }
    if (_b != null ? !_b.equals(either._b) : either._b != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = _a != null ? _a.hashCode() : 0;
    result = 31 * result + (_b != null ? _b.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Either{" +
        "_left=" + _a +
        ", _right=" + _b +
        '}';
  }

  @FudgeBuilderFor(Either.class)
  public static class FudgeBuilder implements org.fudgemsg.mapping.FudgeBuilder<Either> {

    private static final String LEFT = "left";
    private static final String RIGHT = "right";
    private static final String LEFT_TYPE = "left_type";
    private static final String RIGHT_TYPE = "right_type";


    @Override
    public MutableFudgeMsg buildMessage(FudgeSerializer serializer, Either object) {
      MutableFudgeMsg rootMsg = serializer.newMessage();
      if (object._a != null) {
        serializer.addToMessage(rootMsg, LEFT, null, object._a);
        serializer.addToMessageObject(rootMsg, LEFT_TYPE, null, object._a.getClass(), Class.class);
      }
      if (object._b != null) {
        serializer.addToMessage(rootMsg, RIGHT, null, object._b);
        serializer.addToMessageObject(rootMsg, RIGHT_TYPE, null, object._b.getClass(), Class.class);
      }
      return rootMsg;
    }

    @Override
    public Either buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
      Object left = null;
      Object right = null;
      if (message.getByName(LEFT_TYPE) != null) {
        Class leftType = deserializer.fieldValueToObject(Class.class, message.getByName(LEFT_TYPE));
        left = deserializer.fieldValueToObject(leftType, message.getByName(LEFT));
      }
      if (message.getByName(RIGHT_TYPE) != null) {
        Class rightType = deserializer.fieldValueToObject(Class.class, message.getByName(RIGHT_TYPE));
        right = deserializer.fieldValueToObject(rightType, message.getByName(RIGHT));
      }
      return new Either(left, right, left != null);
    }
  }
}
