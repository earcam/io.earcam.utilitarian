# TODO

look at com.github.s4u.plugins/sitemapxml-maven-plugin

[ ] Switch FileMap to use unexceptional - TAKE THE DIFF AND USE AS AN EXAMPLE FOR BLOG !!!!!!!!!!!!!!!!!!!!

https://www.sitemaps.org/protocol.html

generates robots.txt - pointing to the sitemap/index


## Features 


* Can produce a `<sitemapindex>` (TO SUPPORT PLUGIN - DO NOT MERGE multi-module sitemaps, use index)

* Generates sitemap.xml (using JAXB to generate POJOs)

* Generates multiple sitemaps with sitemapindex where a single sitemap would be over 50mb uncompressed

* Optionally gzip xml files (sitemapindex files would need to be aware of the additional ".gz" suffix)

* Submit site map to a given list of engines (Google/Bing/Yahoo/DuckDuck?)   (SUPPORT PLUGIN AS A post-deploy GOAL)   `<engine.com>/ping?sitemap=<sitemap.xml.gz|sitemapindex.xml.gz>`


### Limitations / Not Supported / TODO

Regex excludes needed

**Change Frequency** and **Priority** are not currently supported.  It would be easy to add these as mappers `Function<Path, TChangeFreq>` and `Function<Path, BigDecimal>`. 

`<lastMod>` is taken from the local file system.  Using SCM is a bad idea IMO, as commit date != deploy date.  
But, by extending the sitemap protocol with custom namespace to add SHA1 - along with a download/copy of the 
previous version containing the SHA1, we can compare and use to correctly set the last modified time.

Code needs to be refactored to avoid <abbr title="Out Of Memory">OOM</abbr> for huge datasets. Just stream from file system and write out using SAX.  2nd thoughts given the simple domain/xml-format, SAX would be overkill and simple homemade StringBuilder as template should be preferred.