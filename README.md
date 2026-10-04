# Bank Management System

A simple, complete full-stack banking application built for learning and interview/demo purposes.
It demonstrates the classic flow:

```
Customer Login → View Balance → Deposit → Withdraw → Transfer → Updated Balance → Transaction History
```

> **Demo credentials in this project are for local development only. Do not reuse these passwords anywhere else.**

---

## 1. Features

### Customer
- Register / login (auto-login after registration)
- Dashboard with balance, account details and recent transactions
- Deposit money with description
- Withdraw money (blocked if amount exceeds balance)
- Transfer money to another customer's account (with review/confirm step)
- Transaction history with filters (type, status, date range)
- Transaction detail view
- Update profile (phone, address, date of birth)

### Admin
- Login
- Dashboard: total customers, total accounts, active accounts, total transactions, recent transactions
- View all customers, search customers, view customer detail with their accounts
- Create a new customer (with login, account and optional initial deposit)
- Activate / deactivate a customer (deactivated customers cannot log in or operate)
- View all accounts, search accounts
- Activate / block / close accounts (blocked/closed accounts reject all transactions)
- View all transactions in the bank

### Security rules
- JWT-based stateless authentication (Spring Security)
- BCrypt password hashing
- Role-based authorization: `ROLE_ADMIN`, `ROLE_CUSTOMER`
- A customer can **only** access their own account and transaction data
- Amounts are validated (`amount > 0`), sufficient-balance is enforced, and
  `BLOCKED` / `CLOSED` accounts cannot transact

---

## 2. Technology Stack

| Layer      | Technology |
|------------|------------|
| Backend    | Java 17+, Spring Boot 3.5 |
| Security   | Spring Security + JWT (jjwt 0.12) |
| ORM        | Spring Data JPA / Hibernate |
| Database   | H2 (file-based, no separate server needed) |
| Frontend   | React 18, React Router 6, Axios, Vite 5 |
| Build      | Maven (backend), Vite (frontend) |

---

## 3. Architecture

Simple layered architecture (no microservices, no extra infrastructure):

```
React (Axios)
   │  HTTP + JWT Bearer token
   ▼
Controller  (@RestController)   – validates input, maps URLs
   ▼
Service     (@Service)         – business rules, @Transactional transfers
   ▼
Repository  (Spring Data JPA)  – database access
   ▼
H2 Database
```

Security is a servlet filter (`JwtAuthenticationFilter`) that reads the
`Authorization: Bearer <token>` header, validates the JWT and populates the
Spring Security context. Role checks use `@PreAuthorize`.

---

## 4. Database Design

### Entities and relationships

```
User            1 ── 1      Customer       (login account ↔ bank customer)
Customer        1 ── N      BankAccount    (a customer can have many accounts)
BankAccount     1 ── N      Transaction    (an account has many transactions)
```

### Customer
| Field | Notes |
|-------|-------|
| id | PK, auto |
| customerId | business ID, e.g. `CUST001` (unique) |
| firstName, lastName | required |
| email | unique, valid format |
| phone | required |
| address | optional |
| dateOfBirth | optional |
| createdAt | auto timestamp |
| status | `ACTIVE` / `INACTIVE` |

### User (authentication)
| Field | Notes |
|-------|-------|
| id | PK |
| username | unique |
| password | BCrypt hash |
| role | `ADMIN` / `CUSTOMER` |
| customer | link to Customer (null for admin) |
| enabled | disabled users cannot log in |

### BankAccount
| Field | Notes |
|-------|-------|
| id | PK |
| accountNumber | unique, 10 digits, e.g. `1000010001` |
| customer | owner |
| accountType | `SAVINGS` / `CURRENT` |
| balance | `BigDecimal(19,2)` — never float/double |
| status | `ACTIVE` / `BLOCKED` / `CLOSED` |
| createdAt | auto timestamp |

### Transaction
| Field | Notes |
|-------|-------|
| id | PK |
| transactionId | business ID, e.g. `TXN00001` (unique) |
| account | the account this record belongs to |
| transactionType | `DEPOSIT` / `WITHDRAW` / `TRANSFER` |
| direction | `CREDIT` / `DEBIT` (how it affected this account) |
| amount | `BigDecimal(19,2)` |
| balanceAfterTransaction | snapshot of balance after the operation |
| description | free text |
| referenceAccount | the other account (for transfers) |
| transactionDate | auto timestamp |
| status | `SUCCESS` / `FAILED` |

---

## 5. Project Structure

