{	"portfolioId" : "${portfolio.objectId}",
	"portfolioVersion" : "${portfolio.version}",
	"portfolioName" : "${portfolio.name}",
	"data" : [
	<#list trades as item>
		{	"tradeId" : "${item.objectid}",
			"tradeVersion" : "${item.version}",
			"securityName" : "${item.securityname}",
			"isNew" : ${item.isnew},
			"fields" : [
			<#list item.fields as item>
				{	"fieldName" : "${item.name}",
					"ogValue" : "${item.ogvalue}",
					"dealValue" : "${item.dealvalue}",
					"origDealValue" : "${item.origdealvalue}",
					"selection" : "${item.selection}"
				}<#if item_has_next>,</#if>
			</#list>
			]
		}<#if item_has_next>,</#if>
	</#list>
	]
}
