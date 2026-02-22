import http from 'k6/http';
import { check, sleep } from 'k6';
import exec from 'k6/execution';

const BASE_URL = __ENV.BASE_URL || 'https://vinyl.up.railway.app';
const SCENARIO = __ENV.SCENARIO || 'product_browsing';

const TREND_STATS = ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'];

function optionsForScenario(name) {
  if (name === 'product_browsing') {
    return {
      summaryTrendStats: TREND_STATS,
      thresholds: {
        http_req_duration: ['p(95)<500'],
        http_req_failed: ['rate<0.01'],
      },
      scenarios: {
        product_browsing: {
          executor: 'ramping-vus',
          stages: [
            { duration: '10s', target: 20 },
            { duration: '30s', target: 20 },
            { duration: '10s', target: 0 },
          ],
          gracefulRampDown: '5s',
          exec: 'productBrowsing',
        },
      },
    };
  }

  if (name === 'cart_operations') {
    return {
      summaryTrendStats: TREND_STATS,
      thresholds: {
        http_req_duration: ['p(95)<800'],
        http_req_failed: ['rate<0.02'],
      },
      scenarios: {
        cart_operations: {
          executor: 'ramping-vus',
          stages: [
            { duration: '5s', target: 10 },
            { duration: '20s', target: 10 },
            { duration: '5s', target: 0 },
          ],
          gracefulRampDown: '5s',
          exec: 'cartOperations',
        },
      },
    };
  }

  return {
    summaryTrendStats: TREND_STATS,
    thresholds: {
      http_req_duration: ['p(95)<1000'],
      http_req_failed: ['rate<0.05'],
    },
    scenarios: {
      full_user_journey: {
        executor: 'constant-vus',
        vus: 5,
        duration: '20s',
        exec: 'fullUserJourney',
      },
    },
  };
}

export const options = optionsForScenario(SCENARIO);

function jsonParams(tagName, token) {
  const headers = { 'Content-Type': 'application/json' };
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }
  return { headers, tags: { name: tagName } };
}

function readParams(tagName, token) {
  const headers = {};
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }
  return { headers, tags: { name: tagName } };
}

function think(minSeconds = 1, maxSeconds = 2) {
  sleep(Math.random() * (maxSeconds - minSeconds) + minSeconds);
}

function randomProductId(productIds) {
  if (!productIds || productIds.length === 0) {
    return 1;
  }
  const index = Math.floor(Math.random() * productIds.length);
  return productIds[index];
}

function randomDifferentProductId(productIds, existingId) {
  let next = randomProductId(productIds);
  if (!productIds || productIds.length <= 1) {
    return next;
  }
  while (next === existingId) {
    next = randomProductId(productIds);
  }
  return next;
}

export function setup() {
  let productIds = [];
  for (let i = 0; i < 5; i += 1) {
    const res = http.get(`${BASE_URL}/products`, { tags: { name: 'WARMUP_products' } });
    check(res, { 'warmup products 200': (r) => r.status === 200 });
    if (res.status === 200) {
      try {
        const products = res.json();
        if (Array.isArray(products)) {
          productIds = products.map((p) => p.id).filter((id) => Number.isInteger(id) && id > 0);
        }
      } catch (_) {
        // ignore parse errors, fallback is handled in randomProductId
      }
    }
    sleep(0.3);
  }
  return { warmed: true, productIds };
}

export function productBrowsing(data) {
  const productIds = data?.productIds || [];
  const listRes = http.get(`${BASE_URL}/products`, readParams('GET_products'));
  check(listRes, { 'GET /products is 200': (r) => r.status === 200 });

  think(1, 2);

  const productId = randomProductId(productIds);
  const detailRes = http.get(`${BASE_URL}/products/${productId}`, readParams('GET_product_by_id'));
  check(detailRes, { 'GET /products/{id} is 200': (r) => r.status === 200 });

  think(1, 2);
}

