package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.AddressBook;
import org.apache.ibatis.annotations.Mapper;

import javax.mail.Address;

@Mapper
public interface AddressBookMapper extends BaseMapper<AddressBook> {
}
