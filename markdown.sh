curl -X POST \
     -v \
     -H "Accept: application/markdown" http://localhost:3080/documentation \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "url=http://petstore.swagger.io/v2/swagger.json"
