import http from 'k6/http';
import { check } from 'k6';

export const options = {
  vus: 3,
  duration: '30s',
  thresholds: {
    http_req_duration: ['p(95)<300'],
    http_req_failed: ['rate<0.05'],
  },
};

const BASE_URL = __ENV.BASE_URL;

export default function () {
  const payload = JSON.stringify({
    customerId: `PJ-PERF-${__VU}-${__ITER}`,
    companyName: 'Empresa Performance LTDA',
    daysOverdue: 45,
    outstandingAmount: 15000.00,
    creditScore: 420,
    productType: 'CREDIT_CARD',
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const response = http.post(`${BASE_URL}/api/v1/strategies`, payload, params);

  check(response, {
    'status is 201': (r) => r.status === 201,
    'response has customerId': (r) => r.json('customerId') !== undefined,
    'response has creditAction': (r) => r.json('creditAction') !== undefined,
  });
}
