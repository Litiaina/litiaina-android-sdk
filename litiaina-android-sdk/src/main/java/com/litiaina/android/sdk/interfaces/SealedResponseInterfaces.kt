package com.litiaina.android.sdk.interfaces

sealed class TwoFAResult {
    data class Success(val enabled: Boolean) : TwoFAResult()
    data class Failure(val message: String) : TwoFAResult()
}

sealed class LoginResult {
    data class Success(val valid: Boolean) : LoginResult()
    data class Failure(val message: String) : LoginResult()
}

sealed class ModifyResult {
    data class Success(val valid: Boolean) : ModifyResult()
    data class Failure(val message: String) : ModifyResult()
}