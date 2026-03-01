package com.theseuntaylor.hexo.di

import com.google.firebase.firestore.FirebaseFirestore
import com.theseuntaylor.hexo.data.repository.FirebaseRoomRepository
import com.theseuntaylor.hexo.data.repository.LocalRoomRepository
import com.theseuntaylor.hexo.data.repository.RoomRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Named("firebase")
    fun provideFirebaseRoomRepository(
        firestore: FirebaseFirestore,
    ): RoomRepository {
        return FirebaseRoomRepository(firestore)
    }

    @Provides
    @Named("local")
    fun provideLocalRoomRepository(): RoomRepository {
        return LocalRoomRepository()
    }
}