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

// Pantalla de contacto: vídeo + accesos rápidos a llamada/whatsapp/email.
class ContactFragment : Fragment() {

    private var _binding: FragmentContactBinding? = null
    private val binding get() = _binding!!

    // Infla vista de contacto.
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Configuro vídeo y botones al crear la vista.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupVideo()
        setupActions()
    }

    // Monta reproducción del vídeo local de res/raw.
    private fun setupVideo() {
        val mediaController = MediaController(requireContext())
        mediaController.setAnchorView(binding.videoView)
        binding.videoView.setMediaController(mediaController)

        val rutaVideo = "android.resource://${requireContext().packageName}/${R.raw.video}"
        binding.videoView.setVideoPath(rutaVideo)
        binding.videoView.start()
    }

    // Asigna acciones a los tres botones de contacto.
    private fun setupActions() {
        binding.btnCall.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:${getString(R.string.contact_phone_number)}")
            startActivity(intent)
        }

        binding.btnWhatsApp.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(getString(R.string.contact_whatsapp_url))
            startActivity(intent)
        }

        binding.btnEmail.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:${getString(R.string.contact_email_address)}")
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.contact_email_subject))
            startActivity(intent)
        }
    }

    // Limpia recursos de vídeo y binding al destruir vista.
    override fun onDestroyView() {
        super.onDestroyView()
        binding.videoView.stopPlayback()
        _binding = null
    }
}
