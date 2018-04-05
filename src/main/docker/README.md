Instructions for using docker-compose

First, we need to build the image for the aekos-api app
```bash
# might have to build the loader
./mvnw -Pprod clean package docker:build
```

Now we can run the stack:
```bash
docker-compose up
```
