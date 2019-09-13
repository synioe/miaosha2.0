package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.PromoDaoMapper;
import com.miaoshaproject.dataobject.PromoDao;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.PromoService;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.ItemModel;
import com.miaoshaproject.service.model.PromoModel;
import com.miaoshaproject.service.model.UserModel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class PromoServiceImpl implements PromoService {

    @Autowired
    private PromoDaoMapper promoDaoMapper;

    @Autowired
    private ItemService itemService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    @Override
    public PromoModel getPromoByItemId(Integer itemId) {

        // 获取对应商品的秒杀活动信息
        PromoDao promoDao = promoDaoMapper.selectByItemId(itemId);

        // dataobject -> model
        PromoModel promoModel = convertFromDataObject(promoDao);

        if (promoModel == null) {
            return null;
        }
        //判断当前时间是否秒杀活动正在进行或即将进行
        if (promoModel.getStartDate().isAfterNow()) {
            promoModel.setStatus(1);
        } else if (promoModel.getEndDate().isBeforeNow()) {
            promoModel.setStatus(3);
        } else {
            promoModel.setStatus(2);
        }

        return promoModel;
    }

    @Override
    public void publishPromo(Integer promoId) {
        //通过活动id获取活动
        PromoDao promoDao = promoDaoMapper.selectByPrimaryKey(promoId);
        if (promoDao.getItemId() == null || Integer.valueOf(promoDao.getItemId()) == 0)
            return;
        ItemModel itemModel = itemService.getItemById(Integer.valueOf(promoDao.getItemId()));

        // 库存同步到redis中
        redisTemplate.opsForValue().set("promo_item_stock_" + itemModel.getId(), itemModel.getStock());

        //将秒杀令牌大闸限制数量 (活动商品库存数量的5倍) 存入redis中
        redisTemplate.opsForValue().set("promo_door_count_" + promoId, itemModel.getStock() * 5);
    }

    //生成秒杀令牌
    @Override
    public String generateSecondKillToken(Integer promoId, Integer itemId, Integer userId) {

        //判断是否库存已售罄，若对应的售罄key存在，则直接返回下单失败
        if (redisTemplate.hasKey("promo_item_stock_invalid_" + itemId)) {
            return null;
        }

        PromoDao promoDao = promoDaoMapper.selectByPrimaryKey(promoId);

        // dataobject -> model
        PromoModel promoModel = convertFromDataObject(promoDao);

        if (promoModel == null) {
            return null;
        }
        //判断当前时间是否秒杀活动正在进行或即将进行
        if (promoModel.getStartDate().isAfterNow()) {
            promoModel.setStatus(1);
        } else if (promoModel.getEndDate().isBeforeNow()) {
            promoModel.setStatus(3);
        } else {
            promoModel.setStatus(2);
        }
        //判断活动信息，不存在返回null
        if (promoModel.getStatus() != 2) {
            return null;
        }

        //判断item信息
        ItemModel itemModel = itemService.getItemByIdInCache(itemId);
        if (itemId == null) {
            return null;
        }

        //判断用户信息
        UserModel userModel = userService.getUserByIdInCache(userId);

        if (userModel == null) {
            return null;
        }

        //获取秒杀大闸的限制数量
        long result = redisTemplate.opsForValue().increment("promo_door_count_" + promoId, -1);
        if (result < 0) {
            return null;
        }
        //生成token令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set("promo_token_" + promoId + "_userId_" + userId + "_itemId_" + itemId, token);
        redisTemplate.expire("promo_token_" + promoId + "_userId_" + userId + "_itemId_" + itemId, 5, TimeUnit.MINUTES);

        return token;
    }

    private PromoModel convertFromDataObject(PromoDao promoDao) {
        if (promoDao == null) {
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
