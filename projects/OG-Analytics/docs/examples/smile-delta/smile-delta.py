from com.opengamma.financial.model.option.definition import SmileDeltaTermStructureParameter
from com.opengamma.math.interpolation.data import ArrayInterpolator1DDataBundle
from com.opengamma.financial.model.option.definition import SmileDeltaParameter
from math import sqrt

TIME_TO_EXPIRY = [0.0, 0.25, 0.50, 1.00, 2.00]
NB_TIME_DATA = 5
ATM = [0.175, 0.185, 0.18, 0.17, 0.16]
DELTA = [0.10, 0.25]
NB_DELTA = 2
RISK_REVERSAL = [[-0.010, -0.0050], [-0.011, -0.0060], [-0.012, -0.0070], [-0.013, -0.0080], [-0.014, -0.0090]]
STRANGLE = [[0.0300, 0.0100], [0.0310, 0.0110], [0.0320, 0.0120], [0.0330, 0.0130], [0.0340, 0.0140]]

smile_term = SmileDeltaTermStructureParameter(TIME_TO_EXPIRY, DELTA, ATM, RISK_REVERSAL, STRANGLE)

forward = 1.40
expiryMax = 2.0
nbExp = 200
nbVol = 5

expiries = []
volatilityT = range(0, nbVol)
variancePeriod0 = range(0, nbVol)
variancePeriod1 = range(0, nbVol)
variancePeriodT = range(0, nbVol)
strikes = []

### @export "create-table"
f = open("smile-delta.txt", "w")

# Write header row
f.write("Expiry\t")
for loopdelta in range(0, NB_DELTA):
    f.write("Put %f\t" % DELTA[loopdelta])
f.write("ATM\t")
for loopdelta in range(0, NB_DELTA):
    f.write("Call %f\t" % DELTA[-(loopdelta+1)])
f.write("\n")

for loopexp in range(0, nbExp):
    expiries.append(loopexp * expiryMax / nbExp)
    interpData = ArrayInterpolator1DDataBundle(TIME_TO_EXPIRY, range(0,NB_TIME_DATA))
    index_lower = interpData.getLowerBoundIndex(expiries[loopexp])
    if (expiries[loopexp] < 1.0E-10):
        for loopvol in range(0, nbVol):
            volatilityT[loopvol] = smile_term.getVolatilityTerm()[index_lower].getVolatility()[loopvol]
    else:
        weight0 = (TIME_TO_EXPIRY[index_lower + 1] - expiries[loopexp]) / (TIME_TO_EXPIRY[index_lower + 1] - TIME_TO_EXPIRY[index_lower]);
        for loopvol in range(0, nbVol):
            variancePeriod0[loopvol] = smile_term.getVolatilityTerm()[index_lower].getVolatility()[loopvol] * smile_term.getVolatilityTerm()[index_lower].getVolatility()[loopvol] * TIME_TO_EXPIRY[index_lower]
            variancePeriod1[loopvol] = smile_term.getVolatilityTerm()[index_lower + 1].getVolatility()[loopvol] * smile_term.getVolatilityTerm()[index_lower + 1].getVolatility()[loopvol] * TIME_TO_EXPIRY[index_lower + 1]
            variancePeriodT[loopvol] = weight0 * variancePeriod0[loopvol] + (1 - weight0) * variancePeriod1[loopvol]
            volatilityT[loopvol] = sqrt(variancePeriodT[loopvol] / expiries[loopexp])
    smile = SmileDeltaParameter(expiries[loopexp], DELTA, volatilityT)
    strikes.append(smile.getStrike(forward))
    f.write("%s\t" % expiries[loopexp])
    for loopvol in range(0, nbVol):
        f.write("%s\t" % strikes[loopexp][loopvol])
    f.write("\n")
