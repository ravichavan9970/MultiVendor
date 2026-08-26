# 🌐 MultiVendor Platform - Deployment Guide

This guide walks you through deploying the **MultiVendor Enterprise Fullstack Application** live to the cloud.

---

## ⚡ Quick Summary of Deployment Methods

| Platform | Difficulty | Cost | Recommended For |
|---|---|---|---|
| **Render.com** (Option 1) | ⭐ Easy (1-Click) | **100% Free** | Direct GitHub deployment, automatic SSL |
| **Railway.app** (Option 2) | ⭐ Easy (Instant) | Free Trial / Low Cost | Zero-config Docker deployment |
| **Neon.tech / Supabase** (Cloud DB) | ⭐ Easy | **100% Free** | Permanent Serverless PostgreSQL Database |
| **Docker / VPS / AWS EC2** (Option 3) | ⭐⭐ Intermediate | $3 - $5/mo | Full root server control |

---

## 🚀 Option 1: Deploy on Render.com (Recommended - 100% Free)

Since your project is already pushed to GitHub: [`https://github.com/ravichavan9970/MultiVendor.git`](https://github.com/ravichavan9970/MultiVendor.git)

### Step 1: Sign in to Render
1. Go to [https://render.com](https://render.com) and click **Sign In with GitHub**.

### Step 2: Create Web Service
1. In your Render Dashboard, click **New +** → **Web Service**.
2. Select your repository: **`ravichavan9970/MultiVendor`**.
3. Choose **Docker** as runtime (or choose **Java**):
   - **Name**: `multivendor-platform`
   - **Region**: Choose closest to you (e.g. *Singapore* or *Frankfurt* or *Oregon*)
   - **Branch**: `master`
   - **Root Directory**: Leave blank (or `backend`)
   - **Docker Command / File**: Uses `./Dockerfile` automatically.

### Step 3: Configure Environment Variables
Under the **Environment Variables** tab, add:
* `PORT` = `8081`
* `JWT_SECRET` = `9a8b7c6d5e4f3a2b1c0d9e8f7a6b5c4d3e2f1a0b9c8d7e6f5a4b3c2d1e0f9a8b`
* `DB_URL` = `jdbc:h2:file:./data/multivendordb;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1;MODE=MySQL` *(or your Cloud PostgreSQL URL from Neon/Supabase)*
* `DB_USERNAME` = `sa`
* `DB_PASSWORD` = `""`

### Step 4: Click "Create Web Service"
* Render will build the Maven project, create the container, and assign you a live HTTPS domain like:
  **`https://multivendor-platform.onrender.com`**
* **Public & Customer Marketplace**: `https://multivendor-platform.onrender.com`
* **Vendor Command Portal**: `https://multivendor-platform.onrender.com/vendor.html`
* **Swagger API Docs**: `https://multivendor-platform.onrender.com/swagger-ui.html`

---

## 🗄️ Setting Up a Free Remote Cloud PostgreSQL (Neon / Supabase)

If you want an external enterprise cloud database server (instead of disk files):

1. **Create Free Database**:
   - Go to [Neon.tech](https://neon.tech) or [Supabase.com](https://supabase.com).
   - Create a new project called `multivendor`.
   - Copy the PostgreSQL connection string:
     `postgres://username:password@ep-cool-fog-12345.us-east-2.aws.neon.tech/neondb?sslmode=require`
2. **Convert to JDBC Format in Render / Railway Environment Variables**:
   - `DB_URL` = `jdbc:postgresql://ep-cool-fog-12345.us-east-2.aws.neon.tech/neondb?sslmode=require`
   - `DB_USERNAME` = `username`
   - `DB_PASSWORD` = `password`
3. Spring Boot will automatically connect to your cloud PostgreSQL database and initialize all tables!

---

## 🚂 Option 2: Deploy on Railway.app

1. Go to [https://railway.app](https://railway.app) and sign in with GitHub.
2. Click **New Project** → **Deploy from GitHub repo** → select `ravichavan9970/MultiVendor`.
3. Railway will detect the `Dockerfile` automatically.
4. Add the same Environment Variables (`PORT=8081`, `JWT_SECRET`, etc.).
5. Under Settings → **Generate Domain** → Your app is live with SSL!

---

## 🐳 Option 3: Deploy with Docker Compose (VPS / AWS / DigitalOcean)

On any Ubuntu / Linux VPS:

```bash
# 1. Clone repository
git clone https://github.com/ravichavan9970/MultiVendor.git
cd MultiVendor

# 2. Launch with Docker Compose
docker-compose up -d --build

# 3. Check status
docker-compose ps
```

Your fullstack application will be live on `http://<your-server-ip>:8081`.
