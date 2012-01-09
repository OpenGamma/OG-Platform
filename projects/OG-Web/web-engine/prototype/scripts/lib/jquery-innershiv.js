/* innerShiv: makes HTML5shim work on innerHTML & jQuery
 * http://jdbartlett.github.com/innershiv
 *
 * This program is free software. It comes without any warranty, to
 * the extent permitted by applicable law. You can redistribute it
 * and/or modify it under the terms of the Do What The Fuck You Want
 * To Public License, Version 2, as published by Sam Hocevar. See
 * http://sam.zoy.org/wtfpl/COPYING for more details.
 */
window.innerShiv = (function () {
    var div;
    var doc = document;
    var needsShiv;
    
    // Array of elements that are new in HTML5
    var html5 = 'abbr article aside audio canvas datalist details figcaption figure footer header hgroup mark meter nav output progress section summary time video'.split(' ');
    
    // Used to idiot-proof self-closing tags
    function fcloseTag(all, front, tag) {
        return (/^(?:area|br|col|embed|hr|img|input|link|meta|param)$/i).test(tag) ? all : front + '></' + tag + '>';
    }
    
    return function (
        html, /* string */
        returnFrag /* optional false bool */
    ) {
        if (!div) {
            div = doc.createElement('div');
            
            // needsShiv if can't use HTML5 elements with innerHTML outside the DOM
            div.innerHTML = '<nav></nav>';
            needsShiv = div.childNodes.length !== 1;
            
            if (needsShiv) {
                // MSIE allows you to create elements in the context of a document
                // fragment. Jon Neal first discovered this trick and used it in his
                // own shimprove: http://www.iecss.com/shimprove/
                var shimmedFrag = doc.createDocumentFragment();
                var i = html5.length;
                while (i--) {
                    shimmedFrag.createElement(html5[i]);
                }
                
                shimmedFrag.appendChild(div);
            }
        }
        
        html = html
            // Trim whitespace to avoid unexpected text nodes in return data:
            .replace(/^\s\s*/, '').replace(/\s\s*$/, '')
            // Strip any scripts:
            .replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '')
            // Fix misuses of self-closing tags:
            .replace(/(<([\w:]+)[^>]*?)\/>/g, fcloseTag)
            ;
        
        // Fix for using innerHTML in a table
        var tabled;
        if (tabled = html.match(/^<(tbody|tr|td|th|col|colgroup|thead|tfoot)[\s\/>]/i)) {
            div.innerHTML = '<table>' + html + '</table>';
        } else {
            div.innerHTML = html;
        }
        
        // Avoid returning the tbody or tr when fixing for table use
        var scope;
        if (tabled) {
            scope = div.getElementsByTagName(tabled[1])[0].parentNode;
        } else {
            scope = div;
        }
        
        // If not in jQuery return mode, return child nodes array
        if (returnFrag === false) {
            return scope.childNodes;
        }
        
        // ...otherwise, build a fragment to return
        var returnedFrag = doc.createDocumentFragment();
        var j = scope.childNodes.length;
        while (j--) {
            returnedFrag.appendChild(scope.firstChild);
        }
        
        return returnedFrag;
    };
}());

(function ($) {
    var init = $.fn.init, html = $.fn.html;
    // overwrite so that $(html_string) gets innerShiv treatment
    $.fn.init = function (selector, context, root) {
        var nodes;
        if (typeof selector === 'string' && ~selector.indexOf('>') && ~selector.indexOf('<'))
            return (nodes = innerShiv(selector, false)).length ?
            // for innerShiv + HTML5 to work, the REAL DOM needs to be touched,
            // it's insufficient to just have nodes in memory (all in IE8)
                (new init(nodes, context, root)).appendTo('body').remove()
                    : new init(selector, context, root);
        return new init(selector, context, root);
    };
    // overwrite so that $(selector).html(html_string) gets innerShiv treatment
    $.fn.html = function (input) {
        if (typeof input !== 'string') return html.call(this, input);
        return this.empty().append(innerShiv(input, false));
    };
})($);