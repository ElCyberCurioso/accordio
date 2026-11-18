/*
 * Contraste de los colores de la interfaz, en claro y en oscuro.
 *
 * Las reglas estaban escritas (CONTEXTO §4.8) pero se vigilaban a mano: «el
 * coral de marca sobre crema da 2,4:1», «el gris del kit se queda en 3,2:1».
 * Aquí se calculan a partir de los mismos tokens que se sirven en
 * /static/accordio.css, así que mover un color que deje un texto sin leerse
 * hace fallar el test en vez de descubrirse en una captura.
 *
 * Se comprueban los ROLES (--ui-*), que es lo que piden los componentes. Los
 * tintes translúcidos (rgba) no entran: su color final depende de lo que
 * tengan debajo.
 */

import test from "node:test";
import assert from "node:assert/strict";
import { WEB_CSS } from "../src/web-html.js";

/** Declaraciones `--nombre:valor` de un bloque de CSS. */
function declaraciones(bloque) {
  const mapa = {};
  for (const m of bloque.replace(/\/\*[\s\S]*?\*\//g, "").matchAll(/(--[\w-]+)\s*:\s*([^;]+);/g)) {
    mapa[m[1]] = m[2].trim();
  }
  return mapa;
}

/** El cuerpo `{…}` que sigue a [cabecera], respetando llaves anidadas. */
function cuerpoDe(cabecera) {
  const desde = WEB_CSS.indexOf(cabecera);
  assert.notEqual(desde, -1, `no encuentro el bloque ${cabecera}`);
  const abre = WEB_CSS.indexOf("{", desde);
  let nivel = 0;
  for (let i = abre; i < WEB_CSS.length; i++) {
    if (WEB_CSS[i] === "{") nivel++;
    else if (WEB_CSS[i] === "}" && --nivel === 0) return WEB_CSS.slice(abre + 1, i);
  }
  throw new Error(`bloque sin cerrar: ${cabecera}`);
}

const claro = declaraciones(cuerpoDe(":root {"));
const oscuro = { ...claro, ...declaraciones(cuerpoDe("[data-theme=dark] {")) };

/** Sigue las cadenas de var() hasta dar con un color; null si no es opaco. */
function resolver(tema, nombre, saltos = 0) {
  assert.ok(saltos < 12, `var() en bucle resolviendo ${nombre}`);
  const valor = tema[nombre];
  assert.ok(valor, `el token ${nombre} no está definido`);
  const ref = /^var\((--[\w-]+)\)$/.exec(valor);
  if (ref) return resolver(tema, ref[1], saltos + 1);
  return /^#[0-9a-f]{6}$/i.test(valor) ? valor : null;
}

function luminancia(hex) {
  const canal = (i) => {
    const c = parseInt(hex.slice(i, i + 2), 16) / 255;
    return c <= 0.03928 ? c / 12.92 : ((c + 0.055) / 1.055) ** 2.4;
  };
  return 0.2126 * canal(1) + 0.7152 * canal(3) + 0.0722 * canal(5);
}

function contraste(a, b) {
  const [clara, oscura] = [luminancia(a), luminancia(b)].sort((x, y) => y - x);
  return (clara + 0.05) / (oscura + 0.05);
}

/*
 * [texto, fondo, mínimo]. 4,5:1 es el AA de WCAG para texto corrido; los
 * rótulos en negrita de botones y etiquetas pasan con 3:1 (texto grande o
 * componente de interfaz).
 */
const PAREJAS = [
  ["--ui-text", "--ui-bg", 4.5],
  ["--ui-text", "--ui-surface", 4.5],
  ["--ui-head", "--ui-surface", 4.5],
  ["--ui-text-muted", "--ui-surface", 4.5],
  ["--ui-text-muted", "--ui-bg", 4.5],
  ["--ui-text-subtle", "--ui-surface-alt", 4.5],
  // Los acordes sobre el crema de la hoja se quedan en 4,41:1 con coral-700,
  // que es la rampa que el kit da para LEER. Van a 18 px y en seminegrita, así
  // que se les pide algo menos que al texto corrido; el suelo evita que baje.
  ["--ui-chord", "--ui-bg", 4.4],
  ["--ui-chord", "--ui-surface", 4.5],
  ["--ui-danger-text", "--ui-surface-alt", 4.5],
  ["--ui-state-text", "--ui-surface", 4.5],
  ["--ui-accent", "--ui-surface-alt", 4.5],
  ["--ui-on-accent", "--ui-accent", 4.5],
  ["--ui-on-active", "--ui-active", 4.5],
  ["--ui-on-state", "--ui-state", 4.5],
  ["--ui-header-text", "--ui-header-bg", 4.5],
  // Texto seleccionado: en oscuro salía casi blanco sobre turquesa claro.
  ["--ui-selection-text", "--ui-selection-bg", 4.5],
  // Blanco sobre el coral de marca: 2,78:1. Es la pareja del kit para la
  // «acción destacada» («Nueva», confirmar un borrado) y no llega al 3:1 de
  // los rótulos grandes. Decisión de marca conocida; el suelo impide empeorarla.
  ["--ui-on-danger", "--ui-danger-solid", 2.7]
];

for (const [nombreTema, tema] of [["claro", claro], ["oscuro", oscuro]]) {
  test(`contraste de los roles de color en ${nombreTema}`, () => {
    const fallos = [];
    for (const [texto, fondo, minimo] of PAREJAS) {
      const a = resolver(tema, texto);
      const b = resolver(tema, fondo);
      assert.ok(a && b, `${texto} y ${fondo} tienen que ser colores opacos`);
      const r = contraste(a, b);
      if (r < minimo) fallos.push(`${texto} (${a}) sobre ${fondo} (${b}): ${r.toFixed(2)}:1, pide ${minimo}:1`);
    }
    assert.deepEqual(fallos, []);
  });
}
