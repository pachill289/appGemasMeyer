package com.example.gemasmeyerapp_ver2.Views

import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.gemasmeyerapp_ver2.Data.ProductoRepository
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gemasmeyerapp_ver2.AdaptadorProductos
import com.example.gemasmeyerapp_ver2.Data.Constantes
import com.example.gemasmeyerapp_ver2.DescargarImagenes
import com.example.gemasmeyerapp_ver2.Models.Producto
import com.example.gemasmeyerapp_ver2.databinding.FragmentStoreBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [StoreFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StoreFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var repositorioProductos : ProductoRepository
    private lateinit var binding: FragmentStoreBinding
    private lateinit var listaProductos: MutableList<Producto>
    val gson = Gson()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    //@RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentStoreBinding.inflate(inflater, container, false)
        binding.txtTotal.text = "Total: ${0}"
        setUpRecylcerView()
        poblarLista()
        return binding.root
    }

    private fun poblarLista() {
        repositorioProductos = ProductoRepository()
        lifecycleScope.launch {
            val productosJson = repositorioProductos.obtenerProductosEnStock()
            listaProductos = gson.fromJson(productosJson, object : TypeToken<MutableList<Producto>>() {}.type)
            if(listaProductos.size != 0)
            {
                //verificar si se ha añadido una nueva imagen
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                val productos = sharedPreferences.getInt(Constantes.CANTIDAD_PRODUCTOS_EN_STOCK_KEY, listaProductos.size)
                //descargar imagenes
                val descargadorImagenes = DescargarImagenes()
                // Verificar si la cantidad de elementos en listaProductos ha aumentado
                if (listaProductos.size > productos) {
                    // La cantidad de elementos en listaProductos ha aumentado
                    descargadorImagenes.downloadImage(binding.root.context,listaProductos.last().imagen,listaProductos.last().nombre)
                    listaProductos.last().imagenRepuesto = descargadorImagenes.obtenerImagen(binding.root.context,listaProductos.last().nombre)
                    // Actualizar el valor almacenado en SharedPreferences con la nueva cantidad
                    val editor = sharedPreferences.edit()
                    editor.putInt(Constantes.CANTIDAD_PRODUCTOS_EN_STOCK_KEY, listaProductos.size)
                    editor.apply()
                }
                else
                {
                    val editor = sharedPreferences.edit()
                    editor.putInt(Constantes.CANTIDAD_PRODUCTOS_EN_STOCK_KEY, listaProductos.size)
                    editor.apply()
                }
                //descargadorImagenes.mostrarArchivosEnMemoria(binding.root.context)
                //Este código descarga la imagen y luego la introduce en la propiedad del objeto llamada imagenRepuesto
                listaProductos.forEach {
                    //Si se ha agregado una nueva imagen utilizar la función downloadImage
                    //descargadorImagenes.downloadImage(binding.root.context,it.imagen,it.nombre)
                    it.imagenRepuesto = descargadorImagenes.obtenerImagen(binding.root.context,it.nombre)
                }
                //descargadorImagenes.mostrarArchivosEnMemoria(binding.root.context)
                //binding.imgPrueba.setImageBitmap(descargadorImagenes.obtenerImagen(binding.root.context,listaProductos[0].nombre))
                val adaptadorDatos = AdaptadorProductos(listaProductos, binding)
                binding.rcView.adapter = adaptadorDatos
            }
            else
            {
                Constantes.showAlert(binding.root.context,"Mensaje","Actualmente no tenemos productos disponibles 🙁",Constantes.Companion.TIPO_ALERTA.ALERTA_POS_NEG)
            }
        }
    }

    private fun setUpRecylcerView() {
        binding.rcView.layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)
        //binding.rcView.layoutManager = GridLayoutManager(context,1)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment StoreFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            StoreFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}