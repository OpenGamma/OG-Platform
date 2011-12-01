<#escape x as x?html>
{
	"header" : {
	    "type":"Added Time Series",
	    "scheme":"${requestScheme}"
	},
	"data" : [
	<#list addedSeries?keys as key>
    	"${key}|${addedSeries[key]}"<#if key_has_next>,</#if>
	</#list>
	]
}
</#escape>