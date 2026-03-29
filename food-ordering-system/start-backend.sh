#!/usr/bin/env bash

set -euo pipefail

workspace_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
log_dir="$workspace_dir/.logs"
services=(
  "customer-service"
  "menu-service"
  "order-service"
  "payment-service"
  "api-gateway"
)
pids=()
selected_services=()

if [[ -f "$workspace_dir/.env" ]]; then
  set -a
  # shellcheck disable=SC1091
  source "$workspace_dir/.env"
  set +a
else
  echo "Missing $workspace_dir/.env" >&2
  exit 1
fi

mkdir -p "$log_dir"

if [[ "$#" -gt 0 ]]; then
  for requested_service in "$@"; do
    found=false
    for service in "${services[@]}"; do
      if [[ "$service" == "$requested_service" ]]; then
        selected_services+=("$service")
        found=true
        break
      fi
    done

    if [[ "$found" == false ]]; then
      echo "Unknown service: $requested_service" >&2
      echo "Valid services: ${services[*]}" >&2
      exit 1
    fi
  done
else
  selected_services=("${services[@]}")
fi

is_selected() {
  local target="$1"

  for service in "${selected_services[@]}"; do
    if [[ "$service" == "$target" ]]; then
      return 0
    fi
  done

  return 1
}

readiness_url_for_service() {
  case "$1" in
    customer-service) printf '%s' "http://127.0.0.1:8081/customers" ;;
    menu-service) printf '%s' "http://127.0.0.1:8082/menu-items" ;;
    order-service) printf '%s' "http://127.0.0.1:8083/orders" ;;
    payment-service) printf '%s' "http://127.0.0.1:8084/payments" ;;
    api-gateway) printf '%s' "http://127.0.0.1:8080/swagger-ui.html" ;;
    *) return 1 ;;
  esac
}

set_service_pid() {
  local var_name="pid_${1//-/_}"
  printf -v "$var_name" '%s' "$2"
}

get_service_pid() {
  local var_name="pid_${1//-/_}"
  printf '%s' "${!var_name:-}"
}

start_service() {
  local service="$1"
  local service_dir="$workspace_dir/$service"
  local log_file="$log_dir/$service.log"

  if [[ ! -d "$service_dir" ]]; then
    echo "Skipping missing directory: $service_dir" >&2
    return 0
  fi

  echo "Starting $service"
  (
    cd "$service_dir"
    exec mvn spring-boot:run
  ) >"$log_file" 2>&1 &

  pids+=("$!")
  set_service_pid "$service" "$!"
}

wait_for_service() {
  local service="$1"
  local url
  local pid
  local max_attempts=90
  local attempt=1

  url="$(readiness_url_for_service "$service")" || return 0
  pid="$(get_service_pid "$service")"

  echo "Waiting for $service to become ready at $url"
  while (( attempt <= max_attempts )); do
    if curl --silent --fail --output /dev/null "$url"; then
      echo "$service is ready"
      return 0
    fi

    if [[ -n "$pid" ]] && ! kill -0 "$pid" 2>/dev/null; then
      echo "$service exited before becoming ready. See $log_dir/$service.log" >&2
      tail -n 40 "$log_dir/$service.log" >&2 || true
      return 1
    fi

    sleep 1
    ((attempt++))
  done

  echo "Timed out waiting for $service to become ready. See $log_dir/$service.log" >&2
  return 1
}

cleanup() {
  for pid in "${pids[@]:-}"; do
    kill "$pid" 2>/dev/null || true
  done
}

trap cleanup INT TERM EXIT

echo "Starting services from $workspace_dir"

for service in "${selected_services[@]}"; do
  if [[ "$service" == "api-gateway" ]]; then
    continue
  fi
  start_service "$service"
done

for service in "${selected_services[@]}"; do
  if [[ "$service" == "api-gateway" ]]; then
    continue
  fi
  wait_for_service "$service"
done

if is_selected "api-gateway"; then
  if [[ "${#selected_services[@]}" -eq 1 ]]; then
    echo "Starting api-gateway without downstream dependency checks."
  fi
  start_service "api-gateway"
  wait_for_service "api-gateway"
fi

echo "Selected services are running."
echo "Logs:"
for service in "${selected_services[@]}"; do
  echo "  $log_dir/$service.log"
done
echo "Press Ctrl+C to stop all started services."

wait
