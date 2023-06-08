#!/bin/bash

pkill -f java
pkill node

# to see if the process is still running use:
# ps aux | grep java

# keep in mind that the process of searching for java processes will appear
# it will be a simpler process and if you do the command multiple times
# you will see the PID chage. So dont get confused if you're thinking the 
# kafka servers are up but in reality you're looking to the grep java :)

# also if you are with vscode running, it normally fires a background java
# process continuosly, so dont try to delete that because it just wont xD