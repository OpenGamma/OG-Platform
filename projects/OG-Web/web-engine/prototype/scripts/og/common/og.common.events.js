/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.events',
    dependencies: [],
    obj: function () {
        var module = this, has = 'hasOwnProperty', coll = module.name, slice = Array.prototype.slice;
        var init_type = function (type) {
            if (!(this[coll] || (this[coll] = {}))[has](type)) {
                this[coll][type] = [];
            }
        };
        return {
            fire: function (type) {
                init_type.call(this, type);
                var args = slice.call(arguments, 1), lcv, events = this[coll][type], len = events.length;
                for (lcv = 0; lcv < len; lcv += 1) {
                    if (false === events[lcv].handler.apply(events[lcv].context, events[lcv].args.concat(args))) {
                        return false;
                    }
                }
                return true;
            },
            off: function (type, handler) {
                if (!this[coll] || !this[coll][has](type)) {
                    return this;
                }
                this[coll][type] = this[coll][type] // if no handler was passed, clear all
                    .filter(function (listener) {return handler ? listener.handler !== handler : false; });
                return this;
            },
            on: function (type, handler, context) {
                if (!handler) {
                    throw new TypeError(module.name + ': handler is undefined for ' + type + ' event');
                }
                init_type.call(this, type);
                this[coll][type].push({handler: handler, args: slice.call(arguments, 3), context: context});
                return this;
            }
        };
    }
});