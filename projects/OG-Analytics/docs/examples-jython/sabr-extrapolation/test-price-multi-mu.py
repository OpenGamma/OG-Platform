### @export "another-table"
from com.opengamma.financial.convention.businessday import BusinessDayConventionFactory
from com.opengamma.financial.convention.calendar import MondayToFridayCalendar
from com.opengamma.financial.convention.daycount import DayCountFactory
from com.opengamma.financial.instrument.annuity import AnnuityCouponFixedDefinition
from com.opengamma.financial.instrument.annuity import AnnuityCouponIborDefinition
from com.opengamma.financial.instrument.index import CMSIndex
from com.opengamma.financial.instrument.index import IborIndex
from com.opengamma.financial.instrument.payment import CouponCMSDefinition
from com.opengamma.financial.instrument.swap import SwapFixedIborDefinition
from com.opengamma.financial.interestrate import PresentValueCalculator
from com.opengamma.financial.interestrate import TestsDataSets
from com.opengamma.financial.interestrate.payments.method import CouponCMSSABRExtrapolationRightReplicationMethod
from com.opengamma.financial.model.option.definition import SABRInterestRateDataBundle
from com.opengamma.financial.schedule import ScheduleCalculator
from com.opengamma.util.money import Currency
from com.opengamma.util.time import DateUtil
from javax.time.calendar import Period

ANNUITY_TENOR = Period.ofYears(5)
CUT_OFF_STRIKE = 0.10
BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following")
CALENDAR = MondayToFridayCalendar("A")
CUR = Currency.USD
DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360")
FIXED_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("30/360")
FIXED_IS_PAYER = True
FIXED_PAYMENT_PERIOD = Period.ofMonths(6)
INDEX_TENOR = Period.ofMonths(3)
IS_EOM = True
NOTIONAL = 1000000
PAYMENT_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360")
RATE = 0.0325
REFERENCE_DATE = DateUtil.getUTCDate(2010, 8, 18)
SETTLEMENT_DATE = DateUtil.getUTCDate(2020, 4, 28)
SETTLEMENT_DAYS = 2

FIXING_DATE = ScheduleCalculator.getAdjustedDate(SETTLEMENT_DATE, CALENDAR, -SETTLEMENT_DAYS)
ACCRUAL_START_DATE = SETTLEMENT_DATE
ACCRUAL_END_DATE = ScheduleCalculator.getAdjustedDate(ACCRUAL_START_DATE, BUSINESS_DAY, CALENDAR, FIXED_PAYMENT_PERIOD)
PAYMENT_DATE = ACCRUAL_END_DATE
ACCRUAL_FACTOR = PAYMENT_DAY_COUNT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE)
IBOR_INDEX = IborIndex(CUR, INDEX_TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM)
IBOR_ANNUITY = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, 1.0, IBOR_INDEX, not FIXED_IS_PAYER);
FIXED_ANNUITY = AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, ANNUITY_TENOR, FIXED_PAYMENT_PERIOD, CALENDAR, FIXED_DAY_COUNT, BUSINESS_DAY, IS_EOM, 1.0, RATE, FIXED_IS_PAYER)
SWAP_DEFINITION = SwapFixedIborDefinition(FIXED_ANNUITY, IBOR_ANNUITY)
CMS_INDEX = CMSIndex(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, ANNUITY_TENOR)
CMS_COUPON_RECEIVER_DEFINITION = CouponCMSDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP_DEFINITION, CMS_INDEX)

FUNDING_CURVE_NAME = "Funding"
FORWARD_CURVE_NAME = "Forward"
CURVES_NAME = [FUNDING_CURVE_NAME, FORWARD_CURVE_NAME]


CMS_COUPON = CMS_COUPON_RECEIVER_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME)
print CMS_COUPON

PVC = PresentValueCalculator.getInstance()

from com.opengamma.financial.interestrate.payments.method import CouponCMSSABRReplicationMethod
METHOD_STANDARD_CPN = CouponCMSSABRReplicationMethod()

sabrParameter = TestsDataSets.createSABR1()
curves = TestsDataSets.createCurves1()
sabrBundle = SABRInterestRateDataBundle(sabrParameter, curves)
priceCouponStd = METHOD_STANDARD_CPN.presentValue(CMS_COUPON, sabrBundle)
rateCouponStd = priceCouponStd / (CMS_COUPON.getPaymentYearFraction() * CMS_COUPON.getNotional() * curves.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(CMS_COUPON.getPaymentTime()))
print "method SABR", rateCouponStd*100

for mu in [1.10, 1.30, 1.55, 2.25, 3.50, 6.00, 15.0]:
    methodExtrapolation = CouponCMSSABRExtrapolationRightReplicationMethod(CUT_OFF_STRIKE, mu)
    priceCouponExtra = methodExtrapolation.presentValue(CMS_COUPON, sabrBundle)
    rateCouponExtra = priceCouponExtra / (CMS_COUPON.getPaymentYearFraction() * CMS_COUPON.getNotional() * curves.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(CMS_COUPON.getPaymentTime()))
    print "method SABR with Extrapolation, mu = ", mu, " : ", rateCouponExtra * 100

priceCouponNoAdj = PVC.visit(CMS_COUPON, curves)
rateCouponNoAdj = priceCouponNoAdj / (CMS_COUPON.getPaymentYearFraction() * CMS_COUPON.getNotional() * curves.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(CMS_COUPON.getPaymentTime()))
print "no convexity adjustment", rateCouponNoAdj * 100

