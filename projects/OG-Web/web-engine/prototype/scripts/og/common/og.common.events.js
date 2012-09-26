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
                    if (false === events[lcv].handler.apply(events[lcv].context, events[lcv].args.concat(args))) break;
            },
            on: function (type, handler, context) {
                if (type in this.events) this.events[type]
                    .push({handler: handler, args: Array.prototype.slice.call(arguments, 3), context: context});
                return this;
            }
        };
    }
});