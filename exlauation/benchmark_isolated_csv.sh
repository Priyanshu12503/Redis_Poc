#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${1:-http://localhost:8091}"
ITERATIONS="${2:-100}"
OUT_DIR="${3:-exlauation/results}"
PAYLOAD_BYTES="${4:-200000}"

RAW_CSV="${OUT_DIR}/benchmark_isolated_raw.csv"
SUMMARY_CSV="${OUT_DIR}/benchmark_isolated_summary.csv"
TMP_BODY="${OUT_DIR}/.isolated_resp.json"

mkdir -p "${OUT_DIR}"
echo "timestamp,scenario,operation,iteration,status_code,time_ms,ref,notes" > "${RAW_CSV}"

BIG_BLOCK="$(head -c "${PAYLOAD_BYTES}" < /dev/zero | tr '\0' 'x')"
HALF_BLOCK="$(head -c "$((PAYLOAD_BYTES / 2))" < /dev/zero | tr '\0' 'y')"

HEAVY_PAYLOAD=$(cat <<JSON
{"profile":{"summary":"${BIG_BLOCK}","skills":["redis","spring","jpa","cache","mysql","lettuce"],"history":[{"company":"Alpha","role":"Engineer","years":3,"notes":"${HALF_BLOCK}"},{"company":"Beta","role":"Lead","years":4,"notes":"${HALF_BLOCK}"}]},"activities":[{"type":"login","timestamp":1712121000,"attributes":{"device":"android","ip":"10.0.0.1","region":"in"}},{"type":"purchase","timestamp":1712121200,"attributes":{"sku":"sku-001","amount":"1200","currency":"INR"}}],"addresses":[{"city":"Delhi","country":"India","lines":["Street 1","Block A","Pin 110001"]}],"preferences":{"theme":"dark","language":"en","timezone":"Asia/Kolkata"},"metrics":{"app":[{"name":"latency","value":12.45,"samples":[10,12,11,13,14]}]},"tagsGrid":[["alpha","beta","gamma"],["delta","epsilon","zeta"]]}
JSON
)

extract_id() {
  sed -n 's/.*"id"[[:space:]]*:[[:space:]]*\([0-9][0-9]*\).*/\1/p' "${TMP_BODY}" | head -n1
}

extract_key() {
  sed -n 's/.*"key"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' "${TMP_BODY}" | head -n1
}

record_call() {
  local scenario="$1"
  local operation="$2"
  local iteration="$3"
  local cmd="$4"
  local ref="$5"
  local notes="$6"
  local result status_code time_s time_ms

  result="$(eval "${cmd}")"
  status_code="${result%,*}"
  time_s="${result#*,}"
  time_ms="$(awk "BEGIN { printf \"%.3f\", ${time_s} * 1000 }")"
  echo "$(date -Iseconds),${scenario},${operation},${iteration},${status_code},${time_ms},${ref},${notes}" >> "${RAW_CSV}"
}

echo "Running student_repo write..."
student_read_id=""
for ((i = 1; i <= ITERATIONS; i++)); do
  call_cmd="curl -s -o \"${TMP_BODY}\" -w \"%{http_code},%{time_total}\" -X POST \"${BASE_URL}/bench/student-repo/write\" -H \"Content-Type: application/json\" -d '${HEAVY_PAYLOAD}'"
  record_call "student_repo" "write" "${i}" "${call_cmd}" "-" "redis_repository"
  if [[ -z "${student_read_id}" ]]; then
    student_read_id="$(extract_id)"
  fi
done

if [[ -z "${student_read_id}" ]]; then
  echo "Failed to produce student_repo id for read benchmark."
  exit 1
fi

echo "Running student_repo read..."
for ((i = 1; i <= ITERATIONS; i++)); do
  call_cmd="curl -s -o \"${TMP_BODY}\" -w \"%{http_code},%{time_total}\" -X GET \"${BASE_URL}/bench/student-repo/read/${student_read_id}\""
  record_call "student_repo" "read" "${i}" "${call_cmd}" "${student_read_id}" "redis_repository"
done

echo "Running human_template write..."
human_read_key=""
for ((i = 1; i <= ITERATIONS; i++)); do
  call_cmd="curl -s -o \"${TMP_BODY}\" -w \"%{http_code},%{time_total}\" -X POST \"${BASE_URL}/bench/human-template/write\" -H \"Content-Type: application/json\" -d '${HEAVY_PAYLOAD}'"
  record_call "human_template" "write" "${i}" "${call_cmd}" "-" "redis_template"
  if [[ -z "${human_read_key}" ]]; then
    human_read_key="$(extract_key)"
  fi
done

if [[ -z "${human_read_key}" ]]; then
  echo "Failed to produce human_template key for read benchmark."
  exit 1
fi

echo "Running human_template read..."
for ((i = 1; i <= ITERATIONS; i++)); do
  call_cmd="curl -s -o \"${TMP_BODY}\" -w \"%{http_code},%{time_total}\" -X GET \"${BASE_URL}/bench/human-template/read/${human_read_key}\""
  record_call "human_template" "read" "${i}" "${call_cmd}" "${human_read_key}" "redis_template"
done

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
  print "scenario,operation,count,success_count,success_pct,avg_ms,min_ms,max_ms"
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
