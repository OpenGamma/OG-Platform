/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */

(function ($) {
    var namespace = 'blurkill', elements = [], current_target;
    $(function () {
        $(document).on('mousedown.' + namespace, function (event) {
            elements = elements.filter(function (element) {
                if (current_target === element.target) return true;
                return element.handler(), false;
            });
            current_target = null;
        });
    });
    $.fn[namespace] = function (close) {
        var element = {
            target: this[0], handler: close || (function (element) {return function () {element.remove();};})(this)
        };
        return elements.push(element), this.off('mousedown.' + namespace)
            .on('mousedown.' + namespace, function () {current_target = element.target;});
    };
})(jQuery);