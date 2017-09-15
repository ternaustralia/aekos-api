#!/bin/bash
docker run \
  --name aekos-api-data \
  -e MYSQL_RANDOM_ROOT_PASSWORD=yes \
  -e MYSQL_ROOT_HOST=172.17.0.1 \
  -d \
  -p 6603:3306 \
  -v /data/mysql-data:/var/lib/mysql \
  mysql/mysql-server:5.7
echo '[INFO] now run `docker logs -f aekos-api-data` to see the root password'
echo '[INFO] then you can connect from this host with `mysql -u root -p -P 6603 -h 127.0.0.1`'

# You'll probably want to run queries like this to get started:
# create user 'apiro'@'%' identified by 'some-password';
# grant select on *.* to 'apiro'@'%';
# create user 'apirw'@'129.127.180.228' identified by 'some-password';
# grant all on *.* to 'apirw'@'129.127.180.228';
# create user 'apirw'@'172.17.0.1' identified by 'some-password';
# grant all on *.* to 'apirw'@'172.17.0.1';
