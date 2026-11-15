/*
 * Partitura en PDF adjunta a una canción.
 *
 * Lo que de verdad importa aquí es UNA cosa: que el PDF se vea exactamente
 * cuando se ve la partitura, y nunca más. Una canción privada tiene el PDF
 * privado; publicarla lo abre y despublicarla lo vuelve a cerrar, sin tocar el
 * fichero. Lo demás (que sea un PDF, que no pese cien megas, que no queden
 * objetos huérfanos en R2) son las redes de alrededor.
 */

import { strict as assert } from "node:assert";
import test from "node:test";
import { handleApi } from "../src/api.js";
import { signToken } from "../src/auth.js";
import { MAX_PDF } from "../src/limits.js";
import { publicSong } from "../src/db.js";

const cors = {};
const SECRETO = "clave-de-prueba";

const duena = { id: "u1", role: "user" };
const otra = { id: "u2", role: "user" };
const editor = { id: "e1", role: "editor" };
const admin = { id: "a1", role: "admin" };

/** Un PDF mínimo: lo único que mira el servidor es la firma del principio. */
const PDF = new TextEncoder().encode("%PDF-1.4\n1 0 obj<<>>endobj\ntrailer<<>>\n%%EOF\n");

function escenario() {
  const users = {};
  for (const u of [duena, otra, editor, admin]) users[u.id] = { ...u, email: u.id + "@x", name: u.id };
  const songs = {
    priv: { id: "priv", owner_id: "u1", visibility: "private", deleted_at: 0, title: "Privada",
            artist: "Ella", r2_key: "songs/priv.txt", pdf_key: "", pdf_size: 0, rev: 1 },
    pub: { id: "pub", owner_id: "u1", visibility: "public", deleted_at: 0, title: "Pública",
           artist: "", r2_key: "songs/pub.txt", pdf_key: "", pdf_size: 0, rev: 1 }
  };
  const objetos = new Map();
  const db = {
    prepare(sql) {
      return {
        bind(...v) {
          return {
            first: async () => {
              if (sql.includes("FROM users WHERE id")) return users[v[0]] || null;
              if (sql.includes("FROM songs WHERE id")) return songs[v[0]] || null;
              return null;
            },
            all: async () => ({ results: [] }),
            run: async () => {
              if (sql.startsWith("UPDATE songs SET pdf_key")) {
                const s = songs[v[3]];
                s.pdf_key = v[0]; s.pdf_size = v[1]; s.rev += 1;
              } else if (sql.startsWith("UPDATE songs SET deleted_at")) {
                songs[v[2]].deleted_at = v[0];
              } else if (sql.startsWith("DELETE FROM songs")) {
                delete songs[v[0]];
              }
              return {};
            }
          };
        }
      };
    }
  };
  const BUCKET = {
    async get(clave) {
      if (!objetos.has(clave)) return null;
      const bytes = objetos.get(clave);
      return { body: bytes, text: async () => new TextDecoder().decode(bytes) };
    },
    async put(clave, cuerpo) { objetos.set(clave, cuerpo); },
    async delete(clave) { objetos.delete(clave); }
  };
  return { songs, objetos, env: { DB: db, BUCKET, AUTH_SECRET: SECRETO } };
}

async function llamar(env, method, path, user, cuerpo, cabeceras = {}) {
  const headers = { ...cabeceras };
  if (user) headers.Authorization = "Bearer " + await signToken({ sub: user.id, role: user.role }, SECRETO);
  if (cuerpo && !headers["Content-Type"]) headers["Content-Type"] = "application/pdf";
  return handleApi(new Request("https://v.test" + path, { method, headers, body: cuerpo }),
                   env, new URL("https://v.test" + path), cors);
}

const subir = (env, id, user, cuerpo = PDF) => llamar(env, "PUT", `/api/songs/${id}/pdf`, user, cuerpo);
const pedir = (env, id, user) => llamar(env, "GET", `/api/songs/${id}/pdf`, user);

test("el PDF de una partitura privada solo lo ven su dueña y el administrador", async () => {
  const { env } = escenario();
  assert.equal((await subir(env, "priv", duena)).status, 200);

  assert.equal((await pedir(env, "priv", duena)).status, 200);
  assert.equal((await pedir(env, "priv", admin)).status, 200);
  // Los mismos 404 que da la partitura: ni siquiera se confirma que existe.
  assert.equal((await pedir(env, "priv", null)).status, 404, "un visitante no lo ve");
  assert.equal((await pedir(env, "priv", otra)).status, 404, "otra usuaria no lo ve");
  assert.equal((await pedir(env, "priv", editor)).status, 404,
    "el editor cuida el catálogo, no el cajón de cada una");
});

test("el PDF de una partitura pública lo ve cualquiera, también sin sesión", async () => {
  const { env } = escenario();
  assert.equal((await subir(env, "pub", duena)).status, 200);
  const res = await pedir(env, "pub", null);
  assert.equal(res.status, 200);
  assert.equal(res.headers.get("Content-Type"), "application/pdf");
  assert.match(res.headers.get("Content-Disposition"), /^inline; filename="Publica\.pdf"$/);
  assert.deepEqual(new Uint8Array(await res.arrayBuffer()), PDF);
});

