/**
 * @copyright 2009 - 2011 by OpenGamma Inc
 * @license See distribution for license
 *
 * Renders a Canvas plot using Flot
 * @see http://code.google.com/p/flot/
 * @param {String} selector
 * @param {Array} data
 */
$.register_module({
    name: 'og.common.util.ui.render_plot',
    dependencies: [],
    obj: function () {
        return function (selector, data) { // Current default plot settings
            $.plot($(selector), [{color: '#487287', shadowSize: '0', yaxis: 2, data: data}],  {
                 lines: {lineWidth: 1, fill: 1, fillColor: '#f3f6fa'},
                 grid: {borderWidth: 1, color: '#ccc'},
                 xaxis: {ticks: 6, mode: 'time'},
                 yaxis: {ticks: 5}
             });
        };
    }
});