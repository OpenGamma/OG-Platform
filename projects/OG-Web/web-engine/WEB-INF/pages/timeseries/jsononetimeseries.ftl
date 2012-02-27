<#escape x as x?html>
{
    "template_data": {
            "name": "${info.name}",
            "object_id": "${info.uniqueId.objectId}",
            "version_id": "${info.uniqueId.version}",
            "data_field": "${info.dataField}",
            "data_source": "${info.dataSource}",
            "name": "${info.name}",
            "data_provider": "${info.dataProvider}",
            "observation_time": "${info.observationTime}"<#if deleted>,
	    "deleted": "${infoDoc.versionToInstant}"</#if>
    },
    "identifiers": [
        <#list info.externalIdBundle.externalIds as item>
            {   "scheme": "${item.externalId.scheme}", 
                "value": "${item.externalId.value}", 
                "date":{"start":"${item.validFrom}", "end":"${item.validTo}"}
            }   <#if item_has_next>,</#if>
   	</#list>
    ],
    "related": [
        <#list related as item>
            {   "object_id": "${item.uniqueId.objectId}", 
                "data_field":"${item.dataField}", 
                "data_source":"${item.dataSource}", 
                "data_provider":"${item.dataProvider}", 
                "observation_time":"${item.observationTime}"
            }   <#if item_has_next>,</#if>
   	</#list>
    ],
    "timeseries": {
        "fieldLabels": ["Time", "Value"],
        "data": [
        	<#list timeseries.timeSeries.toZonedDateTimeDoubleTimeSeries().iterator() as item>[${item.key.toInstant().toEpochMillisLong()?c},${item.value?c}]<#if item_has_next>,</#if></#list>
        ]
    }
}
</#escape>
