package com.emanuel.mivivero.utils

import android.content.Context
import org.json.JSONObject

fun cargarCatalogo(
    context: Context,
    fileName: String
): Map<String, Map<String, List<String>>> {

    val json = context.assets
        .open(fileName)
        .bufferedReader()
        .use { it.readText() }

    val root = JSONObject(json)
    val resultado = mutableMapOf<String, Map<String, List<String>>>()

    root.keys().forEach { familia ->
        val generosJson = root.getJSONObject(familia)
        val generos = mutableMapOf<String, List<String>>()

        generosJson.keys().forEach { genero ->
            val especiesJson = generosJson.getJSONArray(genero)
            val especies = mutableListOf<String>()

            for (i in 0 until especiesJson.length()) {
                especies.add(especiesJson.getString(i))
            }

            generos[genero] = especies
        }

        resultado[familia] = generos
    }

    return resultado
}
