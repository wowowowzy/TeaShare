package com.example.tea.FunctionCalling;

import com.example.tea.entity.dto.Goods.GoodsShowDTO;
import com.example.tea.entity.dto.Order.OrderPayDTO;
import com.example.tea.entity.dto.Order.OrderToolDTO;
import com.example.tea.entity.pojo.Goods.Goods;
import com.example.tea.mapper.GoodsMapper;
import com.example.tea.service.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TeaTool {
    @Autowired
    private OrderService orderService;
    @Autowired
    private GoodsMapper goodsMapper;
    private enum TeaTypeEnum {
        greenTea,    // 对应 绿茶
        blackTea,    // 对应 红茶
        oolongTea,   // 对应 乌龙茶
        whiteTea,    // 对应 白茶
        puerTea,     // 对应 普洱茶
        yellowTea,    // 对应 黄茶
        others
    }


    @Tool(description = "如果用户意向买茶，请根据用户的需求在枚举当中推荐茶叶类型")
    public String getRecommendTea(TeaTypeEnum teaTypeEnum) {
        List<Goods> tea = null;
        switch (teaTypeEnum) {
            case greenTea -> tea = goodsMapper.getTeaByCategoryId(1L);
            case blackTea -> tea = goodsMapper.getTeaByCategoryId(2L);
            case oolongTea -> tea = goodsMapper.getTeaByCategoryId(3L);
            case whiteTea -> tea = goodsMapper.getTeaByCategoryId(4L);
            case puerTea -> tea = goodsMapper.getTeaByCategoryId(5L);
            case yellowTea -> tea = goodsMapper.getTeaByCategoryId(6L);
            case others -> {
                return "商城内没有符合茶叶，抱歉！";
            }
        }
        List<GoodsShowDTO> list = tea.stream().map(t -> {
            GoodsShowDTO showDTO = new GoodsShowDTO();
            BeanUtils.copyProperties(t, showDTO);
            return showDTO;
        }).toList();
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);

            result.put("recommendTeaList", list); // 直接放商品列表

            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
        } catch (JsonProcessingException e) {
            return "暂无推荐";
        }

    }

    @Tool(description = "用于处理用户的茶叶购买和下单请求,当用户说'买','下单','支付'等词汇时使用此工具,获取要购买的商品完整名字！以及商品数量,完成下单")
    public String pay(OrderToolDTO dto, ToolContext context){
        Long userId = (Long) context.getContext().get("userId");
        Integer goodsId = goodsMapper.getGoodsIdByName(dto.getGoodsName());
        List<OrderPayDTO> payDTOList = new ArrayList<>();
        payDTOList.add(OrderPayDTO.builder().goodsId(goodsId).quantity(dto.getQuantity()).build());
        try {
            orderService.pay(payDTOList, null,userId);
        } catch (Exception e) {
            return "支付失败！"+e.getMessage();
        }
        return "支付成功！";
    }
}
