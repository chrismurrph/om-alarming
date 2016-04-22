#!/bin/sh
export LOCALREPO_USERNAME=
export LOCALREPO_PASSWORD=
for file in lib/*.jar
do
    name=$(basename "$file")
    basename=${name%.jar}
    echo "Deploying $basename"
    artifactId="local/$basename"
    lein deploy localrepo1 $artifactId 1.0 $file
    echo "[$artifactId \"1.0\"]" >> dependencies.log
done
