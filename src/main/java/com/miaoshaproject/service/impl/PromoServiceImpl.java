package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.PromoDaoMapper;
import com.miaoshaproject.dataobject.PromoDao;
import com.miaoshaproject.service.PromoService;
import com.miaoshaproject.service.model.PromoModel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PromoServiceImpl implements PromoService {

    @Autowired
    private PromoDaoMapper promoDaoMapper;

    @Override
    public PromoModel getPromoByItemId(Integer itemId) {

        // 获取对应商品的秒杀活动信息
        PromoDao promoDao = promoDaoMapper.selectByItemId(itemId);

        // dataobject -> model
        PromoModel promoModel = convertFromDataObject(promoDao);

        if(promoModel == null){
            return null;
        }
        //判断当前时间是否秒杀活动正在进行或即将进行
        if(promoModel.getStartDate().isAfterNow()){
            promoModel.setStatus(1);
        }else if (promoModel.getEndDate().isBeforeNow()){
            promoModel.setStatus(3);
        }else {
            promoModel.setStatus(2);
        }

        return promoModel;
    }

    private PromoModel convertFromDataObject(PromoDao promoDao){
        if (promoDao == null){
            return null;
        }
        PromoModel promoModel = new PromoModel();
        BeanUtils.copyProperties(promoDao, promoModel);

        // 格式转换
        promoModel.setPromoItemPrice(new BigDecimal(promoDao.getPromoItemPrice()));
        promoModel.setStartDate(new DateTime(promoDao.getStartDate()));
        promoModel.setEndDate(new DateTime(promoDao.getEndDate()));

        return promoModel;
    }
}
