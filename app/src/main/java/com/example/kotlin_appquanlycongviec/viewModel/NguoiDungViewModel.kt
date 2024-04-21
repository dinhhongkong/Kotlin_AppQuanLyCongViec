package com.example.kotlin_appquanlycongviec.viewModel

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlin_appquanlycongviec.api.ApiInstance
import com.example.kotlin_appquanlycongviec.api.apiService.LoginApiService
import com.example.kotlin_appquanlycongviec.api.apiService.NguoiDungApiService
import com.example.kotlin_appquanlycongviec.model.NguoiDung
import com.example.kotlin_appquanlycongviec.request.OTP
import com.example.kotlin_appquanlycongviec.request.SignInRequest
import com.example.kotlin_appquanlycongviec.request.SignUpRequest
import com.example.kotlin_appquanlycongviec.request.Status
import com.example.kotlin_appquanlycongviec.request.Token
import com.example.kotlin_appquanlycongviec.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NguoiDungViewModel @Inject constructor(private val sharedPref: SharedPreferences) :
    ViewModel() {
    private lateinit var token: String
    private var userId: Int = 0
    private lateinit var userEmail: String
    private lateinit var nguoiDungService: NguoiDungApiService
    private lateinit var loginService: LoginApiService
    private val _login = MutableStateFlow<Resource<Token>>(Resource.Unspecified())
    val login = _login.asStateFlow()
    private val _signup = MutableStateFlow<Resource<Status>>(Resource.Unspecified())
    val signup = _signup.asStateFlow()
    private val _user = MutableStateFlow<Resource<NguoiDung>>(Resource.Unspecified())
    val user = _user.asStateFlow()
    private val _otp = MutableStateFlow<Resource<OTP>>(Resource.Unspecified())
    val otp = _otp.asStateFlow()
    private val _emailExist = MutableStateFlow<Resource<Boolean>>(Resource.Unspecified())
    val emailExist = _emailExist.asStateFlow()
//    private val _changePass = MutableStateFlow<Resource<Status>>(Resource.Unspecified())
//    val changePass = _changePass.asStateFlow()


    init {
        initApiService()
    }

    fun initApiService() {
        this.token = sharedPref.getString("token", "").toString()
        this.userId = sharedPref.getInt("userId", 0)
        this.userEmail = sharedPref.getString("userEmail", "").toString()
        var retrofit = ApiInstance.getClient(token)
        nguoiDungService = retrofit.create(NguoiDungApiService::class.java)
        loginService = retrofit.create(LoginApiService::class.java)

    }

    fun getUser() {
        this.userEmail = sharedPref.getString("userEmail", "").toString()
        viewModelScope.launch {
            _user.emit(Resource.Loading())
            val response = nguoiDungService.getUser(userEmail)
            if (response.isSuccessful) {
                _user.emit(Resource.Success(response.body()!!))
                val editor = sharedPref.edit()
                editor.putInt("userId", response.body()!!.maNguoiDung)
                editor.apply()

            } else {
                _user.emit(Resource.Error("Lỗi khi lấy user"))

            }
        }

    }

    fun checkUserEmail(email: String) {
        viewModelScope.launch {
            val response = nguoiDungService.getUser(email)
            if (response.code()==200) {
                _emailExist.emit(Resource.Success(true))

            }
            else{
                _emailExist.emit(Resource.Error("false"))
            }
        }
    }


    fun userLogin(email: String, password: String) {
        val signInRequest = SignInRequest(email, password)
        viewModelScope.launch {
            _login.emit(Resource.Loading())
            val response = loginService.userLogin(signInRequest)
            if (response.isSuccessful) {
                _login.emit(Resource.Success(response.body()!!))
                val editor = sharedPref.edit()
                editor.putString("token", response.body()!!.token)
                editor.putString("userEmail", email)
                editor.apply()
                getUser()

            } else if (response.code() == 404) {
                Log.e("User", "false")
                _login.emit(Resource.Error("Lỗi khi login"))
            }
        }


    }

    fun getOTP(email: String) {
        viewModelScope.launch {
            _otp.emit(Resource.Loading())
            val response = loginService.getOTP(email)
            if (response.isSuccessful) {
                _otp.emit(Resource.Success(response.body()!!))
            } else {
                _otp.emit(Resource.Error("404"))
            }
        }
    }

    fun userSignup(signUpRequest: SignUpRequest) {

        viewModelScope.launch {
            _signup.emit(Resource.Loading())
            val response = loginService.userSignUp(signUpRequest)
            if (response.isSuccessful) {
                _signup.emit(Resource.Success(response.body()!!))

            } else {

                _signup.emit(Resource.Error("Xảy ra lỗi khi đăng kí"))
            }
        }


    }
}