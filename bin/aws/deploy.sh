#!/usr/bin/env bash

set -e

if [ "$TRAVIS_BRANCH" == "master" ]; then
    aws lambda update-function-code \
        --zip-file=fileb://build/libs/epilogue.jar \
        --region=$LAMBDA_REGION \
        --function-name=$LAMBDA_FUNCTION_NAME \
        --query 'LastModified'
fi
