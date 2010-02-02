// automatically created - Mon Feb 01 16:32:20 GMT 2010
// created from /home/andrew/OpenGamma/OG-Build/projects/OG-LiveData/src/com/opengamma/livedata/LiveDataSubscriptionResponse.proto:15(7)
package com.opengamma.livedata;
public enum LiveDataSubscriptionResult {
  NOT_AUTHORIZED (2),
  SUCCESS (0),
  NOT_PRESENT (1),
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
      case 3 : return INVALID_REQUEST;
      default : throw new IllegalArgumentException ("Field is not a LiveDataSubscriptionResult - invalid value '" + fudgeEncoding + "'");
    }
  }
}
