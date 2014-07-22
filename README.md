jul-ext
=======
An extension to the classes and interfaces of the JavaTM 2 platform's core logging facilities.

I am experimenting with [HipChat](https://www.hipchat.com/ "HipChat")
and [logentries](https://www.logentries.com "logentries").

If we are able to use this code into production I might invest more time
in this project.

LogentriesHandler
-----------------
[Logentries](https://logentries.com "logentries") is a cloud-based Log Management and Analytical Tool.
If you are like me and prefer to write directly to the [Inputs](https://logentries.com/doc/inputs/ "inputs")
you found yourself stuck while using java.utils.logging (JUL). Logentries provides direct support using
[log4j](http://logging.apache.org/log4j/1.2/ "log4j") and [logback](http://logback.qos.ch "logback"),
but does not support JUL.

log-ext provides a LogentriesHandler that works with JUL. All you need is your [token](https://logentries.com/doc/input-token/ "token").

Here is an example logging.properties:
```
org.julext.LogentriesHandler.token = 28d5f881-aaaa-44e5-xxxx-201b5c7982dc
```

LogentriesHandler with Apache Tomcat
------------------------------------
The main reason for LogentriesHandler is in combination with [Apache Tomcat](http://tomcat.apache.org "Apache Tomcat"). Its pretty straightforward to send your Web Applications logs to Logentries.
As you certainly know, Apache Tomcat uses its own LogManager implementation called [JULI](http://tomcat.apache.org/tomcat-7.0-doc/logging.html "JULI").

First of all you need to add jul-ext.jar to the System class loader. Otherwise you
end up with ClassNotFoundException.

Copy jul-ext.jar to $CATALINA_HOME/bin and edit the file setenv.sh. Just create
setenv.sh if it does not exist.

```
#!/bin/sh
CLASSPATH="$CATALINA_HOME/bin/julext-1.0-SNAPSHOT.jar"
```

Thats it! LogentriesHandler is now loaded by Apache Tomcat.

Now edit ``$CATALINA_HOME/conf/logging.properties`` and do some logging.
```
1mywebapp.org.julext.LogentriesHandler.level = ALL
1mywebapp.org.julext.LogentriesHandler.token = 28d5f881-aaaa-44e5-xxxx-201b5c7982dc
1mywebapp.org.julext.LogentriesHandler.formatter = org.apache.juli.OneLineFormatter
```

Don't forget to add create the Handler with the handler directive.
```
handlers = [...], 1mywebapp.org.julext.LogentriesHandler
```

Further reading
---------------
http://docs.oracle.com/javase/7/docs/technotes/guides/logging/overview.html
