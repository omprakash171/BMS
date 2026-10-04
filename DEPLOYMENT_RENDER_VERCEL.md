# Deployment Guide — Render (Backend) + Vercel (Frontend)

Deploy the Bank Management System with:

- **Spring Boot backend** → [Render](https://render.com) (free tier)
- **React frontend** → [Vercel](https://vercel.com) (free tier)

```
Browser
  │  HTTPS
  ▼
Vercel  (React app, https://your-app.vercel.app)
  │  HTTPS  Authorization: Bearer <JWT>        POST https://your-api.onrender.com/api/auth/login
  ▼
Render  (Spring Boot + H2, https://your-api.onrender.com)
```

---

## 0. Things to know before you start

| Topic | What it means for this project |
|-------|-------------------------------|
| **H2 database** | Render's free tier has an **ephemeral filesystem** — files are wiped on every restart/redeploy. That is why we switch H2 to **in-memory mode** via an environment variable. Data (and demo users) are re-created automatically on every start by `DataSeeder`. For real persistence you would attach Render PostgreSQL (paid) — out of scope here. |
| **Free tier cold start** | Render free web services **sleep after ~15 min of inactivity**. The first request after a nap takes ~30–60 s to respond while the service boots. Just wait and retry. |
| **Code is already production-ready** | The app reads `PORT`, `JDBC_URL`, `JWT_SECRET`, `ALLOWED_ORIGINS` (backend) and `VITE_API_URL` (frontend) from environment variables, with local-dev defaults. No code changes are needed to deploy. |
| **HTTPS** | Both platforms give you HTTPS automatically. JWT travels in the `Authorization` header (no cookies), so cross-origin HTTPS works without extra config. |

---

## 1. Push the project to GitHub

Create an empty repository on GitHub (e.g. `bank-management-system`), then from the project root:

```bash
cd bank-management-system
git init
git add .
git commit -m "Bank Management System: Spring Boot + React + H2"
git branch -M main
git remote add origin https://github.com/<your-username>/bank-management-system.git
git push -u origin main
```

> `.gitignore` already excludes `node_modules`, `target`, `dist` and the local H2 `data/` folder.

---

## 2. Deploy the backend on Render

### 2.1 Create the Web Service

1. Log in at **dashboard.render.com** → **New +** → **Web Service**.
2. Connect your GitHub account and pick the `bank-management-system` repository.
3. Fill in the form:

| Field | Value |
|-------|-------|
| Name | `bank-management-api` (this becomes your URL) |
| Region | Frankfurt / Oregon — whichever is closest |
| Branch | `main` |
| **Root Directory** | `backend` |
| Runtime | **Java** |
| **Build Command** | `mvn clean package -DskipTests` |
| **Start Command** | `java -jar target/bank-management-1.0.0.jar` |
| Instance Type | **Free** |

### 2.2 Add environment variables

Under **Advanced → Add Environment Variable** (or the *Environment* tab), add **all four**:

| Key | Value | Why |
|-----|-------|-----|
| `JAVA_VERSION` | `17` | Render's default JDK may be newer; Spring Boot 3.5 targets 17 |
| `JDBC_URL` | `jdbc:h2:mem:bankdb` | in-memory H2 (ephemeral disk on free tier) |
| `JWT_SECRET` | any long random string, e.g. 64+ characters | production signing key |
| `ALLOWED_ORIGINS` | `https://your-app.vercel.app` | the Vercel URL — set after step 3, then redeploy |

4. Click **Create Web Service**. The first build takes ~3–5 minutes.
5. When it is live you get a URL like:

```
https://bank-management-api.onrender.com
```

### 2.3 Verify the backend

```bash
curl -X POST https://bank-management-api.onrender.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

You should get back `"token": "eyJhbGci..."`. Demo users (`admin`, `rahul`, `amit`)
exist because `DataSeeder` runs on every fresh start.

---

## 3. Deploy the frontend on Vercel

1. Go to **vercel.com** → **Add New... → Project** → import the same GitHub repo.
2. In the import screen:

| Field | Value |
|-------|-------|
| Framework Preset | **Vite** |
| **Root Directory** | `frontend` (Edit → select) |
| Build Command | `npm run build` (preset default) |
| Output Directory | `dist` (preset default) |

3. Open **Environment Variables** and add:

| Key | Value |
|-----|-------|
| `VITE_API_URL` | `https://bank-management-api.onrender.com/api` |

> `VITE_` variables are **baked in at build time**. If you change the API URL
> later you must trigger a redeploy (*Deployments → ⋯ → Redeploy*).

4. Click **Deploy**. After ~1 minute you get:

```
https://your-app.vercel.app
```

### 3.1 SPA routing — already handled

`frontend/vercel.json` rewrites all paths to `index.html`, so deep links like
`/customer/transactions/TXN00013` and page refreshes work correctly.

---

## 4. Connect the two (CORS)

The backend only accepts browser requests from origins listed in `ALLOWED_ORIGINS`.

1. Copy your exact Vercel URL (including `https://`, no trailing slash).
2. In Render: your service → **Environment** → edit `ALLOWED_ORIGINS` →
   `https://your-app.vercel.app` → **Save Changes** (Render redeploys automatically).
3. Open `https://your-app.vercel.app`, log in with `admin / admin123` —
   you should land on the admin dashboard.

If you deployed the frontend first and then the backend, you only need this one
redeploy; otherwise skip it.

---

## 5. End-to-end smoke test

From the deployed site:

1. **Admin**: login `admin / admin123` → dashboard shows 2 customers, 6 seeded transactions.
2. **Customer**: login `rahul / rahul123` → balance ₹50,000.
3. Deposit ₹5,000 → balance ₹55,000, new transaction appears.
4. Transfer ₹2,000 to `1000010002` → Rahul ₹53,000, Amit's balance ₹32,000.
5. Log in as `amit / amit123` → balance ₹32,000, transfer visible in history.
6. Refresh the page (F5) on any route — you stay logged in and stay on the same page.

---

## 6. Troubleshooting

| Symptom | Cause | Fix |
|---------|-------|-----|
| `CORS policy: No 'Access-Control-Allow-Origin'` in browser console | `ALLOWED_ORIGINS` missing the exact Vercel URL | Set it (with `https://`, no trailing slash) on Render, let it redeploy |
| First page load hangs / `Network Error` then works | Render free tier cold start | Wait ~60 s and retry; keep the tab open to stay warm |
| `Failed to fetch` on every call, backend fine in Postman | Wrong `VITE_API_URL` (typo, missing `/api`) | Fix in Vercel env vars, then **Redeploy** the frontend |
| Login works but deep-link/refresh shows Vercel 404 | `vercel.json` missing | It is committed in `frontend/vercel.json`; redeploy |
| Backend build fails: `Unsupported class file major version` | Render JDK ≠ 17 | Ensure `JAVA_VERSION=17` env var is set |
| `401` on every request after logging in | Frontend and backend URL mismatch, or token blocked | Check `VITE_API_URL` points at the Render `/api`; check browser Network tab for the OPTIONS request |
| All data gone after a Render redeploy | Expected on free tier (ephemeral disk, in-memory H2) | Demo data is re-seeded automatically; for persistence use Render PostgreSQL |

---

## 7. Optional: keep the backend warm

Free services sleep after 15 minutes. To avoid the cold start during a demo,
open the API URL (or hit the login endpoint) ~1 minute before presenting.
Automated pingers work but violate the spirit of the free tier — better to just
wake it up on purpose.
