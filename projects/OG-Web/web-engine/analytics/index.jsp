<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
  <head>
    <title>OpenGamma Analytics</title>
    <meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
    
    <script type="text/javascript" src="/js/jquery/jquery-1.4.2.min.js"></script>
    <script type="text/javascript" src="/js/jquery/jquery.event.drag-2.0.min.js"></script>
    <script type="text/javascript" src="/js/jquery/jquery.json-2.2.js"></script>
    <script type="text/javascript" src="/js/jquery/jquery.sparkline.js"></script>
    <script type="text/javascript" src="/js/jquery/jquery.cookie.js"></script>
    <script type="text/javascript" src="/js/jquery/jquery-ui-1.8.5.custom.min.js"></script>
    <script type="text/javascript" src="/js/jquery/jquery.ui.core.min.js"></script>
    <script type="text/javascript" src="/js/jquery/jquery.ui.tabs.min.js"></script>
    <link rel="stylesheet" href="/css/jquery/smoothness/jquery-ui-1.8.5.custom.css" type="text/css" charset="utf-8"  media="screen, print" />
    
    <!--[if IE]><script language="javascript" type="text/javascript" src="/js/excanvas/excanvas.min.js"></script><![endif]-->
    
    <script type="text/javascript" src="/js/cometd/cometd.js"></script>
    <script type="text/javascript" src="/js/jquery/jquery.cometd.js"></script>
    
    <script type="text/javascript" src="/js/jquery/jquery.transform-0.6.2.min.js"></script>
    <script type="text/javascript" src="/js/jquery/jquery.jdCrazyDots.js"></script>

    <script type="text/javascript" src="/js/slickgrid/slick.editors.js"></script>
    <script type="text/javascript" src="/js/slickgrid/slick.grid.js"></script>
    <script type="text/javascript" src="/js/slickgrid/slick.model.js"></script>
    <link rel="stylesheet" href="/css/slickgrid/slick.grid.css" type="text/css" charset="utf-8" media="screen, print" />
    
    <script type="text/javascript" src="/js/flot/jquery.flot.js"></script>
    <script type="text/javascript" src="/js/flot/jquery.flot.selection.js"></script>
    <script type="text/javascript" src="/js/flot/jquery.flot.navigate.js"></script>
    <script type="text/javascript" src="/js/flot/jquery.flot.crosshair.js"></script>
  
    <script type="text/javascript" src="js/common.js"></script>
    <script type="text/javascript" src="js/userConfig.js"></script>
    <script type="text/javascript" src="js/liveResultsClient.js"></script>
    <script type="text/javascript" src="js/formatting/columnFormatter.js"></script>
    <script type="text/javascript" src="js/formatting/primitiveFormatter.js"></script>
    <script type="text/javascript" src="js/formatting/matrixFormatter.js"></script>
    <script type="text/javascript" src="js/formatting/interpolatedYieldCurveFormatter.js"></script>
    <script type="text/javascript" src="js/formatting/interpolatedYieldCurveDetail.js"></script>
    <script type="text/javascript" src="js/formatting/interpolatedYieldCurveChart.js"></script>
    <script type="text/javascript" src="js/formatting/interpolatedYieldCurveData.js"></script>
    <script type="text/javascript" src="js/formatting/volatilitySurfaceDataFormatter.js"></script>
    <script type="text/javascript" src="js/formatting/volatilitySurfaceDataDetail.js"></script>
    <script type="text/javascript" src="js/formatting/volatilitySurfaceDataData.js"></script>
    <script type="text/javascript" src="js/formatting/labelledMatrix1DFormatter.js"></script>
    <script type="text/javascript" src="js/formatting/labelledMatrix1DDetail.js"></script>
    <script type="text/javascript" src="js/formatting/unknownTypeFormatter.js"></script>
    <script type="text/javascript" src="js/slickGridHelper.js"></script>
    <script type="text/javascript" src="js/popupManager.js"></script>
    <script type="text/javascript" src="js/portfolioViewer.js"></script>
    <script type="text/javascript" src="js/primitivesViewer.js"></script>
    <script type="text/javascript" src="js/tabbedViewResultsViewer.js"></script>
    <script type="text/javascript" src="js/home.js"></script>
    
    <script type="text/javascript">
      var config = {
        contextPath: '${pageContext.request.contextPath}'
      };
    </script>

    <link rel="stylesheet" href="css/analytics-base.css" type="text/css" charset="utf-8" media="screen, print" />
    <link rel="stylesheet" href="css/analytics-print.css" type="text/css" media="print" charset="utf-8" />

    <style type="text/css" media="screen, print">

      .cell-title {
        font-weight: bold;
        margin: 0 4px;
      }

      .cell-effort-driven {
        text-align: center;
      }

      .toggle {
        height: 9px;
        width: 9px;
        margin: 0 0 0 4px;
        display: inline-block;
      }

      .toggle.expand {
        background: url(/images/expand.gif) no-repeat center center;
      }

      .toggle.collapse {
        background: url(/images/collapse.gif) no-repeat center center;
      }

      .tickindicator {
        height: 16px;
        width: 10px;
        display: inline-block;
        vertical-align: middle;
        padding: 0;
        position: absolute;
        right: 2px;
        top: 50%;
        margin: -7px 0 0 0;
      }

      .tickindicator.up {
        background: url(/images/tick-up.png) no-repeat;
      }

      .tickindicator.down {
        background: url(/images/tick-down.png) no-repeat;
      }

      .tickindicator.same {
        background: url(/images/tick-same.png) no-repeat;
      }
      
      .cell-contents {
        width: 100%;
        height: 100%;
        vertical-align: middle;
        position: relative;
      }
      
      .cell-value {
        text-overflow: ellipsis;
        margin: 0;
        padding: 0 4px;
      }
      
      .cell-value.right {
        position: absolute;
        right: 12px;
        background: white;
      }
      
      .negative {
        color: red;
      }

      .primitive-history-sparkline {
        display: inline-block;
        vertical-align: middle;
        margin: 0 0 0 2px;
        padding: 0;
        height: 14px;
        width: 50px;
      }

      .interpolated-yield-curve, .volatility-surface {
        display: inline-block;
        align: center;
        margin: 5px;
      }

      .interpolated-yield-curve-detail-tabs, .volatility-surface-detail-tabs {
        width: 100%;
        height: 100%;
      }

      .interpolated-yield-curve-detail-tabs .ui-tabs-nav li a, .volatility-surface-detail-tabs .ui-tabs-nav li a {
        font-size: 90%;
      }

      .interpolated-yield-curve-detail-tabs .ui-tabs-nav span, .volatility-surface-detail-tabs .ui-tabs-nav span {
        font-size: 100%;
      }

      .interpolated-yield-curve-detail-tabs .data, .interpolated-yield-curve-detail-tabs .curve, .volatility-surface-detail-tabs .data, .volatility-surface-detail-tabs .curve {
        position: absolute;
        top: 2.8em;
        bottom: 0;
        left: 0;
        right: 0;
        padding: 0;
      }

      .interpolated-yield-curve-detail-tabs .data .grid, .volatility-surface-detail-tabs .data .grid {
        position: absolute;
        top: 0;
        bottom: 0;
        left: 0;
        right: 0;
      }

      .interpolated-yield-curve-detail-tabs .curve .chart, .volatility-surface-detail-tabs .curve .chart {
        position: absolute;
        top: 8px;
        bottom: 8px;
        left: 8px;
        right: 8px;
      }
      
      .detail-popup {
        z-index: 1;
        padding: 8px;
      }

      .detail-content {
        height: 100%;
        width: 100%;
      }

      .header {
        width: 289px;
        height: 51px;
        display: inline-block;
        background: url(/images/opengamma.png) no-repeat center center;
        margin-left: 1em;
        margin-bottom: 1em;
        margin-top: 1em;
      }

      .viewcontrols {
        text-align: right;
        float: right;
        margin-top: 1em;
        margin-right: 1em;
      }

      #currentviewcontrols {
        margin-right: 3px;
        margin-top: 5px;
      }

      .imgbutton {
        cursor: pointer;
        vertical-align: middle;
        width: 14px;
        height: 16px;
        margin: 0 1px;
        display: inline-block;
      }

      .imgbutton.resume.on {
        background: url(/images/resume-on.png) no-repeat center center;
      }

      .imgbutton.resume.off {
        background: url(/images/resume-off.png) no-repeat center center;
        cursor: auto;
      }

      .imgbutton.pause.on {
        background: url(/images/pause-on.png) no-repeat center center;
      }

      .imgbutton.pause.off {
        background: url(/images/pause-off.png) no-repeat center center;
        cursor: auto;
      }

      .imgbutton.sparklines {
        width: 16px;
      }

      .imgbutton.sparklines.off {
        background: url(/images/sparklines-off.png) no-repeat center center;
      }

      .imgbutton.sparklines.on {
        background: url(/images/sparklines-on.png) no-repeat center center;
      }

      .imgbutton.revealmore {
        width: 14px;
        height: 14px;
      }

      #viewstatus {
        margin: 0;
        padding: 0;
        vertical-align: middle;
        margin-right: 5px;
        display: inline-block;
      }

      #viewstatus p {
        margin: 0;
        padding: 0;
      }

      .statustitle {
        font-weight: bold;
      }

      #resultsViewer #tabs {
        position: absolute;
        top: 7em;
        left: 1em;
        right: 1em;
        bottom: 1em;
      }

      #loading {
        margin: 5em auto;
        position: relative;
        width: 0;
      }

      #resultsViewer #loading .dot {
        position: absolute;
        opacity: 0.0;
        filter: alpha(opacity=0);
        background: -moz-linear-gradient(top left, #033c5a, #477187);
        background: -webkit-gradient(linear, left top, left bottom, from(#033c5a), to(#477187));
        -moz-border-radius: 50%;
        -webkit-border-radius: 15px;
      }

      #resultsViewer #tabs #portfolio, #resultsViewer #tabs #portfolio #portfolioGrid,
      #resultsViewer #tabs #primitives, #resultsViewer #tabs #primitives #primitivesGrid {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
      }

      #resultsViewer #tabs #portfolio, #resultsViewer #tabs #primitives {
        top: 3em;
      }

      #views {
        display: inline-block;
      }

      /* JQuery UI theme overrides */

      .ui-tabs {
        padding: 0;
        border-bottom-left-radius: 0;
        border-bottom-right-radius: 0;
      }

      .ui-tabs-nav {
        border-top: none;
        border-left: none;
        border-right: none;
        border-bottom-left-radius: 0;
        border-bottom-right-radius: 0;
      }

      .ui-tabs-nav li a {
        font-weight: bold;
      }

      .ui-button .ui-button-text {
        display: inline;
      }

      .ui-autocomplete-input {
        font-size: 1em;
        height: 16px;
        margin: 0;
        padding: 0 0.2em;
      }

   </style>
  </head>
  <body>
    <div class="header"></div>
    <div class="viewcontrols">
      <div>
        <div id="views"></div>
        <div id="changeView"></div>
        <div id="sparklines" class="imgbutton sparklines"></div>
      </div>
      <div id="currentviewcontrols">
        <div id="viewstatus"><p>No view loaded</p></div>
        <div id="resume" class="imgbutton resume"></div>
        <div id="pause" class="imgbutton pause"></div>
      </div>
    </div>

    <div id="resultsViewer"></div>
  </body>
</html>
