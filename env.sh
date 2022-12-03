#!/bin/bash

# USAGE: ./env.sh outdir VAR_ONE VAR_TWO ...

outdir="${1}"

if [[ ! -z "$outdir" ]]; then
  envJs="$outdir/env.js"
else
  echo "Output directory must be provided as the first argument!"
  exit 1
fi

rm -rf ${envJs}
# create directory if it doesn't exist
mkdir -p "$outdir"
touch ${envJs}

echo "window.__env__ = {" >> ${envJs}

for varname in "${@:2}"
do
  varvalue="${!varname}"
  echo "  $varname: \"$varvalue\"," >> ${envJs}
done

echo "};" >> "${envJs}"

echo "generated ${envJs}"
