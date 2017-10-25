

## Netlify Deploy




### API Bugs


listSites returns "site_id" field duplicating "id" but this is not in the schema and must be ignored



0. calls file list to get the map

0. uses baseDir(s).relativize(f.toPath()) to produce the destination path

0. send gzipped JSON to Netlify:
	Content-Type: "application/zip" and the ZIP file as the HTTP request body:    POST /api/v1/:site_id/deploys
	e.g.
	curl -H "Content-Type: application/zip" \
	     -H "Authorization: Bearer my-api-access-token" \
    	 --data-binary="@website.zip" \
	     https://api.netlify.com/api/v1/sites/mysite.netlify.com/deploys
	Response e.g.
	{"deploy_id": "1234", "required": ["907d14fb3af2b0d4f18c2d46abe8aedce17367bd"]}
	iterate across map entry set and if SHA1 in "required" list upload with e.g:  PUT /api/v1/deploys/1234/files/index.html  using Content-Type: "application/octet-stream" 


REQUIRED :site_id AND my-api-access-token


[ ] debug log rate-limiting response headers (see https://www.netlify.com/docs/api/ -> Rate Limiting)   use SLF4J as supporred by Maven > 3.1.0


Use CURL to retrieve data on existing sites



[ ] Create dummy site 
[ ] Use wiremock record-n-replay scenarios
	[ ] upload new site
	[ ] upload replacement site - OVERWRITE
	[ ] upload replacement site - MERGE
	[ ] upload single file (overwrite by virtue)



NEW SITE:  io.earcam.site.deploy.netlify.api.DefaultApi.createSite(Site, Boolean)




options map:
	name: String  (earcam-blog)
	custom_domain: String  (blog.earcam.io)
	url: String  (https://blog.earcam.io)
	ssl: boolean
	force_ssl: boolean
	managed_dns: boolean
	#premium: boolean   ## Not settable on Site.java 
	createIfNotExists: boolean  ### TODO This is custom
	oauthToken: String ### TODO This is custom


io.earcam.site.deploy.netlify.api.DefaultApi.createSiteDeploy(String, DeployFiles)
io.earcam.site.deploy.netlify.api.DefaultApi.uploadDeployFile(String, String, byte[])



## WireMock

 cd into, e.g. ./src/test/resources/wiremock/api.netlify.com and run:
 
		M2_REP0=/data/repository/maven/
		VERSION=2.6.0
		JAR=${M2_REP0}com/github/tomakehurst/wiremock-standalone/${VERSION}/wiremock-standalone-${VERSION}.jar
		java -jar  ${JAR} --verbose --record-mappings --https-port=8443 --proxy-all="https://`echo ${PWD##*/} | tr -s ' ' '/'`"







### List all sites

		curl -H 'User-Agent: Mozilla/5.0 (X11; YouNix; Linux x86_64; rv:53.0) earcam.io/1.0' https://api.netlify.com/api/v1/sites?access_token=!YOUR_ACCESS_TOKEN_GOES_HERE!


### ???

		curl -H 'User-Agent: Mozilla/5.0 (X11; YouNix; Linux x86_64; rv:53.0) earcam.io/1.0'
		https://api.netlify.com/api/v1/
		access_token=!YOUR_ACCESS_TOKEN_GOES_HERE!



