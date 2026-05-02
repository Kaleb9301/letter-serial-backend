package com.bankofabyssinia.letter_serial_backend.service;

import com.bankofabyssinia.letter_serial_backend.dto.Request.LdapLoginRequest;
import com.bankofabyssinia.letter_serial_backend.dto.Response.LdapLoginResponse;

public interface AuthService {

    LdapLoginResponse ldapLogin(LdapLoginRequest request);
}
