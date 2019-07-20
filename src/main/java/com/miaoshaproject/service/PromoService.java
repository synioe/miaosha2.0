package com.miaoshaproject.service;

import com.miaoshaproject.service.model.PromoModel;

public interface PromoService {
    // 根据itemid获得正在进行或者即将开始的秒杀信息
    PromoModel getPromoByItemId(Integer itemId);
}
