/*
 * @copyright 2012 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.analytics.layout_manager',
    dependencies: [],
    obj: function () {
        // start stuff
        var tmp_link = '<div class="tmp-links"><span class="og-open-ts-gadget">timeseries to bottom pane</span></div>';
        $('body').append(tmp_link).find('.tmp-links').css({
            'position': 'absolute', 'top': '10px', 'left': '100px', 'z-index': '2'
        });
        $('.og-open-ts-gadget').on('click', function () {
            $('.OG-layout-analytics-south .OG-gadget-container').addClass('OG-timeseries-gadget').css({
                'position': 'relative', 'margin': '10px'
            });
            og.common.gadgets.timeseries({
                selector: '.OG-layout-analytics-south .OG-gadget-container',
                id: 'DbHts~1001', datapoints_link: false
            });
        });
        // end tmp stuff
        var get_pane = function (elm) {
            console.log(elm);
            return $(elm).parentsUntil('[class*=ui-layout-analytics]', '[class*="OG-layout-analytics"]')
                         .attr('class')
                         .replace(/OG\-layout\-analytics\-([-a-z]+)?\s(?:.+)/, '$1');
        };
        return function () {
            var layout = og.views.common.layout.inner;
            $('.OG-gadget-tabs-options .og-minimize').on('click', function () {
                var max = '25%', min = $(this).parent().parent().height(), pane = get_pane(this);
                layout.state[pane].size === min ? layout.sizePane(pane, max) : layout.sizePane(pane, min);
            });
        }
    }
});