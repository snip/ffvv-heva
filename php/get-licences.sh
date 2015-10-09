#!/bin/sh
curl -v -H"$(./wssetoken.php myWsseLogin myWssePassword)" http://api.licences.ffvv.stadline.com/persons
echo
