// noinspection JSPotentiallyInvalidConstructorUsage,UnreachableCodeJS

/*
 * Licensed under the GPL License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE.
 */
/*  Prototype JavaScript framework, version 1.7.3
 *  (c) 2005-2010 Sam Stephenson
 *
 *  Prototype is freely distributable under the terms of an MIT-style license.
 *  For details, see the Prototype website: http://www.prototypejs.org/
 *
 *--------------------------------------------------------------------------*/


import PropTypes from 'prop-types';

PropTypes.arrayOf = function (shape) {
  return PropTypes.arrayOf(PropTypes.shape(shape));
};
PropTypes.func =  function (shape) {
  return PropTypes.func(PropTypes.shape(shape));
};
PropTypes.func.isRequired = undefined;
let ComponentName = PropTypes.shape({
  toJSON: PropTypes.func.isRequired
});
ComponentName.propTypes = {
  key: PropTypes.arrayOf(PropTypes.shape({
    toJSON: PropTypes.func.isRequired
  })),
  holder: PropTypes.arrayOf(PropTypes.shape({
    toJSON: PropTypes.func.isRequired
  })),
  stack: PropTypes.arrayOf(PropTypes.shape({
    toJSON: PropTypes.func.isRequired
  }))
};

const Prototype = {

  Version: '1.7.3',

  Browser: (function () {
    const ua = navigator.userAgent;
    const isOpera = Object.prototype.toString.call(window.opera) === '[object Opera]';
    return {
      IE: !!window.attachEvent && !isOpera,
      Opera: isOpera,
      WebKit: ua.indexOf('AppleWebKit/') > -1,
      Gecko: ua.indexOf('Gecko') > -1 && ua.indexOf('KHTML') === -1,
      MobileSafari: /Apple.*Mobile/.test(ua)
    }
  })(),

  BrowserFeatures: {
    XPath: !!document.evaluate,

    SelectorsAPI: !!document.querySelector,

    ElementExtensions: (function () {
      const constructor = window.Element || window.HTMLElement;
      return constructor ? constructor.prototype : null;
    })(),
    SpecificElementExtensions: (function () {
      if (typeof window.HTMLDivElement !== 'undefined')
        return true;

      let div = document.createElement('div'),
          form = document.createElement('form'),
          isSupported = false;

      if (Object.getPrototypeOf(div) && (Object.getPrototypeOf(div) !== Object.getPrototypeOf(form))) {
        isSupported = true;
      }

      form = null;

      return isSupported;
    })()
  },

  ScriptFragment: '<script[^>]*>([\\S\\s]*?)<script\\s*>',
  JSONFilter: /^\/\*-secure-([\s\S]*)\*\/\s*$/,

  emptyFunction: function () {
  },

  K: function (x) {
    return x
  }
};

if (Prototype.Browser.MobileSafari)
  Prototype.BrowserFeatures.SpecificElementExtensions = false;
/* Based on Alex Arnell's inheritance implementation. */

