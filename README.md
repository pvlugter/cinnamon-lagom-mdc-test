# Cinnamon Lagom MDC test

Start all services:

```
sbt clean lagomServiceLocatorStart lagomCassandraStart lagomKafkaStart "first/Test/runMain play.core.server.ProdServerStart"
```

```
sbt "second/Test/runMain play.core.server.ProdServerStart"
```

```
sbt "third/Test/runMain play.core.server.ProdServerStart"
```

Update first service:

```
curl http://localhost:9001/api/update/abc
```
