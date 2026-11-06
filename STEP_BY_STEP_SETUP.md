# Step-by-step: Lumière Beauty full-stack setup on Ubuntu

## Step 0 — Verify prerequisites

Open a terminal and check:

```bash
java -version
mvn -version
node -v
npm -v
sudo systemctl status postgresql
```

You need Java **17 or newer** (Java 21 works), Apache Maven, Node.js (Vite 6 needs modern Node), npm, PostgreSQL server, and optionally pgAdmin 4.

If Maven/Java/PostgreSQL are missing on Ubuntu:

```bash
sudo apt update
sudo apt install openjdk-21-jdk maven postgresql postgresql-contrib
sudo systemctl enable --now postgresql
```

> You do **not** need pgAdmin for Spring Boot connectivity. It is just a database management UI.

## Step 1 — Create a dedicated PostgreSQL user and database

```bash
sudo -u postgres psql
```

Inside psql (replace the example password with your own strong secret):

```sql
CREATE USER lumiere_user WITH PASSWORD 'YOUR_STRONG_PASSWORD';
CREATE DATABASE lumiere_db OWNER lumiere_user;
\q
```

If the role/database already exists, do not recreate it; instead use `ALTER ROLE lumiere_user WITH PASSWORD 'YOUR_STRONG_PASSWORD';` to update the password. If `CREATE DATABASE` reports "already exists", keep the existing database.

Check the database login from the normal Ubuntu terminal:

```bash
psql -h localhost -p 5432 -U lumiere_user -d lumiere_db
```

Enter the password, then type `\q` to exit. If this fails, solve PostgreSQL credentials before trying Spring Boot.

## Step 2 — Open the database in pgAdmin 4

Open pgAdmin 4 → Servers → **Register > Server** (or right-click Servers → Register > Server).

- General tab → Name: `Lumiere Local`
- Connection tab → Host: `localhost`, Port: `5432`, Maintenance database: `lumiere_db`, Username: `lumiere_user`, Password: the password you chose.
- Click **Save**.

Tables may be empty at this point. Hibernate creates them when the backend starts.

## Step 3 — Open the folder

Extract `lumiere-fullstack.zip`. Open the extracted folder in VS Code:

```bash
cd ~/Downloads/lumiere-fullstack
code .
```

Adjust this path if you unzipped it elsewhere. You should see folders `backend` and `frontend`.

## Step 4 — Configure the Spring Boot backend

Open a terminal in `lumiere-fullstack/backend`:

```bash
cd backend
```

The PostgreSQL configuration is in:

`backend/src/main/resources/application.properties`

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/lumiere_db}
spring.datasource.username=${DB_USERNAME:lumiere_user}
spring.datasource.password=${DB_PASSWORD:}
spring.jpa.hibernate.ddl-auto=update
```

Set environment variables in this **same backend terminal**, using your actual password:

```bash
export DB_PASSWORD='YOUR_STRONG_PASSWORD'
export APP_ADMIN_EMAIL='admin@lumiere.local'
export APP_ADMIN_PASSWORD='CHOOSE_A_DIFFERENT_LONG_ADMIN_PASSWORD'
```

The admin email/password are YOUR CHOICE. They are not default credentials and will not work until you set them. First startup seeds one admin account if the email does not already exist. Use 8+ characters for admin password (ideally much longer). Don't commit these secrets.

## Step 5 — Run Spring Boot

Still in `backend/`:

```bash
mvn spring-boot:run
```

The backend listens at `http://localhost:8080`. Hibernate will create five tables:

1. `products`
2. `user_accounts`
3. `auth_tokens`
4. `customer_orders`
5. `order_items`

It also inserts 10 sample products once (nine Active, one Draft) and creates your initial Admin account. Existing data is not overwritten when the server restarts.

Test in a **new** terminal:

```bash
curl -i http://localhost:8080/api/products
```

Expected: HTTP **200** with a JSON array of 9 Active products. Test login with your chosen email/password:

```bash
curl -i -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@lumiere.local","password":"YOUR_REAL_ADMIN_PASSWORD"}'
```

Expected: HTTP 200 with `token` and `user`. Never publish real tokens or passwords. If you already run another Spring Boot app on port 8080, stop that app first (`sudo lsof -i :8080`) or change `SERVER_PORT` and your frontend API URL to match.

