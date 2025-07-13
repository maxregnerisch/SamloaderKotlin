package com.aimusicgenerator.database

import androidx.room.*
import com.aimusicgenerator.model.GeneratedMusic
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {
    
    @Query("SELECT * FROM generated_music ORDER BY createdAt DESC")
    fun getAllMusic(): Flow<List<GeneratedMusic>>
    
    @Query("SELECT * FROM generated_music WHERE id = :id")
    suspend fun getMusicById(id: String): GeneratedMusic?
    
    @Query("SELECT * FROM generated_music WHERE genre = :genre ORDER BY createdAt DESC")
    suspend fun getMusicByGenre(genre: String): List<GeneratedMusic>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMusic(music: GeneratedMusic)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMusic(musicList: List<GeneratedMusic>)
    
    @Update
    suspend fun updateMusic(music: GeneratedMusic)
    
    @Delete
    suspend fun deleteMusic(music: GeneratedMusic)
    
    @Query("DELETE FROM generated_music WHERE id = :id")
    suspend fun deleteMusicById(id: String)
    
    @Query("DELETE FROM generated_music")
    suspend fun deleteAllMusic()
    
    @Query("SELECT COUNT(*) FROM generated_music")
    suspend fun getMusicCount(): Int
    
    @Query("SELECT * FROM generated_music WHERE title LIKE '%' || :query || '%' OR genre LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    suspend fun searchMusic(query: String): List<GeneratedMusic>
}

