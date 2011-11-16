### @export "local-date-imports"
from javax.time.calendar import LocalDate
from com.opengamma.util.timeseries.localdate import ArrayLocalDateDoubleTimeSeries
from com.opengamma.util.timeseries.localdate import ListLocalDateDoubleTimeSeries

### @export "build-ts-up"
ts1 = ListLocalDateDoubleTimeSeries()
ts1.putDataPoint(LocalDate.of(2010, 1, 1), 2.1)
ts1.putDataPoint(LocalDate.of(2010, 1, 2), 2.2)
ts1.putDataPoint(LocalDate.of(2010, 1, 3), 2.3)
print ts1

### @export "ts-from-existing"
ts2 = ArrayLocalDateDoubleTimeSeries(ts1)
print ts2

### @export "ts-from-arrays"
date_array = [
        LocalDate.of(2010, 1, 1),
        LocalDate.of(2010, 1, 2),
        LocalDate.of(2010, 1, 3)
    ]
ts3 = ArrayLocalDateDoubleTimeSeries(date_array, [1.4, -1.5, 1.6])
print ts3

### @export "sub-series"
ts2
ts4 = ts2.subSeries(LocalDate.of(2010,1,2), LocalDate.of(2010,1,3))
ts4
ts2.tail(1)
ts2.head(1)

### @export "unary-ops"
ts3
ts3.abs()
ts3.log()
ts3.log10()
ts3.negate()
ts3.reciprocal()

### @export "summaries"
ts3.maxValue()
ts3.minValue()

### @export "info"
ts3.times()
ts3.values()
ts3.getEarliestValue()
ts3.getEarliestTime()
ts3.getLatestValue()
ts3.getLatestTime()

### @export "binary-ops-scalar"
ts3
ts3.multiply(2)
ts3.power(2)

### @export "binary-ops"
ts2
ts3
ts3.add(ts2)
ts3.average(ts2)
ts3.divide(ts2)
ts3.maximum(ts2)
ts3.minimum(ts2)
ts3.multiply(ts2)
ts3.power(ts2)
ts3.subtract(ts2)

### @export "union"
ts2
ts3
ts4
ts2.unionAdd(ts3)
ts2.unionAdd(ts4)
ts2.unionAverage(ts3)
ts2.unionAverage(ts4)

### @export "methods-list"
for c in dir(ts3):
    print c

