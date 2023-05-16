package ru.gamebreaker.tabladeanuncioskotlin.domain.repo

interface AccountRepo {

    fun signUpWithEmail(email: String, password: String)

    fun signInWithEmail(email: String, password: String)

//    fun signInAnonymously(listener: AccountHelper.Listener)
}