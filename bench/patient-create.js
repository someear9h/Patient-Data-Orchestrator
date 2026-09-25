import http from 'k6/http';
import { check } from 'k6';

const TOKEN = __ENV.TOKEN;
if (!TOKEN) throw new Error('TOKEN env var required');

const PHASE = __ENV.PHASE || 'measure';

// warmup: 20 VUs for 30s. Purpose: JIT-compile + fill Hikari pool. Discarded.
// measure: 10 VUs for 60s. Below the knee. ~150-200 req/s. This is your data.
const profiles = {
  warmup:  { executor: 'constant-vus', vus: 20, duration: '30s' },
  measure: { executor: 'constant-vus', vus: 10, duration: '60s' },
};

export const options = {
  discardResponseBodies: true,
  scenarios: { patients: profiles[PHASE] },
  summaryTrendStats: ['avg', 'med', 'p(90)', 'p(95)', 'p(99)', 'max'],
};

export default function () {
  const id = `${__VU}-${__ITER}-${Date.now()}-${Math.random().toString(36).slice(2)}`;
  const body = JSON.stringify({
    name: `User ${id}`,
    email: `user-${id}@bench.local`,
    address: 'bench',
    dateOfBirth: '1990-01-01',
    registeredDate: '2026-01-01',
  });
  const res = http.post('http://localhost:4004/api/patients', body, {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${TOKEN}`,
    },
  });
  check(res, { created: (r) => r.status === 200 || r.status === 201 });
  if (res.status !== 200 && res.status !== 201) {
    console.log(`FAIL status=${res.status}`);
  }
}