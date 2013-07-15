/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.gadgets.manager',
    dependencies: [],
    obj: function () {
        var manager, collector, gadgets = [], interval = 5 * 60 * 1000, layout,
            resize = function () {
                return manager.clean().forEach(function (gadget) {gadget.resize.call(gadget.context);});
            };
        setTimeout((collector = function () {return manager.clean(), setTimeout(collector, interval);}), interval);
        $(function () {if (!(layout = !!og.views.common.layout)) $(window).on('resize', manager.resize);});
        return manager = {
            clean: function () {
                return gadgets = gadgets.filter(function (gadget) {
                    // if context has been cleared (e.g. removed iframe, a compile-and-go error will be thrown)
                    try {return gadget.alive.call(gadget.context);} catch (error) {return og.dev.warn(error), false;}
                });
            },
            gadgets: function () {return gadgets;},
            register: function (gadget) {manager.clean().push(gadget);},
            resize: (function (timeout) {
                return function () {timeout = clearTimeout(timeout) || setTimeout(resize, layout ? 0 : 200);};
            })(null)
        };
    }
});