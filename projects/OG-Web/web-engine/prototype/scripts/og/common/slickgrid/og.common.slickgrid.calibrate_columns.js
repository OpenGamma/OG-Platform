/*
 * @copyright 2011 - present by OpenGamma Inc
 * @license See distribution for license
 *
 * sets the last slick column to remainder of the width of its container
 */
$.register_module({
    name: 'og.common.slickgrid.calibrate_columns',
    dependencies: [],
    obj: function () {
        return function (config) {
            var cols = config.columns, extra_width;
            extra_width = $(config.container).width() - cols.reduce(function (acc, val) {
                return acc + val.width;
            }, 0) - config.buffer;
            cols[cols.length - 1].width += extra_width > 0 ? extra_width : 0;
            return cols;
        }
    }
});