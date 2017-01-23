#!/bin/bash
# usage:  adduuid.sh test.txt > test.out
while read; do
    echo "$REPLY|$(uuidgen)"
done < $1
