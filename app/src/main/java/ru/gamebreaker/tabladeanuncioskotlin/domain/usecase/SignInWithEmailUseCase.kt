package ru.gamebreaker.tabladeanuncioskotlin.domain.usecase

import ru.gamebreaker.tabladeanuncioskotlin.domain.repo.AccountRepo

class SignInWithEmailUseCase(
    private val repo: AccountRepo
) {
    operator fun invoke(email: String, password: String) = repo.signInWithEmail(email, password)
}