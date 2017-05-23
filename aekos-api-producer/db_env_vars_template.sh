#!/bin/bash
# create a copy of this file named 'db_env_vars.sh' and update the values. The new file will be ignored by git and read by other scripts
export DB_NAME='somedb' # add database name
export THEUSER='someuser' # add username
export THEPASS='somepass' # add password
export THEURL='someinstance.aa111aaaaaaa.us-west-1.rds.amazonaws.com' # add instance name, cluster and region
export THEPORT='3306' # change the port if you need to
