/**
 * @copyright 2009 - 2011 by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.common.util.ui.message',
    dependencies: [],
    obj: function () {
        return function (obj) {
            // Delete all messages at the same location
            $('[class^=og-js-message-' + obj.location.substr(1) + '-]').remove();
            var timers = {}, rand = Math.random().toString().replace(/\d\.([0-9]+)/, '$1'),
                css_class = '.og-js-message-' + obj.location.substr(1) + '-' + rand,
                msg_box = '<div class="' + css_class.replace('.', '') + ' OG-message"></div>',
                location = obj.location, css = obj.css,
                message = typeof obj.message === 'object' ? obj.message : {0: obj.message},
                clear_timers = function () {$.each(timers, function (index, timer) {clearTimeout(timer);});};
            if (obj.destroy) return $(location).find(css_class).remove();
            if (typeof location !== 'string') throw new TypeError('obj.location must be a string');
            if (css && typeof css !== 'object') throw new TypeError('obj.css must be an object');
            if ($(location).find(css_class).length === 0) // Add html if it doesn't already exist
                $(location).append(msg_box).find(css_class).css(css || {}).hide();
            if (typeof message === 'object')
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