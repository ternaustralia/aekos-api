Instructions for using docker-compose

First, we need to build the image for the aekos-api app
```bash
cd ../../../ # up to root of this repo
./mvnw -Pprod clean package docker:build
```

Next, we need to edit the `docker-compose.yml` file to add our email address for the `LETSENCRYPT_EMAIL` value.
```yml
aekos:
  ...
  environment:
    LETSENCRYPT_EMAIL: replace-me @ with.your.email # replace this before running, and don't commit
```

We also need to create a few symlinks to the database, etc:
```bash
# in the same directory as this file
ln -s /host/path/to/tdb ./data
ln -s /host/path/to/auth ./auth
ln -s /host/path/to/metrics ./metrics
ln -s /home/path/to/lucene-index ./lucene-index
```

Now we can run the stack:
```bash
docker-compose up
```

If this is the first time you deploy the stack, there won't be any SSL/TLS certificates. They may be generated soon after you start the stack but if they aren't, you can force a refresh to be issued certs:
```bash
ls ./letsencrypt-certs/ # check if certs have been issued
# change `docker_nginx-letsencrypt_1` for the container name if required
docker exec docker_nginx-letsencrypt_1 /app/force_renew
```

You're done. You have a stack running that:
 - will survive system restarts
 - automatically update SSL certs when required
 - redirects non-HTTPS traffic to HTTPS
