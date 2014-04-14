<#escape x as x?html>
<@page title="Time series - ${infoDoc.uniqueId}">

<!--[if IE]><script language="javascript" type="text/javascript" src="/js/excanvas/excanvas.min.js"></script><![endif]-->
<script type="text/javascript" src="/js/jquery/jquery-1.4.2.min.js"></script>
<script type="text/javascript" src="/js/flot/jquery.flot.js"></script>
<script type="text/javascript" src="/js/flot/jquery.flot.selection.js"></script>


<#-- SECTION Time series output -->
<@section title="Time series">
  <p>
    <@rowout label="Reference">${info.uniqueId.value}</@rowout>
   	<#list info.externalIdBundle.externalIds as item>
    <@rowout label="Identifier">${item.externalId}</@rowout>
   	</#list>
    <@rowout label="Data source">${info.dataSource}</@rowout>
    <@rowout label="Data provider">${info.dataProvider}</@rowout>
    <@rowout label="Data field">${info.dataField}</@rowout>
    <@rowout label="Observation time">${info.observationTime}</@rowout>
    <@rowout label="Name">${info.name}</@rowout>
  </p>
  
<#-- SUBSECTION Chart -->
<@subsection title="Chart">

<p><span id="hoverText">&nbsp;</span></p>

<div id="timeSeriesChart" style="width:600px;height:300px;"></div>

<p><input id="resetChart" type="button" value="Reset" /></p>

<script id="timeSeriesChartScript">
$(function () {
    
    var d = [
    <#list timeseries.timeSeries.iterator() as item>
      [${(item.key.toEpochDay() * 86400000)?c}, ${item.value}],
	</#list>
    ];
    
    var options = { xaxis: { mode: "time" }, selection: { mode: "x" }, grid: { hoverable: true, autoHighlight: false } };
    
    var plot = $.plot($("#timeSeriesChart"), [{ data : d }], 
    	options);
    
    $("#timeSeriesChart").bind("plotselected", function (event, ranges) {
	    // do the zooming
	    plot = $.plot($("#timeSeriesChart"), [d],
	                  $.extend(true, {}, options, {
	                      xaxis: { min: ranges.xaxis.from, max: ranges.xaxis.to }
	                  }));
    });
    
    $("#resetChart").click(function () {
        plot = $.plot($("#timeSeriesChart"), [d], options);
    });
    
    var updatePriceTimeout = null;
    var latestPosition = null;
    var previousHighlightedPoint = null;
    
    function updatePrice() {
        updatePriceTimeout = null;
        
        var pos = latestPosition;
        
        var axes = plot.getAxes();
        if (pos.x < axes.xaxis.min || pos.x > axes.xaxis.max ||
            pos.y < axes.yaxis.min || pos.y > axes.yaxis.max)
            return;

        var i, j, dataset = plot.getData();
        for (i = 0; i < dataset.length; ++i) {
            var series = dataset[i];
            if (series.data.length == 0) {
                continue;
            }

            // find the nearest points, x-wise

            for (j = 0; j < series.data.length; ++j)
                if (series.data[j][0] >= pos.x)
                    break;
            
            var p;
            if (j == 0) {
            	p = series.data[j]; 
            } else {
	            var distanceToPrevious = pos.x - series.data[j - 1][0];
		        var distanceToCurrent = series.data[j][0] - pos.x;
		        if (distanceToPrevious >= distanceToCurrent) {
		        	p = series.data[j]; // take current  
		        } else {
		        	p = series.data[j - 1]; // take previous
		        }
            }
            
            var pointDate = new Date(p[0]);
            var year, month, day;
            year = String(pointDate.getFullYear());
	        month = String(pointDate.getMonth() + 1);
	        if (month.length == 1) {
	            month = "0" + month;
	        }
	        day = String(pointDate.toLocalDate());
	        if (day.length == 1) {
	            day = "0" + day;
	        }
            
            $("#hoverText").text(year + "-" + month + "-" + day + " = " + p[1]);

           	if (previousHighlightedPoint) {
           	  plot.unhighlight(series, previousHighlightedPoint);
           	}
           	plot.highlight(series, p);
           	previousHighlightedPoint = p;
        }
    }
    
    $("#timeSeriesChart").bind("plothover",  function (event, pos, item) {
        latestPosition = pos;
        if (!updatePriceTimeout)
            updatePriceTimeout = setTimeout(updatePrice, 50);
    });
    
});
</script>
</@subsection>  
  
<#-- SUBSECTION Data table -->
<@subsection title="Data points">
<@table items=timeseries.timeSeries.times() empty="No data points" headers=["Time","Value"]; item>
      <td>${item}</td>
      <td>${timeseries.timeSeries.getValue(item)}</td>
</@table>
</@subsection>
<@subsection title="Data point export">
<a href="${uris.oneTimeSeries() + ".csv"}">Download as CSV</a>
</@subsection>
</@section>

<#-- SECTION Update time series -->
<@section title="Update Timeseries">
  <@form method="PUT" action="${uris.oneTimeSeries()}">
  <p>
    <@rowin><input type="submit" value="Update" /></@rowin>
  </p>
  </@form>
</@section>

<#-- SECTION Delete time series -->
<@section title="Delete Timeseries">
  <@form method="DELETE" action="${uris.oneTimeSeries()}">
  <p>
    <@rowin><input type="submit" value="Delete" /></@rowin>
  </p>
  </@form>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.allTimeSeries()}">Time series search</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
