# elytron-security-context-propagation

Playground for security context propagation testing in Elytron

```
export JBOSS_HOME=...
$JBOSS_HOME/bin/jboss-cli.sh --file=demo.cli
for i in 1 2; do  cp -r $JBOSS_HOME/standalone $JBOSS_HOME/standalone$i; done;

mvn clean install \
  && cp ejb-entrybean/target/seccontext-entry.jar $JBOSS_HOME/standalone1/deployments \
  && cp ejb-whoamibean/target/seccontext-whoami.jar $JBOSS_HOME/standalone2/deployments

$JBOSS_HOME/bin/standalone.sh -c standalone.xml -Djboss.server.base.dir=$JBOSS_HOME/standalone1 -Djboss.node.name=host1 -Dseccontext.provider.url=remote+http://127.0.0.1:8280
$JBOSS_HOME/bin/standalone.sh -c standalone.xml -Djboss.server.base.dir=$JBOSS_HOME/standalone2 -Djboss.node.name=host2 -Djboss.socket.binding.port-offset=200

mvn exec:java -f ejb-client
```