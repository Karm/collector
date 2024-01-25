# Collector

[![https://collector.foci.life](https://github.com/Karm/collector/actions/workflows/prod.yml/badge.svg)](https://github.com/Karm/collector/actions/workflows/prod.yml)
[![https://stage-collector.foci.life/](https://github.com/Karm/collector/actions/workflows/stage.yml/badge.svg?branch=main)](https://github.com/Karm/collector/actions/workflows/stage.yml)

# Usage (stage)
The staging system is a place that could randomly get re-deployed, removed or that could have its data erased.

## Sign-up a new user

1. Pick a username: 
   ```
   $ curl -s -X POST --data 'username=karm&email=karm@redhat.com' \
     https://stage-collector.foci.life/public/signup | jq
   {
   "message": "User was successfully created. Check your email for further instructions. \
   Having issues? Drop a message to admin@collector.foci.life."
   }
   ```
2. The following email will arrive in your mailbox:
   ```
   Subject: Welcome to the Collector collective!
   
   Hello karm,
   Use this one time token to set your password:
   
   curl -X POST --data 'password=This is my password.&token=b2aef796a6854140097628505e8395d4e75f5f462cff4c3f37a073aca32a8cd10a6e1e0968f5b067087d38cb6115449e39e6d109099beeed636fb242541f6ffc' \
   https://stage-collector.foci.life/public/changepasswd
   ```
3. You can use it to set a password:
   ```
   $ curl -s -X POST --data 'password=This is my password.&token=b2aef796a6854140097628505e8395d4e75f5f462cff4c3f37a073aca32a8cd10a6e1e0968f5b067087d38cb6115449e39e6d109099beeed636fb242541f6ffc' \
   https://stage-collector.foci.life/public/changepasswd | jq
   {
   "message": "Your password was successfully changed. Try logging in, e.g. \
   curl -X POST -b cookie.txt -c cookie.txt --data 'j_username=Your_Username&j_password=Your Password' \
   https://stage-collector.foci.life/j_security_check"
   }
   ```

## Create a new API token

1. Logged-in users can create API tokens. Log-in:
   ```
   $ curl -s -X POST -b cookie.txt -c cookie.txt --data 'j_username=karm&j_password=This is my password.' \
   https://stage-collector.foci.life/j_security_check
   ```
2. Create a token, e.g. with read-only "r" permissions:
   ```
   $ curl -s -b cookie.txt -c cookie.txt -X POST https://stage-collector.foci.life/api/tokens/create/r | jq
   {
   "token": "3dbe70428399c9174debe65597557fd30dc9dae1d4be961f3f723ad586eb81bb313342e1bcc140e74ff21e17d7b1196386fdb51bbb5db1034bea7dfd2c57f9a7",
   "message": "Save the token safely. This is the only time it could be displayed."
   }
   ```
3. Delete a token (so as it's no longer usable):
   ```
   $ curl -s -b cookie.txt -c cookie.txt -X DELETE https://stage-collector.foci.life/api/tokens/delete/3dbe70428399c9174debe65597557fd30dc9dae1d4be961f3f723ad586eb81bb313342e1bcc140e74ff21e17d7b1196386fdb51bbb5db1034bea7dfd2c57f9a7 | jq
   {
   "message": "Deleted tokens: 1"
   }
   ```

## Use API token

e.g. with a demo report:

```
$ curl -s -X POST -H "Content-Type: application/json" -H "token: 2aa3f1a7a560d5e48385dba7d4893593aad409997b8e7ed9dd7d59008d9d5c36b9860921be25d412bc72b3f10fdc7bc1f3a6225c121e7525b81e8559d459703e" \
--data '{"something":"hahaha","data":42}' https://stage-collector.foci.life/api/report/test | jq
{
  "data": 42,
  "something": "hahaha"
}
```

# Deployment

GitHub action builds, tests and deploys `main` branch to https://stage-collector.foci.life/.
The end of the build spits out the expected version:
```
...
Version 1.0.0-6-g7d663ea should be on https://stage-collector.foci.life/
```
And that shows on the web too:
```
$ curl -s https://stage-collector.foci.life/public/version
1.0.0-6-g7d663ea
```

The prod environment is deployed manually by running the [prod](https://github.com/Karm/collector/actions/workflows/prod.yml) workflow, and it appears on https://collector.foci.life/.

## Podman

Having trouble with TestContainers and Podman? Take a look: https://quarkus.io/blog/quarkus-devservices-testcontainers-podman/

## Systemd

The server is controlled by systemd, e.g. in this way:

```
[Unit]
Description=Collector stage instance
After=network-online.target firewalld.service mariadb.service
Wants=network-online.target mariadb.service
[Service]
Restart=on-failure
User=collector
Type=simple
WorkingDirectory=/home/collector/stage
ExecStartPre=-/bin/bash -c 'mv /home/collector/stage/collector.deploy /home/collector/stage/collector'
ExecStart=/home/collector/stage/collector -Dquarkus.profile=stage
ExecStop=/bin/kill -2 $MAINPID
[Install]
WantedBy=multi-user.target
```

Passwords are loaded from a `.env` file, e.g.:

```
_PROD_QUARKUS_DATASOURCE_USERNAME=????
_PROD_QUARKUS_DATASOURCE_PASSWORD=????
_STAGE_QUARKUS_DATASOURCE_USERNAME=????
_STAGE_QUARKUS_DATASOURCE_PASSWORD=????
_STAGE_QUARKUS_HTTP_PORT=????
_PROD_QUARKUS_HTTP_PORT=????
QUARKUS_HTTP_HOST=????
QUARKUS_HTTP_AUTH_SESSION_ENCRYPTION_KEY=????
QUARKUS_MAILER_USERNAME=????
QUARKUS_MAILER_PASSWORD=????
```

# Angular UI development

## Prerequisites

Be sure you've got at least node/yarn versions mentioned in `pom.xml` installed on your system.

## Development

The easiest way to develop the UI is using `yarn proxy` from the `frontend` directory once the
Quarkus back-end is up and running:

```
$ ./mvnw -Dui.deps -Dui.dev clean quarkus:dev
```

In another terminal do:

```
$ cd frontend
$ yarn proxy
yarn run v1.22.10
$ ng serve --proxy-config proxy.conf.json
✔ Browser application bundle generation complete.

Initial Chunk Files   | Names         |  Raw Size
vendor.js             | vendor        |   3.47 MB | 
polyfills.js          | polyfills     | 294.79 kB | 
styles.css, styles.js | styles        | 251.74 kB | 
main.js               | main          |  36.31 kB | 
runtime.js            | runtime       |   6.51 kB | 

                      | Initial Total |   4.04 MB

Build at: 2022-05-11T15:52:35.414Z - Hash: 687102ba1903194d - Time: 4589ms

** Angular Live Development Server is listening on localhost:4200, open your browser on http://localhost:4200/ **


✔ Compiled successfully.
```

Then open the browser at http://localhost:4200/

## Compile for Deployment

Steps are similar to compiling the development version, but using `-Dui` (over `-Dui.dev`) instead.

```
$ ./mvnw -Dui.deps -Dui clean package
```

