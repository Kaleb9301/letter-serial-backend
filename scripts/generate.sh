#!/usr/bin/env bash
set -euo pipefail

TEMPLATE_DIR="${1:-}"
REPO_NAME="${2:-}"
BASE_PACKAGE="${3:-}"
APP_CLASS="${4:-}"

if [[ -z "${TEMPLATE_DIR}" || -z "${REPO_NAME}" || -z "${BASE_PACKAGE}" || -z "${APP_CLASS}" ]]; then
  echo "Usage: generate.sh <template_dir> <repo_name> <base_package> <application_class>"
  exit 1
fi

OLD_PACKAGE="com.bankofabyssinia.letter_serial_backend"
OLD_PACKAGE_PATH="com.bankofabyssinia.letter_serial_backend"
NEW_PACKAGE_PATH="${BASE_PACKAGE//./\/}"

NEW_KEBAB="${REPO_NAME}"
NEW_SNAKE="$(echo "${REPO_NAME}" | tr '[:upper:]' '[:lower:]' | sed -E 's/[^a-z0-9]+/_/g; s/^_+|_+$//g')"
NEW_COMPACT="$(echo "${REPO_NAME}" | tr '[:upper:]' '[:lower:]' | sed -E 's/[^a-z0-9]+//g')"
NEW_PASCAL="${APP_CLASS%Application}"

escape_sed() {
  printf '%s' "$1" | sed -e 's/[\/&]/\\&/g'
}

replace_in_text_files() {
  local root="$1"
  local from="$2"
  local to="$3"

  local from_esc to_esc
  from_esc="$(escape_sed "${from}")"
  to_esc="$(escape_sed "${to}")"

  while IFS= read -r -d '' f; do
    if grep -Iq . "${f}"; then
      sed -i "s/${from_esc}/${to_esc}/g" "${f}"
    fi
  done < <(find "${root}" -type f -not -path "*/.git/*" -print0)
}

rename_paths() {
  local root="$1"
  local from="$2"
  local to="$3"

  while IFS= read -r -d '' p; do
    local dir base newbase target
    dir="$(dirname "${p}")"
    base="$(basename "${p}")"
    newbase="${base//${from}/${to}}"

    if [[ "${newbase}" != "${base}" ]]; then
      target="${dir}/${newbase}"
      if [[ ! -e "${target}" ]]; then
        mv "${p}" "${target}"
      fi
    fi
  done < <(find "${root}" -depth \( -type d -o -type f \) -not -path "*/.git/*" -print0)
}

replace_in_text_files "${TEMPLATE_DIR}" "${OLD_PACKAGE}" "${BASE_PACKAGE}"

for ROOT in "src/main/java" "src/test/java"; do
  if [[ -d "${TEMPLATE_DIR}/${ROOT}/${OLD_PACKAGE_PATH}" ]]; then
    mkdir -p "${TEMPLATE_DIR}/${ROOT}/${NEW_PACKAGE_PATH}"
    shopt -s dotglob
    mv "${TEMPLATE_DIR}/${ROOT}/${OLD_PACKAGE_PATH}/"* "${TEMPLATE_DIR}/${ROOT}/${NEW_PACKAGE_PATH}/" || true
    shopt -u dotglob
    rmdir -p "${TEMPLATE_DIR}/${ROOT}/${OLD_PACKAGE_PATH}" 2>/dev/null || true
  fi
done

OLD_MAIN_FILE="${TEMPLATE_DIR}/src/main/java/${NEW_PACKAGE_PATH}/LetterSerialBackendApplication.java"
NEW_MAIN_FILE="${TEMPLATE_DIR}/src/main/java/${NEW_PACKAGE_PATH}/${APP_CLASS}.java"

if [[ -f "${OLD_MAIN_FILE}" ]]; then
  mv "${OLD_MAIN_FILE}" "${NEW_MAIN_FILE}"
fi

replace_in_text_files "${TEMPLATE_DIR}" "LetterSerialBackendApplication" "${APP_CLASS}"

replace_in_text_files "${TEMPLATE_DIR}" "letter-serial-backend" "${NEW_KEBAB}"
replace_in_text_files "${TEMPLATE_DIR}" "letter_serial_backend" "${NEW_SNAKE}"
replace_in_text_files "${TEMPLATE_DIR}" "letterserialbackend" "${NEW_COMPACT}"
replace_in_text_files "${TEMPLATE_DIR}" "LetterSerialBackend" "${NEW_PASCAL}"
replace_in_text_files "${TEMPLATE_DIR}" "LETTER_SERIAL_BACKEND" "$(echo "${NEW_SNAKE}" | tr '[:lower:]' '[:upper:]')"

rename_paths "${TEMPLATE_DIR}" "letter-serial-backend" "${NEW_KEBAB}"
rename_paths "${TEMPLATE_DIR}" "letter_serial_backend" "${NEW_SNAKE}"
rename_paths "${TEMPLATE_DIR}" "letterserialbackend" "${NEW_COMPACT}"
rename_paths "${TEMPLATE_DIR}" "LetterSerialBackend" "${NEW_PASCAL}"

POM="${TEMPLATE_DIR}/pom.xml"
if [[ -f "${POM}" ]]; then
  GROUP_ID="${BASE_PACKAGE%.*}"
  if [[ "${GROUP_ID}" == "${BASE_PACKAGE}" ]]; then
    GROUP_ID="${BASE_PACKAGE}"
  fi

  sed -i "s#<groupId>com\\.bankofabyssinia</groupId>#<groupId>${GROUP_ID}</groupId>#g" "${POM}"
  sed -i "s#<artifactId>letter-serial-backend</artifactId>#<artifactId>${REPO_NAME}</artifactId>#g" "${POM}"

  if grep -q "<name/>" "${POM}"; then
    sed -i "s#<name/>#<name>${REPO_NAME}</name>#g" "${POM}"
  elif grep -q "<name>.*</name>" "${POM}"; then
    sed -i "s#<name>.*</name>#<name>${REPO_NAME}</name>#g" "${POM}"
  else
    sed -i "/<artifactId>${REPO_NAME//\//\\/}<\\/artifactId>/a\\  <name>${REPO_NAME}</name>" "${POM}"
  fi
fi

echo "Generated project:"
echo "  repo_name=${REPO_NAME}"
echo "  base_package=${BASE_PACKAGE}"
echo "  application_class=${APP_CLASS}"
echo "  kebab=${NEW_KEBAB}"
echo "  snake=${NEW_SNAKE}"
echo "  compact=${NEW_COMPACT}"
echo "  pascal=${NEW_PASCAL}"
