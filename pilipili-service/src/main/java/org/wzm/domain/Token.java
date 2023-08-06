package org.wzm.domain;

import lombok.Data;

@Data
public class Token {
    private String accessToken;
    private String refreshToken;
}
