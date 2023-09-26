package com.example.gemasmeyerapp_ver2.Views

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.load
import com.aallam.openai.api.image.ImageCreation
import com.aallam.openai.api.image.ImageSize
import com.aallam.openai.client.OpenAI
import com.example.gemasmeyerapp_ver2.Data.Constantes
import com.example.gemasmeyerapp_ver2.Data.DeepAIRequest
import com.example.gemasmeyerapp_ver2.Data.DeepAIResponse
import com.example.gemasmeyerapp_ver2.Data.DeepIARepository
import com.example.gemasmeyerapp_ver2.databinding.FragmentImageBinding
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/* dependencias sin usar
import com.example.gemasmeyerapp_ver2.databinding.FragmentVrBinding
import com.google.ar.core.Anchor
import com.google.ar.core.ArCoreApk
import com.google.ar.core.HitResult
import com.google.ar.sceneform.ux.ArFragment*/

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ImageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ImageFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    //private val CAMERA_REQUEST = 1888
    //private lateinit var arFragment : ArFragment
    private lateinit var binding: FragmentImageBinding
    private lateinit var deepAI : DeepIARepository
    //uso openAI
    private var openAI : OpenAI? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        //arFragment = ArFragment()
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ThirdFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ImageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentImageBinding.inflate(inflater, container, false)
        Constantes.showAlert(requireContext(),"Mensaje","Usted puede generar únicamente 10 imágenes por mes. \n si le gusta una imagen, guarde la imagen y envíela al recepcionista con el botón realizar pedido para que el recepcionista considere su pedido",Constantes.Companion.TIPO_ALERTA.ALERTA_SIMPLE)
        binding.prBarImagenNuevaJoya.visibility = View.INVISIBLE
        openAI = OpenAI(Constantes.API_KEY_OPENAI)
        binding.btnGenerar.setOnClickListener {
            if(binding.etPrompt.text.isNotEmpty()) {
                generarImagen()
            }
            else
            {
                Constantes.showAlert(requireContext(),"Advertencia ⚠","Por favor ingrese una descripción para su imagen",Constantes.Companion.TIPO_ALERTA.ALERTA_SIMPLE)
            }
        }
        /* Sin usar
        //Permisos para la cámara
        if (context?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA) } != PackageManager.PERMISSION_GRANTED) {
            // Si la aplicación no tiene permisos, se solicitan
            ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST)
        } else {
            // Si la aplicación ya tiene permisos, se puede acceder a la cámara
            //binding.sceneAr.setBackgroundColor(Color.TRANSPARENT)
            //Se carga el modelo
            val frag = binding.uxFragment.getFragment<Fragment>()
            arFragment = ArFragment()
            arFragment = frag as ArFragment

        }*/
        return binding.root
    }
    private fun generarImagen() {
        lifecycleScope.launch {
            try {
                /*
                val imagenes =
                    openAI?.imageURL(ImageCreation(binding.etPrompt.text.toString(),1, ImageSize.is1024x1024))
                binding.prBarImagenNuevaJoya.visibility = View.VISIBLE
                if (imagenes != null) {
                    binding.imgNuevaJoya.load(imagenes.firstOrNull()?.url){
                        listener(onError = { request, throwable ->
                            binding.prBarImagenNuevaJoya.visibility = View.GONE // Ocultar ProgressBar determinado una vez que la imagen se haya cargado
                            Constantes.showAlert(requireContext(),"Error","Error en la generación de la imagen",Constantes.Companion.TIPO_ALERTA.ALERTA_SIMPLE)
                        }, onSuccess = { _, _ ->
                            binding.prBarImagenNuevaJoya.visibility = View.GONE // Ocultar ProgressBar determinado una vez que la imagen se haya cargado
                        })
                    }
                }*/
                generateImage(binding.etPrompt.text.toString())

            } catch (e: Exception) {
                Constantes.showAlert(requireContext(),"Mensaje","${e.cause?.message}",Constantes.Companion.TIPO_ALERTA.ALERTA_SIMPLE)
            }
            //Constantes.showAlert(requireContext(),"Mensaje","Generando imagen",Constantes.Companion.TIPO_ALERTA.ALERTA_SIMPLE)
        }
    }
    private fun generateImage(prompt: String) {
        deepAI = DeepIARepository()
        val promptText = DeepAIRequest(binding.etPrompt.text.toString())
        val call = deepAI.generateImage("", promptText)

        call.enqueue(object : Callback<DeepAIResponse> {
            override fun onResponse(
                call: Call<DeepAIResponse>,
                response: Response<DeepAIResponse>
            ) {
                if (response.isSuccessful) {
                    val deepAIResponse = response.body()
                    Constantes.showAlert(requireContext(),"Mensaje",deepAIResponse.toString(),Constantes.Companion.TIPO_ALERTA.ALERTA_SIMPLE)
                } else {
                    Constantes.showAlert(requireContext(),"Error",response.toString(),Constantes.Companion.TIPO_ALERTA.ALERTA_SIMPLE)
                }
            }

            override fun onFailure(call: Call<DeepAIResponse>, t: Throwable) {
                Constantes.showAlert(requireContext(),"Error",t.message.toString(),Constantes.Companion.TIPO_ALERTA.ALERTA_SIMPLE)
            }
        })
    }
    //@RequiresApi(Build.VERSION_CODES.N)
    /*private fun createModelRenderable(hitResult: HitResult) {
        val anchor: Anchor = hitResult.createAnchor()

    }*/

    //Comprobar si el dispositivo es compatible
    /*
    override fun onResume() {
        super.onResume()
        val availability = ArCoreApk.getInstance().checkAvailability(context)
        if (availability.isTransient) {
            // ARCore is currently being updated, wait until it's ready.
        } else {
            if (availability.isSupported) {
                //Constantes.showAlert(requireContext(),"Dispositivo compatible","Su dispositivo es compatible con realidad aumentada",Constantes.Companion.TIPO_ALERTA.ALERTA_SIMPLE)
                //Session(requireContext())
                // ARCore is supported on this device, proceed with ARCore app logic.
            } else {
                // ARCore is not supported on this device.
                Constantes.showAlert(requireContext(),"Dispositivo no compatible","Su dispositivo no es compatible con realidad aumentada",Constantes.Companion.TIPO_ALERTA.ALERTA_SIMPLE)
            }
        }
    }*/

}