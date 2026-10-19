package com.guitarchords.app.ui.icons

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import com.guitarchords.app.ui.theme.extendedColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/*
 * Iconos de Accordio. Set completo, dibujado para esta app.
 *
 * No son ficheros: los trazos están aquí y el ImageVector se construye con los
 * colores de donde se pinte. Es a propósito. Cada icono tiene DOS tonos —el
 * trazo, y UN detalle en coral que es el que explica qué hace— y ese coral
 * cambia entre claro y oscuro. Un vector drawable con el color dentro
 * necesitaría dos copias de cada icono y un juego de recursos por tema.
 *
 * El trazo sale por omisión de `LocalContentColor`, o sea del color del texto
 * de alrededor, para que el mismo icono valga sobre una tarjeta clara y sobre
 * la barra teal sin pasarle nada.
 *
 * CÓMO SE PINTAN: `Icon(AccordioIcons.x(), desc, tint = Color.Unspecified)`.
 * El tinte hay que apagarlo SIEMPRE: `Icon` tiñe con `LocalContentColor` por
 * omisión y un tinte aplana el icono entero a un solo color, que se lleva por
 * delante el detalle en coral. Con `Unspecified` manda lo que se construye
 * aquí, que ya es del color que toca.
 *
 * La rejilla: 24 dp, área viva de 20, trazo 1,7 y remates redondeados.
 */

/** Trazos de un icono, separados por el papel que hacen. */
private class IconoKit(
    val trazos: List<String> = emptyList(),
    val trazosAcento: List<String> = emptyList(),
    val rellenos: List<String> = emptyList(),
    val rellenosAcento: List<String> = emptyList()
)

private const val REJILLA = 24f
private const val GROSOR = 1.7f

private fun IconoKit.construir(color: Color, acento: Color, nombre: String): ImageVector {
    val b = ImageVector.Builder(
        name = nombre,
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = REJILLA, viewportHeight = REJILLA
    )
    fun trazar(datos: String, tinta: Color) {
        b.addPath(
            pathData = PathParser().parsePathString(datos).toNodes(),
            stroke = SolidColor(tinta),
            strokeLineWidth = GROSOR,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        )
    }
    rellenos.forEach {
        b.addPath(pathData = PathParser().parsePathString(it).toNodes(), fill = SolidColor(color))
    }
    rellenosAcento.forEach {
        b.addPath(pathData = PathParser().parsePathString(it).toNodes(), fill = SolidColor(acento))
    }
    trazos.forEach { trazar(it, color) }
    trazosAcento.forEach { trazar(it, acento) }
    return b.build()
}
private val DatosAcorde = IconoKit(
    trazos = listOf(
        "M6 4h12a1 1 0 0 1 1 1v14a1 1 0 0 1 -1 1h-12a1 1 0 0 1 -1 -1v-14a1 1 0 0 1 1 -1z",
        "M9.7 4v16M14.3 4v16",
        "M5 9.3h14M5 14.7h14"
    ),
    trazosAcento = emptyList(),
    rellenos = emptyList(),
    rellenosAcento = listOf(
        "M9.7 12 m-1.8 0 a1.8 1.8 0 1 0 3.6 0 a1.8 1.8 0 1 0 -3.6 0z"
    )
)

