# Deployment Guide — AWS (EC2 + Nginx)

Deploy the whole Bank Management System on a **single AWS EC2 Ubuntu instance**:

- **Nginx** (port 80) serves the built React app and reverse-proxies `/api/*` to Spring Boot
- **Spring Boot** runs as a systemd service on `localhost:8081` (never exposed publicly)
- **H2** runs in file mode on the instance disk — data survives reboots

```
Browser
   │  http://<ec2-public-ip>
   ▼
Nginx :80  ── static files ──►  /var/www/bank-app   (React build)
   │
   └─ location /api/ ── proxy_pass ──►  http://127.0.0.1:8081   (Spring Boot jar, systemd)
                                             │
                                             ▼
                                     H2 file: /opt/bank/data/bankdb
```

Because Nginx serves the frontend and the API on the **same origin**, there is
**no CORS configuration needed at all** — the frontend keeps using its default
`/api` base URL.

> **Cost:** a `t2.micro`/`t3.micro` is free-tier eligible (750 h/month for 12
> months). Stop the instance when not using it. Everything here also works on
> any paid instance type.
>
> **Alternative architectures** are summarised in section 9 (Elastic Beanstalk,
> S3 + CloudFront). The single-EC2 approach below is the simplest to understand
> and demonstrate.

---

## 1. Prerequisites

- An AWS account
- Your local machine with the project building successfully
  (`mvn -q package` and `npm run build` both work)

---

## 2. Launch the EC2 instance

1. AWS Console → **EC2** → **Launch instance**.
2. Configure:

| Field | Value |
|-------|-------|
| Name | `bank-management` |
| AMI | **Ubuntu Server 24.04 LTS** (64-bit x86) |
| Instance type | `t2.micro` (or `t3.micro`) |
| Key pair | Create new → `bank-key.pem` → download it and keep it safe |
| Security group | Create new, name `bank-sg` with these inbound rules |

Security group inbound rules:

| Type | Port | Source | Why |
|------|------|--------|-----|
| SSH | 22 | My IP | your shell access only |
| HTTP | 80 | 0.0.0.0/0 | visitors of the app |

> Do **not** open 8081 — the backend is reached only through Nginx on the same machine.

3. Storage: default 8 GiB is fine. Click **Launch instance**.
4. Note the **Public IPv4 address** from the instance details.

---

## 3. Connect via SSH

From the folder containing your key:

```bash
chmod 400 bank-key.pem                 # Git Bash / macOS / Linux
ssh -i bank-key.pem ubuntu@<EC2-PUBLIC-IP>
```

(On Windows PowerShell, skip `chmod` and use the path to the key directly.)

---

## 4. Install Java 17 and Nginx on the server

```bash
sudo apt update -y
sudo apt install -y openjdk-17-jre-headless nginx
java -version     # should print 17.x
```

Create the app directories:

```bash
sudo mkdir -p /opt/bank
sudo mkdir -p /var/www/bank-app
sudo chown -R ubuntu:ubuntu /opt/bank /var/www/bank-app
```

---

## 5. Build the artifacts locally

On your **own machine**, in the project root:

```bash
# Backend jar
cd backend
mvn clean package -DskipTests
# → backend/target/bank-management-1.0.0.jar

# Frontend static build (uses the default '/api' base URL — Nginx proxies it)
cd ../frontend
npm install
npm run build
# → frontend/dist/
```

No `VITE_API_URL` is needed: same-origin Nginx proxying means `/api` just works.

---

## 6. Upload to the server

Still on your machine (replace the IP):

```bash
scp -i bank-key.pem backend/target/bank-management-1.0.0.jar ubuntu@<EC2-PUBLIC-IP>:/opt/bank/

scp -i bank-key.pem -r frontend/dist/* ubuntu@<EC2-PUBLIC-IP>:/var/www/bank-app/
```

---

## 7. Run the backend as a systemd service

Back on the **EC2 instance**:

```bash
sudo nano /etc/systemd/system/bank.service
```

Paste (Ctrl+O, Enter, Ctrl+X to save and exit):

