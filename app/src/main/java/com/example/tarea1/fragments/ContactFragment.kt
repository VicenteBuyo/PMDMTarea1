package com.example.tarea1.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import androidx.fragment.app.Fragment
import com.example.tarea1.R
import com.example.tarea1.databinding.FragmentContactBinding

class ContactFragment : Fragment() {

    // Binding para acceder a los elementos del XML
    private var _binding: FragmentContactBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ---------------------------------------------------------
        // VIDEO
        // ---------------------------------------------------------

        // Creamos el mediacontroller
        val mediaController = MediaController(requireContext())
        mediaController.setAnchorView(binding.videoView)

        // Asociamos el mediacontroller al VideoView
        binding.videoView.setMediaController(mediaController)

        // Ruta del vídeo desde res/raw
        val rutaVideo = "android.resource://${requireContext().packageName}/${R.raw.video}"
        binding.videoView.setVideoPath(rutaVideo)

        // Arrancamos el vídeo automáticamente
        binding.videoView.start()

        // ---------------------------------------------------------
        // BOTÓN LLAMAR
        // ---------------------------------------------------------
        binding.btnCall.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:+34123456789")
            startActivity(intent)
        }

        // ---------------------------------------------------------
        // BOTÓN WHATSAPP
        // ---------------------------------------------------------
        binding.btnWhatsApp.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://wa.me/34123456789")
            startActivity(intent)
        }

        // ---------------------------------------------------------
        // BOTÓN EMAIL
        // ---------------------------------------------------------
        binding.btnEmail.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:info@lalibreria.com")
            intent.putExtra(Intent.EXTRA_SUBJECT, "Consulta desde la app")
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Paramos el vídeo para evitar fugas de memoria
        binding.videoView.stopPlayback()
        _binding = null
    }
}
