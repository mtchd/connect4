#! /bin/bash
set -euo pipefail

sbt assembly

docker build -t connect4 .

docker push mtchd/connect4