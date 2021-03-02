#!/bin/bash

echo starting hugo-dev on http://localhost:8095

export HUGO_VERSION=$(grep "FROM klakegg/hugo" Dockerfile | sed 's/FROM klakegg\/hugo://g' | sed 's/ AS builder//g')
docker run \
  --rm --interactive \
  --publish 8095:8095 \
  --name hugo-dev \
  -v $(pwd):/src \
  klakegg/hugo:${HUGO_VERSION} \
  server -p 8095 --bind 0.0.0.0

