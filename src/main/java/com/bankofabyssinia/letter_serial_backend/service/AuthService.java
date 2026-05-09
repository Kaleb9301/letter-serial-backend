package com.bankofabyssinia.letter_serial_backend.service;

import com.bankofabyssinia.letter_serial_backend.dto.Request.LdapLoginRequest;
import com.bankofabyssinia.letter_serial_backend.dto.Request.LogOutDto;
import com.bankofabyssinia.letter_serial_backend.dto.Request.RefreshTokenRequest;
import com.bankofabyssinia.letter_serial_backend.dto.Response.LdapLoginResponse;
import com.bankofabyssinia.letter_serial_backend.dto.Response.LogOutResponse;

public interface AuthService {

    LdapLoginResponse ldapLogin(LdapLoginRequest request);

    LdapLoginResponse ldapRefresh(RefreshTokenRequest request);

    LogOutResponse logout(LogOutDto request);
}
