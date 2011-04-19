<#escape x as x?html>
{
    "templateData": {
            "id": "${timeseriesDoc.uniqueId.value}",
            "dataSource": "${timeseriesDoc.dataSource}",
            "dataProvider": "${timeseriesDoc.dataProvider}",
            "dataField": "${timeseriesDoc.dataField}",
            "observationTime": "${timeseriesDoc.observationTime}"
    },
    "identifiers": [
    <#list timeseriesDoc.identifiers.iterator() as item>
    	{"scheme": "${item.identityKey.scheme}", "value": "${item.identityKey.value}", "date":{"start":"${item.validFrom}", "end":"${item.validTo}"}}<#if item_has_next>,</#if>
   	</#list>
    ],
    "timeseries": {
        "fieldLabels": ["Time", "Value (USD)"],
        "data": [
        	<#list timeseries.toZonedDateTimeDoubleTimeSeries().iterator() as item>[${item.key.toInstant().toEpochMillisLong()?c},${item.value}]<#if item_has_next>,</#if></#list>
        ]
    }
}
</#escape>