Alert Sink
===

An extensible service for processing Alerts

[![build status](https://gitorious.ctc.com/gitlab/big/alert-sink/badges/master/build.svg)](https://gitorious.ctc.com/gitlab/big/alert-sink/commits/master)
[![coverage report](https://gitorious.ctc.com/gitlab/big/alert-sink/badges/master/coverage.svg)](https://gitorious.ctc.com/gitlab/big/alert-sink/commits/master)

#### What is an "Alert"

- The standardization of an external event that has been ingested into the system
- Something that people or processes connected to our system care about
- Something that we persist and make available through subscription and search

#### Developers

- See the hack directory for scripts to publish sample alerts.
- To use publishNewsAlertForRio and publishSoccerNews, you'll first need to use the register script to register a new application, and then use its uuid in the url path in the publish News scripts.
- When running the docker compose from scratch the mysql container wont get started up in time for lagom, and lagom fails.  At this point the lagom container is in a weird state and cannot be restarted.  The best way to start things up is
  - docker compose up the mysql container
  - docker compose up the rest of the containers
 


#### Kibana

The docker compose in this project wires in Kibana, you can reach it here http://localhost:5601/app/kibana

#### Elasticsearch

To enable the `metadata.location` object to be picked up as a [geo_point](https://www.elastic.co/guide/en/elasticsearch/reference/current/geo-point.html) in Elasticsearch

```
curl -vs localhost:9200/alert-gdelt -XPUT -d '
{
  "mappings": {
    "alert": {
      "properties": {
        "metadata": {
          "properties": {
            "location": {"type": "geo_point"}
          }
        }
      }
    }
  }
}
'
```

https://www.elastic.co/guide/en/elasticsearch/guide/current/nested-mapping.html

#### Configuration

- `KAFKA_BROKER`: kafka host and port (no scheme) (default `172.17.0.1:9092`)
- `ELASTICSEARCH_URI`: uri of elasticsearch (default `http://172.17.0.1:9200`)
- `PERSIST_DB_NAME`: database db name (default `mysql`)
- `PERSIST_HOST`: database host (default `172.17.0.1`)
- `PERSIST_PORT`: database port (default `3306`)
- `PERSIST_USER`: database user (default `mysql`)
- `PERSIST_PASSWORD`: database password (default `mysql`)
