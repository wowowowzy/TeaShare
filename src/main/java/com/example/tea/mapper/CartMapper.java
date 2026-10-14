package com.example.tea.mapper;

import com.example.tea.entity.pojo.Cart.Cart;
import com.example.tea.entity.vo.Cart.CartVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CartMapper {
    void insert(Cart cart);

    void delete(List<Integer> goodsIds,Long userId);

    void update(Cart cart);

    @Select("select * from cart left join goods g on cart.goods_id = g.goods_id where cart.user_id=#{userId}")
    List<CartVO> get(Long userId);

    @Select("select count(*) from cart where user_id=#{userId} and goods_id=#{goodsId}")
    int countExist(Integer goodsId, Long userId);

    @Update("update cart set quantity = quantity + #{quantity}, update_time = now() where user_id=#{userId} and goods_id=#{goodsId}")
    void addQuantity(Integer goodsId, Integer quantity, Long userId);
}
