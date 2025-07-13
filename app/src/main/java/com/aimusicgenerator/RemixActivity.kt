package com.aimusicgenerator

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.aimusicgenerator.adapter.RemixStyleAdapter
import com.aimusicgenerator.ai.AdvancedAudioEngine
import com.aimusicgenerator.databinding.ActivityRemixBinding
import com.aimusicgenerator.model.*
import com.aimusicgenerator.viewmodel.RemixViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class RemixActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityRemixBinding
    private lateinit var viewModel: RemixViewModel
    private lateinit var remixStyleAdapter: RemixStyleAdapter
    private lateinit var audioEngine: AdvancedAudioEngine
    
    private var mediaPlayer: MediaPlayer? = null
    private var selectedAudioFile: String? = null
    private var isPlaying = false
    
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleSelectedFile(uri)
            }
        }
    }
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            openFilePicker()
        } else {
            Toast.makeText(this, "Permissions required to access audio files", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRemixBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this)[RemixViewModel::class.java]
        audioEngine = AdvancedAudioEngine(this)
        
        setupUI()
        setupRecyclerView()
        setupObservers()
        setupSeekBars()
    }
    
    private fun setupUI() {
        binding.apply {
            toolbar.setNavigationOnClickListener { finish() }
            
            btnSelectFile.setOnClickListener { checkPermissionsAndOpenPicker() }
            btnPlayOriginal.setOnClickListener { togglePlayback() }
            btnRemix.setOnClickListener { startRemixing() }
            btnExport.setOnClickListener { showExportDialog() }
            
            // Initially hide remix controls
            layoutRemixControls.visibility = View.GONE
            layoutPlaybackControls.visibility = View.GONE
        }
    }
    
    private fun setupRecyclerView() {
        remixStyleAdapter = RemixStyleAdapter { style ->
            viewModel.selectRemixStyle(style)
            updateRemixParameters(style.presetSettings)
        }
        
        binding.recyclerRemixStyles.apply {
            layoutManager = LinearLayoutManager(this@RemixActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = remixStyleAdapter
        }
        
        // Load remix styles
        remixStyleAdapter.submitList(getRemixStyles())
    }
    
    private fun setupObservers() {
        viewModel.isRemixing.observe(this) { isRemixing ->
            binding.apply {
                btnRemix.isEnabled = !isRemixing
                progressRemix.visibility = if (isRemixing) View.VISIBLE else View.GONE
                if (isRemixing) {
                    btnRemix.text = "Remixing..."
                } else {
                    btnRemix.text = "Start Remix"
                }
            }
        }
        
        viewModel.remixProgress.observe(this) { progress ->
            binding.progressRemix.progress = progress
        }
        
        viewModel.remixComplete.observe(this) { outputPath ->
            if (outputPath != null) {
                Toast.makeText(this, "Remix completed!", Toast.LENGTH_SHORT).show()
                binding.btnExport.visibility = View.VISIBLE
                // Auto-play the remixed version
                playAudioFile(outputPath)
            } else {
                Toast.makeText(this, "Remix failed. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun setupSeekBars() {
        binding.apply {
            seekBarBass.setOnSeekBarChangeListener(createSeekBarListener { progress ->
                val boost = 0.5 + (progress / 100.0) * 1.5 // 0.5x to 2.0x
                textBassValue.text = String.format("%.1fx", boost)
                viewModel.updateBassBoost(boost)
            })
            
            seekBarTreble.setOnSeekBarChangeListener(createSeekBarListener { progress ->
                val boost = 0.5 + (progress / 100.0) * 1.5 // 0.5x to 2.0x
                textTrebleValue.text = String.format("%.1fx", boost)
                viewModel.updateTrebleBoost(boost)
            })
            
            seekBarReverb.setOnSeekBarChangeListener(createSeekBarListener { progress ->
                val reverb = progress / 100.0 // 0.0 to 1.0
                textReverbValue.text = String.format("%.0f%%", reverb * 100)
                viewModel.updateReverb(reverb)
            })
            
            seekBarDelay.setOnSeekBarChangeListener(createSeekBarListener { progress ->
                val delay = progress / 100.0 // 0.0 to 1.0
                textDelayValue.text = String.format("%.0f%%", delay * 100)
                viewModel.updateDelay(delay)
            })
            
            seekBarDistortion.setOnSeekBarChangeListener(createSeekBarListener { progress ->
                val distortion = progress / 100.0 // 0.0 to 1.0
                textDistortionValue.text = String.format("%.0f%%", distortion * 100)
                viewModel.updateDistortion(distortion)
            })
        }
    }
    
    private fun createSeekBarListener(onProgressChanged: (Int) -> Unit): SeekBar.OnSeekBarChangeListener {
        return object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) onProgressChanged(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }
    }
    
    private fun checkPermissionsAndOpenPicker() {
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        
        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (missingPermissions.isEmpty()) {
            openFilePicker()
        } else {
            permissionLauncher.launch(permissions)
        }
    }
    
    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "audio/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                "audio/mpeg", // MP3
                "audio/wav",  // WAV
                "audio/midi", // MIDI
                "audio/x-midi" // MIDI alternative
            ))
        }
        filePickerLauncher.launch(Intent.createChooser(intent, "Select Audio File"))
    }
    
    private fun handleSelectedFile(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val fileName = getFileName(uri) ?: "selected_audio"
            val tempFile = File(cacheDir, fileName)
            
            inputStream?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            selectedAudioFile = tempFile.absolutePath
            
            // Update UI
            binding.apply {
                textSelectedFile.text = fileName
                textSelectedFile.visibility = View.VISIBLE
                layoutRemixControls.visibility = View.VISIBLE
                layoutPlaybackControls.visibility = View.VISIBLE
                btnRemix.isEnabled = true
            }
            
            // Analyze the audio file
            analyzeAudioFile(tempFile.absolutePath)
            
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun getFileName(uri: Uri): String? {
        val cursor = contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val nameIndex = it.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
            if (it.moveToFirst() && nameIndex >= 0) {
                it.getString(nameIndex)
            } else null
        }
    }
    
    private fun analyzeAudioFile(filePath: String) {
        lifecycleScope.launch {
            try {
                val file = File(filePath)
                val audioInfo = AudioFileInfo(
                    filePath = filePath,
                    fileName = file.name,
                    format = file.extension.uppercase(),
                    duration = 0, // Would need audio analysis library for accurate duration
                    sampleRate = 44100, // Default, would need analysis
                    channels = 2,
                    bitRate = 320,
                    fileSize = file.length()
                )
                
                binding.apply {
                    textFileInfo.text = buildString {
                        append("Format: ${audioInfo.format}\n")
                        append("Size: ${audioInfo.fileSize / 1024 / 1024} MB\n")
                        append("Sample Rate: ${audioInfo.sampleRate} Hz\n")
                        append("Channels: ${audioInfo.channels}")
                    }
                    textFileInfo.visibility = View.VISIBLE
                }
                
            } catch (e: Exception) {
                Toast.makeText(this@RemixActivity, "Error analyzing file", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun togglePlayback() {
        selectedAudioFile?.let { filePath ->
            if (isPlaying) {
                stopPlayback()
            } else {
                playAudioFile(filePath)
            }
        }
    }
    
    private fun playAudioFile(filePath: String) {
        try {
            stopPlayback() // Stop any current playback
            
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                prepareAsync()
                setOnPreparedListener {
                    start()
                    this@RemixActivity.isPlaying = true
                    binding.btnPlayOriginal.text = "Pause"
                }
                setOnCompletionListener {
                    this@RemixActivity.isPlaying = false
                    binding.btnPlayOriginal.text = "Play"
                }
                setOnErrorListener { _, _, _ ->
                    Toast.makeText(this@RemixActivity, "Error playing audio", Toast.LENGTH_SHORT).show()
                    this@RemixActivity.isPlaying = false
                    binding.btnPlayOriginal.text = "Play"
                    true
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error playing file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun stopPlayback() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
        isPlaying = false
        binding.btnPlayOriginal.text = "Play"
    }
    
    private fun startRemixing() {
        selectedAudioFile?.let { inputPath ->
            val remixRequest = RemixRequest(
                inputFilePath = inputPath,
                remixStyle = viewModel.selectedRemixStyle.value?.id ?: "custom",
                bassBoost = viewModel.bassBoost.value ?: 1.0,
                trebleBoost = viewModel.trebleBoost.value ?: 1.0,
                reverb = viewModel.reverb.value ?: 0.0,
                delay = viewModel.delay.value ?: 0.0,
                distortion = viewModel.distortion.value ?: 0.0,
                outputFormat = AudioFormat.MP3_320
            )
            
            viewModel.startRemixing(remixRequest, audioEngine)
        }
    }
    
    private fun updateRemixParameters(preset: RemixPreset) {
        binding.apply {
            seekBarBass.progress = ((preset.bassBoost - 0.5) / 1.5 * 100).toInt()
            seekBarTreble.progress = ((preset.trebleBoost - 0.5) / 1.5 * 100).toInt()
            seekBarReverb.progress = (preset.reverb * 100).toInt()
            seekBarDelay.progress = (preset.delay * 100).toInt()
            seekBarDistortion.progress = (preset.distortion * 100).toInt()
            
            textBassValue.text = String.format("%.1fx", preset.bassBoost)
            textTrebleValue.text = String.format("%.1fx", preset.trebleBoost)
            textReverbValue.text = String.format("%.0f%%", preset.reverb * 100)
            textDelayValue.text = String.format("%.0f%%", preset.delay * 100)
            textDistortionValue.text = String.format("%.0f%%", preset.distortion * 100)
        }
    }
    
    private fun showExportDialog() {
        // Create and show export options dialog
        val exportFormats = AudioFormat.values()
        val formatNames = exportFormats.map { "${it.displayName} (${it.quality})" }.toTypedArray()
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Export Quality")
            .setItems(formatNames) { _, which ->
                val selectedFormat = exportFormats[which]
                exportRemixedAudio(selectedFormat)
            }
            .show()
    }
    
    private fun exportRemixedAudio(format: AudioFormat) {
        viewModel.remixOutputPath.value?.let { outputPath ->
            lifecycleScope.launch {
                try {
                    val exportPath = "${getExternalFilesDir(null)}/remixed_${System.currentTimeMillis()}.${format.extension}"
                    
                    // Convert to selected format if needed
                    val success = if (format != AudioFormat.MP3_320) {
                        audioEngine.remixAudio(
                            RemixRequest(
                                inputFilePath = outputPath,
                                remixStyle = "custom",
                                outputFormat = format
                            ),
                            exportPath,
                            format
                        )
                    } else {
                        // Already in MP3_320 format
                        File(outputPath).copyTo(File(exportPath), overwrite = true)
                        true
                    }
                    
                    if (success) {
                        Toast.makeText(this@RemixActivity, "Exported to: $exportPath", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@RemixActivity, "Export failed", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@RemixActivity, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun getRemixStyles(): List<RemixStyle> {
        return listOf(
            RemixStyle(
                id = "deep_house",
                name = "Deep House",
                description = "Smooth, deep basslines with filtered highs",
                icon = "🏠",
                presetSettings = RemixPreset(
                    bassBoost = 1.5,
                    trebleBoost = 0.7,
                    reverb = 0.3,
                    delay = 0.2,
                    distortion = 0.1
                )
            ),
            RemixStyle(
                id = "trap",
                name = "Trap",
                description = "Heavy bass with crisp highs and punchy drums",
                icon = "🔥",
                presetSettings = RemixPreset(
                    bassBoost = 2.0,
                    trebleBoost = 1.3,
                    reverb = 0.1,
                    delay = 0.15,
                    distortion = 0.2
                )
            ),
            RemixStyle(
                id = "dubstep",
                name = "Dubstep",
                description = "Wobble bass with dramatic drops",
                icon = "⚡",
                presetSettings = RemixPreset(
                    bassBoost = 1.8,
                    trebleBoost = 0.5,
                    reverb = 0.4,
                    delay = 0.3,
                    distortion = 0.4
                )
            ),
            RemixStyle(
                id = "ambient",
                name = "Ambient",
                description = "Ethereal, atmospheric soundscape",
                icon = "🌙",
                presetSettings = RemixPreset(
                    bassBoost = 0.8,
                    trebleBoost = 1.2,
                    reverb = 0.6,
                    delay = 0.4,
                    distortion = 0.05
                )
            ),
            RemixStyle(
                id = "orchestral",
                name = "Orchestral",
                description = "Full, rich orchestral enhancement",
                icon = "🎼",
                presetSettings = RemixPreset(
                    bassBoost = 1.1,
                    trebleBoost = 1.2,
                    reverb = 0.4,
                    delay = 0.1,
                    distortion = 0.0
                )
            )
        )
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopPlayback()
    }
}
