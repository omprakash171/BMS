#!/usr/bin/env bash
# Quick end-to-end API test for the Bank Management System.
# Backend must be running on http://localhost:8081
set -u
BASE=http://localhost:8081/api
PASS=0; FAIL=0

check() { # name expected actual
  if [ "$2" = "$3" ]; then PASS=$((PASS+1)); echo "PASS: $1";
  else FAIL=$((FAIL+1)); echo "FAIL: $1 (expected=$2 actual=$3)"; fi
}

token() { curl -s $BASE/auth/login -X POST -H 'Content-Type: application/json' -d "{\"username\":\"$1\",\"password\":\"$2\"}" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p'; }

RAHUL=$(token rahul rahul123)
AMIT=$(token amit amit123)
ADMIN=$(token admin admin123)
check "rahul login gets token" "yes" "$([ -n "$RAHUL" ] && echo yes || echo no)"
check "admin login gets token" "yes" "$([ -n "$ADMIN" ] && echo yes || echo no)"

AUTH_R="Authorization: Bearer $RAHUL"
AUTH_A="Authorization: Bearer $ADMIN"

# --- balance ---
BAL=$(curl -s $BASE/accounts/my-account -H "$AUTH_R" | sed -n 's/.*"balance":\([0-9.]*\).*/\1/p')
echo "rahul starting balance: $BAL"

# --- deposit 5000 ---
R=$(curl -s $BASE/accounts/deposit -X POST -H "$AUTH_R" -H 'Content-Type: application/json' -d '{"amount":5000,"description":"Salary"}')
check "deposit 5000" "55000.00" "$(echo "$R" | sed -n 's/.*"balanceAfterTransaction":\([0-9.]*\).*/\1/p')"

# --- withdraw 6000 ---
R=$(curl -s $BASE/accounts/withdraw -X POST -H "$AUTH_R" -H 'Content-Type: application/json' -d '{"amount":6000,"description":"Rent"}')
check "withdraw 6000" "49000.00" "$(echo "$R" | sed -n 's/.*"balanceAfterTransaction":\([0-9.]*\).*/\1/p')"

# --- withdraw too much -> 400 ---
CODE=$(curl -s -o /tmp/resp.json -w "%{http_code}" $BASE/accounts/withdraw -X POST -H "$AUTH_R" -H 'Content-Type: application/json' -d '{"amount":999999}')
check "overspend withdraw rejected (400)" "400" "$CODE"
echo "       -> $(cat /tmp/resp.json)"

# --- transfer 2000 rahul -> amit ---
R=$(curl -s $BASE/accounts/transfer -X POST -H "$AUTH_R" -H 'Content-Type: application/json' -d '{"toAccountNumber":"1000010002","amount":2000,"description":"Test transfer"}')
check "transfer sender balance" "47000.00" "$(echo "$R" | sed -n 's/.*"balanceAfterTransaction":\([0-9.]*\).*/\1/p')"
AB=$(curl -s $BASE/accounts/1000010002 -H "$AUTH_R" | sed -n 's/.*"balance":\([0-9.]*\).*/\1/p')
check "receiver (amit) balance" "32000.00" "$AB"

# --- transfer to unknown account -> 404 ---
CODE=$(curl -s -o /tmp/resp.json -w "%{http_code}" $BASE/accounts/transfer -X POST -H "$AUTH_R" -H 'Content-Type: application/json' -d '{"toAccountNumber":"9999999999","amount":100}')
check "unknown receiver (404)" "404" "$CODE"

# --- no token -> 401 ---
CODE=$(curl -s -o /dev/null -w "%{http_code}" $BASE/accounts/my-account)
check "no token rejected (401)" "401" "$CODE"

# --- customer blocked from admin endpoint -> 403 ---
CODE=$(curl -s -o /dev/null -w "%{http_code}" $BASE/admin/stats -H "$AUTH_R")
check "customer blocked from admin API (403)" "403" "$CODE"

# --- history + filters ---
N=$(curl -s "$BASE/transactions" -H "$AUTH_R" | grep -o '"transactionId"' | wc -l)
check "rahul has 6 transactions" "6" "$N"
N=$(curl -s "$BASE/transactions?type=TRANSFER" -H "$AUTH_R" | grep -o '"transactionId"' | wc -l)
check "transfer filter returns 1" "1" "$N"

# --- admin endpoints ---
S=$(curl -s $BASE/admin/stats -H "$AUTH_A")
check "admin stats totalCustomers=2" "2" "$(echo "$S" | sed -n 's/.*"totalCustomers":\([0-9]*\).*/\1/p')"
check "admin stats totalAccounts=2" "2" "$(echo "$S" | sed -n 's/.*"totalAccounts":\([0-9]*\).*/\1/p')"
N=$(curl -s "$BASE/admin/customers?search=rahul" -H "$AUTH_A" | grep -o '"customerId"' | wc -l)
check "admin search customers 'rahul'" "1" "$N"

# --- deactivate customer, verify blocked ---
ID=$(curl -s "$BASE/admin/customers?search=rahul" -H "$AUTH_A" | sed -n 's/.*"customerId":"CUST00\([0-9]\)".*/\1/p')
R=$(curl -s $BASE/admin/customers/1/status -X PUT -H "$AUTH_A" -H 'Content-Type: application/json' -d '{"status":"INACTIVE"}')
check "admin deactivates rahul" "INACTIVE" "$(echo "$R" | sed -n 's/.*"status":"\([A-Z]*\)".*/\1/p')"
CODE=$(curl -s -o /tmp/resp.json -w "%{http_code}" $BASE/accounts/deposit -X POST -H "$AUTH_R" -H 'Content-Type: application/json' -d '{"amount":100}')
check "deactivated customer cannot deposit (400)" "400" "$CODE"
CODE=$(curl -s -o /dev/null -w "%{http_code}" $BASE/auth/login -X POST -H 'Content-Type: application/json' -d '{"username":"rahul","password":"rahul123"}')
check "deactivated customer cannot login (401)" "401" "$CODE"

# --- reactivate ---
R=$(curl -s $BASE/admin/customers/1/status -X PUT -H "$AUTH_A" -H 'Content-Type: application/json' -d '{"status":"ACTIVE"}')
check "admin reactivates rahul" "ACTIVE" "$(echo "$R" | sed -n 's/.*"status":"\([A-Z]*\)".*/\1/p')"

echo "----------------------------------------"
echo "RESULT: $PASS passed, $FAIL failed"
