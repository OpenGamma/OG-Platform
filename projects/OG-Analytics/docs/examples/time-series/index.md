# Introduction to Time Series Classes

## Class List

{% set datetime_info = [
  [
	"javax.time.calendar.LocalDate",
	"com.opengamma.util.timeseries.localdate",
	"1 Day",
	"ArrayLocalDateDoubleTimeSeries",
	"ListLocalDateDoubleTimeSeries",
	"MapLocalDateDoubleTimeSeries"
  ],
  [
	"java.util.Date",
	"com.opengamma.util.timeseries.date",
	"1 Day",
	"ArrayDateDoubleTimeSeries",
	"ListDateDoubleTimeSeries",
	"MapDateDoubleTimeSeries"
  ],
  [
	"java.sql.Date",
	"com.opengamma.util.timeseries.sqldate",
	"1 Day",
	"ArraySQLDateDoubleTimeSeries",
	"ListSQLDateDoubleTimeSeries",
	"MapSQLDateDoubleTimeSeries"
  ]
] %}

javax.time.calendar.ZonedDateTime   com.opengamma.util.timeseries.zoneddatetime     ArrayZonedDateTimeDoubleTimeSeries  ListZonedDateTimeDoubleTimeSeries
java.util.Date  com.opengamma.util.timeseries.datetime  ArrayDateTimeDoubleTimeSeries   ListDateTimeDoubleTimeSeries
java.lang.Double    com.opengamma.util.timeseries.yearoffset    ArrayYearOffsetDoubleTimeSeries     ListYearOffsetDoubleTimeSeries 

<table>
<tr><th>Java Date Type</th><th>OG Package</th><th>Accuracy</th><th>Array Storage</th><th>List Storage</th><th>Map Storage</th></tr>
{% for jdpn, ogpn, dur, array_cls, list_cls, map_cls in datetime_info %}
<tr>
  <td>{{ jdpn }}</td>
  <td>{{ ogpn }}</td>
  <td>{{ dur }}</td>
  <td><a href="/dev/java/javadocs/{{ ogpn.replace(".", "/") }}/{{ array_cls }}.html">{{ array_cls }}</a></td>
  <td><a href="/dev/java/javadocs/{{ ogpn.replace(".", "/") }}/{{ list_cls }}.html">{{ list_cls }}</a></td>
  <td><a href="/dev/java/javadocs/{{ ogpn.replace(".", "/") }}/{{ map_cls }}.html">{{ map_cls }}</a></td>
{% endfor %}
</table>








