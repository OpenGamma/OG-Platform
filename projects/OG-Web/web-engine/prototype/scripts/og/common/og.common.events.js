/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.events',
    dependencies: [],
    obj: function () {
        var module = this;
        return {
            fire: function (events) {
                var args = Array.prototype.slice.call(arguments, 1), lcv, len = events.length;
                for (lcv = 0; lcv < len; lcv += 1)
                    if (false === events[lcv].handler.apply(events[lcv].context, events[lcv].args.concat(args)))
                        return false;
                return true;
            },
            off: function (type, handler) {
                if (type in this.events) this.events[type] = this.events[type].filter(function (listener) {
                    return handler ? listener.handler !== handler : false; // if no handler was passed, clear all
                }); else throw new TypeError(module.name + ': no ' + type + ' event exists');
                return this;
            },
            on: function (type, handler, context) {
                if (!handler) throw new TypeError(module.name + ': handler is undefined for ' + type + ' event');
                if (type in this.events) this.events[type]
                    .push({handler: handler, args: Array.prototype.slice.call(arguments, 3), context: context});
                else throw new TypeError(module.name + ': no ' + type + ' event exists');
                return this;
            }
        };
    }
});