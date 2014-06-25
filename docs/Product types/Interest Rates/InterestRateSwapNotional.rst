Bullet, variable, amortizing, accreting & rollercoaster notionals.
============================================================================

Both bullet (i.e. constant) notional and variable notional are supported, e.g. as seen in amortizing, accreting & rollercoaster.

Constant notional.
----------------------------------------------------------------------------

This is the most common type. Specified by providing a currency \& notional amount to com.opengamma.financial.security.irs.InterestRateSwapNotional. e.g:

.. code:: Java

  com.opengamma.financial.security.irs.InterestRateSwapNotional.of(Currency.USD, 1000000)

Variable, custom or free notional.
----------------------------------------------------------------------------

A fully customizable notional schedule may be provided. Each step in the schedule may give the absolute notional to use or reference an adjustment to the previous notional in the schedule.
Each step value is accompanied by a field describing how to modify the notional on that date, supported adjustments are:

  * OUTRIGHT - this provided value will take effect on the given date. This is the default if the type is omitted.
  * DELTA - the previous notional will be multiplied by this amount to obtain the current notional amount (step should be less than 1 for an amortizing swap).
  * ADDITIVE - the shift amount will be added to the previous notional to obtain the current notional amount (step should be negative for an amortizing swap).

Not all periods during the lifetime of the instrument need be provided. For omitted periods the notional will remain unchanged from the last specified period.

For example an example amortising schedule would look like:

.. code:: Java

  com.opengamma.financial.security.irs.InterestRateSwapNotional.of(Currency.USD, Lists.asList(date1, date2), Lists.asList(1e6, 0))

Notional schedule derived from an algorithm or expression.
----------------------------------------------------------------------------

It is not possible to provide an expression to determine the notional. Instead the full schedule must be pre-computed and entered when the trade is loaded.