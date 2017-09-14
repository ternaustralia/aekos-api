#!/bin/bash
# create a copy of this file named 'db_env_vars.sh' and update the values. The new file will be ignored by git and read by other scripts
export DB_NAME_PREFIX='apidata' # update DB name prefix if required, but you probably don't need to
export THEUSER='someuser' # add username
export THEPASS='somepass' # add password
export THEURL='someinstance.aa111aaaaaaa.us-west-1.rds.amazonaws.com' # add instance name (someinstance), cluster (aa111aaaaaaa) and region (us-west-1)
export THEPORT='3306' # change the port if you need to
