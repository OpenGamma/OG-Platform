/**
 * Copyright 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.util.ui.message',
    dependencies: [],
    obj: function () {
        return function (obj) {
            if (typeof obj.location !== 'string') throw new TypeError('obj.location must be a string');
            if (obj.css && typeof obj.css !== 'object') throw new TypeError('obj.css must be an object');
            if (obj.level && typeof obj.level !== 'string') throw new TypeError('obj.level must be a string');
            var timers = {}, rand = Math.random().toString().replace(/\d\.([0-9]+)/, '$1'),
                css_class = '.og-js-message-' + rand,
                msg_box = '<div class="' + css_class.replace('.', '') + ' OG-message"></div>',
                location = obj.location, css = obj.css,
                level = obj.level, levels = {weak: 'og-weak', strong: 'og-strong'},
                message = typeof obj.message === 'object' ? obj.message : {0: obj.message},
                clear_timers = function () {$.each(timers, function (index, timer) {clearTimeout(timer);});};
            $(location).find('[class^=og-js-message-]').remove(); // delete old message at the same location
            if (obj.destroy) return;
            $(location).append(msg_box).find(css_class).css(css || {}).hide();
            if (obj.level && levels[obj.level]) $(location + ' ' + css_class).addClass(levels[obj.level]);
            $.each(message, function (duration, message) { // Show messages
                timers[duration] = setTimeout(function () {
                    return $(location).find(css_class)[0] ? $(location).find(css_class).html(message).show()
                        : clear_timers();
                }, duration);
            });
            if (obj.live_for) // Kill everything after live_for milliseconds
                setTimeout(function () {return clear_timers(), $(location).find(css_class).remove();}, obj.live_for);
        }
    }
});