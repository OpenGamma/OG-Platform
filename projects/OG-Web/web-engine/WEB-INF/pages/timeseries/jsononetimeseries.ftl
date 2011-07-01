<#escape x as x?html>
{
    "template_data": {
            "object_id": "${timeseries.uniqueId.objectId}",
            "version_id": "${timeseries.uniqueId.version}",
            "data_source": "${timeseries.dataSource}",
            "data_provider": "${timeseries.dataProvider}",
            "data_field": "${timeseries.dataField}",
            "observation_time": "${timeseries.observationTime}"
    },
    "identifiers": [
    <#list timeseries.identifiers.iterator() as item>
    	{"scheme": "${item.identityKey.scheme}", "value": "${item.identityKey.value}", "date":{"start":"${item.validFrom}", "end":"${item.validTo}"}}<#if item_has_next>,</#if>
   	</#list>
    ],
    "timeseries": {
        "fieldLabels": ["Time", "Value (USD)"],
        "data": [
        	<#list timeseries.timeSeries.toZonedDateTimeDoubleTimeSeries().iterator() as item>[${item.key.toInstant().toEpochMillisLong()?c},${item.value}]<#if item_has_next>,</#if></#list>
        ]
    }
}
</#escape>