export function cartOperations(data) {
  const productIds = data?.productIds || [];
  const firstId = randomProductId(productIds);
  const secondId = randomDifferentProductId(productIds, firstId);

  const productsRes = http.get(`${BASE_URL}/products`, readParams('GET_products_cart_flow'));
  check(productsRes, { 'cart flow GET /products is 200': (r) => r.status === 200 });
  think(1, 2);

  const addOne = http.post(
    `${BASE_URL}/carts/current/items`,
    JSON.stringify({ id: firstId }),
    jsonParams('POST_cart_add_first')
  );
  check(addOne, { 'POST first cart item is 200': (r) => r.status === 200 });
  think(1, 2);

  const getOne = http.get(`${BASE_URL}/carts/current`, readParams('GET_cart_after_first_add'));
  check(getOne, { 'GET cart after first add is 200': (r) => r.status === 200 });
  think(1, 2);

  const addTwo = http.post(
    `${BASE_URL}/carts/current/items`,
    JSON.stringify({ id: secondId }),
    jsonParams('POST_cart_add_second')
  );
  check(addTwo, { 'POST second cart item is 200': (r) => r.status === 200 });
  think(1, 2);

  const updateRes = http.put(
    `${BASE_URL}/carts/current/items/${firstId}`,
    JSON.stringify({ quantity: 3 }),
    jsonParams('PUT_cart_update_quantity')
  );
  check(updateRes, { 'PUT cart quantity is 200': (r) => r.status === 200 });
  think(1, 2);

  const getTwo = http.get(`${BASE_URL}/carts/current`, readParams('GET_cart_after_update'));
  check(getTwo, { 'GET cart after update is 200': (r) => r.status === 200 });
  think(1, 2);

  const clearRes = http.del(`${BASE_URL}/carts/current/items`, null, readParams('DELETE_cart_clear'));
  check(clearRes, { 'DELETE cart clear is 204': (r) => r.status === 204 });
  think(1, 2);
}

export function fullUserJourney(data) {
  const productIds = data?.productIds || [];
  const vu = exec.vu.idInTest;
  const iter = exec.scenario.iterationInTest;
  const now = Date.now();
  const rand = Math.floor(Math.random() * 100000);
  const email = `k6user_${vu}_${iter}_${now}_${rand}@test.com`;
  const password = 'TestPass123!';
  const productId = randomProductId(productIds);

  const registerRes = http.post(
    `${BASE_URL}/users`,
    JSON.stringify({ name: `k6 user ${vu}`, email, password }),
    jsonParams('POST_register')
  );
  check(registerRes, {
    'POST /users is 201': (r) => r.status === 201,
  });
  think(1, 2);

  const loginRes = http.post(
    `${BASE_URL}/auth/login`,
    JSON.stringify({ email, password }),
    jsonParams('POST_login')
  );
  check(loginRes, { 'POST /auth/login is 200': (r) => r.status === 200 });

  let token = '';
  try {
    token = loginRes.json('token') || '';
  } catch (_) {
    token = '';
  }

  check(token, { 'login returns token': (t) => t && t.length > 0 });
  think(1, 2);

  const productsRes = http.get(`${BASE_URL}/products`, readParams('GET_products_auth', token));
  check(productsRes, { 'auth flow GET /products is 200': (r) => r.status === 200 });
  think(1, 2);

  const addRes = http.post(
    `${BASE_URL}/carts/current/items`,
    JSON.stringify({ id: productId }),
    jsonParams('POST_cart_add_auth', token)
  );
  check(addRes, { 'auth flow POST cart item is 200': (r) => r.status === 200 });
  think(1, 2);

  const cartRes = http.get(`${BASE_URL}/carts/current`, readParams('GET_cart_auth', token));
  check(cartRes, { 'auth flow GET /carts/current is 200': (r) => r.status === 200 });
  think(1, 2);

  const ordersRes = http.get(`${BASE_URL}/orders`, readParams('GET_orders_auth', token));
  check(ordersRes, { 'auth flow GET /orders is 200': (r) => r.status === 200 });
  think(1, 3);
}
