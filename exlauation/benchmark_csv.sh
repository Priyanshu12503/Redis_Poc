#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${1:-http://localhost:8091}"
ITERATIONS="${2:-50}"
OUT_DIR="${3:-exlauation/results}"
PAYLOAD_BYTES="${4:-200000}"

RAW_CSV="${OUT_DIR}/benchmark_raw.csv"
SUMMARY_CSV="${OUT_DIR}/benchmark_summary.csv"
TMP_BODY="${OUT_DIR}/.resp.json"

mkdir -p "${OUT_DIR}"

echo "timestamp,approach,operation,iteration,status_code,time_ms,id,notes" > "${RAW_CSV}"

BIG_BLOCK="$(head -c "${PAYLOAD_BYTES}" < /dev/zero | tr '\0' 'x')"
HALF_BLOCK="$(head -c "$((PAYLOAD_BYTES / 2))" < /dev/zero | tr '\0' 'y')"

build_payload() {
  local name="$1"
  local age="$2"
  local occupation="$3"
  cat <<JSON
{"name":"${name}","age":${age},"occupation":"${occupation}","payload":{"profile":{"summary":"${BIG_BLOCK}","skills":["redis","spring","jpa","cache","mysql","lettuce"],"history":[{"company":"Alpha","role":"Engineer","years":3,"notes":"${HALF_BLOCK}"},{"company":"Beta","role":"Lead","years":4,"notes":"${HALF_BLOCK}"}]},"activities":[{"type":"login","timestamp":1712121000,"attributes":{"device":"android","ip":"10.0.0.1","region":"in"}},{"type":"purchase","timestamp":1712121200,"attributes":{"sku":"sku-001","amount":"1200","currency":"INR"}},{"type":"share","timestamp":1712121500,"attributes":{"channel":"email","campaign":"redis-poc"}}],"addresses":[{"city":"Delhi","country":"India","lines":["Street 1","Block A","Pin 110001"]},{"city":"Pune","country":"India","lines":["Street 2","Block B","Pin 411001"]}],"preferences":{"theme":"dark","language":"en","timezone":"Asia/Kolkata","notifications":"all"},"metrics":{"app":[{"name":"latency","value":12.45,"samples":[10,12,11,13,14]},{"name":"throughput","value":2400.00,"samples":[2200,2300,2400,2500,2600]}],"cache":[{"name":"hit_ratio","value":0.96,"samples":[95,96,97,96,95]},{"name":"evictions","value":2.0,"samples":[1,2,2,3,2]}]},"tagsGrid":[["alpha","beta","gamma"],["delta","epsilon","zeta"],["eta","theta","iota"]]}}
JSON
}

USER_PAYLOAD="$(build_payload "U" 24 "Engineer")"
STUDENT_PAYLOAD="$(build_payload "S" 22 "Analyst")"
HUMAN_PAYLOAD="$(build_payload "H" 30 "Architect")"

USER_SEED_PAYLOAD="$(build_payload "U_base" 24 "Engineer")"
STUDENT_SEED_PAYLOAD="$(build_payload "S_base" 22 "Analyst")"
HUMAN_SEED_PAYLOAD="$(build_payload "H_base" 30 "Architect")"

extract_id() {
  sed -n 's/.*"id"[[:space:]]*:[[:space:]]*\([0-9][0-9]*\).*/\1/p' "${TMP_BODY}" | head -n1
}

run_create() {
  local approach="$1"
  local endpoint="$2"
  local payload="$3"
  local i

  for ((i = 1; i <= ITERATIONS; i++)); do
    local result status_code time_s time_ms id
    result="$(curl -s -o "${TMP_BODY}" -w "%{http_code},%{time_total}" \
      -X POST "${BASE_URL}${endpoint}" \
      -H "Content-Type: application/json" \
      -d "${payload}")"
    status_code="${result%,*}"
    time_s="${result#*,}"
    time_ms="$(awk "BEGIN { printf \"%.3f\", ${time_s} * 1000 }")"
    id="$(extract_id)"
    echo "$(date -Iseconds),${approach},create,${i},${status_code},${time_ms},${id},-" >> "${RAW_CSV}"
  done
}