private val DatosActualizar = IconoKit(
    trazos = listOf(
        "M12 12 m-9 0 a9 9 0 1 0 18 0 a9 9 0 1 0 -18 0z"
    ),
    trazosAcento = listOf(
        "M12 16.5v-9M8.5 11L12 7.5l3.5 3.5"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosAjustes = IconoKit(
    trazos = listOf(
        "M6 4v8M6 16v4M12 4v2M12 10v10M18 4v10M18 18v2",
        "M6 14 m-2 0 a2 2 0 1 0 4 0 a2 2 0 1 0 -4 0z",
        "M18 16 m-2 0 a2 2 0 1 0 4 0 a2 2 0 1 0 -4 0z"
    ),
    trazosAcento = listOf(
        "M12 8 m-2 0 a2 2 0 1 0 4 0 a2 2 0 1 0 -4 0z"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosAnterior = IconoKit(
    trazos = emptyList(),
    trazosAcento = listOf(
        "M15 5l-7 7 7 7"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosAprender = IconoKit(
    trazos = listOf(
        "M2.5 9.5L12 5l9.5 4.5L12 14z",
        "M6.5 11.5v4c0 1.5 2.5 3 5.5 3s5.5-1.5 5.5-3v-4"
    ),
    trazosAcento = listOf(
        "M21.5 9.5v5"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosArrastrar = IconoKit(
    trazos = listOf(
        "M5 10h14M5 14h14"
    ),
    trazosAcento = listOf(
        "M9 6l3-3 3 3M9 18l3 3 3-3"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosBorrar = IconoKit(
    trazos = listOf(
        "M9.5 6.5v-2h5v2",
        "M6 6.5l1 14h10l1-14",
        "M10 10.5v6M14 10.5v6"
    ),
    trazosAcento = listOf(
        "M4 6.5h16"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosBorrarDelTodo = IconoKit(
    trazos = listOf(
        "M4 6.5h16M9.5 6.5v-2h5v2",
        "M6 6.5l1 14h10l1-14"
    ),
    trazosAcento = listOf(
        "M9.5 10.5l5 6M14.5 10.5l-5 6"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosBuscar = IconoKit(
    trazos = listOf(
        "M10.5 10.5 m-6.5 0 a6.5 6.5 0 1 0 13 0 a6.5 6.5 0 1 0 -13 0z"
    ),
    trazosAcento = listOf(
        "M15.5 15.5l5 5"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosCambioAcordes = IconoKit(
    trazos = listOf(
        "M4 8h15M15.5 4.5L19 8l-3.5 3.5"
    ),
    trazosAcento = listOf(
        "M20 16H5M8.5 12.5L5 16l3.5 3.5"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosCandado = IconoKit(
    trazos = listOf(
        "M8 10.5v-3a4 4 0 0 1 8 0v3",
        "M7.5 10.5h9a2 2 0 0 1 2 2v6a2 2 0 0 1 -2 2h-9a2 2 0 0 1 -2 -2v-6a2 2 0 0 1 2 -2z"
    ),
    trazosAcento = listOf(
        "M12 14v3"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosCerrar = IconoKit(
    trazos = emptyList(),
    trazosAcento = listOf(
        "M6 6l12 12M18 6L6 18"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosCompartir = IconoKit(
    trazos = listOf(
        "M18 5 m-2.5 0 a2.5 2.5 0 1 0 5 0 a2.5 2.5 0 1 0 -5 0z",
        "M6 12 m-2.5 0 a2.5 2.5 0 1 0 5 0 a2.5 2.5 0 1 0 -5 0z",
        "M18 19 m-2.5 0 a2.5 2.5 0 1 0 5 0 a2.5 2.5 0 1 0 -5 0z"
    ),
    trazosAcento = listOf(
        "M8.8 10.4l6.4-3.8M8.8 13.6l6.4 3.8"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosDesplegar = IconoKit(
    trazos = emptyList(),
    trazosAcento = listOf(
        "M5 9l7 7 7-7"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosDisco = IconoKit(
    trazos = listOf(
        "M12 12 m-9 0 a9 9 0 1 0 18 0 a9 9 0 1 0 -18 0z",
        "M6.5 12A5.5 5.5 0 0 1 12 6.5"
    ),
    trazosAcento = listOf(
        "M12 12 m-2.5 0 a2.5 2.5 0 1 0 5 0 a2.5 2.5 0 1 0 -5 0z"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosEditar = IconoKit(
    trazos = listOf(
        "M15 4.5l4.5 4.5L9 19.5H4.5V15z"
    ),
    trazosAcento = listOf(
        "M12.5 7l4.5 4.5"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosEnlace = IconoKit(
    trazos = listOf(
        "M10 14a4 4 0 0 0 5.7 0l3-3a4 4 0 0 0-5.7-5.7l-1 1"
    ),
    trazosAcento = listOf(
        "M14 10a4 4 0 0 0-5.7 0l-3 3a4 4 0 0 0 5.7 5.7l1-1"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosEstadisticas = IconoKit(
    trazos = listOf(
        "M4 4v16h16",
        "M9 16v-4M13 16V9"
    ),
    trazosAcento = listOf(
        "M17 16V6"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosEstrella = IconoKit(
    trazos = listOf(
        "M12 2.9L14.4 9.7L21.5 9.8L15.8 14.1L17.9 21L12 16.9L6.1 21L8.2 14.1L2.5 9.8L9.6 9.7Z"
    ),
    trazosAcento = emptyList(),
    rellenos = listOf(
        "M12 2.9L14.4 9.7L21.5 9.8L15.8 14.1L17.9 21L12 16.9L6.1 21L8.2 14.1L2.5 9.8L9.6 9.7Z"
    ),
    rellenosAcento = emptyList()
)

private val DatosEstrellaHueca = IconoKit(
    trazos = listOf(
        "M12 2.9L14.4 9.7L21.5 9.8L15.8 14.1L17.9 21L12 16.9L6.1 21L8.2 14.1L2.5 9.8L9.6 9.7Z"
    ),
    trazosAcento = emptyList(),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosFallado = IconoKit(
    trazos = listOf(
        "M12 12 m-9 0 a9 9 0 1 0 18 0 a9 9 0 1 0 -18 0z"
    ),
    trazosAcento = listOf(
        "M9 9l6 6M15 9l-6 6"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosGuitarra = IconoKit(
    trazos = listOf(
        "M12.9 14.9A5 5 0 1 1 9.1 11.1A3.5 3.5 0 1 1 12.9 14.9Z",
        "M15.1 8.9L19.5 4.5",
        "M18.4 3.4l2.2 2.2"
    ),
    trazosAcento = listOf(
        "M8.6 15.4 m-1.6 0 a1.6 1.6 0 1 0 3.2 0 a1.6 1.6 0 1 0 -3.2 0z"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosHecho = IconoKit(
    trazos = emptyList(),
    trazosAcento = listOf(
        "M5 12.5l4.5 4.5L19 7.5"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosImportar = IconoKit(
    trazos = listOf(
        "M4 14v4a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-4"
    ),
    trazosAcento = listOf(
        "M12 3.5v11M7.5 10l4.5 4.5 4.5-4.5"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosImprimir = IconoKit(
    trazos = listOf(
        "M7 8.5v-5h10v5",
        "M7 17H5a2 2 0 0 1-2-2v-4.5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2V15a2 2 0 0 1-2 2h-2"
    ),
    trazosAcento = listOf(
        "M8 14h8a1 1 0 0 1 1 1v4.5a1 1 0 0 1 -1 1h-8a1 1 0 0 1 -1 -1v-4.5a1 1 0 0 1 1 -1z"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosLimpiar = IconoKit(
    trazos = listOf(
        "M8.5 5H19a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H8.5L3 12z"
    ),
    trazosAcento = listOf(
        "M11.5 9.5l5 5M16.5 9.5l-5 5"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosLogrado = IconoKit(
    trazos = listOf(
        "M12 12 m-9 0 a9 9 0 1 0 18 0 a9 9 0 1 0 -18 0z"
    ),
    trazosAcento = listOf(
        "M8 12.5l2.8 2.8L16.5 9.5"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosMas = IconoKit(
    trazos = emptyList(),
    trazosAcento = listOf(
        "M12 5v14M5 12h14"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosMenos = IconoKit(
    trazos = emptyList(),
    trazosAcento = listOf(
        "M5 12h14"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosMenu = IconoKit(
    trazos = listOf(
        "M12 5 m-1.2 0 a1.2 1.2 0 1 0 2.4 0 a1.2 1.2 0 1 0 -2.4 0z",
        "M12 19 m-1.2 0 a1.2 1.2 0 1 0 2.4 0 a1.2 1.2 0 1 0 -2.4 0z"
    ),
    trazosAcento = listOf(
        "M12 12 m-1.2 0 a1.2 1.2 0 1 0 2.4 0 a1.2 1.2 0 1 0 -2.4 0z"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosMetronomo = IconoKit(
    trazos = listOf(
        "M8.5 3h7l4 18h-15z",
        "M5.8 15h12.4"
    ),
    trazosAcento = listOf(
        "M12 15L15.5 6"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosMicrofono = IconoKit(
    trazos = listOf(
        "M5.5 11a6.5 6.5 0 0 0 13 0",
        "M12 17.5V21M9 21h6"
    ),
    trazosAcento = listOf(
        "M12 3h0a3 3 0 0 1 3 3v5a3 3 0 0 1 -3 3h0a3 3 0 0 1 -3 -3v-5a3 3 0 0 1 3 -3z"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosMover = IconoKit(
    trazos = listOf(
        "M3 7a2 2 0 0 1 2-2h4l2 2h8a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"
    ),
    trazosAcento = listOf(
        "M8.5 13h6.5M12.5 10.5L15 13l-2.5 2.5"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosNeutro = IconoKit(
    trazos = listOf(
        "M12 12 m-9 0 a9 9 0 1 0 18 0 a9 9 0 1 0 -18 0z"
    ),
    trazosAcento = listOf(
        "M8 12h8"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosNotas = IconoKit(
    trazos = listOf(
        "M7 17 m-2.5 0 a2.5 2.5 0 1 0 5 0 a2.5 2.5 0 1 0 -5 0z",
        "M17 15 m-2.5 0 a2.5 2.5 0 1 0 5 0 a2.5 2.5 0 1 0 -5 0z",
        "M9.5 17V6M19.5 15V4"
    ),
    trazosAcento = listOf(
        "M9.5 6L19.5 4"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosNubeAlDia = IconoKit(
    trazos = listOf(
        "M7 18.5H17A4 4 0 0 0 18 10.6A5.5 5.5 0 0 0 7 11.5A3.5 3.5 0 0 0 7 18.5Z"
    ),
    trazosAcento = listOf(
        "M9.5 14l2 2 3.5-3.5"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosNubeSinConexion = IconoKit(
    trazos = listOf(
        "M7 18.5H17A4 4 0 0 0 18 10.6A5.5 5.5 0 0 0 7 11.5A3.5 3.5 0 0 0 7 18.5Z"
    ),
    trazosAcento = listOf(
        "M10 12.5l4 4M14 12.5l-4 4"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosOido = IconoKit(
    trazos = listOf(
        "M5.5 9.5a5 5 0 0 1 10 0c0 3-2.5 4-3.5 6s-1 4-3.5 4a2.5 2.5 0 0 1-2.5-2.5",
        "M8.5 10a2 2 0 0 1 4 0c0 1.5-2 2-2 3.5"
    ),
    trazosAcento = listOf(
        "M18.5 7a5 5 0 0 1 0 7"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosOnda = IconoKit(
    trazos = listOf(
        "M3 12h2M19 12h2"
    ),
    trazosAcento = listOf(
        "M5 12q1.8-8 3.5 0t3.5 0t3.5 0t3.5 0"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosOrdenar = IconoKit(
    trazos = listOf(
        "M4 6h10M4 12h7M4 18h4"
    ),
    trazosAcento = listOf(
        "M18 4v15M15 16l3 3 3-3"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosPartitura = IconoKit(
    trazos = listOf(
        "M7 3h10a2 2 0 0 1 2 2v14a2 2 0 0 1 -2 2h-10a2 2 0 0 1 -2 -2v-14a2 2 0 0 1 2 -2z",
        "M8 11h8M8 14.5h8M8 18h5"
    ),
    trazosAcento = listOf(
        "M8 7.5h2.5M13.5 7.5h2.5"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosPausa = IconoKit(
    trazos = listOf(
        "M12 12 m-9 0 a9 9 0 1 0 18 0 a9 9 0 1 0 -18 0z"
    ),
    trazosAcento = listOf(
        "M10 8.5v7M14 8.5v7"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosPlegar = IconoKit(
    trazos = emptyList(),
    trazosAcento = listOf(
        "M5 15l7-7 7 7"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosPractica = IconoKit(
    trazos = listOf(
        "M7 3h10a2 2 0 0 1 2 2v14a2 2 0 0 1 -2 2h-10a2 2 0 0 1 -2 -2v-14a2 2 0 0 1 2 -2z",
        "M8.5 7.5h7"
    ),
    trazosAcento = listOf(
        "M10 11.5l5 3-5 3z"
    ),
    rellenos = emptyList(),
    rellenosAcento = listOf(
        "M10 11.5l5 3-5 3z"
    )
)

private val DatosRacha = IconoKit(
    trazos = listOf(
        "M12 21a6 6 0 0 0 6-6c0-4.5-3.5-7-4.5-11.5C10.5 6.5 6 10 6 15a6 6 0 0 0 6 6z"
    ),
    trazosAcento = listOf(
        "M12 18.5a2.5 2.5 0 0 1-2.5-2.5c0-1.8 2.5-3.5 2.5-3.5s2.5 1.7 2.5 3.5a2.5 2.5 0 0 1-2.5 2.5z"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosRecargar = IconoKit(
    trazos = listOf(
        "M20 12a8 8 0 1 1-2.3-5.7L20 8.5"
    ),
    trazosAcento = listOf(
        "M20 4v4.5h-4.5"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosReproducir = IconoKit(
    trazos = listOf(
        "M12 12 m-9 0 a9 9 0 1 0 18 0 a9 9 0 1 0 -18 0z"
    ),
    trazosAcento = listOf(
        "M10 8l6.5 4-6.5 4z"
    ),
    rellenos = emptyList(),
    rellenosAcento = listOf(
        "M10 8l6.5 4-6.5 4z"
    )
)

private val DatosRestaurar = IconoKit(
    trazos = listOf(
        "M4 6.5h16M9.5 6.5v-2h5v2",
        "M6 6.5l1 14h10l1-14"
    ),
    trazosAcento = listOf(
        "M12 17.5v-7M9 13l3-3 3 3"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosRevision = IconoKit(
    trazos = listOf(
        "M7 4h10a2 2 0 0 1 2 2v13a2 2 0 0 1 -2 2h-10a2 2 0 0 1 -2 -2v-13a2 2 0 0 1 2 -2z",
        "M10 2.5h4a1 1 0 0 1 1 1v1a1 1 0 0 1 -1 1h-4a1 1 0 0 1 -1 -1v-1a1 1 0 0 1 1 -1z",
        "M8.5 10h7M8.5 16h3"
    ),
    trazosAcento = listOf(
        "M15 16 m-1.8 0 a1.8 1.8 0 1 0 3.6 0 a1.8 1.8 0 1 0 -3.6 0z"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosSaltar = IconoKit(
    trazos = listOf(
        "M12 12 m-9 0 a9 9 0 1 0 18 0 a9 9 0 1 0 -18 0z",
        "M9 8.5l5 3.5-5 3.5z"
    ),
    trazosAcento = listOf(
        "M16 8.5v7"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosSeleccionarTodo = IconoKit(
    trazos = listOf(
        "M4 16V6a2 2 0 0 1 2-2h10",
        "M10 8h9a2 2 0 0 1 2 2v9a2 2 0 0 1 -2 2h-9a2 2 0 0 1 -2 -2v-9a2 2 0 0 1 2 -2z"
    ),
    trazosAcento = listOf(
        "M11 14.5l2.5 2.5 4.5-4.5"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosSiguiente = IconoKit(
    trazos = emptyList(),
    trazosAcento = listOf(
        "M9 5l7 7-7 7"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosSilencio = IconoKit(
    trazos = listOf(
        "M4 9.5h3l5-4v13l-5-4H4z"
    ),
    trazosAcento = listOf(
        "M16 9.5l5 5M21 9.5l-5 5"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosSinResultados = IconoKit(
    trazos = listOf(
        "M10.5 10.5 m-6.5 0 a6.5 6.5 0 1 0 13 0 a6.5 6.5 0 1 0 -13 0z",
        "M15.5 15.5l5 5"
    ),
    trazosAcento = listOf(
        "M8.5 8.5l4 4M12.5 8.5l-4 4"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosSincronizar = IconoKit(
    trazos = listOf(
        "M4 12a8 8 0 0 1 13.7-5.7L20 8.5M20 4v4.5h-4.5"
    ),
    trazosAcento = listOf(
        "M20 12a8 8 0 0 1-13.7 5.7L4 15.5M4 20v-4.5h4.5"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosTema = IconoKit(
    trazos = listOf(
        "M12 12 m-8.5 0 a8.5 8.5 0 1 0 17 0 a8.5 8.5 0 1 0 -17 0z"
    ),
    trazosAcento = listOf(
        "M12 3.5a5 8.5 0 0 1 0 17"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosTeoria = IconoKit(
    trazos = listOf(
        "M5 6h14M5 9h14M5 12h14M5 15h14M5 18h14"
    ),
    trazosAcento = emptyList(),
    rellenos = emptyList(),
    rellenosAcento = listOf(
        "M14 13.5 m-1.9 0 a1.9 1.9 0 1 0 3.8 0 a1.9 1.9 0 1 0 -3.8 0z"
    )
)

private val DatosTiempo = IconoKit(
    trazos = listOf(
        "M12 13 m-8 0 a8 8 0 1 0 16 0 a8 8 0 1 0 -16 0z",
        "M10 2.5h4M12 2.5V5",
        "M18.5 5.5l1.5 1.5"
    ),
    trazosAcento = listOf(
        "M12 13V8.5"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosTrofeo = IconoKit(
    trazos = listOf(
        "M7 5.5H4V7a3 3 0 0 0 3 3M17 5.5h3V7a3 3 0 0 1-3 3",
        "M12 14v6M8 20h8"
    ),
    trazosAcento = listOf(
        "M7 4h10v5a5 5 0 0 1-10 0z"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosVolumen = IconoKit(
    trazos = listOf(
        "M4 9.5h3l5-4v13l-5-4H4z"
    ),
    trazosAcento = listOf(
        "M15.5 9a4.2 4.2 0 0 1 0 6M18 6.5a7.8 7.8 0 0 1 0 11"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

private val DatosVolver = IconoKit(
    trazos = listOf(
        "M19 12H5"
    ),
    trazosAcento = listOf(
        "M11 6l-6 6 6 6"
    ),
    rellenos = emptyList(),
    rellenosAcento = emptyList()
)

/*
 * La marca: el mástil sobre su propio teal. Lleva los colores de la marca
 * dentro y no los del tema, porque es un logotipo y no un icono de interfaz:
 * el mismo rótulo en claro y en oscuro. Píntala con `tint = Color.Unspecified`.
 */
private const val MARCA_W = 132f
private const val MARCA_H = 92f
private val MARCA_TEAL = Color(0xFF1A535C)
private val MARCA_CLARO = Color(0xFFF2FAF6)
private val MARCA_AMARILLO = Color(0xFFFFE66D)
private val MARCA_TURQUESA = Color(0xFF4ECDC4)

object AccordioIcons {
    @Composable
    private fun icono(datos: IconoKit, nombre: String, color: Color?, acento: Color?): ImageVector {
        val trazo = color ?: LocalContentColor.current
        /*
         * El acento sale del ROL `chord`, no de un coral fijo. Con el literal
         * #FF6B6B el detalle daba 2,61:1 sobre la tarjeta clara —por debajo del
         * 3:1 que piden los gráficos que significan algo— y además no se
         * aclaraba en oscuro. El rol da la rampa que se lee en cada modo:
         * coral-700 en claro (5,3:1) y el coral aclarado del kit en oscuro.
         */
        val detalle = acento ?: MaterialTheme.extendedColors.chord
        return datos.construir(trazo, detalle, nombre)
    }

    @Composable fun acorde(color: Color? = null, acento: Color? = null) = icono(DatosAcorde, "Acorde", color, acento)
    @Composable fun actualizar(color: Color? = null, acento: Color? = null) = icono(DatosActualizar, "Actualizar", color, acento)
    @Composable fun ajustes(color: Color? = null, acento: Color? = null) = icono(DatosAjustes, "Ajustes", color, acento)
    @Composable fun anterior(color: Color? = null, acento: Color? = null) = icono(DatosAnterior, "Anterior", color, acento)
    @Composable fun aprender(color: Color? = null, acento: Color? = null) = icono(DatosAprender, "Aprender", color, acento)
    @Composable fun arrastrar(color: Color? = null, acento: Color? = null) = icono(DatosArrastrar, "Arrastrar", color, acento)
    @Composable fun borrar(color: Color? = null, acento: Color? = null) = icono(DatosBorrar, "Borrar", color, acento)
    @Composable fun borrarDelTodo(color: Color? = null, acento: Color? = null) = icono(DatosBorrarDelTodo, "BorrarDelTodo", color, acento)
    @Composable fun buscar(color: Color? = null, acento: Color? = null) = icono(DatosBuscar, "Buscar", color, acento)
    @Composable fun cambioAcordes(color: Color? = null, acento: Color? = null) = icono(DatosCambioAcordes, "CambioAcordes", color, acento)
    @Composable fun candado(color: Color? = null, acento: Color? = null) = icono(DatosCandado, "Candado", color, acento)
    @Composable fun cerrar(color: Color? = null, acento: Color? = null) = icono(DatosCerrar, "Cerrar", color, acento)
    @Composable fun compartir(color: Color? = null, acento: Color? = null) = icono(DatosCompartir, "Compartir", color, acento)
    @Composable fun desplegar(color: Color? = null, acento: Color? = null) = icono(DatosDesplegar, "Desplegar", color, acento)
    @Composable fun disco(color: Color? = null, acento: Color? = null) = icono(DatosDisco, "Disco", color, acento)
    @Composable fun editar(color: Color? = null, acento: Color? = null) = icono(DatosEditar, "Editar", color, acento)
    @Composable fun enlace(color: Color? = null, acento: Color? = null) = icono(DatosEnlace, "Enlace", color, acento)
    @Composable fun estadisticas(color: Color? = null, acento: Color? = null) = icono(DatosEstadisticas, "Estadisticas", color, acento)
    @Composable fun estrella(color: Color? = null, acento: Color? = null) = icono(DatosEstrella, "Estrella", color, acento)
    @Composable fun estrellaHueca(color: Color? = null, acento: Color? = null) = icono(DatosEstrellaHueca, "EstrellaHueca", color, acento)
    @Composable fun fallado(color: Color? = null, acento: Color? = null) = icono(DatosFallado, "Fallado", color, acento)
    @Composable fun guitarra(color: Color? = null, acento: Color? = null) = icono(DatosGuitarra, "Guitarra", color, acento)
    @Composable fun hecho(color: Color? = null, acento: Color? = null) = icono(DatosHecho, "Hecho", color, acento)
    @Composable fun importar(color: Color? = null, acento: Color? = null) = icono(DatosImportar, "Importar", color, acento)
    @Composable fun imprimir(color: Color? = null, acento: Color? = null) = icono(DatosImprimir, "Imprimir", color, acento)
    @Composable fun limpiar(color: Color? = null, acento: Color? = null) = icono(DatosLimpiar, "Limpiar", color, acento)
    @Composable fun logrado(color: Color? = null, acento: Color? = null) = icono(DatosLogrado, "Logrado", color, acento)
    @Composable fun mas(color: Color? = null, acento: Color? = null) = icono(DatosMas, "Mas", color, acento)
    @Composable fun menos(color: Color? = null, acento: Color? = null) = icono(DatosMenos, "Menos", color, acento)
    @Composable fun menu(color: Color? = null, acento: Color? = null) = icono(DatosMenu, "Menu", color, acento)
    @Composable fun metronomo(color: Color? = null, acento: Color? = null) = icono(DatosMetronomo, "Metronomo", color, acento)
    @Composable fun microfono(color: Color? = null, acento: Color? = null) = icono(DatosMicrofono, "Microfono", color, acento)
    @Composable fun mover(color: Color? = null, acento: Color? = null) = icono(DatosMover, "Mover", color, acento)
    @Composable fun neutro(color: Color? = null, acento: Color? = null) = icono(DatosNeutro, "Neutro", color, acento)
    @Composable fun notas(color: Color? = null, acento: Color? = null) = icono(DatosNotas, "Notas", color, acento)
    @Composable fun nubeAlDia(color: Color? = null, acento: Color? = null) = icono(DatosNubeAlDia, "NubeAlDia", color, acento)
    @Composable fun nubeSinConexion(color: Color? = null, acento: Color? = null) = icono(DatosNubeSinConexion, "NubeSinConexion", color, acento)
    @Composable fun oido(color: Color? = null, acento: Color? = null) = icono(DatosOido, "Oido", color, acento)
    @Composable fun onda(color: Color? = null, acento: Color? = null) = icono(DatosOnda, "Onda", color, acento)
    @Composable fun ordenar(color: Color? = null, acento: Color? = null) = icono(DatosOrdenar, "Ordenar", color, acento)
    @Composable fun partitura(color: Color? = null, acento: Color? = null) = icono(DatosPartitura, "Partitura", color, acento)
    @Composable fun pausa(color: Color? = null, acento: Color? = null) = icono(DatosPausa, "Pausa", color, acento)
    @Composable fun plegar(color: Color? = null, acento: Color? = null) = icono(DatosPlegar, "Plegar", color, acento)
    @Composable fun practica(color: Color? = null, acento: Color? = null) = icono(DatosPractica, "Practica", color, acento)
    @Composable fun racha(color: Color? = null, acento: Color? = null) = icono(DatosRacha, "Racha", color, acento)
    @Composable fun recargar(color: Color? = null, acento: Color? = null) = icono(DatosRecargar, "Recargar", color, acento)
    @Composable fun reproducir(color: Color? = null, acento: Color? = null) = icono(DatosReproducir, "Reproducir", color, acento)
    @Composable fun restaurar(color: Color? = null, acento: Color? = null) = icono(DatosRestaurar, "Restaurar", color, acento)
    @Composable fun revision(color: Color? = null, acento: Color? = null) = icono(DatosRevision, "Revision", color, acento)
    @Composable fun saltar(color: Color? = null, acento: Color? = null) = icono(DatosSaltar, "Saltar", color, acento)
    @Composable fun seleccionarTodo(color: Color? = null, acento: Color? = null) = icono(DatosSeleccionarTodo, "SeleccionarTodo", color, acento)
    @Composable fun siguiente(color: Color? = null, acento: Color? = null) = icono(DatosSiguiente, "Siguiente", color, acento)
    @Composable fun silencio(color: Color? = null, acento: Color? = null) = icono(DatosSilencio, "Silencio", color, acento)
    @Composable fun sinResultados(color: Color? = null, acento: Color? = null) = icono(DatosSinResultados, "SinResultados", color, acento)
    @Composable fun sincronizar(color: Color? = null, acento: Color? = null) = icono(DatosSincronizar, "Sincronizar", color, acento)
    @Composable fun tema(color: Color? = null, acento: Color? = null) = icono(DatosTema, "Tema", color, acento)
    @Composable fun teoria(color: Color? = null, acento: Color? = null) = icono(DatosTeoria, "Teoria", color, acento)
    @Composable fun tiempo(color: Color? = null, acento: Color? = null) = icono(DatosTiempo, "Tiempo", color, acento)
    @Composable fun trofeo(color: Color? = null, acento: Color? = null) = icono(DatosTrofeo, "Trofeo", color, acento)
    @Composable fun volumen(color: Color? = null, acento: Color? = null) = icono(DatosVolumen, "Volumen", color, acento)
    @Composable fun volver(color: Color? = null, acento: Color? = null) = icono(DatosVolver, "Volver", color, acento)

    /** La marca de Accordio, con sus colores propios. */
    fun marca(): ImageVector {
        val b = ImageVector.Builder(
            name = "Accordio",
            defaultWidth = 33.dp, defaultHeight = 23.dp,
            viewportWidth = MARCA_W, viewportHeight = MARCA_H
        )
        b.addPath(pathData = PathParser().parsePathString("M14 2h104a12 12 0 0 1 12 12v64a12 12 0 0 1 -12 12h-104a12 12 0 0 1 -12 -12v-64a12 12 0 0 1 12 -12z").toNodes(), fill = SolidColor(MARCA_TEAL))
        b.addPath(pathData = PathParser().parsePathString("M16 18v56").toNodes(), stroke = SolidColor(MARCA_CLARO), strokeLineWidth = 5f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
        b.addPath(pathData = PathParser().parsePathString("M42 18v56M68 18v56M94 18v56M120 18v56").toNodes(), stroke = SolidColor(MARCA_CLARO), strokeLineWidth = 2.5f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
        b.addPath(pathData = PathParser().parsePathString("M12 18h110M12 32h110M12 46h110M12 60h110M12 74h110").toNodes(), stroke = SolidColor(MARCA_CLARO), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
        b.addPath(pathData = PathParser().parsePathString("M29 60 m-6 0 a6 6 0 1 0 12 0 a6 6 0 1 0 -12 0z").toNodes(), fill = SolidColor(MARCA_CLARO))
        b.addPath(pathData = PathParser().parsePathString("M55 46 m-6 0 a6 6 0 1 0 12 0 a6 6 0 1 0 -12 0z").toNodes(), fill = SolidColor(MARCA_AMARILLO))
        b.addPath(pathData = PathParser().parsePathString("M81 32 m-6 0 a6 6 0 1 0 12 0 a6 6 0 1 0 -12 0z").toNodes(), fill = SolidColor(MARCA_TURQUESA))
        return b.build()
    }
}
