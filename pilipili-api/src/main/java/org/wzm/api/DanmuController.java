package org.wzm.api;

import org.springframework.web.bind.annotation.RequestParam;
import org.wzm.domain.Danmu;
import org.wzm.model.response.JsonResponse;

import java.text.ParseException;
import java.util.List;

public interface DanmuController {
    public JsonResponse<List<Danmu>> getDanmus(@RequestParam Long videoId, @RequestParam Long startTime,
                                               @RequestParam Long endTime) throws ParseException;
}
