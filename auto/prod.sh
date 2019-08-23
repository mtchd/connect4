#! /bin/bash
set -euo pipefail

. $(dirname $0)/../config/prod.env

docker run --rm -it -e TOKEN connect4