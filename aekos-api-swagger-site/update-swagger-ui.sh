#!/usr/bin/env bash
# Grabs a new version of the UI and applies changes to the out-of-the-box swagger-ui that we need
cd `dirname $0`
set -e
patchfile='./swagger-ui-index.html.patch'
targetfile='./swagger-ui-dist/index.html'
swaggerjsonurl='swagger-aekos-api.json'
swaggeruidir=$1
if [ -z "$swaggeruidir" ]; then
  echo "[ERROR] no swagger-ui git repo dir supplied. You need to have the git repo cloned and supply the path."
  echo "usage: $0 <swagger ui git dir>"
  echo "   eg: $0 /path/to/git/swagger-ui"
  exit 1
fi
echo '[INFO] copying new version (well, whatever version you have checked out)'
distdir=$swaggeruidir/dist
pushd $distdir > /dev/null
git log -1 > git-commit.txt
popd > /dev/null
pushd swagger-ui-dist > /dev/null
cp -v $distdir/* .
popd > /dev/null
echo '[INFO] updating JSON URL'
sed -i "s+url: \".*\",+url: \"$swaggerjsonurl\",+" $targetfile
echo '[INFO] applying patch'
git apply $patchfile
echo '[INFO] done, you can now `git commit` and deploy (see README for instructions)'
