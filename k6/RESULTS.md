# Load Test Results — Vynl

**Environment:** Railway Hobby plan (single instance)  
**Date:** 2026-02-22  
**Tool:** k6 v1.6.1

Base URL tested: `https://vinyl.up.railway.app`

## Scenario 1: Product Catalog Browsing
- **Virtual Users:** 20 concurrent (ramp 10s, hold 30s, ramp down 10s)
- **Duration:** 30s sustained load
- **Total Requests:** 493
- **Throughput:** 9.23 req/s
- **Latency (p50):** 164.94ms
- **Latency (p95):** 306.27ms
- **Latency (p99):** 323.15ms
- **Error Rate:** 49.49%

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
- `GET /products/{id}` is currently failing with `500` on the deployed service, which drove Scenario 1 error rate to 49.49%. This is a backend endpoint issue on production, not a load-script issue.
- Write-heavy cart and auth flows were stable under configured load (0% errors), with p95 latencies in the 170-225ms range.
- On this single-instance Railway deployment, throughput is modest under realistic think-time load; fixing the product-detail 500 and then scaling instance/resources would improve effective concurrent capacity.
