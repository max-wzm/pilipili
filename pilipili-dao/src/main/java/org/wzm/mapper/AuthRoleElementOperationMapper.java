package org.wzm.mapper;

import org.wzm.domain.AuthRoleElementOperation;

import java.util.List;

public interface AuthRoleElementOperationMapper {
    List<AuthRoleElementOperation> listByRoleCode(String roleCode);
}