## Step 6 — Start React

Open a **second** terminal, keeping Spring Boot running:

```bash
cd ~/Downloads/lumiere-fullstack/frontend
cp .env.example .env
npm install
npm run dev
```

Open `http://localhost:5173` in your browser. `.env` contains:

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

If you change `.env`, restart `npm run dev`. Start Vite specifically on 5173, because the backend's CORS allowlist covers this origin. If that port is occupied, stop the other Vite process or update `FRONTEND_ORIGINS` in the backend to include the new port and restart Spring Boot.

## Step 7 — Test the full connection

1. Visit `http://localhost:5173/shop`. You should see products loaded from PostgreSQL through Spring Boot.
2. Visit `/login` and sign in using `APP_ADMIN_EMAIL` / `APP_ADMIN_PASSWORD` that you chose. You are redirected to `/admin`.
3. Go to `/admin/products` → **Add product**. Enter a name, brand, category, price, stock and image URL (e.g. `/images/cream.svg`), then save. The product is written to PostgreSQL and shown on `/shop` if its status is `Active`.
4. Go to `/admin/users` to see your account and create other users with explicit passwords. Admins can change roles, suspend accounts, and delete accounts without orders.
5. Sign out. Visit `/register` and create a customer account (password at least 8 characters).
6. Add an in-stock product to the shopping cart, then go to `/checkout`. Enter delivery information and place an order. The backend verifies stock and prices, saves an order and its line items, and decreases stock atomically.
7. Visit `/orders` to see the new customer's order history. Sign back in as an admin and visit `/admin/orders` to view all orders.
8. Open pgAdmin → Databases → lumiere_db → Schemas → public → Tables to inspect the data. Right-click a table → **View/Edit Data → All Rows**.

Or run this SQL in pgAdmin's Query Tool:

```sql
SELECT id, name, price, stock, status, deleted FROM products ORDER BY id;
SELECT id, name, email, role, status FROM user_accounts ORDER BY id;
SELECT id, account_id, total, payment_method, created_at FROM customer_orders ORDER BY id DESC;
SELECT id, order_id, product_name, quantity, unit_price, line_total FROM order_items;
```

## Step 8 — Troubleshooting

**`password authentication failed for user "lumiere_user"`**: Recheck `DB_PASSWORD`. Ensure you can log in with `psql -h localhost -U lumiere_user -d lumiere_db`. Use `ALTER ROLE` from postgres if necessary. You must set environment variables in the terminal running Maven.

**`database "lumiere_db" does not exist`**: Create the database in Step 1. pgAdmin does not automatically create the database just by adding a connection.

**Port 8080 already in use**: Run `sudo lsof -i :8080` and stop the correct process before restarting the backend. Don't kill unrelated services.

**React says "Cannot reach Spring Boot" / `ERR_NETWORK`**: Make sure Maven shows a successful startup and `curl http://localhost:8080/api/products` works. Check `.env` URL and restart Vite.

**CORS error**: React must be on `http://localhost:5173` or `http://127.0.0.1:5173`, or you must set `FRONTEND_ORIGINS` to your actual origin before starting Spring Boot.

**HTTP 401**: Log in. Token is missing/invalid/expired, or the account was suspended.

**HTTP 403**: You are logged in as a Customer and attempted an admin-only operation.

**HTTP 405**: You called an endpoint with the wrong HTTP method (e.g. GET instead of POST).

**HTTP 409 during checkout**: Not enough stock; refresh and adjust the cart. **HTTP 409 during registration**: email already exists.

**Admin login fails**: `APP_ADMIN_EMAIL` and `APP_ADMIN_PASSWORD` must be set before the first relevant startup. If an account with that email exists, the seeder intentionally never replaces it or resets its password. Change the existing admin password through a controlled password reset procedure rather than editing plaintext database fields.

## Important limitations

- The backend **requires PostgreSQL**; there is no database server bundled in the zip. You must configure it on your machine.
- Cart state remains in browser localStorage until checkout; it does not sync across devices.
- Payments, emails, and contact forms are **not** implemented as live services. The payment method is only captured as order metadata.
- Intended for local coursework development, not ready for production deployment. Add secure cookie-based authentication, CSRF defenses where needed, migrations, tests, rate-limiting and HTTPS before public launch.
