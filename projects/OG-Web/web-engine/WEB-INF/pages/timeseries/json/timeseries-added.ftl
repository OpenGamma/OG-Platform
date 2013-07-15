<#escape x as x?html>
{
	"header" : {
	    "type":"Added Time Series",
	    "scheme":"${requestScheme.name}",
            "dataField":"${requestDataField}",
            "dataProvider":"${requestDataProvider}"<#if requestStartDate?has_content>,
            "startDate":"${requestStartDate}"</#if><#if requestEndDate?has_content>,
            "endDate":"${requestEndDate}"</#if>
	},
	"data" : [
	<#list addedTimeSeries?keys as key>
    	"${key}|${addedTimeSeries[key]}"<#if key_has_next>,</#if>
	</#list>
	]
}
</#escape>
