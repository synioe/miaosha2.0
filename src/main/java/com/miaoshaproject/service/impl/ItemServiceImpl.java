package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.ItemDaoMapper;
import com.miaoshaproject.dao.ItemStockDaoMapper;
import com.miaoshaproject.dataobject.ItemDao;
import com.miaoshaproject.dataobject.ItemStockDao;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EnumBussinessError;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.PromoService;
import com.miaoshaproject.service.model.ItemModel;
import com.miaoshaproject.service.model.PromoModel;
import com.miaoshaproject.validator.ValidationResult;
import com.miaoshaproject.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
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

    private ItemDao convertItemDaoFromModel(ItemModel itemModel){
        if(itemModel == null){
            return null;
        }
        ItemDao itemDao = new ItemDao();
        BeanUtils.copyProperties(itemModel,itemDao);
        itemDao.setPrice(itemModel.getPrice().doubleValue());
        return itemDao;
    }

    private ItemStockDao convertItemStockFromItemModel(ItemModel itemModel){
        if(itemModel == null){
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
        if(result.isHasErrors()){
            throw new BusinessException(EnumBussinessError.PARAMETER_VALIDATION_ERROR,result.getErrMsg());
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
            return  itemModel;
        }).collect(Collectors.toList()); // 转换得到itemModel的list
        return itemModelList;
    }

    @Override
    public ItemModel getItemById(Integer id) {
        ItemDao itemDao = itemDaoMapper.selectByPrimaryKey(id);

        if(itemDao == null){
            return null;
        }

        //操作获得库存数量
        ItemStockDao itemStockDao = itemStockDaoMapper.selectByItemId(itemDao.getId());

        //将dataobject - > model
        ItemModel itemModel = convertModelFromDataObject(itemDao,itemStockDao);

        //获取活动商品信息
        PromoModel promoModel = promoService.getPromoByItemId(itemModel.getId());
        if(promoModel != null && promoModel.getStatus() != 3){
            itemModel.setPromoModel(promoModel); // 所谓的模型聚合方式：将秒杀商品和秒杀活动关联起来
        }
        return itemModel;
    }

    @Override
    @Transactional
    public Boolean decreaseStock(Integer itemId, Integer amount) throws BusinessException {
        int affectedRow = itemStockDaoMapper.decreaseStock(itemId,amount);
        if(affectedRow >0){
            // 落库更新成功
            return true;
        }else {
            return false;
        }
    }

    @Override
    @Transactional
    public void increaseSales(Integer itemId, Integer amount) throws BusinessException {
        itemDaoMapper.increaseSales(itemId, amount);
    }

    private ItemModel convertModelFromDataObject(ItemDao itemDao, ItemStockDao itemStockDao){
        ItemModel itemModel = new ItemModel();

        BeanUtils.copyProperties(itemDao, itemModel);
        itemModel.setPrice(new BigDecimal(itemDao.getPrice()));
        itemModel.setStock(itemStockDao.getStock());
        return itemModel;
    }
}