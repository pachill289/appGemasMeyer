package com.example.gemasmeyerapp_ver2.Data

import com.google.gson.JsonArray
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ProductoRepository : solicitudesProductos {

    private val productService: solicitudesProductos

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(Constantes.URL_API) // Reemplaza con la URL base de tu API
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        productService = retrofit.create(solicitudesProductos::class.java)
    }

    override suspend fun obtenerProductos(): JsonArray {
        return productService.obtenerProductos()
    }
    override suspend fun obtenerProductosEnStock(): JsonArray {
        return productService.obtenerProductosEnStock()
    }
}
