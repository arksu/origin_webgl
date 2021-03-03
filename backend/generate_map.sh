#!/bin/sh

gradle mapgen --args="new tiles"
python3.8 ../mapgen/rivers.py
gradle mapgen --args="lakes"
python3.8 ../mapgen/spots.py