```ini
[Unit]
Description=Bank Management Spring Boot API
After=network.target

[Service]
User=ubuntu
WorkingDirectory=/opt/bank
ExecStart=/usr/bin/java -jar /opt/bank/bank-management-1.0.0.jar
SuccessExitStatus=143
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Enable and start it:

```bash
sudo systemctl daemon-reload
sudo systemctl enable bank       # start on boot
sudo systemctl start bank
sudo systemctl status bank       # should say "active (running)"
```

Check the logs any time:

```bash
journalctl -u bank -f            # Ctrl+C to stop following
```

The service listens on **8081** (the default in `application.properties`).
Optionally set a production `JWT_SECRET` by adding to the unit file under `[Service]`:

```ini
Environment=JWT_SECRET=change-this-to-a-long-random-string
```

then `sudo systemctl daemon-reload && sudo systemctl restart bank`.

---

## 8. Configure Nginx

```bash
sudo nano /etc/nginx/sites-available/bank-app
```

Paste:

```nginx
server {
    listen 80;
    server_name _;

    # React static files
    root /var/www/bank-app;
    index index.html;

    # API → Spring Boot
    location /api/ {
        proxy_pass http://127.0.0.1:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    # H2 console (dev/demo convenience) — remove for anything serious
    location /h2-console/ {
        proxy_pass http://127.0.0.1:8081;
        proxy_set_header Host $host;
    }

    # SPA fallback: unknown paths serve index.html so React Router works
    location / {
        try_files $uri /index.html;
    }
}
```

Enable it and reload Nginx:

```bash
sudo ln -s /etc/nginx/sites-available/bank-app /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t          # must say "syntax is ok" / "test is successful"
sudo systemctl reload nginx
```

---

## 9. Test it

In a browser:

```
http://<EC2-PUBLIC-IP>
```

1. Login `admin / admin123` → admin dashboard.
2. Login `rahul / rahul123` → deposit, withdraw, transfer to `1000010002`.
3. Hard-refresh (Ctrl+F5) on a deep route — Nginx's `try_files` keeps it working.

CLI checks from the server:

```bash
curl http://localhost:8081/api/auth/login \
  -d '{"username":"admin","password":"admin123"}' \
  -H 'Content-Type: application/json'
```

### Redeploying a new version

```bash
# backend: upload jar then
sudo systemctl restart bank

# frontend: upload dist then — no restart needed
#   (Nginx serves whatever is in /var/www/bank-app)
```

### H2 data location

The file DB lives at `/opt/bank/data/bankdb` (relative to the service working
directory). It survives reboots. To reset to fresh demo data:

```bash
sudo systemctl stop bank
rm -rf /opt/bank/data
sudo systemctl start bank      # DataSeeder re-creates demo users
```

---

## 10. Optional: HTTPS with a real domain (Let's Encrypt)

1. Buy/use a domain (e.g. on Route 53 or any registrar) and add an **A record**
   pointing at the EC2 public IP (or an Elastic IP so it never changes).
2. In Nginx set `server_name yourdomain.com;` and reload.
3. Install Certbot:

```bash
sudo apt install -y certbot python3-certbot-nginx
sudo certbot --nginx -d yourdomain.com
```

Certbot rewrites the Nginx config for HTTPS and auto-renews. The frontend needs
no changes; cookies aren't used (JWT header auth), so nothing else breaks.

---

## 11. Alternative AWS architectures

| Option | How | When to choose |
|--------|-----|----------------|
| **This guide** (EC2 + Nginx) | one VM, both apps, systemd | demos, learning, cheapest |
| **Elastic Beanstalk** | Beanstalk Java platform for the jar (upload via console/CLI); frontend still needs a host | want AWS to manage patching/scaling |
| **S3 + CloudFront (frontend) + EC2 (backend)** | static site on S3, CloudFront in front with SPA error routing → index.html; set `VITE_API_URL=https://<backend-host>/api` and `ALLOWED_ORIGINS=https://<cloudfront-domain>`; backend publicly reachable, so enable CORS | want CDN speed for the UI and separation of concerns |

---

## 12. Troubleshooting

| Symptom | Cause | Fix |
|---------|-------|-----|
| Browser: `502 Bad Gateway` | backend not running | `sudo systemctl status bank`, check `journalctl -u bank -n 50` |
| SSH: `Permission denied (publickey)` | wrong key or user | use `ubuntu@` user and the `.pem` you launched with; `chmod 400` on Git Bash/macOS |
| `curl localhost:8081` → connection refused | jar still starting or crashed | wait ~20 s; check logs; verify `java -version` is 17 |
| Page loads but no data; browser console shows `404` on `/api/...` | Nginx config not enabled | check symlink in `sites-enabled`, `sudo nginx -t`, `sudo systemctl reload nginx` |
| Refresh on a deep route → 404 from Nginx | SPA fallback missing | ensure `try_files $uri /index.html;` in `location /` |
| `port 8081 already in use` in logs | old java process lingering | `sudo fuser -k 8081/tcp` then `sudo systemctl restart bank` |
| App reachable but login 401 for everyone | JWT_SECRET changed between restarts and old tokens invalid | just log in again; set a fixed `JWT_SECRET` to avoid this |
| Out of memory on t2.micro during Maven builds | building on the server | don't — build locally and `scp` the jar (as this guide does) |
