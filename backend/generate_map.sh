#!/bin/sh

gradle mapgen --args="new tiles"
python3 ../mapgen/rivers.py
gradle mapgen --args="lakes"
python3 ../mapgen/spots.py