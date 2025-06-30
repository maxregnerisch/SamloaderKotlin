package com.labyrinth3d.game

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.net.Uri
import android.os.Build
import kotlin.random.Random

class SoundManager(private val context: Context) {
    
    private var soundPool: SoundPool? = null
    private var backgroundMusic: MediaPlayer? = null
    private var audioManager: AudioManager? = null
    
    // Sound effect IDs
    private val soundEffects = mutableMapOf<String, Int>()
    private val soundInstances = mutableMapOf<String, Int>()
    
    // Volume settings
    private var masterVolume = 1.0f
    private var musicVolume = 0.7f
    private var sfxVolume = 0.8f
    private var isEnabled = true
    
    // Background music tracks
    private val musicTracks = mutableListOf<String>()
    private var currentTrackIndex = 0
    private var isPlayingMusic = false
    
    init {
        initializeSoundSystem()
        generateSoundEffects()
    }
    
    private fun initializeSoundSystem() {
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        
        // Initialize SoundPool
        soundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            
            SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(audioAttributes)
                .build()
        } else {
            @Suppress("DEPRECATION")
            SoundPool(10, AudioManager.STREAM_MUSIC, 0)
        }
    }
    
    private fun generateSoundEffects() {
        // Since we can't load actual sound files, we'll create procedural sound effects
        // In a real implementation, you would load actual sound files here
        
        // Generate different types of sound effects
        generateToneSound("coin_collect", 800f, 0.2f)
        generateToneSound("jump", 400f, 0.3f)
        generateToneSound("damage", 200f, 0.5f)
        generateToneSound("heal", 600f, 0.4f)
        generateToneSound("level_up", 1000f, 1.0f)
        generateToneSound("explosion", 150f, 0.8f)
        generateToneSound("footstep", 300f, 0.1f)
        generateToneSound("button_click", 500f, 0.1f)
        generateToneSound("enemy_hit", 250f, 0.3f)
        generateToneSound("magic", 900f, 0.6f)
        generateToneSound("powerup", 700f, 0.5f)
        generateToneSound("teleport", 1200f, 0.7f)
        generateToneSound("shield", 450f, 0.4f)
        generateToneSound("sword_swing", 350f, 0.2f)
        generateToneSound("door_open", 400f, 0.6f)
    }
    
    private fun generateToneSound(name: String, frequency: Float, duration: Float) {
        // This is a placeholder - in a real implementation you would load actual sound files
        // For now, we'll just store the parameters and simulate the sound loading
        soundEffects[name] = Random.nextInt(1000) // Simulate sound ID
    }
    
    // Play sound effects
    fun playSound(soundName: String, volume: Float = 1.0f, pitch: Float = 1.0f, loop: Boolean = false) {
        if (!isEnabled) return
        
        soundEffects[soundName]?.let { soundId ->
            val finalVolume = volume * sfxVolume * masterVolume
            val loopMode = if (loop) -1 else 0
            
            soundPool?.let { pool ->
                val streamId = pool.play(soundId, finalVolume, finalVolume, 1, loopMode, pitch)
                if (loop) {
                    soundInstances[soundName] = streamId
                }
            }
        }
    }
    
    fun stopSound(soundName: String) {
        soundInstances[soundName]?.let { streamId ->
            soundPool?.stop(streamId)
            soundInstances.remove(soundName)
        }
    }
    
    fun pauseSound(soundName: String) {
        soundInstances[soundName]?.let { streamId ->
            soundPool?.pause(streamId)
        }
    }
    
    fun resumeSound(soundName: String) {
        soundInstances[soundName]?.let { streamId ->
            soundPool?.resume(streamId)
        }
    }
    
    // Background music methods
    fun playBackgroundMusic(trackName: String = "main_theme", loop: Boolean = true) {
        if (!isEnabled) return
        
        try {
            stopBackgroundMusic()
            
            // In a real implementation, you would load actual music files
            // For now, we'll simulate background music
            backgroundMusic = MediaPlayer().apply {
                // setDataSource(context, Uri.parse("android.resource://${context.packageName}/raw/$trackName"))
                isLooping = loop
                setVolume(musicVolume * masterVolume, musicVolume * masterVolume)
                
                setOnPreparedListener { mediaPlayer ->
                    mediaPlayer.start()
                    isPlayingMusic = true
                }
                
                setOnCompletionListener {
                    if (!loop) {
                        playNextTrack()
                    }
                }
                
                setOnErrorListener { _, _, _ ->
                    isPlayingMusic = false
                    false
                }
                
                // prepareAsync() // Would be called in real implementation
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun stopBackgroundMusic() {
        backgroundMusic?.let { player ->
            if (player.isPlaying) {
                player.stop()
            }
            player.release()
            backgroundMusic = null
            isPlayingMusic = false
        }
    }
    
    fun pauseBackgroundMusic() {
        backgroundMusic?.let { player ->
            if (player.isPlaying) {
                player.pause()
                isPlayingMusic = false
            }
        }
    }
    
    fun resumeBackgroundMusic() {
        backgroundMusic?.let { player ->
            player.start()
            isPlayingMusic = true
        }
    }
    
    private fun playNextTrack() {
        if (musicTracks.isNotEmpty()) {
            currentTrackIndex = (currentTrackIndex + 1) % musicTracks.size
            playBackgroundMusic(musicTracks[currentTrackIndex])
        }
    }
    
    // Volume control
    fun setMasterVolume(volume: Float) {
        masterVolume = volume.coerceIn(0f, 1f)
        updateMusicVolume()
    }
    
    fun setMusicVolume(volume: Float) {
        musicVolume = volume.coerceIn(0f, 1f)
        updateMusicVolume()
    }
    
    fun setSfxVolume(volume: Float) {
        sfxVolume = volume.coerceIn(0f, 1f)
    }
    
    private fun updateMusicVolume() {
        val finalVolume = musicVolume * masterVolume
        backgroundMusic?.setVolume(finalVolume, finalVolume)
    }
    
    // Enable/disable sound
    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        if (!enabled) {
            stopAllSounds()
            pauseBackgroundMusic()
        }
    }
    
    fun isEnabled() = isEnabled
    
    // Utility methods
    fun stopAllSounds() {
        soundPool?.autoPause()
        soundInstances.clear()
    }
    
    fun resumeAllSounds() {
        soundPool?.autoResume()
    }
    
    // Game-specific sound methods
    fun playFootstep() {
        // Vary the pitch slightly for more natural footsteps
        val pitch = Random.nextFloat() * 0.4f + 0.8f
        playSound("footstep", 0.3f, pitch)
    }
    
    fun playRandomFootstep() {
        if (Random.nextFloat() < 0.3f) { // Only play occasionally
            playFootstep()
        }
    }
    
    fun playCoinCollect() {
        playSound("coin_collect", 0.8f, Random.nextFloat() * 0.3f + 0.9f)
    }
    
    fun playJump() {
        playSound("jump", 0.6f)
    }
    
    fun playDamage() {
        playSound("damage", 0.7f, Random.nextFloat() * 0.2f + 0.9f)
    }
    
    fun playHeal() {
        playSound("heal", 0.6f)
    }
    
    fun playLevelUp() {
        playSound("level_up", 1.0f)
    }
    
    fun playExplosion() {
        playSound("explosion", 0.9f, Random.nextFloat() * 0.3f + 0.8f)
    }
    
    fun playButtonClick() {
        playSound("button_click", 0.5f)
    }
    
    fun playEnemyHit() {
        playSound("enemy_hit", 0.7f, Random.nextFloat() * 0.4f + 0.8f)
    }
    
    fun playMagic() {
        playSound("magic", 0.8f, Random.nextFloat() * 0.5f + 0.8f)
    }
    
    fun playPowerup() {
        playSound("powerup", 0.7f)
    }
    
    fun playTeleport() {
        playSound("teleport", 0.8f)
    }
    
    fun playShield() {
        playSound("shield", 0.6f)
    }
    
    fun playSwordSwing() {
        playSound("sword_swing", 0.5f, Random.nextFloat() * 0.3f + 0.9f)
    }
    
    fun playDoorOpen() {
        playSound("door_open", 0.7f)
    }
    
    // Ambient sound methods
    fun playAmbientLoop(soundName: String) {
        playSound(soundName, 0.3f, 1.0f, true)
    }
    
    fun stopAmbientLoop(soundName: String) {
        stopSound(soundName)
    }
    
    // 3D positional audio simulation
    fun playPositionalSound(soundName: String, playerX: Float, playerZ: Float, 
                           soundX: Float, soundZ: Float, maxDistance: Float = 10f) {
        val distance = kotlin.math.sqrt((playerX - soundX) * (playerX - soundX) + 
                                       (playerZ - soundZ) * (playerZ - soundZ))
        
        if (distance <= maxDistance) {
            val volume = (1f - distance / maxDistance).coerceIn(0f, 1f)
            playSound(soundName, volume)
        }
    }
    
    // Music playlist management
    fun addMusicTrack(trackName: String) {
        musicTracks.add(trackName)
    }
    
    fun removeMusicTrack(trackName: String) {
        musicTracks.remove(trackName)
    }
    
    fun clearMusicPlaylist() {
        musicTracks.clear()
    }
    
    fun shufflePlaylist() {
        musicTracks.shuffle()
        currentTrackIndex = 0
    }
    
    // Audio focus management
    fun onAudioFocusLost() {
        pauseBackgroundMusic()
        stopAllSounds()
    }
    
    fun onAudioFocusGained() {
        if (isEnabled) {
            resumeBackgroundMusic()
        }
    }
    
    // Cleanup
    fun release() {
        stopAllSounds()
        stopBackgroundMusic()
        soundPool?.release()
        soundPool = null
        soundEffects.clear()
        soundInstances.clear()
    }
    
    // Debug methods
    fun getLoadedSounds(): List<String> = soundEffects.keys.toList()
    fun getActiveSounds(): List<String> = soundInstances.keys.toList()
    fun isPlayingMusic() = isPlayingMusic
    fun getCurrentVolumes() = Triple(masterVolume, musicVolume, sfxVolume)
}

