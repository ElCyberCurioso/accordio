import { strict as assert } from "node:assert";
import test from "node:test";
import worker from "../src/index.js";
import { listSuggestedSongs } from "../src/db.js";
import { fakeBucket, fakeD1 } from "./fake-d1.mjs";

/*
 * Alta manual de usuarios desde Administración (POST /api/users) y sugerencias
 * de la portada (GET /api/songs/suggested).
 */

const entorno = () => ({ DB: fakeD1(), BUCKET: fakeBucket(), AUTH_SECRET: "secreto-de-prueba" });

async function pedir(env, method, path, { body, token } = {}) {
  const res = await worker.fetch(new Request("https://accordio.site" + path, {
    method,
    headers: {
      ...(token ? { Authorization: "Bearer " + token } : {}),
      ...(body !== undefined ? { "Content-Type": "application/json" } : {})
    },
    body: body !== undefined ? JSON.stringify(body) : undefined
  }), env);
  const texto = await res.text();
  let datos = {};
  try { datos = texto ? JSON.parse(texto) : {}; } catch { datos = { raw: texto }; }
  return { status: res.status, datos };
}

const alta = (env, email) => pedir(env, "POST", "/auth/register", {
  body: { email, password: "contrasena-larga", name: email.split("@")[0] }
});

test("el administrador da de alta un usuario y este puede entrar", async () => {
  const env = entorno();
  const admin = await alta(env, "admin@accordio.test");
  const r = await pedir(env, "POST", "/api/users", {
    token: admin.datos.token,
    body: { email: "nuevo@accordio.test", password: "otra-clave-larga", name: "Nuevo", role: "editor" }
  });
  assert.equal(r.status, 201);
  assert.equal(r.datos.user.email, "nuevo@accordio.test");
  assert.equal(r.datos.user.role, "editor");
  assert.equal(r.datos.user.password_hash, undefined);

  const login = await pedir(env, "POST", "/auth/login",
    { body: { email: "nuevo@accordio.test", password: "otra-clave-larga" } });
  assert.equal(login.status, 200);
});

test("sin rol, el alta manual crea un usuario normal", async () => {
  const env = entorno();
  const admin = await alta(env, "admin@accordio.test");
  const r = await pedir(env, "POST", "/api/users", {
    token: admin.datos.token,
    body: { email: "normal@accordio.test", password: "otra-clave-larga", name: "" }
  });
  assert.equal(r.status, 201);
  assert.equal(r.datos.user.role, "user");
});

test("el alta manual funciona con las altas públicas cerradas", async () => {
  const env = entorno();
  const admin = await alta(env, "admin@accordio.test");
  await pedir(env, "PUT", "/api/settings", { token: admin.datos.token, body: { registrationOpen: false } });
  const r = await pedir(env, "POST", "/api/users", {
    token: admin.datos.token,
    body: { email: "invitada@accordio.test", password: "otra-clave-larga", name: "Invitada" }
  });
  assert.equal(r.status, 201);
});

test("solo el administrador puede dar de alta usuarios", async () => {
  const env = entorno();
  await alta(env, "admin@accordio.test");
  const normal = await alta(env, "marta@accordio.test");
  const r = await pedir(env, "POST", "/api/users", {
    token: normal.datos.token,
    body: { email: "x@accordio.test", password: "otra-clave-larga", name: "X" }
  });
  assert.equal(r.status, 403);
  const anonimo = await pedir(env, "POST", "/api/users", {
    body: { email: "y@accordio.test", password: "otra-clave-larga", name: "Y" }
  });
  assert.ok(anonimo.status === 401 || anonimo.status === 403);
});

test("el alta manual valida datos, rol y duplicados", async () => {
  const env = entorno();
  const admin = await alta(env, "admin@accordio.test");
  const t = admin.datos.token;
  const corta = await pedir(env, "POST", "/api/users",
    { token: t, body: { email: "a@accordio.test", password: "corta", name: "A" } });
  assert.equal(corta.status, 400);
  const malEmail = await pedir(env, "POST", "/api/users",
    { token: t, body: { email: "no-es-email", password: "otra-clave-larga", name: "A" } });
  assert.equal(malEmail.status, 400);
  const malRol = await pedir(env, "POST", "/api/users",
    { token: t, body: { email: "a@accordio.test", password: "otra-clave-larga", name: "A", role: "dios" } });
  assert.equal(malRol.status, 400);
  const repetido = await pedir(env, "POST", "/api/users",
    { token: t, body: { email: "ADMIN@accordio.test", password: "otra-clave-larga", name: "A" } });
  assert.equal(repetido.status, 409);
});

function dbFalso(salida) {
  const consultas = [];
  return {
    consultas,
    prepare(sql) {
      return {
        bind(...v) {
          consultas.push({ sql: sql.replace(/\s+/g, " ").trim(), valores: v });
          return { all: async () => ({ results: salida }) };
        }
      };
    }
  };
}

test("las sugeridas son públicas, sin papelera, al azar y del dueño del catálogo", async () => {
  const db = dbFalso([{ id: "s1" }]);
  const r = await listSuggestedSongs(db, "admin-1", 8);
  assert.deepEqual(r.map((s) => s.id), ["s1"]);
  const { sql, valores } = db.consultas[0];
  assert.match(sql, /s\.visibility = 'public'/);
  assert.match(sql, /s\.deleted_at = 0/);
  assert.match(sql, /ORDER BY RANDOM\(\)/);
  assert.deepEqual(valores, ["admin-1", 8]);
});

test("las sugeridas acotan el límite", async () => {
  const db = dbFalso([]);
  await listSuggestedSongs(db, null, 500);
  assert.deepEqual(db.consultas[0].valores, [20]);
});
