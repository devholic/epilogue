#!/usr/bin/env bash

set -e

if [ "$TRAVIS_BRANCH" == "master" ]; then
    aws lambda update-function-code \
        --zip-file=fileb://epilogue.zip \
        --region=$LAMBDA_REGION \
        --function-name=$LAMBDA_FUNCTION_NAME
fi
