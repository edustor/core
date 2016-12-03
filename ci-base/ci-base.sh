#!/bin/bash -e

BASEDIR=$(pwd)

cd "$(dirname "$0")"

VERSION_TAG="$(sha1sum Dockerfile.base | sha1sum | cut -d' ' -f1)"
TEST_IMAGE="wutiarn/ci-base-cache:test"
IMAGE_ID="quay.io/wutiarn/ci-base-cache:$VERSION_TAG"

echo -n "$VERSION_TAG" > VERSION_TAG.txt

echo "Dockerfile hash is $VERSION_TAG"

echo "Pulling $IMAGE_ID..."
if docker pull "$IMAGE_ID" | cat; [[ ${PIPESTATUS[0]} == 0 ]]; then
    echo "Pull completed"
else
    echo "No cache image found. Building new one...";
    docker build -t "$IMAGE_ID" -f Dockerfile.base .

    echo "Pushing as $IMAGE_ID..."
    docker push "$IMAGE_ID" | cat
fi;

echo "Tagging $IMAGE_ID as $TEST_IMAGE"
docker tag ${IMAGE_ID} ${TEST_IMAGE}

docker history "$IMAGE_ID"

ESCAPED_IMAGE_ID=$(echo -n "$IMAGE_ID" | sed -e 's/[\/&]/\\&/g')
sed -i "s/\#CI_BASE_IMAGE_HERE/$ESCAPED_IMAGE_ID/" "$BASEDIR/$1"

echo "DONE"