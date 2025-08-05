package com.litiaina.android.sdk.interfaces

import com.litiaina.android.sdk.data.TokenData

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

sealed class PushNotificationResult {
    data class Success(val message: String) : PushNotificationResult()
    data class Failure(val message: String) : PushNotificationResult()
}

sealed class RegisterDeviceTokenResult {
    data class Success(val success: String) : RegisterDeviceTokenResult()
    data class Failure(val failure: String) : RegisterDeviceTokenResult()
}

sealed class RemoveDeviceTokenResult {
    data class Success(val success: String) : RemoveDeviceTokenResult()
    data class Failure(val failure: String) : RemoveDeviceTokenResult()
}

sealed class RetrieveUIDByEmailResult {
    data class Success(val success: String) : RetrieveUIDByEmailResult()
    data class Failure(val failure: String) : RetrieveUIDByEmailResult()
}

sealed class NotificationResult {
    data class Success(val data: List<TokenData>) : NotificationResult()
    data class Failure(val message: String) : NotificationResult()
}
