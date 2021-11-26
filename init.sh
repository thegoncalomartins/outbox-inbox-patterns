#!/bin/bash

docker-compose up --build -d
curl -d @"people/kafka-connector.json" -H "Content-Type: application/json" -X POST http://localhost:8093/connectors
curl -d @"movies/kafka-connector.json" -H "Content-Type: application/json" -X POST http://localhost:8093/connectors
