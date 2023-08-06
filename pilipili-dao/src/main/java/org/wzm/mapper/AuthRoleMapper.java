package org.wzm.mapper;

import org.wzm.domain.AuthRole;

public interface AuthRoleMapper {
    AuthRole getByCode(String code);
}
