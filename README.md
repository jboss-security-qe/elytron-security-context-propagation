# elytron-security-context-propagation

Playground for security context propagation testing in Elytron

```
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

# run the client
mvn exec:java -f ejb-client
```