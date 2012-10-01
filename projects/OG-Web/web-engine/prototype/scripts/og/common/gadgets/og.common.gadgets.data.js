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
            var gadget = this, inst = false, alive = prefix + counter++, selector = $(config.selector), source = {
                   type: 'portfolio',
                   depgraph: false,
                   viewdefinition: "DbCfg~1541221", 
                   live: true,
                   provider: 'Live market data (Bloomberg, Activ, TullettPrebon, ICAP)'
               }, css_position = {position: 'absolute', top: '0', left: 0, right: 0, bottom: 0}, dgrid;
            gadget.alive = function () {
                var live = !!$('.' + alive).length;
                if (!live) gadget.die();
                return live;
            };
            gadget.load = function () {
                var col = 2, row = 16; selector.addClass(alive).css(css_position);
                (new og.analytics.Cell({source: source, col: col, row: row})).on('data', function (data) {
                    if (data) {                
                        if (!inst) dgrid = (selector.ogdata([{data: data}])), inst = true;
                        else gadget.update({data:data});
                    }
                });
            };
            gadget.die = function () {
                dgrid.die();
            };
            gadget.update = function(input) {
                dgrid.update(input)
            };
            gadget.load();
            gadget.resize = gadget.load;
            if (!config.child) og.common.gadgets.manager.register(gadget);
        };
    }
});