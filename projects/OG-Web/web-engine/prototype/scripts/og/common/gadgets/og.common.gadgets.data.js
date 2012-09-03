/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.gadgets.data',
    dependencies: ['og.common.gadgets.manager'],
    obj: function () {
        var prefix = 'og_data_gadget_', counter = 1;
        return function (config) {
            var gadget = this, alive = prefix + counter++;
            gadget.alive = function () {return !!$('.' + alive).length;};
            gadget.load = function () {
                var arr = config.id.split('|'), col = arr[0], row = arr[1];
                $(config.selector)
                    .addClass(alive)
                    .css({position: 'absolute', top: '0', left: 0, right: 0, bottom: 0});
                (new og.analytics.Cell({source: grid.source, col: col, row: row}))
                    .on('data', function (data) {$(config.selector).ogdata([{data: data}]);});
            };
            gadget.load();
            gadget.resize = gadget.load;
            if (!config.child) og.common.gadgets.manager.register(gadget);
        };
    }
});