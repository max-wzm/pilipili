package org.wzm.domain;

import lombok.Data;
import org.wzm.domain.AuthRoleElementOperation;
import org.wzm.domain.AuthRoleMenu;

import java.util.List;

@Data
public class UserAuthInfo {
    List<AuthRoleElementOperation> roleElementOperationList;

    List<AuthRoleMenu> roleMenuList;
}
