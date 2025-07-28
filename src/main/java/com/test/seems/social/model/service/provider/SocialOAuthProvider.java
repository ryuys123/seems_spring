package com.test.seems.social.model.service.provider;

import com.test.seems.social.model.dto.SocialUserDto;

public interface SocialOAuthProvider {
    String getAuthorizationUrl();
    SocialUserDto getUserInfo(String code, String state);
}
