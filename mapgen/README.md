# Map generator
requires python3.8, pip3

# prepare

/usr/local/opt/python@3.8/bin/python3 -m pip install numpy
/usr/local/opt/python@3.8/bin/python3 -m pip install opencv-python
/usr/local/opt/python@3.8/bin/python3 -m pip install scipy
/usr/local/opt/python@3.8/bin/python3 -m pip install opensimplex
/usr/local/opt/python@3.8/bin/python3 -m pip install noise
/usr/local/opt/python@3.8/bin/python3 -m pip install scikit-image==0.17.2


# generate map
`/usr/local/opt/python@3.8/bin/python3 map_gen.py`
