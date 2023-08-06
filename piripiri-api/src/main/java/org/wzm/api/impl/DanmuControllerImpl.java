package org.wzm.api.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.wzm.api.DanmuController;
import org.wzm.domain.Danmu;
import org.wzm.model.response.JsonResponse;
import org.wzm.service.DanmuService;

import java.text.ParseException;
import java.util.List;

@RestController
public class DanmuControllerImpl implements DanmuController {

    @Autowired
    private DanmuService danmuService;

    @Override
    @GetMapping("/danmus")
    public JsonResponse<List<Danmu>> getDanmus(@RequestParam Long videoId, @RequestParam Long startTime,
                                               @RequestParam Long endTime) throws ParseException {
        List<Danmu> list = danmuService.get(videoId, startTime, endTime);
        return new JsonResponse<>(list);
    }

}
