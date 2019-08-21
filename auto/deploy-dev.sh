#!/bin/sh
set -euo pipefail

# Sources dev environment variables
. $(dirname "$0")/../config/dev.env

ENVIRONMENT=${1:-dev}

docker run --rm -t -v $(pwd):/cwd \
    -e AWS_ACCESS_KEY_ID -e AWS_SECRET_ACCESS_KEY -e AWS_SECURITY_TOKEN \
    registry.cowbell.realestate.com.au/cowbell/rea-shipper:1.0.0 \
    --override app.image=${APP_TAG} \
    -c config/rea-shipper.yml \
    ship