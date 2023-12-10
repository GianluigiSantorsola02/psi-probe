
function createElement(elementName, attrs) {
  let parentElement = document.createElement('div');
  let element;

  if (attrs.length) {
    try { // prevent IE "feature": http://dev.rubyonrails.org/ticket/2707
      parentElement.innerHTML = "<" + elementName + " " + attrs + "></" + elementName + ">";
    } catch (e) {}

    element = parentElement.firstChild || null;

    // workaround Firefox 1.0.X bug
    if (!element) {
      element = document.createElement(elementName);
      for (let attr in arguments[1]) {
        element[attr === 'class' ? 'className' : attr] = arguments[1][attr];
      }
    }
  }

  if (element && element.tagName.toUpperCase() !== elementName) {
    element = parentElement.getElementsByTagName(elementName)[0];
  }

  return element;
}
let Builder = {
  NODEMAP: {
    AREA: 'map',
    CAPTION: 'table',
    COL: 'table',
    COLGROUP: 'table',
    LEGEND: 'fieldset',
    OPTGROUP: 'select',
    OPTION: 'select',
    PARAM: 'object',
    TBODY: 'table',
    TD: 'table',
    TFOOT: 'table',
    TH: 'table',
    THEAD: 'table',
    TR: 'table'
  },

  "node": function(elementName) {
    elementName = elementName.toUpperCase();

    let element;

    // try innerHTML approach
    let parentTag = this.NODEMAP[elementName] || 'div';
    let parentElement = document.createElement(parentTag);
    try { // prevent IE "feature": http://dev.rubyonrails.org/ticket/2707
      parentElement.innerHTML = "<" + elementName + "></" + elementName + ">";
    } catch(e) {}
    element = parentElement.firstChild || null;

    // see if browser added wrapping tags
    if(element && (element.tagName.toUpperCase() !== elementName)) {
      element = element.getElementsByTagName(elementName)[0];
    }

    // fallback to createElement approach
    if(!element) {
      element = document.createElement(elementName);
    }

    // abort if nothing could be created
    if(!element) {
      return;
    }

    // attributes (or text)
    if(arguments[1]) {
      if(this._isStringOrNumber(arguments[1]) ||
          (arguments[1] instanceof Array) ||
          arguments[1].tagName) {
        this._children(element, arguments[1]);
      } else {
        let attrs = this._attributes(arguments[1]);
       createElement(elementName, attrs);
      }
    }

    // text, or array of children
    if(arguments[2]) {
      this._children(element, arguments[2]);
    }

    return $(element);
  },
  _text: function(text) {
     return document.createTextNode(text);
  },

  ATTR_MAP: {
    'className': 'class',
    'htmlFor': 'for'
  },

  _attributes: function(attributes) {
    let attrs = [];
    for(attribute in attributes)
      attrs.push((attribute in this.ATTR_MAP ? this.ATTR_MAP[attribute] : attribute) +
          '="' + attributes[attribute].toString().escapeHTML().gsub(/"/,'&quot;') + '"');
    return attrs.join(" ");
  },
  _children: function(element, children) {
    if(children.tagName) {
      element.appendChild(children);
      return;
    }
    if(typeof children=='object') { // array can hold nodes and text
      children.flatten().each( function(e) {
        if(typeof e=='object')
          element.appendChild(e);
        else
          if(Builder._isStringOrNumber(e))
            element.appendChild(Builder._text(e));
      });
    } else
      if(Builder._isStringOrNumber(children))
        element.appendChild(Builder._text(children));
  },
  _isStringOrNumber: function(param) {
    return(typeof param=='string' || typeof param=='number');
  },
};
