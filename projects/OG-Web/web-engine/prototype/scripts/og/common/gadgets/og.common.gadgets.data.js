/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.gadgets.Data',
    dependencies: ['og.common.gadgets.manager'],
    obj: function () {
        var prefix = 'og_data_gadget_', counter = 1;
        return function (config) {
            var gadget = this, instantiated = false, alive = prefix + counter++,
                css_position = {position: 'absolute', top: '0', left: 0, right: 0, bottom: 0}, $data_grid;
            gadget.alive = function () {
                var live = $(config.selector).length;
                if (!live) gadget.die();
                return live;
            };
            gadget.load = function () {
                $(config.selector).addClass(alive).css(css_position);
                gadget.dataman = new og.analytics.Cell({source: config.source, col: config.col, row: config.row})
                    .on('data', function (data) {
                        if (data) {
                            if (!instantiated)
                                $data_grid = (instantiated = true) && $(config.selector).ogdata({data: data});
                            else gadget.update({data: data});
                        }
                    });
            };
            gadget.die = function () {
                try {$data_grid.die();} catch (error) {};
                try {gadget.dataman.kill();} catch (error) {};
            };
            gadget.update = function (input) {$data_grid.update(input);};
            gadget.load();
            gadget.resize = function () {
                $data_grid.resize();
            }
            if (!config.child) og.common.gadgets.manager.register(gadget);
        };
    }
});