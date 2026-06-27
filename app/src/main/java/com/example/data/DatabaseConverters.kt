package com.example.data

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class DatabaseConverters {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
        
    private val workoutExerciseListType = Types.newParameterizedType(List::class.java, WorkoutExercise::class.java)
    private val adapter = moshi.adapter<List<WorkoutExercise>>(workoutExerciseListType)

    @TypeConverter
    fun fromWorkoutExerciseList(value: List<WorkoutExercise>?): String {
        return adapter.toJson(value ?: emptyList())
    }

    @TypeConverter
    fun toWorkoutExerciseList(value: String?): List<WorkoutExercise> {
        if (value.isNullOrEmpty()) return emptyList()
        return try {
            adapter.fromJson(value) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
