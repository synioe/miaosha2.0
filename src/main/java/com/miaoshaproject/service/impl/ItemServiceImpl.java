package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.ItemDaoMapper;
import com.miaoshaproject.dao.ItemStockDaoMapper;
import com.miaoshaproject.dao.StockLogDaoMapper;
import com.miaoshaproject.dataobject.ItemDao;
import com.miaoshaproject.dataobject.ItemStockDao;
import com.miaoshaproject.dataobject.StockLogDao;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EnumBussinessError;
import com.miaoshaproject.mq.MqProducer;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.PromoService;
import com.miaoshaproject.service.model.ItemModel;
import com.miaoshaproject.service.model.PromoModel;
import com.miaoshaproject.validator.ValidationResult;
import com.miaoshaproject.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {
    @Autowired
    private ValidatorImpl validator;
    @Autowired
    private ItemDaoMapper itemDaoMapper;
    @Autowired
    private ItemStockDaoMapper itemStockDaoMapper;

    @Autowired
    private PromoService promoService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MqProducer producer;

    @Autowired
    private StockLogDaoMapper stockLogDaoMapper;

    private ItemDao convertItemDaoFromModel(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        }
        ItemDao itemDao = new ItemDao();
        BeanUtils.copyProperties(itemModel, itemDao);
        itemDao.setPrice(itemModel.getPrice().doubleValue());
        return itemDao;
    }

    private ItemStockDao convertItemStockFromItemModel(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        }
        ItemStockDao itemStockDao = new ItemStockDao();
        itemStockDao.setItemId(itemModel.getId());
        itemStockDao.setStock(itemModel.getStock());
        return itemStockDao;
    }

    @Override
    @Transactional
    public ItemModel createItem(ItemModel itemModel) throws BusinessException {
        //校验入参 itemModel
        ValidationResult result = null; // ERROR
        try {
            result = validator.validate(itemModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (result.isHasErrors()) {
            throw new BusinessException(EnumBussinessError.PARAMETER_VALIDATION_ERROR, result.getErrMsg());
        }

        //转化itemmodel->dataobject
        ItemDao itemDao = this.convertItemDaoFromModel(itemModel);

        //写入数据库
        itemDaoMapper.insertSelective(itemDao);

        itemModel.setId(itemDao.getId());

        ItemStockDao itemStockDao = this.convertItemStockFromItemModel(itemModel);
        try {
            itemStockDaoMapper.insertSelective(itemStockDao);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //返回创建完成的对象
        return this.getItemById(itemModel.getId());
    }

    @Override
    public List<ItemModel> listItem() {
        List<ItemDao> itemDaoList = itemDaoMapper.listItem(); // 返回商品查询list
        List<ItemModel> itemModelList = itemDaoList.stream().map(itemDao -> {
            // 通过itemDao的id查询itemStockDao
            ItemStockDao itemStockDao = itemStockDaoMapper.selectByItemId(itemDao.getId());
            // 由itemDao和itemStockDao转换为itemModel并返回
            ItemModel itemModel = this.convertModelFromDataObject(itemDao, itemStockDao);
            return itemModel;
        }).collect(Collectors.toList()); // 转换得到itemModel的list
        return itemModelList;
    }

    @Override
    public ItemModel getItemById(Integer id) {
        ItemDao itemDao = itemDaoMapper.selectByPrimaryKey(id);

        if (itemDao == null) {
            return null;
        }

        //操作获得库存数量
        ItemStockDao itemStockDao = itemStockDaoMapper.selectByItemId(itemDao.getId());

        //将dataobject - > model
        ItemModel itemModel = convertModelFromDataObject(itemDao, itemStockDao);

        //获取活动商品信息
        PromoModel promoModel = promoService.getPromoByItemId(itemModel.getId());
        if (promoModel != null && promoModel.getStatus() != 3) {
            itemModel.setPromoModel(promoModel); // 所谓的模型聚合方式：将秒杀商品和秒杀活动关联起来
        }
        return itemModel;
    }

    @Override
    public ItemModel getItemByIdInCache(Integer id) {
        ItemModel itemModel = (ItemModel) redisTemplate.opsForValue().get("item_validate_" + id);

        if (itemModel == null) {
            itemModel = this.getItemById(id);
            redisTemplate.opsForValue().set("item_validate_" + id, itemModel);
            redisTemplate.expire("item_validate_" + id, 10, TimeUnit.MINUTES);
        }
        return itemModel;
    }

    @Override
    @Transactional
    public Boolean decreaseStock(Integer itemId, Integer amount) throws BusinessException {
//        int affectedRow = itemStockDaoMapper.decreaseStock(itemId, amount);

        long result = redisTemplate.opsForValue().increment("promo_item_stock_" + itemId, amount * -1);

        if (result > 0) {
            // 落库更新成功
//            boolean mqResult = producer.asyncReduceStock(itemId, amount);
//            if (!mqResult) {
//                redisTemplate.opsForValue().increment("promo_item_stock_" + itemId, amount);
//                return false;
//            }
            return true;
        }else if(result ==0){
          //库存已售罄，打上标识
            redisTemplate.opsForValue().set("promo_item_stock_invalid_"+itemId,"true");
            return true;
        } else {
            //更新库存失败
            increaseStock(itemId,amount);
            return false;
        }
    }

    @Override
    public Boolean increaseStock(Integer itemId, Integer amount) throws BusinessException {
        redisTemplate.opsForValue().increment("promo_item_stock_" + itemId, amount);
        return true;
    }

    // 异步减库存
    @Override
    public boolean asyncDecreaseStock(Integer itemId, Integer amount) {
        boolean mqResult = producer.asyncReduceStock(itemId, amount);

        return mqResult;
    }

    @Override
    @Transactional
    public void increaseSales(Integer itemId, Integer amount) throws BusinessException {
        itemDaoMapper.increaseSales(itemId, amount);
    }

    //初始化对应的库存流水
    @Override
    @Transactional
    public String initStockLog(Integer itemId, Integer amount) {
        StockLogDao stockLogDao = new StockLogDao();
        stockLogDao.setAmount(amount);
        stockLogDao.setItemId(itemId);
        stockLogDao.setItemLogId(UUID.randomUUID().toString().replace("-",""));
        stockLogDao.setStatus(1);

        stockLogDaoMapper.insertSelective(stockLogDao);
        return stockLogDao.getItemLogId();
    }

    private ItemModel convertModelFromDataObject(ItemDao itemDao, ItemStockDao itemStockDao) {
        ItemModel itemModel = new ItemModel();

        BeanUtils.copyProperties(itemDao, itemModel);
        itemModel.setPrice(new BigDecimal(itemDao.getPrice()));
        itemModel.setStock(itemStockDao.getStock());
        return itemModel;
    }
}