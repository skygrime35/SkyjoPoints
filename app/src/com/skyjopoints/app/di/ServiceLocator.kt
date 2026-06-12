package com.skyjopoints.app.di

import android.content.Context
import com.skyjopoints.app.adapter.store.SharedPrefsGameRepository
import com.skyjopoints.app.domain.port.GameRepository
import com.skyjopoints.app.domain.usecase.*

object ServiceLocator {
    private var repository: GameRepository? = null

    fun init(context: Context) {
        if (repository == null) {
            repository = SharedPrefsGameRepository(context.applicationContext)
        }
    }

    fun getRepository(): GameRepository {
        return repository ?: throw IllegalStateException("ServiceLocator not initialized. Call init(context) first.")
    }

    fun provideStartGameUseCase(): StartGameUseCase = StartGameUseCase(getRepository())
    fun provideAddRoundUseCase(): AddRoundUseCase = AddRoundUseCase(getRepository())
    fun provideDeleteRoundUseCase(): DeleteRoundUseCase = DeleteRoundUseCase(getRepository())
    fun provideArchiveGameUseCase(): ArchiveGameUseCase = ArchiveGameUseCase(getRepository())
    fun provideCalculateGridScoreUseCase(): CalculateGridScoreUseCase = CalculateGridScoreUseCase()
    fun provideEditRoundUseCase(): EditRoundUseCase = EditRoundUseCase(getRepository())
}
