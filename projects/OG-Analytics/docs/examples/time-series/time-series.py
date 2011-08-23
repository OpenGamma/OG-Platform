#java.util.Date  com.opengamma.util.timeseries.date  ArrayDateDoubleTimeSeries   ListDateDoubleTimeSeries
#java.sql.Date   com.opengamma.util.timeseries.sqldate   ArraySQLDateDoubleTimeSeries    ListSQLDateDoubleTimeSeries
#javax.time.calendar.ZonedDateTime   com.opengamma.util.timeseries.zoneddatetime     ArrayZonedDateTimeDoubleTimeSeries  ListZonedDateTimeDoubleTimeSeries
#java.util.Date  com.opengamma.util.timeseries.datetime  ArrayDateTimeDoubleTimeSeries   ListDateTimeDoubleTimeSeries
#java.lang.Double    com.opengamma.util.timeseries.yearoffset    ArrayYearOffsetDoubleTimeSeries     ListYearOffsetDoubleTimeSeries 

### @export "local-date-imports"
from javax.time.calendar import LocalDate
from com.opengamma.util.timeseries.localdate import ArrayLocalDateDoubleTimeSeries
from com.opengamma.util.timeseries.localdate import ListLocalDateDoubleTimeSeries

### @export "build-ts-up"
ts1 = ListLocalDateDoubleTimeSeries()
ts1.putDataPoint(LocalDate.of(2010, 1, 1), 2.1)
ts1.putDataPoint(LocalDate.of(2010, 1, 2), 2.2)
ts1.putDataPoint(LocalDate.of(2010, 1, 3), 2.3)

### @export "ts-from-existing"
ts2 = ArrayLocalDateDoubleTimeSeries(ts1)

### @export "ts-from-arrays"
date_array = [
        LocalDate.of(2010, 1, 1),
        LocalDate.of(2010, 1, 2),
        LocalDate.of(2010, 1, 3)
    ]
ts3 = ArrayLocalDateDoubleTimeSeries(date_array, [2.1, 2.2, 2.3])

print ts1
print ts2
print ts3

### @export "ts-methods"
time_series = ts3
for c in dir(time_series):
    print c

print time_series.times()
print time_series.values()
print time_series.maxValue()
print time_series.minValue()

print time_series.negate()
print time_series.reciprocal()