run_update() {
  local approach="$1"
  local endpoint="$2"
  local id="$3"
  local i

  for ((i = 1; i <= ITERATIONS; i++)); do
    local new_name result status_code time_s time_ms
    new_name="${approach}_updated_${i}"
    result="$(curl -s -o "${TMP_BODY}" -w "%{http_code},%{time_total}" \
      -X PATCH "${BASE_URL}${endpoint}/${id}?name=${new_name}")"
    status_code="${result%,*}"
    time_s="${result#*,}"
    time_ms="$(awk "BEGIN { printf \"%.3f\", ${time_s} * 1000 }")"
    echo "$(date -Iseconds),${approach},update,${i},${status_code},${time_ms},${id},-" >> "${RAW_CSV}"
  done
}

run_get_by_id() {
  local approach="$1"
  local endpoint="$2"
  local id="$3"
  local i

  for ((i = 1; i <= ITERATIONS; i++)); do
    local result status_code time_s time_ms
    result="$(curl -s -o "${TMP_BODY}" -w "%{http_code},%{time_total}" \
      -X GET "${BASE_URL}${endpoint}/${id}")"
    status_code="${result%,*}"
    time_s="${result#*,}"
    time_ms="$(awk "BEGIN { printf \"%.3f\", ${time_s} * 1000 }")"
    echo "$(date -Iseconds),${approach},get_by_id,${i},${status_code},${time_ms},${id},-" >> "${RAW_CSV}"
  done
}

echo "Running create benchmarks..."
run_create "user_cacheable" "/api/users" "${USER_PAYLOAD}"
run_create "student_redis_repo" "/api/students" "${STUDENT_PAYLOAD}"
run_create "human_redis_template" "/api/humans" "${HUMAN_PAYLOAD}"

echo "Preparing IDs for update benchmarks..."
user_create_result="$(curl -s -o "${TMP_BODY}" -w "%{http_code}" \
  -X POST "${BASE_URL}/api/users" \
  -H "Content-Type: application/json" \
  -d "${USER_SEED_PAYLOAD}")"
user_id="$(extract_id)"

student_create_result="$(curl -s -o "${TMP_BODY}" -w "%{http_code}" \
  -X POST "${BASE_URL}/api/students" \
  -H "Content-Type: application/json" \
  -d "${STUDENT_SEED_PAYLOAD}")"
student_id="$(extract_id)"

human_create_result="$(curl -s -o "${TMP_BODY}" -w "%{http_code}" \
  -X POST "${BASE_URL}/api/humans" \
  -H "Content-Type: application/json" \
  -d "${HUMAN_SEED_PAYLOAD}")"
human_id="$(extract_id)"

if [[ -z "${user_id}" || "${user_create_result}" -lt 200 || "${user_create_result}" -ge 300 ]]; then
  echo "Failed to create seed user for update benchmark."
  exit 1
fi

if [[ -z "${student_id}" || "${student_create_result}" -lt 200 || "${student_create_result}" -ge 300 ]]; then
  echo "Failed to create seed student for update benchmark."
  exit 1
fi

if [[ -z "${human_id}" || "${human_create_result}" -lt 200 || "${human_create_result}" -ge 300 ]]; then
  echo "Failed to create seed human for benchmark."
  exit 1
fi

echo "Running update benchmarks..."
run_update "user_cacheable" "/api/users" "${user_id}"
run_update "student_redis_repo" "/api/students" "${student_id}"
run_update "human_redis_template" "/api/humans" "${human_id}"

echo "Running get-by-id benchmarks..."
run_get_by_id "user_cacheable" "/api/users" "${user_id}"
run_get_by_id "student_redis_repo" "/api/students" "${student_id}"
run_get_by_id "human_redis_template" "/api/humans" "${human_id}"

awk -F, '
NR==1 { next }
{
  key=$2 "," $3
  if ($5 ~ /^[0-9]+$/) {
    count[key]++
    sum[key]+=$6
    if (min[key]==0 || $6<min[key]) min[key]=$6
    if ($6>max[key]) max[key]=$6
    if ($5>=200 && $5<300) ok[key]++
  }
}
END {
  print "approach,operation,count,success_count,success_pct,avg_ms,min_ms,max_ms"
  for (k in count) {
    split(k, arr, ",")
    success=ok[k]+0
    pct=(count[k] > 0 ? (success*100.0/count[k]) : 0)
    avg=(count[k] > 0 ? (sum[k]/count[k]) : 0)
    printf "%s,%s,%d,%d,%.2f,%.3f,%.3f,%.3f\n", arr[1], arr[2], count[k], success, pct, avg, min[k], max[k]
  }
}
' "${RAW_CSV}" > "${SUMMARY_CSV}"

rm -f "${TMP_BODY}"

echo "Done."
echo "Raw CSV: ${RAW_CSV}"
echo "Summary CSV: ${SUMMARY_CSV}"
