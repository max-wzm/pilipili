package org.wzm.user.impl;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class UserMomentsServiceImplTestTest {
    @Test
    public void name() {
        HashMap<Long, Long> bomExistMap = new HashMap<>();
        List<ProduceBomRemoteDTO> bomList = new ArrayList<>();
        ProduceBomRemoteDTO dto = new ProduceBomRemoteDTO();
        dto.bomName = "haaha";
        dto.id = 1L;
        bomList.add(dto);
        String deletedBomNameList = bomList.stream()
                .filter(bom -> !bomExistMap.containsKey(bom.getBomId()))
                .map(ProduceBomRemoteDTO::getBomName).collect(Collectors.joining("、"));
        System.out.println(deletedBomNameList);
    }

    static class ProduceBomRemoteDTO{
        public String bomName;
        public Long id;
        public String getBomName() {
            return bomName;
        }
        public Long getBomId() {
            return id;
        }
    }
}