Standard (i.e. post Big Bang) CDS
=================================

A post Big Bang CDS security on a single name is represented in the system by
``com.opengamma.financial.security.credit.StandardCDSSecurity``.
Compared to a legacy, or pre Big Bang, cds ``com.opengamma.financial.security.credit.LegacyCDSSecurity`` a number of
attributes (e.g. coupon frequency and restructuring) have been standardised and are thus implicitly specified and
not needed when booking the trade.

Trade date
----------
The date this trade occurred. Commonly referred to as T.

Maturity date
-------------
The date this contract matures.

Reference entity
----------------
The entity this contract refers to. e.g. ``MARKIT_RED_CODE~16B9CT`` for Centrica Plc.

Coupon
------
The coupon amount that the protection buyer must pay. The amount must be given as a fraction (e.g. 100 bps should be represented as 0.01).
Typically 0.01 or 0.05.

Buy protection
--------------
Is protection being bough or sold?

Notional
--------
The notional size and currency of the contract.

Debt seniority
--------------
The seniority of the debt. Supported values:
* SENIOR
* SUBORDINATED
* JRSUBUT2 - Junior Subordinated or Upper Tier 2 Debt (Banks) - MarkIt notation
* PREFT1 - Preference Shares or Tier 1 Capital (Banks) - MarkIt notation
* SECDOM - Secured Debt (Corporate/Financial) or Domestic Currency Sovereign Debt (Government) - MarkIt notation
* SNRFOR - Senior Unsecured Debt (Corporate/Financial), Foreign Currency Sovereign Debt (Government) - MarkIt notation
* SUBLT2 - Subordinated or Lower Tier 2 Debt (Banks) - MarkIt notation
* None




