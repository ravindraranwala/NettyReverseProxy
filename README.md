NettyReverseProxy
=================

A Reverse Proxy which was written using the Netty Transport


To run the program fill the necessary properties in the config.properties file which resides inside the src/main/resources directory and then run the HexDumpProxy class using your IDE.

Properties are described below.

secureProxy - whether the proxy is exposed as a secure/HTTPS endpoint. 
remoteHost - remote host where the backend service resides 
remotePort - remote port associated with the backend service 
localPort - proxy service listens for connections on this port number 
keystore - file system location of the keystore 
keystorepassword - keystore password 
truststore - file system location of the client trust store 
truststorepassword - client trust store password 
secureBackend - whether the backend service is exposed as a secure/HTTPS endpoint or NOT.


How to send the request from the client.
==========================================

First, point your request to the proxy service and append the relative URL of the backend service to that. For an example,

curl -v -k -d @sendReqThree.xml -H "Content-Type: text/xml; charset=utf-8" -H 'Host: w8cert.iconnectdata.com' "SOAPAction:urn:authorizeComchekDraft" http://localhost:8585/IVRWS/services/IVRWS

where the '/IVRWS/services/IVRWS' is the relative URL of the backend service 'w8cert.iconnectdata.com:443' which we need to invoke. Also makesure to give all the HTTP headers as specified in the above sample request. In this case the backend service runs on the 'w8cert.iconnectdata.com' host machine against the 443 port which needs to be provided to the 'remoteHost' and 'remotePort' property values in the config.properties file. HTTP Host header is a must in this case, and otherwise you may end up with HTTP 404 requested resource not found error.

The backend service URL which we need to invoke here is : https://w8cert.iconnectdata.com/IVRWS/services/IVRWS