```
bank-management-system/
├── backend/
│   ├── pom.xml
│   ├── src/main/resources/application.properties
│   └── src/main/java/com/example/bankmanagement/
│       ├── BankManagementApplication.java
│       ├── config/          # IdGenerator, DataSeeder (sample data)
│       ├── controller/      # Auth, Account, Transaction, Customer, Admin
│       ├── service/         # Auth, Account, Transaction, Customer, Admin
│       ├── repository/      # Spring Data JPA interfaces
│       ├── entity/          # User, Customer, BankAccount, Transaction + enums
│       ├── dto/             # request/response objects
│       ├── security/        # SecurityConfig, JwtService, JwtAuthenticationFilter
│       └── exception/       # GlobalExceptionHandler + custom exceptions
└── frontend/
    ├── package.json
    ├── vite.config.js       # dev proxy /api → http://localhost:8081
    └── src/
        ├── main.jsx
        ├── App.jsx          # routes, role guards
        ├── styles.css
        ├── context/AuthContext.jsx
        ├── services/api.js  # axios instance + interceptors + formatters
        ├── components/      # AppLayout, ProtectedRoute, Modal, StatusBadge, TransactionTable
        └── pages/
            ├── Login.jsx, Register.jsx, NotFound.jsx
            ├── customer/    # Dashboard, Account, Deposit, Withdraw,
            │                # Transfer, Transactions, TransactionDetail, Profile
            └── admin/       # AdminDashboard, AdminCustomers,
                             # AdminAccounts, AdminTransactions
```

---

## 6. How to Run

Prerequisites: **JDK 17+**, **Maven 3.8+**, **Node.js 18+**.

### Backend (port 8081)

```bash
cd backend
mvn spring-boot:run
```

The backend starts on **http://localhost:8081**.
Port 8081 is used because 8080 was already occupied on the development machine —
change `server.port` in `application.properties` if you like.

### Frontend (port 5173)

```bash
cd frontend
npm install
npm run dev
```

Open **http://localhost:5173**.

The Vite dev server proxies `/api/*` to `http://localhost:8081`, so both
servers must be running.

### H2 Console (optional, for inspecting data)

- URL: **http://localhost:8081/h2-console**
- Driver: `org.h2.Driver`
- JDBC URL: `jdbc:h2:file:./data/bankdb`
- User: `sa`
- Password: *(empty)*

The database is file-based (`backend/data/bankdb`), so data survives restarts.
Delete the `backend/data` folder to start fresh — the sample data will be
recreated on the next start.

---

## 7. Demo Credentials

Created automatically by `DataSeeder` on first start (skipped if users already exist):

| Role | Username | Password | Details |
|------|----------|----------|---------|
| ADMIN | `admin` | `admin123` | no customer account |
| CUSTOMER | `rahul` | `rahul123` | CUST001, account `1000010001`, balance ₹50,000 |
| CUSTOMER | `amit` | `amit123` | CUST002, account `1000010002`, balance ₹30,000 |

Sample transactions (TXN00001–TXN00006) are also seeded.

---

## 8. API Endpoints

Base URL: `http://localhost:8081/api` — all endpoints except auth require
`Authorization: Bearer <jwt>`.

### Authentication
| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/auth/register` | public | register a new customer (creates customer + user + savings account) |
| POST | `/auth/login` | public | login, returns JWT |

### Customer (self-service)
| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| GET | `/customers/profile` | CUSTOMER | own profile |
| PUT | `/customers/profile` | CUSTOMER | update phone/address/dateOfBirth |

### Accounts & banking operations
| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| GET | `/accounts/my-account` | CUSTOMER | own account details |
| GET | `/accounts/{accountNumber}` | CUSTOMER/ADMIN | account by number (customer = own only) |
| POST | `/accounts/deposit` | CUSTOMER | `{ "amount": 5000, "description": "Salary" }` |
| POST | `/accounts/withdraw` | CUSTOMER | `{ "amount": 1000, "description": "ATM" }` |
| POST | `/accounts/transfer` | CUSTOMER | `{ "toAccountNumber": "1000010002", "amount": 1500, "description": "Rent" }` |

### Transactions
| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| GET | `/transactions` | CUSTOMER | own history; optional params `type`, `status`, `startDate`, `endDate` |
| GET | `/transactions/{transactionId}` | CUSTOMER | own transaction detail |

### Admin
| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| GET | `/admin/stats` | ADMIN | totals for dashboard |
| GET | `/admin/customers` | ADMIN | list/search customers (`?search=`) |
| GET | `/admin/customers/{id}` | ADMIN | customer detail + accounts |
| POST | `/admin/customers` | ADMIN | create customer with account |
| PUT | `/admin/customers/{id}/status` | ADMIN | `ACTIVE` / `INACTIVE` |
| GET | `/admin/accounts` | ADMIN | list/search accounts |
| PUT | `/admin/accounts/{accountNumber}/status` | ADMIN | `ACTIVE` / `BLOCKED` / `CLOSED` |
| GET | `/admin/transactions` | ADMIN | all transactions |

### Error format

All errors return a consistent JSON body with the proper HTTP status:

```json
{ "message": "Insufficient balance", "status": 400, "timestamp": "..." }
```

Handled cases: account/customer not found (404), invalid amount (400),
insufficient balance (400), blocked/closed account (400), duplicate
email/username/account (409), bad credentials / deactivated user (401),
missing token (401), wrong role (403), validation errors (400).

---
## 9. Important Flows

### JWT Authentication Flow

```
React Login form
    │ POST /api/auth/login { username, password }
    ▼