test("el acceso sigue a la partitura: despublicar cierra el PDF y publicar lo abre", async () => {
  const { env, songs } = escenario();
  await subir(env, "pub", duena);
  assert.equal((await pedir(env, "pub", null)).status, 200);

  songs.pub.visibility = "private";
  assert.equal((await pedir(env, "pub", null)).status, 404, "despublicada: fuera");
  assert.equal((await pedir(env, "pub", otra)).status, 404);
  assert.equal((await pedir(env, "pub", duena)).status, 200);

  songs.pub.visibility = "public";
  assert.equal((await pedir(env, "pub", null)).status, 200, "publicada otra vez: dentro");
});

test("en la papelera el PDF deja de verse, igual que la partitura", async () => {
  const { env, songs } = escenario();
  await subir(env, "pub", duena);
  songs.pub.deleted_at = Date.now();
  assert.equal((await pedir(env, "pub", null)).status, 404);
  assert.equal((await pedir(env, "pub", duena)).status, 404);
});

test("una privada no se cachea; una pública se revalida siempre", async () => {
  const { env } = escenario();
  await subir(env, "priv", duena);
  await subir(env, "pub", duena);
  assert.equal((await pedir(env, "priv", duena)).headers.get("Cache-Control"), "private, no-store");
  assert.equal((await pedir(env, "pub", null)).headers.get("Cache-Control"), "private, no-cache");
});

test("solo sube y quita el PDF quien puede editar la partitura", async () => {
  const { env, songs } = escenario();
  assert.equal((await subir(env, "priv", null)).status, 401);
  assert.equal((await subir(env, "priv", otra)).status, 403);
  assert.equal((await subir(env, "priv", editor)).status, 403, "privada ajena: tampoco el editor");
  assert.equal((await subir(env, "pub", otra)).status, 403);
  assert.equal(songs.priv.pdf_key, "");

  assert.equal((await subir(env, "pub", editor)).status, 200, "pública: el editor sí");
  assert.equal((await subir(env, "priv", admin)).status, 200);

  assert.equal((await llamar(env, "DELETE", "/api/songs/pub/pdf", otra)).status, 403);
  assert.equal((await llamar(env, "DELETE", "/api/songs/pub/pdf", null)).status, 401);
  assert.notEqual(songs.pub.pdf_key, "");
  assert.equal((await llamar(env, "DELETE", "/api/songs/pub/pdf", duena)).status, 200);
  assert.equal(songs.pub.pdf_key, "");
  assert.equal((await pedir(env, "pub", null)).status, 404);
});

test("lo que no es un PDF no entra, se llame como se llame", async () => {
  const { env, songs, objetos } = escenario();
  const html = new TextEncoder().encode("<html><script>alert(1)</script></html>");
  assert.equal((await subir(env, "pub", duena, html)).status, 400);
  assert.equal((await subir(env, "pub", duena, new Uint8Array(0))).status, 400);
  assert.equal(songs.pub.pdf_key, "");
  assert.equal(objetos.size, 0, "no se guarda nada en R2 antes de validar");
});

test("un PDF que pasa del tope se rechaza sin guardarlo", async () => {
  const { env, objetos } = escenario();
  const gordo = new Uint8Array(MAX_PDF + 1);
  gordo.set(PDF);
  assert.equal((await subir(env, "pub", duena, gordo)).status, 413);
  assert.equal(objetos.size, 0);
});

test("sustituir o quitar el PDF no deja el fichero anterior en R2", async () => {
  const { env, songs, objetos } = escenario();
  await subir(env, "pub", duena);
  const primera = songs.pub.pdf_key;
  await subir(env, "pub", duena);
  assert.notEqual(songs.pub.pdf_key, primera, "cada subida estrena clave");
  assert.deepEqual([...objetos.keys()], [songs.pub.pdf_key]);
  await llamar(env, "DELETE", "/api/songs/pub/pdf", duena);
  assert.equal(objetos.size, 0);
});

test("subir el PDF sube rev, que es como se entera la app", async () => {
  const { env, songs } = escenario();
  const antes = songs.pub.rev;
  const res = await subir(env, "pub", duena);
  assert.equal(songs.pub.rev, antes + 1);
  const { song } = await res.json();
  assert.equal(song.hasPdf, true);
  assert.equal(song.pdfSize, PDF.length);
});

test("la ficha dice si hay PDF pero nunca enseña la clave de R2", () => {
  const ficha = publicSong({ id: "x", pdf_key: "pdf/x/abc.pdf", pdf_size: 1234 }, true);
  assert.equal(ficha.hasPdf, true);
  assert.equal(ficha.pdfSize, 1234);
  assert.equal(JSON.stringify(ficha).includes("pdf/x/abc.pdf"), false);
  assert.equal(publicSong({ id: "y", pdf_key: "", pdf_size: 99 }).hasPdf, false);
  assert.equal(publicSong({ id: "y", pdf_key: "", pdf_size: 99 }).pdfSize, 0);
});

test("el borrado definitivo se lleva también el PDF", async () => {
  const { env, songs, objetos } = escenario();
  await subir(env, "pub", duena);
  objetos.set("songs/pub.txt", new Uint8Array(1));
  songs.pub.deleted_at = Date.now();
  assert.equal((await llamar(env, "DELETE", "/api/songs/pub?hard=1", duena)).status, 200);
  assert.equal(objetos.size, 0);
});
