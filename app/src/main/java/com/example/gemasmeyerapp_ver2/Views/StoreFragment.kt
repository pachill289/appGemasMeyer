package com.example.gemasmeyerapp_ver2.Views

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.core.view.isNotEmpty
import androidx.fragment.app.FragmentTransaction
import com.example.gemasmeyerapp_ver2.Data.ProductoRepository
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gemasmeyerapp_ver2.AdaptadorProductos
import com.example.gemasmeyerapp_ver2.Data.Constantes
import com.example.gemasmeyerapp_ver2.Data.PedidoRepository
import com.example.gemasmeyerapp_ver2.Data.UsuarioRepository
import com.example.gemasmeyerapp_ver2.DescargarImagenes
import com.example.gemasmeyerapp_ver2.Models.Pedido
import com.example.gemasmeyerapp_ver2.Models.Producto
import com.example.gemasmeyerapp_ver2.Models.Usuario
import com.example.gemasmeyerapp_ver2.R
import com.example.gemasmeyerapp_ver2.databinding.FragmentStoreBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "nombreProducto"
private const val ARG_PARAM2 = "param2"
/**
 * A simple [Fragment] subclass.
 * Use the [StoreFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StoreFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var paramNombreProducto: String? = null
    private var param2: String? = null
    private lateinit var repositorioProductos : ProductoRepository
    private var respuestaRegistro : Boolean? = null
    private lateinit var binding: FragmentStoreBinding
    private lateinit var usuario: Usuario
    private lateinit var listaProductos: MutableList<Producto>
    private lateinit var listaUsuarios: MutableList<Usuario>
    private lateinit var producto: Producto
    private var cantidadProducto: Int? = null
    private var fechaActual: String? = null
    val gson = Gson()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            paramNombreProducto = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }
    //calcular fecha actual
    @SuppressLint("SimpleDateFormat")
    fun obtenerFechaActualEnFormatoDeseado(): String {
        val calendario = Calendar.getInstance()
        val formatoDeseado = SimpleDateFormat("yyyy-MM-dd")
        return formatoDeseado.format(calendario.time)
    }
    //En caso de querer refrescar un fragmento
    fun refrescarFragmento() {
       context.let {
           val fragmentManager = (context as AppCompatActivity).supportFragmentManager
           fragmentManager.let {
               val fragmentoActual = this
               fragmentoActual.let {
                   val fragmentTransaction = fragmentManager.beginTransaction()
                   fragmentTransaction.detach(it)
                   fragmentTransaction.attach(it)
                   fragmentTransaction.commit()
               }
           }
       }
    }
    //@RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentStoreBinding.inflate(inflater, container, false)
        poblarLista()
        obtenerUsuarioApi()
        binding.txtTotal.text = "Total: ${0}"

        binding.btnPedir.setOnClickListener {
            if(binding.listProductos.isNotEmpty()) {
                val productoPedido = binding.listProductos.getItemAtPosition(0)
                //Uso de expresión regular para hallar el producto de acuerdo a esta cadena:
                /*
                 """
                ---------------------------------
                Producto: ${listaProductos[position].nombre}
                Precio: ${listaProductos[position].precio}
                Cantidad: $nuevaCantidad

                 """
                 */
                val regex = Regex("Producto: (.+)\nPrecio: (.+)\nCantidad: (.+)")
                val matchResult = regex.find(productoPedido.toString())
                val nombreProductoLista = matchResult?.groupValues?.get(1)
                cantidadProducto = matchResult?.groupValues?.get(3)?.toInt()
                fechaActual = obtenerFechaActualEnFormatoDeseado()
                producto = listaProductos.firstOrNull { it.nombre.equals(nombreProductoLista) }!!
            }
            else
            {
                Constantes.showAlert(binding.root.context,"Mensaje","Seleccione un producto primero",Constantes.Companion.TIPO_ALERTA.ALERTA_SIMPLE)
            }
                if (cantidadProducto != null && fechaActual != null) {
                    val pedidoNuevo = Pedido(null,usuario.ci,producto.idProducto,3, cantidadProducto!!,
                        fechaActual!!
                    )
                    val pedidoRepository = PedidoRepository()
                    val llamadaPedido = pedidoRepository.registrarPedido(pedidoNuevo)
                    llamadaPedido.enqueue(object : Callback<Boolean> {
                        override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                            if (response.isSuccessful) {
                                // Solicitud exitosa, maneja la respuesta aquí
                                Constantes.showAlert(binding.root.context,"Mensaje","Pedido realizado con éxito",Constantes.Companion.TIPO_ALERTA.ALERTA_SIMPLE)
                                respuestaRegistro = response.body()
                            } else {
                                Constantes.showAlert(binding.root.context,"Error","Se ha producido un error al realizar el pedido",Constantes.Companion.TIPO_ALERTA.ALERTA_SIMPLE)
                            }
                        }
                        override fun onFailure(call: Call<Boolean>, t: Throwable) {
                            Constantes.showAlert(binding.root.context,"Error de servidor","Se ha producido un error: $t",Constantes.Companion.TIPO_ALERTA.ALERTA_SIMPLE)
                        }
                    })
                }
            }
            //val pedidoNuevo = Pedido(usuarioObtenido.ci,)
            //Realizar petición POST para insertar un nuevo pedido
        setUpRecylcerView()

        return binding.root
    }
    private fun obtenerUsuarioApi() {
        val repositorioUsuarios = UsuarioRepository()
        //recuperar usuarios
        lifecycleScope.launch {
            val usuariosJson = repositorioUsuarios.obtenerUsuarios()
            listaUsuarios =
                gson.fromJson(usuariosJson, object : TypeToken<MutableList<Usuario>>() {}.type)
            val prefs =  requireContext().getSharedPreferences(getString(R.string.prefs_file),
                AppCompatActivity.MODE_PRIVATE
            )
            //recuperar el usuario con el email
            usuario = listaUsuarios.firstOrNull { it.correo.equals(prefs.getString("email",null)) }!!
        }
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