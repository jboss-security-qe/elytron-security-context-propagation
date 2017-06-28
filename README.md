# JBEAP-11822 reproducer

EAP 7.1.0.ER1 server stuck during second EJB client call.

```bash
# set path to WildFly/EAP
export JBOSS_HOME=...
# Reconfigure server (configure Elytron, etc.)
$JBOSS_HOME/bin/jboss-cli.sh --file=demo.cli
# make two copies of the standalone folder - one for each server
for i in 1 2; do  cp -r $JBOSS_HOME/standalone $JBOSS_HOME/standalone$i; done;

# compile this project and place the EJB JARs to server1 and server2
mvn clean install \
  && cp ejb-entrybean/target/seccontext-entry.jar $JBOSS_HOME/standalone1/deployments \
  && cp ejb-whoamibean/target/seccontext-whoami.jar $JBOSS_HOME/standalone2/deployments

# start server1 and server2 (use separate terminals for them)
$JBOSS_HOME/bin/standalone.sh -c standalone.xml -Djboss.server.base.dir=$JBOSS_HOME/standalone1 -Djboss.node.name=host1 -Dseccontext.provider.url=remote+http://127.0.0.1:8280
$JBOSS_HOME/bin/standalone.sh -c standalone.xml -Djboss.server.base.dir=$JBOSS_HOME/standalone2 -Djboss.node.name=host2 -Djboss.socket.binding.port-offset=200

# run the client twice
mvn exec:java -f ejb-client
mvn exec:java -f ejb-client
```

## Problem.
Second call of EJB client causes server stuck in some cases. When this happens administrator is not able to shutdown or reload such a server.

### Scenario details:
Elytron is configured as security provider on servers and Elytron client API is used for authentication. Protected stateless beans are used.

```
EJB Client -> Entry bean on Server1 -> WhoAmI bean on Server2
```

* Client uses AuthenticationConfiguration with valid credentials for Server1; Client calls the Entry bean twice - the difference is in arguments provided. The arguments are subsequently used for authentication from Server1 to Server2 (using AuthenticationConfiguration):
    * first call uses null for both username and password
    * second call uses valid credentials for Server2
* Entry bean on Server1 calls WhoAmI bean on Server2 with credentials which was provided as method arguments;
* WhoAmI bean on Server2 just returns caller principal

The Client is executed twice, the second call doesn't finish.