print 'Yield Curves are built using a combination of Interest Rate instruments, including Swaps, Futures, Forward Rate Agreements and Cash loans.'
print 'Here we introduce a couple to familiarize the reader with our way of doing things'


# **** Cash ****

print 'Let us begin with the simplest of instruments, a Cash loan paying Simple-Compounding interest, a unit invested at rate R will pay 1+R*T at maturity, T'
print 'These are important as these simple deposit rates are often used at the short end of the curve, before the first available swap maturity'
import com.opengamma.financial.interestrate as IR
import com.opengamma.financial.interestrate.cash.definition.Cash as Cash
import com.opengamma.util.money.Currency as CCY
ccy = CCY.EUR
T = 1.0
R = 0.03
Nosh = 10000.0
print 'loan = Cash(ccy,T,Nosh,R,"EUR")'
loan = Cash(ccy,T,Nosh,R,"EUR")
print 'With our derivative object defined, we may now make calculations based on it. OpenGamma implements these calculations as Visitors.'
print 'See http://en.wikipedia.org/wiki/Visitor_pattern'

print '\nLet us compute the Present Value'
pvc = IR.PresentValueCalculator.getInstance()

print 'Of course, to price, we will need some market data. The OG way is to bundle up a derivative\'s market data into its own class. For the Cash loan, this is just the yield curve. For now, we will build one assuming known rates at y= 2%'
print 'The unit/convention of our yield curves is Continuous-Compounding, i.e. a zero coupon bond maturing at T has current value exp(-r(T)*T)'

import com.opengamma.financial.model.interestrate.curve as CURVE # contains YieldCurve and YieldAndDiscountCurve 
import com.opengamma.math as MATH
print 'y = 0.02'
print 'yieldCurve = CURVE.YieldCurve(MATH.curve.ConstantDoublesCurve(y))'
print 'mktData = IR.YieldCurveBundle(["EUR"],[yieldCurve])'

y = 0.02
yieldCurve = CURVE.YieldCurve(MATH.curve.ConstantDoublesCurve(y))
mktData = IR.YieldCurveBundle(["EUR"],[yieldCurve])

print 'yieldCurve.getInterestRate(1.0) = ' + str(yieldCurve.getInterestRate(1.0))
print 'yieldCurve.getInterestRate(2.0) = ' + str(yieldCurve.getInterestRate(2.0))
print 'yieldCurve.getDiscountFactor(1.0) = ' + str(yieldCurve.getDiscountFactor(1.0))
print 'yieldCurve.getDiscountFactor(2.0) = ' + str(yieldCurve.getDiscountFactor(2.0))

print 'OK. Here we go. The PV of our loan is '
pv = pvc.visit(loan,mktData)
print pv
print 'To confirm, the theoretical value of loaning one Euro today is Z(0,T)*(1+R*T) - 1'
print 'where Z(0,T) is a zero coupon bond paying 1 at T'
Z = yieldCurve.getDiscountFactor(T)
print str(Nosh * (Z * (1 + R*T) - 1))
print 'Good.'

print 'How about the ParRate of the Loan? Is this the rate that sets the PV to 0? ie (exp(y*T)-1)/T?'
from math import *

print '(exp(y*T)-1)/T = ' + str((exp(y*T)-1)/T)
print 'IR.ParRateCalculator.getInstance().visitCash(loan,mktData)) = ' + str(IR.ParRateCalculator.getInstance().visitCash(loan,mktData))
print 'Indeed it is.\n\n\n'


# **** Annuity ****

print 'Next, we cover the Annuity, a periodic set of interest payments, rate*accrual. The Cash derivative is simply a one period fixed rate annuity with principal exchanges.'
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed as AnnuityCouponFixed
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIbor as AnnuityCouponIbor
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap as FixedFloatSwap
A1 = AnnuityCouponFixed(ccy,[T],R,"EUR",False)
pv = pvc.visit(A1,mktData)
print 'pv of 1 period annuity = ' + str(pv) 
print 'with principal exchange and same notional =  ' + str(Nosh*(pv+Z-1))

print '\nThe (Vanilla) Swap is simply an exchange of two annuities, one paying a fixed rate and the other a floating one.'
print 'In EUR, vanilla receiver swaps pay semiannual floating and annual fixed. We construct a 5 year RFS like so' 
print 'fixed = AnnuityCouponFixed(ccy, range(1,5+1),R,"EUR",False)'
print 'floating = AnnuityCouponIbor(ccy, [i*0.5 for i in range(1,2*5+1)],"EUR","EUR",True)'
print 'swap = FixedFloatSwap(fixed,floating)'

maturity = 5
fixed = AnnuityCouponFixed(ccy, range(1,maturity+1),R,"EUR",False)
floating = AnnuityCouponIbor(ccy, [i*0.5 for i in range(1,2*maturity + 1)],"EUR","EUR",True)
swap = FixedFloatSwap(fixed,floating)
print swap

print 'The PV of each is given next. As the yield is 2% and the fixed rate is 3%, we imagine the fixed leg will be worth more than the float, and hence a Receiver Swap will have a positive value.'
print 'pvc.visit(fixed,mktData) = ' + str(pvc.visit(fixed,mktData))
print 'pvc.visit(floating,mktData) = ' + str(pvc.visit(floating,mktData))
print 'pvc.visit(swap,mktData) = ' + str(pvc.visit(swap,mktData))

print 'And the ParRate?'
print 'IR.ParRateCalculator.getInstance().visit(swap,mktData)) = ' + str(IR.ParRateCalculator.getInstance().visit(swap,mktData))






