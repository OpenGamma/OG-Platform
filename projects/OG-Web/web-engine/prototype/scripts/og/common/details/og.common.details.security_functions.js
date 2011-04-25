/**
 * @copyright 2009 - 2010 by OpenGamma Inc
 * @license See distribution for license
 */

$.register_module({
    name: 'og.common.details.security_functions',
    dependencies: [],
    obj: function () {
        var security_functions,
            render_security_identifiers = function (selector, json) {
                var html = [], id;
                for (id in json) if (json.hasOwnProperty(id)) html.push('<div><strong>', json[id], '</strong></div>');
                $(selector).html(html.join(''));
            };
        return security_functions = {render_security_identifiers: render_security_identifiers};

    }
});