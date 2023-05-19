# Kong API Client

This repo has java code to auto configure apis on Kong Gateway based on provided list of apis in json file.

Jar File:

### Prerequisites
 TESTING
kong gateway should be up and serving the api at localhost:8001

Note: Make sure you use 8001 port, it will fail otherwise since jar is hard coded with port 8001(May be you can update the code that take endpoint as argument)

kong-0.0.1-SNAPSHOT.jar

json file with api mapping listings, every api mapping configuration should have following objects as mandatory

methods:
uris:
upstream_url
host

### Sample Json file
```
{"data":[
{"methods":[
"GET",
"POST",
"OPTIONS"
],
"upstream_url":"http:/v1_0.captcha.service.alln-poc.coi:9000/tenants/ibm/",
"uris":["/tenants/ibm/v1/captcha"],
"name":"ibm"
}
}
```
or you can take dumb from kong server using following endpoint
curl locolhost:8001/apis

save as json file and pass argument as shown below

java -cp kong-0.0.1-SNAPSHOT.jar com.kongapi.mapping.KongApi kong_apis.json

Jar will perform following tasks,

It compares the apis that are listed in the json file with existing api mappings in kong server,

* if new api are listed in the json file, it will add in the kong server,
* if api not listed in json and exist in kong server, it will be deleted from kong server
* if api is modified, it will update the existing api in the kong server.

### Sample out

```
C:\Users\revan\git\kongapi-mapping-clinet> java -cp kong-0.0.1-SNAPSHOT.jar com.kongapi.mapping.KongApi kong_apis.json
Reading json file kong_apis.json
Aug 17, 2017 4:47:50 PM com.kongapi.mapping.KongApi main
INFO: Getting Json data from Kong API
I am here
Aug 17, 2017 4:47:53 PM com.kongapi.mapping.KongClient getRequest
INFO:
Sending 'GET' request to URL : http://localhost:8001/apis
Aug 17, 2017 4:47:53 PM com.kongapi.mapping.KongClient getRequest
INFO: Response Code : 200
Aug 17, 2017 4:47:53 PM com.kongapi.mapping.KongApi main
INFO: From Json File: {"total":3,"data":[]}
Aug 17, 2017 4:47:53 PM com.kongapi.mapping.KongApi main
INFO: From kong api: {"total":1,"data":[{"upstream_send_timeout":60000,"methods":["OPTIONS","GET"],"upstream_url":"https://v1_0.dictionary.service.west-dev.com:9443","https_only":false,"created_at":1501554797293,"preserve_host":false,"http_if_terminated":true,"retries":5,"upstream_connect_timeout":60000,"uris":["/v1/dictionary"],"strip_uri":true,"name":"dictionary","id":"06c6c482-e2ff-4d80-926a-6bc078c21f37","upstream_read_timeout":60000}]}
Aug 17, 2017 4:47:53 PM com.kongapi.mapping.KongApi main
INFO: Verifying API lists from JSON File with Kong API
Updatelist: []
Ignore List: []
Add List: []
Aug 17, 2017 4:47:54 PM com.kongapi.mapping.KongApi deleteList
INFO: Name From Kong: dictionary
Aug 17, 2017 4:47:54 PM com.kongapi.mapping.KongApi deleteList
INFO: Not qualified, I am kicking off myself
http://localhost:8001/apis/dictionary
Aug 17, 2017 4:47:57 PM com.kongapi.mapping.KongClient deleteRequest
INFO: Delete Successfully
Aug 17, 2017 4:47:57 PM com.kongapi.mapping.KongApi deleteList
INFO: Added dictionary api delete list
Delete List: [{"upstream_send_timeout":60000,"methods":["OPTIONS","GET"],"upstream_url":"https://v1_0.dictionary.service.west-dev.com:9443","https_only":false,"created_at":1501554797293,"preserve_host":false,"http_if_terminated":true,"retries":5,"upstream_connect_timeout":60000,"uris":["/v1/dictionary"],"strip_uri":true,"name":"dictionary","id":"06c6c482-e2ff-4d80-926a-6bc078c21f37","upstream_read_timeout":60000}]
```
