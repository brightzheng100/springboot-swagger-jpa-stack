# simple load gen scripts

#
# Typical Usage:
# 
# 1. Port forward
#   pod=`kubectl -n labs get pod -l app=springboot-swagger-jpa-stack -o json | jq -r ".items[0].metadata.name"`
#   kubectl port-forward -n labs $pod 8080:8080
# 
#

url="http://localhost:8080"
if ! [[ -z $1 ]]; then
    url=$1
fi

while true; do
    sleep 1
    
    ###### javatrace specific package ######

    # get one step
    curl -i -X GET "$url/api/v1/spans/1"
    # get all steps
    curl -i -X GET "$url/api/v1/spans/"
    # get error
    curl -i -X GET "$url/api/v1/spans/error"


    ###### other auto-instrumented stuff ######

    # get all students
    curl -i -X GET "$url/api/v1/students"
    # get one student
    curl -i -X GET "$url/api/v1/students/10001"

    # get external websites
    curl -i -X GET "$url/api/v1/httpbin/get"
    curl -i -X POST "$url/api/v1/httpbin/post"

done
