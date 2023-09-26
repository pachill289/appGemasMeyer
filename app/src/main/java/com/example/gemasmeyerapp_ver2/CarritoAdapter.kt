package com.example.gemasmeyerapp_ver2

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.gemasmeyerapp_ver2.Models.ProductoCarrito

class CarritoAdapter(context: Context, productos: MutableList<ProductoCarrito>) :
    ArrayAdapter<ProductoCarrito>(context, R.layout.item_producto_carrito, productos) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.item_producto_carrito, parent, false)

        val producto = getItem(position)

        // Configura los elementos de la vista con los datos del producto
        val nombreTextView = view.findViewById<TextView>(R.id.txtNombreProcCarrito)
        val precioTextView = view.findViewById<TextView>(R.id.txtPrecioProcCarrito)
        val cantidadTextView = view.findViewById<TextView>(R.id.txtCantidadProcCarrito)


        nombreTextView.text = "Nombre: ${producto?.nombre}"
        precioTextView.text = "Precio: ${producto?.precio}"
        cantidadTextView.text = "Cantidad: ${producto?.cantidad}"

        return view
    }
}
