# OpenGamma Platform

## v2.22

### Bug Fixes
* Correct error in TIPS yield when first coupon already fixed

### Enhancements

* Improve interpolator performance by removing unnecessary array copies
* Cache hash code in trade wrapper subclasses to improve performance

## v2.21

### Enhancements

* Cashflows for FRAs

## v2.20

### Bug Fixes

* Correctly pass time-series to analytics for inflation swaps
* Compute fixing date correctly when non-standard offsets used, or when fixing and payment calendars are different
* Support average Ibor-like floating coupons
* Use correct weights for stub interpolation
* Correctly adjust end accrual date for zero-coupons
* Correctly compute reset dates using coupon index when this differs from the main index of the leg

### Enhancements

* Make market data types serializable

## v2.19

### Enhancements

* Engine Support for Zero Coupon Inflation Swaps - PV, Par Rate and Bucketed Sensitivities

## v2.18

### Bug Fixes

* Fix for underlying index period computation.
* Corrected day count of fixing accrued where the day count of the trade was used instead of the day count associated with the index.
* Fix for Sesame examples configuration errors.

### Enhancements

* Add day counts for 1/1, Act/365L, and 30/360 ISDA.

## v2.17

Integrate Sesame subproject into OG-Platform repository. Sesame provides an alternate calculation engine for OG-Platform.