Spring Security  →  DaoAuthenticationProvider verifies BCrypt hash
    │
    ▼
JwtService generates a signed JWT (subject=username, claims=role, 24h expiry)
    │
    ▼
React stores { token, user } in localStorage
    │
    ▼
Axios request interceptor attaches: Authorization: Bearer <token>
    │
    ▼
JwtAuthenticationFilter (once per request)
  → parses & validates the token
  → loads the user (CustomUserDetailsService)
  → populates SecurityContext with ROLE_ADMIN / ROLE_CUSTOMER
    │
    ▼
@PreAuthorize("hasRole('ADMIN')") etc. decide access
```

- React route guards: `ProtectedRoute` checks the stored role and redirects
  (`/admin/*` → ADMIN, `/customer/*` → CUSTOMER).
- On 401 the axios response interceptor clears the stored login and redirects
  to `/login`.

### Deposit Flow

1. Customer enters amount + description, submits.
2. `AccountService.deposit(...)` runs in a transaction:
   - reject `amount <= 0`
   - load own account; reject if not found
   - reject if account status is `BLOCKED` or `CLOSED`, or the customer is `INACTIVE`
   - `balance = balance.add(amount)`
   - save account, then insert a `DEPOSIT / CREDIT` transaction record with the
     new balance snapshot
3. Response returns the transaction (`balanceAfterTransaction` = new balance).
4. React shows: “₹5,000.00 deposited successfully. New balance: ₹25,000.00”.

### Withdrawal Flow

Same as deposit, plus a sufficient-balance check:

```
amount > 0 && account ACTIVE && balance >= amount
```

If `balance < amount` → `InsufficientBalanceException` → HTTP 400
`{ "message": "Insufficient balance" }` and **no money moves** (the exception
rolls the transaction back).

### Transfer Flow (atomic)

```
POST /api/accounts/transfer { toAccountNumber, amount, description }
```

`AccountService.transfer(...)` is annotated `@Transactional`:

1. Validate `amount > 0`.
2. Load sender account (from the JWT-authenticated customer) — must be `ACTIVE`.
3. Load receiver account by number — must exist and be `ACTIVE`.
4. Reject sender == receiver.
5. Check sender balance ≥ amount.
6. `sender.balance -= amount`, `receiver.balance += amount` — both saved.
7. Insert **two** transaction rows:
   - sender: `TRANSFER / DEBIT`, `referenceAccount = receiver`
   - receiver: `TRANSFER / CREDIT`, `referenceAccount = sender`
8. If any step throws, Spring rolls everything back — money can never be
   deducted from the sender without being credited to the receiver.

The UI adds a review step before confirming:

```
Transfer Summary
From: 1000010001   To: 1000010002   Amount: ₹1,500.00
[ Confirm Transfer ]  [ Back ]
```

### Transaction History Flow

- `GET /api/transactions` loads the authenticated customer's account(s) and
  returns their transaction rows newest-first.
- Optional filters (`type`, `status`, `startDate`, `endDate`) are applied with
  simple stream filters in `TransactionService`.
- `GET /api/transactions/{transactionId}` additionally checks ownership: if the
  transaction does not belong to this customer it returns 404
  (“Transaction not found”) so customers cannot probe other people's data.

---

## 10. Why these choices (good interview talking points)

- **`BigDecimal` for money** — `double`/`float` cannot represent decimal
  fractions exactly (0.1 + 0.2 ≠ 0.3), which is unacceptable for currency.
- **`@Transactional` transfer** — makes the debit + credit + two ledger rows
  atomic; a failure anywhere rolls everything back.
- **Stateless JWT** — the server needs no session store; any request can be
  authenticated by its token alone (easy to scale, easy for a React SPA).
- **BCrypt** — passwords are stored as salted one-way hashes; even a database
  leak does not reveal them.
- **DTOs** — entities are never exposed directly to the client, so internal
  fields (e.g. password hashes) can never leak.
- **H2 file database** — zero installation, yet data persists between restarts;
  perfect for a demo that must run anywhere with just Java + Maven.

## 11. Deployment

Step-by-step production deployment guides (both free-tier friendly):

| Guide | Stack |
|---|---|
| [DEPLOYMENT_RENDER_VERCEL.md](DEPLOYMENT_RENDER_VERCEL.md) | Spring Boot on **Render** + React on **Vercel** |
| [DEPLOYMENT_AWS.md](DEPLOYMENT_AWS.md) | Single **AWS EC2** instance running Spring Boot + nginx serving the React build |

The backend already reads `PORT`, `JDBC_URL`, `JWT_SECRET`, `JWT_EXPIRATION_MS`
and `ALLOWED_ORIGINS` from environment variables (with localhost defaults), and
the frontend reads `VITE_API_URL` — so no code changes are needed to deploy.
