# Lumière Beauty React frontend (connected version)

This is the original frontend with product pages, admin screens, login/register, order checkout, and history wired to the Spring Boot API in `../backend`.

**Follow [`../STEP_BY_STEP_SETUP.md`](../STEP_BY_STEP_SETUP.md) first.**

```bash
cp .env.example .env
npm install
npm run dev
```

`src/services/api.js` is the Axios client. `src/context/AuthContext.jsx` stores the current user/token; `src/context/ShopContext.jsx` fetches products from Spring Boot. Cart state remains in localStorage until checkout; all final orders are saved to PostgreSQL.

The original `docs/` folder is retained as coursework reference. Its notes about demo-only backend are historical and do not describe the changes in this connected version.
