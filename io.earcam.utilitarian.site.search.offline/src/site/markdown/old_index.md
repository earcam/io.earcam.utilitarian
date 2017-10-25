


# TODO

[ ] Grab meta description IFF available

[ ] Pluggable content-extractors - use the file-type guessing used in ...web.serve

 	[ ] For xref just index filename and FQN as content 




---

# OLD

## TODO

* Need to expose versions of Lunrjs etc to allow downstream (e.g. plugin) to use same version in CDN URLs for script tag
* Deps be overridden and we would detect the version from webjar dependencies (use JAR/resource scanning to avoid mvn dep)
  
* features; css selectors to in/exclude page content   ... later add a UI element for sites and package as web-fragment (mvn plugin can extract for site)... later additional mime-type parsers (PDF, word, excel, open-doc, whatevers)  

* ARGS:  directories, inc/exclude patterns (as per mvn fileset), field name and content extract inc/exc patterns (regex, xpath, css-selector)
* OPTS:  generate autocomplete list? first check if lunr already supports autocomplete... still worth doing for external-engine but lower priority in that case 

### Automatically include "Add to firefox" etc feature

By using:

		<link rel="search" type="application/opensearchdescription+xml" title="Stack Overflow" href="/opensearch.xml">

Where `opensearch.xml` is:

		<?xml version="1.0" encoding="UTF-8" ?>
		<OpenSearchDescription xmlns="http://a9.com/-/spec/opensearch/1.1/" xmlns:moz="http://www.mozilla.org/2006/browser/search/">
		  <ShortName>Stack Overflow</ShortName>
		  <Description>Search Stack Overflow: Q&amp;A for professional and enthusiast programmers</Description>
		  <InputEncoding>UTF-8</InputEncoding>
		  <Image width="16" height="16" type="image/x-icon">https://cdn.sstatic.net/Sites/stackoverflow/img/favicon.ico?v=4f32ecc8f43d</Image>
		  <Url type="text/html" method="get" template="http://stackoverflow.com/search?q={searchTerms}"></Url>
		</OpenSearchDescription>


### manifest.json  AS PER 

https://developers.google.com/web/fundamentals/web-app-manifest/
https://developer.mozilla.org/en-US/docs/Web/Manifest#Browser_compatibility


### Maven Plugin

* Only generate an index per module

* Aggregator can run teh lunr.Index.prototype.concat function on each site's JS.  As per https://github.com/olivernn/lunr.js/issues/29