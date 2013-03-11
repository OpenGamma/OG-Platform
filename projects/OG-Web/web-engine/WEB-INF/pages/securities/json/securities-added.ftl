<#escape x as x?html>
{
	"header" : {
	    "type":"Added Securities",
	    "scheme":"${requestScheme.name}"
	},
	"data" : [
	<#list addedSecurities?keys as key>
    	"${key}|${addedSecurities[key]}"<#if key_has_next>,</#if>
	</#list>
	]
}
</#escape>