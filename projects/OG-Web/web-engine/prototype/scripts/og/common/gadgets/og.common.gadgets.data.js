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
            var gadget = this, inst = false, alive = prefix + counter++, source = {
                   type: 'portfolio',
                   depgraph: false,
                   viewdefinition: "DbCfg~1541221",
                   live: true,
                   provider: 'Live market data (Bloomberg, Activ, TullettPrebon, ICAP)'
               };
            gadget.alive = function () {return !!$('.' + alive).length;};
            gadget.load = function () {
                var arr = config.id.split('|'), col = 2, row = 16;
                $(config.selector)
                    .addClass(alive)
                    .css({position: 'absolute', top: '0', left: 0, right: 0, bottom: 0});
                (new og.analytics.Cell({source: source, col: col, row: row}))
                    .on('data', function (data) {
                        if(data){            
                            if(!inst) gadget.update = ($(config.selector).ogdata([{data: data}])).update, inst = true;
                            else gadget.update({data:data});
                        }
                    });
            };
            gadget.load();
            gadget.resize = gadget.load;
            if (!config.child) og.common.gadgets.manager.register(gadget);
        };
    }
});