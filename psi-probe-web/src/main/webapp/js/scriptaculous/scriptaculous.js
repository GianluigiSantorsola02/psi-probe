
let Scriptaculous = {
  Version: '1.9.0', REQUIRED_PROTOTYPE: "",

  require: function(libraryName) {
    try{
      // inserting via DOM fails in Safari 2.0, so brute force approach
      document.write('<script type="text/javascript" src="'+libraryName+'"></script>');
    } catch(e) {
      // for xhtml+xml served content, fall back to DOM methods
      let script = document.createElement('script');
      script.type = 'text/javascript';
      script.src = libraryName;
      document.getElementsByTagName('head')[0].appendChild(script);
    }
  },
  load: function() {
    function convertVersionString(versionString) {
      let v = versionString.replace(/_.*|\./g, '');
      v = parseInt(v + '0'.times(4-v.length));
      return versionString.indexOf('_') > -1 ? v-1 : v;
    }

    if((typeof Prototype=='undefined') ||
       (typeof Element == 'undefined') ||
       (typeof Element.Methods=='undefined') ||
       (convertVersionString(Prototype.Version) <
        convertVersionString(Scriptaculous.REQUIRED_PROTOTYPE)))
      throw new Error("script.aculo.us requires the Prototype JavaScript framework >= " +
          Scriptaculous.REQUIRED_PROTOTYPE);

    let js = /scriptaculous\.js(\?.*)?$/;
    $$('script[src]').findAll(function(s) {
      return s.src.match(js);
    }).each(function(s) {
      let path = s.src.replace(js, ''),
          includes = <s className="src match"></s>.exec(s.src);
      (includes ? includes[1] : 'builder,effects,dragdrop,controls,slider,sound').split(',').each(
       function(include) { Scriptaculous.require(path+include+'.js') });
    });
  }
};

Scriptaculous.load();
