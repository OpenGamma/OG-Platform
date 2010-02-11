// Automatically created - do not modify
// Created from com\opengamma\livedata\LiveDataSubscriptionResponse.proto:15(7)
package com.opengamma.livedata;
public enum LiveDataSubscriptionResult {
  NOT_AUTHORIZED (2),
  SUCCESS (0),
  NOT_PRESENT (1),
  INTERNAL_ERROR (4),
  INVALID_REQUEST (3);
  private final int _fudgeEncoding;
  private LiveDataSubscriptionResult (final int fudgeEncoding) {
    _fudgeEncoding = fudgeEncoding;
  }
  public int getFudgeEncoding () {
    return _fudgeEncoding;
  }
  public static LiveDataSubscriptionResult fromFudgeEncoding (final int fudgeEncoding) {
    switch (fudgeEncoding) {
      case 2 : return NOT_AUTHORIZED;
      case 0 : return SUCCESS;
      case 1 : return NOT_PRESENT;
      case 4 : return INTERNAL_ERROR;
      case 3 : return INVALID_REQUEST;
      default : throw new IllegalArgumentException ("Field is not a LiveDataSubscriptionResult - invalid value '" + fudgeEncoding + "'");
    }
  }
}
