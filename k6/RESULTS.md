# Load Test Results — Vynl

**Environment:** Railway Hobby plan (single instance)  
**Date:** 2026-02-22  
**Tool:** k6 v1.6.1

Base URL tested: `https://vinyl.up.railway.app`

## Scenario 1: Product Catalog Browsing
- **Virtual Users:** 20 concurrent (ramp 10s, hold 30s, ramp down 10s)
- **Duration:** 30s sustained load (50s total run)
- **Total Requests:** 525
- **Throughput:** 9.74 req/s
- **Latency (p50):** 88.15ms
- **Latency (p95):** 143.83ms
- **Latency (p99):** 161.57ms
- **Error Rate:** 0.00%

## Scenario 2: Cart Operations
- **Virtual Users:** 10 concurrent (ramp 5s, hold 20s, ramp down 5s)
- **Duration:** 20s sustained load
- **Total Requests:** 191
- **Throughput:** 5.14 req/s
- **Latency (p50):** 101.52ms
- **Latency (p95):** 170.21ms
- **Latency (p99):** 192.81ms
- **Error Rate:** 0.00%

## Scenario 3: Full User Journey
- **Virtual Users:** 5 concurrent
- **Duration:** 20s
- **Total Requests:** 71
- **Throughput:** 2.15 req/s
- **Latency (p50):** 154.91ms
- **Latency (p95):** 225.35ms
- **Latency (p99):** 247.49ms
- **Error Rate:** 0.00%

## Key Findings
- Load testing exposed a real production Redis serialization bug on cached product responses (`LinkedHashMap` deserialization mismatch). The cache serialization config was corrected and validated in production.
- After the Redis fix, Scenario 1 dropped from 49.49% errors to 0.00%, and product-browsing latency improved significantly.
- Write-heavy cart and auth flows were stable under configured load (0% errors), with p95 latencies in the 170-225ms range.
- On this single-instance Railway deployment, throughput is modest under realistic think-time load; scaling instance/resources would improve effective concurrent capacity as traffic grows.
