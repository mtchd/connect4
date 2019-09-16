#! /bin/bash
set -euo pipefail

sbt assembly

docker build -t connect4 .

. $(dirname $0)/../config/dev.env

docker run --rm -it -e TOKEN connect4
