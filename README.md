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