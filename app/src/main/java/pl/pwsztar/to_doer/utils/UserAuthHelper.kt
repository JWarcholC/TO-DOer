package pl.pwsztar.to_doer.utils

import com.google.firebase.auth.FirebaseAuth

fun verifyUser() = FirebaseAuth.getInstance().uid
