package com.guitarchords.app.sync

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * La dirección del servidor la escribe una persona en un campo de texto, así que
 * aquí se fija lo que el cliente tiene que entender. Cada caso es una forma real
 * de equivocarse, no una variante inventada.
 */
class BaseUrlTest {

    @Test
    fun `la direccion buena se queda igual`() {
        assertEquals("https://accordio.site", normalizarBase("https://accordio.site"))
    }

    @Test
    fun `una ruta de mas se recorta`() {
        // Este era el fallo del 404: dejaba cada peticion en
        // /api/api/sync/changes y el servidor respondia «ruta no encontrada»
        // con toda la razon.
        assertEquals("https://accordio.site", normalizarBase("https://accordio.site/api"))
        assertEquals("https://accordio.site", normalizarBase("https://accordio.site/api/"))
        assertEquals("https://accordio.site", normalizarBase("https://accordio.site/"))
    }

    @Test
    fun `la direccion vieja del worker se muda al dominio propio`() {
        // guitarchords-sync.*.workers.dev ya no sirve nada: wrangler.toml solo
        // declara los dominios propios, asi que Cloudflare contesta 404 (1042).
        // Quien la tuviera guardada llevaba pidiendo a un host vacio.
        assertEquals("https://accordio.site",
            normalizarBase("https://guitarchords-sync.elcybercurioso.workers.dev"))
        assertEquals("https://accordio.site",
            normalizarBase("https://guitarchords-sync.elcybercurioso.workers.dev/api"))
        assertEquals("https://accordio.site",
            normalizarBase("http://guitarchords-sync.otracuenta.workers.dev"))
    }

    @Test
    fun `otros workers dev no se tocan, que pueden ser de alguien mas`() {
        assertEquals("https://otra-cosa.alguien.workers.dev",
            normalizarBase("https://otra-cosa.alguien.workers.dev"))
    }

    @Test
    fun `www se quita porque el servidor manda el apex`() {
        assertEquals("https://accordio.site", normalizarBase("https://www.accordio.site"))
    }

    @Test
    fun `http sube a https porque el 308 no se sigue`() {
        assertEquals("https://accordio.site", normalizarBase("http://accordio.site"))
    }

    @Test
    fun `sin esquema se asume https`() {
        assertEquals("https://accordio.site", normalizarBase("accordio.site"))
        assertEquals("https://accordio.site", normalizarBase("  accordio.site/api  "))
    }

    @Test
    fun `localhost se queda en http para poder desarrollar`() {
        assertEquals("http://localhost:8787", normalizarBase("http://localhost:8787"))
        assertEquals("http://127.0.0.1:8787", normalizarBase("http://127.0.0.1:8787/api"))
    }

    @Test
    fun `el puerto se conserva`() {
        assertEquals("https://ejemplo.test:8443", normalizarBase("https://ejemplo.test:8443/x"))
    }

    @Test
    fun `la consulta y el fragmento no son parte de la base`() {
        assertEquals("https://accordio.site", normalizarBase("https://accordio.site?a=1"))
        assertEquals("https://accordio.site", normalizarBase("https://accordio.site#x"))
    }

    @Test
    fun `vacio sigue vacio, que de eso se queja el cliente`() {
        assertEquals("", normalizarBase(""))
        assertEquals("", normalizarBase("   "))
        assertEquals("", normalizarBase("https://"))
    }

    @Test
    fun `las mayusculas del servidor no cuentan`() {
        assertEquals("https://accordio.site", normalizarBase("HTTPS://WWW.Accordio.Site/API"))
    }
}