let Class = (function() {

  let IS_DONTENUM_BUGGY = (function(){
    for (let p in { toString: 1 }) {
      if (p === 'toString') return false;
    }
    return true;
  })();
  function addMethods(source) {
    let ancestor = this.superclass?.prototype;
    let properties;
    properties = Object.keys(source);

    if (IS_DONTENUM_BUGGY) {
      if (source.toString !== Object.prototype.toString)
        properties.push("toString");
      if (source.valueOf !== Object.prototype.valueOf)
        properties.push("valueOf");
    }

    let i = 0, length = properties.length;
    for (; i < length; i++) {
      let property = properties[i], value = source[property];
      if (!(ancestor && Object.isFunction(value) &&
          value.argumentNames()[0] === "$super")) {
        const method = value;
        value = (function (m) {
          return function () {
            return ancestor[m].apply(this, arguments);
          };
      })(property).wrap(method);

        value.valueOf = (function (method) {
          return function () {
            return method.valueOf.call(method);
          };
        })(method);

        value.toString = (function (method) {
          return function () {
            return method.toString.call(method);
          };
        })(method);
      }
      this.prototype[property] = value;
    }

    return this;
  }

  return {
    Create: create,
    Methods: {
      addMethods: addMethods
    }
  };
})();
Object.extend(Function.prototype, (function() {
  let slice = Array.prototype.slice;

  function update(array, args) {
    let arrayLength = array.length, length = args.length;
    while (length--) array[arrayLength + length] = args[length];
    return array;
  }

  function merge(array, args) {
    array = slice.call(array, 0);
    return update(array, args);
  }

  function argumentNames() {
    let names = this.toString().match(/^\s*function[^(]*\(([^)]*)\)/)[1]
        .replace(/\/\/[^\r\n]*|\/\*[^*]*\*+(?:[^*/][^*]*\*+)*\//g, '')
        .replace(/\s+/g, '').split(',');
    return names.length === 1 && !names[0] ? [] : names;
  }

  function bind(context) {
    if (arguments.length < 2 ?. Object.isUndefined(arguments[0]))
      return this;

    if (!Object.isFunction(this))
      throw new TypeError("The object is not callable.");
    createNopPrototype(this);
    let __method = this, args = slice.call(arguments, 1);

    let bound = function() {
      let a = merge(args, arguments);
      let c = this instanceof bound ? this : context;
      return __method.apply(c, a);
    };

    return bound;
  }

  function createNopPrototype(fn) {
    let nop = function() {
      nop.prototype = fn.prototype;
      bound.prototype = new nop();
      return bound;
    };
    return nop;
  }

  function bindAsEventListener(context) {
    let __method = this, args = slice.call(arguments, 1);
    return function(event) {
      let a = update([(window.Event)], args);
      return __method.apply(context, a);
    };
  }

  function delay(timeout) {
    let __method = this, args = slice.call(arguments, 1);
    timeout = timeout * 1000;
    return window.setTimeout(function() {
      return __method.apply(__method, args);
    }, timeout);
  }

  function defer() {
    let args = update([0.01], arguments);
    return this.delay(...args);
  }

  function wrap(wrapper) {
    let __method = this;
    return function() {
      let a = update([__method.bind(this)], arguments);
      return wrapper.apply(this, a);
    };
  }

  function methodize() {
    if (this._methodized) return this._methodized;
    let __method = this;
    this._methodized = function() {
      let a = update([this], arguments);
      this._methodized = __method(...a);
      let methodizedResult = this._methodized;
      this._methodized = methodizedResult;
      return methodizedResult;
    };
    return this._methodized;
  }

  let extensions = {
    argumentNames:       argumentNames,
    bindAsEventListener: bindAsEventListener,
    delay:               delay,
    defer:               defer,
    wrap:                wrap,
    methodize:           methodize
  };

  if (!Function.prototype.bind) {
    extensions.bind = bind;
  }

  return extensions;
})());


(function(proto) {


  function toISOString() {
    return this.getUTCFullYear() + '-' +
      (this.getUTCMonth() + 1).toPaddedString(2) + '-' +
      this.getUTCDate().toPaddedString(2) + 'T' +
      this.getUTCHours().toPaddedString(2) + ':' +
      this.getUTCMinutes().toPaddedString(2) + ':' +
      this.getUTCSeconds().toPaddedString(2) + 'Z';
  }


  function toJSON() {
    return this.toISOString();
  }

  if (!proto.toISOString) proto.toISOString = toISOString;
  if (!proto.toJSON) proto.toJSON = toJSON;

})(Date.prototype);


RegExp.escape = function(str) {
  return String(str).replace(/([.*+?^=!:${}()|[\]\\])/g, '\\$1');
};
let PeriodicalExecuter = Class.Create({
  initialize: function(callback, frequency) {
    this.callback = callback;
    this.frequency = frequency;
    this.currentlyExecuting = false;

    this.registerCallback();
  },

  registerCallback: function() {
    this.timer = setInterval(this.onTimerEvent.bind(this), this.frequency * 1000);
  },

  execute: function() {
    this.callback(this);
  },

  stop: function() {
    if (!this.timer) return;
    clearInterval(this.timer);
    this.timer = null;
  },

  onTimerEvent: function() {
    if (!this.currentlyExecuting) {
      try {
        this.currentlyExecuting = true;
        this.execute();
        this.currentlyExecuting = false;
      } catch(e) {
        this.currentlyExecuting = false;
        throw e;
      }
    }
  }
});
Object.extend(String, {
  interpret: function(value) {
    return value == null ? '' : String(value);
  },
  specialChar: {
    '\b': '\\b',
    '\t': '\\t',
    '\n': '\\n',
    '\f': '\\f',
    '\r': '\\r',
    '\\': '\\\\'
  }
});

Object.extend(String.prototype, (function() {
  let NATIVE_JSON_PARSE_SUPPORT = window.JSON &&
    typeof JSON.parse === 'function' &&
    JSON.parse('{"test": true}').test;

  function prepareReplacement(replacement) {
    return Object.isFunction(replacement) ? replacement : (match) => new Template(replacement).evaluate(match);
  }
  function gsub(pattern, replacement) {
    let result;
    let source = this;
    replacement = prepareReplacement(replacement);

    if (Object.isString(pattern)) {
      pattern = RegExp.escape(pattern);
    }

    let regex = new RegExp(pattern, 'g');
    result = source.replace(regex, replacement);

    return result;
  }
  function scan(pattern, iterator) {
    this.gsub(pattern, iterator);
    return String(this);
  }
  function strip() {
  return this.replace(/^\s+/g, '').replace(/\s+$/g, '')  }

  function stripTags() {
    return this.replace(/<[^>]+>|<\/\w+>/gi, '')

  }

  function stripScripts() {
    return this.replace(new RegExp(Prototype.ScriptFragment, 'img'), '');
  }

  function extractScripts() {
    let matchAll = new RegExp(Prototype.ScriptFragment, 'img');
    return Array.from(this.matchAll(matchAll), (match) => (match[0].match(/<script[^>]*>([\s\S]*?)<\/script>/i) || ['', ''])[1]);
  }

  function evalScripts() {
    return this.extractScripts().map(function(script) { return eval(script); });
  }

  function escapeHTML() {
    return this.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
  }

  function unescapeHTML() {
    return this.stripTags().replace(/&lt;/g,'<').replace(/&gt;/g,'>').replace(/&amp;/g,'&');
  }


  function toQueryParams(separator) {
    let match = this.strip().match(/(([^&=]+)([^#]*)(#.*)?$) /g);
    if (!match) return {};

    return match[1].split(separator || '&').reduce((params, pair) => {
      let [key, value] = pair.split('=').map(decodeURIComponent);
      value = value.replace(/\+/g, ' ');

      if (key in params) {
        if (!Array.isArray(params[key])) params[key] = [params[key]];
        params[key].push(value);
      } else {
        params[key] = value;
      }

      return params;
    }, {});
  }
  function toArray() {
    return this.split('');
  }

  function succ() {
    return this.slice(0, this.length - 1) +
      String.fromCharCode(this.charCodeAt(this.length - 1) + 1);
  }

  function times(count) {
    return count < 1 ? '' : new Array(count + 1).join(this);
  }

  function camelize() {
    return this.replace(/-+(.)?/g, function(match, chr) {
      return chr ? chr.toUpperCase() : '';
    });
  }

  function capitalize() {
    return this.charAt(0).toUpperCase() + this.substring(1).toLowerCase();
  }
  function inspect(useDoubleQuotes) {
    const escapedString = this.replace(/\\/g, (character) => {
      if (character in String.specialChar) {
        return String.specialChar[character];
      }
      return `\\u00${character.charCodeAt().toString(16).padStart(2, '0')}`;
    });

    if (useDoubleQuotes) {
      return `"${escapedString.replace(/"/g, '\\"')}"`;
    } else {
      return `'${escapedString.replace(/'/g, "\\'")}'`;
    }
  }
  function unfilterJSON(filter) {
    return this.replace(filter || Prototype.JSONFilter, '$1');
  }

  function isJSON() {
    let str = this;
    if (str.blank()) return false;
    str = str.replace(/\\(?:["\\bfnrt]|u[0-9a-fA-F]{4})/g, '@');
    str = str.replace(/"[^"]*"|true|false|null|-?/g, ']');
    str = str.replace(/(?:^|:|,)(?:\s*\[)+/g, '');
    return (/^[\],:{}\s]*$/).test(str);
  }

  function evalJSON(sanitize) {
    let json = this.unfilterJSON();

    try {
      if (!sanitize || json.isJSON()) {
        return JSON.parse(json);
      }
    } catch (e) {}

    throw new SyntaxError('Badly formed JSON string: ' + this.inspect());
  }

  function parseJSON() {
    let json = this.unfilterJSON();
    return JSON.parse(json);
  }

  function include(pattern) {
    return this.indexOf(pattern) > -1;
  }

  function startsWith(pattern, position) {
    position = Object.isNumber(position) ? position : 0;
    return this.lastIndexOf(pattern, position) === position;
  }

  function endsWith(pattern, position) {
    pattern = String(pattern);
    position = Object.isNumber(position) ? position : this.length;

    if (position < 0) position = 0;
    if (position > this.length) position = this.length;

    let slicedString = this.slice(position - pattern.length, position);
    return slicedString === pattern;
  }
  function empty() {
    return this == '';
  }

  function blank() {
    return /^\s*$/.test(this);
  }

  function interpolate(object, pattern) {
    return new Template(this, pattern).evaluate(object);
  }

  return {
    gsub:           gsub,
    scan:           scan,
    strip:          String.prototype.trim || strip,
    stripTags:      stripTags,
    stripScripts:   stripScripts,
    extractScripts: extractScripts,
    evalScripts:    evalScripts,
    escapeHTML:     escapeHTML,
    unescapeHTML:   unescapeHTML,
    toQueryParams:  toQueryParams,
    parseQuery:     toQueryParams,
    toArray:        toArray,
    succ:           succ,
    times:          times,
    camelize:       camelize,
    capitalize:     capitalize,
    inspect:        inspect,
    unfilterJSON:   unfilterJSON,
    isJSON:         isJSON,
    evalJSON:       NATIVE_JSON_PARSE_SUPPORT ? parseJSON : evalJSON,
    include:        include,
    startsWith:     String.prototype.startsWith || startsWith,
    endsWith:       String.prototype.endsWith || endsWith,
    empty:          empty,
    blank:          blank,
    interpolate:    interpolate
  };
})());

let Template = Class.Create({
  initialize: function(template, pattern) {
    this.template = template.toString();
    this.pattern = pattern || Template.Pattern;
  },

  evaluate: function(object) {
    if (object && Object.isFunction(object.toTemplateReplacements))
      object = object.toTemplateReplacements();

    return this.template.gsub(this.pattern, function(match) {
      if (object == null) return (match[1] + '');

      let before = match[1] || '';
      if (before == '\\') return match[2];

      let ctx = object, expr = match[3],
          pattern = /^([^.[]+|\[((?:.*?[^\\])?))(\.|\[|$)/;

      match = pattern.exec(expr);
      if (match == null) return before;

      while (match != null) {
        let comp = match[1].startsWith('[') ? match[2].replace(/\\\\]/g, ']') : match[1];
        ctx = ctx[comp];
        if (null == ctx || '' == match[3]) break;
        expr = expr.substring('[' == match[3] ? match[1].length : match[0].length);
        match = pattern.exec(expr);
      }

      return before + String.interpret(ctx);
    });
  }
});
Template.Pattern = /(^|.|\r|\n)(#\{(.*?))/;

let $break = { };

let Enumerable = (function() {
  function each(iterator, context) {
    try {
      this._each(iterator, context);
    } catch (e) {
      if (e != $break) throw e;
    }
    return this;
  }
  function all(iterator, context) {
    iterator = iterator || Prototype.K;
    let result = true;
    this.each(function(value, index) {
      result = result && !!iterator.call(context, value, index, this);
      if (!result) throw $break;
    }, this);
    return result;
  }

  function any(iterator, context) {
    iterator = iterator || Prototype.K;
    let result = false;
    this.each(function(value, index) {
      let result = !!iterator.call(context, value, index, this);
      if (result) {
        throw $break;
      }
    }, this);
    return result;
  }

  function collect(iterator, context) {
    iterator = iterator || Prototype.K;
    let results = [];
    this.each(function(value, index) {
      results.push(iterator.call(context, value, index, this));
    }, this);
    return results;
  }

  function detect(iterator, context) {
    let result;
    this.each(function(value, index) {
      if (iterator.call(context, value, index, this)) {
        result = value;
        throw $break;
      }
    }, this);
    return result;
  }

  function findAll(iterator, context) {
    let results = [];
    this.each(function(value, index) {
      if (iterator.call(context, value, index, this))
        results.push(value);
    }, this);
    return results;
  }
  function include(object) {
    if (Object.isFunction(this.indexOf) && this.indexOf(object) != -1)
      return true;

    let found = false;
    this.each(function(value) {
      if (value == object) {
        found = true;
        throw $break;
      }
    });
    return found;
  }
  function inject(memo, iterator, context) {
    this.each(function(value, index) {
      memo = iterator.call(context, memo, value, index, this);
    }, this);
    return memo;
  }

  function invoke(method) {
    let args = $A(arguments).slice(1);
    return this.map(function(value) {
      return value[method].apply(value, args);
    });
  }

  function max(iterator, context) {
    iterator = iterator || Prototype.K;
    let result;
    this.each(function(value, index) {
      value = iterator.call(context, value, index, this);
      if (result == null || value >= result)
        result = value;
    }, this);
    return result;
  }

  function min(iterator, context) {
    iterator = iterator || Prototype.K;
    let result;
    this.each(function(value, index) {
      value = iterator.call(context, value, index, this);
      if (result == null || value < result)
        result = value;
    }, this);
    return result;
  }
  function pluck(property) {
    let results = [];
    this.each(function(value) {
      results.push(value[property]);
    });
    return results;
  }

  function reject(iterator, context) {
    let results = [];
    this.each(function(value, index) {
      if (!iterator.call(context, value, index, this))
        results.push(value);
    }, this);
    return results;
  }

  function sortBy(iterator, context) {
    return this.map(function(value, index) {
      return {
        value: value,
        criteria: iterator.call(context, value, index, this)
      };
    }, this).sort(function(left, right) {
      let a = left.criteria, b = right.criteria;
      if (a < b) {
        return -1;
      } else if (a > b) {
        return 1;
      } else {
        return 0;
      }

    }).pluck('value');
  }

  function toArray() {
    return this.map();
  }

  function size() {
    return this.toArray().length;
  }

  function inspect() {
    return '#<Enumerable:' + this.toArray().inspect() + '>';
  }

  return {
    each:       each,
    all:        all,
    every:      all,
    any:        any,
    some:       any,
    collect:    collect,
    map:        collect,
    detect:     detect,
    findAll:    findAll,
    select:     findAll,
    filter:     findAll,
    include:    include,
    member:     include,
    inject:     inject,
    invoke:     invoke,
    max:        max,
    min:        min,
    pluck:      pluck,
    reject:     reject,
    sortBy:     sortBy,
    toArray:    toArray,
    entries:    toArray,
    size:       size,
    inspect:    inspect,
    find:       detect
  };
})();

function $A(iterable) {
  if (!iterable) return [];
  if ('toArray' in Object(iterable)) return iterable.toArray();
  let length = iterable.length || 0, results = new Array(length);
  while (length--) results[length] = iterable[length];
  return results;
}


function $w(string) {
  if (!Object.isString(string)) return [];
  string = string.strip();
  return string ? string.split(/\s+/) : [];
}

Array.from = $A;


(function() {
  let arrayProto = Array.prototype,
      slice = arrayProto.slice,
      _each = arrayProto.forEach; // use native browser JS 1.6 implementation if available

  function each(iterator, context) {
    for (let i = 0, length = this.length >>> 0; i < length; i++) {
      if (i in this) iterator.call(context, this[i], i, this);
    }
  }
  if (!_each) _each = each;

  function clear() {
    this.length = 0;
    return this;
  }

  function first() {
    return this[0];
  }

  function last() {
    return this[this.length - 1];
  }

  function compact() {
    return this.select(function(value) {
      return value != null;
    });
  }

  function flatten() {
    return this.inject([], function(array, value) {
      if (Object.isArray(value))
        return array.concat(value.flatten());
      array.push(value);
      return array;
    });
  }

  function without() {
    let values = slice.call(arguments, 0);
    return this.select(function(value) {
      return !values.include(value);
    });
  }
  function clone() {
    return slice.call(this, 0);
  }

  function size() {
    return this.length;
  }

  function inspect() {
    return '[' + this.map(Object.inspect).join(', ') + ']';
  }

  function indexOf(item, i) {
    if (this == null) throw new TypeError();

    let array = Object(this);
    let length = array.length >>> 0;
    if (length === 0) return -1;

    i = Number(i) || 0;
    if (i >= length) return -1;

    return array.findIndex((value, index) => index >= i && value === item);
  }


  function lastIndexOf(item, i) {
    if (this == null) throw new TypeError();

    let array = Object(this);
    let length = array.length >>> 0;
    if (length === 0) return -1;

    if (!Object.isUndefined(i)) {
      i = Number(i);
      if (isNaN(i)) {
        i = 0;
      } else if (i !== 0 && isFinite(i)) {
        i = (i > 0 ? 1 : -1) * Math.floor(Math.abs(i));
      }
    } else {
      i = length - 1;
    }

    return array.lastIndexOf(item, i);
  }
  function concat(_) {
    let array = [];
    let items = [...arguments].flat();
    array.push(...items);
    return array;
  }


  function wrapNative(method) {
    return function(...args) {
      if (args.length === 0) {
        return method.call(this, Prototype.K);
      } else if (args[0] === undefined) {
        args.shift();
        args.unshift(Prototype.K);
        return method.apply(this, args);
      } else {
        return method.apply(this, args);
      }
    };
  }


  function map(iterator) {
    if (this == null) throw new TypeError();

    iterator = iterator || Prototype.K;
    let object = Object(this);
    let context = arguments[1];

    return Array.from(object, (value, index) => iterator.call(context, value, index, object));
  }

  if (arrayProto.map) {
    map = wrapNative(Array.prototype.map);
  }

  function filter(iterator) {
    if (this == null || !Object.isFunction(iterator))
      throw new TypeError();

    let object = Object(this);
    let results = [], context = arguments[1], value;

    for (let i = 0, length = object.length >>> 0; i < length; i++) {
      if (i in object) {
        value = object[i];
        if (iterator.call(context, value, i, object)) {
          results.push(value);
        }
      }
    }
    return results;
  }

  if (arrayProto.filter) {
    filter = Array.prototype.filter;
  }

  function some(iterator) {
    if (this == null) throw new TypeError();

    iterator = iterator || Prototype.K;
    let context = arguments[1];

    let object = Object(this);
    return Array.from(object).some((value, index) => iterator.call(context, value, index, object));
  }
  if (arrayProto.some) {
    some = wrapNative(Array.prototype.some);
  }

  function every(iterator) {
    if (this == null) throw new TypeError();

    iterator = iterator || Prototype.K;
    let context = arguments[1];

    let object = Object(this);
    return Array.from(object).every((value, index) => iterator.call(context, value, index, object));
  }

  if (arrayProto.every) {
    every = wrapNative(Array.prototype.every);
  }


  Object.extend(arrayProto, Enumerable);

  if (arrayProto.entries === Enumerable.entries) {
    delete arrayProto.entries;
  }

  if (!arrayProto._reverse)
    arrayProto._reverse = arrayProto.reverse;

  Object.extend(arrayProto, {
    _each:     _each,

    map:       map,
    collect:   map,
    select:    filter,
    filter:    filter,
    findAll:   filter,
    any:       some,
    all:       every,

    clear:     clear,
    first:     first,
    last:      last,
    compact:   compact,
    flatten:   flatten,
    without:   without,
    clone:     clone,
    toArray:   clone,
    size:      size,
    inspect:   inspect
  });

  let CONCAT_ARGUMENTS_BUGGY = (function() {
    return [].concat(arguments)[0][0] !== 1;
  })(1,2);

  if (CONCAT_ARGUMENTS_BUGGY) arrayProto.concat = concat;

  if (!arrayProto.indexOf) arrayProto.indexOf = indexOf;
  if (!arrayProto.lastIndexOf) arrayProto.lastIndexOf = lastIndexOf;
})();
function $H(object) {
  return new Hash(object);
}

let Hash = Class.Create(Enumerable, (function() {
  function initialize(object) {
    this._object = Object.isHash(object) ? object.toObject() : Object.clone(object);
  }


  function _each(iterator, context) {
    let i = 0;
    const object = this._object;
    const keys = Object.keys(object);

    for (let key of keys) {
      let value = object[key];
      let pair = { key, value };
      iterator.call(context, pair, i);
      i++;
    }
  }
  function set(key, value) {
    this._object[key] = value;
    return this._object[key];

  }

  function get(key) {
    if (this._object[key] !== Object.prototype[key])
      return this._object[key];
  }

  function unset(key) {
    let value = this._object[key];
    delete this._object[key];
    return value;
  }

  function toObject() {
    return Object.clone(this._object);
  }



  function keys() {
    return this.pluck('key');
  }

  function values() {
    return this.pluck('value');
  }

  function index(value) {
    let match = this.detect(function(pair) {
      return pair.value === value;
    });
    return match?.key;

  }


  function update(object) {
    return new Hash(object).inject(this, function(result, pair) {
      result.set(pair.key, pair.value);
      return result;
    });
  }

  function toQueryPair(key, value) {
    if (Object.isUndefined(value)) return key;

    value = String.interpret(value);

    value = value.gsub(/(\r)?\n/, '\r\n');
    value = encodeURIComponent(value);
    value = value.gsub(/%20/, '+');
    return key + '=' + value;
  }

  function toQueryString() {
    return this.inject([], function(results, pair) {
      let key = encodeURIComponent(pair.key), values = pair.value;

      if (values && typeof values == 'object') {
        if (Object.isArray(values)) {
          let queryValues = [];
          for (let i = 0, len = values.length, value; i < len; i++) {
            value = values[i];
            queryValues.push(toQueryPair(key, value));
          }
          return results.concat(queryValues);
        }
      } else results.push(toQueryPair(key, values));
      return results;
    }).join('&');
  }

  function inspect() {
    return '#<Hash:{' + this.map(function(pair) {
      return pair.map(Object.inspect).join(': ');
    }).join(', ') + '}>';
  }

  function clone() {
    return new Hash(this);
  }

  return {
    initialize:             initialize,
    _each:                  _each,
    set:                    set,
    get:                    get,
    unset:                  unset,
    toObject:               toObject,
    toTemplateReplacements: toObject,
    keys:                   keys,
    values:                 values,
    index:                  index,
    update:                 update,
    toQueryString:          toQueryString,
    inspect:                inspect,
    toJSON:                 toObject,
    clone:                  clone
  };
})());

Hash.from = $H;
Object.extend(Number.prototype, (function() {
  function toColorPart() {
    return this.toPaddedString(2, 16);
  }

  function succ() {
    return this + 1;
  }

  function toPaddedString(length, radix) {
    let string = this.toString(radix || 10);
    return '0'.times(length - string.length) + string;
  }
  function round() {
    return Math.round(this);
  }
  return {
    toColorPart:    toColorPart,
    succ:           succ,
    toPaddedString: toPaddedString,
    round:          round
  };
})());

function $R(start, end, exclusive) {
  return new ObjectRange(start, end, exclusive);
}

let ObjectRange = Class.Create(Enumerable, (function() {
  function initialize(start, end, exclusive) {
    this.start = start;
    this.end = end;
    this.exclusive = exclusive;
  }

  function _each(iterator, context) {
    let value = this.start;
    let i = 0;
    while (this.include(value)) {
      iterator.call(context, value, i);
      value = value.succ();
      i++;
    }
  }

  function include(value) {
    if (value < this.start)
      return false;
    if (this.exclusive)
      return value < this.end;
    return value <= this.end;
  }

  return {
    initialize: initialize,
    _each:      _each,
    include:    include
  };
})());



let Abstract = { };


let Try = {
  these: function() {
    let returnValue;

    for (let i = 0, length = arguments.length; i < length; i++) {
      let lambda = arguments[i];
      try {
        returnValue = lambda();
        break;
      } catch (e) { }
    }

    return returnValue;
  }
};

let Ajax = {
  getTransport: function() {
    return Try.these(
      function() {return new XMLHttpRequest()},
      function() {return new ActiveXObject('Msxml2.XMLHTTP')},
      function() {return new ActiveXObject('Microsoft.XMLHTTP')}
    ) || false;
  },

  activeRequestCount: 0
};

Ajax.Responders = {
  responders: [],

  _each: function(iterator, context) {
    this.responders._each(iterator, context);
  },

  register: function(responder) {
    if (!this.include(responder))
      this.responders.push(responder);
  },
  dispatch: function(callback, request, transport, json) {
    this.each(function(responder) {
      if (Object.isFunction(responder[callback])) {
        try {
          responder[callback].apply(responder, [request, transport, json]);
        } catch (e) { }
      }
    });
  }
};

function performRequest() {
  let response = new Ajax.Response(this);
  if (this.options.onCreate) {
    this.options.onCreate(response);
  }
  Ajax.Responders.dispatch('onCreate', this, response);

  this.transport.open(this.method.toUpperCase(), this.url, this.options.asynchronous);

  this.transport.onreadystatechange = this.onStateChange.bind(this);

  if (this.options.asynchronous) {
    this.respondToReadyState.bind(this).defer(1);
  } else {
    this.onStateChange();
  }

  this.setRequestHeaders();

  this.body = this.method === 'post' ? (this.options.postBody || params) : null;
  this.transport.send(this.body);
}

try {
  performRequest.call(this);
} catch (e) {
  this.dispatchException(e);
}

Object.extend(Ajax.Responders, Enumerable);

Ajax.Responders.register({
  onCreate:   function() { Ajax.activeRequestCount++ },
  onComplete: function() { Ajax.activeRequestCount-- }
});
Ajax.Base = Class.Create({
  initialize: function(options) {
    this.options = {
      method:       'post',
      asynchronous: true,
      contentType:  'application/x-www-form-urlencoded',
      encoding:     'UTF-8',
      parameters:   '',
      evalJSON:     true,
      evalJS:       true
    };
    Object.extend(this.options, options || { });

    this.options.method = this.options.method.toLowerCase();

    if (Object.isHash(this.options.parameters))
      this.options.parameters = this.options.parameters.toObject();
  }
});
Ajax.Request = Class.Create(Ajax.Base, {
  _complete: false,

  initialize: function($super, url, options) {
    $super(options);
    this.transport = Ajax.getTransport();
    this.request(url);
  },

  request: function(url) {
    this.url = url;
    this.method = this.options.method;
    let params = Object.isString(this.options.parameters)
        ? this.options.parameters
        : Object.toQueryString(this.options.parameters);

    if (!['get', 'post'].includes(this.method)) {
      params += (params ? '&' : '') + "_method=" + this.method;
      this.method = 'post';
    }

    if (params && this.method === 'get') {
      this.url += (this.url.includes('?') ? '&' : '?') + params;
    }

    this.parameters = params.toQueryParams();

    performRequest.call(this);},

  onStateChange: function() {
    let readyState = this.transport.readyState;
    if (readyState === 4 && !this._complete) {
      this.respondToReadyState(readyState);
    }
  },

  setRequestHeaders: function() {
    let headers = {
      'X-Requested-With': 'XMLHttpRequest',
      'X-Prototype-Version': Prototype.Version,
      'Accept': 'text/javascript, text/html, application/xml, text/xml, */*'
    };

    if (this.method === 'post') {
      headers['Content-type'] = this.options.contentType +
        (this.options.encoding ? '; charset=' + this.options.encoding : '');


      if (this.transport.overrideMimeType &&
          (navigator.userAgent.match(/Gecko\/(\d{4})/) || [0,2005])[1] < 2005)
            headers['Connection'] = 'close';
    }

    if (typeof this.options.requestHeaders == 'object') {
      let extras = this.options.requestHeaders;

      if (Object.isFunction(extras.push))
        for (let i = 0, length = extras.length; i < length; i += 2)
          headers[extras[i]] = extras[i+1];
      else
        $H(extras).each(function(pair) { headers[pair.key] = pair.value });
    }

    for (let name in headers)
      if (headers[name] != null)
        this.transport.setRequestHeader(name, headers[name]);
  },

  success: function() {
    let status = this.getStatus();
    return !status || (status >= 200 && status < 300) || status == 304;
  },

  getStatus: function() {
    try {
      if (this.transport.status === 1223) return 204;
      return this.transport.status || 0;
    } catch (e) { return 0 }
  },

  respondToReadyState: function(readyState) {
    let state = Ajax.Request.Events[readyState];
    let response = new Ajax.Response(this);

    if (state === 'Complete') {
      try {
        this._complete = true;
        let callback = this.options['on' + response.status] ||
            this.options['on' + (this.success() ? 'Success' : 'Failure')] ||
            Prototype.emptyFunction;
        callback(response, response.headerJSON);

        let contentType = response.getHeader('Content-type');
        if (this.options.evalJS == 'force' ||
            (this.options.evalJS && this.isSameOrigin() && contentType &&
                contentType.match(/^\s*(text|application)\/(x-)?(java|ecma)script(;[^;\s]*)?\s*$/i))) {
          this.evalResponse();
        }
      } catch (e) {
        this.dispatchException(e);
      }
    }

    try {
      let eventCallback = this.options['on' + state] || Prototype.emptyFunction;
      eventCallback(response, response.headerJSON);
      Ajax.Responders.dispatch('on' + state, this, response, response.headerJSON);
    } catch (e) {
      this.dispatchException(e);
    }

    if (state === 'Complete') {
      this.transport.onreadystatechange = Prototype.emptyFunction;
    }
  },
  isSameOrigin: function() {
    const m = this.url.match(/^\s*https?:\/\/[^]*/);
    return !m || (m[0] === '#{protocol}//#{domain}#{port}'.interpolate({
      protocol: location.protocol,
      domain: document.body,
      port: location.port ? ':' + location.port : ''
    }));
  },

  getHeader: function(name) {
    try {
      return this.transport.getResponseHeader(name) || null;
    } catch (e) { return null; }
  },

  evalResponse: function() {
    try {
      let fn = new Function("text", "return text");
      return fn((this.transport.responseText || '').unfilterJSON());
    } catch (e) {
      this.dispatchException(e);
    }
  },

  dispatchException: function(exception) {
    (this.options.onException || Prototype.emptyFunction)(this, exception);
    Ajax.Responders.dispatch('onException', this, exception);
  }
});

Ajax.Request.Events =
  ['Uninitialized', 'Loading', 'Loaded', 'Interactive', 'Complete'];








Ajax.Response = Class.Create({
  initialize: function(request){
    this.request = request;
    let transport  = this.transport  = request.transport,
        readyState = this.readyState = transport.readyState;

    if ((readyState > 2 && !Prototype.Browser.IE) || readyState == 4) {
      this.status       = this.getStatus();
      this.statusText   = this.getStatusText();
      this.responseText = String.interpret(transport.responseText);
      this.headerJSON   = this._getHeaderJSON();
    }

    if (readyState == 4) {
      let xml = transport.responseXML;
      this.responseXML  = Object.isUndefined(xml) ? null : xml;
    }
  },

  status:      0,

  statusText: '',

  getStatus: Ajax.Request.prototype.getStatus,

  getStatusText: function() {
    try {
      return this.transport.statusText || '';
    } catch (e) { return '' }
  },

  getHeader: Ajax.Request.prototype.getHeader,
  getResponseHeader: function(name) {
    return this.transport.getResponseHeader(name);
  },

  getAllResponseHeaders: function() {
    return this.transport.getAllResponseHeaders();
  },

  _getHeaderJSON: function() {
    let json = this.getHeader('X-JSON');
    if (!json) return null;

    try {
      json = decodeURIComponent(escape(json));
    } catch(e) {
    }

    try {
      return json.evalJSON(this.request.options.sanitizeJSON ||
        !this.request.isSameOrigin());
    } catch (e) {
      this.request.dispatchException(e);
    }
  }
});

Ajax.Updater = Class.Create(Ajax.Request, {
  initialize: function($super, container, url, options) {
    this.container = {
      success: (container.success || container),
      failure: (container.failure || (container.success ? null : container))
    };

    options = Object.clone(options);
    let onComplete = options.onComplete;
    options.onComplete = (function(response, json) {
      this.updateContent(response.responseText);
      if (Object.isFunction(onComplete)) onComplete(response, json);
    }).bind(this);

    $super(url, options);
  },

  updateContent: function(responseText) {
    let receiver = this.container[this.success() ? 'success' : 'failure'],
        options = this.options;

    if (!options.evalScripts) responseText = responseText.stripScripts();

    receiver = $(receiver);
    if (receiver) {
      if (options.insertion) {
        if (Object.isString(options.insertion)) {
          let insertion = { }; insertion[options.insertion] = responseText;
          receiver.insert(insertion);
        }
        else options.insertion(receiver, responseText);
      }
      else receiver.update(responseText);
    }
  }
});

Ajax.PeriodicalUpdater = Class.Create(Ajax.Base, {
  initialize: function($super, container, url, options) {
    $super(options);
    this.onComplete = this.options.onComplete;

    this.frequency = (this.options.frequency || 2);
    this.decay = (this.options.decay || 1);

    this.updater = { };
    this.container = container;
    this.url = url;

    this.start();
  },

  start: function() {
    this.options.onComplete = this.updateComplete.bind(this);
    this.onTimerEvent();
  },

  stop: function() {
    this.updater.options.onComplete = undefined;
    clearTimeout(this.timer);
    (this.onComplete || Prototype.emptyFunction).apply(this, arguments);
  },

  updateComplete: function(response) {
    if (this.options.decay) {
      this.decay = (response.responseText == this.lastText ?
        this.decay * this.options.decay : 1);

      this.lastText = response.responseText;
    }
    this.timer = this.onTimerEvent.bind(this).delay(this.decay * this.frequency);
  },

  onTimerEvent: function() {
    this.updater = new Ajax.Updater(this.container, this.url, this.options);
  }
});

(function(GLOBAL) {

  let UNDEFINED;
  let SLICE = Array.prototype.slice;

  let DIV = document.createElement('div');


  function $(element) {
    if (typeof element === 'string') {
      element = document.getElementById(element);
    }
    return element;
  }

  if (typeof GLOBAL !== 'undefined' && GLOBAL !== null) {
    GLOBAL.$ = $;
  }


  if (typeof GLOBAL !== 'undefined' && GLOBAL !== null && !GLOBAL.Node) {
    GLOBAL.Node = {};
  }

  if (typeof GLOBAL !== 'undefined' && GLOBAL !== null && GLOBAL.Node && !GLOBAL.Node.ELEMENT_NODE) {
    Object.extend(GLOBAL.Node, {
      ELEMENT_NODE:                1,
      ATTRIBUTE_NODE:              2,
      TEXT_NODE:                   3,
      CDATA_SECTION_NODE:          4,
      ENTITY_REFERENCE_NODE:       5,
      ENTITY_NODE:                 6,
      PROCESSING_INSTRUCTION_NODE: 7,
      COMMENT_NODE:                8,
      DOCUMENT_NODE:               9,
      DOCUMENT_TYPE_NODE:         10,
      DOCUMENT_FRAGMENT_NODE:     11,
      NOTATION_NODE:              12
    });
  }

  let ELEMENT_CACHE = {};

  function shouldUseCreationCache(tagName, attributes) {
    if (tagName === 'select') return false;
    return !('type' in attributes);

  }

  let HAS_EXTENDED_CREATE_ELEMENT_SYNTAX = (function(){
    try {
      let el = document.createElement('<input name="x">');
      return el.tagName.toLowerCase() === 'input' && el.name === 'x';
    }
    catch(err) {
      return false;
    }
  })();


  let oldElement = typeof GLOBAL !== 'undefined' && GLOBAL !== null ? GLOBAL.Element : undefined;
  function Element(tagName, attributes) {
    attributes = attributes || {};
    tagName = tagName.toLowerCase();

    if (HAS_EXTENDED_CREATE_ELEMENT_SYNTAX && attributes.name) {
      attributes = { ...attributes };
      delete attributes.name;
      return Element.writeAttribute(document.createElement(`<${tagName} name="${attributes.name}">`), attributes);
    }

    if (!ELEMENT_CACHE[tagName]) {
      ELEMENT_CACHE[tagName] = Element.extend(document.createElement(tagName));
    }

    let node = shouldUseCreationCache(tagName, attributes)
        ? ELEMENT_CACHE[tagName].cloneNode(false)
        : document.createElement(tagName);

    return Element.writeAttribute(node, attributes);
  }
  if (typeof GLOBAL !== 'undefined' && GLOBAL !== null) {
    GLOBAL.Element = Element;
  }

  if (typeof GLOBAL !== 'undefined' && GLOBAL !== null) {
    Object.extend(GLOBAL.Element, oldElement || {});
    if (oldElement) {
      GLOBAL.Element.prototype = oldElement.prototype;
    }
  }
  Element.Methods = { ByTag: {}, Simulated: {} };

  let methods = {};

  let INSPECT_ATTRIBUTES = { id: 'id', className: 'class' };
  function inspect(element) {
    element = $(element);
    let result = `<${element.tagName.toLowerCase()}`;

    for (let property in INSPECT_ATTRIBUTES) {
      let attribute = INSPECT_ATTRIBUTES[property];
      let value = (element[property] || '').toString();
      if (value) result += ` ${attribute}=${value.inspect(true)}`;
    }

    return result + '>';
  }

  methods.inspect = inspect;


  function visible(element) {
    return $(element).getStyle('display') !== 'none';
  }

  function toggle(element, bool) {
    element = $(element);
    if (typeof bool !== 'boolean')
      bool = !Element.visible(element);
    Element[bool ? 'show' : 'hide'](element);

    return element;
  }

  function hide(element) {
    element = $(element);
    element.style.display = 'none';
    return element;
  }

  function show(element) {
    element = $(element);
    element.style.display = '';
    return element;
  }


  Object.extend(methods, {
    visible: visible,
    toggle:  toggle,
    hide:    hide,
    show:    show
  });


  function remove(element) {
    element = $(element);
    element.parentNode.removeChild(element);
    return element;
  }

  let SELECT_ELEMENT_INNERHTML_BUGGY = (function(){
    let el = document.createElement("select"),
        isBuggy;
    el.innerHTML = "<option value=\"test\">test</option>";
    isBuggy = el?.options?.[0]?.nodeName?.toUpperCase() !== "OPTION";

    el = null;
    return isBuggy;
  })();

  let TABLE_ELEMENT_INNERHTML_BUGGY = (function() {
    try {
      let el = document.createElement("table");
      el.innerHTML = "<tbody><tr><td>test</td></tr></tbody>";
      let isBuggy = !el.tBodies[0];
      el = null;
      return isBuggy;
    } catch (e) {
      return true;
    }
  })();

  let LINK_ELEMENT_INNERHTML_BUGGY = (function() {
    try {
      let el = document.createElement('div');
      el.innerHTML = "<link />";
      return el.childNodes.length === 0;
    } catch(e) {
      return true;
    }
  })();

  let ANY_INNERHTML_BUGGY = SELECT_ELEMENT_INNERHTML_BUGGY ||
   TABLE_ELEMENT_INNERHTML_BUGGY || LINK_ELEMENT_INNERHTML_BUGGY;

  let SCRIPT_ELEMENT_REJECTS_TEXTNODE_APPENDING = (function () {
    let s = document.createElement("script"),
        isBuggy = false;
    try {
      s.appendChild(document.createTextNode(""));
      isBuggy = !s.firstChild ||
        s.firstChild && s.firstChild.nodeType !== 3;
    } catch (e) {
      isBuggy = true;
    }
    s = null;
    return isBuggy;
  })();

  function update(element, content) {
    element = $(element);

    let descendants = element.getElementsByTagName('*');
    for (let i = descendants.length - 1; i >= 0; i--) {
      purgeElement(descendants[i]);
    }

    content = content?.toElement?.();

    if (Object.isElement(content)) {
      return element.update().insert(content);
    }

    content = Object.toHTML(content);
    let tagName = element.tagName.toUpperCase();

    if (tagName === 'SCRIPT' && SCRIPT_ELEMENT_REJECTS_TEXTNODE_APPENDING) {
      element.text = content;
      return element;
    }

    if (ANY_INNERHTML_BUGGY) {
      if (tagName in INSERTION_TRANSLATIONS.tags) {
        element.innerHTML = '';
        let nodes = getContentFromAnonymousElement(tagName, content.stripScripts());
        for (let i = 0; i < nodes.length; i++) {
          element.appendChild(nodes[i]);
        }
      } else if (LINK_ELEMENT_INNERHTML_BUGGY && Object.isString(content) && content.indexOf('<link') > -1) {
        element.innerHTML = '';
        let nodes = getContentFromAnonymousElement(tagName, content.stripScripts(), true);
        for (let i = 0; i < nodes.length; i++) {
          let node = nodes[i];
          if (node) {
            element.appendChild(node);
          }
        }
      } else {
        element.innerHTML = content.stripScripts();
      }
    } else {
      element.innerHTML = content.stripScripts();
    }

    content.evalScripts.bind(content).defer();
    return element;
  }
  function replace(element, content) {
    element = $(element);

    content = content?.toElement?.() || (Object.isElement(content) ? content : (() => {
      let contentHTML = Object.toHTML(content);
      let range = element.ownerDocument.createRange();
      range.selectNode(element);
      contentHTML.evalScripts?.bind(contentHTML)?.defer?.();
      return range.createContextualFragment(contentHTML.stripScripts());
    })());

    element.replaceWith(content);
    return element;
  }
  let INSERTION_TRANSLATIONS = {
    before: function(element, node) {
      element.parentNode.insertBefore(node, element);
    },
    top: function(element, node) {
      element.insertBefore(node, element.firstChild);
    },
    bottom: function(element, node) {
      element.appendChild(node);
    },
    after: function(element, node) {
      element.parentNode.insertBefore(node, element.nextSibling);
    },

    tags: {
      TABLE:  ['<table>',                '</table>',                   1],
      TBODY:  ['<table><tbody>',         '</tbody></table>',           2],
      TR:     ['<table><tbody><tr>',     '</tr></tbody></table>',      3],
      TD:     ['<table><tbody><tr><td>', '</td></tr></tbody></table>', 4],
      SELECT: ['<select>',               '</select>',                  1]
    }
  };

  let tags = INSERTION_TRANSLATIONS.tags;

  Object.extend(tags, {
    THEAD: tags.TBODY,
    TFOOT: tags.TBODY,
    TH:    tags.TD
  });

  function replace_IE(element, content) {
    element = $(element);
    content = content?.toElement?.();

    if (Object.isElement(content)) {
      element.parentNode.replaceChild(content, element);
      return element;
    }


    content = Object.toHTML(content);
    let parent = element.parentNode, tagName = parent.tagName.toUpperCase();

    if (tagName in INSERTION_TRANSLATIONS.tags) {
      let nextSibling = Element.next(element);
      let fragments = getContentFromAnonymousElement(
       tagName, content.stripScripts());

      parent.removeChild(element);

      let iterator;
      if (nextSibling)
        iterator = function(node) { parent.insertBefore(node, nextSibling) };
      else
        iterator = function(node) { parent.appendChild(node); }

      fragments.each(iterator);
    } else {
      element.outerHTML = content.stripScripts();
    }

    content.evalScripts.bind(content).defer();
    return element;
  }

  if ('outerHTML' in document.documentElement)
    replace = replace_IE;

  function isContent(content) {
    if (Object.isString(content) || Object.isNumber(content) || Object.isElement(content)) {
      return true;
    }

    if (content && (content.toElement || content.toHTML)) {
      return true;
    }

    return false;
  }

  function insertContentAt(element, content, position) {
    position = position.toLowerCase();
    let method = INSERTION_TRANSLATIONS[position];

    content = content?.toElement?.() || content;

    if (Object.isElement(content)) {
      element.parentNode.replaceChild(content, element);
      return element;
    }

    content = Object.toHTML(content);
    let tagName = ((position === 'before' || position === 'after') ?
        element.parentNode : element).tagName.toUpperCase();

    let childNodes = getContentFromAnonymousElement(tagName, content.stripScripts());

    if (position === 'top' || position === 'after') {
      childNodes.reverse();
    }

    for (let i = 0; i < childNodes.length; i++) {
      let node = childNodes[i];
      method(element, node);
    }

    content.evalScripts?.bind(content)?.defer?.();
  }
  function insert(element, insertions) {
    element = $(element);

    if (isContent(insertions)) {
      insertions = { bottom: insertions };
    }

    Object.entries(insertions).forEach(([position, content]) => {
      insertContentAt(element, content, position);
    });

    return element;
  }
  function wrap(element, wrapper, attributes) {
    element = $(element);

    if (Object.isElement(wrapper)) {
      $(wrapper).writeAttribute(attributes || {});
    } else if (Object.isString(wrapper)) {
      wrapper = new Element(wrapper, attributes);
    } else {
      wrapper = new Element('div', wrapper);
    }

    element.replaceWith(wrapper, element);

    wrapper.appendChild(element);

    return wrapper;
  }

  function cleanWhitespace(element) {
    element = $(element);

    Array.from(element.childNodes).forEach(node => {
      if (node.nodeType === Node.TEXT_NODE && !/\S/.test(node.nodeValue)) {
        element.removeChild(node);
      }
    });

    return element;
  }
  function empty(element) {
    return $(element).innerHTML.blank();
  }

  function getContentFromAnonymousElement(tagName, html, force) {
    let t = INSERTION_TRANSLATIONS.tags[tagName], div = DIV;

    let workaround = !!t;
    if (!workaround && force) {
      workaround = true;
      t = ['', '', 0];
    }

    if (workaround) {
      div.innerHTML = '&#160;' + t[0] + html + t[1];
      for (let i = t[2]; i--; ) {
        div = div.firstChild;
      }
    } else {
      div.innerHTML = html;
    }

    return Array.from(div.childNodes);
  }

  function clone(element, deep) {
    element = $(element);
    if (!element) return;

    let clone = element.cloneNode(deep);

    if (!HAS_UNIQUE_ID_PROPERTY && deep) {
      let descendants = Element.select(clone, '*');
      descendants.forEach(descendant => {
        descendant._prototypeUID = UNDEFINED;
      });
    }

    return Element.extend(clone);
  }

  function purgeElement(element) {
    let uid = getUniqueElementID(element);
    if (uid) {
      Element.stopObserving(element);
      delete Element.Storage[uid];
    }
  }
  Object.extend(methods, {
    remove:  remove,
    update:  update,
    replace: replace,
    insert:  insert,
    wrap:    wrap,
    cleanWhitespace: cleanWhitespace,
    empty:   empty,
    clone:   clone
  });



  function recursivelyCollect(element, property, maximumLength) {
    element = $(element);
    maximumLength = maximumLength || -1;
    let elements = [];

    while (elements.length !== maximumLength) {
      element = element[property];

      if (element.nodeType === Node.ELEMENT_NODE) {
        elements.push(Element.extend(element));
      }
    }

    return elements;
  }

  function firstDescendant(element) {
    element = $(element).firstChild;
    while (element && element.nodeType !== Node.ELEMENT_NODE)
      element = element.nextSibling;

    return $(element);
  }

  function immediateDescendants(element) {
    let results = [], child = $(element).firstChild;

    while (child) {
      if (child.nodeType === Node.ELEMENT_NODE)
        results.push(Element.extend(child));

      child = child.nextSibling;
    }

    return results;
  }

  function previousSiblings(element) {
    return recursivelyCollect(element, 'previousSibling');
  }

  function nextSiblings(element) {
    return recursivelyCollect(element, 'nextSibling');
  }

  previous.toReversed = function () {

    let reversed = [];


    return undefined;
  };

  function siblings(element) {
    element = $(element);
    let previous = previousSiblings(element),
        next = nextSiblings(element);

    // Use toReversed directly
    let reversedPrevious = previous.toReversed();

    return reversedPrevious.concat(next);
  }


  function match(element, selector) {
    element = $(element);

    if (Object.isString(selector))
      return Prototype.Selector.match(element, selector);

    return selector.match(element);
  }


  function _recursivelyFind(element, property, expression, index) {
    element = $(element);
    expression = expression || 0;
    index = index || 0;

    if (Object.isNumber(expression)) {
      index = expression;
      expression = null;
    }

    while (element.nodeType === Node.ELEMENT_NODE) {
      element = element[property];

      if (element.nodeType === 1) {
        if (expression && !Prototype.Selector.match(element, expression)) {
          continue;
        }

        if (--index < 0) {
          return Element.extend(element);
        }
      }
    }
  }


  function up(element, expression, index) {
    element = $(element);

    if (!expression && arguments.length === 1) {
      return $(element.parentNode);
    }

    return _recursivelyFind(element, 'parentNode', expression, index);
  }

  function down(element, expression, index) {
    if (arguments.length === 1) return firstDescendant(element);
    element = $(element)
    expression = expression || 0
    index = index || 0;

    if (Object.isNumber(expression)){
      index = expression
      expression = '*';
    }


    let node = Prototype.Selector.select(expression, element)[index];
    return Element.extend(node);
  }

  function previous(element, expression, index) {
    return _recursivelyFind(element, 'previousSibling', expression, index);
  }

  function next(element, expression, index) {
    return _recursivelyFind(element, 'nextSibling', expression, index);
  }

  function select(element) {
    element = $(element);
    let expressions = SLICE.call(arguments, 1).join(', ');
    return Prototype.Selector.select(expressions, element);
  }
  function descendantOf_DOM(element, ancestor) {
    element = $(element);
    ancestor = $(ancestor);

    if (!element || !ancestor) return false;

    while (element.parentNode !== ancestor) {
      if (element === ancestor) return true;
    }

    return false;
  }

  function descendantOf_contains(element, ancestor) {
    element = $(element);
    ancestor = $(ancestor);

    if (!element || !ancestor) return false;

    if (!ancestor.contains) {
      return descendantOf_DOM(element, ancestor);
    }

    return ancestor.contains(element) && ancestor !== element;
  }

  function descendantOf_compareDocumentPosition(element, ancestor) {
    element = $(element)
    ancestor = $(ancestor);
    if (!element || !ancestor) return false;
    return (element.compareDocumentPosition(ancestor) & 8) === 8;
  }

  let descendantOf;
  if (DIV.compareDocumentPosition) {
    descendantOf = descendantOf_compareDocumentPosition;
  } else if (DIV.contains) {
    descendantOf = descendantOf_contains;
  } else {
    descendantOf = descendantOf_DOM;
  }


  Object.extend(methods, {
    siblings:             siblings,
    match:                match,
    up:                   up,
    down:                 down,
    next:                 next,
    select:               select,
    descendantOf:         descendantOf,

    getElementsBySelector: select,

    childElements:         immediateDescendants
  });


  let idCounter = 1;
  function identify(element) {
    element = $(element);
    let id = Element.readAttribute(element, 'id');
    if (id) return id;

    let idCounter = 1;
    while (true) {
      id = 'anonymous_element_' + idCounter;
      if (!$(id)) break;
      idCounter++;
    }

    Element.writeAttribute(element, 'id', id);
    return id;
  }


  function readAttribute(element, name) {
    return $(element).getAttribute(name);
  }

  function readAttribute_IE(element, name) {
    element = $(element);

    let table = ATTRIBUTE_TRANSLATIONS.read;

    if (table.values[name]) {
      return table.values[name](element, name);
    }

    name = table.names[name] || name;

    if (name.includes(':')) {
      return element?.attributes?.[name]?.value || null;
    }

    return element.getAttribute(name);
  }
  function readAttribute_Opera(element, name) {
    if (name === 'title') return element.title;
    return element.getAttribute(name);
  }

  let PROBLEMATIC_ATTRIBUTE_READING = (function() {
    DIV.setAttribute('onclick', []);
    let value = DIV.getAttribute('onclick');
    let isFunction = Object.isArray(value);
    DIV.removeAttribute('onclick');
    return isFunction;
  })();

  if (PROBLEMATIC_ATTRIBUTE_READING) {
    readAttribute = readAttribute_IE;
  } else if (Prototype.Browser.Opera) {
    readAttribute = readAttribute_Opera;
  }


  function writeAttribute(element, name, value) {
    element = $(element);
    let attributes = {}, table = ATTRIBUTE_TRANSLATIONS.write;

    if (typeof name === 'object') {
      attributes = name;
    } else {
      attributes[name] = Object.isUndefined(value) ? true : value;
    }

    for (let attr in attributes) {
      name = table.names[attr] || attr;
      value = attributes[attr];
      if (table.values[attr]) {
        value = table.values[attr](element, value);
        if (Object.isUndefined(value)) continue;
      }
      if (!value) {
        element.removeAttribute(name);
      } else {
        element.setAttribute(name, value);
      }
    }

    return element;
  }
  let PROBLEMATIC_HAS_ATTRIBUTE_WITH_CHECKBOXES = (function () {
    if (!HAS_EXTENDED_CREATE_ELEMENT_SYNTAX) {
      return false;
    }
    let checkbox = document.createElement('<input type="checkbox">');
    checkbox.checked = true;
    let node = checkbox.getAttributeNode('checked');
    return !node?.specified;
  })();

  function hasAttribute(element, attribute) {
    attribute = ATTRIBUTE_TRANSLATIONS.has[attribute] || attribute;
    let node = $(element).getAttributeNode(attribute);
    return !!node?.specified;
  }

  function hasAttribute_IE(element, attribute) {
    if (attribute === 'checked') {
      return element.checked;
    }
    return element.hasAttribute(attribute);
  }

  if (typeof GLOBAL !== 'undefined' && GLOBAL !== null && GLOBAL.Element && GLOBAL.Element.Methods && GLOBAL.Element.Methods.Simulated) {
    GLOBAL.Element.Methods.Simulated.hasAttribute = PROBLEMATIC_HAS_ATTRIBUTE_WITH_CHECKBOXES ? hasAttribute_IE : hasAttribute;
  }

  function classNames(element) {
    return new Element.ClassNames(element);
  }

  let regExpCache = {};
  function getRegExpForClassName(className) {
    if (regExpCache[className]) return regExpCache[className];

    let re = new RegExp("(^|\\s+)" + className + "(\\s+|$)");
    regExpCache[className] = re;
    return re;
  }

  function hasClassName(element, className) {
    element = $(element);
    if (!element) {
      return false;
    }
    let elementClassName = element.className;

    return elementClassName.includes(className);
  }

  function addClassName(element, className) {
    element = $(element);
    if (!element) {
      return;
    }

    if (!hasClassName(element, className)) {
      element.classList.add(className);
    }

    return element;
  }

  function removeClassName(element, className) {
    element = $(element);
    if (!element) {return;}

    element.className = element.className.replace(
     getRegExpForClassName(className), ' ').strip();

    return element;
  }
  let ATTRIBUTE_TRANSLATIONS = {};

  let classProp = 'className', forProp = 'for';

  DIV.setAttribute(classProp, 'x');
  if (DIV.className !== 'x') {
    DIV.setAttribute('class', 'x');
    if (DIV.className === 'x')
      classProp = 'class';
  }

  let LABEL = document.createElement('label');
  LABEL.setAttribute(forProp, 'x');
  if (LABEL.htmlFor !== 'x') {
    LABEL.setAttribute('htmlFor', 'x');
    if (LABEL.htmlFor === 'x')
      forProp = 'htmlFor';
  }
  LABEL = null;

  function _getAttr(element, attribute) {
    return element.getAttribute(attribute);
  }

  function _getAttr2(element, attribute) {
    return element.getAttribute(attribute, 2);
  }

  function _getAttrNode(element, attribute) {
    let node = element.getAttributeNode(attribute);
    return node ? node.value : '';
  }

  function _getFlag(element, attribute) {
    return $(element).hasAttribute(attribute) ? attribute : null;
  }

  DIV.onclick = Prototype.emptyFunction;
  let onclickValue = DIV.getAttribute('onclick');

  let _getEv;

  if (String(onclickValue).indexOf('{') > -1) {
    _getEv = function(element, attribute) {
      let value = element.getAttribute(attribute);
      if (!value) return null;
      value = value.toString();
      value = value.split('{')[1];
      value = value.split('}')[0];
      return value.strip();
    };
  }
  else if (onclickValue === '') {
    _getEv = function(element, attribute) {
      let value = element.getAttribute(attribute);
      if (!value) return null;
      return value.strip();
    };
  }

  ATTRIBUTE_TRANSLATIONS.read = {
    names: {
      'class':     classProp,
      'className': classProp,
      'for':       forProp,
      'htmlFor':   forProp
    },

    values: {
      style: function(element) {
        return element.style.cssText.toLowerCase();
      },
      title: function(element) {
        return element.title;
      }
    }
  };

  ATTRIBUTE_TRANSLATIONS.write = {
    names: {
      className:   'class',
      htmlFor:     'for',
      cellpadding: 'cellPadding',
      cellspacing: 'cellSpacing'
    },

    values: {
      checked: function(element, value) {
        value = !!value;
        element.checked = value;
        return value ? 'checked' : null;
      },

      style: function(element, value) {
        element.style.cssText = value ? value : '';
      }
    }
  };

  ATTRIBUTE_TRANSLATIONS.has = { names: {} };

  Object.extend(ATTRIBUTE_TRANSLATIONS.write.names,
   ATTRIBUTE_TRANSLATIONS.read.names);

  let CAMEL_CASED_ATTRIBUTE_NAMES = $w('colSpan rowSpan vAlign dateTime ' +
   'accessKey tabIndex encType maxLength readOnly longDesc frameBorder');


  let i;
  let attr = CAMEL_CASED_ATTRIBUTE_NAMES[i];
  for (i = 0; i < CAMEL_CASED_ATTRIBUTE_NAMES.length; i++) {
    ATTRIBUTE_TRANSLATIONS.write.names[attr.toLowerCase()] = attr;
    ATTRIBUTE_TRANSLATIONS.has.names[attr.toLowerCase()]   = attr;
  }


  Object.extend(ATTRIBUTE_TRANSLATIONS.read.values, {
    href:        _getAttr2,
    src:         _getAttr2,
    type:        _getAttr,
    action:      _getAttrNode,
    disabled:    _getFlag,
    checked:     _getFlag,
    readonly:    _getFlag,
    multiple:    _getFlag,
    onload:      _getEv,
    onunload:    _getEv,
    onclick:     _getEv,
    ondblclick:  _getEv,
    onmousedown: _getEv,
    onmouseup:   _getEv,
    onmouseover: _getEv,
    onmousemove: _getEv,
    onmouseout:  _getEv,
    onfocus:     _getEv,
    onblur:      _getEv,
    onkeypress:  _getEv,
    onkeydown:   _getEv,
    onkeyup:     _getEv,
    onsubmit:    _getEv,
    onreset:     _getEv,
    onselect:    _getEv,
    onchange:    _getEv
  });


  Object.extend(methods, {
    identify:        identify,
    readAttribute:   readAttribute,
    writeAttribute:  writeAttribute,
    classNames:      classNames,
    hasClassName:    hasClassName,
    addClassName:    addClassName,
    removeClassName: removeClassName
  });


  function normalizeStyleName(style) {
    if (style === 'float' || style === 'styleFloat') {
      return 'cssFloat';
    }
    return style.replace(/-./g, match => match.charAt(1).toUpperCase());
  }

  function normalizeStyleName_IE(style) {
    if (style === 'float' || style === 'cssFloat') {
      return 'styleFloat';
    }
    return style.replace(/-./g, match => match.charAt(1).toUpperCase());
  }
  function setStyle(element, styles) {
    element = $(element);
    let elementStyle = element.style;

    if (Object.isString(styles)) {
      elementStyle.cssText += ';' + styles;
      if (styles.includes('opacity')) {
        let opacity = styles.match(/opacity:\s*(\d?\.?\d*)/)[1];
        Element.setOpacity(element, opacity);
      }
      return element;
    }

    for (let property in styles) {
      if (property === 'opacity') {
        Element.setOpacity(element, styles[property]);
      } else if (property === 'float' || property === 'cssFloat') {
        elementStyle.styleFloat = styles[property];
      } else {
        elementStyle[property] = styles[property];
      }
    }

    return element;
  }

  function getStyle(element, style) {
    element = $(element);
    style = normalizeStyleName(style);

    let value = element.style[style] || getComputedStyle(element)[style];

    if (style === 'opacity') {
      return value ? parseFloat(value) : 1.0;
    }

    return value === 'auto' ? null : value;
  }
  function getStyle_IE(element, style) {
    element = $(element);
    style = normalizeStyleName_IE(style);

    let value = element.style[style];
    if (!value && element.currentStyle) {
      value = element.currentStyle[style];
    }

    if (style === 'opacity') {
      if (!STANDARD_CSS_OPACITY_SUPPORTED)
        return getOpacity_IE(element);
      else return value ? parseFloat(value) : 1.0;
    }

    if (value === 'auto') {
      if ((style === 'width' || style === 'height') && Element.visible(element))
        return Element.measure(element, style) + 'px';
      return null;
    }

    return value;
  }

  function stripAlphaFromFilter_IE(filter) {
    return (filter || '').replace(/alpha\([^]*\)/gi, '');
  }

  function hasLayout_IE(element) {
    element?.currentStyle?.hasLayout || (element.style.zoom = 1);
    return element;

  }

  let STANDARD_CSS_OPACITY_SUPPORTED = (function() {
    DIV.style.cssText = "opacity:.55";
    return /^0.55/.test(DIV.style.opacity);
  })();

  function setOpacity(element, value) {
    element = $(element);
    if (value == 1 || value === '') value = '';
    else if (value < 0.00001) value = 0;
    element.style.opacity = value;
    return element;
  }

  function setOpacity_IE(element, value) {
    if (STANDARD_CSS_OPACITY_SUPPORTED)
      return setOpacity(element, value);

    element = hasLayout_IE($(element));
    let filter = Element.getStyle(element, 'filter'),
     style = element.style;

    if (value == 1 || value === '') {
      filter = stripAlphaFromFilter_IE(filter);
      if (filter) style.filter = filter;
      else style.removeAttribute('filter');
      return element;
    }

    if (value < 0.00001) value = 0;

    style.filter = stripAlphaFromFilter_IE(filter) +
     ' alpha(opacity=' + (value * 100) + ')';

    return element;
  }


  function getOpacity(element) {
    return Element.getStyle(element, 'opacity');
  }

  function getOpacity_IE(element) {
    if (STANDARD_CSS_OPACITY_SUPPORTED)
      return getOpacity(element);

    let filter = Element.getStyle(element, 'filter');
    if (filter.length === 0) return 1.0;
    let match = (filter || '').match(/alpha\(opacity=(.*)\)/i);
    return match?.[1] ? parseFloat(match[1]) / 100 : 1.0;
  }


  Object.extend(methods, {
    setStyle:   setStyle,
    getStyle:   getStyle,
    setOpacity: setOpacity,
    getOpacity: getOpacity
  });

  if ('styleFloat' in DIV.style) {
    methods.getStyle = getStyle_IE;
    methods.setOpacity = setOpacity_IE;
    methods.getOpacity = getOpacity_IE;
  }

  if (typeof GLOBAL !== 'undefined' && GLOBAL !== null) {
    GLOBAL.Element.Storage = { UID: 1 };
  }

  function getUniqueElementID(element) {
    if (element === window) return 0;

    if (typeof element._prototypeUID === 'undefined')
      element._prototypeUID = Element.Storage.UID++;
    return element._prototypeUID;
  }

  function getUniqueElementID_IE(element) {
    if (element === window) return 0;
    if (element == document) return 1;
    return element.uniqueID;
  }

  let HAS_UNIQUE_ID_PROPERTY = ('uniqueID' in DIV);
  if (HAS_UNIQUE_ID_PROPERTY)
    getUniqueElementID = getUniqueElementID_IE;

  function getStorage(element) {
    element = $(element);
    if (!element) {return;}
    let uid = getUniqueElementID(element);

    if (!Element.Storage[uid])
      Element.Storage[uid] = $H();

    return Element.Storage[uid];
  }

  function store(element, key, value) {
    element = $(element);
    if (!element) {return;}
    let storage = getStorage(element);
    if (arguments.length === 2) {
      storage.update(key);
    } else {
      storage.set(key, value);
    }
    return element;
  }

  function retrieve(element, key, defaultValue) {
    element = $(element);
    if (!element) {return;}

    let storage = getStorage(element), value = storage.get(key);

    if (Object.isUndefined(value)) {
      storage.set(key, defaultValue);
      value = defaultValue;
    }

    return value;
  }


  Object.extend(methods, {
    getStorage: getStorage,
    store:      store,
    retrieve:   retrieve
  });


  let Methods = {}, ByTag = Element.Methods.ByTag,
   F = Prototype.BrowserFeatures;

  if (!F.ElementExtensions && ('__proto__' in DIV)) {
    if (typeof GLOBAL !== 'undefined' && GLOBAL !== null) {
      GLOBAL.HTMLElement = {};
    }
    if (typeof GLOBAL !== 'undefined' && GLOBAL !== null) {
      GLOBAL.HTMLElement.prototype = DIV['__proto__'];
    }
    F.ElementExtensions = true;
  }

  function checkElementPrototypeDeficiency(tagName) {
    if (typeof window.Element === 'undefined') return false;
    if (!HAS_EXTENDED_CREATE_ELEMENT_SYNTAX) return false;
    let proto = window.Element.prototype;
    if (proto) {
      let id = '_' + (Math.random() + '').slice(2),
       el = document.createElement(tagName);
      proto[id] = 'x';
      let isBuggy = (el[id] !== 'x');
      delete proto[id];
      el = null;
      return isBuggy;
    }

    return false;
  }

  let HTMLOBJECTELEMENT_PROTOTYPE_BUGGY =
   checkElementPrototypeDeficiency('object');

  function extendElementWith(element, methods) {
    for (let property in methods) {
      let value = methods[property];
      if (Object.isFunction(value) && !(property in element))
        element[property] = value.methodize();
    }
  }

  let EXTENDED = {};
  function elementIsExtended(element) {
    let uid = getUniqueElementID(element);
    return (uid in EXTENDED);
  }

  function extend(element) {
    if (!element || elementIsExtended(element)) return element;
    if (element.nodeType !== Node.ELEMENT_NODE || element == window)
      return element;

    let methods = Object.clone(Methods),
     tagName = element.tagName.toUpperCase();

    if (ByTag[tagName]) Object.extend(methods, ByTag[tagName]);

    extendElementWith(element, methods);
    EXTENDED[getUniqueElementID(element)] = true;
    return element;
  }

  function extend_IE8(element) {
    if (!element || elementIsExtended(element)) return element;

    let t = element.tagName;
    if (t && (/^(?:object|applet|embed)$/i.test(t))) {
      extendElementWith(element, Element.Methods);
      extendElementWith(element, Element.Methods.Simulated);
      extendElementWith(element, Element.Methods.ByTag[t.toUpperCase()]);
    }

    return element;
  }

  if (F.SpecificElementExtensions) {
    extend = HTMLOBJECTELEMENT_PROTOTYPE_BUGGY ? extend_IE8 : Prototype.K;
  }

  function addMethodsToTagName(tagName, methods) {
    tagName = tagName.toUpperCase();
    if (!ByTag[tagName]) ByTag[tagName] = {};
    Object.extend(ByTag[tagName], methods);
  }

  function mergeMethods(destination, methods, onlyIfAbsent = false) {
    for (let property in methods) {
      let value = methods[property];
      if (Object.isFunction(value) && (!onlyIfAbsent || !(property in destination))) {
        destination[property] = value.methodize();
      }
    }
  }
  function findDOMClass(tagName) {
    let klass;
    let trans = {
      "OPTGROUP": "OptGroup", "TEXTAREA": "TextArea", "P": "Paragraph",
      "FIELDSET": "FieldSet", "UL": "UList", "OL": "OList", "DL": "DList",
      "DIR": "Directory", "H1": "Heading", "H2": "Heading", "H3": "Heading",
      "H4": "Heading", "H5": "Heading", "H6": "Heading", "Q": "Quote",
      "INS": "Mod", "DEL": "Mod", "A": "Anchor", "IMG": "Image", "CAPTION":
      "TableCaption", "COL": "TableCol", "COLGROUP": "TableCol", "THEAD":
      "TableSection", "TFOOT": "TableSection", "TBODY": "TableSection", "TR":
      "TableRow", "TH": "TableCell", "TD": "TableCell", "FRAMESET":
      "FrameSet", "IFRAME": "IFrame"
    };
    if (trans[tagName]) klass = 'HTML' + trans[tagName] + 'Element';
    if (window[klass]) return window[klass];
    klass = 'HTML' + tagName + 'Element';
    if (window[klass]) return window[klass];
    klass = 'HTML' + tagName.capitalize() + 'Element';
    if (window[klass]) return window[klass];

    let element = document.createElement(tagName),
     proto = element['__proto__'] || element.constructor.prototype;

    element = null;
    return proto;
  }

  function addMethods(methods) {
    if (arguments.length === 0) addFormMethods();

    if (arguments.length === 2) {
      methods = arguments[1];
    }

    if (!tagName) {
      Object.extend(Element.Methods, methods || {});
    } else if (Object.isArray(tagName)) {
      let tag = tagName[i];
      for (let i = 0;i < tagName.length ; i++) {
        addMethodsToTagName(tag, methods);
      }
    } else {
      addMethodsToTagName(tagName, methods);
    }


    let ELEMENT_PROTOTYPE = window.HTMLElement ? HTMLElement.prototype :
     Element.prototype;

    if (F.ElementExtensions) {
      mergeMethods(ELEMENT_PROTOTYPE, Element.Methods);
      mergeMethods(ELEMENT_PROTOTYPE, Element.Methods.Simulated, true);
    }

    if (F.SpecificElementExtensions) {
      for (let tag in Element.Methods.ByTag) {
        let klass = findDOMClass(tag);
        if (Object.isUndefined(klass)) continue;
        mergeMethods(klass.prototype, ByTag[tag]);
      }
    }

    Object.extend(Element, Element.Methods);
    Object.extend(Element, Element.Methods.Simulated);
    delete Element.ByTag;
    delete Element.Simulated;

    Element.extend.refresh();

    ELEMENT_CACHE = {};
  }

  if (typeof GLOBAL !== 'undefined' && GLOBAL !== null && GLOBAL.Element) {
    Object.extend(GLOBAL.Element, {
      extend: extend,
      addMethods: addMethods
    });
  }

  if (extend === Prototype.K) {
    if (typeof GLOBAL !== 'undefined' && GLOBAL.Element && GLOBAL.Element.extend) {
      GLOBAL.Element.extend.refresh = Prototype.emptyFunction;
    }
  } else if (typeof GLOBAL !== 'undefined' && GLOBAL.Element && GLOBAL.Element.extend) {
    GLOBAL.Element.extend.refresh = function() {
      if (Prototype.BrowserFeatures.ElementExtensions) return;
      Object.extend(Methods, Element.Methods);
      Object.extend(Methods, Element.Methods.Simulated);

      EXTENDED = {};
    };
  }


  function addFormMethods() {
    Object.extend(Form, Form.Methods);
    Object.extend(Form.Element, Form.Element.Methods);
    Object.extend(Element.Methods.ByTag, {
      "FORM":     Object.clone(Form.Methods),
      "INPUT":    Object.clone(Form.Element.Methods),
      "SELECT":   Object.clone(Form.Element.Methods),
      "TEXTAREA": Object.clone(Form.Element.Methods),
      "BUTTON":   Object.clone(Form.Element.Methods)
    });
  }

  Element.addMethods(methods);

  function destroyCache_IE() {
    DIV = null;
    ELEMENT_CACHE = null;
  }

  if (window.attachEvent)
    window.attachEvent('onunload', destroyCache_IE);

})(this);
(function() {

  function toDecimal(pctString) {
    let match = pctString.match(/^(\d+)%?$/i);
    if (!match) return null;
    return (Number(match[1]) / 100);
  }

  function getRawStyle(element, style) {
    element = $(element);

    let value = element.style[style];
    if (!value || value === 'auto') {
      let css = document.defaultView.getComputedStyle(element, null);
      value = css ? css[style] : null;
    }

    if (style === 'opacity') return value ? parseFloat(value) : 1.0;
    return value === 'auto' ? null : value;
  }

  function getRawStyle_IE(element, style) {
    let value = element.style[style];
    if (!value && element.currentStyle) {
      value = element.currentStyle[style];
    }
    return value;
  }

  function getContentWidth(element, context) {
    let boxWidth = element.offsetWidth;

    let bl = getPixelValue(element, 'borderLeftWidth',  context) || 0;
    let br = getPixelValue(element, 'borderRightWidth', context) || 0;
    let pl = getPixelValue(element, 'paddingLeft',      context) || 0;
    let pr = getPixelValue(element, 'paddingRight',     context) || 0;

    return boxWidth - bl - br - pl - pr;
  }

  if (!Object.isUndefined(document.documentElement.currentStyle) && !Prototype.Browser.Opera) {
    getRawStyle = getRawStyle_IE;
  }


  function getPixelValue(value, property, context) {
    if (Object.isElement(value)) {
      value = getRawStyle(value, property);
    }

    if (value === null || Object.isUndefined(value)) {
      return null;
    }

    if ((/^?\d+(\.\d+)?(px)?$/i).test(value)) {
      return parseFloat(value);
    }

    if (/\d/.test(value)) {
      if (context && context !== document.viewport && value.includes('%')) {
        let decimal = toDecimal(value);
        let whole = null;

        let isHorizontal = property.includes('left') || property.includes('right') ||
            property.includes('width');

        let isVertical = property.includes('top') || property.includes('bottom') ||
            property.includes('height');

        if (isHorizontal ) {
          whole = context.getWidth();
        } else if (isVertical) {
          whole = context.getHeight();
        }

        return (whole === null) ? 0 : whole * decimal;
      } else {
        return parseFloat(value);
      }
    }

    return 0;
  }
  function isDisplayed(element) {
    while (element?.parentNode) {
      let display = element.getStyle('display');
      if (display === 'none') {
        return false;
      }
      element = $(element.parentNode);
    }
    return true;
  }

  let hasLayout = Prototype.K;
  if ('currentStyle' in document.documentElement) {
    hasLayout = function(element) {
      if (!element.currentStyle.hasLayout) {
        element.style.zoom = 1;
      }
      return element;
    };
  }
  Element.Layout = Class.Create(Hash, {
    initialize: function($super, element, preCompute) {
      $super();
      this.element = $(element);

      Element.Layout.PROPERTIES.each( function(property) {
        this._set(property, null);
      }, this);

      if (preCompute) {
        this._preComputing = true;
        this._begin();
        Element.Layout.PROPERTIES.each( this._compute, this );
        this._end();
        this._preComputing = false;
      }
    },

    _set: function(property, value) {
      return Hash.prototype.set.call(this, property, value);
    },


    get: function($super, property) {
      let value = $super(property);
      return value === null ? this._compute(property) : value;
    },

    _begin: function() {
      if (this._isPrepared()) return;

      let element = this.element;
      if (isDisplayed(element)) {
        this._setPrepared(true);
        return;
      }


      let originalStyles = {
        position:   element.style.position   || '',
        width:      element.style.width      || '',
        visibility: element.style.visibility || '',
        display:    element.style.display    || ''
      };

      element.store('prototype_original_styles', originalStyles);

      let position = getRawStyle(element, 'position'), width = element.offsetWidth;

      if (width === 0 || width === null) {
        element.style.display = 'block';
        width = element.offsetWidth;
      }

      let context = (position === 'fixed') ? document.viewport :
          element.parentNode;

      let tempStyles = {
        visibility: 'hidden',
        display:    'block'
      };

      if (position !== 'fixed') tempStyles.position = 'absolute';

      element.setStyle(tempStyles);

      let positionedWidth = element.offsetWidth, newWidth;
      if (width && (positionedWidth === width)) {
        newWidth = getContentWidth(element, context);
      } else if (position === 'absolute' || position === 'fixed') {
        newWidth = getContentWidth(element, context);
      } else {
        let parent = element.parentNode, pLayout = $(parent).getLayout();

        newWidth = pLayout.get('width') -
            this.get('margin-left') -
            this.get('border-left') -
            this.get('padding-left') -
            this.get('padding-right') -
            this.get('border-right') -
            this.get('margin-right');
      }

      element.setStyle({ width: newWidth + 'px' });

      this._setPrepared(true);
    },

    _end: function() {
      let element = this.element;
      let originalStyles = element.retrieve('prototype_original_styles');
      element.store('prototype_original_styles', null);
      element.setStyle(originalStyles);
      this._setPrepared(false);
    },

    _compute: function(property) {
      let COMPUTATIONS = Element.Layout.COMPUTATIONS;
      if (!(property in COMPUTATIONS)) {
        throw new Error("Property not found.");
      }


      return this._set(property, COMPUTATIONS[property].call(this, this.element));
    },

    _isPrepared: function() {
      return this.element.retrieve('prototype_element_layout_prepared', false);
    },

    _setPrepared: function(bool) {
      return this.element.store('prototype_element_layout_prepared', bool);
    },

    toObject: function() {
      let args = $A(arguments);
      let keys = (args.length === 0) ? Element.Layout.PROPERTIES :
          args.join(' ').split(' ');
      let obj = {};
      keys.each( function(key) {
        if (!Element.Layout.PROPERTIES.include(key)) return;
        let value = this.get(key);
        if (value != null) obj[key] = value;
      }, this);
      return obj;
    },
    inspect: function() {
      return "#<Element.Layout>";
    }
  });

  Object.extend(Element.Layout, {
    PROPERTIES: $w('height width top left right bottom border-left border-right border-top border-bottom padding-left padding-right padding-top padding-bottom margin-top margin-bottom margin-left margin-right padding-box-width padding-box-height border-box-width border-box-height margin-box-width margin-box-height'),

    COMPOSITE_PROPERTIES: $w('padding-box-width padding-box-height margin-box-width margin-box-height border-box-width border-box-height'),

    COMPUTATIONS:{'height': function() {
      let bHeight = this.get('border-box-height');
      if (bHeight <= 0) {
        return 0;
      }

      let bTop = this.get('border-top'),
          bBottom = this.get('border-bottom');

      let pTop = this.get('padding-top'),
          pBottom = this.get('padding-bottom');

      return bHeight - bTop - bBottom - pTop - pBottom;
    },

      'width': function(element) {
        if (!this._preComputing) this._begin();

        let bWidth = this.get('border-box-width');
        if (bWidth <= 0) {
          if (!this._preComputing) this._end();
          return 0;
        }

        let bLeft = this.get('border-left'),
            bRight = this.get('border-right');

        let pLeft = this.get('padding-left'),
            pRight = this.get('padding-right');

        if (!this._preComputing) this._end();
        return bWidth - bLeft - bRight - pLeft - pRight;
      },

      'padding-box-height': function(element) {
        let height = this.get('height'),
            pTop = this.get('padding-top'),
            pBottom = this.get('padding-bottom');

        return height + pTop + pBottom;
      },

      'padding-box-width': function(element) {
        let width = this.get('width'),
            pLeft = this.get('padding-left'),
            pRight = this.get('padding-right');

        return width + pLeft + pRight;
      },

      'border-box-height': function(element) {
        if (!this._preComputing) this._begin();
        let height = element.offsetHeight;
        if (!this._preComputing) this._end();
        return height;
      },

      'border-box-width': function(element) {
        if (!this._preComputing) this._begin();
        let width = element.offsetWidth;
        if (!this._preComputing) this._end();
        return width;
      },

      'margin-box-height': function(element) {
        let bHeight = this.get('border-box-height'),
            mTop = this.get('margin-top'),
            mBottom = this.get('margin-bottom');

        if (bHeight <= 0) return 0;

        return bHeight + mTop + mBottom;
      },

      'margin-box-width': function(element) {
        let bWidth = this.get('border-box-width'),
            mLeft = this.get('margin-left'),
            mRight = this.get('margin-right');

        if (bWidth <= 0) return 0;

        return bWidth + mLeft + mRight;
      },

      'top': function(element) {
        let offset = element.positionedOffset();
        return offset.top;
      },

      'bottom': function(element) {
        let offset = element.positionedOffset(),
            parent = element.getOffsetParent(),
            pHeight = parent.measure('height');

        let mHeight = this.get('border-box-height');

        return pHeight - mHeight - offset.top;
      },

      'left': function(element) {
        let offset = element.positionedOffset();
        return offset.left;
      },

      'right': function(element) {
        let offset = element.positionedOffset(),
            parent = element.getOffsetParent(),
            pWidth = parent.measure('width');

        let mWidth = this.get('border-box-width');

        return pWidth - mWidth - offset.left;
      },

      'padding-top': function(element) {
        return getPixelValue(element, 'paddingTop');
      },

      'padding-bottom': function(element) {
        return getPixelValue(element, 'paddingBottom');
      },

      'padding-left': function(element) {
        return getPixelValue(element, 'paddingLeft');
      },

      'padding-right': function(element) {
        return getPixelValue(element, 'paddingRight');
      },

      'border-top': function(element) {
        return getPixelValue(element, 'borderTopWidth');
      },

      'border-bottom': function(element) {
        return getPixelValue(element, 'borderBottomWidth');
      },

      'border-left': function(element) {
        return getPixelValue(element, 'borderLeftWidth');
      },

      'border-right': function(element) {
        return getPixelValue(element, 'borderRightWidth');
      },

      'margin-top': function(element) {
        return getPixelValue(element, 'marginTop');
      },

      'margin-bottom': function(element) {
        return getPixelValue(element, 'marginBottom');
      },

      'margin-left': function(element) {
        return getPixelValue(element, 'marginLeft');
      },

      'margin-right': function(element) {
        return getPixelValue(element, 'marginRight');
      }
    }
  });

  if ('getBoundingClientRect' in document.documentElement) {
    Object.extend(Element.Layout.COMPUTATIONS, {
      'right': function(element) {
        let parent = hasLayout(element.getOffsetParent());
        let rect = element.getBoundingClientRect(),
            pRect = parent.getBoundingClientRect();

        return (pRect.right - rect.right).round();
      },

      'bottom': function(element) {
        let parent = hasLayout(element.getOffsetParent());
        let rect = element.getBoundingClientRect(),
            pRect = parent.getBoundingClientRect();

        return (pRect.bottom - rect.bottom).round();
      }
    });
  }

  Element.Offset = Class.Create({
    initialize: function(left, top) {
      this.left = left.round();
      this.top  = top.round();

      this[0] = this.left;
      this[1] = this.top;
    },

    relativeTo: function(offset) {
      return new Element.Offset(
          this.left - offset.left,
          this.top  - offset.top
      );
    },

    inspect: function() {
      return "#<Element.Offset left: #{left} top: #{top}>".interpolate(this);
    },

    toString: function() {
      return "[#{left}, #{top}]".interpolate(this);
    },

    toArray: function() {
      return [this.left, this.top];
    }
  });

  function getLayout(element, preCompute) {
    return new Element.Layout(element, preCompute);
  }

  function measure(element, property) {
    return $(element).getLayout().get(property);
  }

  function getHeight(element) {
    return Element.getDimensions(element).height;
  }

  function getWidth(element) {
    return Element.getDimensions(element).width;
  }

  function getDimensions(element) {
    element = $(element);
    let display = Element.getStyle(element, 'display');

    if (display && display !== 'none') {
      return { width: element.offsetWidth, height: element.offsetHeight };
    }

    let style = element.style;
    let originalStyles = {
      visibility: style.visibility,
      position:   style.position,
      display:    style.display
    };

    let newStyles = {
      visibility: 'hidden',
      display:    'block'
    };

    if (originalStyles.position !== 'fixed')
      newStyles.position = 'absolute';

    Element.setStyle(element, newStyles);

    let dimensions = {
      width:  element.offsetWidth,
      height: element.offsetHeight
    };

    Element.setStyle(element, originalStyles);

    return dimensions;
  }

  function getOffsetParent(element) {
    element = $(element);

    function selfOrBody(element) {
      return isHtml(element) ? $(document.body) : $(element);
    }

    if (isDocument(element) || isDetached(element) || isBody(element) || isHtml(element))
      return $(document.body);

    let isInline = (Element.getStyle(element, 'display') === 'inline');
    if (!isInline && element.offsetParent) return selfOrBody(element.offsetParent);

    let elements= element.parentNode;
    while (elements !== document.body) {
      if (Element.getStyle(element, 'position') !== 'static') {
        return selfOrBody(element);
      }
    }

    return $(document.body);
  }


  function cumulativeOffset(element) {
    element = $(element);
    let valueT = 0, valueL = 0;
    if (element.parentNode) {
      do {
        valueT += element.offsetTop  || 0;
        valueL += element.offsetLeft || 0;
        element = element.offsetParent;
      } while (element);
    }
    return new Element.Offset(valueL, valueT);
  }

  function positionedOffset(element) {
    element = $(element);

    let layout = element.getLayout();

    let valueT = 0, valueL = 0;
    do {
      valueT += element.offsetTop  || 0;
      valueL += element.offsetLeft || 0;
      element = element.offsetParent;
      if (element) {
        if (isBody(element)) break;
        let p = Element.getStyle(element, 'position');
        if (p !== 'static') break;
      }
    } while (element);

    valueL -= layout.get('margin-left');
    valueT -= layout.get('margin-top');

    return new Element.Offset(valueL, valueT);
  }

  function cumulativeScrollOffset(element) {
    let valueT = 0, valueL = 0;
    do {
      if (element === document.body) {
        let bodyScrollNode = document.documentElement || document.body.parentNode || document.body;
        valueT += !Object.isUndefined(window.screenY) ? window.screenY : bodyScrollNode.scrollTop || 0;
        valueL += !Object.isUndefined(window.screenX) ? window.screenX : bodyScrollNode.scrollLeft || 0;
        break;
      } else {
        valueT += element.scrollTop  || 0;
        valueL += element.scrollLeft || 0;
        element = element.parentNode;
      }
    } while (element);
    return new Element.Offset(valueL, valueT);
  }

  function viewportOffset(forElement) {
    let valueT = 0, valueL = 0, docBody = document.body;

    forElement = $(forElement);
    let element = forElement;
    do {
      valueT += element.offsetTop  || 0;
      valueL += element.offsetLeft || 0;
      if (element.offsetParent == docBody &&
          Element.getStyle(element, 'position') == 'absolute') break;
    } while (element = element.offsetParent);

    element = forElement;
    do {
      if (element != docBody) {
        valueT -= element.scrollTop  || 0;
        valueL -= element.scrollLeft || 0;
      }
    } while (element = element.parentNode);
    return new Element.Offset(valueL, valueT);
  }

  function absolutize(element) {
    element = $(element);

    if (Element.getStyle(element, 'position') === 'absolute') {
      return element;
    }

    let offsetParent = getOffsetParent(element);
    let eOffset = element.viewportOffset(),
        pOffset = offsetParent.viewportOffset();

    let offset = eOffset.relativeTo(pOffset);
    let layout = element.getLayout();

    element.store('prototype_absolutize_original_styles', {
      position: element.getStyle('position'),
      left:     element.getStyle('left'),
      top:      element.getStyle('top'),
      width:    element.getStyle('width'),
      height:   element.getStyle('height')
    });

    element.setStyle({
      position: 'absolute',
      top:    offset.top + 'px',
      left:   offset.left + 'px',
      width:  layout.get('width') + 'px',
      height: layout.get('height') + 'px'
    });

    return element;
  }

  function relativize(element) {
    element = $(element);
    if (Element.getStyle(element, 'position') === 'relative') {
      return element;
    }

    let originalStyles =
        element.retrieve('prototype_absolutize_original_styles');

    if (originalStyles) element.setStyle(originalStyles);
    return element;
  }


  function scrollTo(element) {
    element = $(element);
    let pos = Element.cumulativeOffset(element);
    window.scrollTo(pos.left, pos.top);
    return element;
  }


  function makePositioned(element) {
    element = $(element);
    let position = Element.getStyle(element, 'position'), styles = {};
    if (position === 'static' || !position) {
      styles.position = 'relative';
      if (Prototype.Browser.Opera) {
        styles.top  = 0;
        styles.left = 0;
      }
      Element.setStyle(element, styles);
      Element.store(element, 'prototype_made_positioned', true);
    }
    return element;
  }

  function undoPositioned(element) {
    element = $(element);
    let storage = Element.getStorage(element),
        madePositioned = storage.get('prototype_made_positioned');

    if (madePositioned) {
      storage.unset('prototype_made_positioned');
      Element.setStyle(element, {
        position: '',
        top:      '',
        bottom:   '',
        left:     '',
        right:    ''
      });
    }
    return element;
  }

  function makeClipping(element) {
    element = $(element);

    let storage = Element.getStorage(element),
        madeClipping = storage.get('prototype_made_clipping');

    if (Object.isUndefined(madeClipping)) {
      let overflow = Element.getStyle(element, 'overflow');
      storage.set('prototype_made_clipping', overflow);
      if (overflow !== 'hidden')
        element.style.overflow = 'hidden';
    }

    return element;
  }

  function undoClipping(element) {
    element = $(element);
    let storage = Element.getStorage(element),
        overflow = storage.get('prototype_made_clipping');

    if (!Object.isUndefined(overflow)) {
      storage.unset('prototype_made_clipping');
      element.style.overflow = overflow || '';
    }

    return element;
  }

  function clonePosition(element, source, options) {
    options = Object.extend({
      setLeft:    true,
      setTop:     true,
      setWidth:   true,
      setHeight:  true,
      offsetTop:  0,
      offsetLeft: 0
    }, options || {});

    let docEl = document.documentElement;

    source  = $(source);
    element = $(element);
    let p, delta, layout, styles = {};

    if (options.setLeft || options.setTop) {
      p = Element.viewportOffset(source);
      delta = [0, 0];
      if (Element.getStyle(element, 'position') === 'absolute') {
        let parent = Element.getOffsetParent(element);
        if (parent !== document.body) delta = Element.viewportOffset(parent);
      }
    }

    function pageScrollXY() {
      let x = 0, y = 0;
      if (Object.isNumber(window.screenX)) {
        x = window.screenX;
        y = window.screenY;
      } else if (document.body && (document.body.scrollLeft || document.body.scrollTop)) {
        x = document.body.scrollLeft;
        y = document.body.scrollTop;
      } else if (docEl && (docEl.scrollLeft || docEl.scrollTop)) {
        x = docEl.scrollLeft;
        y = docEl.scrollTop;
      }
      return { x: x, y: y };
    }

    let pageXY = pageScrollXY();


    if (options.setWidth || options.setHeight) {
      layout = Element.getLayout(source);
    }

    if (options.setLeft)
      styles.left = (p[0] + pageXY.x - delta[0] + options.offsetLeft) + 'px';
    if (options.setTop)
      styles.top  = (p[1] + pageXY.y - delta[1] + options.offsetTop)  + 'px';
    element.getLayout();
    if (options.setWidth) {
      styles.width = layout.get('width')  + 'px';
    }
    if (options.setHeight) {
      styles.height = layout.get('height') + 'px';
    }

    return Element.setStyle(element, styles);
  }


  if (Prototype.Browser.IE) {
    getOffsetParent = getOffsetParent.wrap(
        function(proceed, element) {
          element = $(element);

          if (isDocument(element) || isDetached(element) || isBody(element) || isHtml(element))
            return $(document.body);

          let position = element.getStyle('position');
          if (position !== 'static') return proceed(element);

          element.setStyle({ position: 'relative' });
          let value = proceed(element);
          element.setStyle({ position: position });
          return value;
        }
    );

    positionedOffset = positionedOffset.wrap(function(proceed, element) {
      element = $(element);
      if (!element.parentNode) return new Element.Offset(0, 0);
      let position = element.getStyle('position');
      if (position !== 'static') return proceed(element);

      let offsetParent = element.getOffsetParent();
      if (offsetParent && offsetParent.getStyle('position') === 'fixed')
        hasLayout(offsetParent);

      element.setStyle({ position: 'relative' });
      let value = proceed(element);
      element.setStyle({ position: position });
      return value;
    });
  } else if (Prototype.Browser.Webkit) {
    cumulativeOffset = function(element) {
      element = $(element);
      let valueT = 0, valueL = 0;
      do {
        valueT += element.offsetTop  || 0;
        valueL += element.offsetLeft || 0;
        if (element.offsetParent == document.body) {
          if (Element.getStyle(element, 'position') == 'absolute') break;
        }

        element = element.offsetParent;
      } while (element);

      return new Element.Offset(valueL, valueT);
    };
  }


  Element.addMethods({
    getLayout:              getLayout,
    measure:                measure,
    getWidth:               getWidth,
    getHeight:              getHeight,
    getDimensions:          getDimensions,
    getOffsetParent:        getOffsetParent,
    cumulativeOffset:       cumulativeOffset,
    positionedOffset:       positionedOffset,
    cumulativeScrollOffset: cumulativeScrollOffset,
    viewportOffset:         viewportOffset,
    absolutize:             absolutize,
    relativize:             relativize,
    scrollTo:               scrollTo,
    makePositioned:         makePositioned,
    undoPositioned:         undoPositioned,
    makeClipping:           makeClipping,
    undoClipping:           undoClipping,
    clonePosition:          clonePosition
  });

  function isBody(element) {
    return element.nodeName.toUpperCase() === 'BODY';
  }

  function isHtml(element) {
    return element.nodeName.toUpperCase() === 'HTML';
  }

  function isDocument(element) {
    return element.nodeType === Node.DOCUMENT_NODE;
  }

  function isDetached(element) {
    return element !== document.body &&
        !Element.descendantOf(element, document.body);
  }

  if ('getBoundingClientRect' in document.documentElement) {
    Element.addMethods({
      viewportOffset: function(element) {
        element = $(element);
        if (isDetached(element)) return new Element.Offset(0, 0);

        let rect = element.getBoundingClientRect(),
            docEl = document.documentElement;
        return new Element.Offset(rect.left - docEl.clientLeft,
            rect.top - docEl.clientTop);
      }
    });
  }


})();

(function() {

  let IS_OLD_OPERA = Prototype.Browser.Opera &&
   (window.parseFloat(window.opera.version()) < 9.5);
  let ROOT = null;
  function getRootElement() {
    if (ROOT) return ROOT;
    ROOT = IS_OLD_OPERA ? document.body : document.documentElement;
    return ROOT;
  }

  function getDimensions() {
    return { width: this.getWidth(), height: this.getHeight() };
  }

  function getWidth() {
    return getRootElement().clientWidth;
  }

  function getHeight() {
    return getRootElement().clientHeight;
  }

  function getScrollOffsets() {
    let x = window.screenX || document.documentElement.scrollLeft ||
     document.body.scrollLeft;
    let y = window.screenY || document.documentElement.scrollTop ||
     document.body.scrollTop;

    return new Element.Offset(x, y);
  }

  document.viewport = {
    getDimensions:    getDimensions,
    getWidth:         getWidth,
    getHeight:        getHeight,
    getScrollOffsets: getScrollOffsets
  };

})();
window.$$ = function() {
  let expression = $A(arguments).join(', ');
  return Prototype.Selector.select(expression, document);
};

Prototype.Selector = (function() {

  function select() {
    throw new Error('Method "Prototype.Selector.select" must be defined.');
  }

  function match() {
    throw new Error('Method "Prototype.Selector.match" must be defined.');
  }

  function find(elements, expression, index) {
    index = index || 0;
    let match = Prototype.Selector.match, length = elements.length, matchIndex = 0, i;

    for (i = 0; i < length; i++) {
      if (match(elements[i], expression) && index == matchIndex++) {
        return Element.extend(elements[i]);
      }
    }
  }

  function extendElements(elements) {
    for (let i = 0, length = elements.length; i < length; i++) {
      Element.extend(elements[i]);
    }
    return elements;
  }


  let K = Prototype.K;

  return {
    select: select,
    match: match,
    find: find,
    extendElements: (Element.extend === K) ? K : extendElements,
    extendElement: Element.extend
  };
})();
Prototype._original_property = window.Sizzle;

;(function () {
  function fakeDefine(fn) {
    Prototype._actual_sizzle = fn();
  }
  fakeDefine.amd = true;

  if (typeof define !== 'undefined' && define.amd) {
    Prototype._original_define = define;
    Prototype._actual_sizzle = null;
    window.define = fakeDefine;
  }
})();

class setFilters {
  constructor(Expr) {
    this.Expr = <Expr></Expr>
  }

}

/*!
 * Sizzle CSS Selector Engine v1.10.18
 * https://github.com/jquery/sizzle/wiki
 *
 * Copyright 2013 jQuery Foundation, Inc. and other contributors
 * Released under the MIT license
 * https://jquery.org/license/
 *
 * Date: 2014-02-05
 */
(function( window ) {

let i,
	support,
	Expr,
	getText,
	isXML,
	compile,
	select,
	outermostContext,
	sortInput,
	hasDuplicate,

	setDocument,
	document,
	docElem,
	documentIsHTML,
	rbuggyQSA,
	rbuggyMatches,
	matches,
	contains,

	expando = "sizzle" + -(new Date()),
	preferredDoc = window.document,
	dirruns = 0,
	done = 0,
	classCache = createCache(),
	tokenCache = createCache(),
	compilerCache = createCache(),
	sortOrder = function( a, b ) {
		if ( a === b ) {
			hasDuplicate = true;
		}
		return 0;
	},

	strundefined = typeof undefined,
	MAX_NEGATIVE = 1 << 31,

	hasOwn = ({}).hasOwnProperty,
	arr = [],
	push_native = arr.push,
	push = arr.push,
	slice = arr.slice,
	indexOf = arr.indexOf || function( elem ) {
		let i = 0,
			len = this.length;
		for ( ; i < len; i++ ) {
			if ( this[i] === elem ) {
				return i;
			}
		}
		return -1;
	},

	booleans = "checked|selected|async|autofocus|autoplay|controls|defer|disabled|hidden|ismap|loop|multiple|open|readonly|required|scoped",


	whitespace = "[\\x20\\t\\r\\n\\f]",
	characterEncoding = "(?:\\\\.|[\\w-]|[^\\x00-\\xa0])+",

	identifier = characterEncoding.replace( "w", "w#" ),

	attributes = "\\[" + whitespace + "*(" + characterEncoding + ")" + whitespace +
		"*(?:([*^$|!~]?=)" + whitespace + "*(?:(['\"])((?:\\\\.|[^\\\\])*?)\\3|(" + identifier + ")|)|)" + whitespace + "*\\]",

    pseudos = ":(" + characterEncoding + ")(?:\\(((['\"])((?:\\\\.|[^\\\\])*?)\\3|((?:\\\\.|[^\\\\()[\\]]|" + String(attributes.replace('3', 8)) + ")*)|.*)\\)|)",
    rtrim = new RegExp("^(?:(?:" + whitespace + "+)|(?:((?:^|[^\\\\])(?:\\\\.)*)" + whitespace + "+$))", "g"),
	rcomma = new RegExp( "^" + whitespace + "*," + whitespace + "*" ),
	rcombinators = new RegExp( "^" + whitespace + "*([>+~]|" + whitespace + ")" + whitespace + "*" ),

    rattributeQuotes = new RegExp( "=" + whitespace + "*([^\\]'\"\\]]*?)" + whitespace + "*\\]", "g" ),
	rpseudo = new RegExp( pseudos ),
	ridentifier = new RegExp( "^" + identifier + "$" ),

	matchExpr = {
      "ID": new RegExp("^#(" + characterEncoding.replace(/[\x7F-\x9F]/g, "") + ")"),
      "CLASS": new RegExp("^\\.(" + characterEncoding.replace(/[\x7F-\x9F]/g, "") + ")"),
		"TAG": new RegExp( "^(" + characterEncoding.replace( "w", "w*" ) + ")" ),
		"ATTR": new RegExp( "^" + attributes ),
		"PSEUDO": new RegExp( "^" + pseudos ),
		"CHILD": new RegExp( "^:(only|first|last|nth|nth-last)-(child|of-type)(?:\\(" + whitespace +
            "*(even|odd|(([+-]|)(\\d+)n|)" + whitespace + "*(?:([+-]|)" + whitespace +
            "*(\\d+)|))" + whitespace + "*\\)|)", "i" ),
		"bool": new RegExp( "^(?:" + booleans + ")$", "i" ),
      "needsContext": new RegExp(
          "^(?:" + whitespace + "*[>+~]|:(even|odd|eq|gt|lt|nth|first|last)(?:\\(" +
          whitespace + "*((?:-?\\d)+)" + whitespace + "*\\)|)(?=[^-]|$))", "i"
      )
	},

	rinputs = /^(?:input|select|textarea|button)$/i,
	rheader = /^h\d$/i,

	rnative = /^[^{]+\{\s*\[native \w/,

	rquickExpr = /^(?:#([\w-]+)|(\w+)|\.([\w-]+))$/,

	rsibling = /[+~]/,
    rescape = /[\\']/g,

	runescape = new RegExp( "\\\\([\\da-f]{1,6}" + whitespace + "?|(" + whitespace + ")|.)", "ig" ),
	funescape = function( _, escaped, escapedWhitespace ) {
		let high = "0x" + escaped - 0x10000;
      if (high !== high || escapedWhitespace) {
        return escaped;
      } else if (high < 0) {
        return String.fromCharCode(high + 0x10000);
      } else {
        return String.fromCharCode(high >> 10 | 0xD800, high & 0x3FF | 0xDC00);
      }
    };


      try {
        let arr = slice.call(preferredDoc.childNodes);
        push.apply(arr, preferredDoc.childNodes);
      } catch ( e ) {
	push = { apply: arr.length ?

		function( target, els ) {
			push_native.apply( target, slice.call(els) );
		} :

		function( target, els ) {
			let j = target.length,
				i = 0;
			while ( (target[j++] = els[i++]) )
			target.length = j - 1;
		}
	};
}

function Sizzle( selector, context, results, seed ) {
	let  elem, nodeType,
		i, groups, old, nid, newContext, newSelector;

	if ( ( context ? context.ownerDocument || context : preferredDoc ) !== document ) {
		setDocument( context );
	}

	context = context || document;
	results = results || [];

	if ( !selector || typeof selector !== "string" ) {
		return results;
	}

	if ( (nodeType = context.nodeType) !== 1 && nodeType !== 9 ) {
		return [];
	}

	if ( documentIsHTML && !seed ) {
        let match = rquickExpr.exec( selector );
		if (match) {
          let m = match[1];
			if (m) {
				if ( nodeType === 9 ) {
					elem = context.getElementById( m );
                  if (elem?.parentNode &&  elem.id === m ) {
							results.push( elem );
							return results;
					} else {
						return results;
					}
				} else if ( context.ownerDocument && (elem = context.ownerDocument.getElementById( m )) &&
						contains( context, elem ) && elem.id === m ) {
						results.push( elem );
						return results;
					}


			} else if ( match[2] ) {
				push.apply( results, context.getElementsByTagName( selector ) );
				return results;

			} else

				push.apply( results, context.getElementsByClassName( m ) );
				return results;

		}

      if (support?.qsa && (!rbuggyQSA?.test(selector) ))
      {
			nid = old = expando;
			newContext = context;
			newSelector = nodeType === 9 && selector;

			if ( nodeType === 1 && context.nodeName.toLowerCase() !== "object" ) {
				groups = tokenize( selector );
              let old = context.getAttribute("id");
              if (old) {
                nid = old.replace(rescape, "\\$&");
				} else {
					context.setAttribute( "id", nid );
				}
				nid = "[id='" + nid + "'] ";

				i = groups.length;
				while ( i-- ) {
					groups[i] = nid + toSelector( groups[i] );
				}
				newContext = rsibling.test( selector ) && testContext( context.parentNode ) || context;
				newSelector = groups.join(",");
			}

			if ( newSelector ) {
				try {
					push.apply( results,
						newContext.querySelectorAll( newSelector )
					);
					return results;
				} catch(qsaError) {
				} finally {
					if ( !old ) {
						context.removeAttribute("id");
					}
				}
			}
		}
	}

	return select( selector.replace( rtrim, "$1" ), context, results, seed );
}

/**
 * Create key-value caches of limited size
 * @returns {Function(string, Object)} Returns the Object data after storing it on itself with
 *	property name the (space-suffixed) string and (if the cache is larger than Expr.cacheLength)
 *	deleting the oldest entry
 */
function createCache() {
	let keys = [];

  function cache(key, value) {
    let cacheKey = key + " ";

    if (keys.push(cacheKey) > Expr.cacheLength) {
      delete cache[keys.shift()];
    }
  cache[cacheKey] = value;
    return cache[cacheKey] ;
  }

	return cache;
}

/**
 * Mark a function for special use by Sizzle
 * @param {Function} fn The function to mark
 */
function markFunction( fn ) {
	fn[ expando ] = true;
	return fn;
}

/**
 * Support testing using an element
 * @param {Function} fn Passed the created div and expects a boolean result
 */
function assert( fn ) {
	let div = document.createElement("div");

	try {
		return !!fn( div );
	} catch (e) {
		return false;
	} finally {
		if ( div.parentNode ) {
			div.parentNode.removeChild( div );
		}
		div = null;
	}
}

/**
 * Adds the same handler for all of the specified attrs
 * @param {String} attrs Pipe-separated list of attributes
 * @param {Function} handler The method that will be applied
 */
function addHandle( attrs, handler ) {
	let arr = attrs.split("|"),
		i = attrs.length;

	while ( i-- ) {
		Expr.attrHandle[ arr[i] ] = handler;
	}
}

/**
 * Checks document order of two siblings
 * @param {Element} a
 * @param {Element} b
 * @returns {Number} Returns less than 0 if a precedes b, greater than 0 if a follows b
 */
function siblingCheck( a, b ) {
	let cur = b && a,
		diff = cur && a.nodeType === 1 && b.nodeType === 1 &&
			( ~b.sourceIndex || MAX_NEGATIVE ) -
			( ~a.sourceIndex || MAX_NEGATIVE );

	if ( diff ) {
		return diff;
	}

	if ( cur ) {
		while ( (cur = cur.nextSibling) ) {
			if ( cur === b ) {
				return -1;
			}
		}
	}

	return a ? 1 : -1;
}

/**
 * Returns a function to use in pseudos for input types
 * @param {String} type
 */
function createInputPseudo( type ) {
	return function( elem ) {
		let name = elem.nodeName.toLowerCase();
		return name === "input" && elem.type === type;
	};
}

/**
 * Returns a function to use in pseudos for buttons
 * @param {String} type
 */
function createButtonPseudo( type ) {
	return function( elem ) {
		let name = elem.nodeName.toLowerCase();
		return (name === "input" || name === "button") && elem.type === type;
	};
}

/**
 * Returns a function to use in pseudos for positionals
 * @param {Function} fn
 */
function createPositionalPseudo( fn ) {
	return markFunction(function( argument ) {
		argument = +argument;
		return markFunction(function( seed, matches ) {
			let
				matchIndexes = fn( [], seed.length, argument ),
				i = matchIndexes.length;

			while ( i-- ) {
              let j = matchIndexes[i];
              matches[j] = seed[j];
              if (seed[j]) {
                seed[j] = !matches[j] ;
              }

            }
		});
	});
}

/**
 * Checks a node for validity as a Sizzle context
 * @param {Element|Object=} context
 * @returns {Element|Object|Boolean} The input node if acceptable, otherwise a falsy value
 */
function testContext( context ) {
	return context && typeof context.getElementsByTagName !== strundefined && context;
}

support = Sizzle.support = {};

/**
 * Detects XML nodes
 * @param {Element|Object} elem An element or a document
 * @returns {Boolean} True iff elem is a non-HTML XML node
 */
isXML = Sizzle.isXML = function( elem ) {
	let documentElement = elem && (elem.ownerDocument || elem).documentElement;
	return documentElement ? documentElement.nodeName !== "HTML" : false;
};

/**
 * Sets document-related letiables once based on the current document
 * @returns {Object} Returns the current document
 * @param node
 */
setDocument = Sizzle.setDocument = function( node ) {
	let hasCompare,
		doc = node ? node.ownerDocument || node : preferredDoc,
		parent = doc.defaultView;

	if ( doc === document || doc.nodeType !== 9 || !doc.documentElement ) {
		return document;
	}

	document = doc;
	docElem = doc.documentElement;

	documentIsHTML = !isXML( doc );

	if ( parent && parent !== parent.top ) {
		if ( parent.addEventListener ) {
			parent.addEventListener( "unload", function() {
				setDocument();
			}, false );
		} else if ( parent.attachEvent ) {
			parent.attachEvent( "onunload", function() {
				setDocument();
			});
		}
	}

	/* Attributes
	---------------------------------------------------------------------- */

	support.attributes = assert(function( div ) {
		div.className = "i";
		return !div.getAttribute("className");
	});

	/* getElement(s)By*
	---------------------------------------------------------------------- */

	support.getElementsByTagName = assert(function( div ) {
		div.appendChild( doc.createComment("") );
		return !div.getElementsByTagName("*").length;
	});

	support.getElementsByClassName = rnative.test( doc.getElementsByClassName ) && assert(function( div ) {
		div.innerHTML = "<div class='a'></div><div class='a i'></div>";

		div.firstChild.className = "i";
		return div.getElementsByClassName("i").length === 2;
	});

	support.getById = assert(function( div ) {
		docElem.appendChild( div ).id = expando;
      return !doc?.getElementsByName?.(expando)?.length;
    });

	if ( support.getById ) {
		Expr.find["ID"] = function( id, context ) {
			if ( typeof context.getElementById !== strundefined && documentIsHTML ) {
				let m = context.getElementById( id );
              return m?.parentNode ? [m] : [];
            }
		};
		Expr.filter["ID"] = function( id ) {
			let attrId = id.replace( runescape, funescape );
			return function( elem ) {
				return elem.getAttribute("id") === attrId;
			};
		};
	} else {
		delete Expr.find["ID"];

		Expr.filter["ID"] =  function( id ) {
			let attrId = id.replace( runescape, funescape );
			return function( elem ) {
				let node = typeof elem.getAttributeNode !== strundefined && elem.getAttributeNode("id");
				return node && node.value === attrId;
			};
		};
	}

	Expr.find["TAG"] = support.getElementsByTagName ?
		function( tag, context ) {
			if ( typeof context.getElementsByTagName !== strundefined ) {
				return context.getElementsByTagName( tag );
			}
		} :
		function( tag, context ) {
			let elem,
				tmp = [],
				i = 0,
				results = context.getElementsByTagName( tag );

			if ( tag === "*" ) {
				while ( (elem = results[i++]) ) {
					if ( elem.nodeType === 1 ) {
						tmp.push( elem );
					}
				}

				return tmp;
			}
			return results;
		};

	Expr.find["CLASS"] = support.getElementsByClassName && function( className, context ) {
		if ( typeof context.getElementsByClassName !== strundefined && documentIsHTML ) {
			return context.getElementsByClassName( className );
		}
	};

	/* QSA/matchesSelector
	---------------------------------------------------------------------- */


	rbuggyMatches = [];

	rbuggyQSA = [];

  support.qsa = rnative.test(doc.querySelectorAll);
  if (support.qsa) {
    assert(function( div ) {
			div.innerHTML = "<select t=''><option selected=''></option></select>";

			if ( div.querySelectorAll("[t^='']").length ) {
				rbuggyQSA.push( "[*^$]=" + whitespace + "*(?:''|\"\")" );
			}

			if ( !div.querySelectorAll("[selected]").length ) {
				rbuggyQSA.push( "\\[" + whitespace + "*(?:value|" + booleans + ")" );
			}

			if ( !div.querySelectorAll(":checked").length ) {
				rbuggyQSA.push(":checked");
			}
		});

		assert(function( div ) {
			let input = doc.createElement("input");
			input.setAttribute( "type", "hidden" );
			div.appendChild( input ).setAttribute( "name", "D" );

			if ( div.querySelectorAll("[name=d]").length ) {
				rbuggyQSA.push( "name" + whitespace + "*[*^$|!~]?=" );
			}

			if ( !div.querySelectorAll(":enabled").length ) {
				rbuggyQSA.push( ":enabled", ":disabled" );
			}

			div.querySelectorAll("*,:enabled,:disabled");
			rbuggyQSA.push(",.*:");
		});
	}
  matches = docElem.webkitMatchesSelector;
  support.matchesSelector = rnative.test(matches);
	if ( (support.matchesSelector ||
		docElem.mozMatchesSelector ||
		docElem.oMatchesSelector ||
		docElem.msMatchesSelector) ) {

		assert(function( div ) {
			support.disconnectedMatch = matches.call( div, "div" );

			matches.call( div, "[s!='']:x" );
			rbuggyMatches.push( "!=", pseudos );
		});
	}

	rbuggyQSA = rbuggyQSA.length && new RegExp( rbuggyQSA.join("|") );
	rbuggyMatches = rbuggyMatches.length && new RegExp( rbuggyMatches.join("|") );

	/* Contains
	---------------------------------------------------------------------- */
	hasCompare = rnative.test( docElem.compareDocumentPosition );

	contains = hasCompare || rnative.test( docElem.contains ) ?
		function( a, b ) {
			let adown = a.nodeType === 9 ? a.documentElement : a,
                bup = b?.parentNode;
          return a === bup || !!( bup && bup.nodeType === 1 && (
				adown.contains ?
					adown.contains( bup ) :
					a.compareDocumentPosition ?. a.compareDocumentPosition( bup ) && 16
			));
		} :
		function( a, b ) {
			if ( b ) {
				while ( (b = b.parentNode) ) {
					if ( b === a ) {
						return true;
					}
				}
			}
			return false;
		};

	/* Sorting
	---------------------------------------------------------------------- */

	sortOrder = hasCompare ?
	function( a, b ) {

		if ( a === b ) {
			hasDuplicate = true;
			return 0;
		}

		let compare = !a.compareDocumentPosition - !b.compareDocumentPosition;
		if ( compare ) {
			return compare;
		}

		compare = ( a.ownerDocument || a ) === ( b.ownerDocument || b ) ?
			a.compareDocumentPosition( b ) :

			1;

		if ( compare & 1 ||
			(!support.sortDetached && b.compareDocumentPosition( a ) === compare) ) {

			if ( a === doc || a.ownerDocument === preferredDoc && contains(preferredDoc, a) ) {
				return -1;
			}
			if ( b === doc || b.ownerDocument === preferredDoc && contains(preferredDoc, b) ) {
				return 1;
			}

			return sortInput ?
				( indexOf.call( sortInput, a ) - indexOf.call( sortInput, b ) ) :
				0;
		}

		return compare & 4 ? -1 : 1;
	} :
	function( a, b ) {
		if ( a === b ) {
			hasDuplicate = true;
			return 0;
		}

		let cur,
			i = 0,
			aup = a.parentNode,
			bup = b.parentNode,
			ap = [ a ],
			bp = [ b ];

      if (!aup || !bup) {
        if (a === doc) {
          return -1;
        } else if (b === doc) {
          return 1;
        } else if (aup) {
          return -1;
        } else if (bup) {
          return 1;
        } else if (sortInput) {
          return indexOf.call(sortInput, a) - indexOf.call(sortInput, b);
        } else {
          return 0;
        }
      }
      else if ( aup === bup ) {
			return siblingCheck( a, b );
		}

		cur = a;
		while ( (cur = cur.parentNode) ) {
			ap.unshift( cur );
		}
		cur = b;
		while ( (cur = cur.parentNode) ) {
			bp.unshift( cur );
		}

		while ( ap[i] === bp[i] ) {
			i++;
		}

      let result;
      if (i) {
        result = siblingCheck(ap[i], bp[i]);
      } else {
        let result;

        if (ap[i] === preferredDoc) {
          result = -1;
        } else if (bp[i] === preferredDoc) {
          result = 1;
        } else {
          result = 0;
        }
        return result;
      }

      return result;


    };

	return doc;
};

Sizzle.matches = function( expr, elements ) {
	return Sizzle( expr, null, null, elements );
};

Sizzle.matchesSelector = function( elem, expr ) {
	if ( ( elem.ownerDocument || elem ) !== document ) {
		setDocument( elem );
	}

  expr = expr.replace(rattributeQuotes, "='$1']");

  if (
      support.matchesSelector ?.
      documentIsHTML?.(!rbuggyMatches?.test?.(expr))?.( !rbuggyQSA?.test?.(expr))
  ) {

		try {
			let ret = matches.call( elem, expr );

			if ( ret || support.disconnectedMatch ||
					elem.document && elem.document.nodeType !== 11 ) {
				return ret;
			}
		} catch(e) {}
	}

	return Sizzle( expr, document, null, [elem] ).length > 0;
};

Sizzle.contains = function( context, elem ) {
	if ( ( context.ownerDocument || context ) !== document ) {
		setDocument( context );
	}
	return contains( context, elem );
};

Sizzle.attr = function( elem, name ) {
	if ( ( elem.ownerDocument || elem ) !== document ) {
		setDocument( elem );
	}

	let fn = Expr.attrHandle[ name.toLowerCase() ],
		val = fn && hasOwn.call( Expr.attrHandle, name.toLowerCase() ) ?
			fn( elem, name, !documentIsHTML ) :
			undefined;

  let val1 = elem.getAttributeNode(name) ;

  let result;

  if (val1 && val.specified) {
    result = val.value;
  } else if (val !== undefined) {
    result = val;
  } else if (support.attributes || !documentIsHTML) {
    result = elem.getAttribute(name);
  } else {
    result = null;
  }

  return result;

};

Sizzle.error = function( msg ) {
	throw new Error( "Syntax error, unrecognized expression: " + msg );
};

/**
 * Document sorting and removing duplicates
 * @param {ArrayLike} results
 */
Sizzle.uniqueSort = function( results ) {
	let elem,
		duplicates = [],
		j = 0,
		i = 0;

	hasDuplicate = !support.detectDuplicates;
	sortInput = !support.sortStable && results.slice( 0 );
	results.sort( sortOrder );

	if ( hasDuplicate ) {
		while ( (elem = results[i++]) ) {
			if ( elem === results[ i ] ) {
				j = duplicates.push( i );
			}
		}
		while ( j-- ) {
			results.splice( duplicates[ j ], 1 );
		}
	}

	sortInput = null;

	return results;
};

/**
 * Utility function for retrieving the text value of an array of DOM nodes
 * @param {Array|Element} elem
 */
getText = Sizzle.getText = function( elem ) {
	let node,
		ret = "",
		i = 0,
		nodeType = elem.nodeType;

	if ( !nodeType ) {
		while ( (node = elem[i++]) ) {
			ret += getText( node );
		}
	} else if ( nodeType === 1 || nodeType === 9 || nodeType === 11 ) {
		if ( typeof elem.textContent === "string" ) {
			return elem.textContent;
		} else {
			for ( elem = elem.firstChild; elem; elem = elem.nextSibling ) {
				ret += getText( elem );
			}
		}
	} else if ( nodeType === 3 || nodeType === 4 ) {
		return elem.nodeValue;
	}

	return ret;
};

Expr = Sizzle.selectors = {

	cacheLength: 50,

	createPseudo: markFunction,

	match: matchExpr,

	attrHandle: {},

	find: {},

	relative: {
		">": { dir: "parentNode", first: true },
		" ": { dir: "parentNode" },
		"+": { dir: "previousSibling", first: true },
		"~": { dir: "previousSibling" }
	},

	preFilter: {
		"ATTR": function( match ) {
			match[1] = match[1].replace( runescape, funescape );

			match[3] = ( match[4] || match[5] || "" ).replace( runescape, funescape );

			if ( match[2] === "~=" ) {
				match[3] = " " + match[3] + " ";
			}

			return match.slice( 0, 4 );
		},

		"CHILD": function( match ) {
			/* matches from matchExpr["CHILD"]
				1 type (only|nth|...)
				2 what (child|of-type)
				3 argument (even|odd|\d*|\d*n([+-]\d+)?|...)
				4 xn-component of xn+y argument ([+-]?\d*n|)
				5 sign of xn-component
				6 x of xn-component
				7 sign of y-component
				8 y of y-component
			*/
			match[1] = match[1].toLowerCase();

			if ( match[1].slice( 0, 3 ) === "nth" ) {
				if ( !match[3] ) {
					Sizzle.error( match[0] );
				}

				match[4] = +( match[4] ? match[5] + (match[6] || 1) : 2 * ( match[3] === "even" || match[3] === "odd" ) );
				match[5] = +( ( match[7] + match[8] ) || match[3] === "odd" );

			} else if ( match[3] ) {
				Sizzle.error( match[0] );
			}

			return match;
		},

		"PSEUDO": function( match ) {
			let excess,
				unquoted = !match[5] && match[2];

			if ( matchExpr["CHILD"].test( match[0] ) ) {
				return null;
			}

			if ( match[3] && match[4] !== undefined ) {
				match[2] = match[4];

			} else if ( unquoted && rpseudo.test( unquoted ) &&
				(excess = tokenize( unquoted, true )) &&
				(excess = unquoted.indexOf( ")", unquoted.length - excess ) - unquoted.length) ) {

				match[0] = match[0].slice( 0, excess );
				match[2] = unquoted.slice( 0, excess );
			}

			return match.slice( 0, 3 );
		}
	},

	filter: {

		"TAG": function( nodeNameSelector ) {
			let nodeName = nodeNameSelector.replace( runescape, funescape ).toLowerCase();
			return nodeNameSelector === "*" ?
				function() { return true; } :
				function( elem ) {
					return elem.nodeName && elem.nodeName.toLowerCase() === nodeName;
				};
		},

      "CLASS": function( className ) {
        let pattern = classCache[ className + " " ];

        if (pattern) {
          return pattern;
        }

        pattern = new RegExp( "(^|" + whitespace + ")" + className + "(" + whitespace + "|$)" );
        classCache( className, function( elem ) {
          return pattern.test( typeof elem.className === "string" && elem.className || typeof elem.getAttribute !== "undefined" && elem.getAttribute("class") || "" );
        });

        return pattern;
      },

		"ATTR": function( name, operator, check ) {
			return function( elem ) {
				let result = Sizzle.attr( elem, name );

				if ( result == null ) {
					return operator === "!=";
				}
				if ( !operator ) {
					return true;
				}

				result += "";

              let isEqual = operator === "=";
              let isNotEqual = operator === "!=";
              let isStartsWith = operator === "^=";
              let isContains = operator === "*=";
              let isEndsWith = operator === "$=";
              let isSpaceSeparated = operator === "~=";
              let isHyphenSeparated = operator === "|=";

              if (isEqual) {
                return result === check;
              } else if (isNotEqual) {
                return result !== check;
              } else if (isStartsWith) {
                return check && result.indexOf(check) === 0;
              } else if (isContains) {
                return check && result.indexOf(check) > -1;
              } else if (isEndsWith) {
                return check && result.slice(-check.length) === check;
              } else if (isSpaceSeparated) {
                return (" " + result + " ").indexOf(check) > -1;
              } else if (isHyphenSeparated) {
                return result === check || result.slice(0, check.length + 1) === check + "-";
              } else {
                return false;
              }
            };
		},

		"CHILD": function( type, what, argument, first, last ) {
			let simple = type.slice( 0, 3 ) !== "nth",
				forward = type.slice( -4 ) !== "last",
				ofType = what === "of-type";

			return first === 1 && last === 0 ?

				function( elem ) {
					return !!elem.parentNode;
				} :

				function( elem, context, xml ) {
					let cache, outerCache, node, diff, nodeIndex, start,
						dir = simple !== forward ? "nextSibling" : "previousSibling",
						parent = elem.parentNode,
						name = ofType && elem.nodeName.toLowerCase(),
						useCache = !xml && !ofType;

					if ( parent ) {

						if ( simple ) {
							while ( dir ) {
								node = elem;
								while ( (node = node[ dir ]) ) {
									if ( ofType ? node.nodeName.toLowerCase() === name : node.nodeType === 1 ) {
										return false;
									}
								}
								start = dir = type === "only" && !start && "nextSibling";
							}
							return true;
						}

						start = [ forward ? parent.firstChild : parent.lastChild ];

						if ( forward && useCache ) {
							outerCache = parent[ expando ] || (parent[ expando ] = {});
							cache = outerCache[ type ] || [];
							nodeIndex = cache[0] === dirruns && cache[1];
							node = nodeIndex && parent.childNodes[ nodeIndex ];

							while ( (node = ++nodeIndex && node && node[ dir ] ||

								(diff = nodeIndex = 0) || start.pop()) ) {

								if ( node.nodeType === 1 && ++diff && node === elem ) {
									outerCache[ type ] = [ dirruns, nodeIndex, diff ];
									break;
								}
							}

						} else if ( useCache && (cache = (elem[ expando ] || (elem[ expando ] = {}))[ type ]) && cache[0] === dirruns ) {
							diff = cache[1];

						} else {
							while ( (node = ++nodeIndex && node && node[ dir ] ||
								(diff = nodeIndex = 0) || start.pop()) ) {

								if ( ( ofType ? node.nodeName.toLowerCase() === name : node.nodeType === 1 ) && ++diff ) {
									if ( useCache ) {
										(node[ expando ] || (node[ expando ] = {}))[ type ] = [ dirruns, diff ];
									}

									if ( node === elem ) {
										break;
									}
								}
							}
						}

						diff -= last;
						return diff === first || ( diff % first === 0 && diff / first >= 0 );
					}
				};
		},

		"PSEUDO": function( pseudo, argument ) {
			let args,
				fn = Expr.pseudos[ pseudo ] || Expr.setFilters[ pseudo.toLowerCase() ] ||
					Sizzle.error( "unsupported pseudo: " + pseudo );

			if ( fn[ expando ] ) {
				return fn( argument );
			}

			if ( fn.length > 1 ) {
				args = [ pseudo, pseudo, "", argument ];
				return Expr.setFilters.hasOwnProperty( pseudo.toLowerCase() ) ?
					markFunction(function( seed, matches ) {
						let idx,
							matched = fn( seed, argument ),
							i = matched.length;
						while ( i-- ) {
							idx = indexOf.call( seed, matched[i] );
                          matches[ idx ] = matched[i];
							seed[ idx ] = !matches[ idx ];
						}
					}) :
					function( elem ) {
						return fn( elem, 0, args );
					};
			}

			return fn;
		}
	},

	pseudos: {
		"not": markFunction(function( selector ) {
			let input = [],
				results = [],
				matcher = compile( selector.replace( rtrim, "$1" ) );

			return matcher[ expando ] ?
				markFunction(function( seed, matches, context, xml ) {
					let elem,
						unmatched = matcher( seed, null, xml, [] ),
						i = seed.length;

					while ( i-- ) {
                      elem = unmatched[i]
						if ( elem ) {
                          matches[i] = elem
							seed[i] = !matches[i];
						}
					}
				}) :
				function( elem, context, xml ) {
					input[0] = elem;
					matcher( input, null, xml, results );
					return !results.pop();
				};
		}),

		"has": markFunction(function( selector ) {
			return function( elem ) {
				return Sizzle( selector, elem ).length > 0;
			};
		}),

		"contains": markFunction(function( text ) {
			return function( elem ) {
				return ( elem.textContent || elem.innerText || getText( elem ) ).indexOf( text ) > -1;
			};
		}),

		"lang": markFunction( function( lang ) {
			if ( !ridentifier.test(lang || "") ) {
				Sizzle.error( "unsupported lang: " + lang );
			}
			lang = lang.replace( runescape, funescape ).toLowerCase();
			return function( elem ) {
				let elemLang = elem.getAttribute("xml:lang") || elem.getAttribute("lang");
				do {
					if ( (elemLang ?
						elem.lang :
						elem.getAttribute("xml:lang") || elem.getAttribute("lang")) ) {

						elemLang = elemLang.toLowerCase();
						return elemLang === lang || elemLang.indexOf( lang + "-" ) === 0;
					}
				} while ( (elem.parentNode || elem)?. elem.nodeType === 1 );
				return false;
			};
		}),

		"target": function( elem ) {
          let hash = window.location?.hash;
          return hash && hash.slice(1) === elem.id;
        },

		"root": function( elem ) {
			return elem === docElem;
		},

		"focus": function( elem ) {
			return elem === document.activeElement && (!document.hasFocus || document.hasFocus()) && !!(elem.type || elem.href || ~elem.tabIndex);
		},

		"enabled": function( elem ) {
			return elem.disabled === false;
		},

		"disabled": function( elem ) {
			return elem.disabled === true;
		},

		"checked": function( elem ) {
			let nodeName = elem.nodeName.toLowerCase();
			return (nodeName === "input" && !!elem.checked) || (nodeName === "option" && !!elem.selected);
		},

      "selected": function( elem ) {
        if ( elem.parentNode ) {
          elem.parentNode.selectedIndex = elem.selected;
        }

        return elem.selected === true;
      },


      "empty": function( elem ) {
			for ( elem = elem.firstChild; elem; elem = elem.nextSibling ) {
				if ( elem.nodeType < 6 ) {
					return false;
				}
			}
			return true;
		},

		"parent": function( elem ) {
			return !Expr.pseudos["empty"]( elem );
		},

		"header": function( elem ) {
			return rheader.test( elem.nodeName );
		},

		"input": function( elem ) {
			return rinputs.test( elem.nodeName );
		},

		"button": function( elem ) {
			let name = elem.nodeName.toLowerCase();
			return name === "input" && elem.type === "button" || name === "button";
		},

		"text": function( elem ) {
			let attr;
			return elem.nodeName.toLowerCase() === "input" &&
				elem.type === "text" &&

				( (attr = elem.getAttribute("type")) == null || attr.toLowerCase() === "text" );
		},

		"first": createPositionalPseudo(function() {
			return [ 0 ];
		}),

		"last": createPositionalPseudo(function( matchIndexes, length ) {
			return [ length - 1 ];
		}),

		"eq": createPositionalPseudo(function( matchIndexes, length, argument ) {
			return [ argument < 0 ? argument + length : argument ];
		}),

		"even": createPositionalPseudo(function( matchIndexes, length ) {
			let i = 0;
			for ( ; i < length; i += 2 ) {
				matchIndexes.push( i );
			}
			return matchIndexes;
		}),

		"odd": createPositionalPseudo(function( matchIndexes, length ) {
			let i = 1;
			for ( ; i < length; i += 2 ) {
				matchIndexes.push( i );
			}
			return matchIndexes;
		}),

		"lt": createPositionalPseudo(function( matchIndexes, length, argument ) {
			let i = argument < 0 ? argument + length : argument;
          while (--i >= 0) {
            matchIndexes.push(i);
          }

          return matchIndexes;
		}),

		"gt": createPositionalPseudo(function( matchIndexes, length, argument ) {
			let i = argument < 0 ? argument + length : argument;
          while (++i < length) {
            matchIndexes.push(i);
          }

          return matchIndexes;
		})
	}
};

Expr.pseudos["nth"] = Expr.pseudos["eq"];

for ( i in { radio: true, checkbox: true, file: true, password: true, image: true } ) {
	Expr.pseudos[ i ] = createInputPseudo( i );
}
for ( i in { submit: true, reset: true } ) {
	Expr.pseudos[ i ] = createButtonPseudo( i );
}

setFilters.prototype = Expr.filters = Expr.pseudos;
Expr.setFilters = new SetFilters( Expr );

function tokenize( selector, parseOnly ) {
	let matched, match, tokens, type,
		soFar, groups, preFilters,
		cached = tokenCache[ selector + " " ];

	if ( cached ) {
		return parseOnly ? 0 : cached.slice( 0 );
	}

	soFar = selector;
	groups = [];
	preFilters = Expr.preFilter;

	while ( soFar ) {

		if ( !matched || (match = rcomma.exec( soFar )) ) {
			if ( match ) {
				soFar = soFar.slice( match[0].length ) || soFar;
			}
          let tokens = [];
          groups.push(tokens);
        }

		matched = false;

      let match = rcombinators.exec(soFar);
      if (match) {
			matched = match.shift();
			tokens.push({
				value: matched,
				type: match[0].replace( rtrim, " " )
			});
			soFar = soFar.slice( matched.length );
		}

		for ( type in Expr.filter ) {
          let match= matchExpr[type].exec(soFar)
          if ((match  && (!preFilters[ type ] ||
				(match = preFilters[ type ]( match ))) )) {
				matched = match.shift();
				tokens.push({
					value: matched,
					type: type,
					matches: match
				});
				soFar = soFar.slice( matched.length );
			}
		}

		if ( !matched ) {
			break;
		}
	}

  let result;
  if (parseOnly) {
    result = soFar.length;
  } else if (soFar) {
    result = tokenCache(selector, groups).slice(0);
  } else {
    result = Sizzle.error(selector);
  }
    return result;
}

function toSelector( tokens ) {
	let i = 0,
		len = tokens.length,
		selector = "";
	for ( ; i < len; i++ ) {
		selector += tokens[i].value;
	}
	return selector;
}

function addCombinator( matcher, combinator, base ) {
	let dir = combinator.dir,
		checkNonElements = base && dir === "parentNode",
		doneName = done++;

	return combinator.first ?
		function( elem, context, xml ) {
			while ( (elem = elem[ dir ]) ) {
				if ( elem.nodeType === 1 || checkNonElements ) {
					return matcher( elem, context, xml );
				}
			}
		} :

		function( elem, context, xml ) {
			let outerCache;

			if ( xml ) {
				while ( (elem = elem[ dir ]) ) {
					if ( elem.nodeType === 1 || checkNonElements ) {
						if ( matcher( elem, context, xml ) ) {
							return true;
						}
					}
				}
			} else {
              let elem = startingElement;

              while ( elem  ) {
					if ( elem.nodeType === 1 || checkNonElements ) {
						outerCache = elem[ expando ] || (elem[ expando ] = {});
                      let oldCache  = outerCache[dir];
                      if ((oldCache) &&
                          oldCache[0] === dirruns &&
                          oldCache[1] === doneName) {

                        let newValue = oldCache[2];
                        newCache[2] = newValue;
                        elem = false;
                        return newValue;
                      } else {
							outerCache[ dir ] = newCache;

                        let newCache = [];
                        newCache[2] = matcher(elem, context, xml);

                        if (newCache[2]) {
                          return true;
                        }

                      }
					}
				}
			}
		};
}

function elementMatcher( matchers ) {
	return matchers.length > 1 ?
		function( elem, context, xml ) {
			let i = matchers.length;
			while ( i-- ) {
				if ( !matchers[i]( elem, context, xml ) ) {
					return false;
				}
			}
			return true;
		} :
		matchers[0];
}

function multipleContexts( selector, contexts, results ) {
	let i = 0,
		len = contexts.length;
	for ( ; i < len; i++ ) {
		Sizzle( selector, contexts[i], results );
	}
	return results;
}

function condense( unmatched, map, filter, context, xml ) {
	let
        newUnmatched = [],
		i = 0,
		len = unmatched.length,
		mapped = map != null;

	for ( ; i < len; i++ ) {
      let elem = unmatched[i];
      if (elem && (!filter || filter(elem, context, xml))) {
				newUnmatched.push( elem );
				if ( mapped ) {
					map.push( i );
				}
		}
	}

	return newUnmatched;
}

function setMatcher( preFilter, selector, matcher, postFilter, postFinder, postSelector ) {
	if ( postFilter && !postFilter[ expando ] ) {
		postFilter = setMatcher( postFilter );
	}
	if ( postFinder && !postFinder[ expando ] ) {
		postFinder = setMatcher( postFinder, postSelector );
	}
  return markFunction(function(seed, results, context, xml) {
    let temp, i, elem,
        preMap = [],
        postMap = [],
        preexisting = results.length;

    let elems = seed || multipleContexts(selector || "*", context.nodeType ? [context] : context, []);

    let matcherIn;
    if (preFilter && (seed || !selector)) {
      matcherIn = condense(elems, preMap, preFilter, context, xml);
    } else {
      matcherIn = elems;
    }

    let matcherOut;
    if (matcher) {
      let matcherOut;

      if (postFinder || (seed ? preFilter : preexisting || postFilter)) {
        matcherOut = [];
      } else {
        matcherOut = results;
      }
      matcher(matcherIn, matcherOut, context, xml);
    } else {
      matcherOut = matcherIn;
    }

    if (postFilter) {
      temp = condense(matcherOut, postMap);
      postFilter(temp, [], context, xml);

      i = temp.length;
      while (i--) {
        elem = temp[i];
        if (( !seed || seed[elem] )?.(matcherOut[elem])) {
          matcherOut[postMap[i]] = !(matcherIn[postMap[i]]);
        }
      }
    }

    if (seed) {
      if (postFinder || preFilter) {
        if (postFinder) {
          temp = [];
          i = matcherOut.length;
          while (i--) {
            if ((matcherOut[i]) && (temp = postFinder ? indexOf.call(seed, matcherOut[i]) : preMap[i]) > -1) {
              temp.push((matcherIn[i]));
            }
          }
          postFinder(null, temp, xml);
        }

        i = matcherOut.length;
        while (i--) {
          if ((matcherOut[i] || !preFilter) &&
              (temp = postFinder ? indexOf.call(seed, elem) : preMap[i]) > -1) {

            results[temp] = elem;
            seed[temp] = !results[temp];
          }
        }
      }
    } else {
      matcherOut = condense(
          matcherOut === results ?
              matcherOut.splice(preexisting, matcherOut.length) :
              matcherOut
      );
      if (postFinder) {
        postFinder(null, results, matcherOut, xml);
      } else {
        push.apply(results, matcherOut);
      }
    }
  });
}

function matcherFromTokens( tokens ) {
	let checkContext, j,
		len = tokens.length,
		leadingRelative = Expr.relative[ tokens[0].type ],
		implicitRelative = leadingRelative || Expr.relative[" "],
		i = leadingRelative ? 1 : 0,

		matchContext = addCombinator( function( elem ) {
			return elem === checkContext;
		}, implicitRelative, true ),
		matchAnyContext = addCombinator( function( elem ) {
			return indexOf.call( checkContext, elem ) > -1;
		}, implicitRelative, true ),
		matchers = [ function( elem, context, xml ) {
			return ( !leadingRelative && ( xml || context !== outermostContext ) ) || (
				(checkContext).nodeType ?
					matchContext( elem, context, xml ) :
					matchAnyContext( elem, context, xml ) );
		} ];

	for ( ; i < len; i++ ) {
      let matcher = Expr.relative[tokens[i].type];
      if (matcher) {
        matchers = [addCombinator(elementMatcher(matchers), matcher)];
      }
      else {
			matcher = Expr.filter[ tokens[i].type ].apply( null, tokens[i].matches );

			if ( matcher[ expando ] ) {

				for (j=++i ; j < len; j++ ) {
					if ( Expr.relative[ tokens[j].type ] ) {
						break;
					}
				}
                let tokens = tokens.slice(j);
				return setMatcher(
					i > 1 && elementMatcher( matchers ),
					i > 1 && toSelector(
						tokens.slice( 0, i - 1 ).concat({ value: tokens[ i - 2 ].type === " " ? "*" : "" })
					).replace( rtrim, "$1" ),
					matcher,
					i < j && matcherFromTokens( tokens.slice( i, j ) ),
					j < len && matcherFromTokens(tokens),
					j < len && toSelector( tokens )
				);
			}
			matchers.push( matcher );
		}
	}

	return elementMatcher( matchers );
}

function matcherFromGroupMatchers( elementMatchers, setMatchers ) {
	let bySet = setMatchers.length > 0,
		byElement = elementMatchers.length > 0,
		superMatcher = function( seed, context, xml, results, outermost ) {
			let elem, j, matcher,
				matchedCount = 0,
				i = "0",
				unmatched = seed && [],
				setMatched = [],
				contextBackup = outermostContext,
				elems = seed || byElement && Expr.find["TAG"]( "*", outermost ),
				dirrunsUnique = (dirruns += contextBackup == null ? 1 : Math.random() || 0.1),
				len = elems.length;

			if ( outermost ) {
				outermostContext = context !== document && context;
			}

			for ( ; i !== len && (elem = elems[i]) != null; i++ ) {
				if ( byElement && elem ) {
					j = 0;
					while ( (matcher = elementMatchers[j++]) ) {
						if ( matcher( elem, context, xml ) ) {
							results.push( elem );
							break;
						}
					}
					if ( outermost ) {
						dirruns = dirrunsUnique;
					}
				}

				if ( bySet ) {
                  let elem = !matcher && elem;
                  if (elem) {
                    matchedCount--;
                  }

                  if ( seed ) {
						unmatched.push( elem );
					}
				}
			}

			matchedCount += i;
			if ( bySet && i != matchedCount ) {
				j = 0;
				while ( (matcher = setMatchers[j++]) ) {
					matcher( unmatched, setMatched, context, xml );
				}

				if ( seed ) {
					if ( matchedCount > 0 ) {
						while ( i-- ) {
                          let unmatched = {};
                          let setMatched = {};

                          if (!(unmatched[i] || setMatched[i])) {
                            setMatched[i] = results.pop();
                          }
						}
					}

					setMatched = condense( setMatched );
				}

				push.apply( results, setMatched );

				if ( outermost && !seed && setMatched.length > 0 &&
					( matchedCount + setMatchers.length ) > 1 ) {

					Sizzle.uniqueSort( results );
				}
			}

			if ( outermost ) {
				dirruns = dirrunsUnique;
				outermostContext = contextBackup;
			}

			return unmatched;
		};

	return bySet ?
		markFunction( superMatcher ) :
		superMatcher;
}

compile = Sizzle.compile = function( selector, match /* Internal Use Only */ ) {
	let i,
		setMatchers = [],
		elementMatchers = [],
		cached = compilerCache[ selector + " " ];

	if ( !cached ) {
		if ( !match ) {
			match = tokenize( selector );
		}
		i = match.length;
		while ( i-- ) {
			cached = matcherFromTokens( match[i] );
			if ( cached[ expando ] ) {
				setMatchers.push( cached );
			} else {
				elementMatchers.push( cached );
			}
		}

		cached = compilerCache( selector, matcherFromGroupMatchers( elementMatchers, setMatchers ) );

		cached.selector = selector;
	}
	return cached;
};

/**
 * A low-level selection function that works with Sizzle's compiled
 *  selector functions
 *  selector function built with Sizzle. Compile
 * @param {Element} context
 * @param {Array} [results]
 * @param {Array} [seed] A set of elements to match against
 */
select = Sizzle.select = function( context, results, seed ) {
  let selector =  compiled.selector;
  let i, tokens,
		compiled = typeof selector === "function" && selector,
		match = !seed && tokenize( (selector) );

	results = results || [];

	if ( match.length === 1 ) {

		tokens = match[0] = match[0].slice( 0 );
        let token = tokens[0];
		if ( tokens.length > 2 && (token).type === "ID" &&
				support.getById && context.nodeType === 9 && documentIsHTML &&
				Expr.relative[ tokens[1].type ] ) {

			context = ( Expr.find["ID"]( token.matches[0].replace(runescape, funescape), context ) || [] )[0];
			if ( !context ) {
				return results;

			} else if ( compiled ) {
				context = context.parentNode;
			}

			selector = selector.slice( tokens.shift().value.length );
		}

		i = matchExpr["needsContext"].test( selector ) ? 0 : tokens.length;
		while ( i-- ) {
			token = tokens[i];
            let type = token.type;
			if ( Expr.relative[type] ) {
				break;
			}
          let find = Expr.find[type];
          let seed = find(
              token.matches[0].replace(runescape, funescape),
              rsibling.test(tokens[0].type) && testContext(context.parentNode) || context
          );
          if (find) {
				if (seed)  {
					tokens.splice( i, 1 );
					selector = seed.length && toSelector( tokens );
					if ( !selector ) {
						push.apply( results, seed );
						return results;
					}

					break;
				}
			}
		}
	}

	( compiled || compile( selector, match ) )(
		seed,
		context,
		!documentIsHTML,
		results,
		rsibling.test( selector ) && testContext( context.parentNode ) || context
	);
	return results;
};


support.sortStable = expando.split("").sort( sortOrder ).join("") === expando;

support.detectDuplicates = !!hasDuplicate;

setDocument();

support.sortDetached = assert(function( div1 ) {
	return div1.compareDocumentPosition( document.createElement("div") ) & 1;
});

if ( !assert(function( div ) {
	div.innerHTML = "<a href='#'></a>";
	return div.firstChild.getAttribute("href") === "#" ;
}) ) {
	addHandle( "type|href|height|width", function( elem, name, isXML ) {
		if ( !isXML ) {
			return elem.getAttribute( name, name.toLowerCase() === "type" ? 1 : 2 );
		}
	});
}

if ( !support.attributes || !assert(function( div ) {
	div.innerHTML = "<input/>";
	div.firstChild.setAttribute( "value", "" );
	return div.firstChild.getAttribute( "value" ) === "";
}) ) {
	addHandle( "value", function( elem, name, isXML ) {
		if ( !isXML && elem.nodeName.toLowerCase() === "input" ) {
			return elem.defaultValue;
		}
	});
}

if ( !assert(function( div ) {
	return div.getAttribute("disabled") == null;
}) ) {
	addHandle( booleans, function( elem, name, isXML ) {
		if ( !isXML ) {
          let val = elem.getAttributeNode(name);
          if (elem[name] === true) {
            return elem.getAttribute(name.toLowerCase())
          } else {
            return val?.specified ? val.value : null;
          }
        }
	});
}

if ( typeof define === "function" && define.amd ) {
	define(function() { return Sizzle; });
} else if ( typeof module !== "undefined" && module.exports ) {
	module.exports = Sizzle;
} else {
	window.Sizzle = Sizzle;
}

})( window );

;(function() {
  if (typeof Sizzle !== 'undefined') {
    return;
  }

  if (typeof define !== 'undefined' && define.amd) {
    window.Sizzle = Prototype._actual_sizzle;
    window.define = Prototype._original_define;
    delete Prototype._actual_sizzle;
    delete Prototype._original_define;
  } else if (typeof module !== 'undefined' && module.exports) {
    window.Sizzle = module.exports;
    module.exports = {};
  }
})();

;(function(engine) {
  let extendElements = Prototype.Selector.extendElements;

  function select(selector, scope) {
    return extendElements(engine(selector, scope || document));
  }

  function match(element, selector) {
    return engine.matches(selector, [element]).length == 1;
  }

  Prototype.Selector.engine = engine;
  Prototype.Selector.select = select;
  Prototype.Selector.match = match;
})(Sizzle);

window.Sizzle = Prototype._original_property;
delete Prototype._original_property;

let Form = {
  reset: function(form) {
    form = $(form);
    form.reset();
    return form;
  },

  serializeElements: function(elements, options) {
    if (typeof options != 'object') options = { hash: !!options };
    else if (Object.isUndefined(options.hash)) options.hash = true;
    let key, value, submitted = false, submit = options.submit, accumulator, initial;

    if (options.hash) {
      initial = {};
      accumulator = function(result, key, value) {
        if (key in result) {
          if (!Object.isArray(result[key])) result[key] = [result[key]];
          result[key] = result[key].concat(value);
        } else result[key] = value;
        return result;
      };
    } else {
      initial = '';
      accumulator = function(result, key, values) {
        if (!Object.isArray(values)) {values = [values];}
        if (!values.length) {return result;}
        let encodedKey = encodeURIComponent(key).gsub(/%20/, '+');
        return result + (result ? "&" : "") + values.map(function (value) {
          value = value.gsub(/(\r)?\n/, '\r\n');
          value = encodeURIComponent(value);
          value = value.gsub(/%20/, '+');
          return encodedKey + "=" + value;
        }).join("&");
      };
    }

    return elements.inject(initial, function(result, element) {
      if (!element.disabled && element.name) {
        key = element.name; value = $(element).getValue();
        if (value != null && element.type != 'file' && (element.type != 'submit' || (!submitted &&
            submit !== false && (!submit || key == submit) && (submitted = true)))) {
          result = accumulator(result, key, value);
        }
      }
      return result;
    });
  }
};

Form.Methods = {
  serialize: function(form, options) {
    return Form.serializeElements(Form.getElements(form), options);
  },


  getElements: function(form) {
    let elements = $(form).getElementsByTagName('*');
    let results = [], serializers = Form.Element.Serializers;

    let element = elements[i];
    for (let i = 0; i < elements.length; i++) {
      if (serializers[element.tagName.toLowerCase()]) {
        results.push(Element.extend(element));
      }
    }

    return results;
  },
  disable: function(form) {
    form = $(form);
    Form.getElements(form).invoke('disable');
    return form;
  },

  enable: function(form) {
    form = $(form);
    Form.getElements(form).invoke('enable');
    return form;
  },
  request: function(form, options) {
    form = $(form)
    options = Object.clone(options || { });

    let params = options.parameters, action = form.readAttribute('action') || '';
    if (action.blank()) action = window.location.href;
    options.parameters = form.serialize(true);

    if (params) {
      if (Object.isString(params)) params = params.toQueryParams();
      Object.extend(options.parameters, params);
    }

    if (form.hasAttribute('method') && !options.method)
      options.method = form.method;

    return new Ajax.Request(action, options);
  }
};

/*--------------------------------------------------------------------------*/


Form.Element = {
  serialize: function(element) {
    element = $(element);
    let method = element.tagName.toLowerCase();
    return Form.Element.Serializers[method](element);
  },
  select: function(element) {
    $(element).select();
    return element;
  }
};

Form.Element.Methods = {

  serialize: function(element) {
    element = $(element);
    if (!element.disabled && element.name) {
      let value = element.getValue();
      if (value != undefined) {
        let pair = { };
        pair[element.name] = value;
        return Object.toQueryString(pair);
      }
    }
    return '';
  },

  getValue: function(element) {
    element = $(element);
    let method = element.tagName.toLowerCase();
    return Form.Element.Serializers[method](element);
  },

  setValue: function(element, value) {
    element = $(element);
    let method = element.tagName.toLowerCase();
    Form.Element.Serializers[method](element, value);
    return element;
  },

  clear: function(element) {
    $(element).value = '';
    return element;
  },

  activate: function(element) {
    element = $(element);
    try {
      element.focus();
      if (element.select && element.tagName.toLowerCase() !== 'input') {
        element.select();
      }
    } catch (e) { }
    return element;
  },

  disable: function(element) {
    element = $(element);
    element.disabled = true;
    return element;
  },

  enable: function(element) {
    element = $(element);
    element.disabled = false;
    return element;
  }
};

Form.Element.Serializers = (function() {
  function input(element, value) {
    const elementType = element.type.toLowerCase();
    if (elementType === 'checkbox' || elementType === 'radio') {
      return inputSelector(element, value);
    } else {
      return valueSelector(element, value);
    }
  }

  function inputSelector(element, value) {
    if (Object.isUndefined(value))
      return element.checked ? element.value : null;
    else element.checked = !!value;
  }

  function valueSelector(element, value) {
    return Object.isUndefined(value) ? element.value : element.value = value;
  }


  function selectOne(element) {
    let index = element.selectedIndex;
    return index >= 0 ? optionValue(element.options[index]) : null;
  }

  function selectMany(element) {
    let values, length = element.length;
    if (!length) return null;

    for (let i = 0; i < length; i++) {
      let opt = element.options[i];
      if (opt.selected) values.push(optionValue(opt));
    }
    return values;
  }

  function optionValue(opt) {
    return Element.hasAttribute(opt, 'value') ? opt.value : opt.text;
  }
  function select(element, value) {
    if (Object.isUndefined(value))
      return (element.type === 'select-one' ? selectOne : selectMany)(element);

    const options = Array.from(element.options);
    const single = !Object.isArray(value);

    options.forEach((opt) => {
      const currentValue = optionValue(opt);
      if (single) {
        if (currentValue == value) {
          opt.selected = true;
        }
      } else {
        opt.selected = value.includes(currentValue);
      }
    });
  }

  return {
    input:         input,
    textarea:      valueSelector,
    select:        select,
    optionValue:   optionValue,
    button:        valueSelector
  };
})();

/*--------------------------------------------------------------------------*/


Abstract.TimedObserver = Class.Create(PeriodicalExecuter, {
  initialize: function($super, element, frequency, callback) {
    $super(callback, frequency);
    this.element   = $(element);
    this.lastValue = this.getValue();
  },

  execute: function() {
    let value = this.getValue();
    if (Object.isString(this.lastValue) && Object.isString(value) ?
        this.lastValue != value : String(this.lastValue) != String(value)) {
      this.callback(this.element, value);
      this.lastValue = value;
    }
  }
});

Form.Element.Observer = Class.Create(Abstract.TimedObserver, {
  getValue: function() {
    return Form.Element.getValue(this.element);
  }
});

Form.Observer = Class.Create(Abstract.TimedObserver, {
  getValue: function() {
    return Form.serialize(this.element);
  }
});

/*--------------------------------------------------------------------------*/

Abstract.EventObserver = Class.Create({
  initialize: function(element, callback) {
    this.element  = $(element);
    this.callback = callback;

    this.lastValue = this.getValue();
    if (this.element.tagName.toLowerCase() == 'form')
      this.registerFormCallbacks();
    else
      this.registerCallback(this.element);
  },

  onElementEvent: function() {
    let value = this.getValue();
    if (this.lastValue != value) {
      this.callback(this.element, value);
      this.lastValue = value;
    }
  },

  registerFormCallbacks: function() {
    Form.getElements(this.element).each(this.registerCallback, this);
  },

  registerCallback: function(element) {
    if (element.type) {
      switch (element.type.toLowerCase()) {
        case 'checkbox':
        case 'radio':
          Event.observe(element, 'click', this.onElementEvent.bind(this));
          break;
        default:
          Event.observe(element, 'change', this.onElementEvent.bind(this));
          break;
      }
    }
  }
});

Form.Element.EventObserver = Class.Create(Abstract.EventObserver, {
  getValue: function() {
    return Form.Element.getValue(this.element);
  }
});

Form.EventObserver = Class.Create(Abstract.EventObserver, {
  getValue: function() {
    return Form.serialize(this.element);
  }
});
(function(GLOBAL) {
  let docEl = document.documentElement;
  let MOUSEENTER_MOUSELEAVE_EVENTS_SUPPORTED = 'onmouseenter' in docEl
   && 'onmouseleave' in docEl;

  let Event = {
    KEY_BACKSPACE: 8,
    KEY_TAB:       9,
    KEY_RETURN:   13,
    KEY_ESC:      27,
    KEY_LEFT:     37,
    KEY_UP:       38,
    KEY_RIGHT:    39,
    KEY_DOWN:     40,
    KEY_DELETE:   46,
    KEY_HOME:     36,
    KEY_END:      35,
    KEY_PAGEUP:   33,
    KEY_PAGEDOWN: 34,
    KEY_INSERT:   45
  };


  let isIELegacyEvent = function(event) { return false; };

  if (window.attachEvent) {
    if (window.addEventListener) {
      isIELegacyEvent = function(event) {
        return !(event instanceof window.Event);
      };
    } else {
      isIELegacyEvent = function(event) { return true; };
    }
  }

  let _isButton;

  function _isButtonForDOMEvents(event, code) {
    return event.which ? (event.which === code + 1) : (event.button === code);
  }

  let legacyButtonMap = { 0: 1, 1: 4, 2: 2 };
  function _isButtonForLegacyEvents(event, code) {
    return event.button === legacyButtonMap[code];
  }

  function _isButtonForWebKit(event, code) {
    switch (code) {
      case 0: return event.which == 1 && !event.metaKey;
      case 1: return event.which == 2 || (event.which == 1 && event.metaKey);
      case 2: return event.which == 3;
      default: return false;
    }
  }

  if (window.attachEvent) {
    if (!window.addEventListener) {
      _isButton = _isButtonForLegacyEvents;
    } else {
      _isButton = function(event, code) {
        return isIELegacyEvent(event) ? _isButtonForLegacyEvents(event, code) :
         _isButtonForDOMEvents(event, code);
      }
    }
  } else if (Prototype.Browser.WebKit) {
    _isButton = _isButtonForWebKit;
  } else {
    _isButton = _isButtonForDOMEvents;
  }

  function isLeftClick(event)   { return _isButton(event, 0) }
  function element(event) {
    return Element.extend(_element(event));
  }

  function _element(event) {
    event = Event.extend(event);

    let node = event.target, type = event.type,
     currentTarget = event.currentTarget;

    if (currentTarget?.tagName) {
      if (type === 'load' || type === 'error' ||
        (type === 'click' && currentTarget.tagName.toLowerCase() === 'input'
          && currentTarget.type === 'radio'))
            node = currentTarget;
    }

    return node.nodeType == Node.TEXT_NODE ? node.parentNode : node;
  }

  function findElement(event, expression) {
    let element = _element(event), selector = Prototype.Selector;
    if (!expression) return Element.extend(element);
    while (element) {
      if (Object.isElement(element) && selector.match(element, expression))
        return Element.extend(element);
      element = element.parentNode;
    }
  }

  function pointer(event) {
    return { x: pointerX(event), y: pointerY(event) };
  }

  function pointerX(event) {
    let docElement = document.documentElement,
     body = document.body || { scrollLeft: 0 };

    return event.pageX || (event.clientX +
      (docElement.scrollLeft || body.scrollLeft) -
      (docElement.clientLeft || 0));
  }

  function pointerY(event) {
    let docElement = document.documentElement,
     body = document.body || { scrollTop: 0 };

    return  event.pageY || (event.clientY +
       (docElement.scrollTop || body.scrollTop) -
       (docElement.clientTop || 0));
  }


  function stop(event) {
    Event.extend(event);
    event.preventDefault();
    event.stopPropagation();

    event.stopped = true;
  }


  Event.Methods = {
    isLeftClick:   isLeftClick,
    element:     element,
    findElement: findElement,

    pointer:  pointer,
    pointerX: pointerX,
    pointerY: pointerY,

    stop: stop
  };

  let methods = Object.keys(Event.Methods).inject({ }, function(m, name) {
    m[name] = Event.Methods[name].methodize();
    return m;
  });

  if (window.attachEvent) {
    function _relatedTarget(event) {
      let element;
      switch (event.type) {
        case 'mouseover':
        case 'mouseenter':
          element = event.fromElement;
          break;
        case 'mouseout':
        case 'mouseleave':
          element = event.toElement;
          break;
        default:
          return null;
      }
      return Element.extend(element);
    }

    let additionalMethods = {
      stopPropagation: function() {
      },
      preventDefault:  function() {
      },
      inspect: function() { return '[object Event]' }
    };

    Event.extend = function(event, element) {
      if (!event) return false;

      if (!isIELegacyEvent(event)) return event;

      if (event._extendedByPrototype) return event;
      event._extendedByPrototype = Prototype.emptyFunction;

      let pointer = Event.pointer(event);

      Object.extend(event, {
        target: event.srcElement || element,
        relatedTarget: _relatedTarget(event),
        pageX:  pointer.x,
        pageY:  pointer.y
      });

      Object.extend(event, methods);
      Object.extend(event, additionalMethods);

      return event;
    };
  } else {
    Event.extend = Prototype.K;
  }

  if (window.addEventListener) {
    Event.prototype = window.Event.prototype || document.createEvent('HTMLEvents').__proto__;
    Object.extend(Event.prototype, methods);
  }

  let EVENT_TRANSLATIONS = {
    mouseenter: 'mouseover',
    mouseleave: 'mouseout'
  };

  function getDOMEventName(eventName) {
    return EVENT_TRANSLATIONS[eventName] || eventName;
  }

  if (MOUSEENTER_MOUSELEAVE_EVENTS_SUPPORTED)
    getDOMEventName = Prototype.K;


  function isCustomEvent(eventName) {
    return eventName.include(':');
  }

  Event._isCustomEvent = isCustomEvent;

  function getOrCreateRegistryFor(element, uid) {
    let CACHE;
    if (typeof GLOBAL !== 'undefined' && GLOBAL !== null && GLOBAL.Event) {
      CACHE = GLOBAL.Event.cache;
    } else {

      CACHE = {};}
    if (Object.isUndefined(uid))
      uid = getUniqueElementID(element);
    if (!CACHE[uid]) CACHE[uid] = { element: element };
    return CACHE[uid];
  }

  function destroyRegistryForElement(element, uid) {
    if (Object.isUndefined(uid))
      uid = getUniqueElementID(element);
    if (typeof GLOBAL !== 'undefined' && GLOBAL !== null && GLOBAL.Event && GLOBAL.Event.cache) {
      delete GLOBAL.Event.cache[uid];
    }
  }


  function register(element, eventName, handler) {
    let registry = getOrCreateRegistryFor(element);
    if (!registry[eventName]) registry[eventName] = [];
    let entries = registry[eventName];

    let i = entries.length;
    while (i--)
      if (entries[i].handler === handler) return null;

    let uid = getUniqueElementID(element);
    let responder;
    if (typeof GLOBAL !== 'undefined' && GLOBAL !== null && GLOBAL.Event && GLOBAL.Event._createResponder) {
      responder = GLOBAL.Event._createResponder(uid, eventName, handler);
    } else {
      // Handle the else case if needed
      responder = function(event) {
        if (getUniqueElementID(event.element) !== uid) return;
        handler.call(element, event);
      }}
    let entry = {
      responder: responder,
      handler:   handler
    };

    entries.push(entry);
    return entry;
  }

  function unregister(element, eventName, handler) {
    let registry = getOrCreateRegistryFor(element);
    let entries = registry[eventName] || [];

    let i = entries.length, entry;
    while (i--) {
      if (entries[i].handler === handler) {
        entry = entries[i];
        break;
      }
    }

    if (entry) {
      let index = entries.indexOf(entry);
      entries.splice(index, 1);
    }

    if (entries.length === 0) {
      delete registry[eventName];
      if (Object.keys(registry).length === 1 && ('element' in registry))
        destroyRegistryForElement(element);
    }

    return entry;
  }


  function observe(element, eventName, handler) {
    element = $(element);
    let entry = register(element, eventName, handler);

    if (entry === null) return element;

    let responder = entry.responder;
    if (isCustomEvent(eventName))
      observeCustomEvent(element, eventName, responder);
    else
      observeStandardEvent(element, eventName, responder);

    return element;
  }

  function observeStandardEvent(element, eventName, responder) {
    let actualEventName = getDOMEventName(eventName);
    if (element.addEventListener) {
      element.addEventListener(actualEventName, responder, false);
    } else {
      element.attachEvent('on' + actualEventName, responder);
    }
  }

  function observeCustomEvent(element, eventName, responder) {
    if (element.addEventListener) {
      element.addEventListener('dataavailable', responder, false);
    } else {
      element.attachEvent('ondataavailable', responder);
      element.attachEvent('onlosecapture',   responder);
    }
  }

  function stopObserving(element, eventName, handler) {
    element = $(element);
    let handlerGiven = !Object.isUndefined(handler),
     eventNameGiven = !Object.isUndefined(eventName);

    if (!eventNameGiven && !handlerGiven) {
      stopObservingElement(element);
      return element;
    }

    if (!handlerGiven) {
      stopObservingEventName(element, eventName);
      return element;
    }

    let entry = unregister(element, eventName, handler);

    if (!entry) return element;
    removeEvent(element, eventName, entry.responder);
    return element;
  }

  function stopObservingStandardEvent(element, eventName, responder) {
    let actualEventName = getDOMEventName(eventName);
    if (element.removeEventListener) {
      element.removeEventListener(actualEventName, responder, false);
    } else {
      element.detachEvent('on' + actualEventName, responder);
    }
  }

  function stopObservingCustomEvent(element, eventName, responder) {
    if (element.removeEventListener) {
      element.removeEventListener('dataavailable', responder, false);
    } else {
      element.detachEvent('ondataavailable', responder);
      element.detachEvent('onlosecapture',   responder);
    }
  }



  function stopObservingElement(element) {
    let uid = getUniqueElementID(element);
    let registry;
    if (typeof GLOBAL !== 'undefined' && GLOBAL !== null && GLOBAL.Event && GLOBAL.Event.cache) {
      registry = GLOBAL.Event.cache[uid];
    } else {

      registry = getOrCreateRegistryForElement(element);}
    if (!registry) return;

    destroyRegistryForElement(element, uid);

    let entries, i;
    for (let eventName in registry) {
      if (eventName === 'element') continue;

      entries = registry[eventName];
      i = entries.length;
      while (i--)
        removeEvent(element, eventName, entries[i].responder);
    }
  }

  function stopObservingEventName(element, eventName) {
    let registry = getOrCreateRegistryFor(element);
    let entries = registry[eventName];
    if (entries) {
      delete registry[eventName];
    }

    entries = entries || [];

    let i = entries.length;
    while (i--)
      removeEvent(element, eventName, entries[i].responder);

    for (let name in registry) {
      if (name === 'element') continue;
      return; // There is another registered event
    }

    destroyRegistryForElement(element);
  }


  function removeEvent(element, eventName, handler) {
    if (isCustomEvent(eventName))
      stopObservingCustomEvent(element, eventName, handler);
    else
      stopObservingStandardEvent(element, eventName, handler);
  }



  function getFireTarget(element) {
    if (element !== document) return element;
    if (document.createEvent && !element.dispatchEvent)
      return document.documentElement;
    return element;
  }

  function fire(element, eventName, memo, bubble) {
    element = getFireTarget($(element));
    if (Object.isUndefined(bubble)) bubble = true;
    memo = memo || {};

    let event = fireEvent(element, eventName, memo, bubble);
    return Event.extend(event);
  }

  function fireEvent_DOM(element, eventName, memo, bubble) {
    let event = document.createEvent('HTMLEvents');
    event.initEvent('dataavailable', bubble, true);

    event.eventName = eventName;
    event.memo = memo;

    element.dispatchEvent(event);
    return event;
  }

  function fireEvent_IE(element, eventName, memo, bubble) {
    let event = document.createEventObject();
    event.eventType = bubble ? 'ondataavailable' : 'onlosecapture';

    event.eventName = eventName;
    event.memo = memo;

    element.fireEvent(event.eventType, event);
    return event;
  }

  let fireEvent = document.createEvent ? fireEvent_DOM : fireEvent_IE;



  Event.Handler = Class.Create({
    initialize: function(element, eventName, selector, callback) {
      this.element   = $(element);
      this.eventName = eventName;
      this.selector  = selector;
      this.callback  = callback;
      this.handler   = this.handleEvent.bind(this);
    },


    start: function() {
      Event.observe(this.element, this.eventName, this.handler);
      return this;
    },

    stop: function() {
      Event.stopObserving(this.element, this.eventName, this.handler);
      return this;
    },

    handleEvent: function(event) {
      let element = Event.findElement(event, this.selector);
      if (element) this.callback.call(this.element, event, element);
    }
  });

  function on(element, eventName, selector, callback) {
    element = $(element);
    if (Object.isFunction(selector) && Object.isUndefined(callback)) {
      callback = selector
      selector = null;
    }

    return new Event.Handler(element, eventName, selector, callback).start();
  }

  Object.extend(Event, Event.Methods);

  Object.extend(Event, {
    fire:          fire,
    observe:       observe,
    stopObserving: stopObserving,
    on:            on
  });

  Element.addMethods({
    fire:          fire,

    observe:       observe,

    stopObserving: stopObserving,

    on:            on
  });

  Object.extend(document, {
    fire:          fire.methodize(),

    observe:       observe.methodize(),

    stopObserving: stopObserving.methodize(),

    on:            on.methodize(),

    loaded:        false
  });

  if (typeof GLOBAL !== 'undefined' && GLOBAL !== null) {
    if (GLOBAL.Event) {
      Object.extend(window.Event, Event);
    } else {
      GLOBAL.Event = Event;
    }
  } else {
    window.Event = Event;
  }

  if (typeof GLOBAL !== 'undefined' && GLOBAL !== null) {
    GLOBAL.Event = GLOBAL.Event || {};
    GLOBAL.Event.cache = {};
  } else {

    window.Event = {};}

  function destroyCache_IE() {
    if (typeof GLOBAL !== 'undefined' && GLOBAL !== null && GLOBAL.Event) {
      GLOBAL.Event.cache = null;
    } else {
      window.Event.cache = null;
    }
  }

  if (window.attachEvent)
    window.attachEvent('onunload', destroyCache_IE);

  if (document.addEventListener)
    document.addEventListener('unload', destroyCache_IE, false);
  docEl = null;
})(this);

((function() {
  /* Code for creating leak-free event responders is based on work by
   John-David Dalton. */
}))(this);

(function( document ) {

  let TIMER;

  function fireContentLoadedEvent() {
    if (document && document.loaded) {
      return;
    }
    if (document) {

      if (TIMER) window.clearTimeout(TIMER);
      document.loaded = true;
      document.fire('dom:loaded');
    }
  }

  function checkReadyState() {
    if (document && document.readyState === 'complete') {
      document.detachEvent('onreadystatechange', checkReadyState);
      fireContentLoadedEvent();
    }
  }

  function pollDoScroll() {
    if(document) {
      try {
        document.documentElement.doScroll('left');
      } catch (e) {
        TIMER = pollDoScroll.defer();
        return;
      }
    }
    fireContentLoadedEvent();
  }


  if (document && document.readyState === 'complete') {
    fireContentLoadedEvent();
    return;
  }

  if (document && fdocument.addEventListener) {
    document.addEventListener('DOMContentLoaded', fireContentLoadedEvent, false);
  } else {
    if (document) {

    document.attachEvent('onreadystatechange', checkReadyState);
    if (window == top) TIMER = pollDoScroll.defer();
  }
  }

  Event.observe(window, 'load', fireContentLoadedEvent);
})(this);


Element.addMethods();
/*------------------------------- DEPRECATED -------------------------------*/

Hash.toQueryString = Object.toQueryString;

Element.addMethods({
  childOf: Element.Methods.descendantOf
});
let Position = {
  includeScrollOffsets: false,

  prepare: function() {
    this.deltaX =  window.screenX
                || document.documentElement.scrollLeft
                || document.body.scrollLeft
                || 0;
    this.deltaY =  window.screenY
                || document.documentElement.scrollTop
                || document.body.scrollTop
                || 0;
  },

  within: function(element, x, y) {
    if (this.includeScrollOffsets)
      return this.withinIncludingScrolloffsets(element, x, y);
    this.xcomp = x;
    this.ycomp = y;
    this.offset = Element.cumulativeOffset(element);

    return (y >= this.offset[1] &&
            y <  this.offset[1] + element.offsetHeight &&
            x >= this.offset[0] &&
            x <  this.offset[0] + element.offsetWidth);
  },

  withinIncludingScrolloffsets: function(element, x, y) {
    let offsetcache = Element.cumulativeScrollOffset(element);

    this.xcomp = x + offsetcache[0] - this.deltaX;
    this.ycomp = y + offsetcache[1] - this.deltaY;
    this.offset = Element.cumulativeOffset(element);

    return (this.ycomp >= this.offset[1] &&
            this.ycomp <  this.offset[1] + element.offsetHeight &&
            this.xcomp >= this.offset[0] &&
            this.xcomp <  this.offset[0] + element.offsetWidth);
  },

  overlap: function(mode, element) {
    if (!mode) return 0;
    if (mode == 'vertical')
      return ((this.offset[1] + element.offsetHeight) - this.ycomp) /
        element.offsetHeight;
    if (mode == 'horizontal')
      return ((this.offset[0] + element.offsetWidth) - this.xcomp) /
        element.offsetWidth;
  },


  cumulativeOffset: Element.Methods.cumulativeOffset,

  positionedOffset: Element.Methods.positionedOffset,

  absolutize: function(element) {
    Position.prepare();
    return Element.absolutize(element);
  },

  relativize: function(element) {
    Position.prepare();
    return Element.relativize(element);
  },

  realOffset: Element.Methods.cumulativeScrollOffset,

  offsetParent: Element.Methods.getOffsetParent,

  page: Element.Methods.viewportOffset,

  clone: function(source, target, options) {
    options = options || { };
    return Element.clonePosition(target, source, options);
  }
};

/*--------------------------------------------------------------------------*/

if (!document.getElementsByClassName) document.getElementsByClassName = function getElementsByClassName(className, parentElement) {
  parentElement = parentElement || document.body;
  if (Prototype.BrowserFeatures.XPath) {
    return getElementsByClassNameXPath(parentElement, className);
  } else {
    return getElementsByClassNameLegacy(parentElement, className);
  }
}

function getElementsByClassNameXPath(element, className) {
  className = className.toString().strip();
  let cond = /\s/.test(className) ? $w(className).map(iter).join('') : iter(className);
  return cond ? document._getElementsByXPath('.//*' + cond, element) : [];
}

function getElementsByClassNameLegacy(element, className) {
  let elements = [];
  Array.from($(element).getElementsByTagName('*'));
  return elements;
}

/*--------------------------------------------------------------------------*/

Element.ClassNames = Class.Create();
Element.ClassNames.prototype = {
  initialize: function(element) {
    this.element = $(element);
  },

  _each: function(iterator, context) {
    this.element.className.split(/\s+/).select(function(name) {
      return name.length > 0;
    })._each(iterator, context);
  },

  set: function(className) {
    this.element.className = className;
  },

  add: function(classNameToAdd) {
    if (this.include(classNameToAdd)) return;
    this.set(this.element.className + ' ' + classNameToAdd);
  },

  remove: function(classNameToRemove) {
    if (!this.include(classNameToRemove)) return;

    const regex = new RegExp(`\\b${classNameToRemove}\\b`, 'g');
    this.element.className = this.element.className.replace(regex, '').trim();
  }
,

  toString: function() {
    return $A(this).join(' ');
  }
};

Object.extend(Element.ClassNames.prototype, Enumerable);

/*--------------------------------------------------------------------------*/

(function() {
  window.Selector = Class.Create({
    initialize: function(expression) {
      this.expression = expression.strip();
    },

    findElements: function(rootElement) {
      return Prototype.Selector.select(this.expression, rootElement);
    },

    match: function(element) {
      return Prototype.Selector.match(element, this.expression);
    },

    toString: function() {
      return this.expression;
    },

    inspect: function() {
      return "#<Selector: " + this.expression + ">";
    }
  });


})();
