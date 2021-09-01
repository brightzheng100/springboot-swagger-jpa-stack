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


# DELETE it if any
curl -i -X DELETE "$url/api/v1/students/90001"

while true; do
    sleep 0.5
    
    # POST to create a new record
    curl -i -X POST "$url/api/v1/students" \
        -H "Content-Type: application/json" \
        --data '{"id":90001,"nid":"test","name":"test"}'

    # GET it back, without hitting the database directly!
    curl -i -X GET "$url/api/v1/students/90001"

    # PUT to update this record
    curl -i -X PUT "$url/api/v1/students" \
        -H "Content-Type: application/json" \
        --data '{"id":90001,"nid":"test2","name":"test2"}'

    # GET it back, again, without hitting the database directly!
    curl -X GET "$url/api/v1/students/90001"
done
